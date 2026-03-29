package clm.demo.services;

import clm.demo.models.enums.DocumentFormat;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileParserService Unit Tests")
@Slf4j
class FileServiceTest {

    @InjectMocks
    private FileParserService fileService;

    private static final String TEST_RESOURCES_PATH = "src/test/resources";

    private MultipartFile pdfFile;
    private MultipartFile docxFile;
    private MultipartFile emptyFile;
    private MultipartFile invalidFile;

    @BeforeEach
    void setUp() throws IOException {
        pdfFile = loadTestFile(
                TEST_RESOURCES_PATH + "/pdf_file.pdf",
                "pdf_file.pdf",
                "application/pdf"
        );
        docxFile = loadTestFile(
                TEST_RESOURCES_PATH + "/doc_file.docx",
                "doc_file.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );
        emptyFile = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]
        );
        invalidFile = new MockMultipartFile(
                "file", "invalid.txt", "text/plain", "Some text content".getBytes()
        );
    }

    @Test
    @DisplayName("Should parse DOCX file and extract placeholders")
    void testParseDocxFile() throws IOException {
        FileParserService.ParsedDocumentResponse response =
                fileService.parseTemplate(docxFile, DocumentFormat.DOCX);

        assertNotNull(response);
        assertNotNull(response.getDocumentText());
        assertFalse(response.getDocumentText().isBlank());
        assertTrue(response.getPlaceholderCount() >= 0);
        assertNotNull(response.getPlaceholders());
        assertEquals(response.getPlaceholderCount(), response.getPlaceholders().size());

        log.info("DOCX: {} chars, {} placeholders",
                response.getDocumentText().length(), response.getPlaceholderCount());
    }

    @Test
    @DisplayName("Should parse PDF file and extract placeholders")
    void testParsePdfFile() throws IOException {
        FileParserService.ParsedDocumentResponse response =
                fileService.parseTemplate(pdfFile, DocumentFormat.PDF);

        assertNotNull(response);
        assertNotNull(response.getDocumentText());
        assertFalse(response.getDocumentText().isBlank());
        assertTrue(response.getPlaceholderCount() >= 0);
        assertNotNull(response.getPlaceholders());
        assertEquals(response.getPlaceholderCount(), response.getPlaceholders().size());

        log.info("PDF: {} chars, {} placeholders",
                response.getDocumentText().length(), response.getPlaceholderCount());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for empty file")
    void testEmptyFile_ThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> fileService.parseTemplate(emptyFile, DocumentFormat.PDF)
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for file with no name")
    void testNoFileName_ThrowsException() {
        MultipartFile noNameFile = new MockMultipartFile(
                "file", "", "application/pdf", "content".getBytes()
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> fileService.parseTemplate(noNameFile, DocumentFormat.PDF)
        );
    }

    @Test
    @DisplayName("Placeholder indices should be consistent with document text")
    void testPlaceholderIndicesMatchDocumentText() throws IOException {
        FileParserService.ParsedDocumentResponse response =
                fileService.parseTemplate(docxFile, DocumentFormat.DOCX);

        String text = response.getDocumentText();
        response.getPlaceholders().forEach(ph -> {
            String extracted = text.substring(ph.getStartIndex(), ph.getEndIndex());
            assertEquals(ph.getPlaceholderText(), extracted,
                    "Placeholder at position " + ph.getPosition() + " does not match indices");
        });
    }

    private MultipartFile loadTestFile(String filePath, String fileName, String contentType)
            throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("Test file not found: " + path.toAbsolutePath());
        }
        return new MockMultipartFile("file", fileName, contentType, Files.readAllBytes(path));
    }
}