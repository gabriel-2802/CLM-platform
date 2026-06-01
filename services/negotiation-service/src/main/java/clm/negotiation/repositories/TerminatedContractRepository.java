package clm.negotiation.repositories;

import clm.negotiation.models.TerminatedContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TerminatedContractRepository extends JpaRepository<TerminatedContract, Long> {
    boolean existsByContractId(Long contractId);
}
