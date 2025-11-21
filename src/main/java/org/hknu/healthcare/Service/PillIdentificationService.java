package org.hknu.healthcare.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hknu.healthcare.DTO.PillDto;
import org.hknu.healthcare.util.HtmlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PillIdentificationService {

    private static final Logger logger = LoggerFactory.getLogger(PillIdentificationService.class);

    @Autowired
    private OcrService ocrService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    @Lazy
    private NaturalLanguageService naturalLanguageService;

    @Value("${api.public.key}")
    private String publicApiKey;

    @Value("${api.public.url}")
    private String publicApiUrl;

    @Value("${api.identification.url}")
    private String identificationApiUrl;

    /**
     * [기능 1] 텍스트(약 이름)로 약물 정보 검색
     */
    public PillDto searchPillByName(String pillName) throws Exception {
        if (pillName == null || pillName.trim().isEmpty()) {
            throw new IllegalArgumentException("검색할 약 이름이 비어있습니다.");
        }
        return callPublicApi(pillName.trim());
    }


    //ai 기반으로 업체명 추청
    public JsonNode searchRawPillsByCompany(String companyName) {
        if (companyName == null || companyName.isBlank()) {
            return null;
        }

        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(identificationApiUrl)
                    .queryParam("serviceKey", publicApiKey)
                    .queryParam("ENTP_NAME", companyName)
                    .queryParam("type", "json")
                    .queryParam("numOfRows", 50);

            URI uri = uriBuilder.encode(StandardCharsets.UTF_8).build().toUri();
            logger.info("업체명 검색 API 요청: {}", uri);

            String responseString = restTemplate.getForObject(uri, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseString);
            JsonNode body = root.path("body");

            if (body.path("totalCount").asInt() == 0) {
                return null;
            }

            // 여기서 반환하는 body.path("items")는 JsonNode 타입입니다.
            // 따라서 이 메서드의 반환 타입도 JsonNode여야 합니다.
            return body.path("items");

        } catch (Exception e) {
            logger.error("업체명 검색 실패: {}", e.getMessage());
            return null;
        }
    }


    /**
     * [기능 2] 이미지를 처리하여 약물 정보 목록 반환
     */
    public List<PillDto> analyzeImageForPills(MultipartFile imageFile) throws Exception {
        String fullOcrText = ocrService.extractTextFromImage(imageFile);
        if (fullOcrText == null || fullOcrText.isEmpty()) {
            logger.warn("OCR 텍스트 추출 실패");
            return new ArrayList<>();
        }

        logger.info("=== OCR 추출 텍스트 ===\n{}", fullOcrText);

        List<String> drugNames = naturalLanguageService.extractDrugNamesFromOcr(fullOcrText);

        logger.info("=== AI가 추출한 약 이름 후보 ===\n{}", drugNames);

        if (drugNames.isEmpty()) {
            return new ArrayList<>();
        }

        List<PillDto> results = new ArrayList<>();
        for (String pillName : drugNames) {
            try {
                PillDto info = naturalLanguageService.searchByNameWithAiFallback(pillName);
                if (info != null) {
                    results.add(info);
                }
                Thread.sleep(100); // API 과부하 방지

            } catch (Exception e) {
                logger.error("'{}' 정보 조회 실패: {}", pillName, e.getMessage());
            }
        }
        return results;
    }

    /**
     * [파싱 로직] OCR 텍스트를 파싱하여 약 이름 목록 추출
     */
    private List<String> parseTextForDrugNames(String fullOcrText) {
        Set<String> candidates = new HashSet<>();
        String[] lines = fullOcrText.split("\\r?\\n");

        Pattern pattern = Pattern.compile("^\\d*\\.?\\s*([^\\s(]+(정|캡슐|시럽|펜|밀리그람|그램|mg|g))", Pattern.CASE_INSENSITIVE);

        for (String line : lines) {
            String trimmedLine = line.trim();

            if (trimmedLine.contains("식후") || trimmedLine.contains("식전") ||
                    trimmedLine.contains("1일") || trimmedLine.contains("환자") ||
                    trimmedLine.contains("병원") || trimmedLine.contains("용법")) {
                continue;
            }

            Matcher matcher = pattern.matcher(trimmedLine);
            if (matcher.find()) {
                String potentialDrugName = matcher.group(1);
                potentialDrugName = potentialDrugName.split("(?i)(mg|g)")[0];
                candidates.add(potentialDrugName.trim());
            }
        }
        return new ArrayList<>();
    }

    /**
     * [공통 로직] 공공 API 호출
     */
    public PillDto callPublicApi(String pillName) throws Exception {
        URI uri = UriComponentsBuilder.fromUriString(publicApiUrl)
                .queryParam("serviceKey", publicApiKey)
                .queryParam("itemName", pillName)
                .queryParam("type", "json")
                .queryParam("numOfRows", 1)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        String responseString = restTemplate.getForObject(uri, String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseString);
        JsonNode body = root.path("body");

        if (body.path("totalCount").asInt() == 0) {
            return null;
        }

        JsonNode items = body.path("items");
        if (items.isMissingNode() || !items.isArray() || items.size() == 0) {
            return null;
        }

        JsonNode firstItem = items.get(0);
        return new PillDto(
                HtmlUtil.stripHtml(firstItem.path("itemName").asText(null)),
                HtmlUtil.stripHtml(firstItem.path("efcyQesitm").asText(null)),
                HtmlUtil.stripHtml(firstItem.path("useMethodQesitm").asText(null)),
                HtmlUtil.stripHtml(firstItem.path("atpnWarnQesitm").asText(null)),
                firstItem.path("itemImage").asText(null) // 이미지 URL 추가 처리
        );
    }
}