import type { Meta, StoryObj } from '@storybook/react'
import Problems from './Problems'

const meta = {
  title: 'Components/Problems',
  component: Problems,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof Problems>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    problems: [
      {
        name: 'Invalid JSON',
        isActive: true,
        type: 'error',
        message: 'The JSON is invalid.',
      },
      {
        name: 'Invalid Diagram JSON',
        isActive: true,
        type: 'error',
        message: 'The diagram JSON is invalid.',
      },
      {
        name: 'No Start Event',
        isActive: false,
        type: 'warning',
        message: 'There are missing nodes in the diagram.',
      },
      {
        name: 'No End Event',
        isActive: true,
        type: 'warning',
        message: 'There are missing nodes in the diagram.',
      },
    ],
  },
}
