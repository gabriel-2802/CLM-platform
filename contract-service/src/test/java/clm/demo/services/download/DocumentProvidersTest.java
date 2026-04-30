package clm.demo.services.download;

import clm.demo.exceptions.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.exceptions.SignedDocumentNotAvailableException;
import clm.demo.models.Appendix;
import clm.demo.models.Contract;
import clm.demo.models.DocumentTemplate;
import clm.demo.models.enums.AppendixStatus;
import clm.demo.models.enums.ContractStatus;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;
import clm.demo.repositories.AppendixRepository;
import clm.demo.repositories.ContractRepository;
import clm.demo.repositories.DocumentTemplateRepository;
import clm.demo.services.download.document.providers.SignedAppendixProvider;
import clm.demo.services.download.document.providers.SignedContractProvider;
import clm.demo.services.download.document.providers.TemplateProvider;
import clm.demo.services.download.document.providers.UnsignedAppendixProvider;
import clm.demo.services.download.document.providers.UnsignedContractProvider;
import clm.demo.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * unit tests for all five DocumentProvider implementations.
 * providers are constructed manually to keep all mocks at the outer class level,
 * avoiding ambiguity with MockitoExtension in nested classes.
 */
@ExtendWith(MockitoExtension.class)
class DocumentProvidersTest {

    @Mock ContractRepository          contractRepository;
    @Mock AppendixRepository          appendixRepository;
    @Mock DocumentTemplateRepository  templateRepository;

    SignedContractProvider    signedContractProvider;
    UnsignedContractProvider  unsignedContractProvider;
    TemplateProvider          templateProvider;
    SignedAppendixProvider    signedAppendixProvider;
    UnsignedAppendixProvider  unsignedAppendixProvider;

    @BeforeEach
    void setUp() {
        signedContractProvider   = new SignedContractProvider(contractRepository);
        unsignedContractProvider = new UnsignedContractProvider(contractRepository);
        templateProvider         = new TemplateProvider(templateRepository);
        signedAppendixProvider   = new SignedAppendixProvider(appendixRepository);
        unsignedAppendixProvider = new UnsignedAppendixProvider(appendixRepository);
    }

    // ================================================================== //
    //  SignedContractProvider                                              //
    // ================================================================== //

    @Nested
    class SignedContractProviderTests {

