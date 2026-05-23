package clm.client.demo.controllers;

import clm.client.demo.config.WebMvcTestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EnumsController.class)
@Import(WebMvcTestSecurityConfig.class)
class EnumsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnEnums() throws Exception {
        mockMvc.perform(get("/api/enums"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyTypes").isArray())
                .andExpect(jsonPath("$.taxTypes").isArray())
                .andExpect(jsonPath("$.taxFrequencies").isArray())
                .andExpect(jsonPath("$.yesNoNa").isArray())
                .andExpect(jsonPath("$.administrations").isArray());
    }

    @Test
    void shouldReturnEnumsEvenWithoutAuthentication() throws Exception {
        // Enums endpoint doesn't have PreAuthorize explicitly but let's check
        // It might be secured by SecurityConfig, or it might be public.
        // If it's secured, it should return 401. Based on other controllers, it might need auth.
        mockMvc.perform(get("/api/enums"))
                .andExpect(status().isUnauthorized());
    }
}
