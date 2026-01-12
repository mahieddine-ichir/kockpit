import { Action, Layout, Model, TabNode } from 'flexlayout-react'
import BpmnCanvas from './components/BpmnCanvas'
import JsonEditor from './components/JsonEditor'
import { useLayout, useCurrentRuleId } from './hooks/custom-hooks'
import ProblemsTab from './components/ProblemsTab'
import { Component } from './util/initial-values'
import Properties from './components/Properties'
import './components/flexlayout.css'
import { useEffect, useState } from 'react'
import MermaidFlowChart from './components/MermaidFlowChart'
import { ChevronRight, ChevronLeft } from 'lucide-react'

export default function FlexLayout2() {
  const { currentRuleId } = useCurrentRuleId()
  const [isCollapsed, setIsCollapsed] = useState(false)

  useEffect(() => {
    if (currentRuleId !== null) {
      return
    }
  }, [currentRuleId])

  return (
    <div className='relative h-full w-full'>
      <Inner />
      <div
        className={`absolute top-0 right-0 h-full bg-slate-800 border-l border-slate-700 shadow-2xl z-50 rounded-lg overflow-hidden transition-all duration-300 ${
          isCollapsed ? 'w-12' : 'w-64'
        }`}
      >
        <div className='relative h-full'>
          <button
            onClick={() => setIsCollapsed(!isCollapsed)}
            className='absolute top-4 right-2 z-10 p-1.5 rounded-md bg-slate-700 hover:bg-slate-600 text-slate-300 hover:text-white transition-colors'
            title={isCollapsed ? 'Expand Properties' : 'Collapse Properties'}
          >
            {isCollapsed ? (
              <ChevronLeft className='h-4 w-4' />
            ) : (
              <ChevronRight className='h-4 w-4' />
            )}
          </button>
          {!isCollapsed && <Properties />}
        </div>
      </div>
    </div>
  )
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

  // Remove old Properties panel from saved layouts
  const cleanedLayout = removePropertiesFromLayout(layout)
  const model = Model.fromJson(cleanedLayout)

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

// Remove Properties and Problems panels from layout if they exist
function removePropertiesFromLayout(layout: any): any {
  const newLayout = JSON.parse(JSON.stringify(layout)) // Deep clone

  function removeOldPanelsRecursive(node: any): any {
    if (!node) return node

    // If this is a tabset, filter out Properties and Problems tabs
    if (node.type === 'tabset' && node.children) {
      node.children = node.children.filter((child: any) =>
        child.component !== 'Properties' && child.component !== 'Problems'
      )
    }

    // If this is a row, filter out empty tabsets and recurse
    if (node.type === 'row' && node.children) {
      node.children = node.children
        .map(removeOldPanelsRecursive)
        .filter((child: any) => {
          // Remove tabsets that have no children
          if (child.type === 'tabset') {
            return child.children && child.children.length > 0
          }
          return true
        })
    }

    return node
  }

  if (newLayout.layout) {
    newLayout.layout = removeOldPanelsRecursive(newLayout.layout)
  }

  return newLayout
}
