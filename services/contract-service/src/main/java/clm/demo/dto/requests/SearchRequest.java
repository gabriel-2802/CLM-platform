package clm.demo.dto.requests;

import clm.demo.models.enums.ContractStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Search and pagination parameters for contract queries.
 *
 * <p>All filter fields are optional (null = not applied).
 * {@code page} and {@code size} control server-side pagination;
 * defaults (0 / 20) are applied in {@code ContractService} when null.</p>
 */
public record SearchRequest(
        String notes,
        ContractStatus contractStatus,
        Integer clientId,
        Integer generatedByUser,
        List<String> labelValues,
        String templateName,
        String templateDescription,
        LocalDate createdAfter,
        LocalDate createdBefore,
        /** Zero-based page index. Defaults to 0 when null. */
        Integer page,
        /** Page size (max rows returned). Defaults to 20 when null. */
        Integer size
) {}
