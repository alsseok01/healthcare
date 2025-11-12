package org.hknu.healthcare.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AiParseResultDto {

    @JsonProperty("shape") // Gemini가 반환할 JSON 키
    private String shape;

    @JsonProperty("color")
    private String color;

    @JsonProperty("imprint")
    private String imprint;
}