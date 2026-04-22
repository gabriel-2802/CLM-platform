package clm.demo.controllers;

import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.services.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Validated
@Tag(name = "Reports", description = "Reporting endpoints consumed by the notification microservice")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contracts/report")
public class ReportController {

    private final ReportService reportService;

    @Operation(
        summary     = "List contracts expiring within N days",
        description = """
            Returns all `ACTIVE` contracts whose `contractEndDate` falls within the next `days`
            calendar days (inclusive of today). Intended for the notification microservice to build
            expiry-alert email digests. Results are ordered by `contractEndDate ASC`.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Expiring contracts returned",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ContractResponseDTO.class)))),
        @ApiResponse(responseCode = "204", description = "No contracts expiring within the given window",
            content = @Content),
        @ApiResponse(responseCode = "400", description = "days must be ≥ 1", content = @Content)
    })
    @GetMapping("/expiring")
    public ResponseEntity<List<ContractResponseDTO>> getExpiringContracts(
            @Parameter(description = "Look-ahead window in calendar days (must be ≥ 1)", required = true, example = "30")
            @RequestParam @Min(1) int days) {

        List<ContractResponseDTO> result = reportService.getExpiringContracts(days);
        return result.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(result);
    }

    @Operation(
        summary     = "List contracts with no client renegotiation in N months",
        description = """
            Returns `ACTIVE` contracts where every other contract the client holds within the
            look-back window carries the same `contractValue` — meaning no monetary renegotiation
            has occurred in that period. Clients whose rate changed at any point within the window
            are excluded. Results are ordered by `clientId ASC`, then `contractStartDate ASC`.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Inactive-client contracts returned",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ContractResponseDTO.class)))),
        @ApiResponse(responseCode = "204", description = "No matching contracts found",
            content = @Content),
        @ApiResponse(responseCode = "400", description = "months must be ≥ 1", content = @Content)
    })
    @GetMapping("/inactive-clients")
    public ResponseEntity<List<ContractResponseDTO>> getInactiveClientContracts(
            @Parameter(description = "Minimum contract age in calendar months (must be ≥ 1)", required = true, example = "6")
            @RequestParam @Min(1) int months) {

        List<ContractResponseDTO> result = reportService.getInactiveClientContracts(months);
        return result.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(result);
    }
}
