package clm.client.demo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "done", nullable = false)
    private boolean done;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "notes")
    private String notes;

    @Column(name = "blocked")
    private String blocked;

    @Column(name = "objective")
    private String objective;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
}
