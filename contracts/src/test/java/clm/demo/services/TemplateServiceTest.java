package clm.demo.services;

import clm.demo.dto.requests.UploadTemplateRequest;
import clm.demo.dto.responses.ParsedTemplateResponseDTO;
import clm.demo.dto.responses.TemplateResponseDTO;
import clm.demo.exceptions.EmptyFileNameException;
import clm.demo.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.UnsupportedFileException;
import clm.demo.mappers.ContractTemplateMapper;
import clm.demo.mappers.ParsedDocumentMapper;
import clm.demo.models.ContractTemplate;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.repositories.ContractTemplateRepository;
import clm.demo.repositories.TemplateFieldRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TemplateService covering upload, retrieval, and deletion functionality.
 * Uses actual test resource files (PDF and DOCX) from src/test/resources.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TemplateService Unit Tests")
public class TemplateServiceTest {

    @Mock
    private ContractTemplateRepository templateRepository;

    @Mock
    private TemplateFieldRepository templateFieldRepository;

    @Mock
    private FileParserService fileParserService;

    @Mock
    private ContractTemplateMapper contractTemplateMapper;

    @Mock
    private ParsedDocumentMapper parsedDocumentMapper;

    @Mock
    private FileZipService zipService;

    @InjectMocks
    private TemplateService templateService;

    private MultipartFile pdfFile;
    private MultipartFile docxFile;
    private MultipartFile emptyFile;
    private MultipartFile invalidFile;

    @BeforeEach
    void setUp() throws IOException {
        // load actual test files from classpath
        pdfFile = loadTestFile("pdf_file.pdf", "application/pdf");
        docxFile = loadTestFile("doc_file.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        // create mock files for error cases
        emptyFile = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);
        invalidFile = new MockMultipartFile("file", "invalid.txt", "text/plain", "Some text content".getBytes());
    }

    /**
     * Loads a test file from the classpath (src/test/resources)
     */
    private MultipartFile loadTestFile(String filename, String contentType) throws IOException {
        Resource resource = new ClassPathResource(filename);
        byte[] fileContent = Files.readAllBytes(resource.getFile().toPath());
        return new MockMultipartFile("file", filename, contentType, fileContent);
    }
    
    @Test
    @DisplayName("Should successfully upload PDF template with placeholders")
    void testUploadPdfTemplate_Success() throws IOException {
        // arrange
        UploadTemplateRequest request = new UploadTemplateRequest(
                pdfFile,
                "Service Agreement 2026",
                "Standard service agreement template"
        );

        byte[] compressedContent = "compressed-pdf-content".getBytes();
        byte[] originalContent = pdfFile.getBytes();

        FileParserService.PlaceholderInfo placeholder1 = FileParserService.PlaceholderInfo.builder()
                .position(0)
                .placeholderText("......")
                .startIndex(100)
                .endIndex(106)
                .build();

        FileParserService.PlaceholderInfo placeholder2 = FileParserService.PlaceholderInfo.builder()
                .position(1)
                .placeholderText("......")
                .startIndex(250)
                .endIndex(256)
                .build();

        FileParserService.ParsedDocumentResponse parsedResponse = FileParserService.ParsedDocumentResponse.builder()
                .documentText("Sample document with ...... and ......")
                .placeholderCount(2)
                .placeholders(List.of(placeholder1, placeholder2))
                .build();

        ParsedTemplateResponseDTO dtoResponse = ParsedTemplateResponseDTO.builder()
                .documentText("Sample document with ...... and ......")
                .placeholderCount(2)
                .build();

        ContractTemplate savedTemplate = ContractTemplate.builder()
                .id(1L)
                .templateName("Service Agreement 2026")
                .description("Standard service agreement template")
                .documentFormat(DocumentFormat.PDF)
                .documentContent(compressedContent)
                .fieldCount(2)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // setup mocks
        when(fileParserService.parseTemplate(pdfFile, DocumentFormat.PDF))
                .thenReturn(parsedResponse);
        when(zipService.compress(originalContent))
                .thenReturn(compressedContent);
        when(templateRepository.save(any(ContractTemplate.class)))
                .thenReturn(savedTemplate);
        when(parsedDocumentMapper.toResponseDTO(parsedResponse))
                .thenReturn(dtoResponse);

        // act
        ParsedTemplateResponseDTO result = templateService.uploadTemplate(request);

        // assert
        assertNotNull(result);
        assertEquals("Sample document with ...... and ......", result.getDocumentText());
        assertEquals(2, result.getPlaceholderCount());

        // verify interactions
        verify(fileParserService).parseTemplate(pdfFile, DocumentFormat.PDF);
        verify(zipService).compress(originalContent);
        verify(templateRepository).save(any(ContractTemplate.class));
        verify(templateFieldRepository).saveAll(anyList());
        verify(parsedDocumentMapper).toResponseDTO(parsedResponse);

        // verify TemplateFields were created
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TemplateField>> fieldsCaptor = ArgumentCaptor.forClass(List.class);
        verify(templateFieldRepository).saveAll(fieldsCaptor.capture());
        List<TemplateField> savedFields = fieldsCaptor.getValue();
        assertEquals(2, savedFields.size());
        assertEquals("Field 1", savedFields.get(0).getFieldLabel());
        assertEquals("Field 2", savedFields.get(1).getFieldLabel());
    }

