import React, {
  Dispatch,
  SetStateAction,
  useEffect,
  useRef,
  useState,
} from 'react'
import mermaid from 'mermaid'
import { useDiagramJson } from '../hooks/custom-hooks'
import {
  JSON3,
  Json3WoIds,
  Step,
  StepWoId,
} from '../util/xml-json-utils/convert-json2-to-json3'
import { assertCannotReach } from '@/lib/utils'
import { createSeededIdGenerator } from '../util/xml-json-utils/util'
import { Button } from '@/components/ui/button'
import { atom, useAtom } from 'jotai'
import {
  ZoomIn,
  ZoomOut,
  RefreshCcw,
  ArrowDown,
  ArrowRight,
} from 'lucide-react'
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'

const zoomInc = 0.1

interface ToolboxProps {
  zoomLevel: number
  onZoomIn: () => void
  onZoomOut: () => void
  onResetZoom: () => void
  orientation?: 'vertical' | 'horizontal'
  setOrientation: (orientation: 'vertical' | 'horizontal') => void
}

const Toolbox: React.FC<ToolboxProps> = ({
  zoomLevel,
  onZoomIn,
  onZoomOut,
  onResetZoom,
  orientation = 'horizontal',
  setOrientation,
}) => {
  return (
    <div className='absolute bottom-2 left-1/2 z-10 flex -translate-x-1/2 gap-2'>
      <div className='flex items-center rounded-lg border bg-slate-100'>
        <Button
          onClick={onZoomOut}
          variant='outline'
          size='icon'
          className='rounded-none rounded-l-lg border-b-0 border-l-0 border-t-0 bg-transparent'
        >
          <ZoomOut className='h-4 w-4' />
        </Button>
        <div className='flex w-16 select-none items-center justify-center self-stretch bg-white px-2'>
          {Math.round(zoomLevel * 100)}%
        </div>
        <Button
          onClick={onZoomIn}
          variant='outline'
          size='icon'
          className='rounded-none border-b-0 border-r-0 border-t-0 bg-transparent'
        >
          <ZoomIn className='h-4 w-4' />
        </Button>
        <Button
          onClick={onResetZoom}
          variant='outline'
          size='icon'
          className='rounded-none rounded-r-lg border-b-0 border-r-0 border-t-0 bg-transparent'
        >
          <RefreshCcw className='h-4 w-4' />
        </Button>
      </div>

      <ToggleGroup
        variant='outline'
        className='flex gap-0 -space-x-px rounded-lg shadow-sm shadow-black/5 rtl:space-x-reverse'
        type='single'
        value={orientation}
        onValueChange={(value) => {
          if (value) setOrientation(value as 'vertical' | 'horizontal')
        }}
      >
        {['horizontal', 'vertical'].map((orientationOption) => (
          <ToggleGroupItem
            key={orientationOption}
            value={orientationOption}
            className='flex-1 rounded-none bg-white shadow-none first:rounded-s-lg last:rounded-e-lg focus-visible:z-10'
          >
            {orientationOption === 'horizontal' ? (
              <ArrowRight className='h-4 w-4' />
            ) : (
              <ArrowDown className='h-4 w-4' />
            )}
          </ToggleGroupItem>
        ))}
      </ToggleGroup>
    </div>
  )
}

const translateXAtom = atom(0)
const translateYAtom = atom(0)
const zoomLevelAtom = atom(1)

export default function MermaidFlowChart() {
  const [error, setError] = useState('')
  const [chartStr, setChartStr] = useState('')
  const { jsonStr } = useDiagramJson()
  const chartRef = useRef<HTMLDivElement>(null)
  const [zoomLevel, setZoomLevel] = useAtom(zoomLevelAtom)
  const [translateX, setTranslateX] = useAtom(translateXAtom)
  const [translateY, setTranslateY] = useAtom(translateYAtom)
  const [orientation, setOrientation] = useState<'horizontal' | 'vertical'>(
    'horizontal'
  )

  const handleZoomIn = () => {
    setZoomLevel((prevZoom) => prevZoom + zoomInc)
  }

  const handleZoomOut = () => {
    setZoomLevel((prevZoom) => prevZoom - zoomInc)
  }

  const handleReset = () => {
    setZoomLevel(1)
    setTranslateX(0)
    setTranslateY(0)
  }

  useEffect(() => {
    const jsonObjWithIDs = createJsonObjWithIDs(jsonStr)
    if (jsonObjWithIDs === null) {
      setError('Error, Failed to create Mermaid flow chart.')
      return
    }
    setError('')

    if (jsonObjWithIDs.steps.length > 0) {
      const lines = [`flowchart ${orientation === 'horizontal' ? 'LR' : 'TB'};`]
      const predicates: Array<Predicate> = []
      abcd({
        steps: jsonObjWithIDs.steps,
        lines: lines,
        predicates,
      })
      predicates.forEach((predicate) => {
        lines.push(
          '  ' + predicate.id + '{"' + predicate.name + '"}:::predicateNode;'
        )
      })
      lines.push(
        '\n  classDef predicateNode fill:#ffcccc,stroke:#000000,stroke-width:2px;'
      )
      setChartStr(lines.join('\n'))
    }
  }, [jsonStr, orientation])

  if (error !== '') {
    return <h3>{error}</h3>
  }

  return (
    <>
      <Toolbox
        orientation={orientation}
        setOrientation={setOrientation}
        zoomLevel={zoomLevel}
        onZoomIn={handleZoomIn}
        onZoomOut={handleZoomOut}
        onResetZoom={handleReset}
      />
      <MermaidChart
        chart={chartStr}
        chartRef={chartRef}
        zoomLevel={zoomLevel}
        setZoomLevel={setZoomLevel}
        translateX={translateX}
        setTranslateX={setTranslateX}
        translateY={translateY}
        setTranslateY={setTranslateY}
      />
    </>
  )
}

