package org.hknu.healthcare.Service;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Service
public class OcrService {
    /**
     * Google Cloud Vision API를 호출하여 이미지에서 텍스트를 추출합니다.
     */
    public String extractTextFromImage(MultipartFile imageFile) throws IOException {
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {

            ByteString imgBytes = ByteString.copyFrom(imageFile.getBytes());
            Image img = Image.newBuilder().setContent(imgBytes).build();

            Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(img)
                    .build();

            List<AnnotateImageRequest> requests = new ArrayList<>();
            requests.add(request);
            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);

            List<AnnotateImageResponse> responses = response.getResponsesList();
            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.err.println("Error: " + res.getError().getMessage());
                    throw new RuntimeException("Google Vision API Error: " + res.getError().getMessage());
                }

                if (!res.getTextAnnotationsList().isEmpty()) {
                    return res.getTextAnnotationsList().get(0).getDescription();
                }
            }
            return null;
        }
    }
}
