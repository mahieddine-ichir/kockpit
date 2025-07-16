import React from 'react';

const KeyValueTab = ({ request }) => {
  return (
    <div className="bg-white shadow overflow-hidden sm:rounded-lg">
      <div className="px-4 py-5 sm:px-6">
        <h3 className="text-lg leading-6 font-medium text-gray-900">Key-Value Pairs</h3>
      </div>
      <div className="border-t border-gray-200">
        {request?.indexedKeyValues && request.indexedKeyValues.length > 0 ? (
          <div className="p-4 grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {request.indexedKeyValues.map((kv, index) => (
              <div key={index} className="flex items-center bg-gray-50 border border-gray-200 rounded-lg p-3 shadow-sm">
                <div className="flex-1 min-w-0">
                  <div className="text-xs font-semibold text-gray-600 truncate">{kv.key}</div>
                  <div className="text-sm text-gray-900 truncate">
                    {kv.value || kv.valueInteger || kv.valueFloat || (kv.valueDate ? new Date(kv.valueDate).toLocaleString() : 'N/A')}
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="px-4 py-5 sm:px-6 text-sm text-gray-500">
            No key-value pairs available
          </div>
        )}
      </div>
    </div>
  );
};

export default KeyValueTab;