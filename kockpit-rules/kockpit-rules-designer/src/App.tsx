import FlexLayout2 from './flex-layout-2/FlexLayout2'
import './App.css'
import { Toaster } from './components/ui/sonner'
import { SidebarProvider } from './components/ui/sidebar'
import { AppSidebar } from './flex-layout-2/components/AppSidebar/AppSidebar'

export const theme = 'light'

function App() {
  return (
    <SidebarProvider>
      <AppSidebar />
      <div className='flex h-screen grow'>
        <div className='flex-grow p-2'>
          <div className='relative h-full'>
            <FlexLayout2 />
          </div>
        </div>
      </div>
      <Toaster />
    </SidebarProvider>
  )
}

export default App
