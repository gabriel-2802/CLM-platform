package clm.demo.repositories;

import clm.demo.models.ContractDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractDetailsRepository extends JpaRepository<ContractDetails, Long> {

    List<ContractDetails> findByContractId(Long contractId);
}
