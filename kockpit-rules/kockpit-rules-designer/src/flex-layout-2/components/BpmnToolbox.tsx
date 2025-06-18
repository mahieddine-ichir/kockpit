import React from 'react'
import { Button } from '@/components/ui/button'
import { ZoomIn, ZoomOut, RefreshCcw } from 'lucide-react'

interface BpmnToolboxProps {
  zoomLevel: number
  onZoomIn: () => void
  onZoomOut: () => void
  onResetZoom: () => void
}

const BpmnToolbox: React.FC<BpmnToolboxProps> = ({
  zoomLevel,
  onZoomIn,
  onZoomOut,
  onResetZoom,
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
    </div>
  )
}

export default BpmnToolbox
