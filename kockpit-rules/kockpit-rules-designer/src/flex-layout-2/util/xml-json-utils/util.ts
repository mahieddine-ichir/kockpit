export type BElement = {
  id: string
  name?: string
  description?: string
  type: 'predicate' | 'action'|'ComplexGateway'
}

export type BLink = {
  id: string
  name?: string
  description?: string
  source: string
  target: string
}
export type JSON2 = {
  ruleId: string
  ruleName?: string
  ruleDescription?: string
  ruleClassName?: string
  elements: BElement[]
  links: BLink[]
}

export const s = {
  bpmnProcess: 'bpmn:process',
  bpmnDefinitions: 'bpmn:definitions',
  bpmnTask: 'bpmn:task',
  bpmnStartEvent: 'bpmn:startEvent',
  bpmnExclusiveGateway: 'bpmn:exclusiveGateway',
  bpmnComplexGateway:'bpmn:complexGateway',
  bpmnSequenceFlow: 'bpmn:sequenceFlow',
} as const

const renamePrefMap = {
  Activity: 'action',
  Gateway: 'predicate',
  Flow: 'link',
} as const

export function renameId(id: string): string {
  return id
  // const [prefix, num] = id.split('_') as [keyof typeof renamePrefMap, string]
  // return renamePrefMap[prefix] + '_' + num
}

export const p = {
  id: '@_id',
  sourceRef: '@_sourceRef',
  targetRef: '@_targetRef',
  name: '@_name',
  description: '@_description',
} as const

function createLCG(seed: number) {
  let state = seed
  return function () {
    // Constants for the LCG
    const a = 1664525
    const c = 1013904223
    const m = 2 ** 32
    state = (a * state + c) % m
    return state / m
  }
}

export function createSeededIdGenerator(seed: string) {
  const seedNumber = seed
      .split('')
      .reduce((acc, char) => acc + char.charCodeAt(0), 0)
  const rng = createLCG(seedNumber)

  const characters =
      'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  return function generateIdSuffix() {
    let result = ''
    for (let i = 0; i < 7; i++) {
      const randomIndex = Math.floor(rng() * characters.length)
      result += characters[randomIndex]
    }
    return result
  }
}

// Usage example:
// const generateId = createSeededIdGenerator('your-seed');
// const idSuffix = generateId();
