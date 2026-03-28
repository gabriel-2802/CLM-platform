package clm.demo.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {
    @PostMapping("/generate")
    public ResponseEntity<Void> generateTemplate() {
        return null;
    }

    @PutMapping("/{templateId}/mappings")
    public ResponseEntity<Void> updateFieldMappings() {
        return null;
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate() {
        return null;
    }
}
