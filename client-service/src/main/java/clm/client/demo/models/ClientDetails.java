package clm.client.demo.models;

import clm.client.demo.models.enums.YesNoNa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detalii")
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

    @Column(name = "registru_uc", nullable = false)
    private boolean ucRegistry;

    @Enumerated(EnumType.STRING)
    @Column(name = "registru_ev_fiscala", nullable = false)
    private YesNoNa fiscalEvidenceRegistry;

    @Column(name = "of_spalare_bani", nullable = false)
    private boolean moneyLaunderingOffice;

    @Column(name = "regulament_ordine_interioara", nullable = false)
    private boolean internalRules;

    @Column(name = "manual_politici_contabile", nullable = false)
    private boolean accountingPoliciesManual;

    @Column(name = "adresa_revisal", nullable = false)
    private boolean revisalAddress;

    @Column(name = "parola_itm")
    private String itmPassword;

    @Column(name = "depunere_declaratii_online", nullable = false)
    private boolean onlineDeclarations;

    @Enumerated(EnumType.STRING)
    @Column(name = "acces_dosar_fiscal", nullable = false)
    private YesNoNa fiscalFileAccess;
}