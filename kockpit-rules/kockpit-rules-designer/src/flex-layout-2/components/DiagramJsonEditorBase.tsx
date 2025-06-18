import { json } from '@codemirror/lang-json'
import ReactCodeMirror, { ReactCodeMirrorRef } from '@uiw/react-codemirror'
import clsx from 'clsx'
import React from 'react'
import { ProblemsDispatch, useProblems } from './ProblemsTab'
import { z } from 'zod'
import { convertJsonToXml } from '@/flex-layout-2/components/convert-json-to-xml/convert-json-to-xml'
import { JSON3 } from '../util/xml-json-utils/convert-json2-to-json3'
import { useCanvasXmlHistory } from '../hooks/custom-hooks'
import { useDebounceCallback } from 'usehooks-ts'
import {
  addMissingIds,
  isValidNeiDiagramJson,
  NeiDiagramJson,
  NeiStep,
} from './handle-non-existing-ids'
import {
  elementTypeAtom,
  elementNameAtom,
  elementDescriptionAtom,
  ElementType,
  elementIdAtom,
} from '../atoms'
import { useAtom, useAtomValue } from 'jotai'
import { createSeededIdGenerator } from '../util/xml-json-utils/util'

export default function DiagramJsonEditorBase({
                                                jsonStr,
                                                setJsonStr,
                                                editorRef,
                                              }: {
  jsonStr: string
  setJsonStr: (jsonStr: string) => void
  editorRef: React.RefObject<ReactCodeMirrorRef>
}) {
  const elementType = useAtomValue(elementTypeAtom)
  const elementId = useAtomValue(elementIdAtom)
  const [elementName, setElementName] = useAtom(elementNameAtom)
  const [elementDescription, setElementDescription] = useAtom(
      elementDescriptionAtom
  )
  const { setXml } = useCanvasXmlHistory()
  const { dispatch } = useProblems()
  const onChange = useDebounceCallback(
      getOnChangeHandler({
        jsonStr,
        setJsonStr,
        elementType,
        elementName,
        elementId,
        setElementName,
        elementDescription,
        setElementDescription,
        dispatch,
        setXml,
      }),
      500
  )

  return (
      <ReactCodeMirror
          ref={editorRef}
          // TODO: calculate `error` from `problems`
          // className={clsx('h-full rounded border', error && 'border-red-400')}
          className={clsx('h-full rounded border')}
          value={jsonStr}
          height='100%'
          extensions={[json()]}
          onChange={onChange}
          theme={'light'}
      />
  )
}

export const validateAndConvertJsonToXml = async ({
                                                    NewJsonStr,
                                                    elementType,
                                                    elementName,
                                                    elementId,
                                                    setElementName,
                                                    elementDescription,
                                                    setElementDescription,
                                                    dispatch,
                                                  }: {
  elementType: ElementType
  elementName: string
  elementId: string
  setElementName: (elementName: string) => void
  elementDescription: string
  setElementDescription: (elementDescription: string) => void
  NewJsonStr: string
  dispatch: ProblemsDispatch
}) => {
  const { isValidJson, obj, errorMessage } = validateJson(NewJsonStr)
  if (!isValidJson) {
    dispatch({
      component: 'JsonEditor',
      name: 'Invalid JSON',
      isActive: true,
      message: errorMessage,
      type: 'error',
    })
    return null
  }
  dispatch({
    component: 'JsonEditor',
    name: 'Invalid JSON',
    isActive: false,
    message: '',
    type: 'error',
  })
  const { isValid, errorMessage: validDiagramJsonErrorMessage } =
      isValidDiagramJson(obj)

  const { isValid: isValidNei } = isValidNeiDiagramJson(obj)

  if (!isValid && !isValidNei) {
    dispatch({
      component: 'BpmnCanvas',
      name: 'Invalid Diagram JSON',
      isActive: true,
      message: validDiagramJsonErrorMessage,
      type: 'error',
    })
    return null
  }

  if (!isValid && isValidNei) {
    const gen = createSeededIdGenerator('1234')
    addMissingIds(obj.steps as NeiStep[], gen)
  }

  dispatch({
    component: 'BpmnCanvas',
    name: 'Invalid Diagram JSON',
    isActive: false,
    message: '',
    type: 'error',
  })
  if (elementType === 'Rule') {
    if (elementName !== obj.name) {
      setElementName(obj.name ?? '')
    }
    if (elementDescription !== obj.description) {
      setElementDescription(obj.description ?? '')
    }
  } else if (elementType === 'Action' || elementType === 'Predicate' || elementType === 'ComplexGateway') {
    const step = getStepById(obj.steps, elementId)
    if (step !== null) {
      if (step.name !== elementName) {
        setElementName(step.name ?? '')
      }
      if (step.description !== elementDescription) {
        setElementDescription(step.description ?? '')
      }
    }
  }
  const res = await convertJsonToXml(obj as JSON3)
  return res
}

