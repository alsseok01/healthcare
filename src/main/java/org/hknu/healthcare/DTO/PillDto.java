package org.hknu.healthcare.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PillDto {

    private String itemName;        // 제품명
    private String efcyQesitm;      // 효능
    private String useMethodQesitm; // 사용법
    private String atpnWarnQesitm;  // 주의사항 경고

}