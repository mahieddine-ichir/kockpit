import { z } from 'zod'
import { createSeededIdGenerator } from '../util/xml-json-utils/util'

const NeiStepTypeSchema = z.enum(['action', 'predicate'])

type NeiStepType = z.infer<typeof NeiStepTypeSchema>

/** Nei stands for Next Existing Id */
const NeiStepActionSchema = z
  .object({
    id: z.string().optional(),
    type: z.literal('action' satisfies NeiStepType),
    name: z.string().optional(),
    description: z.string().optional(),
  })
  .strict()

type NeiStepPredicate = {
  id?: string
  type: 'predicate'
  name?: string
  description?: string
  true: (NeiStepPredicate | NeiStepAction)[]
  false: (NeiStepPredicate | NeiStepAction)[]
}

export type NeiStep = NeiStepPredicate | NeiStepAction

const NeiStepPredicateSchema: z.ZodType<NeiStepPredicate> = z.lazy(() =>
  z
    .object({
      id: z.string().optional(),
      type: z.literal('predicate' satisfies NeiStepType),
      name: z.string().optional(),
      description: z.string().optional(),
      true: z.array(z.union([NeiStepActionSchema, NeiStepPredicateSchema])),
      false: z.array(z.union([NeiStepActionSchema, NeiStepPredicateSchema])),
    })
    .strict()
)

type NeiStepAction = z.infer<typeof NeiStepActionSchema>

const NeiStepSchema = z.union([NeiStepActionSchema, NeiStepPredicateSchema])

const NeiDiagramJsonSchema = z
  .object({
    name: z.string(),
    description: z.string().optional(),
    className: z.string().optional(),
    steps: z.array(NeiStepSchema),
  })
  .strict()

export type NeiDiagramJson = z.infer<typeof NeiDiagramJsonSchema>

export function isValidNeiDiagramJson(obj: {
  [key: string]: any
}):
  | { isValid: true; errorMessage: null }
  | { isValid: false; errorMessage: string } {
  try {
    NeiDiagramJsonSchema.parse(obj)
    return { isValid: true, errorMessage: null }
  } catch (e) {
    if (e instanceof Error) {
      return { isValid: false, errorMessage: e.message }
    }
    return { isValid: false, errorMessage: 'Unknown error occurred' }
  }
}

export function addMissingIds(steps: NeiStep[], gen: () => string) {
  for (const step of steps) {
    if (step.id === undefined) {
      step['id'] = (step.type === 'action' ? 'Action_' : 'Predicate_') + gen()
    }
  }
  if (steps.length === 0) {
    return
  }
  if (steps.at(-1)?.type === 'action') {
    return
  }
  const lastPredicate = steps.at(-1) as NeiStepPredicate
  addMissingIds(lastPredicate.true, gen)
  addMissingIds(lastPredicate.false, gen)
}
