package clm.client.demo.models;

import clm.client.demo.models.enums.YesNoNa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "client_histories",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"client_id", "year"})
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

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "turnover", nullable = false)
    private BigDecimal turnover;

    @Column(name = "inventory", nullable = false)
    private boolean inventory;

    @Enumerated(EnumType.STRING)
    @Column(name = "june_semester_balance", nullable = false)
    private YesNoNa juneSemesterBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "annual_balance", nullable = false)
    private YesNoNa annualBalance;
}
