import { useAtom } from 'jotai'
import { atomWithReducer } from 'jotai/utils'
import Problems from './base/Problems/Problems'

export type ProblemComponent = 'JsonEditor' | 'BpmnCanvas'
type ProblemName =
  | 'Invalid JSON'
  | 'Invalid Diagram JSON'
  | 'No Start Event'
  | 'No End Event'
  | 'Transition has the same source and target'
type ProblemType = 'error' | 'warning'

export const problemsInitialState = [
  {
    component: 'JsonEditor',
    name: 'Invalid JSON',
    type: 'error',
    isActive: false as boolean,
    message: '' as string,
  },
  {
    component: 'BpmnCanvas',
    name: 'Invalid Diagram JSON',
    type: 'error',
    isActive: false as boolean,
    message: '' as string,
  },
  {
    component: 'BpmnCanvas',
    name: 'No Start Event',
    type: 'warning',
    isActive: false as boolean,
    message: '' as string,
  },
  {
    component: 'BpmnCanvas',
    name: 'No End Event',
    type: 'warning',
    isActive: false as boolean,
    message: '' as string,
  },
  {
    component: 'BpmnCanvas',
    name: 'Transition has the same source and target',
    type: 'error',
    isActive: false as boolean,
    message: '' as string,
  },
] as const satisfies Array<{
  component: ProblemComponent
  name: ProblemName
  type: ProblemType
  isActive: boolean
  message: string
}>

export type Problems = typeof problemsInitialState
export type Problem = Problems[number]

const problemsReducer = (state: Problems, action: Problem) => {
  return state.map((problem) =>
    problem.name === action.name
      ? { ...problem, isActive: action.isActive, message: action.message }
      : problem
  )
}

const problemsAtom = atomWithReducer<Problems, Problem>(
  problemsInitialState,
  // @ts-expect-error
  problemsReducer
)

export function useProblems() {
  const [problems, dispatch] = useAtom(problemsAtom)
  function clearProblems(component: ProblemComponent) {
    for (const problem of problems) {
      if (problem.component === component) {
        dispatch({ ...problem, isActive: false, message: '' })
      }
    }
  }
  return { problems, dispatch, clearProblems }
}

export type ProblemsDispatch = ReturnType<typeof useProblems>['dispatch']

export default function ProblemsTab() {
  const { problems } = useProblems()

  return <Problems problems={problems} />
}
