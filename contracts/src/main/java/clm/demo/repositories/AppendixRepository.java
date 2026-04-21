package clm.demo.repositories;

import clm.demo.models.Appendix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppendixRepository extends JpaRepository<Appendix, Long> {

    List<Appendix> findByContractId(Long contractId);
}
