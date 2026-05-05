package clm.demo.services.download;

import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;
import clm.demo.services.download.document.providers.DocumentProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * verifies that DocumentProviderRegistry correctly indexes providers by type,
 * rejects missing lookups, and rejects duplicate providers at construction time.
 */
class DocumentProviderRegistryTest {

    // ------------------------------------------------------------------ //
    //  helpers                                                             //
    // ------------------------------------------------------------------ //

    private DocumentProvider providerFor(DocumentType type) {
        DocumentProvider p = mock(DocumentProvider.class);
        when(p.getDocumentType()).thenReturn(type);
        return p;
    }

    // ================================================================== //
    //  registration                                                        //
    // ================================================================== //

    @Test
    void single_provider_registered_and_retrieved() {
        DocumentProvider tp = providerFor(DocumentType.TEMPLATE);
        DocumentProviderRegistry registry = new DocumentProviderRegistry(List.of(tp));

        assertThat(registry.getProvider(DocumentType.TEMPLATE)).isSameAs(tp);
    }

    @Test
    void multiple_providers_each_retrievable_by_type() {
        DocumentProvider tp = providerFor(DocumentType.TEMPLATE);
        DocumentProvider cp = providerFor(DocumentType.UNSIGNED_CONTRACT);
        DocumentProvider sp = providerFor(DocumentType.SIGNED_CONTRACT);
        DocumentProviderRegistry registry = new DocumentProviderRegistry(List.of(tp, cp, sp));

        assertThat(registry.getProvider(DocumentType.TEMPLATE)).isSameAs(tp);
        assertThat(registry.getProvider(DocumentType.UNSIGNED_CONTRACT)).isSameAs(cp);
        assertThat(registry.getProvider(DocumentType.SIGNED_CONTRACT)).isSameAs(sp);
    }

    @Test
    void empty_provider_list_creates_empty_registry() {
        DocumentProviderRegistry registry = new DocumentProviderRegistry(List.of());

        assertThatThrownBy(() -> registry.getProvider(DocumentType.TEMPLATE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ================================================================== //
    //  lookup failures                                                     //
    // ================================================================== //

    @Test
    void unregistered_type_throws_illegal_argument_with_type_name() {
        DocumentProviderRegistry registry = new DocumentProviderRegistry(List.of());

        assertThatThrownBy(() -> registry.getProvider(DocumentType.TEMPLATE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TEMPLATE");
    }

    @Test
    void registered_type_does_not_affect_unregistered_lookup() {
        DocumentProvider tp = providerFor(DocumentType.TEMPLATE);
        DocumentProviderRegistry registry = new DocumentProviderRegistry(List.of(tp));

        assertThatThrownBy(() -> registry.getProvider(DocumentType.UNSIGNED_CONTRACT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ================================================================== //
    //  duplicate detection                                                 //
    // ================================================================== //

    @Test
    void duplicate_provider_for_same_type_throws_illegal_state_on_construction() {
        DocumentProvider p1 = providerFor(DocumentType.TEMPLATE);
        DocumentProvider p2 = providerFor(DocumentType.TEMPLATE);

        assertThatThrownBy(() -> new DocumentProviderRegistry(List.of(p1, p2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    void different_types_do_not_trigger_duplicate_check() {
        DocumentProvider p1 = providerFor(DocumentType.TEMPLATE);
        DocumentProvider p2 = providerFor(DocumentType.UNSIGNED_CONTRACT);

        // must not throw
        new DocumentProviderRegistry(List.of(p1, p2));
    }

    // ================================================================== //
    //  supportsFormat contract                                             //
    // ================================================================== //

    @Test
    void provider_supports_format_contract_is_delegated_to_provider() {
        DocumentProvider tp = providerFor(DocumentType.TEMPLATE);
        when(tp.supportsFormat(DocumentFormat.PDF)).thenReturn(true);
        when(tp.supportsFormat(DocumentFormat.DOCX)).thenReturn(true);
        DocumentProviderRegistry registry = new DocumentProviderRegistry(List.of(tp));

        DocumentProvider retrieved = registry.getProvider(DocumentType.TEMPLATE);

        assertThat(retrieved.supportsFormat(DocumentFormat.PDF)).isTrue();
        assertThat(retrieved.supportsFormat(DocumentFormat.DOCX)).isTrue();
    }
}
