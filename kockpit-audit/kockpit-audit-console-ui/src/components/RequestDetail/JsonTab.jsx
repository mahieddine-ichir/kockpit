import React from 'react';

const JsonTab = ({ request }) => {
  return (
    <div className="bg-white shadow overflow-hidden sm:rounded-lg">
      <div className="px-4 py-5 sm:px-6">
        <h3 className="text-lg leading-6 font-medium text-gray-900">Full JSON Request</h3>
      </div>
      <div className="border-t border-gray-200 px-4 py-5 sm:p-6">
        <pre className="text-sm text-gray-900 bg-gray-50 p-4 rounded overflow-x-auto">
          {JSON.stringify(request, null, 2)}
        </pre>
      </div>
    </div>
  );
};

export default JsonTab;