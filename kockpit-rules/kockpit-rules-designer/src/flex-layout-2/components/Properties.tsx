import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import {
  MoveRight,
  Circle,
  Diamond,
  Network,
  RectangleHorizontal,
  GitBranch
} from 'lucide-react'
import { ScrollArea } from '@/components/ui/scroll-area'
import { useAtom, useAtomValue } from 'jotai'
import {
  elementNameAtom,
  elementTypeAtom,
  elementDescriptionAtom,
  elementIdAtom,
  ElementType,
} from '../atoms'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { useDebounceCallback } from 'usehooks-ts'
import { useCanvasXmlHistory } from '../hooks/custom-hooks'
import { useDiagramJson } from '../hooks/custom-hooks'
import { Step } from '../util/xml-json-utils/convert-json2-to-json3'
import { buildXml, xmlParser } from '../util/xml-json-utils/xml-parser'
import {
  BpmnExclusiveGateway,
  BpmnTask,
  XMLObj,
} from '../util/xml-json-utils/xml-obj-type'
import { toast } from 'sonner'
import { assertCannotReach } from '@/lib/utils'
import { convertXmlToJson } from '../util/xml-json-utils/convert-xml-to-json'
import { stripIds } from './BpmnCanvasBase'
import { strc } from '../util/xml-json-utils/xml-str-strings'

export default function Properties() {
  const { xml, setXml } = useCanvasXmlHistory()
  const id = useAtomValue(elementIdAtom)
  const [name, setName] = useAtom(elementNameAtom)
  const [description, setDescription] = useAtom(elementDescriptionAtom)
  const type = useAtomValue(elementTypeAtom)
  const updateElementPropertyDebounced = useDebounceCallback(
      updateElementProperty,
      700
  )
  const { jsonStr, setJsonStr } = useDiagramJson()

  function handleNameChange(e: React.ChangeEvent<HTMLInputElement>) {
    setName(e.target.value)
    updateElementPropertyDebounced({
      elementType: type,
      id: id,
      propertyName: 'name',
      newValue: e.target.value,
      jsonStr,
      setJsonStr,
      xml,
      setXml,
    })
  }

  function handleDescriptionChange(e: React.ChangeEvent<HTMLTextAreaElement>) {
    setDescription(e.target.value)
    updateElementPropertyDebounced({
      elementType: type,
      id: id,
      propertyName: 'description',
      newValue: e.target.value,
      jsonStr,
      setJsonStr,
      xml,
      setXml,
    })
  }

  return (
      <div className='absolute inset-0 bg-secondary'>
        <ScrollArea className='flex h-full flex-col gap-4 px-3'>
          <div className='mx-1'>
            <div className='h-2' />
            <h2 className='mb-6 text-xl font-bold'>Properties</h2>
            <div className='flex items-center justify-between'>
              <h3 className='text-md font-bold'>{type}</h3>
              {type === 'Rule' && <Network className='h-6 w-6' />}
              {type === 'Action' && <RectangleHorizontal className='h-6 w-6' />}
              {type === 'Predicate' && <Diamond className='h-6 w-6' />}
              {type === 'ComplexGateway' && <GitBranch className='h-6 w-6' />}
              {type === 'Event' && <Circle className='h-6 w-6' />}
              {type === 'Transition' && <MoveRight className='h-6 w-6' />}
            </div>

            <div className='h-4' />
            <div className='mb-4 hidden'>
              <Label htmlFor='id'>ID</Label>
              <Input value={id} className='bg-background' disabled />
            </div>

            <div className='mb-4'>
              <Label htmlFor='name'>
                {type === 'Transition' ? 'Value' : 'Name'}
              </Label>
              {type === 'Transition' ? (
                  <Select>
                    <SelectTrigger className='bg-background'>
                      <SelectValue placeholder='Select a value' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='true'>true</SelectItem>
                      <SelectItem value='false'>false</SelectItem>
                    </SelectContent>
                  </Select>
              ) : (
                  <Input
                      value={name}
                      className='bg-background'
                      onChange={handleNameChange}
                  />
              )}
            </div>

            <div className='mb-4'>
              <Label htmlFor='description'>Description</Label>
              <Textarea
                  value={description}
                  rows={4}
                  className='bg-background'
                  onChange={handleDescriptionChange}
              />
            </div>
          </div>
        </ScrollArea>
      </div>
  )
}

function updateElementProperty({
                                 elementType,
                                 id,
                                 propertyName,
                                 newValue,
                                 jsonStr,
                                 setJsonStr,
                                 xml,
                                 setXml,
                               }: {
  elementType: ElementType
  id: string
  propertyName: 'name' | 'description'
  newValue: string
  jsonStr: string
  setJsonStr: (jsonStr: string) => void
  xml: string
  setXml: (xml: string) => void
}) {
  updateStates({
    elementType,
    xml,
    setXml,
    id,
    propertyName,
    newValue,
    setJsonStr,
  })
}

