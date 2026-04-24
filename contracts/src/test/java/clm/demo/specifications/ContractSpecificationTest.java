package clm.demo.specifications;

import clm.demo.dto.requests.SearchRequest;
import clm.demo.models.Contract;
import clm.demo.models.enums.ContractStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * verifies that ContractSpecification.buildSearchSpecification adds the correct
 * predicate type for each filter field and returns a conjunction when all fields
 * are null. uses mocked JPA Criteria API — no database required.
 */
@ExtendWith(MockitoExtension.class)
class ContractSpecificationTest {

    ContractSpecification spec = new ContractSpecification();

    @Mock CriteriaBuilder          cb;
    @SuppressWarnings("rawtypes")
    @Mock CriteriaQuery            query;
    @SuppressWarnings("rawtypes")
    @Mock Root                     root;

    Predicate sentinel = mock(Predicate.class);

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // treat as a data query (not a COUNT query) by default
        when(query.getResultType()).thenReturn(Contract.class);

        // fetch() is called for data queries — return a harmless null stub
        org.mockito.Mockito.lenient()
                .when(root.fetch(anyString(), any())).thenReturn(null);

        // deep stubs allow root.get("documentTemplate").get("templateName") to work
        Path<?> deepPath = mock(Path.class, RETURNS_DEEP_STUBS);
        org.mockito.Mockito.lenient()
                .when(root.get(anyString())).thenReturn(deepPath);