function validateJson(
    jsonStr: string
):
    | { isValidJson: true; obj: { [key: string]: any }; errorMessage: null }
    | { isValidJson: false; obj: null; errorMessage: string } {
  try {
    const obj = JSON.parse(jsonStr)
    return { isValidJson: true, obj, errorMessage: null }
  } catch (e) {
    if (e instanceof Error) {
      // return { isValidJson: false, obj: null, errorMessage: e.message }
      return {
        isValidJson: false,
        obj: null,
        errorMessage: JSON.stringify(e.message),
      }
    }
    return {
      isValidJson: false,
      obj: null,
      errorMessage: 'Unknown error occurred',
    }
  }
}

const StepTypeSchema = z.enum(['action', 'predicate','ComplexGateway'])

type StepType = z.infer<typeof StepTypeSchema>

const StepActionSchema = z
    .object({
      id: z.string(),
      type: z.literal('action' satisfies StepType),
      name: z.string().optional(),
      description: z.string().optional(),
    })
    .strict()



// type StepPredicate = {
//   id: string
//   type: 'predicate'
//   name?: string
//   description?: string
//   true: (StepPredicate | StepAction)[]
//   false: (StepPredicate | StepAction)[]
// }

type StepPredicate = {
  id: string;
  type: 'predicate';
  name?: string;
  description?: string;
  true: IntermediateStep[];
  false: IntermediateStep[];
};

type Step = StepPredicate | StepAction | z.infer<typeof StepComplexGatewaySchema>

// const StepPredicateSchema: z.ZodType<StepPredicate> = z.lazy(() =>
//   z.object({
//       id: z.string(),
//       type: z.literal('predicate' satisfies StepType),
//       name: z.string().optional(),
//       description: z.string().optional(),
//       true: z.array(z.union([StepActionSchema, StepPredicateSchema])),
//       false: z.array(z.union([StepActionSchema, StepPredicateSchema])),
//     })
//     .strict()
// )

type IntermediateStep = StepAction | StepPredicate | z.infer<typeof StepComplexGatewaySchema>;

const StepPredicateSchema: z.ZodType<StepPredicate> = z.lazy(() =>
    z.object({
      id: z.string(),
      type: z.literal('predicate'),
      name: z.string().optional(),
      description: z.string().optional(),
      true: z.array(z.union([
        StepActionSchema,
        StepPredicateSchema,
        StepComplexGatewaySchema
      ] as const)),
      false: z.array(z.union([
        StepActionSchema,
        StepPredicateSchema,
        StepComplexGatewaySchema
      ] as const)),
    }).strict()
);


// const StepPredicateSchema: z.ZodType<StepPredicate> = z.lazy(() =>
//   z.object({
//     id: z.string(),
//     type: z.literal('predicate'),
//     name: z.string().optional(),
//     description: z.string().optional(),
//     true: z.array(z.union([StepActionSchema, StepPredicateSchema, StepComplexGatewaySchema])),
//     false: z.array(z.union([StepActionSchema, StepPredicateSchema, StepComplexGatewaySchema])),
//   }).strict()
// );

type StepAction = z.infer<typeof StepActionSchema>