// function updateJsonStr({
//   elementType,
//   jsonStr,
//   setJsonStr,
//   id,
//   propertyName,
//   newValue,
// }: {
//   elementType: ElementType
//   jsonStr: string
//   setJsonStr: (jsonStr: string) => void
//   id: string
//   propertyName: 'name' | 'description'
//   newValue: string
// }) {
//   try {
//     const jsonObj = JSON.parse(jsonStr) as JSON3
//     if (elementType === 'Rule') {
//       jsonObj[propertyName] = newValue
//       setJsonStr(JSON.stringify(jsonObj, null, 2))
//     } else if (elementType === 'Action') {
//       const element = getElementWithId(jsonObj.steps, id)
//       if (element !== null) {
//         element[propertyName] = newValue
//       }
//       setJsonStr(JSON.stringify(jsonObj, null, 2))
//     } else {
//       toast.error(
//         `Changing the ${propertyName} of ${elementType} is not supported yet.`
//       )
//     }
//   } catch (e) {
//     toast.error(`Error updating property ${propertyName} of ${elementType}.`)
//     console.error(
//       `Error updating property ${propertyName} of ${elementType}:`,
//       e
//     )
//     return
//   }
// }



function updateStates({
                        elementType,
                        xml,
                        setXml,
                        id,
                        propertyName,
                        newValue,
                        setJsonStr,
                      }: {
  elementType: ElementType
  xml: string
  setXml: (xml: string) => void
  id: string
  propertyName: 'name' | 'description'
  newValue: string
  setJsonStr: (jsonStr: string) => void
}) {
  try {
    const xmlObj = xmlParser.parse(xml) as XMLObj
    const process = xmlObj['bpmn:definitions']['bpmn:process']

    if (elementType === 'Rule') {
      if (propertyName === 'name') {
        process['@_name'] = newValue
      } else if (propertyName === 'description') {
        process['@_description'] = newValue
      } else {
        assertCannotReach(propertyName)
      }
      updateStatesInner({
        xmlObj,
        setXml,
        setJsonStr,
      })
    }
    else if (elementType === 'Action') {
      const elements = process['bpmn:task']
      const element = Array.isArray(elements)
          ? elements.find((e) => e['@_id'] === id)
          : elements?.['@_id'] === id ? elements : null

      if (element) {
        if (propertyName === 'name') {
          element['@_name'] = newValue
        } else if (propertyName === 'description') {
          element['@_description'] = strc.json2xml(newValue)
        } else {
          assertCannotReach(propertyName)
        }
        updateStatesInner({ xmlObj, setXml, setJsonStr })
      }
    }
    else if (elementType === 'Predicate' || elementType === 'ComplexGateway') {
      const elements = elementType === 'Predicate'
          ? process['bpmn:exclusiveGateway']
          : process['bpmn:complexGateway']

      const element = Array.isArray(elements)
          ? elements.find((e) => e['@_id'] === id)
          : elements?.['@_id'] === id ? elements : null

      if (element) {
        if (propertyName === 'name') {
          element['@_name'] = newValue
        } else if (propertyName === 'description') {
          element['@_description'] = strc.json2xml(newValue)
        } else {
          assertCannotReach(propertyName)
        }
        updateStatesInner({ xmlObj, setXml, setJsonStr })
      }
    }
    else if (elementType === 'Event') {
      toast.error(`Changing properties of ${elementType} is not supported yet.`)
    }
    else if (elementType === 'Transition') {
      toast.error(`Changing properties of ${elementType} is not supported yet.`)
    }
    else {
      toast.error(
          `Changing the ${propertyName} of ${elementType} is not supported yet.`
      )
    }
  } catch (e) {
    toast.error(`Error updating property ${propertyName} of ${elementType}.`)
    console.error(
        `Error updating property ${propertyName} of ${elementType}:`,
        e
    )
    return
  }
}


function getElementWithId(steps: Step[], id: string): Step | null {
  for (const step of steps) {
    if (step.id === id) {
      return step
    }
  }
  if (steps.length > 0) {
    const lastStep = steps.at(-1) as Step
    const { type } = lastStep
    if (type === 'action') {
      return null
    } else if (type === 'predicate') {
      const foundStep = getElementWithId(lastStep.true, id)
      if (foundStep !== null) {
        return foundStep
      }
      return getElementWithId(lastStep.false, id)
    }
    // we will return null for now
    // return null

  }
  return null
}

function updateStatesInner({
                             xmlObj,
                             setXml,
                             setJsonStr,
                           }: {
  xmlObj: XMLObj
  setXml: (xml: string) => void
  setJsonStr: (jsonStr: string) => void
}) {
  const newXml = buildXml(xmlObj)
  const newJson3 = convertXmlToJson(newXml)
  setXml(newXml)
  delete (newJson3 as any).id
  stripIds(newJson3.steps)
  setJsonStr(JSON.stringify(newJson3, null, 2))
}
