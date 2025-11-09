package org.kockpit.audit.samples;

import org.junit.jupiter.api.Test;
import org.kockpit.audit.NotificationAuditReportManager;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    List<AuditReportNotificationService> auditReportNotificationServices;

    @MockitoSpyBean
    NotificationAuditReportManager notificationAuditReportManager;

    @Test
    void on_get() throws Exception {
        List<AuditReportNotificationService> spies = auditReportNotificationServices.stream().map(Mockito::spy)
                .toList();


        mockMvc.perform(get("/api"))
                .andExpect(status().isOk());

        Mockito.verify(notificationAuditReportManager).addAuditReport(Mockito.any());
        spies.forEach(Mockito::verifyNoMoreInteractions);
    }
}
