import React from 'react';
import CopyButton from "../CopyButton.jsx";

const JsonTab = ({ request }) => {
  return (
      <div className="border-t border-gray-200">
          <div className="px-4 py-5 sm:px-6">
              <h3 className="text-lg leading-6 font-medium text-gray-900">Full Json
                <CopyButton value={JSON.stringify(request, null, 2)}/>
              </h3>
          </div>
        <pre className="text-sm text-gray-900 bg-gray-50 p-4 rounded overflow-x-auto">
          {JSON.stringify(request, null, 2)}
        </pre>
      </div>
  );
};

export default JsonTab;