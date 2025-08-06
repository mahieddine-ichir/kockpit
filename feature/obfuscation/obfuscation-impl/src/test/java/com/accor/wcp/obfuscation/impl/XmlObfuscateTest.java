package com.accor.wcp.obfuscation.impl;

import com.accor.wcp.obfuscation.impl.masker.MaskerServiceImpl;
import com.accor.wcp.obfuscation.impl.masker.maskers.EmailMasker;
import com.accor.wcp.obfuscation.impl.masker.maskers.KeepFirstNbCharsMasker;
import com.accor.wcp.obfuscation.impl.masker.maskers.KeepLastNbCharsMasker;
import com.accor.wcp.obfuscation.impl.obfuscators.xml.XmlObfuscateConfig;
import com.accor.wcp.obfuscation.impl.obfuscators.xml.XmlObfuscateConfig.PathConfig;
import com.accor.wcp.obfuscation.masker.MaskerService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.nio.file.Files.readString;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
class XmlObfuscateTest {
  private static XmlObfuscateConfig createObfuscateConfig() {
    return XmlObfuscateConfig.builder()
        .pathConfigs(
            List.of(
                PathConfig.builder()
                    .path(
                        "/xml/list/ReloadReservationTO/reloadReservableTOs/ReloadReservableTO/recipientEmail")
                    .maskerId("email")
                    .build(),
                PathConfig.builder()
                    .path(
                        "/xml/list/ReloadReservationTO/reloadReservableTOs/ReloadReservableTO/reserverEmail")
                    .maskerId("email")
                    .build(),
                    PathConfig.builder()
                            .path(
                                    "/xml/list/ReloadReservationTO/reloadReservableTOs/ReloadReservableTO/recipientFirstname")
                            .maskerId("keepLast2")
                    .build(),
                PathConfig.builder()
                    .path(
                        "/xml/list/ReloadReservationTO/reloadReservableTOs/ReloadReservableTO/hostFirstname")
                    .build(),
                    PathConfig.builder()
                            .path(
                                    "/xml/list/ReloadReservationTO/reloadReservableTOs/ReloadReservableTO/recipientTel")
                            .maskerId("keepLast4")
                    .build(),
                PathConfig.builder()
                    .path("/xml/list/ReloadReservationTO/hostTOList/HostTO[@email]")
                    .maskerId("email")
                    .build(),
                // this path does not exist
                PathConfig.builder().path("/xml/list/ReloadReservationTO/tel").build(),
                // this path is not a xPath format
                PathConfig.builder().path("param*").build()))
        .build();
  }

  private static XmlObfuscate createXMLObfuscate() {
    MaskerService maskerService =
            new MaskerServiceImpl(
                    List.of(new EmailMasker(), new KeepFirstNbCharsMasker(1), new KeepFirstNbCharsMasker(2), new KeepFirstNbCharsMasker(3), new KeepFirstNbCharsMasker(4),
                            new KeepLastNbCharsMasker(1), new KeepLastNbCharsMasker(2), new KeepLastNbCharsMasker(3), new KeepLastNbCharsMasker(4)));
    return new XmlObfuscate(maskerService);
  }

  @Test
  void should_obfuscate_xml() throws URISyntaxException, IOException {
    // Given
    XmlObfuscate underTest = createXMLObfuscate();
    String xml =
        readString(
            Path.of(
                requireNonNull(XmlObfuscateTest.class.getResource("/xml/test-ob1.xml")).toURI()));
    XmlObfuscateConfig config = createObfuscateConfig();

    // When
    String obfuscated = underTest.obfuscate(xml, config);

    // Then
    assertThat(obfuscated).isNotNull();
    String expectedObfuscated =
        readString(
            Path.of(
                requireNonNull(XmlObfuscateTest.class.getResource("/xml/test-ob1-obfuscated1.xml"))
                    .toURI()));
    assertThat(obfuscated).isEqualTo(expectedObfuscated.trim());
  }

  @Test
  void should_not_obfuscate_xml_when_content_is_not_a_xml() {
    // Given
    XmlObfuscate underTest = createXMLObfuscate();
    XmlObfuscateConfig config = createObfuscateConfig();

    // When
    String obfuscated = underTest.obfuscate("it is not a valid xml !!", config);

    // Then
    assertThat(obfuscated).isEqualTo("it is not a valid xml !!");
  }

  @Test
  void should_obfuscate_1_XML() throws URISyntaxException, IOException {
    // Given
    XmlObfuscate underTest = createXMLObfuscate();
    String xml =
        readString(
            Path.of(
                requireNonNull(XmlObfuscateTest.class.getResource("/xml/test-ob1.xml")).toURI()));
    XmlObfuscateConfig config = createObfuscateConfig();

    // When
    long startTime = System.currentTimeMillis();
    String result = underTest.obfuscate(xml, config);

    // Then
    assertThat(result).isNotNull();
    log.debug("XML obfuscation take {} ms for 1 XML", System.currentTimeMillis() - startTime);
  }

  @Test
  void should_obfuscate_10000_XML_multi_threaded()
      throws URISyntaxException, IOException, InterruptedException {
    // Given
    XmlObfuscate underTest = createXMLObfuscate();
    String xml =
        readString(
            Path.of(
                requireNonNull(XmlObfuscateTest.class.getResource("/xml/test-ob1.xml")).toURI()));
    XmlObfuscateConfig config = createObfuscateConfig();

    // When
    ExecutorService es = Executors.newFixedThreadPool(20);
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < 10000; i++) {
      es.execute(() -> underTest.obfuscate(xml, config));
    }
    es.shutdown();
    boolean result = es.awaitTermination(2, TimeUnit.MINUTES);

    // Then
    assertThat(result).isTrue();
    log.debug("XML obfuscation take {} ms for 10000 XML", System.currentTimeMillis() - startTime);
  }
}
