package com.phishware.controller;

import com.phishware.entity.EducationalContent;
import com.phishware.service.EducationalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/education")
@RequiredArgsConstructor
@Tag(name = "Módulo Educativo", description = "Contenido educativo sobre ciberseguridad y phishing")
public class EducationalController {

    private final EducationalService educationalService;

    @GetMapping
    @Operation(summary = "Listar contenido", description = "Lista el contenido educativo publicado con filtros opcionales")
    public ResponseEntity<Page<EducationalContent>> listContent(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(educationalService.getAll(category, difficulty, pageable));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Obtener artículo", description = "Retorna el contenido de un artículo por su slug")
    public ResponseEntity<EducationalContent> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(educationalService.getBySlug(slug));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear contenido", description = "Crea nuevo contenido educativo (solo admins)")
    public ResponseEntity<EducationalContent> create(@RequestBody EducationalContent content) {
        return ResponseEntity.ok(educationalService.save(content));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar contenido", description = "Actualiza un artículo existente (solo admins)")
    public ResponseEntity<EducationalContent> update(
            @PathVariable Long id,
            @RequestBody EducationalContent content) {
        content.setId(id);
        return ResponseEntity.ok(educationalService.save(content));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar contenido", description = "Elimina un artículo (solo admins)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        educationalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
