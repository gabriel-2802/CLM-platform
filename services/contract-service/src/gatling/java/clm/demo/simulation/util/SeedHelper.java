package clm.demo.simulation.util;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates real test data in the application before the Gatling simulation starts.
 *
 * Called from the simulation's {@code before {}} hook so that all virtual-user
 * feeders receive IDs that actually exist in the database.
 *
 * Sequence:
 *   1. Discover existing client IDs via /api/clients (best-effort; falls back to [1,2,3]).
 *   2. Upload {@code numTemplates} minimal DOCX files to /api/templates/upload.
 *   3. Generate {@code contractsPerTemplate} contracts for each created template.
 *
 * Apache POI (already a compile-scope dependency) is used to build the DOCX bytes
 * in-memory so no test fixture files are needed on disk.
 * java.net.http.HttpClient handles the HTTP calls with a trust-all SSLContext,
 * because nginx uses a self-signed certificate at https://localhost.
 */
public class SeedHelper {

    private static final String CRLF = "\r\n";
    private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");

    public record SeedResult(List<Long> templateIds, List<Long> clientIds, List<Long> contractIds) {}

    private final String baseUrl;
    private final String authHeader;
    private final HttpClient http;

    public SeedHelper(String baseUrl, String authHeader) throws Exception {
        this.baseUrl    = baseUrl;
        this.authHeader = authHeader;
        this.http = HttpClient.newBuilder()
                .sslContext(trustAllSslContext())
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Seeds the database and returns the created IDs.
     *
     * @param numTemplates          number of templates to upload
     * @param contractsPerTemplate  number of contracts to generate per template
     */
    public SeedResult seed(int numTemplates, int contractsPerTemplate) {
        System.out.printf("[SEED] Creating %d template(s) × %d contract(s) each …%n",
                numTemplates, contractsPerTemplate);

        List<Long> clientIds = discoverClientIds();
        System.out.printf("[SEED] Client IDs resolved: %s%n", clientIds);

        List<Long> templateIds = new ArrayList<>();
        for (int i = 1; i <= numTemplates; i++) {
            try {
                Long tid = createTemplate(
                        "Gatling Template " + i,
                        "Auto-generated for load test run @" + System.currentTimeMillis());
                if (tid != null) {
                    templateIds.add(tid);
                    System.out.printf("[SEED] ✓ Template id=%d (%d/%d)%n", tid, i, numTemplates);
                }
            } catch (Exception e) {
                System.err.printf("[SEED] ✗ Template %d failed: %s%n", i, e.getMessage());
            }
        }

        if (templateIds.isEmpty()) {
            System.err.println("[SEED] ✗ No templates created — contract generation will be skipped.");
            System.err.println("[SEED]   Read-only scenarios (LIST, SEARCH, REPORTS) still run.");
            return new SeedResult(templateIds, clientIds, new ArrayList<>());
        }

        List<Long> contractIds = new ArrayList<>();
        int seq = 0;
        for (Long tid : templateIds) {
            for (int c = 0; c < contractsPerTemplate; c++) {
                Long clientId = clientIds.get(seq % clientIds.size());
                try {
                    Long cid = createContract(tid, clientId);
                    if (cid != null) {
                        contractIds.add(cid);
                        System.out.printf("[SEED] ✓ Contract id=%d (template=%d, client=%d)%n",
                                cid, tid, clientId);
                    }
                } catch (Exception e) {
                    System.err.printf("[SEED] ✗ Contract (template=%d, client=%d) failed: %s%n",
                            tid, clientId, e.getMessage());
                }
                seq++;
            }
        }

        System.out.printf("[SEED] Done — templates=%d  contracts=%d  clients=%d%n",
                templateIds.size(), contractIds.size(), clientIds.size());
        return new SeedResult(templateIds, clientIds, contractIds);
    }

    // =========================================================================
    // Client discovery
    // =========================================================================

    private List<Long> discoverClientIds() {
        try {
            HttpRequest req = get("/api/clients?page=0&size=20");
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                List<Long> ids = extractAllIds(resp.body());
                if (!ids.isEmpty()) return ids;
            }
            System.err.printf("[SEED] /api/clients returned HTTP %d — using fallback ids%n",
                    resp.statusCode());
        } catch (Exception e) {
            System.err.printf("[SEED] Cannot reach /api/clients (%s) — using fallback ids [1,2,3]%n",
                    e.getMessage());
        }
        return List.of(1L, 2L, 3L);
    }

