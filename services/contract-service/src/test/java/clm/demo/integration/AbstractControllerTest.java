package clm.demo.integration;

import clm.demo.exceptions.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Base class for standalone MockMvc controller tests.
 * Subclasses provide the controller instance and call {@link #buildMockMvc(Object...)}.
 */
public abstract class AbstractControllerTest {

    protected MockMvc mockMvc;

    protected void buildMockMvc(Object... controllers) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controllers)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }
}
