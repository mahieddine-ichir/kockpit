import React from 'react'
import {
  Network,
  Redo2,
  Undo2,
  RotateCcw,
  Download,
  LucideProps,
} from 'lucide-react'
import CollapsibleToolbar, { MenuItem } from '../CollapsibleToolbar'
import { useLocalStorage } from 'usehooks-ts'
import Modeler from 'bpmn-js/lib/Modeler'
// @ts-expect-error
import { layoutProcess } from '@marstamm/bpmn-auto-layout'
import {
  emptyBpmnXml,
  initialDiagramJson,
} from '@/flex-layout-2/util/initial-values'
import {
  useCanvasXmlHistory,
  useDiagramJson,
} from '@/flex-layout-2/hooks/custom-hooks'

const AutoLayout = React.forwardRef<SVGSVGElement, Omit<LucideProps, 'ref'>>(
  (props, ref) => {
    return (
      <Network {...props} ref={ref} style={{ transform: 'rotate(-90deg)' }} />
    )
  }
)

export default function CanvasToolbar({
  modelerRef,
}: {
  modelerRef: React.MutableRefObject<Modeler<null> | null>
}) {
  const [isExpanded, setIsExpanded] = useLocalStorage(
    'canvasToolbarExpanded',
    true
  )
  const { xml, setXml, undo, redo } = useCanvasXmlHistory()
  const { setJsonStr } = useDiagramJson()
  const menuItems = createMenuItems({
    xml,
    setXml,
    setJsonStr,
    undo,
    redo,
  })

  const handleToggle = () => {
    setIsExpanded(!isExpanded)
  }

  return (
    <CollapsibleToolbar
      isExpanded={isExpanded}
      menuItems={menuItems}
      handleToggle={handleToggle}
      modelerRef={modelerRef}
    />
  )
}

function createMenuItems({
  xml,
  setXml,
  setJsonStr,
  undo,
  redo,
}: {
  xml: string
  setXml: (newValue: string) => void
  setJsonStr: (newValue: string) => void
  undo: () => void
  redo: () => void
}) {
  const menuItems: MenuItem[] = [
    {
      icon: Undo2,
      label: 'Undo',
      tooltip: 'Undo',
      eventHandler: undo,
    },
    {
      icon: Redo2,
      label: 'Redo',
      tooltip: 'Redo',
      eventHandler: redo,
    },
    {
      icon: AutoLayout,
      label: 'Auto Layout',
      tooltip: 'Auto Layout',
      eventHandler: getAutoLayoutHandler({ xml, setXml }),
    },
    {
      type: 'export-button',
      icon: Download,
      label: 'Export',
      tooltip: 'Export',
      eventHandler: getExportHandler(),
    },
    {
      icon: RotateCcw,
      label: 'Reset',
      tooltip: 'Reset',
      eventHandler: getResetHandler({ setXml, setJsonStr }),
    },
  ]
  return menuItems
}

function getExportHandler() {
  return () => {}
}

function getAutoLayoutHandler({
  xml,
  setXml,
}: {
  xml: string
  setXml: (newValue: string) => void
}) {
  return async () => {
    try {
      const layouted = await layoutProcess(xml)
      setXml(layouted)
    } catch (err) {
      console.error('Error auto-layouting the diagram:', err)
    }
  }
}

function getResetHandler({
  setXml,
  setJsonStr,
}: {
  setXml: (newValue: string) => void
  setJsonStr: (newValue: string) => void
}) {
  return () => {
    setJsonStr(initialDiagramJson)
    setXml(emptyBpmnXml)
  }
}
