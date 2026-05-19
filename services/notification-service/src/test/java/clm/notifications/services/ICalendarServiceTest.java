package clm.notifications.services;

import clm.notifications.dto.ContractSummaryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ICalendarServiceTest {

    ICalendarService service;

    @BeforeEach
    void setUp() {
        service = new ICalendarService();
    }

    // ──────────────────────────────────────────────────────────────────── //
    //  Structure                                                            //
    // ──────────────────────────────────────────────────────────────────── //

    @Nested
    class CalendarStructure {

        @Test
        void output_starts_and_ends_with_vcalendar_tags() {
            String ics = generate(contractExpiring("2027-06-01"));
            assertThat(ics).startsWith("BEGIN:VCALENDAR");
            assertThat(ics).endsWith("END:VCALENDAR\r\n");
        }

        @Test
        void output_contains_required_calendar_headers() {
            String ics = generate(contractExpiring("2027-06-01"));
            assertThat(ics).contains("VERSION:2.0");
            assertThat(ics).contains("CALSCALE:GREGORIAN");
            assertThat(ics).contains("METHOD:PUBLISH");
        }

        @Test
        void one_contract_produces_two_vevents() {
            String ics = generate(contractExpiring("2027-06-01"));
            long count = ics.lines().filter("BEGIN:VEVENT"::equals).count();
            assertThat(count).isEqualTo(2);
        }

        @Test
        void two_contracts_produce_four_vevents() {
            ContractSummaryDTO c1 = contractExpiring("2027-06-01");
            ContractSummaryDTO c2 = contractExpiring("2027-09-15");
            String ics = generate(c1, c2);
            long count = ics.lines().filter("BEGIN:VEVENT"::equals).count();
            assertThat(count).isEqualTo(4);
        }

        @Test
        void empty_list_produces_no_vevents() {
            String ics = new String(service.buildCalendarFile(List.of()));
            assertThat(ics).doesNotContain("BEGIN:VEVENT");
            assertThat(ics).contains("BEGIN:VCALENDAR");
        }

        @Test
        void contract_with_null_end_date_is_skipped() {
            ContractSummaryDTO c = new ContractSummaryDTO();
            c.setId(1L);
            c.setContractEndDate(null);

            String ics = new String(service.buildCalendarFile(List.of(c)));
            assertThat(ics).doesNotContain("BEGIN:VEVENT");
        }
    }

    // ──────────────────────────────────────────────────────────────────── //
    //  Dates                                                               //
    // ──────────────────────────────────────────────────────────────────── //

    @Nested
    class EventDates {

        @Test
        void expiry_event_starts_on_contract_end_date() {
            String ics = generate(contractExpiring("2027-06-01"));
            assertThat(ics).contains("DTSTART;VALUE=DATE:20270601");
        }

        @Test
        void expiry_event_ends_day_after_contract_end_date() {
            String ics = generate(contractExpiring("2027-06-01"));
            assertThat(ics).contains("DTEND;VALUE=DATE:20270602");
        }

        @Test
        void warning_event_starts_30_days_before_expiry() {
            // 2027-06-01 minus 30 days = 2027-05-02
            String ics = generate(contractExpiring("2027-06-01"));
            assertThat(ics).contains("DTSTART;VALUE=DATE:20270502");
        }

        @Test
        void warning_event_ends_day_after_warning_start() {
            String ics = generate(contractExpiring("2027-06-01"));
            assertThat(ics).contains("DTEND;VALUE=DATE:20270503");
        }
    }

    // ──────────────────────────────────────────────────────────────────── //
    //  Content                                                             //
    // ──────────────────────────────────────────────────────────────────── //

    @Nested
    class EventContent {

        @Test
        void expiry_event_summary_contains_client_id() {
            ContractSummaryDTO c = contractExpiring("2027-06-01");
            c.setId(42L);
            String ics = generate(c);
            assertThat(ics).contains("SUMMARY:Contract #42");
        }

        @Test
        void warning_event_summary_contains_warning_text() {
            String ics = generate(contractExpiring("2027-06-01"));
            assertThat(ics).contains("expiră în 30 de zile");
        }

        @Test
        void expiry_event_summary_contains_expiry_text() {
            String ics = generate(contractExpiring("2027-06-01"));
            assertThat(ics).contains("expiră azi");
        }

        @Test
        void both_events_contain_valarm() {
            String ics = generate(contractExpiring("2027-06-01"));
            long alarmCount = ics.lines().filter("BEGIN:VALARM"::equals).count();
            assertThat(alarmCount).isEqualTo(2);
        }

        @Test
        void result_is_valid_utf8_bytes() {
            ContractSummaryDTO c = contractExpiring("2027-06-01");
            c.setClientId(5);
            byte[] bytes = service.buildCalendarFile(List.of(c));
            String decoded = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            assertThat(decoded).contains("BEGIN:VCALENDAR");
        }
    }

    // ──────────────────────────────────────────────────────────────────── //
    //  Helpers                                                             //
    // ──────────────────────────────────────────────────────────────────── //

    private String generate(ContractSummaryDTO... contracts) {
        return new String(service.buildCalendarFile(List.of(contracts)));
    }

    private ContractSummaryDTO contractExpiring(String isoDate) {
        ContractSummaryDTO c = new ContractSummaryDTO();
        c.setId(1L);
        c.setClientId(10);
        c.setContractEndDate(LocalDate.parse(isoDate));
        c.setContractValue(BigDecimal.valueOf(10_000));
        return c;
    }
}
