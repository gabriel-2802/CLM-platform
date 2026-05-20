package clm.demo.models;

import clm.demo.models.enums.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A filled document generated for a specific client.
 * Extends {@link Document} (JOINED inheritance) and owns the client-specific
 * lifecycle fields. Auxiliary files are attached as {@link Appendix} children.
 */
@Entity
@Table(name = "contract", schema = "clm", indexes = {
        @Index(name = "idx_contract_template_client", columnList = "document_id, client_id")
})
@DiscriminatorValue("CONTRACT")
@PrimaryKeyJoinColumn(name = "document_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = false)
public class Contract extends Document {

    @Column(name = "client_id", nullable = false)
    private Integer clientId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    @Column(name = "contract_status", nullable = false)
    @Builder.Default
    private ContractStatus contractStatus = ContractStatus.PENDING_SIGNATURE;

    @Column(name = "auto_renew", nullable = false)
    @Builder.Default
    private Boolean autoRenew = false;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "reasons_for_termination", length = 1000)
    @Builder.Default
    private String reasonsForTermination = "";

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    @Builder.Default
    private List<Appendix> appendices = new ArrayList<>();

    @OneToMany(mappedBy = "contract")
    @BatchSize(size = 10)
    @Builder.Default
    private List<ContractDetails> contractDetailsList = new ArrayList<>();

    /* audit data */

    @Column(name = "terminated_at")
    private LocalDateTime terminatedAt;

    @Column(name = "terminated_by_user_id")
    private Integer terminatedByUserId;
}
