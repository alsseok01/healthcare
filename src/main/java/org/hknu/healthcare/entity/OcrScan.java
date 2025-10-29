package org.hknu.healthcare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class OcrScan extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private PersonProfile profile;

    private String imageUrl;
    @Column(length = 4000)
    private String extractedText;

    // 파싱된 약 코드/명(다건 매핑은 별도 테이블로 확장 가능)
    private String parsedDrugCode;
    private String parsedDrugName;
}
