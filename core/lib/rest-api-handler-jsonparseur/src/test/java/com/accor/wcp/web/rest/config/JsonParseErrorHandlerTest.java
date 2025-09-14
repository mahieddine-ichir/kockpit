package com.accor.wcp.web.rest.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.JsonParser;
import java.io.FileReader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@SpringBootTest(classes = SpringBoot4IntegrationTestApplication.class)
@AutoConfigureMockMvc
class JsonParseErrorHandlerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void should_handleJsonParseException() throws Exception {

    String json = "invalidJson";

    MvcResult mvcResult =
        this.mockMvc
            .perform(
                MockMvcRequestBuilders.get("/test")
                    .content(json)
                    .contentType(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andReturn();

    assertThat(mvcResult.getResponse().getContentAsString())
        .isEqualTo(
            "{\"title\":\"Invalid json message received\",\"status\":400,\"detail\":\"JSON parse error : you did not provide a valid JSON file\",\"code\":\"INPUT_JSON_INVALID\",\"fieldErrors\":null}");
    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void should_handleInvalidFormatException() throws Exception {

    String json =
        JsonParser.parseReader(new FileReader("src/test/resources/invalid_age.json")).toString();

    MvcResult mvcResult =
        this.mockMvc
            .perform(
                MockMvcRequestBuilders.get("/test")
                    .content(json)
                    .contentType(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andReturn();

    assertThat(mvcResult.getResponse().getContentAsString())
        .isEqualTo(
            "{\"title\":\"Method argument not valid\",\"status\":400,\"detail\":\"Input validation error, parser raised some issues that are defined in fieldErrors array\",\"code\":\"INPUT_VALIDATION_METHODARGINVALID\",\"fieldErrors\":[{\"objectName\":\"FakeUser[\\\"age\\\"]\",\"field\":\"age\",\"message\":\"Cannot deserialize value of type `java.lang.Integer` from String \\\"aaaaa\\\": not a valid `java.lang.Integer` value\"}]}");
    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void should_InvalidTypeIdException() throws Exception {

    String json =
        JsonParser.parseReader(new FileReader("src/test/resources/invalid_type.json")).toString();

    MvcResult mvcResult =
        this.mockMvc
            .perform(
                MockMvcRequestBuilders.get("/test")
                    .content(json)
                    .contentType(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andReturn();

    assertThat(mvcResult.getResponse().getContentAsString())
        .isEqualTo(
            "{\"title\":\"Invalid json message received\",\"status\":400,\"detail\":\"Invalid type id: null in field: origin\",\"code\":\"INPUT_VALIDATION_INVALIDTYPEID\",\"fieldErrors\":null}");
    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }
}
