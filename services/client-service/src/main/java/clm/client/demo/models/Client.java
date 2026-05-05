package clm.client.demo.models;

import clm.client.demo.models.enums.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "company_type", nullable = false)
    private CompanyType type;

    @Column(name = "tax_id", nullable = false, unique = true)
    private String taxId;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "verification_date")
    private LocalDate verificationDate;

    @Column(name = "address")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "administration", nullable = false)
    private Administration administration;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_type")
    private TaxType taxType;

    @Enumerated(EnumType.STRING)
    @Column(name = "vat_payer", nullable = false)
    private TaxFrequency vatPayer;

    @Column(name = "vat_on_collection")
    private Boolean vatOnCollection;

    @Column(name = "has_eu_vat_code")
    private Boolean hasEuVatCode;

    @Column(name = "eu_vat_code")
    private String euVatCode;

    @Column(name = "eu_operation")
    private Boolean euOperation;

    @Column(name = "dividends")
    private Boolean dividends;

    @Column(name = "employees")
    private String employees;

    @Column(name = "cash_register")
    private Boolean cashRegister;

    @Column(name = "hq_expiration_date")
    private LocalDate hqExpirationDate;

    @Column(name = "admin_mandate_expiration")
    private LocalDate adminMandateExpiration;

    @Column(name = "fiscal_certificate_date")
    private LocalDate fiscalCertificateDate;

    @Column(name = "payer_sheet_date")
    private LocalDate payerSheetDate;

    @Column(name = "fiscal_vector_date")
    private LocalDate fiscalVectorDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // relationships 

    @OneToOne(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ClientDetails details;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @BatchSize(size = 50)
    private Set<WorkPoint> workPoints = new HashSet<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @BatchSize(size = 50)
    private Set<ClientHistory> histories = new HashSet<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @BatchSize(size = 50)
    private Set<UserClient> userClients = new HashSet<>();

    // bidirectional sync helpers 

    public void setDetails(ClientDetails details) {
        if (Objects.nonNull(details)) {
            details.setClient(this);
        }
        this.details = details;
    }

    public void addWorkPoint(WorkPoint workPoint) {
        if (Objects.nonNull(workPoint)) {
            workPoints.add(workPoint);
            workPoint.setClient(this);
        }
    }

    public void addHistory(ClientHistory history) {
        if (Objects.nonNull(history)) {
            histories.add(history);
            history.setClient(this);
        }
    }
}