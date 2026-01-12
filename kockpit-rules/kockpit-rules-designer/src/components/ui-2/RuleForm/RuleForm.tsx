import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { z } from 'zod'

import { Button } from '@/components/ui/button'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { X } from 'lucide-react'
import { db } from '@/flex-layout-2/db'
import {
  createEmptyJsonStr,
  createEmptyXmlHistory,
} from '@/flex-layout-2/components/RulesManager'
import { initialLayoutJson } from '@/flex-layout-2/util/initial-values'
import { Textarea } from '@/components/ui/textarea'

const formSchema = z.object({
  ruleName: z.string().min(1, {
    message: 'Rule name must have at least 1 character.',
  }),
  ruleDescription: z.string(),
})

export default function RuleForm({
  setShowRuleForm,
}: {
  setShowRuleForm: React.Dispatch<React.SetStateAction<boolean>>
}) {
  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      ruleName: '',
      ruleDescription: '',
    },
  })

  async function onSubmit(values: z.infer<typeof formSchema>) {
    console.log(values)
    await db.rules.add({
      name: values.ruleName,
      description: values.ruleDescription,
      xmlHistory: createEmptyXmlHistory({
        ruleName: values.ruleName,
        ruleDescription: values.ruleDescription,
      }),
      jsonStr: createEmptyJsonStr({
        ruleName: values.ruleName,
        ruleDescription: values.ruleDescription,
      }),
      layout: initialLayoutJson,
    })
    setShowRuleForm(false)
  }

  return (
    <div className=''>
      <Form {...form}>
        <form
          onSubmit={form.handleSubmit(onSubmit)}
          className='flex flex-col gap-4'
        >
          <div className='mb-2 flex items-center justify-between'>
            <h2 className='flex-grow text-white font-semibold'>Add Rule</h2>
            <Button
              type='button'
              variant='ghost'
              size='icon'
              className='text-slate-300 hover:bg-slate-700 hover:text-white'
              onClick={() => setShowRuleForm(false)}
              style={{ width: '24px', height: '24px', padding: '4px' }}
            >
              <X className='h-4 w-4' />
            </Button>
          </div>
          <FormField
            control={form.control}
            name='ruleName'
            render={({ field }) => (
              <FormItem>
                <FormLabel className='text-slate-300'>Rule name</FormLabel>
                <FormControl>
                  <Input {...field} className='bg-slate-700 border-slate-600 text-white' autoFocus />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name='ruleDescription'
            render={({ field }) => (
              <FormItem>
                <FormLabel className='text-slate-300'>Rule description</FormLabel>
                <FormControl>
                  <Textarea {...field} className='bg-slate-700 border-slate-600 text-white' />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <Button type='submit' className='mt-4 self-end bg-blue-600 hover:bg-blue-700'>
            Save
          </Button>
        </form>
      </Form>
    </div>
  )
}
