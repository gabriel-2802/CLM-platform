package clm.demo.specifications;

import clm.demo.dto.requests.SearchRequest;
import clm.demo.models.Contract;
import clm.demo.models.DocumentFieldValue;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builds dynamic JPA Specifications for contract search queries.
 * All predicates use AND logic. Trigram GIN indexes on the clm schema
 * are used for LIKE patterns; B-tree indexes for equality/range filters.
 */
@Component
public class ContractSpecification {

    private static final String FIELD_NOTES             = "notes";
    private static final String FIELD_CONTRACT_STATUS   = "contractStatus";
    private static final String FIELD_CLIENT_ID         = "clientId";
    private static final String FIELD_GENERATED_BY      = "generatedBy";
    private static final String FIELD_CREATED_AT        = "createdAt";
    private static final String FIELD_DOCUMENT_TEMPLATE = "documentTemplate";
    private static final String FIELD_TEMPLATE_NAME     = "templateName";
    private static final String FIELD_DESCRIPTION       = "description";
    private static final String FIELD_DOCUMENT          = "document";
    private static final String FIELD_FIELD_VALUE       = "fieldValue";

    private static String contains(String term) {
        return "%" + term.toLowerCase() + "%";
    }

    public Specification<Contract> buildSearchSpecification(SearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            boolean isDataQuery = !Long.class.equals(query.getResultType())
                    && !long.class.equals(query.getResultType());
            if (isDataQuery) {
                root.fetch(FIELD_DOCUMENT_TEMPLATE, JoinType.LEFT);
                query.distinct(true);
            }

            addTextPredicates(request, root, cb, predicates);
            addEqualityPredicates(request, root, cb, predicates);
            addDatePredicates(request, root, cb, predicates);
            addLabelValuePredicates(request, root, query, cb, predicates);

            return predicates.isEmpty()
                    ? cb.conjunction()
                    : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addTextPredicates(
            SearchRequest request, Root<Contract> root,
            CriteriaBuilder cb, List<Predicate> predicates) {

        if (Objects.nonNull(request.notes()) && !request.notes().isBlank()) {
            predicates.add(cb.like(cb.lower(root.get(FIELD_NOTES)), contains(request.notes())));
        }
        if (Objects.nonNull(request.templateName()) && !request.templateName().isBlank()) {
            predicates.add(cb.like(
                    cb.lower(root.get(FIELD_DOCUMENT_TEMPLATE).get(FIELD_TEMPLATE_NAME)),
                    contains(request.templateName())
            ));
        }
        if (Objects.nonNull(request.templateDescription()) && !request.templateDescription().isBlank()) {
            predicates.add(cb.like(
                    cb.lower(root.get(FIELD_DOCUMENT_TEMPLATE).get(FIELD_DESCRIPTION)),
                    contains(request.templateDescription())
            ));
        }
    }

    private void addEqualityPredicates(
            SearchRequest request, Root<Contract> root,
            CriteriaBuilder cb, List<Predicate> predicates) {

        if (Objects.nonNull(request.contractStatus())) {
            predicates.add(cb.equal(root.get(FIELD_CONTRACT_STATUS), request.contractStatus()));
        }
        if (Objects.nonNull(request.clientId())) {
            predicates.add(cb.equal(root.get(FIELD_CLIENT_ID), request.clientId()));
        }
        if (Objects.nonNull(request.generatedBy())) {
            predicates.add(cb.equal(root.get(FIELD_GENERATED_BY), request.generatedBy()));
        }
    }

    private void addDatePredicates(
            SearchRequest request, Root<Contract> root,
            CriteriaBuilder cb, List<Predicate> predicates) {

        if (Objects.nonNull(request.createdAfter())) {
            predicates.add(cb.greaterThanOrEqualTo(
                    root.get(FIELD_CREATED_AT),
                    request.createdAfter().atStartOfDay()
            ));
        }
        if (Objects.nonNull(request.createdBefore())) {
            predicates.add(cb.lessThanOrEqualTo(
                    root.get(FIELD_CREATED_AT),
                    LocalDateTime.of(request.createdBefore(), LocalTime.MAX)
            ));
        }
    }

    /**
     * Correlated EXISTS per term (AND intersection).
     * For each term we add:
     * <pre>
     *   AND EXISTS (
     *       SELECT 1 FROM document_field_value dfv
     *       WHERE dfv.document_id = d.id
     *         AND LOWER(dfv.field_value) LIKE '%{term}%'
     *   )
     * </pre>
     * PostgreSQL uses idx_dfv_value_lower_trgm (GIN) + FK index per probe.
     */
    private void addLabelValuePredicates(
            SearchRequest request, Root<Contract> root,
            CriteriaQuery<?> query, CriteriaBuilder cb, List<Predicate> predicates) {

        if (Objects.isNull(request.labelValues())) {
            return;
        }

        for (String term : request.labelValues()) {
            if (Objects.isNull(term) || term.isBlank()) {
                continue;
            }

            Subquery<Long> sub = query.subquery(Long.class);
            Root<DocumentFieldValue> dfvRoot = sub.from(DocumentFieldValue.class);
            sub.select(cb.literal(1L))
                    .where(
                            cb.equal(dfvRoot.get(FIELD_DOCUMENT), root),
                            cb.like(cb.lower(dfvRoot.get(FIELD_FIELD_VALUE)), contains(term))
                    );
            predicates.add(cb.exists(sub));
        }
    }
}