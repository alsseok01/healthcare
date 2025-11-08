package org.hknu.healthcare.Controller;

import org.hknu.healthcare.DTO.DrugInfoDto;
import org.hknu.healthcare.Serivce.DrugInfoService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class DrugInfoController {

    private final DrugInfoService service;

    public DrugInfoController(DrugInfoService service) {
        this.service = service;
    }

    @GetMapping("/drug-info")
    public ResponseEntity<?> getDrugInfo(@RequestParam String itemName) {
        Optional<DrugInfoDto> dto = service.findByItemName(itemName);
        return dto.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No result")));
    }
}
