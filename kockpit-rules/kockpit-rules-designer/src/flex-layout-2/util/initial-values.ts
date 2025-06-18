import { IJsonModel } from 'flexlayout-react'

export type Component =
  | 'Canvas'
  | 'Problems'
  | 'JsonEditor'
  | 'Properties'
  | 'MermaidFlowChart'

const layoutJsonMain: IJsonModel = {
  global: {
    tabSetMinWidth: 100,
    tabSetMinHeight: 100,
    borderMinSize: 100,
    borderEnableAutoHide: true,
  },
  borders: [
    {
      type: 'border',
      location: 'bottom',
      children: [],
    },
    {
      type: 'border',
      location: 'right',
      children: [],
    },
    {
      type: 'border',
      size: 260,
      location: 'left',
      children: [],
    },
  ],
  layout: {
    type: 'row',
    id: '#a47eca02-799a-4503-8292-7f94b5eda689',
    children: [
      {
        type: 'row',
        id: '#ad059e00-3f5c-456d-bec3-ef8c49da228b',
        weight: 83.86075949367088,
        children: [
          {
            type: 'row',
            id: '#4775d0cf-ae67-442f-8cee-9417aa714e32',
            weight: 45.13368983957219,
            children: [
              {
                type: 'tabset',
                id: '#6f6f91fa-5ca4-407c-9d95-6bc046bd0528',
                weight: 54.21098983741849,
                children: [
                  {
                    type: 'tab',
                    id: '#47d36169-89e8-4e5f-b8c8-40f55a5a1b15',
                    name: 'Json Editor',
                    component: 'JsonEditor',
                  },
                ],
                active: true,
              },
              {
                type: 'tabset',
                id: '#94e08494-13ca-4b32-bc40-681b268da51d',
                weight: 22.35570024837468,
                children: [
                  {
                    type: 'tab',
                    id: '#47d36139-83e8-4e5f-b839-41f55a5a1b15',
                    name: 'Properties',
                    component: 'Properties',
                  },
                ],
              },
              {
                type: 'tabset',
                id: '#20c97bc2-04a0-4090-97a2-c9c8ff9ca0c3',
                weight: 23.433309914206834,
                children: [
                  {
                    type: 'tab',
                    id: '#47d36169-82e8-4eff-b8c8-53395faa1b15',
                    name: 'Problems',
                    component: 'Problems',
                  },
                ],
              },
            ],
          },
          {
            type: 'tabset',
            id: '#73c604d0-f860-4181-b0e4-1bbb20f771b2',
            weight: 54.86631016042781,
            children: [
              {
                type: 'tab',
                id: '#47d36167-82e8-4e3f-b8c8-50f95aaa1b15',
                name: 'Canvas',
                component: 'Canvas',
              },
              {
                type: 'tab',
                id: '#01936167-82e8-4e3f-b8c8-50f95aaa1b15',
                name: 'Mermaid',
                component: 'MermaidFlowChart',
              },
            ],
          },
        ],
      },
    ],
  },
  popouts: {},
}

export const initialLayoutJson: IJsonModel = layoutJsonMain

export const emptyBpmnXml = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="Process_1" isExecutable="false" />
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1" />
  </bpmndi:BPMNDiagram>
</bpmn:definitions>
`

export const initialDiagramJson = `{
  "name": "",
  "description": "",
  "steps": []
}`
