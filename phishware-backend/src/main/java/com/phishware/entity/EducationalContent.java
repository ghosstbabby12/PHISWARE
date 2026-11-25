package com.phishware.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.phishware.i18n.ContentLanguage;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "educational_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationalContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String summary;

    @Column(nullable = false, length = 20)
    private String difficulty;

    @Column(name = "reading_time_min", nullable = false)
    @Builder.Default
    private int readingTimeMin = 5;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> tags;

    @Column(nullable = false)
    @Builder.Default
    private int views = 0;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private boolean isPublished = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    // ── Traducción al inglés (V9). Solo escritura en JSON: la API responde con title/content
    // ya localizados (ver localized()), así que no se duplican en la respuesta.
    @Column(name = "title_en", length = 200)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String titleEn;

    @Column(name = "summary_en", length = 500)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String summaryEn;

    @Column(name = "content_en", columnDefinition = "TEXT")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String contentEn;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tags_en", columnDefinition = "text[]")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> tagsEn;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Copia desacoplada (no gestionada por JPA) con los textos en el idioma de la petición.
     * Nunca se modifica la entidad original, para no persistir la traducción por accidente.
     */
    public EducationalContent localized() {
        if (!ContentLanguage.isEnglish()) return this;
        return EducationalContent.builder()
            .id(id).slug(slug).category(category).difficulty(difficulty)
            .readingTimeMin(readingTimeMin).thumbnailUrl(thumbnailUrl).views(views)
            .isPublished(isPublished).author(author).createdAt(createdAt).updatedAt(updatedAt)
            .title(ContentLanguage.pick(title, titleEn))
            .summary(ContentLanguage.pick(summary, summaryEn))
            .content(ContentLanguage.pick(content, contentEn))
            .tags(ContentLanguage.pick(tags, tagsEn))
            .build();
    }
}
