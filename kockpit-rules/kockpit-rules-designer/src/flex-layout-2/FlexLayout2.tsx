import { Action, Layout, Model, TabNode } from 'flexlayout-react'
import BpmnCanvas from './components/BpmnCanvas'
import JsonEditor from './components/JsonEditor'
import { useLayout, useCurrentRuleId } from './hooks/custom-hooks'
import ProblemsTab from './components/ProblemsTab'
import { Component } from './util/initial-values'
import Properties from './components/Properties'
import './components/flexlayout.css'
import { useEffect } from 'react'
import MermaidFlowChart from './components/MermaidFlowChart'

export default function FlexLayout2() {
  const { currentRuleId } = useCurrentRuleId()
  useEffect(() => {
    if (currentRuleId !== null) {
      return
    }
  }, [currentRuleId])
  return <Inner />
}

// prettier-ignore
const mapComponentToReactNode: Record<Component, () => React.ReactNode> = {
  Canvas           : () => <BpmnCanvas />,
  MermaidFlowChart : () => <MermaidFlowChart />,
  Problems         : () => <ProblemsTab />,
  Properties       : () => <Properties />,
  JsonEditor       : () => <JsonEditor />,
}

function Inner() {
  const { layout, setLayout } = useLayout()
  const factory = (node: TabNode) => {
    const component = node.getComponent() as Component
    const ReactNode = mapComponentToReactNode[component]
    return ReactNode ? <ReactNode /> : null
  }
  const model = Model.fromJson(layout)

  return (
    <Layout
      model={model}
      factory={factory}
      onModelChange={(model: Model, action: Action) => {
        if (
          [
            'FlexLayout_MoveNode',
            'FlexLayout_AdjustWeights',
            'FlexLayout_DeleteTab',
            // "FlexLayout_SetActiveTabset",
            // 'FlexLayout_SelectTab',
          ].includes(action.type)
        ) {
          setLayout(model.toJson())
        }
      }}
    />
  )
}
