package clm.negotiation.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "terminated_contract", schema = "negotiations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TerminatedContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_id", nullable = false, unique = true)
    private Long contractId;

    @Column(name = "terminated_at", nullable = false, updatable = false)
    private LocalDateTime terminatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.terminatedAt == null) {
            this.terminatedAt = LocalDateTime.now();
        }
    }
}
