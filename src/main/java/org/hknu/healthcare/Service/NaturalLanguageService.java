package org.hknu.healthcare.Service;

import com.fasterxml.jackson.databind.JsonNode;
import org.hknu.healthcare.DTO.AiOcrResultDto;
import org.hknu.healthcare.DTO.AiParseResultDto;
import org.hknu.healthcare.DTO.PillDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

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

    public PillDto searchByNameWithAiFallback(String name) {
        try {
            PillDto apiResult = pillIdentificationService.searchPillByName(name);
            if (apiResult != null) {
                logger.info("'{}' 정보 공공 API에서 발견됨.", name);
                return apiResult;
            }
        } catch (Exception e) {
            logger.warn("공공 API 검색 중 오류 발생 (AI로 전환): {}", e.getMessage());
        }

        logger.info("'{}' 정보 공공 API에 없음. AI 생성 시작.", name);
        PillDto aiResult = generatePillDetailsWithAi(name);

        if (aiResult != null) {
            aiResult.setItemImage(null);
            aiResult.setItemName(name);
        }

        return aiResult;
    }

    public List<String> extractDrugNamesFromOcr(String ocrText) {
        logger.info("AI에게 OCR 텍스트 파싱 요청");

        BeanOutputConverter<AiOcrResultDto> converter = new BeanOutputConverter<>(AiOcrResultDto.class);

        String template = """
            아래는 처방전이나 약 봉투에서 추출한 OCR 텍스트야.
            여기서 '약품명(약 이름)'만 정확하게 추출해서 리스트로 만들어줘.
            
            [OCR 텍스트]
            "{ocrText}"
            
            [조건]
            1. '소염진통제', '위장약', '식후 30분' 같은 일반 단어나 복용법은 제외해.
            2. 약 이름에 용량(mg, g)이 붙어있다면 그대로 포함해. (예: 타이레놀정500mg)
            3. 약 이름 뒤에 괄호나 특수문자가 붙어있으면 제거하고 약 이름만 추출해.
            
            반드시 아래 JSON 형식으로 응답해줘.
            {
              "drugNames": ["약이름1", "약이름2", ...]
            }
            """;

        try {
            String prompt = template.replace("{ocrText}", ocrText) + "\n" + converter.getFormat();
            String response = chatClient.prompt().user(prompt).call().content();
            AiOcrResultDto result = converter.convert(response);

            return result != null ? result.getDrugNames() : Collections.emptyList();
        } catch (Exception e) {
            logger.error("AI OCR 파싱 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public PillDto searchByDescription(String description) {
        AiParseResultDto aiResult = extractFeaturesWithAi(description);

        if (aiResult == null || aiResult.getCompany() == null) {
            logger.warn("AI가 업체명을 추론하지 못했습니다.");
            return null;
        }

        logger.info("AI 추론 - 업체: {}, 앞: {}, 뒤: {}, 색: {}",
                aiResult.getCompany(), aiResult.getFront_imprint(), aiResult.getBack_imprint(), aiResult.getColor());

        JsonNode apiItems = pillIdentificationService.searchRawPillsByCompany(aiResult.getCompany());

        if (apiItems == null || apiItems.isEmpty()) {
            logger.warn("해당 업체의 알약 정보를 찾을 수 없습니다.");
            return null;
        }

        JsonNode matchedItem = findBestMatch(apiItems, aiResult);

        if (matchedItem != null) {
            String itemName = matchedItem.path("ITEM_NAME").asText();
            logger.info("최종 매칭 성공: {}", itemName);

            PillDto aiGeneratedDetail = generatePillDetailsWithAi(itemName);

            if (aiGeneratedDetail != null) {
                aiGeneratedDetail.setItemImage(matchedItem.path("ITEM_IMAGE").asText(null));
                aiGeneratedDetail.setItemName(itemName);
                return aiGeneratedDetail;
            } else {
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

    private AiParseResultDto extractFeaturesWithAi(String description) {
        BeanOutputConverter<AiParseResultDto> converter = new BeanOutputConverter<>(AiParseResultDto.class);

        String template = """
            사용자의 묘사: "{description}"
            
            1. 이 묘사에 가장 부합하는 알약의 '업체명(제조사)'를 알려줘. (예: 한미약품, 종근당)
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