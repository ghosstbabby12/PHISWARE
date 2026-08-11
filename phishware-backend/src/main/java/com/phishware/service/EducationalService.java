package com.phishware.service;

import com.phishware.entity.EducationalContent;
import com.phishware.exception.ResourceNotFoundException;
import com.phishware.repository.EducationalContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EducationalService {

    private final EducationalContentRepository contentRepository;

    @Transactional(readOnly = true)
    public Page<EducationalContent> getAll(String category, String difficulty, Pageable pageable) {
        return contentRepository.findWithFilters(category, difficulty, pageable);
    }

    @Transactional
    public EducationalContent getBySlug(String slug) {
        EducationalContent content = contentRepository.findBySlugAndIsPublishedTrue(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Contenido no encontrado: " + slug));
        incrementViewsAsync(content.getId());
        return content;
    }

    @Transactional(readOnly = true)
    public EducationalContent getById(Long id) {
        return contentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contenido no encontrado: " + id));
    }

    @Transactional
    public EducationalContent save(EducationalContent content) {
        return contentRepository.save(content);
    }

    @Transactional
    public void delete(Long id) {
        if (!contentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Contenido no encontrado: " + id);
        }
        contentRepository.deleteById(id);
    }

    @Async
    @Transactional
    public void incrementViewsAsync(Long contentId) {
        contentRepository.incrementViews(contentId);
    }
}
