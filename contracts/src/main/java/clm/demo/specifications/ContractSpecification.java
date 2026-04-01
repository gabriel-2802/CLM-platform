package clm.demo.specifications;

import clm.demo.dto.requests.SearchRequest;
import clm.demo.models.Contract;
import clm.demo.models.ContractFieldValue;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification for building dynamic, server-side search queries.
 *
 */
@Component
public class ContractSpecification {

    /**
     * Builds a server-side Specification from all non-null/non-empty fields in
     * {@code request}. All conditions are combined with AND logic.
     *
     * @param request the search request containing filter criteria
     * @return a Specification that combines all applicable filters with AND logic
     */
    public Specification<Contract> buildSearchSpecification(SearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            /**
             * Eager-fetch contractTemplate for the data query only.
             * Hibernate also executes a COUNT query when Pageable is used; adding
             * a fetch to that query causes an exception, so we guard with the
             * result-type check.
             */
            boolean isDataQuery = !Long.class.equals(query.getResultType())
                    && !long.class.equals(query.getResultType());
            if (isDataQuery) {
                root.fetch("contractTemplate", JoinType.LEFT);
                query.distinct(true);
            }

            // 1. notes : functional GIN trigram index: idx_gc_notes_lower_trgm
            if (request.notes() != null && !request.notes().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("notes")),
                        "%" + request.notes().toLowerCase() + "%"
                ));
            }

            // 2. contractStatus : B-tree equality: idx_generated_contract_status
            if (request.contractStatus() != null) {
                predicates.add(cb.equal(root.get("contractStatus"), request.contractStatus()));
            }

            // 3. clientId : B-tree equality: idx_generated_contract_client_id
            if (request.clientId() != null) {
                predicates.add(cb.equal(root.get("clientId"), request.clientId()));
            }

            // 4. generatedBy : B-tree equality: idx_generated_contract_generated_by
            if (request.generatedBy() != null) {
                predicates.add(cb.equal(root.get("generatedBy"), request.generatedBy()));
            }

            // 5. templateName : GIN trigram index: idx_ct_name_lower_trgm (on joined table)
            if (request.templateName() != null && !request.templateName().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("contractTemplate").get("templateName")),
                        "%" + request.templateName().toLowerCase() + "%"
                ));
            }

            // 6. templateDescription : GIN trigram index: idx_ct_desc_lower_trgm (on joined table)
            if (request.templateDescription() != null && !request.templateDescription().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("contractTemplate").get("description")),
                        "%" + request.templateDescription().toLowerCase() + "%"
                ));
            }

            // 7. createdAfter : B-tree range: idx_generated_contract_created_date_range
            if (request.createdAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        request.createdAfter().atStartOfDay()
                ));
            }

            // 8. createdBefore : B-tree range: idx_generated_contract_created_date_range
            if (request.createdBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"),
                        LocalDateTime.of(request.createdBefore(), LocalTime.MAX)
                ));
            }

            /**
             * 9. labelValues : server-side AND intersection via correlated EXISTS.
             *
             * for each search term, we add:
             *   AND EXISTS (
             *       SELECT 1 FROM contract_field_value cfv
             *       WHERE cfv.generated_contract_id = gc.id
             *         AND LOWER(cfv.field_value) LIKE '%<term>%'
             *   )
             *
             * One EXISTS clause per term produces strict AND semantics:
             * the contract must contain a field value matching every term.
             *
             * PostgreSQL execution plan:
             *   - Outer: Index Scan / Bitmap Heap Scan on generated_contract
             *     using the composite predicate indexes from V2.
             *   - Inner (per term): Index Scan on idx_cfv_field_value_lower_trgm
             *     (GIN trigram, V4) intersected with the FK index on
             *     generated_contract_id : typically a very small probe.
             *   Result: no rows cross the network boundary until after all
             *   filtering is complete.
             */
            if (request.labelValues() != null) {
                for (String term : request.labelValues()) {
                    if (term == null || term.isBlank()) continue;

                    Subquery<Long> sub = query.subquery(Long.class);
                    var cfvRoot = sub.from(ContractFieldValue.class);
                    sub.select(cb.literal(1L))
                            .where(
                                    cb.equal(cfvRoot.get("contract"), root),
                                    cb.like(
                                            cb.lower(cfvRoot.get("fieldValue")),
                                            "%" + term.toLowerCase() + "%"
                                    )
                            );
                    predicates.add(cb.exists(sub));
                }
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
