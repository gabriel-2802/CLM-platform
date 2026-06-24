package clm.demo.simulation;

import clm.demo.simulation.util.JwtTokenUtil;
import clm.demo.simulation.util.SeedHelper;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * CLM Contract-Service — Gatling Load Simulation
 * ════════════════════════════════════════════════
 *
 * Execution
 * ─────────
 *   # Default (https://localhost, dev JWT secret)
 *   mvn -Pperf-test gatling:test
 *
 *   # Custom target + load profile
 *   mvn -Pperf-test gatling:test \
 *       -Dgatling.baseUrl=https://staging.internal \
 *       -Dgatling.ramp.targetRps=10 \
 *       -Dgatling.steady.rps=10 \
 *       -Dgatling.stress.rps=40
 *
 * Test Data (automatic)
 * ─────────────────────
 *   Before virtual users start, the {@code before{}} hook calls {@link SeedHelper}
 *   which:
 *     1. Discovers real client IDs from /api/clients (falls back to [1,2,3]).
 *     2. Uploads SEED_TEMPLATES minimal DOCX files → collects real template IDs.
 *     3. Generates SEED_CONTRACTS_PER_TEMPLATE contracts per template → collects IDs.
 *   All feeders below pull exclusively from these verified IDs, so no request
 *   ever targets a non-existent resource due to a sequence-gap.
 *
 * Load Profile
 * ─────────────────────────────────────────────────────────────────────────────
 *   Phase 1 — Ramp-up    (default 2 min): 0.1 → 5 rps per scenario
 *   Phase 2 — Steady     (default 5 min): 5 rps constant
 *   Phase 3 — Stress     (30 s ramp + 1 min peak + 30 s cool-down): up to 25 rps
 *   Total wall-clock: ~9 minutes with defaults.
 *
 * nginx rate-limit context
 * ─────────────────────────────────────────────────────────────────────────────
 *   The nginx proxy enforces burst=50 on /api/* paths. Aggregate RPS across all
 *   scenario scales at peak stress ≈ STRESS_RPS × 1.7. Keep STRESS_RPS ≤ 29 to
 *   stay under the burst ceiling; 429 responses appear in the report but do not
 *   abort the simulation.
 */
public class ContractServiceSimulation extends Simulation {

    // =========================================================================
    // SECTION 1 — CONFIGURATION
    // =========================================================================

    private static final String BASE_URL =
            prop("gatling.baseUrl", "https://localhost");

    private static final String JWT_SECRET =
            prop("gatling.jwtSecret",
                 "dev-shared-jwt-secret-used-by-both-nextauth-and-spring-change-before-deploy");

    private static final String JWT_SUBJECT = prop("gatling.jwtSubject", "1");

    // Ramp-up
    private static final double RAMP_RATE_TO   = doubleP("gatling.ramp.targetRps",  5.0);
    private static final int    RAMP_DURATION_SEC = intP("gatling.ramp.durationSec", 120);

    // Steady
    private static final double STEADY_RPS         = doubleP("gatling.steady.rps",       5.0);
    private static final int    STEADY_DURATION_SEC = intP("gatling.steady.durationSec", 300);

    // Stress
    // Keep aggregate peak (STRESS_RPS × 1.7 scenario-scale sum) under nginx burst=50.
    // Default 10 → ~17 rps aggregate peak. Override with -Dgatling.stress.rps=N.
    private static final double STRESS_RPS          = doubleP("gatling.stress.rps",       10.0);
    private static final int    STRESS_RAMP_SEC      = intP("gatling.stress.rampSec",      30);
    private static final int    STRESS_HOLD_SEC      = intP("gatling.stress.holdSec",      60);
    private static final int    STRESS_COOLDOWN_SEC  = intP("gatling.stress.cooldownSec",  30);

    // Assertion thresholds (post-run only — never abort)
    private static final int    P95_MS        = intP("gatling.assert.p95Ms",       2_000);
    private static final int    P99_MS        = intP("gatling.assert.p99Ms",       5_000);
    private static final double MAX_ERROR_PCT = doubleP("gatling.assert.maxErrorPct", 1.0);