    // =========================================================================
    // Template creation
    // =========================================================================

    private Long createTemplate(String name, String description) throws Exception {
        byte[] docxBytes  = createMinimalDocx(name);
        String boundary   = "GatlingBoundary" + System.nanoTime();
        byte[] bodyBytes  = buildMultipartBody(boundary, docxBytes, "test-template.docx", name, description);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/templates/upload"))
                .header("Authorization", authHeader)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(bodyBytes))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 201 || resp.statusCode() == 200) {
            return extractFirstId(resp.body());
        }
        System.err.printf("[SEED] Template upload → HTTP %d: %.300s%n", resp.statusCode(), resp.body());
        return null;
    }

    // =========================================================================
    // Contract creation
    // =========================================================================

    private Long createContract(Long templateId, Long clientId) throws Exception {
        String today   = LocalDate.now().toString();
        String endDate = LocalDate.now().plusYears(2).toString();
        // mappings is an empty object — valid because the test DOCX has no placeholder fields.
        String body = """
                {
                    "templateId": %d,
                    "userId": 1,
                    "clientId": %d,
                    "startDate": "%s",
                    "endDate": "%s",
                    "mappings": {},
                    "autoRenew": false,
                    "contractBalance": 50000.00,
                    "value": 50000.00,
                    "notes": "Gatling load-test contract — safe to delete"
                }
                """.formatted(templateId, clientId, today, endDate);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/contracts/generate"))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 201 || resp.statusCode() == 200) {
            return extractFirstId(resp.body());
        }
        System.err.printf("[SEED] Contract generate → HTTP %d: %.300s%n", resp.statusCode(), resp.body());
        return null;
    }

    // =========================================================================
    // DOCX generation (Apache POI)
    // =========================================================================

    private static byte[] createMinimalDocx(String title) throws IOException {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFRun heading = doc.createParagraph().createRun();
            heading.setBold(true);
            heading.setFontSize(14);
            heading.setText(title);

            doc.createParagraph().createRun()
                    .setText("Document auto-generated by Gatling for load-testing. Safe to delete.");
            doc.createParagraph().createRun()
                    .setText("Generated: " + LocalDate.now());

            doc.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    // Multipart body builder
    // =========================================================================

    private static byte[] buildMultipartBody(String boundary, byte[] fileBytes,
                                              String filename, String templateName,
                                              String description) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // file part
        appendFilePart(out, boundary, "file", filename,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                fileBytes);
        // templateName part
        appendTextPart(out, boundary, "templateName", templateName);
        // description part (optional)
        if (description != null && !description.isBlank()) {
            appendTextPart(out, boundary, "description", description);
        }
        // closing boundary
        out.write(("--" + boundary + "--" + CRLF).getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }

    private static void appendFilePart(ByteArrayOutputStream out, String boundary,
                                        String field, String filename, String contentType,
                                        byte[] data) throws IOException {
        out.write(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"" + field
                   + "\"; filename=\"" + filename + "\"" + CRLF).getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Type: " + contentType + CRLF + CRLF).getBytes(StandardCharsets.UTF_8));
        out.write(data);
        out.write(CRLF.getBytes(StandardCharsets.UTF_8));
    }

    private static void appendTextPart(ByteArrayOutputStream out, String boundary,
                                        String name, String value) throws IOException {
        out.write(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"" + name + "\"" + CRLF + CRLF)
                .getBytes(StandardCharsets.UTF_8));
        out.write(value.getBytes(StandardCharsets.UTF_8));
        out.write(CRLF.getBytes(StandardCharsets.UTF_8));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private HttpRequest get(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Authorization", authHeader)
                .header("Accept", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    private static Long extractFirstId(String json) {
        Matcher m = ID_PATTERN.matcher(json);
        return m.find() ? Long.parseLong(m.group(1)) : null;
    }

    private static List<Long> extractAllIds(String json) {
        List<Long> ids = new ArrayList<>();
        Matcher m = ID_PATTERN.matcher(json);
        while (m.find()) ids.add(Long.parseLong(m.group(1)));
        return ids;
    }

    private static SSLContext trustAllSslContext() throws Exception {
        TrustManager[] trustAll = {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                public void checkClientTrusted(X509Certificate[] c, String a) {}
                public void checkServerTrusted(X509Certificate[] c, String a) {}
            }
        };
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, trustAll, new SecureRandom());
        return ctx;
    }
}
