import Modeler from 'bpmn-js/lib/Modeler'
import { useEffect, useRef, useId } from 'react'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import { createModeler } from '../util/create-modeler'
import { useAtom, useSetAtom } from 'jotai'
import {
  elementTypeAtom,
  elementIdAtom,
  elementNameAtom,
  elementDescriptionAtom,
} from '../atoms'
import { convertBpmnXmlBaseToDiagramXml } from '../util/convert-xml-base-to-xml'
import { convertXmlToJson } from '../util/xml-json-utils/convert-xml-to-json'
import { useCanvasXmlHistory, useDiagramJson } from '../hooks/custom-hooks'
import { JSON3, Step } from '../util/xml-json-utils/convert-json2-to-json3'
import { strc } from '../util/xml-json-utils/xml-str-strings'
import { buildXml, xmlParser } from '../util/xml-json-utils/xml-parser'
import { BpmndiBpmnShape, XMLObj } from '../util/xml-json-utils/xml-obj-type'
import { bpmnZoomLevelAtom } from './BpmnCanvas'

type viewboxChangeEvent = {
  viewbox: {
    x: number
    y: number
    width: number
    height: number
    scale: number
  }
}

export default function BpmnCanvasBase({
                                         modelerRef,
                                       }: {
  modelerRef: React.MutableRefObject<Modeler<null> | null>
}) {
  const { jsonStr, setJsonStr } = useDiagramJson()
  const setType = useSetAtom(elementTypeAtom)
  const setId = useSetAtom(elementIdAtom)
  const setName = useSetAtom(elementNameAtom)
  const setDescription = useSetAtom(elementDescriptionAtom)

  const [zoomLevel, setZoomLevel] = useAtom(bpmnZoomLevelAtom)
  const { xml, setXml } = useCanvasXmlHistory()
  const canvasId = useId()
  const isInitialized = useRef(false)

  const updateXmlState = async () => {
    if (!modelerRef || !modelerRef.current) return
    try {
      const saveXmlResult = await modelerRef.current.saveXML({ format: true })
      if (typeof saveXmlResult.xml !== 'undefined') {
        const newXml = convertBpmnXmlBaseToDiagramXml(saveXmlResult.xml)
        const obj = convertXmlToJson(newXml)
        const newObj = { ...obj } as any
        delete (newObj as any).id
        stripIds(newObj.steps)
        const stringified = JSON.stringify(newObj, null, 2)
        if (stringified !== jsonStr) {
          setJsonStr(stringified)
        }
        setXml(newXml)
      }
    } catch (err) {
      console.error('Error saving XML:', err)
    }
  }

  useEffect(() => {
    ;(async () => {
      if (!modelerRef || !modelerRef.current) return
      try {
        try {
          const parsed = xmlParser.parse(xml)
          adjustSizes(parsed)
          const newXml: string = buildXml(parsed)
          await modelerRef.current.importXML(newXml)
        } catch {
          await modelerRef.current.importXML(xml)
        }
      } catch (err) {
        console.error('Failed to import BPMN diagram:', err)
      }
      /**
       * This function call below was commented because calling it causes
       * the following problem:
       * - if json editor is changed, jsonStr can be changed
       * - if jsonStr is changed, xml is changed
       * - if xml is changed, this useEffect callback is called
       * - if callback is called, function `changeActionDefaultSize` is called
       * - which itself causes function call `modeling.resizeShape`
       * - which itself triggeres the `commandStack.changed` event
       * - which itself changes the `jsonStr`
       * - which causes the JSON editor to change (while user is editing it,
       * edits applied are not done by user, but by commandStack.changed callback)
       content is changed, this will update the xml, then this function
       below is called, then it will trigger the commandStack.changed event
       */
      // changeActionDefaultSize(modelerRef.current)
    })()
  }, [xml])

  useEffect(() => {
    ;(async () => {
      if (!isInitialized.current) {
        const canvas = document.getElementById(canvasId)

        if (canvas) {
          modelerRef.current = createModeler(canvas)
          try {
            try {
              const parsed = xmlParser.parse(xml)
              adjustSizes(parsed)
              const newXml = buildXml(parsed)
              await modelerRef.current.importXML(newXml)
            } catch {
              await modelerRef.current.importXML(xml)
            }
            // Set initial pan position
            const canvasService = modelerRef.current.get('canvas')
            // @ts-expect-error TODO:
            const viewbox = canvasService.viewbox()
            // @ts-expect-error TODO:
            canvasService.viewbox({
              x: -70,
              y: -viewbox.height * 0.37,
              width: viewbox.width,
              height: viewbox.height,
            }) //, width: 500, height: 500 }) // Adjust x, y, width, height as needed
            // @ts-expect-error TODO:
            modelerRef.current.get(canvasId).zoom('fit-viewport')
          } catch (err) {
            console.error('Failed to import BPMN diagram:', err)
          }
          modelerRef.current.on('commandStack.changed', updateXmlState)
          // modelerRef.current.on('canvas.viewbox.changed', (event) => {
          //   console.log('Viewbox changed:', event.viewbox)
          // })

          modelerRef.current.on('element.click', async (event) => {
            if (!modelerRef || !modelerRef.current) return
            try {
              // @ts-expect-error TODO: fix later
              const element = event.element
              if (element.type === 'bpmn:Process') {
                setType('Rule')
              } else if (element.type === 'bpmn:Task') {
                setType('Action')
              } else if (
                  element.type === 'bpmn:StartEvent' ||
                  element.type === 'bpmn:EndEvent'
              ) {
                setType('Event')
              } else if (element.type === 'bpmn:ExclusiveGateway') {
                setType('Predicate')
              } else if (element.type === 'bpmn:ComplexGateway') {
                setType('ComplexGateway')
              } else if (element.type === 'bpmn:SequenceFlow') {
                setType('Transition')
              }
              setName(strc.xml2json(element.businessObject.name) || '')
              setDescription(
                  strc.xml2json(element.businessObject['$attrs']?.description) ||
                  ''
              )
              setId(element.id)
            } catch (err) {
              console.error('Error saving XML:', err)
            }
          })

          modelerRef.current.on('canvas.viewbox.changed', (event: viewboxChangeEvent) => {
            setZoomLevel(event.viewbox.scale)
          })

          // Reset to Rule when clicking on canvas background
          modelerRef.current.on('root.click', () => {
            setType('Rule')
            setName('')
            setDescription('')
            setId('')
          })

          isInitialized.current = true
        }
      }
    })()

    // Cleanup function to dispose of the viewer if the component is unmounted
    return () => {
      if (modelerRef.current) {
        modelerRef.current.destroy()
        modelerRef.current = null
      }
    }
  }, [])

  return (
      <div
          id={canvasId}
          className='absolute inset-0 bg-slate-50 text-black'
      ></div>
  )
}

