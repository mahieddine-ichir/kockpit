import React, { useState } from 'react';
import OverviewTab from './OverviewTab';
import RequestResponseTab from './RequestResponseTab';
import JsonTab from './JsonTab';
import KeyValueTab from './KeyValueTab';
import HttpExchangeTab from './HttpExchangeTab';
import RulesTab from "./RulesTab.jsx";

const DetailTabs = ({ request }) => {
  const [activeTab, setActiveTab] = useState('details');

  const renderTabContent = () => {
    switch(activeTab) {
      case 'details':
        return <OverviewTab request={request} />;
      case 'keyvalue':
        return <KeyValueTab request={request} />;
      case 'httpexchange':
        return <HttpExchangeTab request={request} />;
      case 'request':
        return <RequestResponseTab request={request} />;
      case 'json':
        return <JsonTab request={request} />;
      case 'rules':
        return <RulesTab request={request} />;
      default:
        return null;
    }
  };

  return (
    <div>
      <div className="border-b border-gray-200">
        <nav className="flex -mb-px">
          {['details', 'keyvalue', 'httpexchange' ,'request', 'rules', 'json'].map(tab => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`mr-8 py-4 px-1 border-b-2 font-medium text-sm ${
                activeTab === tab 
                  ? 'border-blue-500 text-blue-600' 
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              {tab === 'keyvalue' ? 'Key-Value' :
                  tab === 'httpexchange' ? 'HTTP Exchange' :
                      tab.charAt(0).toUpperCase() + tab.slice(1)}
            </button>
          ))}
        </nav>
      </div>
      <div className="py-4">
        {renderTabContent()}
      </div>
    </div>
  );
};

export default DetailTabs;