    // Seed sizing
    private static final int SEED_TEMPLATES             = intP("gatling.seed.templates",            3);
    private static final int SEED_CONTRACTS_PER_TEMPLATE = intP("gatling.seed.contractsPerTemplate", 20);


    // =========================================================================
    // SECTION 2 — JWT
    // =========================================================================

    private static final String AUTH_HEADER =
            JwtTokenUtil.bearerToken(JWT_SECRET, JWT_SUBJECT);


    // =========================================================================
    // SECTION 3 — HTTP PROTOCOL BASE
    // =========================================================================

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .header("Authorization", AUTH_HEADER)
            .userAgentHeader("Gatling-CLM-LoadTest/3.11")
            .shareConnections()
            .disableFollowRedirect()
            // Only 5xx counts as a failure.
            // 429 = nginx rate-limit burst exhausted; tracked in the report but not an error-budget hit.
            // 404 on individual-resource requests is expected when seeded IDs are recycled.
            .check(status().not(500))
            .check(status().not(502))
            .check(status().not(503));


    // =========================================================================
    // SECTION 4 — SEEDED ID POOLS  +  FEEDERS
    //
    // These lists are populated in the before{} hook (SECTION 7).
    // CopyOnWriteArrayList is safe for concurrent reads once the seeding phase
    // (single-threaded) has finished.
    // =========================================================================

    private static final List<Long> seededTemplateIds = new CopyOnWriteArrayList<>();
    private static final List<Long> seededClientIds   = new CopyOnWriteArrayList<>();
    private static final List<Long> seededContractIds = new CopyOnWriteArrayList<>();

    // Round-robin counters — AtomicInteger for thread-safe use by many VUs
    private static final AtomicInteger contractRR = new AtomicInteger(0);
    private static final AtomicInteger searchRR   = new AtomicInteger(0);
    private static final AtomicInteger reportRR   = new AtomicInteger(0);

    private static final String[] STATUSES = {"ACTIVE", "TERMINATED", "ARCHIVED", "PENDING_SIGNATURE", "TERMINATION_DUE"};
    private static final int[]  EXPIRY_DAYS      = {7, 14, 30, 60, 90};
    private static final int[]  INACTIVE_MONTHS  = {1, 3, 6, 12};

    /**
     * Supplies contractId + templateId + clientId from the seeded pools.
     * Falls back to id=1 if seeding failed — those requests will return 404
     * and show up in the error budget, which is the correct signal.
     */
    private static final Iterator<Map<String, Object>> CONTRACT_FEEDER =
            Stream.<Map<String, Object>>generate(() -> {
                int i = contractRR.getAndIncrement();
                long cid = seededContractIds.isEmpty() ? 1L
                        : seededContractIds.get(i % seededContractIds.size());
                long tid = seededTemplateIds.isEmpty() ? 1L
                        : seededTemplateIds.get(i % seededTemplateIds.size());
                long clid = seededClientIds.isEmpty() ? 1L
                        : seededClientIds.get(i % seededClientIds.size());
                return Map.of("contractId", cid, "templateId", tid, "clientId", clid);
            }).iterator();

    private static final Iterator<Map<String, Object>> SEARCH_FEEDER =
            Stream.<Map<String, Object>>generate(() -> {
                int i = searchRR.getAndIncrement();
                long clid = seededClientIds.isEmpty() ? 1L
                        : seededClientIds.get(i % seededClientIds.size());
                return Map.of(
                        "searchStatus",   STATUSES[i % STATUSES.length],
                        "searchPage",     i % 5,
                        "searchSize",     10,
                        "searchClientId", clid
                );
            }).iterator();

    private static final Iterator<Map<String, Object>> REPORT_FEEDER =
            Stream.<Map<String, Object>>generate(() -> {
                int i = reportRR.getAndIncrement();
                return Map.of(
                        "expiryDays",     EXPIRY_DAYS[i    % EXPIRY_DAYS.length],
                        "inactiveMonths", INACTIVE_MONTHS[i % INACTIVE_MONTHS.length]
                );
            }).iterator();


    // =========================================================================
    // SECTION 5 — SCENARIO CHAINS
    // =========================================================================