        // expression / predicate stubs
        org.mockito.Mockito.lenient()
                .when(cb.lower(any())).thenReturn(mock(Expression.class));
        org.mockito.Mockito.lenient()
                .when(cb.like(any(), anyString())).thenReturn(sentinel);
        org.mockito.Mockito.lenient()
                .when(cb.equal(any(), any())).thenReturn(sentinel);
        org.mockito.Mockito.lenient()
                .when(cb.greaterThanOrEqualTo(any(), any(LocalDateTime.class))).thenReturn(sentinel);
        org.mockito.Mockito.lenient()
                .when(cb.lessThanOrEqualTo(any(), any(LocalDateTime.class))).thenReturn(sentinel);
        org.mockito.Mockito.lenient()
                .when(cb.and(any(Predicate[].class))).thenReturn(sentinel);
        org.mockito.Mockito.lenient()
                .when(cb.conjunction()).thenReturn(sentinel);
        org.mockito.Mockito.lenient()
                .when(cb.exists(any())).thenReturn(sentinel);
        org.mockito.Mockito.lenient()
                .when(cb.literal(any())).thenReturn(mock(Expression.class));
        org.mockito.Mockito.lenient()
                .when(query.subquery(any(Class.class))).thenReturn(mock(Subquery.class, RETURNS_DEEP_STUBS));
    }

    // ================================================================== //
    //  empty request                                                       //
    // ================================================================== //

    @Nested
    class EmptyRequest {

        @Test
        @SuppressWarnings("unchecked")
        void all_null_fields_return_conjunction() {
            SearchRequest req = allNull();

            Predicate result = spec.buildSearchSpecification(req).toPredicate(root, query, cb);

            verify(cb).conjunction();
            verify(cb, never()).and(any(Predicate[].class));
            assertThat(result).isSameAs(sentinel);
        }
    }

    // ================================================================== //
    //  individual filters                                                  //
    // ================================================================== //

    @Nested
    class IndividualFilters {

        @Test
        @SuppressWarnings("unchecked")
        void notes_filter_adds_like_predicate_with_lowercase_wrapped_value() {
            SearchRequest req = req(r -> r.notes("Acme"));

            spec.buildSearchSpecification(req).toPredicate(root, query, cb);

            verify(cb).like(any(), eq("%acme%"));
        }

        @Test
        @SuppressWarnings("unchecked")
        void blank_notes_does_not_add_any_predicate() {
            SearchRequest req = req(r -> r.notes("   "));

            spec.buildSearchSpecification(req).toPredicate(root, query, cb);

            verify(cb, never()).like(any(), anyString());
            verify(cb).conjunction();
        }

        @Test
        @SuppressWarnings("unchecked")
        void contract_status_filter_adds_equal_predicate() {
            SearchRequest req = req(r -> r.contractStatus(ContractStatus.ACTIVE));

            spec.buildSearchSpecification(req).toPredicate(root, query, cb);

            verify(cb).equal(any(), eq(ContractStatus.ACTIVE));
        }

        @Test
        @SuppressWarnings("unchecked")
        void client_id_filter_adds_equal_predicate() {
            SearchRequest req = req(r -> r.clientId(42));

            spec.buildSearchSpecification(req).toPredicate(root, query, cb);

            verify(cb).equal(any(), eq(42));
        }

        @Test
        @SuppressWarnings("unchecked")
        void generated_by_filter_adds_equal_predicate() {
            SearchRequest req = req(r -> r.generatedBy(10));

            spec.buildSearchSpecification(req).toPredicate(root, query, cb);

            verify(cb).equal(any(), eq(10));
        }

        @Test
        @SuppressWarnings("unchecked")
        void template_name_filter_adds_like_predicate_lowercased() {
            SearchRequest req = req(r -> r.templateName("NDA"));

            spec.buildSearchSpecification(req).toPredicate(root, query, cb);

            verify(cb).like(any(), eq("%nda%"));
        }

        @Test
        @SuppressWarnings("unchecked")
        void blank_template_name_does_not_add_predicate() {
            SearchRequest req = req(r -> r.templateName("  "));

            spec.buildSearchSpecification(req).toPredicate(root, query, cb);

            verify(cb, never()).like(any(), anyString());
        }

        @Test
        @SuppressWarnings("unchecked")
        void template_description_filter_adds_like_predicate() {
            SearchRequest req = req(r -> r.templateDescription("service"));

            spec.buildSearchSpecification(req).toPredicate(root, query, cb);

            verify(cb).like(any(), eq("%service%"));
        }

        @Test
        @SuppressWarnings("unchecked")
        void created_after_filter_adds_greater_than_or_equal_predicate() {
            SearchRequest req = req(r -> r.createdAfter(LocalDate.of(2025, 1, 1)));

            spec.buildSearchSpecification(req).toPredicate(root, query, cb);

            verify(cb).greaterThanOrEqualTo(any(), any(LocalDateTime.class));
        }

        @Test
        @SuppressWarnings("unchecked")
        void created_before_filter_adds_less_than_or_equal_predicate() {
            SearchRequest req = req(r -> r.createdBefore(LocalDate.of(2025, 12, 31)));

            spec.buildSearchSpecification(req).toPredicate(root, query, cb);

            verify(cb).lessThanOrEqualTo(any(), any(LocalDateTime.class));
        }

        @Test
        @SuppressWarnings("unchecked")
        void label_values_filter_adds_exists_subquery_per_term() {
            SearchRequest req = req(r -> r.labelValues(List.of("Acme", "Corp")));

            spec.buildSearchSpecification(req).toPredicate(root, query, cb);

            // one EXISTS subquery per non-blank term
            verify(cb, atLeastOnce()).exists(any());
        }

        @Test
        @SuppressWarnings("unchecked")
        void blank_label_value_term_is_skipped() {
            SearchRequest req = req(r -> r.labelValues(List.of("  ")));

            spec.buildSearchSpecification(req).toPredicate(root, query, cb);

            verify(cb, never()).exists(any());
            verify(cb).conjunction();
        }

        @Test
        @SuppressWarnings("unchecked")
        void null_label_value_term_is_skipped() {
            SearchRequest req = req(r -> r.labelValues(List.of()));

            spec.buildSearchSpecification(req).toPredicate(root, query, cb);

            verify(cb, never()).exists(any());
        }
    }

    // ================================================================== //
    //  count query — no fetch                                              //
    // ================================================================== //

    @Nested
    class CountQuery {

        @Test
        @SuppressWarnings("unchecked")
        void count_query_skips_fetch_and_distinct() {
            when(query.getResultType()).thenReturn(Long.class);
            SearchRequest req = allNull();

            spec.buildSearchSpecification(req).toPredicate(root, query, cb);

            verify(root, never()).fetch(anyString(), any());
        }
    }

    // ================================================================== //
    //  multiple filters combined                                           //
    // ================================================================== //

    @Nested
    class CombinedFilters {

        @Test
        @SuppressWarnings("unchecked")
        void two_active_filters_produce_and_predicate() {
            SearchRequest req = req(r -> r.contractStatus(ContractStatus.ACTIVE).clientId(42));

            spec.buildSearchSpecification(req).toPredicate(root, query, cb);

            // cb.and(...) called when predicates list is non-empty
            verify(cb).and(any(Predicate[].class));
        }
    }

    // ------------------------------------------------------------------ //
    //  DSL helpers for building SearchRequest with selective field overrides //
    // ------------------------------------------------------------------ //

    /** returns a SearchRequest with all fields null. */
    private SearchRequest allNull() {
        return new SearchRequest(null, null, null, null, null, null, null, null, null, null, null);
    }

    /** builds a SearchRequest by applying a consumer to a mutable builder. */
    private SearchRequest req(java.util.function.Consumer<RequestBuilder> configure) {
        RequestBuilder b = new RequestBuilder();
        configure.accept(b);
        return b.build();
    }

    /** mutable builder for SearchRequest — keeps tests readable. */
    static class RequestBuilder {
        String         notes;
        ContractStatus contractStatus;
        Integer        clientId;
        Integer        generatedBy;
        List<String>   labelValues;
        String         templateName;
        String         templateDescription;
        LocalDate      createdAfter;
        LocalDate      createdBefore;

        RequestBuilder notes(String v)               { notes = v;               return this; }
        RequestBuilder contractStatus(ContractStatus v) { contractStatus = v;   return this; }
        RequestBuilder clientId(Integer v)           { clientId = v;            return this; }
        RequestBuilder generatedBy(Integer v)        { generatedBy = v;         return this; }
        RequestBuilder labelValues(List<String> v)   { labelValues = v;         return this; }
        RequestBuilder templateName(String v)        { templateName = v;        return this; }
        RequestBuilder templateDescription(String v) { templateDescription = v; return this; }
        RequestBuilder createdAfter(LocalDate v)     { createdAfter = v;        return this; }
        RequestBuilder createdBefore(LocalDate v)    { createdBefore = v;       return this; }

        SearchRequest build() {
            return new SearchRequest(notes, contractStatus, clientId, generatedBy,
                    labelValues, templateName, templateDescription,
                    createdAfter, createdBefore, null, null);
        }
    }
}
