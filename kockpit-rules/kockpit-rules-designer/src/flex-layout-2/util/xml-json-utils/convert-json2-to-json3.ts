import { assertCannotReach } from '@/lib/utils'
import { BElement, JSON2 } from './util'

export function convertJson2ToJson3(json2: JSON2): JSON3 {
  const steps: Step[] = []
  convertJson2ToJson3Steps(json2, steps)
  const obj: { [k in string]: any } = {
    id: json2.ruleId,
  }
  if (typeof json2['ruleName'] !== 'undefined') {
    obj['name'] = json2.ruleName
  } else {
    obj['name'] = ''
  }
  if (typeof json2['ruleDescription'] !== 'undefined') {
    obj['description'] = json2.ruleDescription
  } else {
    obj['description'] = ''
  }
  if (typeof json2['ruleClassName'] !== 'undefined') {
    obj['className'] = json2.ruleClassName
  }
  obj['steps'] = steps
  return obj as JSON3
}

export function convertJson2ToJson3Steps(json2: JSON2, steps: Step[]): void {
  if (json2.elements.length === 0) {
    return
  }
  if (
      json2.elements.length === 1 &&
      json2.elements[0].type === 'action' &&
      json2.links.length === 0
  ) {
    steps.push(convertElementToStep(json2.elements[0]))
    return
  }

  const firstElementResult: FirstElementResult = getFirstElement(json2)
  if (firstElementResult.type === 'error') {
    return
  }
  const firstStep: Step = firstElementResult.value
  steps.push(firstStep)

  if (firstStep.type === 'action') {
    convertIntermediateJson2ToJson3Steps({
      json2input: json2,
      pathName: null,
      previousStep: firstStep,
      steps,
    })
  } else if (firstStep.type === 'predicate') {
    convertIntermediateJson2ToJson3Steps({
      json2input: json2,
      pathName: 'true',
      previousStep: firstStep,
      steps: (firstStep as StepPredicate).true,
    })
    convertIntermediateJson2ToJson3Steps({
      json2input: json2,
      pathName: 'false',
      previousStep: firstStep,
      steps: (firstStep as StepPredicate).false,
    })
  } else if (firstStep.type === 'ComplexGateway') {
    processComplexGateway(json2, firstStep, steps);
  }
}

function processComplexGateway(json2: JSON2, gateway: StepComplexGateway, steps: Step[]) {
  const outgoingLinks = json2.links.filter(link => link.source === gateway.id);

  outgoingLinks.forEach(link => {
    const targetElement = json2.elements.find(e => e.id === link.target);
    if (!targetElement) return;

    const targetStep = convertElementToStep(targetElement);

    if (link.name) {
      gateway.cases.push({
        id: `case_${gateway.cases.length + 1}`,
        condition: link.name,
        targetId: targetStep.id
      });
    } else {
      gateway.defaultId = targetStep.id;
    }

    const targetSteps: Step[] = [];
    convertIntermediateJson2ToJson3Steps({
      json2input: json2,
      pathName: null,
      previousStep: targetStep,
      steps: targetSteps
    });

    if (targetSteps.length > 0) {
      steps.push(targetStep, ...targetSteps);
    } else {
      steps.push(targetStep);
    }
  });
}



type StepAction = {
  type: 'action'
  id: string
  name?: string
  description?: string
}

type StepComplexGateway = {
  type: 'ComplexGateway';
  id: string;
  name?: string;
  description?: string;
  cases: Array<{
    id: string;
    condition: string;
    targetId: string;
  }>;
  defaultId?: string;
};

export type StepPredicate = {
  type: 'predicate'
  id: string
  name?: string
  description?: string
  true: Array<StepPredicate | StepAction>
  false: Array<StepPredicate | StepAction>
}

export type StepPredicateWoId = Omit<StepPredicate, 'id'>
export type StepActionWoId = Omit<StepAction, 'id'>

export type Step = StepPredicate | StepAction |StepComplexGateway

export type StepWoId = StepPredicateWoId | StepActionWoId

export type JSON3 = {
  id?: string
  name?: string
  description?: string
  className?: string
  steps: Step[]
}

