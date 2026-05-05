package clm.client.demo.controllers;

import clm.client.demo.models.enums.Administration;
import clm.client.demo.models.enums.CompanyType;
import clm.client.demo.models.enums.TaxFrequency;
import clm.client.demo.models.enums.TaxType;
import clm.client.demo.models.enums.YesNoNa;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enums")
@Tag(name = "Enums", description = "Returns all valid enum values used by the client domain")
public class EnumsController {

    @GetMapping
    @Operation(summary = "Get all enum values")
    public ResponseEntity<Map<String, List<String>>> getEnums() {
        return ResponseEntity.ok(Map.of(
                "companyTypes",    toList(CompanyType.values()),
                "taxTypes",        toList(TaxType.values()),
                "taxFrequencies",  toList(TaxFrequency.values()),
                "yesNoNa",         toList(YesNoNa.values()),
                "administrations", toList(Administration.values())
        ));
    }

    private <E extends Enum<E>> List<String> toList(E[] values) {
        return Arrays.stream(values).map(Enum::name).toList();
    }
}
