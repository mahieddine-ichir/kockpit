import DiagramJsonEditorBase from './DiagramJsonEditorBase'
import { useDiagramJson } from '../hooks/custom-hooks'
import { ReactCodeMirrorRef } from '@uiw/react-codemirror'

export default function DiagramJsonEditor({
  editorRef,
}: {
  editorRef: React.RefObject<ReactCodeMirrorRef>
}) {
  const { jsonStr, setJsonStr } = useDiagramJson()

  return (
    <div className='absolute inset-0 bg-white'>
      <DiagramJsonEditorBase
        editorRef={editorRef}
        jsonStr={jsonStr}
        setJsonStr={setJsonStr}
      />
    </div>
  )
}
