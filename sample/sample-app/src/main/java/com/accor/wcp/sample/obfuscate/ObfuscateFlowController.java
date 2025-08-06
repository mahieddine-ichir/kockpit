package com.accor.wcp.sample.obfuscate;

import com.accor.wcp.sample.audit.kinesis.AuditKinesisProducerService;
import com.accor.wcp.sample.audit.sqs.AuditSqsService;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Deprecated
@RestController
@Validated
@RequiredArgsConstructor
public class ObfuscateFlowController {
  private final AuditKinesisProducerService auditKinesisProducerService;
  private final AuditSqsService auditSqsService;
  private final RestTemplate restTemplate;

  @Value("${application.basepath}")
  private String basepath;

  /**
   * Use to demonstrate obfuscation capacity in sqs, kinesis, http request (body and query param),
   * http exchange
   *
   * <p>Property to set obfuscate field : wcp.sdk.service.audit.obfuscate.fields
   */
  @GetMapping(
      value = "/obfuscate",
      produces = {"application/json"})
  public ResponseEntity<ObfuscateResponse> obfuscate(
      @RequestParam("secret") String secret, @RequestParam("notSecret") String notSecret) {

    restTemplate.getForObject(basepath + "/obfuscate/dummy/json?secret=hide", Object.class);
    restTemplate.getForObject(basepath + "/obfuscate/dummy/xml?notSecret=keep", Object.class);

    auditKinesisProducerService.sendToKinesis(1);
    auditSqsService.sendToSqs(1);

    return ResponseEntity.ok(
        ObfuscateResponse.builder().secret(secret).notSecret(notSecret).build());
  }

  @GetMapping(
      value = "/obfuscate/dummy/json",
      produces = {"application/json"})
  public ResponseEntity<String> dummyJson() {
    return ResponseEntity.ok("{\"secret\" : \" to hide\", \"notSecret\" : \"keep\"}");
  }

  @GetMapping(
      value = "/obfuscate/dummy/xml",
      produces = {"application/xml"})
  public ResponseEntity<String> dummyXml() {
    return ResponseEntity.ok(
        "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
            + "<root>"
            + "  <secret>to hide</secret>"
            + "  <notSecret>keep</notSecret>"
            + "</root>");
  }

  @GetMapping(
      value = "/obfuscate/basket/xml",
      produces = {"application/xml"})
  public ResponseEntity<String> basketXml() throws URISyntaxException, IOException {
    String xml = Files.readString(Path.of(getClass().getResource("/data/basket1.xml").toURI()));
    return ResponseEntity.ok(xml);
  }

  @Builder
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ObfuscateResponse implements Serializable {
    private String notSecret;
    private String secret;
  }
}
