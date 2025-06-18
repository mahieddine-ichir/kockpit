import { strc } from '@/flex-layout-2/util/xml-json-utils/xml-str-strings'

export type ElType =
    | 'task'
    | 'gateway'
    | 'exclusiveGateway'
    | 'ComplexGateway'
    | 'startEvent'
    | 'endEvent'

/** El can be any element except sourceFlow */
// export class El {
//   id: string
//   type: ElType
//   name?: string
//   description?: string
//   incomingFlowsIDs: string[]
//   outgoingFlowsIDs: string[]

//   constructor({
//     id,
//     type,
//     incomingFlowsIDs,
//     outgoingFlowsIDs,
//     name,
//     description,
//   }: {
//     id: string
//     type: ElType
//     incomingFlowsIDs: string[]
//     outgoingFlowsIDs: string[]
//     name?: string
//     description?: string
//   }) {
//     this.id = id
//     this.type = type
//     this.name = name
//     this.description = description
//     this.incomingFlowsIDs = incomingFlowsIDs
//     this.outgoingFlowsIDs = outgoingFlowsIDs
//   }

//   toXmlTag(): string {
//     const attributes = [
//       `id="${this.id}"`,
//       'name' in this && typeof this.name !== 'undefined'
//         ? `name="${this.name}"`
//         : '',
//       'description' in this && typeof this.description !== 'undefined'
//         ? `description="${strc.json2xml(this.description)}"`
//         : '',
//     ].join(' ')

//     const incomingFlows = this.incomingFlowsIDs
//       .map((flowId) => `<bpmn:incoming>${flowId}</bpmn:incoming>`)
//       .join('')

//     const outgoingFlows = this.outgoingFlowsIDs
//       .map((flowId) => `<bpmn:outgoing>${flowId}</bpmn:outgoing>`)
//       .join('')

//     return `<bpmn:${this.type} ${attributes}>${incomingFlows}${outgoingFlows}</bpmn:${this.type}>`
//   }
// }

export class El {
  id: string;
  type: 'task' | 'exclusiveGateway' | 'ComplexGateway';
  name?: string;
  description?: string;
  incomingFlowsIDs: string[];
  outgoingFlowsIDs: string[];

  constructor(params: {
    id: string;
    type: 'task' | 'exclusiveGateway' | 'ComplexGateway';
    incomingFlowsIDs?: string[];
    outgoingFlowsIDs?: string[];
    name?: string;
    description?: string;
  }) {
    this.id = params.id;
    this.type = params.type;
    this.name = params.name;
    this.description = params.description;
    this.incomingFlowsIDs = params.incomingFlowsIDs || [];
    this.outgoingFlowsIDs = params.outgoingFlowsIDs || [];
  }

  toXmlTag(): string {
    const attrs = [
      `id="${this.id}"`,
      this.name ? `name="${this.name}"` : '',
      this.description ? `description="${strc.json2xml(this.description)}"` : ''
    ].filter(Boolean).join(' ');

    const incoming = this.incomingFlowsIDs.map(id => `<bpmn:incoming>${id}</bpmn:incoming>`).join('');
    const outgoing = this.outgoingFlowsIDs.map(id => `<bpmn:outgoing>${id}</bpmn:outgoing>`).join('');

    return `<bpmn:${this.type} ${attrs}>${incoming}${outgoing}</bpmn:${this.type}>`;
  }
}

export class SequenceFlow {
  id: string;
  sourceRef: string;
  targetRef: string;
  name?: string;

  constructor(params: {
    id?: string;
    sourceRef: string;
    targetRef: string;
    name?: string;
  }) {
    this.id = params.id || `Flow_${Math.random().toString(36).substring(2, 9)}`;
    this.sourceRef = params.sourceRef;
    this.targetRef = params.targetRef;
    this.name = params.name;
  }

  toXmlTag(): string {
    const attrs = [
      `id="${this.id}"`,
      `sourceRef="${this.sourceRef}"`,
      `targetRef="${this.targetRef}"`,
      this.name ? `name="${this.name}"` : ''
    ].filter(Boolean).join(' ');
    return `<bpmn:sequenceFlow ${attrs} />`;
  }
}

// export class SequenceFlow {
//   id: string
//   name?: string
//   description?: string
//   sourceRef: string
//   targetRef: string

//   constructor({
//     sourceRef,
//     targetRef,
//     name,
//     description,
//   }: {
//     sourceRef: string
//     targetRef: string
//     name?: string
//     description?: string
//   }) {
//     this.id = this.generateId(sourceRef, targetRef)
//     // this.id = 'Flow_' + Math.random().toString(36).substring(2, 9)
//     this.name = name
//     this.description = description
//     this.sourceRef = sourceRef
//     this.targetRef = targetRef
//   }

//   private generateId(sourceRef: string, targetRef: string): string {
//     const str = sourceRef + targetRef
//     let hash = 5381
//     for (let i = 0; i < str.length; i++) {
//       hash = (hash * 33) ^ str.charCodeAt(i)
//     }
//     return 'Flow_' + (hash >>> 0).toString(36)
//   }

//   toXmlTag(): string {
//     const attributes = [
//       `id="${this.id}"`,
//       'name' in this && typeof this.name !== 'undefined'
//         ? `name="${this.name}"`
//         : '',
//       'description' in this && typeof this.description !== 'undefined'
//         ? `description="${strc.json2xml(this.description)}"`
//         : '',
//       `sourceRef="${this.sourceRef}"`,
//       `targetRef="${this.targetRef}"`,
//     ].join(' ')
//     return `<bpmn:sequenceFlow ${attributes} />`
//   }
// }

export function wrap({
                       xml,
                       id,
                       name,
                       description,
                       className,
                     }: {
  xml: string
  id: string
  name: string | null
  description: string | null
  className: string | null
}) {
  return getStart({ id, name, description, className }) + xml + getEnd(id)
}

function getStart({
                    id,
                    name,
                    description,
                    className,
                  }: {
  id: string
  name: string | null
  description: string | null
  className: string | null
}) {
  const start =
      '<?xml version="1.0" encoding="UTF-8"?>' +
      '<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" ' +
      'xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" ' +
      'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ' +
      'targetNamespace="http://bpmn.io/schema/bpmn">' +
      '<bpmn:process' +
      ' ' +
      // prettier-ignore
      [
        id          !== null ? 'id="'          + id          + '"' : '',
        name        !== null ? 'name="'        + name        + '"' : '',
        description !== null ? 'description="' + description + '"' : '',
        className   !== null ? 'className="'   + className   + '"' : '',
      ].join(' ') +
      ' ' +
      'isExecutable="false">'

  return start
}

function getEnd(id: string) {
  return `  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="${id}" />
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`
}
