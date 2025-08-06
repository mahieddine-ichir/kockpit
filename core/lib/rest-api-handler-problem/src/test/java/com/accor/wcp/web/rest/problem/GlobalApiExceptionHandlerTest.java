package com.accor.wcp.web.rest.problem;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import java.io.FileReader;

@SpringBootTest(classes = SpringBoot4IntegrationTestApplication.class)
@AutoConfigureMockMvc
class GlobalApiExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_handleMissingRequestHeader() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders.get("/requestHeader"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(content()
                        .string("{\"type\":\"about:blank\",\"title\":\"Method argument not valid\",\"status\":400,\"detail\":\"Input validation error, parser raised some issues that are defined in fieldErrors array\",\"instance\":\"/requestHeader\",\"fieldErrors\":[{\"objectName\":\"headers\",\"field\":\"testHeaderException\",\"message\":\"must not be null\"}],\"code\":\"INPUT_VALIDATION_METHODARGINVALID\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_handleMissingQueryParam() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders.get("/queryParam"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(content()
                .string("{\"type\":\"about:blank\",\"title\":\"Bad Request\",\"status\":400,\"detail\":\"Required query parameter 'param' is not present.\",\"instance\":\"/queryParam\",\"code\":\"MISSING_QUERY_PARAMETER\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void should_handleMethodArgumentNotValid_when_blank_name() throws Exception {

        String json = JsonParser.parseReader(new FileReader("src/test/resources/blank_name.json")).toString();

        this.mockMvc.perform(MockMvcRequestBuilders.get("/requestBody")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(content()
                        .string("{\"type\":\"about:blank\",\"title\":\"Method argument not valid\",\"status\":400,\"detail\":\"Input validation error, parser raised some issues that are defined in fieldErrors array\",\"instance\":\"/requestBody\",\"fieldErrors\":[{\"objectName\":\"fakeUser\",\"field\":\"name\",\"message\":\"must not be blank\"}],\"code\":\"INPUT_VALIDATION_METHODARGINVALID\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_handleMethodArgumentNotValid_when_invalid_email() throws Exception {

        String json = JsonParser.parseReader(new FileReader("src/test/resources/invalid_email.json")).toString();

        this.mockMvc.perform(MockMvcRequestBuilders.get("/requestBody")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(content()
                        .string("{\"type\":\"about:blank\",\"title\":\"Method argument not valid\",\"status\":400,\"detail\":\"Input validation error, parser raised some issues that are defined in fieldErrors array\",\"instance\":\"/requestBody\",\"fieldErrors\":[{\"objectName\":\"fakeUser\",\"field\":\"email\",\"message\":\"must be a well-formed email address\"}],\"code\":\"INPUT_VALIDATION_METHODARGINVALID\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_handleMethodArgumentNotValid_when_less_than_min_age() throws Exception {

        String json = JsonParser.parseReader(new FileReader("src/test/resources/less_than_min_age.json")).toString();

        this.mockMvc.perform(MockMvcRequestBuilders.get("/requestBody")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(content()
                        .string("{\"type\":\"about:blank\",\"title\":\"Method argument not valid\",\"status\":400,\"detail\":\"Input validation error, parser raised some issues that are defined in fieldErrors array\",\"instance\":\"/requestBody\",\"fieldErrors\":[{\"objectName\":\"fakeUser\",\"field\":\"age\",\"message\":\"must be greater than or equal to 1\"}],\"code\":\"INPUT_VALIDATION_METHODARGINVALID\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_handleMethodArgumentNotValid_when_greater_than_max_age() throws Exception {

        String json = JsonParser.parseReader(new FileReader("src/test/resources/greater_than_max_age.json")).toString();

        this.mockMvc.perform(MockMvcRequestBuilders.get("/requestBody")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(content()
                        .string("{\"type\":\"about:blank\",\"title\":\"Method argument not valid\",\"status\":400,\"detail\":\"Input validation error, parser raised some issues that are defined in fieldErrors array\",\"instance\":\"/requestBody\",\"fieldErrors\":[{\"objectName\":\"fakeUser\",\"field\":\"age\",\"message\":\"must be less than or equal to 150\"}],\"code\":\"INPUT_VALIDATION_METHODARGINVALID\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_handleMethodArgumentNotValid_when_message_is_too_short() throws Exception {

        String json = JsonParser.parseReader(new FileReader("src/test/resources/message_too_short.json")).toString();

        this.mockMvc.perform(MockMvcRequestBuilders.get("/requestBody")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(content()
                        .string("{\"type\":\"about:blank\",\"title\":\"Method argument not valid\",\"status\":400,\"detail\":\"Input validation error, parser raised some issues that are defined in fieldErrors array\",\"instance\":\"/requestBody\",\"fieldErrors\":[{\"objectName\":\"fakeUser\",\"field\":\"message\",\"message\":\"size must be between 10 and 100\"}],\"code\":\"INPUT_VALIDATION_METHODARGINVALID\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_handleHttpMessageNotReadable() throws Exception {

        String json = JsonParser.parseReader(new FileReader("src/test/resources/invalid_age.json")).toString();

        this.mockMvc.perform(MockMvcRequestBuilders.get("/requestBody")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(content()
                        .string("{\"type\":\"about:blank\",\"title\":\"Invalid json message received\",\"status\":400,\"detail\":\"JSON parse error: Cannot deserialize value of type `java.lang.Integer` from String \\\"aaaa\\\": not a valid `java.lang.Integer` value\",\"instance\":\"/requestBody\",\"code\":\"INPUT_JSON_INVALID\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_handleTypeMismatch() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders.get("/typeMismatch"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(content()
                        .string("{\"type\":\"about:blank\",\"title\":\"Bad Request\",\"status\":400,\"detail\":\"Failed to convert 'null' with value: 'TypeMismatch'\",\"instance\":\"/typeMismatch\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_handleMethodArgumentTypeMismatch() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders.get("/methodArgTypeMismatch/exception")
                        .header("X-WCP-TraceId", "WCP-TraceId-Test"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(content()
                        .string("{\"type\":\"about:blank\",\"title\":\"Bad Request\",\"status\":400,\"detail\":\"Property 'id' with value 'exception' is not a valid Long\",\"instance\":\"/methodArgTypeMismatch/exception\",\"code\":\"INPUT_VALIDATION_METHODARGINVALID\"}"))
                .andExpect(header().string("X-WCP-TraceId", "WCP-TraceId-Test"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_handleHttpMediaTypeNotSupported() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders.get("/httpMediaTypeNotSupportedException"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(content()
                        .string("{\"type\":\"about:blank\",\"title\":\"Unsupported Media Type\",\"status\":415,\"detail\":\"Could not parse Content-Type.\",\"instance\":\"/httpMediaTypeNotSupportedException\",\"code\":\"UNSUPPORTED_MEDIA_TYPE\"}"))
                .andExpect(status().isUnsupportedMediaType());
    }
}
