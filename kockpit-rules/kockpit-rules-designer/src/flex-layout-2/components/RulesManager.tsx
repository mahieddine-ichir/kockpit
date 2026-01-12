import { db } from '../db'
import { StringHistory } from '../hooks/useStringLocalStorageHistory'
import { useLiveQuery } from 'dexie-react-hooks'
import {
  useCanvasXmlHistory,
  useCurrentRuleId,
  useDiagramJson,
  useLayout,
} from '../hooks/custom-hooks'
import {
  elementTypeAtom,
  elementNameAtom,
  elementDescriptionAtom,
  enableTabSetStripAtom,
} from '../atoms'
import { useAtom, useSetAtom } from 'jotai'
import { Actions, IJsonModel, Model } from 'flexlayout-react'
import { Switch } from '@/components/ui/switch'
import { RuleAddButton } from '@/components/ui-2/RuleAddButton/RuleAddButton'
import clsx from 'clsx'
import { Trash2, ChevronDown, ChevronRight } from 'lucide-react'
import classes from './RulesManager.module.css'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Label } from '@/components/ui/label'
import { useState } from 'react'

export default function RulesManager() {
  const { layout, setLayout } = useLayout()
  const [enableTabSetStrip, setEnableTabSetStrip] = useAtom(
      enableTabSetStripAtom
  )
  return (
      <div className='absolute inset-0 flex flex-col justify-between bg-slate-50 p-2'>
        <RulesList />
        <div className='mx-2 mb-2 flex items-center justify-between'>
          <Label htmlFor='toggle-tab-header'>Toggle Tab Header</Label>
          <Switch
              id='toggle-tab-header'
              checked={enableTabSetStrip}
              onCheckedChange={
                // (value) => setEnabled(value)
                (value) =>
                    toggleTabHeader({
                      layout,
                      setLayout,
                      setEnableTabSetStrip,
                      value,
                    })
              }
          />
        </div>
      </div>
  )
}

export function RulesList() {
  const { jsonStr, setJsonStr } = useDiagramJson()
  const { history, setHistory } = useCanvasXmlHistory()
  const { layout, setLayout } = useLayout()
  const { currentRuleId, setCurrentRuleId } = useCurrentRuleId()
  const setElementType = useSetAtom(elementTypeAtom)
  const setElementName = useSetAtom(elementNameAtom)
  const setElementDescription = useSetAtom(elementDescriptionAtom)
  const rules = useLiveQuery(() => db.rules.toArray())
  const [ruleIdToDelete, setRuleIdToDelete] = useState<number | null>(null)
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [isExpanded, setIsExpanded] = useState(true)

  return (
      <div className='py-4'>
        <div
          className='px-6 py-2 text-xs font-semibold text-slate-400 uppercase tracking-wider sticky top-0 bg-slate-800/90 z-10 flex items-center justify-between cursor-pointer hover:text-slate-300 transition-colors'
          onClick={() => setIsExpanded(!isExpanded)}
        >
          <span>Rules</span>
          <div className='ml-auto'>
            {isExpanded ? (
              <ChevronDown className='h-4 w-4' />
            ) : (
              <ChevronRight className='h-4 w-4' />
            )}
          </div>
        </div>
        {isExpanded && (
          <nav className='space-y-1 px-2'>
            <div className='px-3 py-2'>
              <RuleAddButton />
            </div>
            {rules?.map((rule) => (
              <div
                  key={rule.id}
                  className={clsx(
                      'flex items-center px-3 py-2.5 rounded-lg cursor-pointer transition-all duration-200 relative',
                      classes.ruleItem,
                      rule.id === currentRuleId
                          ? 'bg-blue-600/20 text-blue-400 border-l-4 border-blue-500 shadow-md'
                          : 'text-slate-300 hover:bg-slate-700 hover:text-white border-l-4 border-transparent'
                  )}
                  style={{ minHeight: '44px' }}
                  onClick={async () => {
                    // Update the current rule in the database
                    if (currentRuleId !== null) {
                      const currentRule = rules?.find((r) => r.id === currentRuleId)
                      if (currentRule) {
                        // @ts-ignore TODO: fix later
                        await db.rules.update(currentRuleId, {
                          jsonStr: jsonStr,
                          xmlHistory: history,
                          layout: layout,
                        })
                      }
                    }

                    // Switch to the new rule
                    setCurrentRuleId(rule.id)
                    setJsonStr(rule.jsonStr)
                    setHistory(rule.xmlHistory)
                    setLayout(rule.layout)
                    setElementType('Rule')
                    setElementName(rule.name)
                    setElementDescription(rule.description)
                  }}
              >
                <span className='flex-1 text-sm font-medium'>{rule.name}</span>
                <button
                    className={clsx(
                        'p-1.5 rounded-md hover:bg-slate-600 transition-all',
                        classes.deleteBtn
                    )}
                    onClick={(e) => {
                      e.stopPropagation()
                      setRuleIdToDelete(rule.id)
                      setIsDialogOpen(true)
                    }}
                >
                  <Trash2 className='h-4 w-4 text-slate-400 hover:text-red-400' />
                </button>
              </div>
            ))}
          </nav>
        )}
        <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Confirm Deletion</DialogTitle>
            </DialogHeader>
            <DialogDescription>
              Are you sure you want to delete this rule? This action cannot be
              undone.
            </DialogDescription>
            <DialogFooter>
              <Button variant='ghost' onClick={() => setIsDialogOpen(false)}>
                Cancel
              </Button>
              <Button
                  variant='destructive'
                  onClick={async () => {
                    if (ruleIdToDelete) {
                      await db.rules.delete(ruleIdToDelete)
                      setIsDialogOpen(false)
                    }
                  }}
              >
                Delete
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>
  )
}

export function createEmptyXmlHistory({
                                        ruleName,
                                        ruleDescription,
                                      }: {
  ruleName: string
  ruleDescription: string
}): StringHistory {
  const ruleId = `Rule_${generateRandomSuffix()}`
  const xmlStr = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="${ruleId}" name="${ruleName}" description="${ruleDescription}" isExecutable="false" />
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="${ruleId}" />
  </bpmndi:BPMNDiagram>
</bpmn:definitions>
`
  return {
    currentIndex: 0,
    histArray: [xmlStr],
  }
}

export function createEmptyJsonStr({
                                     ruleName,
                                     ruleDescription,
                                   }: {
  ruleName: string
  ruleDescription: string
}) {
  return `{
  "name": "${ruleName}",
  "description": "${ruleDescription}",
  "steps": []
}`
}

function generateRandomSuffix() {
  return Math.random().toString(36).substring(2, 9)
}

function toggleTabHeader({
                           layout,
                           setLayout,
                           setEnableTabSetStrip,
                           value,
                         }: {
  layout: IJsonModel
  setLayout: (layout: IJsonModel) => void
  setEnableTabSetStrip: (enabled: boolean) => void
  value: boolean
}) {
  setEnableTabSetStrip(value)
  const model = Model.fromJson(layout)
  model.doAction(Actions.updateModelAttributes({ tabSetEnableTabStrip: value }))
  setLayout(model.toJson())
}
