import { useCallback, useRef, useState } from 'react'
import DiagramJsonEditor from './DiagramJsonEditor'
import DiagramJsonEditorToolbar from './toolbars/DiagramJsonEditorToolbar'
import { ReactCodeMirrorRef } from '@uiw/react-codemirror'
import { DropzoneOptions, useDropzone } from 'react-dropzone'
import clsx from 'clsx'
import { toast } from 'sonner'
import { Card, CardContent } from '@/components/ui/card'
import { useCanvasXmlHistory, useDiagramJson } from '../hooks/custom-hooks'
import { validateAndConvertJsonToXml } from './DiagramJsonEditorBase'
import { useProblems } from './ProblemsTab'
import {
  elementTypeAtom,
  elementNameAtom,
  elementDescriptionAtom,
  elementIdAtom,
} from '../atoms'
import { useAtomValue, useAtom } from 'jotai'

export default function JsonEditor() {
  const { setXml } = useCanvasXmlHistory()
  const { dispatch } = useProblems()
  const editorRef = useRef<ReactCodeMirrorRef>(null)
  const [isMultipleFiles, setIsMultipleFiles] = useState(false)
  const [isNonJsonFile, setIsNonJsonFile] = useState(false)
  const elementType = useAtomValue(elementTypeAtom)
  const elementId = useAtomValue(elementIdAtom)
  const [elementName, setElementName] = useAtom(elementNameAtom)
  const [elementDescription, setElementDescription] = useAtom(
    elementDescriptionAtom
  )
  const { setJsonStr } = useDiagramJson()
  const isError = isMultipleFiles || isNonJsonFile

  const onDrop = useCallback(
    (acceptedFiles: File[]) => {
      if (acceptedFiles.length > 1) {
        toast.error('Only one file is allowed.', {
          description: 'Multiple files are not accepted.',
        })
      } else if (acceptedFiles.length === 1) {
        const file = acceptedFiles[0]
        if (file.type !== 'application/json' && !file.name.endsWith('.json')) {
          toast.error('File format not accepted', {
            description: 'Only JSON format is allowed.',
          })
        } else {
          const reader = new FileReader()
          reader.onload = async (event) => {
            try {
              const newJsonStr = event.target?.result as string
              JSON.parse(newJsonStr)
              setJsonStr(newJsonStr)
              const res = await validateAndConvertJsonToXml({
                NewJsonStr: newJsonStr,
                dispatch,
                elementType,
                elementName,
                elementId,
                setElementName,
                elementDescription,
                setElementDescription,
              })
              if (res !== null) {
                setXml(res)
              }
              // if (editorRef.current?.view) {
              //   editorRef.current.view.dispatch({
              //     changes: {
              //       from: 0,
              //       to: editorRef.current.view.state.doc.length,
              //       insert: jsonStr,
              //     },
              //   })
              //   toast.success('File loaded successfully')
              // }
            } catch (error) {
              toast.error('Failed to load file: Invalid JSON')
            }
          }
          reader.readAsText(file)
        }
      }
      setIsMultipleFiles(false)
      setIsNonJsonFile(false)
    },
    [editorRef]
  )

  const onDragEnter = useCallback((event: DragEvent) => {
    const items = event.dataTransfer?.items
    if (items) {
      if (items.length > 1) {
        setIsMultipleFiles(true)
      } else {
        setIsMultipleFiles(false)
      }

      if (items.length === 1) {
        const item = items[0]
        if (item.kind === 'file') {
          if (item.type !== 'application/json') {
            setIsNonJsonFile(true)
          } else {
            setIsNonJsonFile(false)
          }
        }
      }
    }
  }, [])

  const onDragLeave = useCallback(() => {
    setIsMultipleFiles(false)
    setIsNonJsonFile(false)
  }, [])

  const { getRootProps, isDragActive } = useDropzone({
    onDrop,
    onDragEnter: onDragEnter as unknown as DropzoneOptions['onDragEnter'],
    onDragLeave: onDragLeave as DropzoneOptions['onDragLeave'],
  })
  return (
    <div {...getRootProps()}>
      <div className='absolute inset-0 bg-slate-200'>
        <div className='absolute right-5 top-5 z-10'>
          <DiagramJsonEditorToolbar editorRef={editorRef} />
        </div>
        <div className='flex-1'>
          <DiagramJsonEditor editorRef={editorRef} />
        </div>
      </div>
      <div
        className={clsx(
          'absolute inset-0 z-10 flex items-center justify-center',
          isDragActive && 'bg-opacity-20 backdrop-blur-sm',
          isDragActive && !isError ? 'bg-slate-500' : '',
          isDragActive && isError ? 'bg-red-300' : ''
        )}
        style={{ pointerEvents: isDragActive ? 'auto' : 'none' }}
      >
        {isDragActive && (
          <div className='text-lg font-semibold text-white'>
            <DraggingContent
              isMultipleFiles={isMultipleFiles}
              isNonJsonFile={isNonJsonFile}
            />
          </div>
        )}
      </div>
    </div>
  )
}

const DraggingContent = ({
  isMultipleFiles,
  isNonJsonFile,
}: {
  isMultipleFiles: boolean
  isNonJsonFile: boolean
}) => {
  return (
    <Card>
      <CardContent className='pt-4'>
        <div className='flex flex-col items-center justify-center'>
          <svg
            className='mb-3 h-8 w-8 text-gray-500 dark:text-gray-400'
            aria-hidden='true'
            xmlns='http://www.w3.org/2000/svg'
            fill='none'
            viewBox='0 0 20 16'
          >
            <path
              stroke='currentColor'
              strokeLinecap='round'
              strokeLinejoin='round'
              strokeWidth='2'
              d='M13 13h3a3 3 0 0 0 0-6h-.025A5.56 5.56 0 0 0 16 6.5 5.5 5.5 0 0 0 5.207 5.021C5.137 5.017 5.071 5 5 5a4 4 0 0 0 0 8h2.167M10 15V6m0 0L8 8m2-2 2 2'
            />
          </svg>
          <p className='text-center text-sm text-gray-500 dark:text-gray-400'>
            {isMultipleFiles && (
              <>
                <p className='text-red-500'>Multiple files are not allowed</p>
                <p className='text-red-500'>
                  Only a single JSON file is accepted
                </p>
              </>
            )}
            {isNonJsonFile && (
              <>
                <p className='text-red-500'>File format not allowed</p>
                <p className='text-red-500'>Only JSON format is accepted</p>
              </>
            )}
          </p>
          {!isMultipleFiles && !isNonJsonFile && (
            <p className='mb-1 text-sm text-gray-500 dark:text-gray-400'>
              <span className='font-semibold'>Drop your JSON file here </span>
              <br />
              to change editor content
            </p>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
