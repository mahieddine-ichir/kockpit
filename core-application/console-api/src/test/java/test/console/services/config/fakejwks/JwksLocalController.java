package test.console.services.config.fakejwks;

import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStreamReader;

@RestController()
public class JwksLocalController {

    @GetMapping(value = "/public/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public String jwksJson() throws IOException {
        return FileCopyUtils.copyToString(new InputStreamReader(getClass().getResourceAsStream("/local/jwks.json")));
    }

    @GetMapping(value = "/public/.well-known/openid-configuration", produces = MediaType.APPLICATION_JSON_VALUE)
    public String openidConfiguration() throws IOException {
        return FileCopyUtils.copyToString(new InputStreamReader(getClass().getResourceAsStream("/local/openid-configuration.json")));
    }

}