export type Json3WoIds = {
  id?: string
  name?: string
  description?: string
  className?: string
  steps: StepWoId[]
}

export type RichJSON3 = {
  id: string
  name?: string
  description?: string
  className?: string
  steps: Step[]
}

type FirstElementResult =
    | {
  type: 'success'
  value: Step
}
    | {
  type: 'error'
  value: 'zero first elements' | 'more than one first elements'
}

/**
 * Find in 'source' attributes `id` that doesn't exist in any
 * `target` attributers.
 * Possible errors:
 *  - no first element (zero, e.g. cycle, empty diagram, ...).
 *  - Multiple first elements
 */
function getFirstElement(json2: JSON2): FirstElementResult {
  const firstElementsIds: string[] = []
  const uniqueSourcesIds = Array.from(
      new Set(json2.links.map((link) => link.source))
  )
  const uniqueTargetsIds = Array.from(
      new Set(json2.links.map((link) => link.target))
  )

  for (const sourceId of uniqueSourcesIds) {
    if (!uniqueTargetsIds.includes(sourceId)) {
      firstElementsIds.push(sourceId)
    }
  }
  if (firstElementsIds.length === 0) {
    return { type: 'error', value: 'zero first elements' }
  }
  if (firstElementsIds.length > 1) {
    return { type: 'error', value: 'more than one first elements' }
  }
  return {
    type: 'success',
    value: convertElementToStep(
        json2.elements.find(
            (element) => element.id === firstElementsIds[0]
        ) as BElement
    ),
  }
}

function convertElementToStep(element: BElement): Step {
  if (element.type === 'action') {
    const step: Step = {
      type: 'action',
      id: element.id,
    }
    if ('name' in element) {
      step['name'] = element.name
    }
    if ('description' in element) {
      step['description'] = element.description
    }
    return step
  }
  if (element.type === 'predicate') {
    const step: Step = {
      type: 'predicate',
      id: element.id,
      ...('name' in element ? { name: element.name } : {}),
      ...('description' in element ? { description: element.description } : {}),
      true: [],
      false: [],
    }
    return step
  }
  if (element.type === 'ComplexGateway') {
    const step: StepComplexGateway = {
      type: 'ComplexGateway',
      id: element.id,
      ...('name' in element && { name: element.name }),
      ...('description' in element && { description: element.description }),
      cases: [],
    }
    return step
  }
  assertCannotReach(element.type)
  /** This line below is here only to satisfy typescript */
  throw new Error()
}

function convertIntermediateJson2ToJson3Steps({
                                                json2input,
                                                pathName,
                                                previousStep,
                                                steps,
                                              }: {
  json2input: JSON2
  previousStep: Step
  pathName: String | null
  steps: Step[]
}) {
  const linksForPreviousStep =
      pathName === null
          ? json2input.links.filter((link) => link.source === previousStep.id)
          : json2input.links.filter(
              (link) => link.source === previousStep.id && link.name === pathName
          )
  if (previousStep.type === 'ComplexGateway') {

    return;
  }
  if (linksForPreviousStep.length === 0 || linksForPreviousStep.length > 1) {
    return
  }
  const nextStep: Step = convertElementToStep(
      json2input.elements.find(
          (element) => element.id === linksForPreviousStep[0].target
      ) as BElement
  )
  steps.push(nextStep)
  if (nextStep.type === 'action') {
    convertIntermediateJson2ToJson3Steps({
      json2input,
      pathName: null,
      previousStep: nextStep,
      steps,
    })
  } else if (nextStep.type === 'predicate') {
    convertIntermediateJson2ToJson3Steps({
      json2input,
      pathName: 'true',
      previousStep: nextStep,
      steps: (nextStep as StepPredicate).true,
    })
    convertIntermediateJson2ToJson3Steps({
      json2input,
      pathName: 'false',
      previousStep: nextStep,
      steps: (nextStep as StepPredicate).false,
    })
  } else if (nextStep.type === 'ComplexGateway') {
    processComplexGateway(json2input, nextStep as StepComplexGateway, steps);
  }
}
