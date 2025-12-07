package org.hknu.healthcare.Controller;

import org.hknu.healthcare.DTO.AlarmCreateRequestDto;
import org.hknu.healthcare.DTO.RoutineDto;
import org.hknu.healthcare.Service.AlarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/list")
    public ResponseEntity<?> getMyRoutines() {
        try {
            Long currentUserId = 1L; // 임시 ID
            List<RoutineDto> routines = alarmService.getMyRoutines(currentUserId);
            return ResponseEntity.ok(routines);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("목록 조회 실패: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoutine(@PathVariable Long id, @RequestBody RoutineDto request) {
        try {
            alarmService.updateRoutine(id, request);
            return ResponseEntity.ok("루틴이 수정되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("수정 실패: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoutine(@PathVariable Long id) {
        try {
            alarmService.deleteRoutine(id);
            return ResponseEntity.ok("루틴이 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("삭제 실패: " + e.getMessage());
        }
    }

    @PostMapping("/schedule")
    public ResponseEntity<?> createSimpleSchedule(@RequestBody Map<String, String> request) {
        try {
            Long currentUserId = 1L; // 임시 유저 ID
            String title = request.get("title");
            String type = request.get("type");

            if (title == null || title.isEmpty()) {
                return ResponseEntity.badRequest().body("제목(title)이 필요합니다.");
            }

            System.out.println("일정 생성 요청 들어옴: " + title); // 로그 확인용

            alarmService.createSimpleSchedule(currentUserId, title, type);

            return ResponseEntity.ok("일정이 생성되었습니다.");
        } catch (Exception e) {
            e.printStackTrace(); // 에러 발생 시 콘솔에 출력
            return ResponseEntity.status(500).body("일정 생성 실패: " + e.getMessage());
        }
    }
}