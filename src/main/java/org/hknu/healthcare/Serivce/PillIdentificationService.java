package org.hknu.healthcare.Serivce;

import org.hknu.healthcare.Controller.PillDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PillIdentificationService {

    @Autowired
    private OcrService ocrService; // 1.3에서 만든 Google Vision API 서비스

    // 공공 API 키 (application.properties 등에서 주입)
    @Value("${api.public.key}")
    private String publicApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 이미지 파일을 받아 OCR -> 텍스트 파싱 -> 공공 API 호출을 총괄
     */
    public PillDto identifyPillFromImage(MultipartFile imageFile) throws Exception {

        // 1. Google Vision API로 텍스트 추출
        String ocrResult = ocrService.extractTextFromImage(imageFile);
        if (ocrResult == null || ocrResult.isEmpty()) {
            throw new Exception("이미지에서 텍스트를 추출할 수 없습니다.");
        }

        // 2. OCR 텍스트 파싱 (매우 중요)
        // 예: "ABC\n100" -> "ABC 100" 또는 "ABC", "100"
        // 이 부분은 OCR 결과와 공공 API가 요구하는 형식에 맞춰 정교하게 만들어야 합니다.
        String parsedText = parseOcrText(ocrResult);

        // 3. 공공 API 호출하여 알약 정보 매핑
        // 예시: 공공 API 엔드포인트
        String apiUrl = "http://public.api.url/getPillInfo?key=" + publicApiKey + "&text=" + parsedText;

        // restTemplate.getForObject(...) 등으로 API 호출
        // ... (호출 및 결과 매핑 로직) ...

        // 4. 공공 API 결과를 PillDto로 변환하여 반환
        PillDto resultDto = new PillDto();
        // resultDto.setPillName( ... );
        // resultDto.setEffect( ... );

        return resultDto;
    }

    /**
     * OCR 결과를 공공 API 쿼리에 맞게 파싱하는 로직
     */
    private String parseOcrText(String ocrText) {
        // 예시: 줄바꿈을 공백으로 바꾸고, 특수문자 제거 등
        return ocrText.replaceAll("\\s+", " ").trim();
        // 실제로는 더 복잡한 파싱 로직 (예: 정규식 사용)이 필요할 수 있습니다.
    }
}