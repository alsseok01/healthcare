package org.hknu.healthcare.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pill")
public class PillController {

    @Autowired
    private PillIdentificationService pillIdentificationService;

    @PostMapping("/identify")
    public ResponseEntity<?> identifyPill(@RequestParam("image") MultipartFile image) {
        try {
            // 이미지 파일을 받아 알약 식별 서비스 호출
            PillDto pillInfo = pillIdentificationService.identifyPillFromImage(image);

            if (pillInfo == null) {
                return ResponseEntity.status(404).body("일치하는 알약 정보를 찾을 수 없습니다.");
            }
            return ResponseEntity.ok(pillInfo); // React Native로 알약 정보(JSON) 반환

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("서버 오류 발생: " + e.getMessage());
        }
    }
}

// 간단한 DTO 예시 (React Native로 반환할 형태)
class PillDto {
    private String pillName;
    private String effect;
    // ... 기타 정보

    // Getters and Setters
}