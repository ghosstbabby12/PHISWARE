package com.phishware.service;

import com.phishware.entity.CommunityReport;
import com.phishware.repository.CommunityReportRepository;
import com.phishware.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityReportServiceTest {

    @Mock private CommunityReportRepository reportRepository;
    @Mock private UserRepository userRepository;
    @Mock private GamificationService gamificationService;

    @InjectMocks
    private CommunityReportService service;

    private static CommunityReport report(String url) {
        return CommunityReport.builder().reportedUrl(url).reportType("PHISHING").status("CONFIRMED").build();
    }

    @Test
    void confirmedDomainsAreRegistrableAndDistinct() {
        when(reportRepository.findTop500ByStatusOrderByReviewedAtDesc("CONFIRMED")).thenReturn(List.of(
            report("https://soporte-nequi-falso.tk/acceso"),
            report("http://soporte-nequi-falso.tk/otro"),
            report("not a url"),
            report(null)
        ));

        var domains = service.confirmedDomains(500);

        assertThat(domains).containsExactly("soporte-nequi-falso.tk");
    }

    @Test
    void registrableDomainOfHandlesGarbage() {
        assertThat(CommunityReportService.registrableDomainOf("https://a.bancolombia.com.co/x")).isEqualTo("bancolombia.com.co");
        assertThat(CommunityReportService.registrableDomainOf("notaurl")).isEqualTo("");
        assertThat(CommunityReportService.registrableDomainOf(null)).isEqualTo("");
    }
}
