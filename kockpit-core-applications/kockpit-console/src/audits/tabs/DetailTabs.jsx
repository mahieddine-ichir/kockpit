import React, {useState} from 'react';
import { Info, Tag, Network, ArrowLeftRight, GitBranch, FileJson } from 'lucide-react';
import JsonTab from "./JsonTab.jsx";
import RequestResponseTab from "./RequestResponseTab.jsx";
import KeyValueTab from "./KeyValueTab.jsx";
import HttpExchangeTab from "./HttpExchangeTab.jsx";
import RulesTab from "./RulesTab.jsx";
import OverviewTab from "./OverviewTab.jsx";

const COMPONENTS = [
  {
    title: 'Details',
    key: 'details',
    icon: <Info className="w-4 h-4" />,
    view: (request, openTab) => <OverviewTab request={request} onOpenTab={openTab} />
  },
  {
    title: 'Key & Value',
    key: 'keyvalue',
    icon: <Tag className="w-4 h-4" />,
    view: (request) => <KeyValueTab request={request} />
  },
  {
    title: 'HTTP Exchange',
    key: 'httpexchange',
    icon: <Network className="w-4 h-4" />,
    view: (request) => <HttpExchangeTab request={request} />
  },
  {
    title: 'Request & Response',
    key: 'request',
    icon: <ArrowLeftRight className="w-4 h-4" />,
    disabled: (request) => !request['audits']?.find(audit => audit.type === 'builtin.web'),
    view: (request) => <RequestResponseTab request={request} />
  },
  {
    title: 'Rules',
    key: 'rules',
    icon: <GitBranch className="w-4 h-4" />,
    view: (request) => <RulesTab request={request} />
  },
  {
    title: 'Json',
    key: 'json',
    icon: <FileJson className="w-4 h-4" />,
    view: (request) => <JsonTab request={request} />
  }
];

const DetailTabs = ({ request }) => {
  const [activeTab, setActiveTab] = useState('details');

  const renderTabContent = () => {
    return COMPONENTS.find(component => component.key === activeTab).view(request, openTab);
  };

  // lets a tab (Details) send the user to another one - unless that one is disabled for this audit
  const openTab = (key) => {
    const target = COMPONENTS.find(component => component.key === key);
    if (target && !(target.disabled?.(request) ?? false)) setActiveTab(key);
  };

  return (
      <div>
        <div className="border-b border-slate-100 px-6 pt-4">
          <nav className="flex gap-2">
            {COMPONENTS.map(({ key, title, icon, disabled }) => {
              const isDisabled = disabled?.(request) ?? false;
              return (
                <button
                    key={key}
                    onClick={() => !isDisabled && setActiveTab(key)}
                    disabled={isDisabled}
                    className={`px-4 py-2 rounded-t-lg font-semibold text-sm transition-all duration-150 focus:outline-none flex items-center gap-2
                ${isDisabled
                        ? 'text-slate-300 border-b-2 border-transparent cursor-not-allowed'
                        : activeTab === key
                          ? 'bg-blue-50 text-blue-700 border-b-2 border-blue-600 shadow-sm'
                          : 'text-slate-500 hover:text-blue-600 hover:bg-slate-50 border-b-2 border-transparent'}
              `}
                    style={{ minWidth: 110 }}
                >
                  {icon}
                  <span>{title}</span>
                </button>
              );
            })}
          </nav>
        </div>
        <div className="py-6 px-6">
          {renderTabContent()}
        </div>
      </div>
  )
};

export default DetailTabs;