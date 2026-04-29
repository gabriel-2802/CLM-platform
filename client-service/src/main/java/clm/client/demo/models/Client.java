package clm.client.demo.models;

import clm.client.demo.models.enums.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @Column(name = "denumire", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip", nullable = false)
    private CompanyType type;

    @Column(name = "cui", nullable = false, unique = true)
    private String taxId;

    @Column(name = "activa", nullable = false)
    private boolean active;

    @Column(name = "data_verificarii")
    private LocalDateTime verificationDate;

    @Column(name = "adresa")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "administratie", nullable = false)
    private Administration administration;

    @Enumerated(EnumType.STRING)
    @Column(name = "impozit")
    private TaxType taxType;

    @Enumerated(EnumType.STRING)
    @Column(name = "platitor_tva", nullable = false)
    private TaxFrequency vatPayer;

    @Column(name = "tva_la_incasare")
    private Boolean vatOnCollection;

    @Column(name = "are_cod_tva_ue")
    private Boolean hasEuVatCode;

    @Column(name = "cod_tva_ue")
    private String euVatCode;

    @Column(name = "operatiune_ue")
    private Boolean euOperation;

    @Column(name = "dividende")
    private Boolean dividends;

    @Column(name = "salariati")
    private String employees;

    @Column(name = "casa_de_marcat")
    private Boolean cashRegister;

    @Column(name = "data_exp_sediu_social")
    private LocalDateTime hqExpirationDate;

    @Column(name = "data_exp_mandat_admin")
    private LocalDateTime adminMandateExpiration;

    @Column(name = "data_certificat_fiscal")
    private LocalDateTime fiscalCertificateDate;

    @Column(name = "data_fisa_platitor")
    private LocalDateTime payerSheetDate;

    @Column(name = "data_vect_fiscal")
    private LocalDateTime fiscalVectorDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ─── Relationships ────────────────────────────────────────────────────────

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

    // ─── Bidirectional Sync Helpers ───────────────────────────────────────────

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