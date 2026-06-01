package clm.demo.unit.service;

import clm.demo.models.ContractDetails;
import clm.demo.services.ContractService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContractServiceActiveDetailsTest {

    // ── getCurentlyActiveContractDetails ──────────────────────────────────────

    @Test
    void should_return_single_detail_when_only_one_exists() {
        ContractDetails details = details(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 1),
                LocalDateTime.now()
        );
        assertThat(ContractService.getCurentlyActiveContractDetails(List.of(details)))
                .isSameAs(details);
    }

    @Test
    void should_throw_when_list_is_empty() {
        assertThatThrownBy(() -> ContractService.getCurentlyActiveContractDetails(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least one ContractDetails");
    }

    @Test
    void should_throw_when_list_is_null() {
        assertThatThrownBy(() -> ContractService.getCurentlyActiveContractDetails(null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_return_most_recent_active_details_when_multiple_exist() {
        LocalDate today = LocalDate.now();

        ContractDetails old = details(
                today.minusDays(30),
                today.plusYears(1),
                LocalDateTime.now().minusDays(10)
        );
        ContractDetails recent = details(
                today.minusDays(5),
                today.plusYears(1),
                LocalDateTime.now().minusDays(1)
        );

        assertThat(ContractService.getCurentlyActiveContractDetails(List.of(old, recent)))
                .isSameAs(recent);
    }

    @Test
    void should_throw_when_no_detail_covers_today() {
        LocalDate future = LocalDate.now().plusYears(1);

        // Two future-dated details — neither covers today, and size > 1 so the date check runs
        ContractDetails futureDetail1 = details(future, future.plusYears(1), LocalDateTime.now());
        ContractDetails futureDetail2 = details(future.plusDays(1), future.plusYears(2), LocalDateTime.now().minusSeconds(1));

        assertThatThrownBy(() -> ContractService.getCurentlyActiveContractDetails(List.of(futureDetail1, futureDetail2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No active ContractDetails found");
    }

    @Test
    void should_return_first_when_list_has_exactly_one_element() {
        ContractDetails only = details(
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2030, 12, 31),
                LocalDateTime.now()
        );
        ContractDetails result = ContractService.getCurentlyActiveContractDetails(List.of(only));
        assertThat(result).isSameAs(only);
    }

    @ParameterizedTest
    @MethodSource("multipleDetailsScenarios")
    void should_select_most_recent_created_among_currently_valid_details(
            List<ContractDetails> detailsList, int expectedIndex) {
        ContractDetails result = ContractService.getCurentlyActiveContractDetails(detailsList);
        assertThat(result).isSameAs(detailsList.get(expectedIndex));
    }

    static Stream<Object[]> multipleDetailsScenarios() {
        LocalDate today = LocalDate.now();
        ContractDetails d1 = details(today.minusDays(20), today.plusDays(100), LocalDateTime.now().minusDays(20));
        ContractDetails d2 = details(today.minusDays(10), today.plusDays(100), LocalDateTime.now().minusDays(10));
        ContractDetails d3 = details(today.minusDays(5),  today.plusDays(100), LocalDateTime.now().minusDays(5));
        return Stream.of(
                new Object[]{List.of(d1, d2, d3), 2},
                new Object[]{List.of(d3, d2, d1), 0}
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static ContractDetails details(LocalDate start, LocalDate end, LocalDateTime createdAt) {
        return ContractDetails.builder()
                .contractValue(BigDecimal.valueOf(10_000))
                .contractBalance(BigDecimal.valueOf(10_000))
                .startDate(start)
                .endDate(end)
                .createdAt(createdAt)
                .createdByUserId(1)
                .build();
    }
}
