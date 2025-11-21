package org.hknu.healthcare.DTO;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class AlarmCreateRequestDto {

    private String title;
    private List<String> pillNames;

    // 설정 필드
    private boolean isGrouped;

    private int frequencyHours;
    private int durationDays;

    // 요일 설정
    private boolean mon;
    private boolean tue;
    private boolean wed;
    private boolean thu;
    private boolean fri;
    private boolean sat;
    private boolean sun;

    private String startDate;
}