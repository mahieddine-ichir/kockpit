import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  // DialogDescription,
  // DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'
import Modeler from 'bpmn-js/lib/Modeler'
import clsx from 'clsx'
import jsPDF from 'jspdf'
import { useState, useEffect } from 'react'
import { toast } from 'sonner'

export function ChooseImageFormatModalButton({
  isExpanded,
  item,
  modelerRef,
}: {
  isExpanded: boolean
  item: {
    tooltip: string
    icon: any
    eventHandler: () => void
  }
  modelerRef: React.MutableRefObject<Modeler<null> | null>
}) {
  const formats = ['JPEG', 'PNG', 'PDF', 'SVG']
  const [selectedFormat, setSelectedFormat] = useState(formats[0])
  const [fileName, setFileName] = useState('')
  const [isDialogOpen, setIsDialogOpen] = useState(false)

  useEffect(() => {
    if (isDialogOpen) {
      const now = new Date()
      const currentDate = now.toISOString().split('T')[0] // Format: YYYY-MM-DD
      const currentTime = now.toTimeString().split(' ')[0].replace(/:/g, '-') // Format: HH-MM-SS
      setFileName(`Rule_diagram_${currentDate}_${currentTime}`)
    }
  }, [isDialogOpen])

  const handleExport = async ({
    modelerRef,
    filename,
    format,
  }: {
    modelerRef: React.MutableRefObject<Modeler<null> | null>
    filename: string
    format: string
  }) => {
    const newFilename = `${filename}.${format.toLowerCase()}`
    switch (format) {
      case 'JPEG':
        return downloadDiagramJpeg(modelerRef, newFilename)
      case 'PNG':
        return downloadDiagramPng(modelerRef, newFilename)
      case 'PDF':
        return downloadDiagramPdf(modelerRef, newFilename)
      case 'SVG':
        return downloadDiagramSvg(modelerRef, newFilename)
    }
  }

  return (
    <Dialog onOpenChange={setIsDialogOpen}>
      <DialogTrigger asChild>
        <Button
          variant='outline'
          size='icon'
          className={clsx(
            'rounded-full',
            'transition-all',
            'duration-300',
            'ease-in-out',
            {
              'h-0': !isExpanded,
              'w-0': !isExpanded,
              'border-none': !isExpanded,
            }
          )}
          onClick={item.eventHandler}
        >
          <item.icon
            className={clsx(
              'h-4',
              'w-4',
              'transition-all',
              'duration-300',
              'ease-in-out',
              'opacity-0',
              {
                'h-0': !isExpanded,
                'w-0': !isExpanded,
                'opacity-100': isExpanded,
              }
            )}
          />
          <span className='sr-only'>{item.tooltip}</span>
        </Button>
      </DialogTrigger>
      <DialogContent className='sm:max-w-[425px]'>
        <DialogHeader>
          <DialogTitle>File export</DialogTitle>
        </DialogHeader>

        <div className='flex flex-col gap-8'>
          <div>
            <label className='mb-1 mt-4 block text-sm font-medium text-gray-700'>
              Choose format
            </label>

            <ToggleGroup
              variant='outline'
              className='flex gap-0 -space-x-px rounded-lg shadow-sm shadow-black/5 rtl:space-x-reverse'
              type='single'
              value={selectedFormat}
              onValueChange={(value) => {
                if (value) setSelectedFormat(value)
              }}
            >
              {formats.map((format) => (
                <ToggleGroupItem
                  key={format}
                  value={format}
                  className='flex-1 rounded-none shadow-none first:rounded-s-lg last:rounded-e-lg focus-visible:z-10'
                >
                  {format}
                </ToggleGroupItem>
              ))}
            </ToggleGroup>
          </div>

          <div>
            <label className='block text-sm font-medium text-gray-700'>
              File Name
            </label>

            <div className='flex rounded-lg shadow-sm shadow-black/5'>
              <Input
                className='-me-px rounded-e-none shadow-none'
                placeholder='File name (without extension)'
                type='text'
                value={fileName}
                onChange={(e) => setFileName(e.target.value)}
              />
              <span className='-z-10 inline-flex items-center rounded-e-lg border border-input bg-background px-3 text-sm text-muted-foreground'>
                .{selectedFormat.toLowerCase()}
              </span>
            </div>
          </div>

          <div className='flex justify-end'>
            <Button
              autoFocus
              onClick={() =>
                handleExport({
                  modelerRef,
                  filename: fileName,
                  format: selectedFormat,
                })
              }
            >
              Export
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}

const downloadDiagramSvg = async (
  modelerRef: React.MutableRefObject<Modeler<null> | null>,
  filename: string
) => {
  if (!modelerRef || !modelerRef.current) return
  try {
    const { svg } = await modelerRef.current.saveSVG()
    const blob = new Blob([svg], { type: 'image/svg+xml' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  } catch (err) {
    console.error('Error exporting diagram as SVG:', err)
  }
}

const downloadDiagramPng = async (
  modelerRef: React.MutableRefObject<Modeler<null> | null>,
  filename: string
) => {
  if (!modelerRef || !modelerRef.current) return
  try {
    const { svg } = await modelerRef.current.saveSVG()
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    const img = new Image()

    img.onload = () => {
      canvas.width = img.width
      canvas.height = img.height
      ctx?.drawImage(img, 0, 0)
      canvas.toBlob((blob) => {
        if (!blob) {
          toast('Error exporting diagram as PNG', {
            description:
              'One possible reason is that the diagram is empty. Add elements in the canvas and try again.',
          })
          return
        }
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = filename
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
        URL.revokeObjectURL(url)
      }, 'image/png')
    }

    img.src = 'data:image/svg+xml;base64,' + btoa(svg)
  } catch (err) {
    console.error('Error exporting diagram as PNG:', err)
  }
}

const downloadDiagramJpeg = async (
  modelerRef: React.MutableRefObject<Modeler<null> | null>,
  filename: string
) => {
  if (!modelerRef || !modelerRef.current) {
    return
  }
  try {
    const { svg } = await modelerRef.current.saveSVG()
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    const img = new Image()

    img.onload = () => {
      canvas.width = img.width
      canvas.height = img.height
      if (ctx) {
        ctx.fillStyle = 'white'
        ctx.fillRect(0, 0, canvas.width, canvas.height)
        ctx.drawImage(img, 0, 0)
        canvas.toBlob((blob) => {
          if (!blob) {
            toast('Error exporting diagram as JPEG', {
              description:
                'One possible reason is that the diagram is empty. Add elements in the canvas and try again.',
            })
            return
          }
          const url = URL.createObjectURL(blob)
          const a = document.createElement('a')
          a.href = url
          a.download = filename
          document.body.appendChild(a)
          a.click()
          document.body.removeChild(a)
          URL.revokeObjectURL(url)
        }, 'image/jpeg')
      }
    }

    img.src = 'data:image/svg+xml;base64,' + btoa(svg)
  } catch (err) {
    console.error('Error exporting diagram as JPEG:', err)
  }
}

const downloadDiagramPdf = async (
  modelerRef: React.MutableRefObject<Modeler<null> | null>,
  filename: string
) => {
  if (!modelerRef || !modelerRef.current) return
  try {
    const { svg } = await modelerRef.current.saveSVG()
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    const img = new Image()

    img.onload = () => {
      canvas.width = img.width
      canvas.height = img.height
      ctx?.drawImage(img, 0, 0)

      canvas.toBlob((blob) => {
        if (!blob) {
          toast('Error exporting diagram as PDF', {
            description:
              'One possible reason is that the diagram is empty. Add elements in the canvas and try again.',
          })
          return
        }
      }, 'image/png')

      const pdf = new jsPDF({
        orientation: img.width > img.height ? 'landscape' : 'portrait',
        unit: 'px',
        format: [img.width, img.height],
      })

      pdf.addImage(canvas, 'PNG', 0, 0, img.width, img.height)
      pdf.save(filename)
    }

    img.src = 'data:image/svg+xml;base64,' + btoa(svg)
  } catch (err) {
    console.error('Error exporting diagram as PDF:', err)
  }
}
