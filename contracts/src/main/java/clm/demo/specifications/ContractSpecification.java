package clm.demo.specifications;

import clm.demo.dto.requests.SearchRequest;
import clm.demo.models.Contract;
import clm.demo.models.DocumentFieldValue;
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
 * Builds dynamic JPA Specifications for contract search queries.
 * All predicates use AND logic. Trigram GIN indexes on the clm schema
 * are used for LIKE patterns; B-tree indexes for equality/range filters.
 */
@Component
public class ContractSpecification {

    public Specification<Contract> buildSearchSpecification(SearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Eager-fetch documentTemplate only for the data query, not the COUNT query.
            boolean isDataQuery = !Long.class.equals(query.getResultType())
                    && !long.class.equals(query.getResultType());
            if (isDataQuery) {
                root.fetch("documentTemplate", JoinType.LEFT);
                query.distinct(true);
            }

            // 1. notes — GIN trigram: idx_doc_notes_lower_trgm
            if (request.notes() != null && !request.notes().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("notes")),
                        "%" + request.notes().toLowerCase() + "%"
                ));
            }

            // 2. contractStatus — B-tree: idx_contract_status
            if (request.contractStatus() != null) {
                predicates.add(cb.equal(root.get("contractStatus"), request.contractStatus()));
            }

            // 3. clientId — B-tree: idx_contract_client
            if (request.clientId() != null) {
                predicates.add(cb.equal(root.get("clientId"), request.clientId()));
            }

            // 4. generatedBy — B-tree: idx_document_type (base table)
            if (request.generatedBy() != null) {
                predicates.add(cb.equal(root.get("generatedBy"), request.generatedBy()));
            }

            // 5. templateName — GIN trigram: idx_dt_name_lower_trgm
            if (request.templateName() != null && !request.templateName().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("documentTemplate").get("templateName")),
                        "%" + request.templateName().toLowerCase() + "%"
                ));
            }

            // 6. templateDescription — GIN trigram: idx_dt_desc_lower_trgm
            if (request.templateDescription() != null && !request.templateDescription().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("documentTemplate").get("description")),
                        "%" + request.templateDescription().toLowerCase() + "%"
                ));
            }

            // 7. createdAfter — B-tree: idx_document_created_at
            if (request.createdAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        request.createdAfter().atStartOfDay()
                ));
            }

            // 8. createdBefore — B-tree: idx_document_created_at
            if (request.createdBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"),
                        LocalDateTime.of(request.createdBefore(), LocalTime.MAX)
                ));
            }

            /*
             * 9. labelValues — correlated EXISTS per term (AND intersection).
             * For each term we add:
             *   AND EXISTS (
             *       SELECT 1 FROM document_field_value dfv
             *       WHERE dfv.document_id = d.id
             *         AND LOWER(dfv.field_value) LIKE '%<term>%'
             *   )
             * PostgreSQL uses idx_dfv_value_lower_trgm (GIN) + FK index per probe.
             */
            if (request.labelValues() != null) {
                for (String term : request.labelValues()) {
                    if (term == null || term.isBlank()) continue;

                    Subquery<Long> sub = query.subquery(Long.class);
                    var dfvRoot = sub.from(DocumentFieldValue.class);
                    sub.select(cb.literal(1L))
                            .where(
                                    cb.equal(dfvRoot.get("document"), root),
                                    cb.like(
                                            cb.lower(dfvRoot.get("fieldValue")),
                                            "%" + term.toLowerCase() + "%"
                                    )
                            );
                    predicates.add(cb.exists(sub));
                }
            }

            return predicates.isEmpty() ? cb.conjunction()
                                        : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
