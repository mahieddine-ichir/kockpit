import { Badge } from '@/components/ui/badge'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Problem } from '../../ProblemsTab'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { XCircle } from 'lucide-react'
import { AlertTriangle } from 'lucide-react'
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/components/ui/tooltip'

export default function Problems({ problems }: { problems: Problem[] }) {
  const count = problems.reduce((acc, p2) => acc + +p2.isActive, 0)

  return (
    <div className='flex h-full flex-col gap-4 bg-slate-100 p-3'>
      <h2 className='flex flex-shrink-0 items-center gap-2 text-xl font-bold'>
        Problems{' '}
        <Badge variant={count > 0 ? 'destructive' : 'default'}>{count}</Badge>
      </h2>
      <ScrollArea
        className='flex h-full flex-grow rounded border border-gray-400 bg-sky-100'
        type='always'
      >
        <ul className='flex flex-col gap-2 px-4 py-2'>
          {problems.map(
            ({ isActive, name, type, message }, i) =>
              isActive && (
                // Array.from(Array(10).keys()).map((i) => (
                <li key={i} className='rounded-xl bg-white'>
                  <Alert
                    key={i}
                    variant='default'
                    className={
                      '' +
                      ' ' +
                      (type === 'warning'
                        ? 'border border-orange-400 text-orange-400'
                        : 'border border-red-500 text-red-500') +
                      ' ' +
                      (message !== '' ? 'h-16' : '')
                    }
                  >
                    {type === 'error' ? (
                      <XCircle
                        className='-mt-1 h-4 w-4 text-red-500'
                        style={{ color: 'rgb(239, 68, 68)' }}
                      />
                    ) : (
                      <AlertTriangle
                        className='-mt-1 h-4 w-4 text-orange-500'
                        style={{ color: 'rgb(249, 115, 22)' }}
                      />
                    )}
                    <TooltipProvider>
                      <Tooltip>
                        <div className='relative'>
                          <TooltipTrigger>
                            <AlertTitle className='absolute left-6 right-0 top-0 truncate'>
                              {name}
                            </AlertTitle>
                            <TooltipContent
                              side='top'
                              className='max-h-96 overflow-auto'
                            >
                              <pre className='overflow-auto'>{name}</pre>
                            </TooltipContent>
                          </TooltipTrigger>
                        </div>
                      </Tooltip>
                      <Tooltip>
                        <div className='relative'>
                          <TooltipTrigger>
                            <AlertDescription className='absolute left-6 right-0 top-0 truncate'>
                              {message}
                            </AlertDescription>
                            <TooltipContent
                              side='top'
                              className='max-h-96 overflow-auto'
                            >
                              <pre className='overflow-auto'>{message}</pre>
                            </TooltipContent>
                          </TooltipTrigger>
                        </div>
                      </Tooltip>
                    </TooltipProvider>
                  </Alert>
                </li>
              )
            // ))
          )}
        </ul>
      </ScrollArea>
    </div>
  )
}
