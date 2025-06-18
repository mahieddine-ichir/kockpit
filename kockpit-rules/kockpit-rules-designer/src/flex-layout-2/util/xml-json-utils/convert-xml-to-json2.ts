import { BElement, BLink, p, renameId, s } from './util'
import { BpmnComplexGateway,BpmnExclusiveGateway, BpmnTask, XMLObj } from './xml-obj-type'
import { xmlParser } from './xml-parser'
import { JSON2 } from './util'
import { strc } from './xml-str-strings'

export function convertXmlToJson2(xml: string): JSON2 {
  const parsed = xmlParser.parse(xml) as XMLObj
  const process = parsed[s.bpmnDefinitions][s.bpmnProcess]

  const parsed2: JSON2 = {
    ruleId: process['@_id'],
    elements: [],
    links: [],
  }

  if (s.bpmnTask in process) {
    const tasks = process[s.bpmnTask]
    if (Array.isArray(tasks)) {
      parsed2.elements.push(
          ...tasks.map((task) => createBElement(task, 'action'))
      )
    } else {
      const task = tasks as BpmnTask
      parsed2.elements.push(createBElement(task, 'action'))
    }
  }

  if (s.bpmnExclusiveGateway in process) {
    const gateWays = process[s.bpmnExclusiveGateway]
    if (Array.isArray(gateWays)) {
      parsed2.elements.push(
          ...gateWays.map((gateway) => createBElement(gateway, 'predicate'))
      )
    } else {
      const gateway = gateWays as BpmnExclusiveGateway
      parsed2.elements.push(createBElement(gateway, 'predicate'))
    }
  }

  if (s.bpmnComplexGateway in process) {
    const complexGateways = process[s.bpmnComplexGateway]
    if (Array.isArray(complexGateways)) {
      parsed2.elements.push(
          ...complexGateways.map((gateway) => createBElement(gateway, 'ComplexGateway'))
      )
    } else {
      parsed2.elements.push(createBElement(complexGateways as BpmnComplexGateway, 'ComplexGateway'))
    }
  }

  if (s.bpmnSequenceFlow in process) {
    const sequenceFlowElements = process[s.bpmnSequenceFlow]
    if (Array.isArray(sequenceFlowElements)) {
      parsed2.links.push(
          ...sequenceFlowElements
              .filter(
                  (sequenceFlow) =>
                      !(
                          (sequenceFlow[p.sourceRef] as string).startsWith('Event') ||
                          (sequenceFlow[p.targetRef] as string).startsWith('Event')
                      )
              )
              .map((sequenceFlow) => {
                const newSequenceFlow: BLink = {
                  id: renameId(sequenceFlow['@_id']),
                  source: renameId(sequenceFlow['@_sourceRef']),
                  target: renameId(sequenceFlow['@_targetRef']),
                }
                if (p.name in sequenceFlow) {
                  newSequenceFlow['name'] = sequenceFlow[p.name]
                }
                if (p.description in sequenceFlow) {
                  newSequenceFlow['description'] = strc.xml2json(
                      sequenceFlow[p.description]
                  )
                }
                return newSequenceFlow
              })
      )
    } else {
      if (
          typeof sequenceFlowElements !== 'undefined' &&
          !(sequenceFlowElements[p.sourceRef] as string).startsWith('Event') &&
          !(sequenceFlowElements[p.targetRef] as string).startsWith('Event')
      ) {
        const newSequenceFlow: BLink = {
          id: renameId(sequenceFlowElements['@_id']),
          source: renameId(sequenceFlowElements['@_sourceRef']),
          target: renameId(sequenceFlowElements['@_targetRef']),
        }
        if (p.name in sequenceFlowElements) {
          newSequenceFlow['name'] = sequenceFlowElements[p.name]
        }
        if (p.description in sequenceFlowElements) {
          newSequenceFlow['description'] = strc.xml2json(
              sequenceFlowElements[p.description]
          )
        }
        parsed2.links.push(newSequenceFlow)
      }
    }
  }

  if ('@_name' in process) {
    parsed2.ruleName = process['@_name']
  }
  if ('@_description' in process) {
    parsed2.ruleDescription = strc.xml2json(process['@_description'])
  }
  if ('@_className' in process) {
    parsed2['ruleClassName'] = strc.xml2json(process['@_className'])
  }
  return parsed2
}

// strc.xml2json
// strc.json2xml

function createBElement(
    element: BpmnTask | BpmnExclusiveGateway |BpmnComplexGateway,
    type: 'action' | 'predicate' | 'ComplexGateway'
) {
  const bElement: BElement = {
    id: renameId(element[p.id]),
    type,
  }
  if ('@_name' in element) {
    bElement['name'] = element[p.name]
  }
  if ('@_description' in element) {
    bElement['description'] = strc.xml2json(element[p.description])
  }
  return bElement
}
