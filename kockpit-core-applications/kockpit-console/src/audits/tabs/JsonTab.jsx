import React, { useState } from 'react';
import { Copy, Download, Check, Code2, Maximize2, Minimize2, Search } from 'lucide-react';

const JsonTab = ({ request }) => {
  const [copied, setCopied] = useState(false);
  const [expanded, setExpanded] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const jsonString = JSON.stringify(request, null, 2);

  const copyToClipboard = () => {
    navigator.clipboard.writeText(jsonString);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const downloadJson = () => {
    const blob = new Blob([jsonString], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `audit-${request?.requestId || 'data'}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const highlightJson = (json) => {
    return json
      .replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, (match) => {
        let cls = 'text-purple-600'; // number
        if (/^"/.test(match)) {
          if (/:$/.test(match)) {
            cls = 'text-blue-600 font-semibold'; // key
          } else {
            cls = 'text-green-600'; // string
          }
        } else if (/true|false/.test(match)) {
          cls = 'text-orange-600 font-semibold'; // boolean
        } else if (/null/.test(match)) {
          cls = 'text-red-600 font-semibold'; // null
        }
        return `<span class="${cls}">${match}</span>`;
      });
  };

  const highlightedJson = highlightJson(jsonString);

  const filteredJson = searchTerm
    ? jsonString
        .split('\n')
        .filter(line => line.toLowerCase().includes(searchTerm.toLowerCase()))
        .join('\n')
    : jsonString;

  const displayJson = searchTerm ? filteredJson : highlightedJson;

  return (
    <div className="bg-white shadow-sm overflow-hidden sm:rounded-lg border border-gray-200">
      {/* Header */}
      <div className="px-6 py-4 border-b border-gray-200 bg-gradient-to-r from-gray-50 to-white">
        <div className="flex items-center justify-between flex-wrap gap-4">
          <div className="flex items-center gap-3">
            <Code2 className="w-5 h-5 text-gray-600" />
            <h3 className="text-lg font-semibold text-gray-900">Raw JSON</h3>
            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
              {jsonString.length.toLocaleString()} chars
            </span>
          </div>

          <div className="flex items-center gap-2">
            {/* Search */}
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                type="text"
                placeholder="Search JSON..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-9 pr-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent w-48"
              />
            </div>

            {/* Expand/Collapse */}
            <button
              onClick={() => setExpanded(!expanded)}
              className="flex items-center gap-2 px-3 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
              title={expanded ? "Collapse" : "Expand"}
            >
              {expanded ? <Minimize2 className="w-4 h-4" /> : <Maximize2 className="w-4 h-4" />}
            </button>

            {/* Copy */}
            <button
              onClick={copyToClipboard}
              className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium"
            >
              {copied ? (
                <>
                  <Check className="w-4 h-4" />
                  <span>Copied!</span>
                </>
              ) : (
                <>
                  <Copy className="w-4 h-4" />
                  <span>Copy</span>
                </>
              )}
            </button>

            {/* Download */}
            <button
              onClick={downloadJson}
              className="flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors font-medium"
            >
              <Download className="w-4 h-4" />
              <span>Download</span>
            </button>
          </div>
        </div>
      </div>

      {/* JSON Content */}
      <div className={`p-6 bg-gray-900 overflow-auto ${expanded ? 'max-h-screen' : 'max-h-[600px]'}`}>
        <pre className="text-sm font-mono leading-relaxed">
          <code dangerouslySetInnerHTML={{ __html: displayJson }} />
        </pre>
      </div>

      {/* Footer */}
      {searchTerm && (
        <div className="px-6 py-3 bg-yellow-50 border-t border-yellow-200">
          <div className="flex items-center gap-2">
            <Search className="w-4 h-4 text-yellow-600" />
            <span className="text-sm text-yellow-800">
              Filtering by: <span className="font-semibold">"{searchTerm}"</span>
            </span>
            <button
              onClick={() => setSearchTerm('')}
              className="ml-auto text-xs text-yellow-600 hover:text-yellow-800 font-medium"
            >
              Clear filter
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default JsonTab;