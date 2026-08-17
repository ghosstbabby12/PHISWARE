package com.phishware.service;

import com.phishware.dto.request.UrlAnalysisRequest;
import com.phishware.dto.response.UrlAnalysisResponse;
import com.phishware.entity.User;
import com.phishware.entity.enums.RiskLevel;
import com.phishware.repository.AlertRepository;
import com.phishware.repository.UrlAnalysisRepository;
import com.phishware.repository.UserRepository;
import com.phishware.security.InputSanitizerService;
import com.phishware.security.NistThreatClassifier;
import com.phishware.security.OwaspComplianceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UrlAnalysisService - Tests unitarios")
class UrlAnalysisServiceTest {

    @Mock private UrlAnalysisRepository analysisRepository;
    @Mock private UserRepository userRepository;
    @Mock private AlertRepository alertRepository;
    @Mock private GoogleSafeBrowsingService safeBrowsingService;
    @Mock private VirusTotalService virusTotalService;
    @Mock private GamificationService gamificationService;
    @Mock private AuditLogService auditLogService;
    @Mock private InputSanitizerService inputSanitizer;
    @Mock private NistThreatClassifier nistClassifier;
    @Mock private OwaspComplianceValidator owaspValidator;

    @InjectMocks
    private UrlAnalysisService urlAnalysisService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .password("hashedpassword")
            .points(0)
            .level(1)
            .build();

        lenient().when(inputSanitizer.sanitizeAndValidateUrl(anyString()))
            .thenAnswer(invocation -> {
                String url = invocation.getArgument(0);
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    return "https://" + url;
                }
                return url;
            });

        lenient().when(owaspValidator.validateUrlForInjection(anyString()))
            .thenReturn(new OwaspComplianceValidator.ValidationResult(true, List.of()));

        lenient().when(nistClassifier.classify(any(), any()))
            .thenReturn(new NistThreatClassifier.HeuristicResult(
                RiskLevel.SAFE, BigDecimal.ZERO, List.of(), List.of()));
    }

    @Test
    @DisplayName("Debe clasificar URL como SAFE cuando ambas APIs no detectan amenazas")
    void analyzeUrl_WhenNoThreats_ShouldReturnSafe() {
        // Arrange
        UrlAnalysisRequest request = new UrlAnalysisRequest();
        request.setUrl("https://www.google.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(safeBrowsingService.checkUrl(any())).thenReturn(
            GoogleSafeBrowsingService.SafeBrowsingResult.safe()
        );
        when(virusTotalService.analyzeUrl(any())).thenReturn(
            VirusTotalService.VirusTotalResult.safe(0, 70)
        );
        when(analysisRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        UrlAnalysisResponse response = urlAnalysisService.analyzeUrl(request, 1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getRiskLevel()).isEqualTo(RiskLevel.SAFE);
        assertThat(response.isPhishing()).isFalse();
        verify(alertRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe clasificar URL como DANGEROUS cuando Google Safe Browsing detecta amenaza")
    void analyzeUrl_WhenGsbDetectsThreat_ShouldReturnDangerous() {
        // Arrange
        UrlAnalysisRequest request = new UrlAnalysisRequest();
        request.setUrl("https://paypa1.com/login");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(safeBrowsingService.checkUrl(any())).thenReturn(
            GoogleSafeBrowsingService.SafeBrowsingResult.dangerous(
                List.of("SOCIAL_ENGINEERING"), "SOCIAL_ENGINEERING"
            )
        );
        when(virusTotalService.analyzeUrl(any())).thenReturn(
            VirusTotalService.VirusTotalResult.safe(0, 70)
        );
        when(analysisRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        UrlAnalysisResponse response = urlAnalysisService.analyzeUrl(request, 1L);

        // Assert
        assertThat(response.getRiskLevel()).isEqualTo(RiskLevel.DANGEROUS);
        assertThat(response.isPhishing()).isTrue();
        verify(alertRepository).save(any());
    }

    @Test
    @DisplayName("Debe agregar https:// si la URL no tiene protocolo")
    void analyzeUrl_WhenNoProtocol_ShouldAddHttps() {
        // Arrange
        UrlAnalysisRequest request = new UrlAnalysisRequest();
        request.setUrl("google.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(safeBrowsingService.checkUrl(anyString())).thenReturn(
            GoogleSafeBrowsingService.SafeBrowsingResult.safe()
        );
        when(virusTotalService.analyzeUrl(anyString())).thenReturn(
            VirusTotalService.VirusTotalResult.safe(0, 70)
        );
        when(analysisRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        UrlAnalysisResponse response = urlAnalysisService.analyzeUrl(request, 1L);

        // Assert
        assertThat(response.getOriginalUrl()).startsWith("https://");
    }

    @Test
    @DisplayName("Debe lanzar excepción con URL inválida")
    void analyzeUrl_WhenInvalidUrl_ShouldThrowException() {
        // Arrange
        UrlAnalysisRequest request = new UrlAnalysisRequest();
        request.setUrl("not-a-valid-url-!!@@##");

        when(inputSanitizer.sanitizeAndValidateUrl(anyString()))
            .thenThrow(new IllegalArgumentException("URL inválida"));

        // Act & Assert
        assertThatThrownBy(() -> urlAnalysisService.analyzeUrl(request, 1L))
            .isInstanceOf(com.phishware.exception.UrlValidationException.class);
    }
}
