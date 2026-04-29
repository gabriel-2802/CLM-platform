package clm.client.demo.models;

import clm.client.demo.models.enums.Administration;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "puncte_de_lucru")
@Getter
@Setter
@NoArgsConstructor
public class WorkPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "denumire", nullable = false)
    private String name;

    @Column(name = "de_la", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "pana_la")
    private LocalDateTime validTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "administratie", nullable = false)
    private Administration administration;

    @Column(name = "registru_uc", nullable = false)
    private boolean ucRegistry;

    @Column(name = "salariati", nullable = false)
    private int employeeCount;

    @Column(name = "cui")
    private String taxId;

    @Column(name = "casa_de_marcat", nullable = false)
    private boolean cashRegister;
}