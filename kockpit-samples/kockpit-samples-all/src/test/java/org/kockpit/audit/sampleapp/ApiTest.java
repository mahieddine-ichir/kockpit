package org.kockpit.audit.sampleapp;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {
        SampleApplication.class
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ApiTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        Mockito.when(restTemplate.getForObject("http://localhost/api/v2/john", Map.class))
                .thenReturn(Map.of("name", "john"));
    }

    @Test
    void on_get() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                        get("/api/{name}", "john")
                ).andExpect(status().isOk())
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getContentAsString())
                .isEqualTo("""
                        {"name":"JOHN"}
                        """.trim());
    }
}
