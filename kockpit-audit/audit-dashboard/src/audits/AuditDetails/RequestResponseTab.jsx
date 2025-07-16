import React from 'react';
import CopyButton from "../components/CopyButton.jsx";

function renderHeadersTable(headers) {
  if (!headers) return <div className="text-gray-400">No headers</div>;
  if (typeof headers === 'string') {
    headers = JSON.parse(headers);
  }
  const entries = Object.entries(headers);
  if (entries.length === 0) return <div className="text-gray-400">No headers</div>;
  return (
    <table className="min-w-full border text-sm bg-gray-50 rounded">
      <thead>
        <tr>
          <th className="border px-3 py-1 text-left font-semibold">Header Key</th>
          <th className="border px-3 py-1 text-left font-semibold">Header Value</th>
        </tr>
      </thead>
      <tbody>
        {entries.map(([key, value]) => (
          <tr key={key}>
            <td className="border px-3 py-1 font-mono text-xs text-gray-700">{key}</td>
            <td className="border px-3 py-1 text-gray-900">{Array.isArray(value) ? value.join(', ') : value}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

function prettyPrintBody(body) {
  if (!body) return '';
  try {
    const json = typeof body === 'string' ? JSON.parse(body) : body;
    return JSON.stringify(json, null, 2);
  } catch {
    return body;
  }
}

const RequestResponseTab = ({ request }) => {
  let events = request['audits']?.find(audit => audit.type === 'builtin.web')?.events;
  let httpAuditedRequest = events
      .map(event => {
        if (typeof event === "string") {
          return JSON.parse(event);
        } else {
          return event;
        }
      })[0]['httpAuditedRequest']

  let httpAuditedResponse = events
      .map(event => {
        if (typeof event === "string") {
          return JSON.parse(event);
        } else {
          return event;
        }
      })[0]['httpAuditedResponse']

  console.log(httpAuditedResponse);

  return (
    <div className="space-y-6">
      <div className="bg-white shadow overflow-hidden sm:rounded-lg">
        <div className="px-4 py-5 sm:px-6 bg-blue-50">
          <h3 className="text-lg leading-6 font-medium text-blue-900">Request</h3>
        </div>
        <div className="px-4 py-5 sm:p-6">
          <div className="mb-4">
            <h4 className="text-sm font-medium text-gray-500">Headers</h4>
            {renderHeadersTable(httpAuditedRequest?.headers)}
          </div>
          <div className="mb-4">
            <h4 className="text-sm font-medium text-gray-500">Body <CopyButton value={httpAuditedRequest?.body}/>
            </h4>
            <pre className="mt-1 text-sm text-gray-900 bg-gray-50 p-2 rounded overflow-x-auto">
              {prettyPrintBody(httpAuditedRequest?.body)}
            </pre>
          </div>
        </div>
      </div>

      <div className="bg-white shadow overflow-hidden sm:rounded-lg">
        <div className="px-4 py-5 sm:px-6 bg-green-50">
          <h3 className="text-lg leading-6 font-medium text-green-900">Response</h3>
        </div>
        <div className="px-4 py-5 sm:p-6">
          <div className="mb-4">
            <h4 className="text-sm font-medium text-gray-500">Status</h4>
            <div className="mt-1 text-sm text-gray-900">{httpAuditedResponse?.status}</div>
          </div>
          <div className="mb-4">
            <h4 className="text-sm font-medium text-gray-500">Headers</h4>
            {renderHeadersTable(httpAuditedResponse?.headers)}
          </div>
          <div>
            <h4 className="text-sm font-medium text-gray-500">Payload</h4>
            <pre className="mt-1 text-sm text-gray-900 bg-gray-50 p-2 rounded overflow-x-auto">
              {prettyPrintBody(httpAuditedResponse?.payload)}
            </pre>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RequestResponseTab;