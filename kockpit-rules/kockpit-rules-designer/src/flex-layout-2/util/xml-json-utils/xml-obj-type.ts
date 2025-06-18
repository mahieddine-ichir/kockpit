export type BpmnTask = {
  '@_id': string
  '@_name'?: string
  '@_description'?: string
  'bpmn:incoming'?: string[] | string
  'bpmn:outgoing'?: string[] | string
}

export type BpmnExclusiveGateway = {
  '@_id': string
  '@_name'?: string
  '@_description'?: string
  'bpmn:incoming'?: string[] | string
  'bpmn:outgoing'?: string[] | string
}

export type BpmnComplexGateway = {
  '@_id': string
  '@_name'?: string
  '@_description'?: string
  'bpmn:incoming'?: string[] | string
  'bpmn:outgoing'?: string[] | string
  '@_default'?: string // this is the default we will see later what i do with
}


export type BpmnSequenceFlow = {
  '@_id': string
  '@_name'?: string
  '@_description'?: string
  '@_sourceRef': string
  '@_targetRef': string
}

export type BpmndiBpmnShape = {
  '@_id': string
  '@_bpmnElement': string
  'dc:Bounds': {
    '@_x': string
    '@_y': string
    '@_width': string
    '@_height': string
  }
}


export type XMLObj = {
  '?xml': {
    '@_version': '1.0'
    '@_encoding': 'UTF-8'
  }
  'bpmn:definitions': {
    'bpmn:process': {
      '@_id': string
      '@_name'?: string
      '@_description'?: string
      '@_className'?: string
      '@_isExecutable': 'false'
      'bpmn:task'?: Array<BpmnTask> | BpmnTask
      'bpmn:exclusiveGateway'?:
          | Array<BpmnExclusiveGateway>
          | BpmnExclusiveGateway
      'bpmn:complexGateway'?: Array<BpmnComplexGateway> | BpmnComplexGateway
      'bpmn:sequenceFlow'?: Array<BpmnSequenceFlow> | BpmnSequenceFlow
    }
    'bpmndi:BPMNDiagram'?: {
      '@_id': string
      'bpmndi:BPMNPlane': {
        'bpmndi:BPMNShape': Array<BpmndiBpmnShape> | BpmndiBpmnShape
      }
    }
    '@_xmlns:bpmn': 'http://www.omg.org/spec/BPMN/20100524/MODEL'
    '@_xmlns:bpmndi': 'http://www.omg.org/spec/BPMN/20100524/DI'
    '@_xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance'
    '@_xmlns:dc': 'http://www.omg.org/spec/DD/20100524/DC'
    '@_xmlns:di': 'http://www.omg.org/spec/DD/20100524/DI'
    '@_targetNamespace': 'http://bpmn.io/schema/bpmn'
  }
}
