import type { Meta, StoryObj } from '@storybook/react'

import RuleForm from './RuleForm'

const meta = {
  component: RuleForm,
  parameters: {
    layout: 'centered',
  },
} satisfies Meta<typeof RuleForm>

export default meta

type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    setShowRuleForm: () => {},
  },
  decorators: [
    (Story) => (
      <div className='w-60 rounded border bg-slate-50 p-4'>
        <Story />
      </div>
    ),
  ],
}
