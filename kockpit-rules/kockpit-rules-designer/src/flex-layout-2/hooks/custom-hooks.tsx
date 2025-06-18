import {
  emptyBpmnXml,
  initialDiagramJson,
  initialLayoutJson,
} from '@/flex-layout-2/util/initial-values'
import { IJsonModel } from 'flexlayout-react'
import { useLocalStorage } from 'usehooks-ts'
import { useCallback } from 'react'
import { useStringLocalStorageHistory } from './useStringLocalStorageHistory'
import { convertXmlToJson } from '../util/xml-json-utils/convert-xml-to-json'
import { stripIds } from '../components/BpmnCanvasBase'

const postfix = '0qga4iv'

export function useLayout() {
  const [layout, setLayout] = useLocalStorage<IJsonModel>(
    `layout-${postfix}`,
    initialLayoutJson
  )
  function resetLayout() {
    setLayout(initialLayoutJson)
  }
  return { layout, setLayout, resetLayout }
}

export function useDiagramJson() {
  const [jsonStr, setJsonStr] = useLocalStorage<string>(
    `diagramJson-${postfix}`,
    initialDiagramJson
  )
  return { jsonStr, setJsonStr }
}

export type RuleProperties = {
  name: string
  description: string
}

export function useRuleProperties() {
  const [ruleProperties, setRuleProperties] = useLocalStorage<RuleProperties>(
    `rule_${postfix}`,
    {
      name: '',
      description: '',
    }
  )
  return { ruleProperties, setRuleProperties }
}

export function useCanvasXmlHistory() {
  const { setJsonStr } = useDiagramJson()
  const { value, setValue, history, setHistory, canUndo, canRedo } =
    useStringLocalStorageHistory({
      localStorageKey: `bpmnXmlHistory-${postfix}`,
      initialValue: emptyBpmnXml,
    })

  const undo = useCallback(() => {
    if (history.currentIndex > 0) {
      const newJsonStr = convertXmlToJson(
        history.histArray[history.currentIndex - 1]
      )
      stripIds(newJsonStr.steps)
      setJsonStr(JSON.stringify(newJsonStr, null, 2))
      setHistory((prevHistory) => ({
        ...prevHistory,
        currentIndex: prevHistory.currentIndex - 1,
      }))
    }
  }, [history.currentIndex])

  const redo = useCallback(() => {
    if (history.currentIndex < history.histArray.length - 1) {
      const newJsonStr = convertXmlToJson(
        history.histArray[history.currentIndex + 1]
      )
      stripIds(newJsonStr.steps)
      setJsonStr(JSON.stringify(newJsonStr, null, 2))
      setHistory((prevHistory) => ({
        ...prevHistory,
        currentIndex: prevHistory.currentIndex + 1,
      }))
    }
  }, [history.currentIndex, history.histArray.length])

  return {
    xml: value,
    setXml: setValue,
    undo,
    redo,
    canUndo,
    canRedo,
    history,
    setHistory,
  }
}

export function useRules() {}

export function useCurrentRuleId() {
  const [currentRuleId, setCurrentRuleId] = useLocalStorage<number | null>(
    'rule-id-' + postfix,
    null
  )
  return { currentRuleId, setCurrentRuleId }
}
