package clm.client.demo.models;

import clm.client.demo.models.enums.Administration;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "work_points", schema = "clients")
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

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "administration", nullable = false)
    private Administration administration;

    @Column(name = "uc_registry", nullable = false)
    private boolean ucRegistry;

    @Column(name = "employee_count", nullable = false)
    private int employeeCount;

    @Column(name = "tax_id")
    private String taxId;

    @Column(name = "cash_register", nullable = false)
    private boolean cashRegister;
}
