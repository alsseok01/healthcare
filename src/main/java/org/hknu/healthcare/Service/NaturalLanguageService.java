package org.hknu.healthcare.Service;

import com.fasterxml.jackson.databind.JsonNode;
import org.hknu.healthcare.DTO.AiParseResultDto;
import org.hknu.healthcare.DTO.PillDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NaturalLanguageService {

    private static final Logger logger = LoggerFactory.getLogger(NaturalLanguageService.class);
    private final ChatClient chatClient;
    private final PillIdentificationService pillIdentificationService;

    @Autowired
    public NaturalLanguageService(ChatClient chatClient, PillIdentificationService pillIdentificationService) {
        this.chatClient = chatClient;
        this.pillIdentificationService = pillIdentificationService;
    }

    public PillDto searchByDescription(String description) {
        // [단계 1] AI: 묘사를 분석하여 '추정 업체명'과 '특징' 추출
        AiParseResultDto aiResult = extractFeaturesWithAi(description);

        if (aiResult == null || aiResult.getCompany() == null) {
            logger.warn("AI가 업체명을 추론하지 못했습니다.");
            return null;
        }

        logger.info("AI 추론 - 업체: {}, 앞: {}, 뒤: {}, 색: {}",
                aiResult.getCompany(), aiResult.getFront_imprint(), aiResult.getBack_imprint(), aiResult.getColor());

        // [단계 2] API 호출: 업체명으로 낱알식별 정보 리스트(원본 JSON) 조회
        JsonNode apiItems = pillIdentificationService.searchRawPillsByCompany(aiResult.getCompany());

        if (apiItems == null || apiItems.isEmpty()) {
            logger.warn("해당 업체의 알약 정보를 찾을 수 없습니다.");
            return null;
        }

        // [단계 3] 필터링: API 리스트 중에서 사용자 묘사(각인, 색상)와 일치하는 알약 찾기
        JsonNode matchedItem = findBestMatch(apiItems, aiResult);

        if (matchedItem != null) {
            String itemName = matchedItem.path("ITEM_NAME").asText();
            logger.info("최종 매칭 성공: {}", itemName);

            // [단계 4] 매칭된 약 '이름'을 가지고 AI에게 상세 정보(효능, 복용법 등) 생성 요청
            PillDto aiGeneratedDetail = generatePillDetailsWithAi(itemName);

            if (aiGeneratedDetail != null) {
                // 낱알식별 API에서 가져온 정확한 이미지 URL을 덮어씌움 (AI는 이미지를 못 주므로)
                aiGeneratedDetail.setItemImage(matchedItem.path("ITEM_IMAGE").asText(null));
                // 이름도 API 데이터로 명확히 설정
                aiGeneratedDetail.setItemName(itemName);
                return aiGeneratedDetail;
            } else {
                // AI 생성 실패 시 기본 정보만 반환 (비상용)
                PillDto fallback = new PillDto();
                fallback.setItemName(itemName);
                fallback.setItemImage(matchedItem.path("ITEM_IMAGE").asText(null));
                fallback.setEfcyQesitm("상세 정보를 생성하지 못했습니다.");
                return fallback;
            }

        } else {
            logger.info("일치하는 알약을 찾지 못했습니다.");
            return null;
        }
    }

    // [보조 메서드 1] 묘사에서 특징 추출 (AI)
    private AiParseResultDto extractFeaturesWithAi(String description) {
        BeanOutputConverter<AiParseResultDto> converter = new BeanOutputConverter<>(AiParseResultDto.class);

        String template = """
            사용자의 묘사: "{description}"
            
            1. 이 묘사에 가장 부합하는 알약의 '제조사(업체명)'를 추론해줘. (예: 한미약품, 종근당)
            2. 알약의 앞면 각인, 뒷면 각인, 색상, 모양 정보를 추출해줘.
            
            반드시 아래 JSON 형식으로만 응답해줘.
            {
              "company": "추론한 업체명",
              "front_imprint": "앞면 글자(없으면 null)",
              "back_imprint": "뒷면 글자(없으면 null)",
              "color": "색상(예: 하양, 분홍)",
              "shape": "모양"
            }
            """;

        try {
            String prompt = template.replace("{description}", description);
            String response = chatClient.prompt().user(prompt).call().content();
            return converter.convert(response);
        } catch (Exception e) {
            logger.error("AI 특징 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    // [보조 메서드 2] 약 이름으로 상세 정보 생성 (AI)
    private PillDto generatePillDetailsWithAi(String itemName) {
        logger.info("AI에게 상세 정보 생성 요청: {}", itemName);

        BeanOutputConverter<PillDto> converter = new BeanOutputConverter<>(PillDto.class);

        String template = """
            약 이름: "{itemName}"
            
            위 약에 대한 정보를 의학적 지식을 바탕으로 상세히 알려줘.
            다음 필드를 포함한 JSON 형식으로 응답해줘:
            
            - efcyQesitm: 효능 및 효과 (한글로 간단히 요약)
            - useMethodQesitm: 용법 및 용량 (한글로 간단히 요약)
            - atpnWarnQesitm: 사용상 주의사항 및 부작용 (한글로 간단히 요약)
            
            (itemName은 "{itemName}" 그대로 사용하고, 이미지는 null로 둬)
            """;

        // 포맷 가이드 추가 (중요)
        String prompt = template.replace("{itemName}", itemName) + "\n" + converter.getFormat();

        try {
            String response = chatClient.prompt().user(prompt).call().content();
            return converter.convert(response);
        } catch (Exception e) {
            logger.error("AI 상세 정보 생성 실패: {}", e.getMessage());
            return null;
        }
    }

    // [보조 메서드 3] 필터링 로직 (Java)
    private JsonNode findBestMatch(JsonNode items, AiParseResultDto aiResult) {
        for (JsonNode item : items) {
            String apiFront = item.path("PRINT_FRONT").asText("").trim();
            String apiBack = item.path("PRINT_BACK").asText("").trim();
            String apiColor = item.path("COLOR_CLASS1").asText("");

            String userFront = aiResult.getFront_imprint();
            String userBack = aiResult.getBack_imprint();
            String userColor = aiResult.getColor();

            // 1. 앞면 각인 비교
            boolean frontMatch = true;
            if (userFront != null && !userFront.isBlank()) {
                frontMatch = apiFront.contains(userFront);
            }

            // 2. 뒷면 각인 비교
            boolean backMatch = true;
            if (userBack != null && !userBack.isBlank()) {
                backMatch = apiBack.contains(userBack);
            }

            // 3. 색상 비교
            boolean colorMatch = true;
            if (userColor != null && !userColor.isBlank()) {
                colorMatch = apiColor.contains(userColor);
            }

            // 모든 조건 만족 시 반환
            if (frontMatch && backMatch && colorMatch) {
                return item;
            }
        }
        return null;
    }
}