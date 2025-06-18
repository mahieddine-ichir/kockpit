import type { Meta, StoryObj } from '@storybook/react'

import { RuleAddButton } from './RuleAddButton'

const meta = {
  component: RuleAddButton,
  parameters: {
    layout: 'centered',
  },
} satisfies Meta<typeof RuleAddButton>

export default meta

type Story = StoryObj<typeof meta>

export const Default: Story = {}