interface MermaidChartProps {
  chart: string
  chartRef: React.RefObject<HTMLDivElement>
  zoomLevel: number
  setZoomLevel: Dispatch<SetStateAction<number>>
  translateX: number
  setTranslateX: Dispatch<SetStateAction<number>>
  translateY: number
  setTranslateY: Dispatch<SetStateAction<number>>
}

mermaid.initialize({})

const MermaidChart: React.FC<MermaidChartProps> = ({
  chart = '',
  chartRef,
  zoomLevel,
  setZoomLevel,
  translateX,
  setTranslateX,
  translateY,
  setTranslateY,
}) => {
  useEffect(() => {
    if (
      typeof chartRef !== 'undefined' &&
      'current' in chartRef &&
      chartRef.current
    ) {
      chartRef.current.removeAttribute('data-processed')
      mermaid.contentLoaded()
    }
  }, [chart])

  const [isPanning, setIsPanning] = useState(false)

  const handleMouseDown = (e: React.MouseEvent) => {
    setIsPanning(true)
  }

  const handleMouseMove = (e: React.MouseEvent) => {
    if (!isPanning) return
    setTranslateX((prevTranslateX) => prevTranslateX + e.movementX / zoomLevel)
    setTranslateY((prevTranslateY) => prevTranslateY + e.movementY / zoomLevel)
  }

  const handleMouseUp = () => {
    setIsPanning(false)
  }

  const handleWheel = (e: React.WheelEvent) => {
    e.preventDefault()
    if (e.deltaY < 0) {
      // Zoom in
      setZoomLevel((prevZoom) => prevZoom + zoomInc)
    } else {
      // Zoom out
      setZoomLevel((prevZoom) => prevZoom - zoomInc)
    }
  }

  const transform = `scale(${zoomLevel}) translate(${translateX}px, ${translateY}px)`

  return (
    <div className='h-full overflow-hidden bg-slate-50'>
      <div
        ref={chartRef}
        className='mermaid flex h-full justify-center'
        onMouseDown={handleMouseDown}
        onMouseMove={handleMouseMove}
        onMouseUp={handleMouseUp}
        onMouseLeave={handleMouseUp}
        onWheel={handleWheel}
        style={{
          transform: transform,
          cursor: isPanning ? 'grabbing' : 'grab',
          overflow: 'hidden',
          userSelect: 'none',
        }}
      >
        {chart}
      </div>
    </div>
  )
}

function getStringIntoLinesSplitter(
  lineLength: number
): (str: string) => string {
  // return (str: string) =>
  //   str.match(new RegExp(`.{1,${lineLength}}`, 'g'))?.join('\n') ?? ''
  return (str: string) => {
    const parts = str.split('_')
    if (parts.length === 1 && parts[0].length <= lineLength) {
      return parts[0]
    }
    const lines = []
    let line = parts[0]
    for (const part of parts.slice(1)) {
      line += '_' + part
      if (line.length < lineLength) {
        continue
      }
      lines.push(line)
      line = ''
    }
    return lines.join('\n')
  }
}

const splitIntoLines = getStringIntoLinesSplitter(10)

type Predicate = { id: string; name: string }

