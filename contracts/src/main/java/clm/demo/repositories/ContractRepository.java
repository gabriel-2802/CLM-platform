package clm.demo.repositories;

import clm.demo.models.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for the {@link Contract} entity.
 *
 * <p>Inherits all CRUD operations from {@link JpaRepository} and dynamic
 * server-side filtering via {@link JpaSpecificationExecutor}.  The paginated
 * overload {@code findAll(Specification, Pageable)} — provided by
 * {@link JpaSpecificationExecutor} — is the primary entry point for search:
 * it pushes {@code LIMIT}/{@code OFFSET} into the SQL query so the database
 * never ships more rows than the requested page size.</p>
 *
 * <p>The previously unused custom JPQL queries ({@code findByFieldValues},
 * {@code findByCreatedDateRange}) have been removed: date-range filtering is
 * now handled inside {@link clm.demo.specifications.ContractSpecification},
 * and label-value intersection is expressed as correlated EXISTS subqueries
 * in the same specification — both executing entirely on the server.</p>
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, Long>, JpaSpecificationExecutor<Contract> {

    // findAll(Specification<Contract>, Pageable) → Page<Contract>
    // is inherited from JpaSpecificationExecutor — no override needed.
}