        @Test
        void contract_not_found_throws_resource_not_found() {
            when(contractRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> signedContractProvider.getDocument(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void no_signed_content_throws_signed_document_not_available() {
            Contract c = TestDataFactory.contract(1L, ContractStatus.PENDING_SIGNATURE);
            c.setSignedDocumentContent(null);
            when(contractRepository.findById(1L)).thenReturn(Optional.of(c));

            assertThatThrownBy(() -> signedContractProvider.getDocument(1L))
                    .isInstanceOf(SignedDocumentNotAvailableException.class);
        }

        @Test
        void signed_content_present_returns_pdf_result() {
            Contract c = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            c.setSignedDocumentContent(new byte[]{1, 2, 3});
            when(contractRepository.findById(1L)).thenReturn(Optional.of(c));

            DocumentResult result = signedContractProvider.getDocument(1L);

            assertThat(result.compressedContent()).isEqualTo(new byte[]{1, 2, 3});
            assertThat(result.nativeFormat()).isEqualTo(DocumentFormat.PDF);
        }

        @Test
        void supports_only_pdf() {
            assertThat(signedContractProvider.supportsFormat(DocumentFormat.PDF)).isTrue();
            assertThat(signedContractProvider.supportsFormat(DocumentFormat.DOCX)).isFalse();
        }

        @Test
        void document_type_is_signed_contract() {
            assertThat(signedContractProvider.getDocumentType()).isEqualTo(DocumentType.SIGNED_CONTRACT);
        }
    }

    // ================================================================== //
    //  UnsignedContractProvider                                           //
    // ================================================================== //

    @Nested
    class UnsignedContractProviderTests {

        @Test
        void contract_not_found_throws_resource_not_found() {
            when(contractRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> unsignedContractProvider.getDocument(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void null_document_content_throws_resource_not_found_with_message() {
            Contract c = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            c.setDocumentContent(null);
            when(contractRepository.findById(1L)).thenReturn(Optional.of(c));

            assertThatThrownBy(() -> unsignedContractProvider.getDocument(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("not yet available");
        }

        @Test
        void document_content_present_returns_pdf_result() {
            Contract c = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            c.setDocumentContent(new byte[]{7, 8});
            when(contractRepository.findById(1L)).thenReturn(Optional.of(c));

            DocumentResult result = unsignedContractProvider.getDocument(1L);

            assertThat(result.nativeFormat()).isEqualTo(DocumentFormat.PDF);
            assertThat(result.compressedContent()).isEqualTo(new byte[]{7, 8});
        }

        @Test
        void supports_pdf_and_docx() {
            assertThat(unsignedContractProvider.supportsFormat(DocumentFormat.PDF)).isTrue();
            assertThat(unsignedContractProvider.supportsFormat(DocumentFormat.DOCX)).isTrue();
        }

        @Test
        void document_type_is_unsigned_contract() {
            assertThat(unsignedContractProvider.getDocumentType()).isEqualTo(DocumentType.UNSIGNED_CONTRACT);
        }
    }

    // ================================================================== //
    //  TemplateProvider                                                    //
    // ================================================================== //

    @Nested
    class TemplateProviderTests {

        @Test
        void template_not_found_throws_resource_not_found() {
            when(templateRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> templateProvider.getDocument(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void returns_document_result_with_template_native_format() {
            DocumentTemplate t = TestDataFactory.templateWithId(1L, "NDA");
            t.setDocumentContent(new byte[]{7, 8, 9});
            t.setDocumentFormat(DocumentFormat.DOCX);
            when(templateRepository.findById(1L)).thenReturn(Optional.of(t));

            DocumentResult result = templateProvider.getDocument(1L);

            assertThat(result.nativeFormat()).isEqualTo(DocumentFormat.DOCX);
            assertThat(result.compressedContent()).isEqualTo(new byte[]{7, 8, 9});
        }

        @Test
        void supports_pdf_and_docx() {
            assertThat(templateProvider.supportsFormat(DocumentFormat.PDF)).isTrue();
            assertThat(templateProvider.supportsFormat(DocumentFormat.DOCX)).isTrue();
        }

        @Test
        void document_type_is_template() {
            assertThat(templateProvider.getDocumentType()).isEqualTo(DocumentType.TEMPLATE);
        }
    }

    // ================================================================== //
    //  SignedAppendixProvider                                              //
    // ================================================================== //

    @Nested
    class SignedAppendixProviderTests {

        @Test
        void appendix_not_found_throws_resource_not_found() {
            when(appendixRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> signedAppendixProvider.getDocument(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void no_signed_content_throws_signed_document_not_available() {
            Contract c  = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            Appendix a  = TestDataFactory.appendix(12L, c, AppendixStatus.DRAFT);
            a.setSignedDocumentContent(null);
            when(appendixRepository.findById(12L)).thenReturn(Optional.of(a));

            assertThatThrownBy(() -> signedAppendixProvider.getDocument(12L))
                    .isInstanceOf(SignedDocumentNotAvailableException.class);
        }

        @Test
        void signed_content_present_returns_pdf_result() {
            Contract c  = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            Appendix a  = TestDataFactory.appendix(12L, c, AppendixStatus.SIGNED);
            a.setSignedDocumentContent(new byte[]{5, 6});
            when(appendixRepository.findById(12L)).thenReturn(Optional.of(a));

            DocumentResult result = signedAppendixProvider.getDocument(12L);

            assertThat(result.nativeFormat()).isEqualTo(DocumentFormat.PDF);
            assertThat(result.compressedContent()).isEqualTo(new byte[]{5, 6});
        }

        @Test
        void supports_only_pdf() {
            assertThat(signedAppendixProvider.supportsFormat(DocumentFormat.PDF)).isTrue();
            assertThat(signedAppendixProvider.supportsFormat(DocumentFormat.DOCX)).isFalse();
        }

        @Test
        void document_type_is_signed_appendix() {
            assertThat(signedAppendixProvider.getDocumentType()).isEqualTo(DocumentType.SIGNED_APPENDIX);
        }
    }

    // ================================================================== //
    //  UnsignedAppendixProvider                                           //
    // ================================================================== //

    @Nested
    class UnsignedAppendixProviderTests {

        @Test
        void appendix_not_found_throws_resource_not_found() {
            when(appendixRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> unsignedAppendixProvider.getDocument(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void null_document_content_throws_resource_not_found_with_message() {
            Contract c = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            Appendix a = TestDataFactory.appendix(12L, c, AppendixStatus.DRAFT);
            a.setDocumentContent(null);
            when(appendixRepository.findById(12L)).thenReturn(Optional.of(a));

            assertThatThrownBy(() -> unsignedAppendixProvider.getDocument(12L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("not yet available");
        }

        @Test
        void document_content_present_uses_stored_format() {
            Contract c = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            Appendix a = TestDataFactory.appendix(12L, c, AppendixStatus.DRAFT);
            a.setDocumentContent(new byte[]{1, 2, 3});
            a.setDocumentFormat(DocumentFormat.DOCX);
            when(appendixRepository.findById(12L)).thenReturn(Optional.of(a));

            DocumentResult result = unsignedAppendixProvider.getDocument(12L);

            assertThat(result.nativeFormat()).isEqualTo(DocumentFormat.DOCX);
        }

        @Test
        void null_document_format_falls_back_to_pdf() {
            Contract c = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            Appendix a = TestDataFactory.appendix(12L, c, AppendixStatus.DRAFT);
            a.setDocumentContent(new byte[]{1, 2, 3});
            a.setDocumentFormat(null);   // explicit null — format not yet detected
            when(appendixRepository.findById(12L)).thenReturn(Optional.of(a));

            DocumentResult result = unsignedAppendixProvider.getDocument(12L);

            assertThat(result.nativeFormat()).isEqualTo(DocumentFormat.PDF);
        }

        @Test
        void supports_pdf_and_docx() {
            assertThat(unsignedAppendixProvider.supportsFormat(DocumentFormat.PDF)).isTrue();
            assertThat(unsignedAppendixProvider.supportsFormat(DocumentFormat.DOCX)).isTrue();
        }

        @Test
        void document_type_is_unsigned_appendix() {
            assertThat(unsignedAppendixProvider.getDocumentType()).isEqualTo(DocumentType.UNSIGNED_APPENDIX);
        }
    }
}
