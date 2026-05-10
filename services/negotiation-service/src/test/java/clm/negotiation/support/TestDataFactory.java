package clm.negotiation.support;

import clm.negotiation.dto.requests.CreateNegotiationRequest;
import clm.negotiation.dto.requests.UpdateNotesRequest;
import clm.negotiation.dto.responses.NegotiationResponseDTO;
import clm.negotiation.models.Negotiation;
import clm.negotiation.models.enums.NegotiationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class TestDataFactory {

    private TestDataFactory() {}

    // ------------------------------------------------------------------ //
    //  models                                                              //
    // ------------------------------------------------------------------ //

    public static Negotiation negotiation(Long id, NegotiationStatus status) {
        Negotiation n = Negotiation.builder()
                .contractId(10L)
                .clientId(42)
                .proposedValue(BigDecimal.valueOf(4_500))
                .proposedEndDate(LocalDate.of(2027, 6, 1))
                .status(status)
                .notes("negociere trimestru Q2")
                .createdByUserId(7)
                .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
        setId(n, id);
        return n;
    }

    public static Negotiation negotiationWithoutEndDate(Long id, NegotiationStatus status) {
        Negotiation n = Negotiation.builder()
                .contractId(10L)
                .clientId(42)
                .proposedValue(BigDecimal.valueOf(4_500))
                .status(status)
                .createdByUserId(7)
                .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
        setId(n, id);
        return n;
    }

    public static Negotiation negotiationWithoutValue(Long id, NegotiationStatus status) {
        Negotiation n = Negotiation.builder()
                .contractId(10L)
                .clientId(42)
                .proposedEndDate(LocalDate.of(2027, 6, 1))
                .status(status)
                .createdByUserId(7)
                .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
        setId(n, id);
        return n;
    }

    // ------------------------------------------------------------------ //
    //  requests                                                            //
    // ------------------------------------------------------------------ //

    public static CreateNegotiationRequest createRequest() {
        return new CreateNegotiationRequest(
                10L,
                42,
                BigDecimal.valueOf(4_500),
                LocalDate.of(2027, 6, 1),
                7,
                "negociere trimestru Q2"
        );
    }

    public static CreateNegotiationRequest createRequestOnlyValue() {
        return new CreateNegotiationRequest(
                10L, 42, BigDecimal.valueOf(4_500), null, 7, null
        );
    }

    public static CreateNegotiationRequest createRequestOnlyEndDate() {
        return new CreateNegotiationRequest(
                10L, 42, null, LocalDate.of(2027, 6, 1), 7, null
        );
    }

    public static CreateNegotiationRequest createRequestMissingContractId() {
        return new CreateNegotiationRequest(
                null, 42, BigDecimal.valueOf(4_500), LocalDate.of(2027, 6, 1), 7, null
        );
    }

    public static CreateNegotiationRequest createRequestMissingClientId() {
        return new CreateNegotiationRequest(
                10L, null, BigDecimal.valueOf(4_500), LocalDate.of(2027, 6, 1), 7, null
        );
    }

    public static CreateNegotiationRequest createRequestMissingCreatedBy() {
        return new CreateNegotiationRequest(
                10L, 42, BigDecimal.valueOf(4_500), LocalDate.of(2027, 6, 1), null, null
        );
    }

    public static UpdateNotesRequest updateNotesRequest(String notes) {
        return new UpdateNotesRequest(notes);
    }

    // ------------------------------------------------------------------ //
    //  response DTOs                                                       //
    // ------------------------------------------------------------------ //

    public static NegotiationResponseDTO response(Long id, NegotiationStatus status) {
        return new NegotiationResponseDTO(
                id,
                10L,
                42,
                BigDecimal.valueOf(4_500),
                LocalDate.of(2027, 6, 1),
                status,
                "negociere trimestru Q2",
                7,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                null
        );
    }

    public static NegotiationResponseDTO resolvedResponse(Long id, NegotiationStatus status) {
        return new NegotiationResponseDTO(
                id,
                10L,
                42,
                BigDecimal.valueOf(4_500),
                LocalDate.of(2027, 6, 1),
                status,
                "negociere trimestru Q2",
                7,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 3, 1, 12, 0)
        );
    }

    // ------------------------------------------------------------------ //
    //  internal helpers                                                    //
    // ------------------------------------------------------------------ //

    public static void setId(Object entity, Long id) {
        try {
            Class<?> clazz = entity.getClass();
            while (clazz != null) {
                try {
                    var f = clazz.getDeclaredField("id");
                    f.setAccessible(true);
                    f.set(entity, id);
                    return;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("could not set id on " + entity.getClass().getSimpleName(), e);
        }
    }
}
