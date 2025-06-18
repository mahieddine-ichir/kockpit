import { Button } from '@/components/ui/button'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import RuleForm from '../RuleForm/RuleForm'
import { Plus } from 'lucide-react'
import { useState } from 'react'

export const RuleAddButton = () => {
  const [showRuleForm, setShowRuleForm] = useState(false)

  return (
    <Popover open={showRuleForm} onOpenChange={setShowRuleForm}>
      <PopoverTrigger asChild>
        <Button
          variant='ghost'
          size='icon'
          style={{ width: '24px', height: '24px', padding: '4px' }}
        >
          <Plus className='h-4 w-4' />
        </Button>
      </PopoverTrigger>
      <PopoverContent align='end' side='bottom' className='w-56 bg-slate-50'>
        <RuleForm setShowRuleForm={setShowRuleForm} />
      </PopoverContent>
    </Popover>
  )
}