function changeActionDefaultSize(modeler: Modeler<null>) {
  const elementRegistry = modeler.get('elementRegistry')
  const modeling = modeler.get('modeling')
  // Adjust size of BPMN activities
  // @ts-expect-error TODO:
  elementRegistry.forEach((element) => {
    if (element.type === 'bpmn:Task' || element.type === 'bpmn:Activity') {
      // @ts-expect-error TODO:
      const shape = elementRegistry.get(element.id)
      const newWidth = 121 // default is 100
      const newHeight = 101 // default is 80
      const newBounds = {
        x: shape.x,
        y: shape.y,
        width: newWidth,
        height: newHeight,
      }
      // @ts-expect-error TODO:
      modeling.resizeShape(shape, newBounds)
    }
  })
}

export function stripIds(steps: JSON3['steps']) {
  for (const step of steps) {
    if ('id' in step) {
      delete (step as any).id
    }

    if (steps.length === 0) {
      return
    }
    const lastStep = steps.at(-1) as Step
    if (lastStep.type === 'action') {
      return
    }
    if (lastStep.type === 'predicate') {
      stripIds(lastStep.true)
      stripIds(lastStep.false)
    }
  }
}

function adjustSizes(xml: XMLObj) {
  const shapes =
      xml['bpmn:definitions']['bpmndi:BPMNDiagram']?.['bpmndi:BPMNPlane']?.[
          'bpmndi:BPMNShape'
          ]
  if (typeof shapes === 'undefined') {
    return
  }
  if (Array.isArray(shapes)) {
    shapes.forEach((shape) => adjustShapeSize(shape))
  } else {
    const shape = shapes
    adjustShapeSize(shape)
  }
}

function adjustShapeSize(shape: BpmndiBpmnShape) {
  if (shape['@_id'].startsWith('Action')) {
    shape['dc:Bounds']['@_height'] = '95'
    // shape['dc:Bounds']['@_width'] = '150'
  }
}
