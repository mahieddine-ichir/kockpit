import React, { useState } from 'react';
import { Hash, Type, Calendar, Tag, Copy, Check } from 'lucide-react';

const KeyValueTab = ({ request }) => {
  const [copiedIndex, setCopiedIndex] = useState(null);

  const getValueType = (kv) => {
    if (kv.valueInteger !== null && kv.valueInteger !== undefined) return 'integer';
    if (kv.valueFloat !== null && kv.valueFloat !== undefined) return 'float';
    if (kv.valueDate) return 'date';
    return 'string';
  };

  const getValue = (kv) => {
    if (kv.valueInteger !== null && kv.valueInteger !== undefined) return kv.valueInteger;
    if (kv.valueFloat !== null && kv.valueFloat !== undefined) return kv.valueFloat;
    if (kv.valueDate) return new Date(kv.valueDate).toLocaleString();
    return kv.value || 'N/A';
  };

  const getTypeIcon = (type) => {
    switch (type) {
      case 'integer':
        return <Hash className="w-4 h-4" />;
      case 'float':
        return <Hash className="w-4 h-4" />;
      case 'date':
        return <Calendar className="w-4 h-4" />;
      default:
        return <Type className="w-4 h-4" />;
    }
  };

  const getTypeColor = (type) => {
    switch (type) {
      case 'integer':
        return 'bg-blue-50 border-blue-200 text-blue-700';
      case 'float':
        return 'bg-purple-50 border-purple-200 text-purple-700';
      case 'date':
        return 'bg-green-50 border-green-200 text-green-700';
      default:
        return 'bg-gray-50 border-gray-200 text-gray-700';
    }
  };

  const copyToClipboard = (text, index) => {
    navigator.clipboard.writeText(text);
    setCopiedIndex(index);
    setTimeout(() => setCopiedIndex(null), 2000);
  };

  return (
    <div className="bg-white shadow-sm overflow-hidden sm:rounded-lg border border-gray-200">
      <div className="px-6 py-4 border-b border-gray-200 bg-gradient-to-r from-gray-50 to-white">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Tag className="w-5 h-5 text-gray-600" />
            <h3 className="text-lg font-semibold text-gray-900">Indexed Key-Value Pairs</h3>
          </div>
          {request?.indexedKeyValues && request.indexedKeyValues.length > 0 && (
            <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
              {request.indexedKeyValues.length} {request.indexedKeyValues.length === 1 ? 'pair' : 'pairs'}
            </span>
          )}
        </div>
      </div>
      <div className="p-6">
        {request?.indexedKeyValues && request.indexedKeyValues.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {request.indexedKeyValues.map((kv, index) => {
              const type = getValueType(kv);
              const value = getValue(kv);
              const typeColor = getTypeColor(type);

              return (
                <div
                  key={index}
                  className={`group relative border-2 ${typeColor} rounded-lg p-4 transition-all duration-200 hover:shadow-md hover:scale-[1.02]`}
                >
                  <div className="flex items-start justify-between gap-2 mb-2">
                    <div className="flex items-center gap-2 min-w-0 flex-1">
                      <div className="flex-shrink-0">
                        {getTypeIcon(type)}
                      </div>
                      <div className="text-xs font-semibold uppercase tracking-wide truncate">
                        {kv.key}
                      </div>
                    </div>
                    <button
                      onClick={() => copyToClipboard(value.toString(), index)}
                      className="flex-shrink-0 opacity-0 group-hover:opacity-100 transition-opacity p-1 hover:bg-white/50 rounded"
                      title="Copy value"
                    >
                      {copiedIndex === index ? (
                        <Check className="w-3 h-3 text-green-600" />
                      ) : (
                        <Copy className="w-3 h-3" />
                      )}
                    </button>
                  </div>
                  <div className="text-base font-mono font-semibold break-all">
                    {value}
                  </div>
                  <div className="mt-2 text-xs opacity-60 uppercase tracking-wider">
                    {type}
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="text-center py-12">
            <Tag className="w-12 h-12 text-gray-300 mx-auto mb-3" />
            <p className="text-sm text-gray-500 font-medium">No key-value pairs available</p>
            <p className="text-xs text-gray-400 mt-1">Indexed values will appear here when available</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default KeyValueTab;