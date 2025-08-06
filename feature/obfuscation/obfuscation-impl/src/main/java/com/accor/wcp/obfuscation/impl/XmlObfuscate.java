package com.accor.wcp.obfuscation.impl;

import static java.util.Objects.nonNull;

import com.accor.wcp.obfuscation.Obfuscate;
import com.accor.wcp.obfuscation.impl.obfuscators.xml.XmlObfuscateConfig;
import com.accor.wcp.obfuscation.impl.obfuscators.xml.XmlObfuscateConfig.PathConfig;
import com.accor.wcp.obfuscation.masker.MaskerService;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Slf4j
class XmlObfuscate implements Obfuscate<XmlObfuscateConfig> {

  private static final String REGEX_ATTRIBUTE = "(.*)\\[@(.*)]";

  private final Pattern xpathPatternAttribute;

  private final MaskerService maskerService;

  public XmlObfuscate(MaskerService maskerService) {
    this.maskerService = maskerService;
    xpathPatternAttribute = Pattern.compile(REGEX_ATTRIBUTE);
  }

  @Override
  public String doObfuscate(String data, XmlObfuscateConfig config) {
    try {
      var builderFactory = DocumentBuilderFactory.newInstance();
      builderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      builderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      builderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      var builder = builderFactory.newDocumentBuilder();

      var xmlDocument = builder.parse(new InputSource(new StringReader(data)));

      var xPath = XPathFactory.newInstance().newXPath();

      config
          .getPathConfigs()
          .forEach(
              pathConfig -> {
                try {
                  @SuppressWarnings("findsecbugs:XPATH_INJECTION")
                  var nodeList =
                      (NodeList)
                          xPath
                              .compile(pathConfig.getPath())
                              .evaluate(xmlDocument, XPathConstants.NODESET);
                  maskNodeList(nodeList, pathConfig);
                } catch (XPathExpressionException e) {
                  log.debug("xPath expression error on: {}, error={}", pathConfig, e.getMessage());
                }
              });

      var tf = TransformerFactory.newInstance();
      tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
      var transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      var writer = new StringWriter();
      transformer.transform(new DOMSource(xmlDocument), new StreamResult(writer));

      return writer.getBuffer().toString();
    } catch (Exception e) {
      log.debug("An exception occurred during process", e);
      return data;
    }
  }

  private void maskNodeList(NodeList nodeList, PathConfig pathConfig) {
    var matcher = xpathPatternAttribute.matcher(pathConfig.getPath());
    String attribute = null;
    if (matcher.find()) {
      attribute = matcher.group(2);
    }
    String maskerId = pathConfig.getMaskerId();
    for (var i = 0; i < nodeList.getLength(); i++) {
      var node = nodeList.item(i);
      if (nonNull(attribute)) {
        // Attribute case
        Node namedItem = node.getAttributes().getNamedItem(attribute);
        if (nonNull(namedItem)) {
          namedItem.setNodeValue(maskerService.mask(namedItem.getNodeValue(), maskerId));
        }
      } else {
        // Node value case
        node.setTextContent(maskerService.mask(node.getTextContent(), maskerId));
      }
    }
  }
}
