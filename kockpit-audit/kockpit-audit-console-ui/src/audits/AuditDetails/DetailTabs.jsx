import React, {useState} from 'react';
import JsonTab from "./JsonTab.jsx";
import RequestResponseTab from "./RequestResponseTab.jsx";
import KeyValueTab from "./KeyValueTab.jsx";
import HttpExchangeTab from "./HttpExchangeTab.jsx";
import RulesTab from "./RulesTab.jsx";
import DetailsPage from "../pages/DetailsPage.jsx";
import OverviewTab from "./OverviewTab.jsx";

const Tab = ({title, key, active, handleClick}) => {
  return (
      <button
          key={key}
          onClick={() => handleClick(key)}
          className={`mr-8 py-4 px-1 border-b-2 font-medium text-sm ${
              active
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
          }`}
      >
        {title}
      </button>
  )
}

const COMPONENTS = [
  {
    title: 'Details',
    key: 'details',
    view: (request) => <OverviewTab request={request} />
  },
  {
    title: 'Key & Value',
    key: 'keyvalue',
    view: (request) => <KeyValueTab request={request} />
  },
  {
    title: 'HTTP Exchange',
    key: 'httpexchange',
    view: (request) => <HttpExchangeTab request={request} />
  },
  {
    title: 'Request & Response',
    key: 'request',
    view: (request) => <RequestResponseTab request={request} />
  },
  {
    title: 'Rules',
    key: 'rules',
    view: (request) => <RulesTab request={request} />
  },
  {
    title: 'Json',
    key: 'json',
    view: (request) => <JsonTab request={request} />
  }
];

const DetailTabs = ({ request }) => {
  const [activeTab, setActiveTab] = useState('details');

  const renderTabContent = () => {
    return COMPONENTS.find(component => component.key === activeTab).view(request);
  };

  return (
      <div className="bg-white rounded-xl shadow-md border-l-4 border-blue-600/60 p-0">
        <div className="border-b border-slate-100 px-6 pt-4">
          <nav className="flex gap-2">
            {COMPONENTS.map(({ key, title }) => (
                <button
                    key={key}
                    onClick={() => setActiveTab(key)}
                    className={`px-4 py-2 rounded-t-lg font-semibold text-sm transition-all duration-150 focus:outline-none
                ${activeTab === key
                        ? 'bg-blue-50 text-blue-700 border-b-2 border-blue-600 shadow-sm'
                        : 'text-slate-500 hover:text-blue-600 hover:bg-slate-50 border-b-2 border-transparent'}
              `}
                    style={{ minWidth: 110 }}
                >
                  {title}
                </button>
            ))}
          </nav>
        </div>
        <div className="py-6 px-6">
          {renderTabContent()}
        </div>
      </div>
  );
};

export default DetailTabs;