import { XMLParser, XMLBuilder } from 'fast-xml-parser'

export const xmlParser = new XMLParser({
  ignoreAttributes: false,
  attributeNamePrefix: '@_',
  // preserveOrder: true,
})

function getDiagramXmlBuilder() {
  function buildXml(obj: { [k in any]: any }) {
    const xmlBuilder = new XMLBuilder({
      ignoreAttributes: false,
      attributeNamePrefix: '@_',
    })
    const xmlPre = xmlBuilder.build(obj)
    // prettier-ignore
    // this below is done because of a bug in fast-xml-parser library
    const newXml = xmlPre.replace(
      / name sourceRef="/g,
      ' name="true" sourceRef="'
    )
    return newXml
  }
  return buildXml
}

export const buildXml = getDiagramXmlBuilder()
