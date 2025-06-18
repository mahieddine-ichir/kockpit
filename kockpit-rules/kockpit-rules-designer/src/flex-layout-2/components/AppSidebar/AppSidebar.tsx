import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarHeader,
} from '@/components/ui/sidebar'
import { Network } from 'lucide-react'
import { RulesList } from '../RulesManager'

export function AppSidebar() {
  return (
    <Sidebar>
      <SidebarHeader>
        <div className='flex items-center justify-between p-2'>
          <span className='text-lg font-semibold'>Rule Designer</span>
          <Network className='text-muted-foreground' />
        </div>
      </SidebarHeader>
      <SidebarContent>
        <SidebarGroup>
          <RulesList />
        </SidebarGroup>
      </SidebarContent>
    </Sidebar>
  )
}
