package clm.client.demo.models;

import clm.client.demo.models.enums.YesNoNa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "client_details")
@Getter
@Setter
@NoArgsConstructor
public class ClientDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;

    @Column(name = "uc_registry", nullable = false)
    private boolean ucRegistry;

    @Enumerated(EnumType.STRING)
    @Column(name = "fiscal_evidence_registry", nullable = false)
    private YesNoNa fiscalEvidenceRegistry;

    @Column(name = "money_laundering_office", nullable = false)
    private boolean moneyLaunderingOffice;

    @Column(name = "internal_rules", nullable = false)
    private boolean internalRules;

    @Column(name = "accounting_policies_manual", nullable = false)
    private boolean accountingPoliciesManual;

    @Column(name = "revisal_address", nullable = false)
    private boolean revisalAddress;

    @Column(name = "itm_password")
    private String itmPassword;

    @Column(name = "online_declarations", nullable = false)
    private boolean onlineDeclarations;

    @Enumerated(EnumType.STRING)
    @Column(name = "fiscal_file_access", nullable = false)
    private YesNoNa fiscalFileAccess;
}