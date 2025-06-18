import CollapsibleToolbar from '../CollapsibleToolbar'
import { useLocalStorage } from 'usehooks-ts'
import { MenuItem } from '../CollapsibleToolbar'
import {
  FileText,
  Download,
  Copy,
  Redo2,
  Undo2,
  RotateCcw,
  Upload,
} from 'lucide-react'
import { toast } from 'sonner'

import { ReactCodeMirrorRef } from '@uiw/react-codemirror'
import { redo, undo } from '@codemirror/commands'
import { emptyBpmnXml, initialDiagramJson } from '../../util/initial-values'
import { useCanvasXmlHistory } from '@/flex-layout-2/hooks/custom-hooks'

export default function DiagramJsonEditorToolbar({
  editorRef,
}: {
  editorRef: React.RefObject<ReactCodeMirrorRef>
}) {
  const { setXml } = useCanvasXmlHistory()
  const [isExpanded, setIsExpanded] = useLocalStorage(
    'diagramJsonEditorToolbarExpanded',
    true
  )
  const handleToggle = () => {
    setIsExpanded(!isExpanded)
  }
  const menuItems = createMenuItems({ editorRef, setXml })
  return (
    <CollapsibleToolbar
      modelerRef={null}
      isExpanded={isExpanded}
      handleToggle={handleToggle}
      menuItems={menuItems}
    />
  )
}
function createMenuItems({
  editorRef,
  setXml,
}: {
  editorRef: React.RefObject<ReactCodeMirrorRef>
  setXml: (value: string) => void
}) {
  const menuItems: MenuItem[] = [
    {
      icon: Undo2,
      label: 'Undo',
      tooltip: 'Undo',
      eventHandler: getUndoHandler(editorRef),
    },
    {
      icon: Redo2,
      label: 'Redo',
      tooltip: 'Redo',
      eventHandler: getRedoHandler(editorRef),
    },
    {
      icon: Upload,
      label: 'Upload',
      tooltip: 'Upload',
      eventHandler: getUploadHandler(editorRef),
    },
    {
      icon: Copy,
      label: 'Copy',
      tooltip: 'Copy',
      eventHandler: getCopyHandler(editorRef),
    },
    {
      icon: Download,
      label: 'Download',
      tooltip: 'Download',
      eventHandler: getDownloadHandler(editorRef),
    },
    {
      icon: FileText,
      label: 'Format',
      tooltip: 'Format',
      eventHandler: getFormatHandler(editorRef),
    },
    {
      icon: RotateCcw,
      label: 'Reset',
      tooltip: 'Reset',
      eventHandler: getResetHandler({ editorRef, setXml }),
    },
  ]
  return menuItems
}

function getUndoHandler(editorRef: React.RefObject<ReactCodeMirrorRef>) {
  return () => {
    if (editorRef.current?.view) {
      undo({
        state: editorRef.current.view.state,
        dispatch: editorRef.current.view.dispatch,
      })
    }
  }
}

function getRedoHandler(editorRef: React.RefObject<ReactCodeMirrorRef>) {
  return () => {
    if (editorRef.current?.view) {
      redo({
        state: editorRef.current.view.state,
        dispatch: editorRef.current.view.dispatch,
      })
    }
  }
}

function getUploadHandler(editorRef: React.RefObject<ReactCodeMirrorRef>) {
  return () => {
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = 'application/json'
    input.onchange = async (event) => {
      const file = (event.target as HTMLInputElement).files?.[0]
      if (file) {
        try {
          const text = await file.text()
          const json = JSON.parse(text) // Validate JSON
          if (editorRef.current?.view) {
            editorRef.current.view.dispatch({
              changes: {
                from: 0,
                to: editorRef.current.view.state.doc.length,
                insert: JSON.stringify(json, null, 2),
              },
            })
            toast.success('File uploaded successfully')
          }
        } catch (error) {
          toast.error('Failed to upload file: Invalid JSON')
        }
      }
    }
    input.click()
  }
}

function getCopyHandler(editorRef: React.RefObject<ReactCodeMirrorRef>) {
  return () => {
    if (editorRef.current?.view) {
      const content = editorRef.current.view.state.doc.toString()
      navigator.clipboard
        .writeText(content)
        .then(() => {
          toast.success('Content copied to clipboard', {
            // style: { backgroundColor: 'green', color: 'white' },
          })
        })
        .catch((err) => {
          toast.error('Failed to copy content: ' + err, {
            // style: { backgroundColor: 'red', color: 'white' },
          })
        })
    }
  }
}

function getDownloadHandler(editorRef: React.RefObject<ReactCodeMirrorRef>) {
  return () => {
    if (editorRef.current?.view) {
      const content = editorRef.current.view.state.doc.toString()
      const blob = new Blob([content], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url

      // Inject current date and time into the file name
      const now = new Date()
      const currentDate = now.toISOString().split('T')[0] // Format: YYYY-MM-DD
      const currentTime = now.toTimeString().split(' ')[0].replace(/:/g, '-') // Format: HH-MM-SS
      a.download = `rule_diagram_${currentDate}_${currentTime}.json` // Updated file name with date and time

      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
    }
  }
}

function getFormatHandler(editorRef: React.RefObject<ReactCodeMirrorRef>) {
  return () => {
    if (editorRef.current?.view) {
      const content = editorRef.current.view.state.doc.toString()
      try {
        const formattedContent = JSON.stringify(JSON.parse(content), null, 2)
        editorRef.current.view.dispatch({
          changes: { from: 0, to: content.length, insert: formattedContent },
        })
        // toast.success('Content formatted successfully')
      } catch (error) {
        toast.error('Failed to format content: Invalid JSON')
      }
    }
  }
}

function getResetHandler({
  editorRef,
  setXml,
}: {
  editorRef: React.RefObject<ReactCodeMirrorRef>
  setXml: (value: string) => void
}) {
  return () => {
    if (editorRef.current?.view) {
      editorRef.current.view.dispatch({
        changes: {
          from: 0,
          to: editorRef.current.view.state.doc.length,
          insert: initialDiagramJson,
        },
      })
      setXml(emptyBpmnXml)
    }
  }
}
