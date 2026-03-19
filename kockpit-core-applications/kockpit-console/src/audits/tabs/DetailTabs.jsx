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
    icon: Info,
    view: (request) => <OverviewTab request={request} />
  },
  {
    title: 'Key & Value',
    key: 'keyvalue',
    icon: Tag,
    view: (request) => <KeyValueTab request={request} />
  },
  {
    title: 'HTTP Exchange',
    key: 'httpexchange',
    icon: Network,
    view: (request) => <HttpExchangeTab request={request} />
  },
  {
    title: 'Request & Response',
    key: 'request',
    icon: ArrowLeftRight,
    disabled: (request) => !request['audits']?.find(audit => audit.type === 'builtin.web'),
    view: (request) => <RequestResponseTab request={request} />
  },
  {
    title: 'Rules',
    key: 'rules',
    icon: GitBranch,
    view: (request) => <RulesTab request={request} />
  },
  {
    title: 'Json',
    key: 'json',
    icon: FileJson,
    view: (request) => <JsonTab request={request} />
  }
];

const DetailTabs = ({ request }) => {
  const [activeTab, setActiveTab] = useState('details');

  const renderTabContent = () => {
    return COMPONENTS.find(component => component.key === activeTab).view(request);
  };

  return (
      <div>
        <div className="border-b border-slate-100 px-6 pt-4">
          <nav className="flex gap-2">
            {COMPONENTS.map(({ key, title, icon: Icon, disabled }) => {
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
                  <Icon className="w-4 h-4" />
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