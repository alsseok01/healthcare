package org.hknu.healthcare.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hknu.healthcare.DTO.AiParseResultDto;
import org.hknu.healthcare.DTO.PillDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Service
public class NaturalLanguageService {

    private static final Logger logger = LoggerFactory.getLogger(NaturalLanguageService.class);

    private final ChatClient chatClient;
    private final RestTemplate restTemplate;
    private final PillIdentificationService pillIdentificationService; // 'e약은요' API 호출기 (재사용)

    @Value("${api.identification.key}")
    private String identificationApiKey;

    @Value("${api.identification.url}")
    private String identificationApiUrl;

    @Autowired
    public NaturalLanguageService(ChatClient chatClient, RestTemplate restTemplate, PillIdentificationService pillIdentificationService) {
        this.chatClient = chatClient;
        this.restTemplate = restTemplate;
        this.pillIdentificationService = pillIdentificationService;
    }

    public PillDto searchByDescription(String description) throws Exception {

        AiParseResultDto parsedResult = parseDescriptionWithAi(description);
        if (parsedResult == null || parsedResult.getImprint() == null) {
            logger.warn("Gemini AI가 묘사에서 각인을 추출하지 못했습니다.");
            return null;
        }

        String itemName = callIdentificationApi(parsedResult);
        if (itemName == null) {
            logger.warn("낱알식별 API가 '" + parsedResult.getImprint() + "'에 해당하는 itemName을 찾지 못했습니다.");
            return null;
        }

        logger.info("최종 검색된 itemName: {}", itemName);
        return pillIdentificationService.callPublicApi(itemName);
    }


    private AiParseResultDto parseDescriptionWithAi(String description) {
        logger.info("Gemini AI에 묘사 분석 요청: {}", description);

        String template = """
            사용자의 알약 묘사: "{description}"
            위 묘사에서 '모양(shape)', '색상(color)', '각인(imprint)' 정보를 추출해줘.
            정보가 없으면 null로 처리해.
            반드시 아래 JSON 형식으로만 응답해줘. 다른 말은 절대 덧붙이지 마.

            {
              "shape": "추출한 모양",
              "color": "추출한 색상",
              "imprint": "추출한 각인"
            }
            """;

        BeanOutputConverter<AiParseResultDto> outputConverter =
                new BeanOutputConverter<>(AiParseResultDto.class);

        String promptMessage = template.replace("{description}", description);

        // 프롬프트에 출력 형식 정보를 추가
        promptMessage += "\n" + outputConverter.getFormat();

        try {
            Prompt prompt = new Prompt(promptMessage);

            String aiResponse = chatClient.prompt()
                        .user(promptMessage)
                        .call()
                        .content();


            logger.info("Gemini AI 응답: {}", aiResponse);
            return outputConverter.convert(aiResponse); // 안전한 파싱

        } catch (Exception e) {
            logger.error("Gemini AI 응답 DTO 변환 실패: {}", e.getMessage());
            return null;
        }
    }

    private String callIdentificationApi(AiParseResultDto result) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(identificationApiUrl)
                .queryParam("serviceKey", identificationApiKey)
                .queryParam("print_front", result.getImprint()) // AI가 추출한 각인
                .queryParam("type", "json")
                .queryParam("numOfRows", 1); // 가장 정확한 1개만

        if (result.getColor() != null && !result.getColor().isBlank()) {
            uriBuilder.queryParam("color_class1", result.getColor());
        }
        if (result.getShape() != null && !result.getShape().isBlank()) {
            uriBuilder.queryParam("shape", result.getShape());
        }

        URI uri = uriBuilder.encode(StandardCharsets.UTF_8).build().toUri();
        logger.info("낱알식별 API 요청 URI: {}", uri);

        try {
            String responseString = restTemplate.getForObject(uri, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseString);
            JsonNode body = root.path("body");

            if (body.path("totalCount").asInt() == 0) {
                return null;
            }

            // "itemName" 필드 추출
            return body.path("items").get(0).path("ITEM_NAME").asText(null);

        } catch (Exception e) {
            logger.error("낱알식별 API 호출 실패: {}", e.getMessage());
            return null;
        }
    }
}