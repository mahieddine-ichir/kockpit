package org.kockpit.samples;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void on_call_api() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                        get("/api/{name}", "john").queryParam("upper", "true")
                ).andExpect(status().isOk())
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("JOHN");
    }

    @Test
    void on_call_api_without_param() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                        get("/api/{name}", "john")
                ).andExpect(status().isOk())
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("john");
    }
}
