package clm.client.demo.models;

import clm.client.demo.models.enums.YesNoNa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "istorice",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"client_id", "anul"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ClientHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "anul", nullable = false)
    private int year;

    @Column(name = "cifra_afaceri", nullable = false)
    private double turnover;

    @Column(name = "inventar", nullable = false)
    private boolean inventory;

    @Enumerated(EnumType.STRING)
    @Column(name = "bilant_sem_iun", nullable = false)
    private YesNoNa juneSemesterBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "bilant_anual", nullable = false)
    private YesNoNa annualBalance;
}