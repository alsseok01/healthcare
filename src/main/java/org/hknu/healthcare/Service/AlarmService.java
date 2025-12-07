package org.hknu.healthcare.Service;

import org.hknu.healthcare.DTO.AlarmCreateRequestDto;
import org.hknu.healthcare.DTO.RoutineDto;
import org.hknu.healthcare.entity.*;
import org.hknu.healthcare.Repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlarmService {

    @Autowired private PrescriptionRepository prescriptionRepository;
    @Autowired private IntakeReminderRepository reminderRepository;
    @Autowired private PillRepository pillRepository;
    @Autowired private PersonProfileRepository profileRepository;
    @Autowired private MedicationHistoryRepository medicationHistoryRepository;

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
            for (String pillName : request.getPillNames()) {
                createPrescriptionAndReminders(profile, pillName, start, end, calculatedTimes, request, pillName + " 복용");
            }
        }
    }

    private void createPrescriptionAndReminders(PersonProfile profile, String pillName, LocalDate start, LocalDate end,
                                                List<LocalTime> times, AlarmCreateRequestDto request, String title) {

        Pill pill = pillRepository.findByName(pillName).orElse(null);
        if (pill == null) {
            pill = new Pill();
            pill.setName(pillName);
            pill.setDescription("사용자 직접 입력 또는 자동 생성된 약");
            pill = pillRepository.save(pill);
        }

        // 1. 처방전(Prescription) 생성 (복용 기간 및 메모 저장)
        Prescription prescription = new Prescription();
        prescription.setProfile(profile);
        prescription.setPill(pill);
        prescription.setStartDate(start);
        prescription.setEndDate(end);
        prescription.setDirections(title);
        prescriptionRepository.save(prescription);

        // 2. 알림(IntakeReminder) 생성 (설정된 시간마다 알림 생성)
        for (LocalTime time : times) {
            IntakeReminder reminder = new IntakeReminder();
            reminder.setPrescription(prescription);
            reminder.setRemindAt(time);

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
        int startHour = 8;

        for (int hour = startHour; hour < 24; hour += frequencyHours) {
            times.add(LocalTime.of(hour, 0));
        }
        return times;
    }

    @Transactional
    public void archiveFinishedAlarms() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<Prescription> finishedPrescriptions = prescriptionRepository.findByEndDateBeforeAndDeletedFalse(LocalDate.now());

        for (Prescription p : finishedPrescriptions) {
            MedicationHistory history = new MedicationHistory();
            history.setUser(p.getProfile().getUser());
            history.setPillName(p.getPill().getName());
            history.setStartDate(p.getStartDate());
            history.setEndDate(p.getEndDate());

            medicationHistoryRepository.save(history);

            p.setDeleted(true);
        }
    }

    @Transactional(readOnly = true)
    public List<RoutineDto> getMyRoutines(Long userId) {
        // 등록순(ID 오름차순) 정렬 조회
        List<Prescription> prescriptions = prescriptionRepository.findByProfile_User_IdAndDeletedFalseOrderByIdAsc(userId);

        return prescriptions.stream().map(p -> {
            RoutineDto dto = new RoutineDto();
            dto.setPrescriptionId(p.getId());
            dto.setTitle(p.getDirections());
            dto.setPillName(p.getPill().getName());

            // [추가] DB에 저장된 타입 불러오기 (없으면 routine이 기본)
            dto.setType(p.getType() != null ? p.getType() : "routine");

            List<IntakeReminder> reminders = reminderRepository.findByPrescription(p);
            if (!reminders.isEmpty()) {
                IntakeReminder r = reminders.get(0);
                dto.setMon(r.getMon()); dto.setTue(r.getTue()); dto.setWed(r.getWed());
                dto.setThu(r.getThu()); dto.setFri(r.getFri()); dto.setSat(r.getSat()); dto.setSun(r.getSun());
                dto.setTime(r.getRemindAt().toString());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void deleteRoutine(Long prescriptionId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 루틴입니다."));

        // 1. 처방전 삭제 처리
        prescription.setDeleted(true);

        // 2. 연결된 알림들도 비활성화 처리 (선택사항, 로직에 따라 필요)
        // List<IntakeReminder> reminders = reminderRepository.findByPrescription(prescription);
        // reminders.forEach(r -> r.setStatus(enums.ReminderStatus.STOPPED));
    }

    @Transactional
    public void updateRoutine(Long prescriptionId, RoutineDto updateDto) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 루틴입니다."));

        if (updateDto.getTitle() != null) {
            prescription.setDirections(updateDto.getTitle());
        }
        if (updateDto.getType() != null) {
            prescription.setType(updateDto.getType());
        }

        // 요일 및 시간 수정
        List<IntakeReminder> reminders = reminderRepository.findByPrescription(prescription);
        for (IntakeReminder r : reminders) {
            r.setMon(updateDto.isMon());
            r.setTue(updateDto.isTue());
            r.setWed(updateDto.isWed());
            r.setThu(updateDto.isThu());
            r.setFri(updateDto.isFri());
            r.setSat(updateDto.isSat());
            r.setSun(updateDto.isSun());

            if (updateDto.getTime() != null) {
                r.setRemindAt(LocalTime.parse(updateDto.getTime()));
            }
        }
    }

    @Transactional
    public void createSimpleSchedule(Long userId, String title, String type) {
        PersonProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필 없음"));

        Pill pill = pillRepository.findByName(title).orElse(null);
        if (pill == null) {
            pill = new Pill();
            pill.setName(title);
            pill.setDescription("일정");
            pill = pillRepository.save(pill);
        }

        Prescription prescription = new Prescription();
        prescription.setProfile(profile);
        prescription.setPill(pill);
        prescription.setStartDate(LocalDate.now());
        prescription.setEndDate(LocalDate.now().plusYears(1));
        prescription.setDirections(title);

        // [추가] 요청받은 타입(schedule/common) 저장
        prescription.setType(type);

        prescriptionRepository.save(prescription);
    }
}