package org.hknu.healthcare.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AiParseResultDto {

    @JsonProperty("company")
    private String company;

    @JsonProperty("shape")
    private String shape;

    @JsonProperty("color")
    private String color;

    @JsonProperty("front_imprint")
    private String front_imprint;

    @JsonProperty("back_imprint")
    private String back_imprint;
}