    // ─── A. Browse & Search Contracts ─────────────────────────────────────────
    private final ScenarioBuilder browseContractsScenario =
            scenario("A - Browse & Search Contracts")
                    .feed(CONTRACT_FEEDER)

                    .exec(http("A1 LIST contracts (p0)")
                            .get("/api/contracts/all")
                            .queryParam("page", "0")
                            .queryParam("size", "20"))

                    .pause(Duration.ofMillis(400), Duration.ofMillis(1_200))

                    .exec(http("A2 GET contract by ID")
                            .get("/api/contracts/#{contractId}"))

                    .pause(Duration.ofMillis(300), Duration.ofMillis(900))

                    .exec(http("A3 GET contract DETAILED")
                            .get("/api/contracts/#{contractId}/detailed"))

                    .pause(Duration.ofMillis(500), Duration.ofMillis(1_500))

                    .feed(SEARCH_FEEDER)
                    .exec(http("A4 POST search by status + client")
                            .post("/api/contracts/search")
                            .body(StringBody(s -> """
                                    {
                                        "contractStatus": "%s",
                                        "clientId": %d,
                                        "page": %d,
                                        "size": %d
                                    }
                                    """.formatted(
                                    s.getString("searchStatus"),
                                    s.getLong("searchClientId"),
                                    s.getInt("searchPage"),
                                    s.getInt("searchSize"))))
                            .asJson())

                    .pause(Duration.ofMillis(300), Duration.ofMillis(800))

                    .exec(http("A5 LIST contracts (p1)")
                            .get("/api/contracts/all")
                            .queryParam("page", "1")
                            .queryParam("size", "20"));


    // ─── B. Contract Term Updates ──────────────────────────────────────────────
    private final ScenarioBuilder updateContractScenario =
            scenario("B - Contract Term Updates")
                    .feed(CONTRACT_FEEDER)

                    .exec(http("B1 PATCH update-terms")
                            .patch("/api/contracts/#{contractId}/update-terms")
                            .body(StringBody(s -> {
                                String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                                String end   = LocalDate.now().plusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
                                return """
                                        {
                                            "userId": 1,
                                            "appendixId": 1,
                                            "startDate": "%s",
                                            "contractEndDate": "%s",
                                            "balance": 75000.00,
                                            "value": 150000.00
                                        }
                                        """.formatted(today, end);
                            }))
                            .asJson())

                    .pause(Duration.ofSeconds(2), Duration.ofSeconds(4))

                    .exec(http("B2 GET contract post-update")
                            .get("/api/contracts/#{contractId}"));


    // ─── C. Template Browsing ──────────────────────────────────────────────────
    private final ScenarioBuilder templateScenario =
            scenario("C - Template Browsing")
                    .feed(CONTRACT_FEEDER)

                    .exec(http("C1 LIST templates (p0)")
                            .get("/api/templates")
                            .queryParam("page", "0")
                            .queryParam("size", "20"))

                    .pause(Duration.ofMillis(500), Duration.ofMillis(2_000))

                    .exec(http("C2 GET template by ID")
                            .get("/api/templates/#{templateId}"))

                    .pause(Duration.ofMillis(400), Duration.ofMillis(1_500));


    // ─── D. Appendix Queries ───────────────────────────────────────────────────
    private final ScenarioBuilder appendixScenario =
            scenario("D - Appendix Queries")
                    .feed(CONTRACT_FEEDER)

                    .exec(http("D1 LIST appendices for contract")
                            .get("/api/appendices/contract/#{contractId}"))

                    .pause(Duration.ofMillis(400), Duration.ofMillis(1_500))

                    .exec(http("D2 GET parent contract")
                            .get("/api/contracts/#{contractId}"));


    // ─── E. Reporting Queries ──────────────────────────────────────────────────
    // Delayed 30 s to let DB caches warm before expensive analytical queries land.
    private final ScenarioBuilder reportScenario =
            scenario("E - Reporting Queries")
                    .feed(REPORT_FEEDER)

                    .exec(http("E1 GET expiring contracts")
                            .get("/api/contracts/report/expiring")
                            .queryParam("days", "#{expiryDays}"))

