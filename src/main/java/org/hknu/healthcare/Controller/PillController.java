package org.hknu.healthcare.Controller;

import org.hknu.healthcare.DTO.PillDto;
import org.hknu.healthcare.Service.PillIdentificationService;
import org.hknu.healthcare.Service.NaturalLanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/pill")
public class PillController {

    @Autowired
    private PillIdentificationService pillIdentificationService;

    @Autowired
    private NaturalLanguageService naturalLanguageService;

    /**
     * [기능 1] 텍스트로 알약 검색 (GET)
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchPillByText(@RequestParam("name") String pillName) {
        try {
            PillDto pillInfo = pillIdentificationService.searchPillByName(pillName);
            if (pillInfo == null) {
                return ResponseEntity.status(404).body("'" + pillName + "'과(와) 일치하는 알약 정보를 찾을 수 없습니다.");
            }
            return ResponseEntity.ok(pillInfo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("서버 오류(텍스트 검색): " + e.getMessage());
        }
    }

    /**
     * [기능 2] 처방전/설명서 이미지 스캔 (POST)
     */
    @PostMapping("/analyze-image")
    public ResponseEntity<?> analyzeImageForPills(@RequestParam("image") MultipartFile image) {
        if (image.isEmpty()) {
            return ResponseEntity.badRequest().body("이미지 파일이 비어있습니다.");
        }
        try {
            List<PillDto> pillList = pillIdentificationService.analyzeImageForPills(image);
            if (pillList == null || pillList.isEmpty()) {
                return ResponseEntity.status(404).body("이미지에서 유효한 약품 정보를 찾을 수 없습니다.");
            }
            return ResponseEntity.ok(pillList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("서버 오류(이미지 분석): " + e.getMessage());
        }
    }

    /**
     * [기능 3] 자연어(묘사)로 알약 검색 (GET) (새로 추가된 엔드포인트)
     */
    @GetMapping("/search-by-description")
    public ResponseEntity<?> searchByDescription(@RequestParam("q") String description) {
        try {
            PillDto pillInfo = naturalLanguageService.searchByDescription(description);
            if (pillInfo == null) {
                return ResponseEntity.status(404).body("'" + description + "' 묘사와 일치하는 알약 정보를 찾을 수 없습니다.");
            }
            return ResponseEntity.ok(pillInfo); // 최종 PillDto 반환

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("서버 오류(묘사 검색): " + e.getMessage());
        }
    }
}