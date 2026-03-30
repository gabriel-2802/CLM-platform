package clm.demo.models;

import clm.demo.models.enums.ContractStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The final output: a filled document generated for a specific client.
 * <p><b>Data Resolution:</b> At generation, placeholders are replaced with
 * real data and the resulting binary is stored in {@code documentContent}.</p>
 * <p><b>Architecture:</b> Uses raw IDs for {@code clientId} and {@code generatedBy}
 * to respect the boundary between this 'contracts' schema and the main application schema.</p>
 */
@Entity
@Table(name = "generated_contract", schema = "contracts", indexes = {
        @Index(name = "idx_generated_contract_template_client", columnList = "template_id, client_id"),
        @Index(name = "idx_generated_contract_validity", columnList = "contract_start_date, contract_end_date"),
        @Index(name = "idx_generated_contract_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Source template. Lazy loaded to keep contract lookups lightweight. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template contractTemplate;

    /** * Foreign reference to the main 'Client' table.
     * <b>Note:</b> No JPA @ManyToOne used because Client resides in a different schema/service.
     */
    @Column(name = "client_id", nullable = false)
    private Integer clientId;

    /** Current lifecycle state (e.g., PENDING_SIGNATURE, ACTIVE, TERMINATED, ARCHIVED). */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)   // ← was SqlTypes.VARCHAR
    @Column(name = "contract_status", nullable = false)
    @Builder.Default
    private ContractStatus contractStatus = ContractStatus.PENDING_SIGNATURE;

    /** Reference to the User (staff) who initiated the generation. */
    @Column(name = "generated_by")
    private Integer generatedBy;

    /** Email address of the user who generated the contract. */
    @Column(name = "generated_by_mail", length = 255)
    private String generatedByMail;

    /** * The final filled document.
     * <b>Note:</b> Stored as BYTEA. Nullable - set after document generation completes.
     */
    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "document_content")
    private byte[] documentContent;

    /** * The signed version of the document (populated when contract is signed).
     * <b>Note:</b> Stored as BYTEA. Nullable - only set after signature/ACTIVE status.
     */
    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "signed_document_content")
    private byte[] signedDocument;

    /** Monetary value of the contract for reporting and filtering. */
    @Column(name = "contract_value", precision = 12, scale = 2)
    private BigDecimal contractValue;

    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    /** Contextual notes or generation metadata. */
    @Column(name = "notes", length = 1000)
    private String notes;

    /** Date when the contract was terminated (null if not terminated). */
    @Column(name = "termination_date")
    private LocalDate terminationDate;

    /** Reasons for contract termination (empty string by default). */
    @Column(name = "reasons_for_termination", length = 1000)
    @Builder.Default
    private String reasonsForTermination = "";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** * Historical record of exactly what values were injected into the template
     * at the moment of generation. Used for auditing.
     */
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContractFieldValue> fieldValues = new ArrayList<>();
}