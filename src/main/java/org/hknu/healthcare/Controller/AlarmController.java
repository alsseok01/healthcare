package org.hknu.healthcare.Controller;

import org.hknu.healthcare.DTO.AlarmCreateRequestDto;
import org.hknu.healthcare.Service.AlarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alarm")
public class AlarmController {

    @Autowired
    private AlarmService alarmService;

    @PostMapping("/create")
    public ResponseEntity<?> createAlarm(@RequestBody AlarmCreateRequestDto request) {
        try {
            // 현재 로그인한 유저 ID (예시로 1L 사용, 실제론 SecurityContext 등에서 추출)
            Long currentUserId = 1L;

            alarmService.createAlarm(currentUserId, request);

            return ResponseEntity.ok("알람이 성공적으로 생성되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("알람 생성 실패: " + e.getMessage());
        }
    }
}