package com.accor.wcp.web.rest.problem;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SpringBoot4IntegrationTestApplication.class)
@AutoConfigureMockMvc
class InternalServerErrorHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_handle_allOthers_exception() throws Exception {
        MvcResult mvcResult =
                this.mockMvc
                        .perform(MockMvcRequestBuilders.get("/nullPointerException"))
                        .andDo(MockMvcResultHandlers.print())
                        .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString())
                .isEqualTo("{\"type\":\"about:blank\",\"title\":\"Internal Server error\",\"status\":500,\"detail\":\"Unable to process data. Please contact Welcome Connect team.\",\"instance\":\"/nullPointerException\",\"code\":\"TECHNICAL_ERROR\"}");
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
