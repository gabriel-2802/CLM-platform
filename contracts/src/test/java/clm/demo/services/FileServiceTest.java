package clm.demo.services;

import clm.demo.models.enums.DocumentFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("FileService Integration Tests")
@Slf4j
class FileServiceTest {

    @Autowired
    private FileParserService fileService;

    private ObjectMapper objectMapper;
    private static final String TEST_RESOURCES_PATH = "src/test/resources";
    private static final String OUTPUT_PATH = "target/test-results";

    /**
     *  Create output directory if it doesn't exist
     * @throws IOException if unable
     */
    @BeforeEach
    void setUp() throws IOException {
        objectMapper = new ObjectMapper();
        Files.createDirectories(Paths.get(OUTPUT_PATH));
    }

    @Test
    @DisplayName("Should parse DOCX file and save results as JSON")
    void testParseDocxFile() throws IOException {
        // mock file received via http
        String docxFilePath = TEST_RESOURCES_PATH + "/doc_file.docx";
        MultipartFile multipartFile = loadTestFile(docxFilePath, "doc_file.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        FileParserService.ParsedDocumentResponse response = fileService.parseTemplate(multipartFile, DocumentFormat.DOCX);

        assertNotNull(response);
        assertNotNull(response.getDocumentText());
        assertFalse(response.getDocumentText().isBlank());
        assertTrue(response.getPlaceholderCount() >= 0);
        assertNotNull(response.getPlaceholders());

        // save results as JSON
        String outputPath = OUTPUT_PATH + "/docx-parse-result.json";
        saveResultsAsJson(response, outputPath);
        log.info("DOCX test results saved to: {}", outputPath);

    }

    @Test
    @DisplayName("Should parse PDF file and save results as JSON")
    void testParsePdfFile() throws IOException {
        // mock file received via http
        String pdfFilePath = TEST_RESOURCES_PATH + "/pdf_file.pdf";
        MultipartFile multipartFile = loadTestFile(pdfFilePath, "pdf_file.pdf", "application/pdf");

        FileParserService.ParsedDocumentResponse response = fileService.parseTemplate(multipartFile, DocumentFormat.PDF);

        assertNotNull(response);
        assertNotNull(response.getDocumentText());
        assertFalse(response.getDocumentText().isBlank());
        assertTrue(response.getPlaceholderCount() >= 0);
        assertNotNull(response.getPlaceholders());

        // save results as JSON
        String outputPath = OUTPUT_PATH + "/pdf-parse-result.json";
        saveResultsAsJson(response, outputPath);
        log.info("PDF test results saved to: {}", outputPath);
    }

    /**
     * Loads a test file from the test resources directory and creates a MockMultipartFile.
     *
     * @param filePath   the path to the file relative to the project root
     * @param fileName   the name of the file
     * @param contentType the MIME type of the file
     * @return MockMultipartFile instance
     * @throws IOException if file cannot be read
     */
    private MultipartFile loadTestFile(String filePath, String fileName, String contentType) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("Test file not found: " + path.toAbsolutePath());
        }
        byte[] fileContent = Files.readAllBytes(path);
        return new MockMultipartFile(
                "file",
                fileName,
                contentType,
                fileContent
        );
    }

    /**
     * Saves the parsed document response as a formatted JSON file.
     *
     * @param response the ParsedDocumentResponse to serialize
     * @param outputPath the file path where JSON should be saved
     * @throws IOException if the file cannot be written
     */
    private void saveResultsAsJson(FileParserService.ParsedDocumentResponse response, String outputPath) throws IOException {
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        Files.writeString(Paths.get(outputPath), json);
    }
}

