package org.hknu.healthcare.Serivce;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class OcrService {
    /**
     * Google Cloud Vision API를 호출하여 이미지에서 텍스트를 추출합니다.
     */
    public String extractTextFromImage(MultipartFile imageFile) throws IOException {
        // 1. ImageAnnotatorClient 인스턴스 생성 (인증은 환경변수로 자동 처리됨)
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {

            // 2. MultipartFile을 ByteString으로 변환
            ByteString imgBytes = ByteString.copyFrom(imageFile.getBytes());
            Image img = Image.newBuilder().setContent(imgBytes).build();

            // 3. 텍스트 감지(TEXT_DETECTION) 기능 요청
            Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(img)
                    .build();

            // 4. API 요청 및 응답 받기
            List<AnnotateImageRequest> requests = new ArrayList<>();
            requests.add(request);
            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);

            // 5. 응답 결과 파싱
            List<AnnotateImageResponse> responses = response.getResponsesList();
            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.err.println("Error: " + res.getError().getMessage());
                    throw new RuntimeException("Google Vision API Error: " + res.getError().getMessage());
                }

                // 텍스트 감지 결과가 있으면 첫 번째 결과(전체 텍스트)를 반환
                if (!res.getTextAnnotationsList().isEmpty()) {
                    // textAnnotations(0)은 항상 이미지에서 감지된 전체 텍스트 블록입니다.
                    return res.getTextAnnotationsList().get(0).getDescription();
                }
            }
            // 감지된 텍스트가 없는 경우
            return null;
        }
    }
}
