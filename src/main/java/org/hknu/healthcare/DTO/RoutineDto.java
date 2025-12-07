package org.hknu.healthcare.DTO;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalTime;

@Getter
@Setter
public class RoutineDto {
    private Long prescriptionId;
    private String title;
    private String pillName;

    private String type;

    // 요일 설정
    private boolean mon;
    private boolean tue;
    private boolean wed;
    private boolean thu;
    private boolean fri;
    private boolean sat;
    private boolean sun;

    private String time;
    private boolean active;
}