const CaseSchema = z.object({
  id: z.string(),
  condition: z.string(),
  targetId: z.string()
})
const StepComplexGatewaySchema = z.object({
  id: z.string(),
  type: z.literal('ComplexGateway'),
  name: z.string().optional(),
  description: z.string().optional(),
  cases: z.array(CaseSchema),
  defaultId: z.string().optional()
}).strict()

const StepSchema = z.union([StepActionSchema, StepPredicateSchema, StepComplexGatewaySchema])

const DiagramJsonSchema = z
    .object({
      id: z.string(),
      name: z.string(),
      className: z.string().optional(),
      description: z.string().optional(),
      steps: z.array(StepSchema),
    })
    .strict()



function isValidDiagramJson(obj: any): { isValid: boolean; errorMessage: string } {
  try {

    if (!obj || typeof obj !== 'object') {
      return { isValid: false, errorMessage: 'Invalid JSON structure' };
    }


    const gen = createSeededIdGenerator('1234');
    if (!obj.id) obj.id = `Process_${gen()}`;

    if (obj.steps) {
      const normalizedSteps = obj.steps.map((step: any) => {
        if (!step.id) step.id = `Step_${gen()}`;

        if (step.type === 'predicate') {
          return {
            ...step,
            true: step.true?.map((s: any) => ({
              ...s,
              id: s.id || `Step_${gen()}`,
            })) || [],
            false: step.false?.map((s: any) => ({
              ...s,
              id: s.id || `Step_${gen()}`,
            })) || [],
          };
        }

        if (step.type === 'ComplexGateway') {
          return {
            ...step,
            cases: step.cases?.map((c: any) => ({
              ...c,
              id: c.id || `Case_${gen()}`,
              targetId: c.targetId || `Target_${gen()}`,
            })) || [],
          };
        }

        return step;
      });

      obj.steps = normalizedSteps;
    }

    DiagramJsonSchema.parse(obj);
    return { isValid: true, errorMessage: '' };
  } catch (e) {
    if (e instanceof z.ZodError) {
      return {
        isValid: false,
        errorMessage: e.errors.map(err =>
            `${err.path.join('.')}: ${err.message}`
        ).join('\n')
      };
    }
    return { isValid: false, errorMessage: 'Unknown validation error' };
  }
}

function getOnChangeHandler({
                              jsonStr,
                              setJsonStr,
                              elementType,
                              elementName,
                              elementId,
                              setElementName,
                              elementDescription,
                              setElementDescription,
                              dispatch,
                              setXml,
                            }: {
  jsonStr: string
  setJsonStr: (value: string) => void
  elementType: ElementType
  elementName: string
  elementId: string
  setElementName: (elementName: string) => void
  elementDescription: string
  setElementDescription: (elementDescription: string) => void
  dispatch: ProblemsDispatch
  setXml: (xml: string) => void
}) {
  return async (val: string) => {
    const oldJsonStr = jsonStr
    if (isJsonEquivalent(oldJsonStr, val)) {
      return
    }
    setJsonStr(val)

    const res = await validateAndConvertJsonToXml({
      NewJsonStr: val,
      elementType,
      elementName,
      elementId,
      setElementName,
      elementDescription,
      setElementDescription,
      dispatch,
    })
    if (res !== null) {
      setXml(res)
    }
  }
}

function isJsonEquivalent(oldJsonStr: string, newJsonStr: string) {
  try {
    const isEquivalent =
        JSON.stringify(JSON.parse(oldJsonStr.trim())) ===
        JSON.stringify(JSON.parse(newJsonStr.trim()))
    return isEquivalent
  } catch {
    return false
  }
}

function getStepById(steps: Step[], id: string): Step | null {
  for (const step of steps) {
    if (step.id === id) {
      return step
    }
  }
  if (steps.length === 0) {
    return null
  }
  const lastStep = steps.at(-1) as Step
  if (lastStep.type === 'action') {
    return null
  }
  if (lastStep.type === 'predicate') {
    const foundStep = getStepById(lastStep.true, id)
    if (foundStep !== null) {
      return foundStep
    }
    return getStepById(lastStep.false, id)
  }
  return null
}
