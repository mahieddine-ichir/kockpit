import React, {useState} from 'react';
import JsonTab from "./JsonTab.jsx";
import RequestResponseTab from "./RequestResponseTab.jsx";
import KeyValueTab from "./KeyValueTab.jsx";
import HttpExchangeTab from "./HttpExchangeTab.jsx";
import RulesTab from "./RulesTab.jsx";
import RequestDetail from "../../pages/RequestDetail.jsx";
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
    <div>
      <div className="border-b border-gray-200">
        <nav className="flex -mb-px">
          {
            COMPONENTS.map(tab =>
                  (
                      <Tab title={tab.title} key={tab.key} active={activeTab === tab.key}
                       handleClick={() => setActiveTab(tab.key)} />
                  )
                )
          }
        </nav>
      </div>
      <div className="py-4">
        {renderTabContent()}
      </div>
    </div>
  );
};

export default DetailTabs;