    @Test
    @DisplayName("Should successfully upload DOCX template with placeholders")
    void testUploadDocxTemplate_Success() throws IOException {
        // arrange
        UploadTemplateRequest request = new UploadTemplateRequest(
                docxFile,
                "Employment Contract",
                "Standard employment contract"
        );

        byte[] compressedContent = "compressed-docx-content".getBytes();
        byte[] originalContent = docxFile.getBytes();

        FileParserService.PlaceholderInfo placeholder = FileParserService.PlaceholderInfo.builder()
                .position(0)
                .placeholderText("........")
                .startIndex(50)
                .endIndex(58)
                .build();

        FileParserService.ParsedDocumentResponse parsedResponse = FileParserService.ParsedDocumentResponse.builder()
                .documentText("Employment contract with ........")
                .placeholderCount(1)
                .placeholders(List.of(placeholder))
                .build();

        ParsedTemplateResponseDTO dtoResponse = ParsedTemplateResponseDTO.builder()
                .documentText("Employment contract with ........")
                .placeholderCount(1)
                .build();

        ContractTemplate savedTemplate = ContractTemplate.builder()
                .id(2L)
                .templateName("Employment Contract")
                .description("Standard employment contract")
                .documentFormat(DocumentFormat.DOCX)
                .documentContent(compressedContent)
                .fieldCount(1)
                .isFullyMapped(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // setup mocks
        when(fileParserService.parseTemplate(docxFile, DocumentFormat.DOCX))
                .thenReturn(parsedResponse);
        when(zipService.compress(originalContent))
                .thenReturn(compressedContent);
        when(templateRepository.save(any(ContractTemplate.class)))
                .thenReturn(savedTemplate);
        when(parsedDocumentMapper.toResponseDTO(parsedResponse))
                .thenReturn(dtoResponse);

        // act
        ParsedTemplateResponseDTO result = templateService.uploadTemplate(request);

        // assert
        assertNotNull(result);
        assertEquals("Employment contract with ........", result.getDocumentText());
        assertEquals(1, result.getPlaceholderCount());

        // verify
        verify(fileParserService).parseTemplate(docxFile, DocumentFormat.DOCX);
        verify(templateRepository).save(any(ContractTemplate.class));
        verify(templateFieldRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when file is empty")
    void testUploadTemplate_EmptyFile_ThrowsException() throws IOException {
        // arrange
        UploadTemplateRequest request = new UploadTemplateRequest(
                emptyFile,
                "Empty Template",
                "This file is empty"
        );

        // act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> {
                    try {
                        templateService.uploadTemplate(request);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                "Should throw IllegalArgumentException for empty file"
        );

        assertEquals("File cannot be empty", exception.getMessage());
        verify(fileParserService, never()).parseTemplate(any(), any());
        verify(templateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw UnsupportedFileException for unsupported file type")
    void testUploadTemplate_UnsupportedFileType_ThrowsException() {
        // arrange
        UploadTemplateRequest request = new UploadTemplateRequest(
                invalidFile,
                "Invalid Template",
                "Text file is not supported"
        );

        // act & assert
        assertThrows(
                UnsupportedFileException.class,
                () -> {
                    try {
                        templateService.uploadTemplate(request);
                        verify(fileParserService, never()).parseTemplate(any(), any());
                        verify(templateRepository, never()).save(any());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                "Should throw UnsupportedFileException for .txt file"
        );
    }

    @Test
    @DisplayName("Should throw EmptyFileNameException when file has no name")
    void testUploadTemplate_NoFileName_ThrowsException() {
        // arrange
        MultipartFile noNameFile = new MockMultipartFile("file", "", "application/pdf", "content".getBytes());
        UploadTemplateRequest request = new UploadTemplateRequest(
                noNameFile,
                "No Name Template",
                "File with no name"
        );

        // act & assert
        assertThrows(
                EmptyFileNameException.class,
                () -> {
                    try {
                        templateService.uploadTemplate(request);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                "Should throw EmptyFileNameException"
        );

        verify(templateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IOException when file parsing fails")
    void testUploadTemplate_ParsingFails_ThrowsException() throws IOException {
        // arrange
        UploadTemplateRequest request = new UploadTemplateRequest(
                pdfFile,
                "Bad PDF",
                "This PDF will fail to parse"
        );

        when(fileParserService.parseTemplate(pdfFile, DocumentFormat.PDF))
                .thenThrow(new IOException("Corrupt PDF file"));

        // act & assert
        IOException exception = assertThrows(
                IOException.class,
                () -> templateService.uploadTemplate(request),
                "Should throw IOException when parsing fails"
        );

        assertEquals("Corrupt PDF file", exception.getMessage());
        verify(templateRepository, never()).save(any());
    }


    @Test
    @DisplayName("Should successfully retrieve template by ID")
    void testGetTemplate_Success() {
        // arrange
        Long templateId = 1L;
        ContractTemplate template = ContractTemplate.builder()
                .id(templateId)
                .templateName("Service Agreement")
                .description("Service agreement template")
                .documentFormat(DocumentFormat.PDF)
                .fieldCount(3)
                .isFullyMapped(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TemplateResponseDTO expectedResponse = TemplateResponseDTO.builder()
                .templateId(templateId)
                .templateName("Service Agreement")
                .description("Service agreement template")
                .fieldCount(3)
                .build();

        when(templateRepository.findById(templateId))
                .thenReturn(Optional.of(template));
        when(contractTemplateMapper.toResponseDTO(template))
                .thenReturn(expectedResponse);

        // act
        TemplateResponseDTO result = templateService.getTemplate(templateId);

        // assert
        assertNotNull(result);
        assertEquals(templateId, result.getTemplateId());
        assertEquals("Service Agreement", result.getTemplateName());
        assertEquals(3, result.getFieldCount());

        // verify
        verify(templateRepository).findById(templateId);
        verify(contractTemplateMapper).toResponseDTO(template);
    }

    @Test
    @DisplayName("Should throw RuntimeException when template is not found")
    void testGetTemplate_NotFound_ThrowsException() {
        // arrange
        Long templateId = 999L;
        when(templateRepository.findById(templateId))
                .thenReturn(Optional.empty());

        // act & assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> templateService.getTemplate(templateId),
                "Should throw RuntimeException when template not found"
        );

        assertTrue(exception.getMessage().contains("Template not found"));
        verify(templateRepository).findById(templateId);
        verify(contractTemplateMapper, never()).toResponseDTO(any());
    }
    

    @Test
    @DisplayName("Should successfully delete template by ID")
    void testDeleteTemplate_Success() {
        // arrange
        Long templateId = 1L;
        when(templateRepository.existsById(templateId))
                .thenReturn(true);

        // act
        templateService.deleteTemplate(templateId);

        // assert
        verify(templateRepository).existsById(templateId);
        verify(templateRepository).deleteById(templateId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when template to delete does not exist")
    void testDeleteTemplate_NotFound_ThrowsException() {
        // arrange
        Long templateId = 999L;
        when(templateRepository.existsById(templateId))
                .thenReturn(false);

        // act & assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> templateService.deleteTemplate(templateId),
                "Should throw ResourceNotFoundException when template not found"
        );

        assertTrue(exception.getMessage().contains("Template not found"));
        verify(templateRepository).existsById(templateId);
        verify(templateRepository, never()).deleteById(any());
    }


    @Test
    @DisplayName("Should create correct number of TemplateFields with proper naming")
    void testUploadTemplate_TemplateFieldCreation() throws IOException {
        // arrange
        UploadTemplateRequest request = new UploadTemplateRequest(
                pdfFile,
                "Multi-Field Template",
                "Template with multiple placeholders"
        );

        byte[] compressedContent = "compressed".getBytes();

        // create 5 placeholders
        List<FileParserService.PlaceholderInfo> placeholders = List.of(
                createPlaceholder(0, 100, 106),
                createPlaceholder(1, 200, 206),
                createPlaceholder(2, 300, 306),
                createPlaceholder(3, 400, 406),
                createPlaceholder(4, 500, 506)
        );

        FileParserService.ParsedDocumentResponse parsedResponse = FileParserService.ParsedDocumentResponse.builder()
                .documentText("Document with multiple placeholders")
                .placeholderCount(5)
                .placeholders(placeholders)
                .build();

        ContractTemplate savedTemplate = ContractTemplate.builder()
                .id(1L)
                .templateName("Multi-Field Template")
                .fieldCount(5)
                .isFullyMapped(false)
                .build();

        when(fileParserService.parseTemplate(pdfFile, DocumentFormat.PDF))
                .thenReturn(parsedResponse);
        when(zipService.compress(any()))
                .thenReturn(compressedContent);
        when(templateRepository.save(any()))
                .thenReturn(savedTemplate);
        when(parsedDocumentMapper.toResponseDTO(parsedResponse))
                .thenReturn(new ParsedTemplateResponseDTO());

        // act
        templateService.uploadTemplate(request);

        // assert
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TemplateField>> fieldsCaptor = ArgumentCaptor.forClass(List.class);
        verify(templateFieldRepository).saveAll(fieldsCaptor.capture());
        
        List<TemplateField> savedFields = fieldsCaptor.getValue();
        assertEquals(5, savedFields.size());
        
        for (int i = 0; i < 5; i++) {
            assertEquals("Field " + (i + 1), savedFields.get(i).getFieldLabel());
            assertEquals(i, savedFields.get(i).getFieldPosition());
        }
    }

    @Test
    @DisplayName("Should preserve template metadata during upload")
    void testUploadTemplate_MetadataPreservation() throws IOException {
        // arrange
        String templateName = "Important Contract 2026";
        String description = "This is a very important contract";
        UploadTemplateRequest request = new UploadTemplateRequest(pdfFile, templateName, description);

        byte[] compressedContent = "compressed".getBytes();
        FileParserService.ParsedDocumentResponse parsedResponse = FileParserService.ParsedDocumentResponse.builder()
                .documentText("Content")
                .placeholderCount(0)
                .placeholders(List.of())
                .build();

        ContractTemplate savedTemplate = ContractTemplate.builder()
                .id(1L)
                .templateName(templateName)
                .description(description)
                .documentFormat(DocumentFormat.PDF)
                .fieldCount(0)
                .isFullyMapped(false)
                .build();

        when(fileParserService.parseTemplate(pdfFile, DocumentFormat.PDF))
                .thenReturn(parsedResponse);
        when(zipService.compress(any()))
                .thenReturn(compressedContent);
        when(templateRepository.save(any()))
                .thenReturn(savedTemplate);
        when(parsedDocumentMapper.toResponseDTO(parsedResponse))
                .thenReturn(new ParsedTemplateResponseDTO());

        // act
        templateService.uploadTemplate(request);

        // assert
        ArgumentCaptor<ContractTemplate> templateCaptor = ArgumentCaptor.forClass(ContractTemplate.class);
        verify(templateRepository).save(templateCaptor.capture());
        
        ContractTemplate capturedTemplate = templateCaptor.getValue();
        assertEquals(templateName, capturedTemplate.getTemplateName());
        assertEquals(description, capturedTemplate.getDescription());
        assertEquals(DocumentFormat.PDF, capturedTemplate.getDocumentFormat());
    }

    private FileParserService.PlaceholderInfo createPlaceholder(int position, int startIndex, int endIndex) {
        return FileParserService.PlaceholderInfo.builder()
                .position(position)
                .placeholderText("......")
                .startIndex(startIndex)
                .endIndex(endIndex)
                .build();
    }
}
