package clm.demo.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_details", schema = "clm", indexes = {
        @Index(name = "idx_contract_details_contract", columnList = "contract_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "contract_value", precision = 12, scale = 2)
    private BigDecimal contractValue;

    @Column(name = "contract_balance", precision = 12, scale = 2, nullable = false)
    private BigDecimal contractBalance;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by_user_id")
    private Integer createdByUserId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appendix_id", unique = true)
    private Appendix appendix;
}
