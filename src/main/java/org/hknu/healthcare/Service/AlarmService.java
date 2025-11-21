package org.hknu.healthcare.Service;

import org.hknu.healthcare.DTO.AlarmCreateRequestDto;
import org.hknu.healthcare.entity.*;
import org.hknu.healthcare.Repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlarmService {

    @Autowired private PrescriptionRepository prescriptionRepository;
    @Autowired private IntakeReminderRepository reminderRepository;
    @Autowired private PillRepository pillRepository;
    @Autowired private PersonProfileRepository profileRepository;

    @Transactional
    public void createAlarm(Long userId, AlarmCreateRequestDto request) {
        PersonProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        LocalDate start = LocalDate.parse(request.getStartDate());
        LocalDate end = start.plusDays(request.getDurationDays());
        List<LocalTime> calculatedTimes = calculateTimes(request.getFrequencyHours());

        if (request.isGrouped()) {
            // [묶음 알림] 제목을 사용자 입력(request.getTitle())로 통일
            for (String pillName : request.getPillNames()) {
                createPrescriptionAndReminders(profile, pillName, start, end, calculatedTimes, request, request.getTitle());
            }
        } else {
            // [개별 알림] 제목을 '약 이름'으로 설정
            for (String pillName : request.getPillNames()) {
                createPrescriptionAndReminders(profile, pillName, start, end, calculatedTimes, request, pillName + " 복용");
            }
        }
    }

    private void createPrescriptionAndReminders(PersonProfile profile, String pillName, LocalDate start, LocalDate end,
                                                List<LocalTime> times, AlarmCreateRequestDto request, String title) {

        // [핵심] 약 이름으로 DB 조회 -> 없으면 '직접 입력'으로 간주하고 자동 생성
        Pill pill = pillRepository.findByName(pillName);
        if (pill == null) {
            pill = new Pill();
            pill.setName(pillName);
            pill.setDescription("사용자 직접 입력 또는 자동 생성된 약");
            pill = pillRepository.save(pill); // 새 약 저장
        }

        // 1. 처방전(Prescription) 생성 (복용 기간 및 메모 저장)
        Prescription prescription = new Prescription();
        prescription.setProfile(profile);
        prescription.setPill(pill);
        prescription.setStartDate(start);
        prescription.setEndDate(end);
        prescription.setDirections(title); // 알람 제목을 여기에 저장
        prescriptionRepository.save(prescription);

        // 2. 알림(IntakeReminder) 생성 (설정된 시간마다 알림 생성)
        for (LocalTime time : times) {
            IntakeReminder reminder = new IntakeReminder();
            reminder.setPrescription(prescription);
            reminder.setRemindAt(time);

            // 요일 설정 적용
            reminder.setMon(request.isMon());
            reminder.setTue(request.isTue());
            reminder.setWed(request.isWed());
            reminder.setThu(request.isThu());
            reminder.setFri(request.isFri());
            reminder.setSat(request.isSat());
            reminder.setSun(request.isSun());

            reminder.setValidFrom(start);
            reminder.setValidUntil(end);

            reminderRepository.save(reminder);
        }
    }

    private List<LocalTime> calculateTimes(int frequencyHours) {
        List<LocalTime> times = new ArrayList<>();
        int startHour = 8; // 기본 시작 시간 (필요 시 DTO에서 받을 수 있음)

        // 24시간을 넘지 않는 범위에서 시간 추가
        for (int hour = startHour; hour < 24; hour += frequencyHours) {
            times.add(LocalTime.of(hour, 0));
        }
        return times;
    }

    @Transactional
    public void archiveFinishedAlarms() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 종료일이 지난 처방전 조회 (쿼리 예시)
        List<Prescription> finishedPrescriptions = prescriptionRepository.findByEndDateBeforeAndDeletedFalse(LocalDate.now());

        for (Prescription p : finishedPrescriptions) {
            // 1. 히스토리 엔티티로 저장
            MedicationHistory history = new MedicationHistory();
            history.setUser(p.getProfile().getUser()); // 프로필을 통해 유저 추적
            history.setPillName(p.getPill().getName());
            history.setStartDate(p.getStartDate());
            history.setEndDate(p.getEndDate());

            medicationHistoryRepository.save(history);

            // 2. 기존 데이터는 삭제 처리 (Soft Delete) 혹은 상태 변경
            p.setDeleted(true);
            // 연관된 Reminder들도 비활성화 처리 로직 추가
        }
}