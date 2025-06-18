import BpmnCanvasBase from './BpmnCanvasBase'
import CanvasToolbar from './toolbars/CanvasToolbar'
import BpmnToolbox from './BpmnToolbox'
import { useRef, useState } from 'react'
import Modeler from 'bpmn-js/lib/Modeler'
import { atom, useAtom } from 'jotai'

export const bpmnZoomLevelAtom = atom(1)

export default function BpmnCanvas() {
  const modelerRef = useRef<Modeler<null> | null>(null)
  const [zoomLevel, setZoomLevel] = useAtom(bpmnZoomLevelAtom)

  const handleZoomIn = () => {
    if (modelerRef.current) {
      const canvas = modelerRef.current.get('canvas')
      // @ts-expect-error
      canvas.zoom(zoomLevel + 0.1)
      setZoomLevel(zoomLevel + 0.1)
    }
  }

  const handleZoomOut = () => {
    if (modelerRef.current) {
      const canvas = modelerRef.current.get('canvas')
      // @ts-expect-error
      canvas.zoom(zoomLevel - 0.1)
      setZoomLevel(zoomLevel - 0.1)
    }
  }

  const handleResetZoom = () => {
    if (modelerRef.current) {
      const canvas = modelerRef.current.get('canvas') as any
      for (const i in [1, 2]) {
        const viewbox = canvas.viewbox()
        canvas.viewbox({
          x: -70,
          y: -viewbox.height * 0.37,
          width: viewbox.width,
          height: viewbox.height,
        }) //, width: 500, height: 500 }) // Adjust x, y, width, height as needed
        canvas.zoom(1)
      }
      setZoomLevel(1)
      // canvasService.zoom('fit-viewport')
    }
  }

  return (
      <div className='absolute inset-0'>
        <div className='absolute right-3 top-3 z-[101]'>
          <CanvasToolbar modelerRef={modelerRef} />
        </div>
        <div className='flex-1'>
          <BpmnCanvasBase modelerRef={modelerRef} />
        </div>
        <BpmnToolbox
            zoomLevel={zoomLevel}
            onZoomIn={handleZoomIn}
            onZoomOut={handleZoomOut}
            onResetZoom={handleResetZoom}
        />
      </div>
  )
}
