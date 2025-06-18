import { ChevronUp } from 'lucide-react'
import clsx from 'clsx'

import { Button } from '@/components/ui/button'
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/components/ui/tooltip'
import { ChooseImageFormatModalButton } from './ChooseImageFormatModalButton'
import Modeler from 'bpmn-js/lib/Modeler'

export type MenuItem = {
  type?: 'normal' | 'export-button'
  icon: any
  label: string
  tooltip: string
  eventHandler: (() => Promise<void>) | (() => void)
}

type CollapsibleToolbarProps = {
  isExpanded: boolean
  handleToggle: () => void
  menuItems: MenuItem[]
  modelerRef: React.MutableRefObject<Modeler<null> | null> | null
}

export default function CollapsibleToolbar({
  isExpanded,
  handleToggle,
  menuItems,
  modelerRef,
}: CollapsibleToolbarProps) {
  return (
    <TooltipProvider>
      <div
        className={clsx(
          'absolute',
          'top-0',
          'right-0',
          'inline-flex',
          'flex-col',
          'items-center',
          'rounded-full',
          'p-1',
          'gap-0',
          'transition-all',
          'duration-300',
          'ease-in-out',
          'h-fit',
          'bg-slate-200',
          {
            'bg-transparent': !isExpanded,
            // border: isExpanded,
            'h-fit': isExpanded,
            'gap-1': isExpanded,
            // 'p-0': !isExpanded,
          }
        )}
      >
        {menuItems.map((item) => (
          <Tooltip key={item.label}>
            <TooltipTrigger asChild>
              {item.type === 'export-button' && modelerRef !== null ? (
                <ChooseImageFormatModalButton
                  isExpanded={isExpanded}
                  item={item}
                  modelerRef={modelerRef}
                />
              ) : (
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
                      // hidden: !isExpanded,
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
              )}
            </TooltipTrigger>
            <TooltipContent side='left'>{item.tooltip}</TooltipContent>
          </Tooltip>
        ))}

        <Tooltip>
          <TooltipTrigger asChild>
            <Button
              variant='outline'
              size='icon'
              className={clsx(
                'rounded-full',
                'transition-all',
                'duration-300',
                'ease-in-out',
                'bg-slate-300',
                {
                  'bg-slate-200': !isExpanded,
                }
              )}
              onClick={handleToggle}
            >
              {/* {isExpanded ? ( */}
              <ChevronUp
                className={clsx(
                  'h-4',
                  'w-4',
                  'transition-all',
                  'duration-300',
                  'ease-in-out',
                  {
                    'rotate-180': !isExpanded,
                  }
                )}
              />
              {/* ) : (
                <ChevronDown className='h-4 w-4' />
              )} */}
              <span className='sr-only'>
                {isExpanded ? 'Hide' : 'Show'} Toolbar
              </span>
            </Button>
          </TooltipTrigger>
          <TooltipContent side='left'>
            {isExpanded ? 'Hide' : 'Show'} Toolbar
          </TooltipContent>
        </Tooltip>
      </div>
    </TooltipProvider>
  )
}
