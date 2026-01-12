import { Network } from 'lucide-react'
import { RulesList } from '../RulesManager'

export function AppSidebar() {
  return (
    <div className='bg-gradient-to-b from-slate-900 via-slate-800 to-slate-700 h-screen flex flex-col text-white shadow-2xl'>
      <div className='flex items-center justify-between p-4 border-b border-slate-700'>
        <span className='text-lg font-semibold text-white'>Rule Designer</span>
        <Network className='text-slate-300 h-6 w-6' />
      </div>
      <div className='flex-1 overflow-y-auto'>
        <RulesList />
      </div>
      <div className='border-t border-slate-700 bg-slate-800/70'>
        <div className='px-4 pt-2 pb-1'>
          <p className='text-xs text-slate-400'>
            Version: {__BUILD_VERSION__}
          </p>
          <p className='text-xs text-slate-400'>
            Build: {new Date(__BUILD_TIME__).toLocaleString()}
          </p>
        </div>
      </div>
    </div>
  )
}
