package org.hknu.healthcare.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AiOcrResultDto {
    @JsonProperty("drugNames")
    private List<String> drugNames;
}