package com.accor.wcp.helloworld;

import com.accor.wcp.sdk.application.service.dynaconfig.DynaConfigAttribute;
import com.accor.wcp.sdk.application.service.dynaconfig.DynaConfigEnabler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@DynaConfigEnabler
@RestController
public class WorldController {

    @Value("${helloapp.greeting:hello}")
    @DynaConfigAttribute
    private String helloGreeting;

    @GetMapping("/world/{name}")
    public String home(HttpServletRequest request, @PathVariable("name") @NotBlank String name) {
        request.setAttribute("hello_name", name);
        return helloGreeting + " " + name;
    }
}
