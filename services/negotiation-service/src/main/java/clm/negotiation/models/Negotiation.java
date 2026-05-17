package clm.negotiation.models;

import clm.negotiation.models.enums.NegotiationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "negotiation", schema = "negotiations", indexes = {
        @Index(name = "idx_negotiation_contract", columnList = "contract_id"),
        @Index(name = "idx_negotiation_client",   columnList = "client_id"),
        @Index(name = "idx_negotiation_status",   columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Negotiation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "client_id", nullable = false)
    private Integer clientId;

    @Column(name = "proposed_value", precision = 12, scale = 2)
    private BigDecimal proposedValue;

    @Column(name = "proposed_end_date")
    private LocalDate proposedEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private NegotiationStatus status = NegotiationStatus.DRAFT;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "created_by_user_id", nullable = false)
    private Integer createdByUserId;

    @Column(name = "created_by_user_name", length = 255)
    private String createdByUserName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
