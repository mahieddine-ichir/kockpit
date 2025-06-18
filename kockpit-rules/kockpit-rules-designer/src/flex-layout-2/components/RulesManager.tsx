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
import { Trash2 } from 'lucide-react'
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
  return (
      <div className='flex flex-col gap-2 p-2'>
        <div className='flex items-center justify-between'>
          <h2 className='text-sm font-medium text-gray-500'>Rules</h2>
          <RuleAddButton />
        </div>
        <ul className='space-y-2'>
          {rules?.map((rule) => (
              <li key={rule.id}>
                <Button
                    variant={rule.id === currentRuleId ? 'outline' : 'ghost'}
                    className={clsx(
                        'w-full',
                        classes.ruleBtn,
                        rule.id === currentRuleId && 'border hover:bg-white'
                    )}
                    style={{ textAlign: 'left' }}
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
                  <div style={{ textAlign: 'left', width: '100%' }}>
                    {rule.name}
                  </div>
                  <Button
                      variant={'ghost'}
                      size='icon'
                      className={clsx(
                          'h-5',
                          'w-5',
                          rule.id !== currentRuleId && 'hover:bg-white',
                          classes.deleteBtn
                      )}
                      onClick={(e) => {
                        e.stopPropagation()
                        setRuleIdToDelete(rule.id)
                        setIsDialogOpen(true)
                      }}
                  >
                    <Trash2 className='h-3 w-3' />
                  </Button>
                </Button>
              </li>
          ))}
        </ul>
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