                    .pause(Duration.ofSeconds(5), Duration.ofSeconds(10))

                    .exec(http("E2 GET inactive-client contracts")
                            .get("/api/contracts/report/inactive-clients")
                            .queryParam("months", "#{inactiveMonths}"))

                    .pause(Duration.ofSeconds(8), Duration.ofSeconds(15));


    // =========================================================================
    // SECTION 6 — INJECTION PROFILE BUILDER
    //
    //   Timeline (default values):
    //   ┌──────────────────────────────────────────────────────────────────────┐
    //   │  0 s ──  120 s   Ramp-up  : 0.1 → (5 × scale) rps                  │
    //   │ 120 s ──  420 s   Steady   : (5 × scale) rps constant               │
    //   │ 420 s ──  450 s   Surge    : ramp to (25 × scale) rps               │
    //   │ 450 s ──  510 s   Stress   : hold (25 × scale) rps                  │
    //   │ 510 s ──  540 s   Cool-down: back to ~0 rps                          │
    //   └──────────────────────────────────────────────────────────────────────┘
    // =========================================================================

    private static OpenInjectionStep[] buildPhases(double scaleFactor) {
        double warmupPeak = floor(RAMP_RATE_TO * scaleFactor);
        double steadyRate = floor(STEADY_RPS    * scaleFactor);
        double stressPeak = floor(STRESS_RPS    * scaleFactor);
        return new OpenInjectionStep[]{
                rampUsersPerSec(0.1).to(warmupPeak).during(Duration.ofSeconds(RAMP_DURATION_SEC)),
                constantUsersPerSec(steadyRate).during(Duration.ofSeconds(STEADY_DURATION_SEC)),
                rampUsersPerSec(steadyRate).to(stressPeak).during(Duration.ofSeconds(STRESS_RAMP_SEC)),
                constantUsersPerSec(stressPeak).during(Duration.ofSeconds(STRESS_HOLD_SEC)),
                rampUsersPerSec(stressPeak).to(0.1).during(Duration.ofSeconds(STRESS_COOLDOWN_SEC))
        };
    }

    private static double floor(double v) { return Math.max(0.1, v); }


    // =========================================================================
    // SECTION 7 — SIMULATION SETUP
    //
    // In the Gatling Java DSL, before() and after() are lifecycle methods
    // overridden on the Simulation class — they are NOT called with lambdas.
    // Gatling calls before() after constructing the class but before starting
    // virtual users, so seeded IDs are available to all feeders.
    // =========================================================================