function abcd({
  steps,
  lines,
  predicates,
}: {
  steps: Step[]
  lines: string[]
  predicates: Array<Predicate>
}) {
  if (steps.length === 0) {
    return
  }
  if (steps.length === 1) {
    const step = steps[0]

    const nextNode =
      step.type === 'predicate'
        ? step.id + '{"' + (step.name ?? ' ') + '"}'
        : step.id + '["' + (step.name ?? ' ') + '"]'

    if (step.type === 'predicate') {
      if (typeof predicates.find((p) => p.id === step.id) === 'undefined') {
        predicates.push({ id: step.id, name: step.name ?? ' ' })
      }

      if (step.true.length > 0) {
        const nextNextNode = (() => {
          const type = step.true[0].type
          if (type === 'action') {
            return step.true[0].id + '["' + (step.true[0].name ?? ' ') + '"]'
          }
          if (type === 'predicate') {
            return step.true[0].id + '{"' + (step.true[0].name ?? ' ') + '"}'
          }
          assertCannotReach(type)
        })()
        lines.push('  ' + nextNode + ' --> ' + '|true|' + nextNextNode + ';')
        abcd({ steps: step.true, lines, predicates })
      }

      if (step.false.length > 0) {
        const nextNextNode = (() => {
          const type = step.false[0].type
          if (type === 'action') {
            return step.false[0].id + '["' + (step.false[0].name ?? ' ') + '"]'
          }
          if (type === 'predicate') {
            return step.false[0].id + '{"' + (step.false[0].name ?? ' ') + '"}'
          }
          assertCannotReach(type)
        })()
        lines.push('  ' + nextNode + ' --> ' + '|false|' + nextNextNode + ';')
        abcd({ steps: step.false, lines, predicates })
      }
    }
  }
  const nameLineWidth = 15
  for (let i = 0; i < steps.length - 1; i++) {
    const currentStep = { ...steps[i] }
    const nextStep = { ...steps[i + 1] }
    ;[currentStep, nextStep].forEach((step) => {
      if (typeof step.name === 'string') {
        step.name = splitIntoLines(step.name)
      }
    })
    if (nextStep.type === 'predicate') {
      if (nextStep.true.length > 0) {
        nextStep.true[0].name = splitIntoLines(nextStep.true[0].name ?? ' ')
      }
      if (nextStep.false.length > 0) {
        nextStep.false[0].name = splitIntoLines(nextStep.false[0].name ?? ' ')
      }
    }
    const currentNode = currentStep.id + '["' + (currentStep.name ?? ' ') + '"]'
    const nextNode =
      nextStep.type === 'predicate'
        ? nextStep.id + '{"' + (nextStep.name ?? ' ') + '"}'
        : nextStep.id + '["' + (nextStep.name ?? ' ') + '"]'
    lines.push('  ' + currentNode + ' --> ' + nextNode + ';')

    if (nextStep.type === 'predicate') {
      if (typeof predicates.find((p) => p.id === nextStep.id) === 'undefined') {
        predicates.push({ id: nextStep.id, name: nextStep.name ?? ' ' })
      }
      if (nextStep.true.length > 0) {
        const nextNextNode = (() => {
          const type = nextStep.true[0].type
          if (type === 'action') {
            return (
              nextStep.true[0].id + '["' + (nextStep.true[0].name ?? ' ') + '"]'
            )
          }
          if (type === 'predicate') {
            return (
              nextStep.true[0].id + '{"' + (nextStep.true[0].name ?? ' ') + '"}'
            )
          }
          assertCannotReach(type)
        })()
        lines.push('  ' + nextNode + ' --> ' + '|true|' + nextNextNode + ';')
        abcd({ steps: nextStep.true, lines, predicates })
      }

      if (nextStep.false.length > 0) {
        const nextNextNode = (() => {
          const type = nextStep.false[0].type
          if (type === 'action') {
            return (
              nextStep.false[0].id +
              '["' +
              (nextStep.false[0].name ?? ' ') +
              '"]'
            )
          }
          if (type === 'predicate') {
            return (
              nextStep.false[0].id +
              '{"' +
              (nextStep.false[0].name ?? ' ') +
              '"}'
            )
          }
          assertCannotReach(type)
        })()
        lines.push('  ' + nextNode + ' --> ' + '|false|' + nextNextNode + ';')
        abcd({ steps: nextStep.false, lines, predicates })
      }
    }
  }
}

function createJsonObjWithIDs(jsonStr: string): JSON3 | null {
  const gen = createSeededIdGenerator('12345')
  try {
    const obj = JSON.parse(jsonStr) as Json3WoIds
    addIdsToSteps(obj.steps, gen)
    return obj as JSON3
  } catch (e) {
    return null
  }
}

function addIdsToSteps(steps: StepWoId[], gen: () => string) {
  for (const step of steps) {
    // @ts-expect-error
    step['id'] = gen()
  }
  if (steps.length === 0) {
    return
  }
  let lastStep = steps.at(-1) as StepWoId
  if (lastStep?.type === 'action') {
    return
  }
  addIdsToSteps(lastStep.true, gen)
  addIdsToSteps(lastStep.false, gen)
}
