package clm.demo.services.download;

import clm.demo.models.enums.DocumentType;
import clm.demo.services.download.document.providers.DocumentProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry that auto-discovers all {@link DocumentProvider} beans in the application
 * context and indexes them by their {@link DocumentType}.
 *
 * Adding a new provider (e.g. AnnexDocumentProvider) requires no changes here —
 * Spring will pick it up automatically as long as it is annotated with @Component
 * and implements DocumentProvider.
 */
@Component
public class DocumentProviderRegistry {

    private final Map<DocumentType, DocumentProvider> providers;

    /**
     * Spring injects every DocumentProvider bean as a list; we index them by type.
     *
     * @throws IllegalStateException if two providers share the same DocumentType
     */
    public DocumentProviderRegistry(List<DocumentProvider> providers) {
        this.providers = providers.stream()
                .collect(Collectors.toMap(
                        DocumentProvider::getDocumentType,
                        Function.identity(),
                        (a, b) -> {
                            throw new IllegalStateException(
                                    "Duplicate DocumentProvider for type: " + a.getDocumentType());
                        }
                ));
    }

    /**
     * Returns the provider registered for the given document type.
     *
     * @param documentType the type to look up
     * @return the corresponding {@link DocumentProvider}
     * @throws IllegalArgumentException if no provider is registered for the type
     */
    public DocumentProvider getProvider(DocumentType documentType) {
        DocumentProvider provider = providers.get(documentType);
        if (provider == null) {
            throw new IllegalArgumentException("No DocumentProvider registered for type: " + documentType);
        }
        return provider;
    }
}