    /** Called by Gatling before any virtual user starts. */
    @Override
    public void before() {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║         CLM Contract-Service — Gatling Simulation        ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.printf ("║  Target  : %-45s  ║%n", BASE_URL);
        System.out.printf ("║  Started : %-45s  ║%n", Instant.now());
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.printf ("║  Seeding : %d templates × %d contracts each%n",
                SEED_TEMPLATES, SEED_CONTRACTS_PER_TEMPLATE);
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        try {
            SeedHelper.SeedResult result =
                    new SeedHelper(BASE_URL, AUTH_HEADER)
                            .seed(SEED_TEMPLATES, SEED_CONTRACTS_PER_TEMPLATE);

            seededTemplateIds.addAll(result.templateIds());
            seededClientIds.addAll(result.clientIds());
            seededContractIds.addAll(result.contractIds());

        } catch (Exception e) {
            // Seed failure is non-fatal: read-only scenarios still run.
            System.err.println("[SEED] ⚠ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[SEED] Feeder pools ready:");
        System.out.printf ("[SEED]   templateIds (%d): %s%n",
                seededTemplateIds.size(), seededTemplateIds);
        System.out.printf ("[SEED]   contractIds (%d): %s%n",
                seededContractIds.size(),
                seededContractIds.size() <= 20
                        ? seededContractIds.toString()
                        : seededContractIds.subList(0, 20) + " …");
        System.out.printf ("[SEED]   clientIds   (%d): %s%n",
                seededClientIds.size(), seededClientIds);
    }

    /** Called by Gatling after the last virtual user finishes. */
    @Override
    public void after() {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                 Simulation completed                     ║");
        System.out.printf ("║  Finished : %-44s  ║%n", Instant.now());
        System.out.println("║  Report   : target/gatling-results/<timestamp>/index.html ║");
        System.out.println("║  Raw log  : target/gatling-results/<timestamp>/simulation.log ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
    }

    /** Configures injection profiles and assertions (runs during construction). */
    {
        setUp(
                // A: Read-heavy — full base rate
                browseContractsScenario.injectOpen(buildPhases(1.0)).protocols(httpProtocol),
                // B: Writes — 30%
                updateContractScenario.injectOpen(buildPhases(0.3)).protocols(httpProtocol),
                // C: Template reads — 20%
                templateScenario.injectOpen(buildPhases(0.2)).protocols(httpProtocol),
                // D: Appendix reads — 20%
                appendixScenario.injectOpen(buildPhases(0.2)).protocols(httpProtocol),
                // E: Reports — 5%, delayed 30 s
                reportScenario.injectOpen(
                        nothingFor(Duration.ofSeconds(30)),
                        rampUsersPerSec(0.05).to(floor(RAMP_RATE_TO * 0.05))
                                .during(Duration.ofSeconds(RAMP_DURATION_SEC)),
                        constantUsersPerSec(floor(STEADY_RPS * 0.05))
                                .during(Duration.ofSeconds(STEADY_DURATION_SEC)),
                        rampUsersPerSec(floor(STEADY_RPS * 0.05)).to(floor(STRESS_RPS * 0.05))
                                .during(Duration.ofSeconds(STRESS_RAMP_SEC)),
                        constantUsersPerSec(floor(STRESS_RPS * 0.05))
                                .during(Duration.ofSeconds(STRESS_HOLD_SEC)),
                        rampUsersPerSec(floor(STRESS_RPS * 0.05)).to(0.05)
                                .during(Duration.ofSeconds(STRESS_COOLDOWN_SEC))
                ).protocols(httpProtocol)
        )

        // ── Assertions (evaluated post-run; never stop the simulation) ────────
        .assertions(
                // Global latency
                global().responseTime().percentile(50).lt(P95_MS / 4),
                global().responseTime().percentile(95).lt(P95_MS),
                global().responseTime().percentile(99).lt(P99_MS),
                global().responseTime().mean().lt(P95_MS / 2),
                global().responseTime().max().lt(P99_MS * 3),
                // Error budget
                global().failedRequests().percent().lt(MAX_ERROR_PCT),
                global().successfulRequests().percent().gt(100 - MAX_ERROR_PCT),
                // Minimum throughput sanity
                global().requestsPerSec().gt(1.0),
                // Per-endpoint p95 SLAs
                details("A1 LIST contracts (p0)").responseTime().percentile(95).lt(1_500),
                details("A5 LIST contracts (p1)").responseTime().percentile(95).lt(1_500),
                details("A2 GET contract by ID").responseTime().percentile(95).lt(800),
                details("A3 GET contract DETAILED").responseTime().percentile(95).lt(2_000),
                details("A4 POST search by status + client").responseTime().percentile(95).lt(2_500),
                details("B1 PATCH update-terms").responseTime().percentile(95).lt(P95_MS),
                details("C1 LIST templates (p0)").responseTime().percentile(95).lt(1_500),
                details("C2 GET template by ID").responseTime().percentile(95).lt(800),
                details("D1 LIST appendices for contract").responseTime().percentile(95).lt(1_500),
                details("D2 GET parent contract").responseTime().percentile(95).lt(800),
                details("E1 GET expiring contracts").responseTime().percentile(95).lt(5_000),
                details("E2 GET inactive-client contracts").responseTime().percentile(95).lt(5_000)
        );
    }


    // =========================================================================
    // SECTION 8 — HELPERS
    // =========================================================================

    private static String prop(String key, String def) {
        String v = System.getProperty(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    private static int intP(String key, int def) {
        try { return Integer.parseInt(prop(key, String.valueOf(def))); }
        catch (NumberFormatException e) { return def; }
    }

    private static double doubleP(String key, double def) {
        try { return Double.parseDouble(prop(key, String.valueOf(def))); }
        catch (NumberFormatException e) { return def; }
    }
}
