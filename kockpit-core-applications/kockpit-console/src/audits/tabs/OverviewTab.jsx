import React from 'react';
import { Globe, Activity, Clock, Server, Package, Hash, FileText, Timer, Calendar } from 'lucide-react';
import CopyButton from "../../components/CopyButton.jsx";

function formatFriendlyDate(dateString) {
  if (!dateString) return '';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return dateString;
  return date.toLocaleString('en-US', {
    year: 'numeric',
    month: 'long',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  }).replace(',', '');
}

const getMethodColor = (method) => {
  const colors = {
    GET: 'bg-blue-100 text-blue-800 border-blue-200',
    POST: 'bg-green-100 text-green-800 border-green-200',
    PUT: 'bg-orange-100 text-orange-800 border-orange-200',
    DELETE: 'bg-red-100 text-red-800 border-red-200',
    PATCH: 'bg-purple-100 text-purple-800 border-purple-200',
  };
  return colors[method] || 'bg-gray-100 text-gray-800 border-gray-200';
};

const getStatusColor = (status) => {
  if (status >= 200 && status < 300) return 'bg-green-100 text-green-800 border-green-200';
  if (status >= 300 && status < 400) return 'bg-blue-100 text-blue-800 border-blue-200';
  if (status >= 400 && status < 500) return 'bg-yellow-100 text-yellow-800 border-yellow-200';
  if (status >= 500) return 'bg-red-100 text-red-800 border-red-200';
  return 'bg-gray-100 text-gray-800 border-gray-200';
};

const getDurationColor = (duration) => {
  if (duration < 100) return 'text-green-600';
  if (duration < 500) return 'text-blue-600';
  if (duration < 1000) return 'text-orange-600';
  return 'text-red-600';
};

const InfoCard = ({ icon: Icon, title, children }) => (
  <div className="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-shadow duration-200">
    <div className="flex items-center gap-2 mb-4">
      <Icon className="w-5 h-5 text-gray-600" />
      <h4 className="text-sm font-semibold text-gray-700 uppercase tracking-wide">{title}</h4>
    </div>
    <div className="space-y-3">
      {children}
    </div>
  </div>
);

const InfoRow = ({ label, value, badge = false, badgeColor = '' }) => (
  <div className="flex items-start justify-between gap-4">
    <span className="text-sm font-medium text-gray-500 min-w-[120px]">{label}</span>
    {badge ? (
      <span className={`px-3 py-1 rounded-full text-xs font-bold border ${badgeColor}`}>
        {value}
      </span>
    ) : (
      <span className="text-sm text-gray-900 font-medium text-right flex-1">{value}</span>
    )}
  </div>
);

const OverviewTab = ({ request }) => {
  //console.log('Full request data:', request);
  //console.log('Audits:', request?.audits);
  //console.log('Web audit events:', request['audits']?.find(audit => audit.type === 'builtin.web')?.events);
  let events = request['audits']?.find(audit => audit.type === 'builtin.web')?.events;
  let httpAuditedRequest = []
  let httpAuditedResponse = []
  if (events) {
      httpAuditedRequest = events
          .map(event => {
              if (typeof event === "string") {
                  return JSON.parse(event);
              } else {
                  return event;
              }
          })[0]['httpAuditedRequest']

      httpAuditedResponse = events
            .map(event => {
                if (typeof event === "string") {
                    return JSON.parse(event);
                } else {
                    return event;
                }
            })[0]['httpAuditedResponse']
    }

  function traceId(request) {
    return request['indexedKeyValues'] ?
        request['indexedKeyValues'].find(kv => kv.key === 'traceId')?.value || null : null;
  }

  const duration = request?.end && request?.start ? request.end - request.start : 0;
  const traceIdValue = traceId(request);

  return (
    <div className="space-y-4">
      {/* HTTP Request Summary Card */}
      <div className="bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-lg p-6">
        <div className="flex flex-wrap items-center gap-4">
          <div className="flex items-center gap-3">
            <Activity className="w-6 h-6 text-blue-600" />
            <div>
              <div className="text-xs text-gray-600 font-medium uppercase">HTTP Request</div>
              <div className="text-lg font-bold text-gray-900">{httpAuditedRequest?.uri || 'N/A'}</div>
            </div>
          </div>
          <div className="flex items-center gap-3 ml-auto">
            <span className={`px-4 py-2 rounded-full text-sm font-bold border-2 ${getMethodColor(httpAuditedRequest?.method)}`}>
              {httpAuditedRequest?.method}
            </span>
            <span className={`px-4 py-2 rounded-full text-sm font-bold border-2 ${getStatusColor(httpAuditedResponse?.status)}`}>
              {httpAuditedResponse?.status}
            </span>
            <div className="flex items-center gap-2 bg-white rounded-lg px-4 py-2 border border-gray-300">
              <Timer className={`w-4 h-4 ${getDurationColor(duration)}`} />
              <span className={`text-sm font-bold ${getDurationColor(duration)}`}>
                {duration}ms
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Grid of Info Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Environment Info */}
        <InfoCard icon={Globe} title="Environment">
          <InfoRow label="Domain" value={request?.domain || 'N/A'} />
          <InfoRow label="Environment" value={request?.env || 'N/A'} />
          <InfoRow label="Hostname" value={request?.hostname || 'N/A'} />
        </InfoCard>

        {/* Application Info */}
        <InfoCard icon={Package} title="Application">
          <InfoRow label="App ID" value={request?.appId || 'N/A'} />
          <InfoRow label="Version" value={request?.version || 'N/A'} />
          <InfoRow label="Artifact" value={request?.artifact || 'N/A'} />
        </InfoCard>

        {/* Timing Info */}
        <InfoCard icon={Clock} title="Timing">
          <InfoRow label="Start Time" value={formatFriendlyDate(request?.start)} />
          <InfoRow label="End Time" value={formatFriendlyDate(request?.end)} />
          <InfoRow label="TTL (days)" value={request?.ttl || 'N/A'} />
        </InfoCard>

        {/* Tracing Info */}
        <InfoCard icon={Hash} title="Identifiers">
          <div className="space-y-3">
            <div className="flex items-start justify-between gap-4">
              <span className="text-sm font-medium text-gray-500 min-w-[120px]">Request ID</span>
              <div className="flex items-center gap-2 flex-1 justify-end">
                <span className="text-sm text-gray-900 font-mono text-right truncate">{request?.requestId}</span>
                {request?.requestId && <CopyButton value={request.requestId} />}
              </div>
            </div>
            {traceIdValue && (
              <div className="flex items-start justify-between gap-4">
                <span className="text-sm font-medium text-gray-500 min-w-[120px]">Trace ID</span>
                <div className="flex items-center gap-2 flex-1 justify-end">
                  <span className="text-sm text-gray-900 font-mono text-right truncate">{traceIdValue}</span>
                  <CopyButton value={traceIdValue} />
                </div>
              </div>
            )}
          </div>
        </InfoCard>
      </div>
    </div>
  );
};

export default OverviewTab;
