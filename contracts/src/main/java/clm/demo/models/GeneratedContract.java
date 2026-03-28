package clm.demo.models;

import clm.demo.models.enums.ContractGenerationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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
public class GeneratedContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Source template. Lazy loaded to keep contract lookups lightweight. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ContractTemplate contractTemplate;

    /** * Foreign reference to the main 'Client' table.
     * <b>Note:</b> No JPA @ManyToOne used because Client resides in a different schema/service.
     */
    @Column(name = "client_id", nullable = false)
    private Integer clientId;

    /** Current lifecycle state (e.g., GENERATED, SIGNED, EXPIRED). */
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_status", nullable = false, length = 50)
    @Builder.Default
    private ContractGenerationStatus contractStatus = ContractGenerationStatus.GENERATED;

    /** Reference to the User (staff) who initiated the generation. */
    @Column(name = "generated_by")
    private Integer generatedBy;

    /** * The final filled document.
     * <b>Note:</b> Lazy loaded as BYTEA to prevent memory exhaustion during list views.
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "document_content", columnDefinition = "BYTEA")
    private byte[] documentContent;

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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** * Historical record of exactly what values were injected into the template
     * at the moment of generation. Used for auditing.
     */
    @OneToMany(mappedBy = "generatedContract", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContractFieldValue> fieldValues = new ArrayList<>();
}