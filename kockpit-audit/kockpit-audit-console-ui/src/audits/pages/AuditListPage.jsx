import React, {useEffect, useRef, useState} from 'react';
import {fetchAuditReportsWithPaging, searchAudits} from '../../services/api.js';
import {CheckIcon, ClipboardDocumentIcon, EyeIcon} from '@heroicons/react/24/outline';
import {AdjustmentsHorizontalIcon, MagnifyingGlassIcon} from '@heroicons/react/20/solid';
import {useNavigate} from 'react-router-dom';
import StatusBadge from '../../components/StatusBadge.jsx';
import TruncateWithTooltip from "../../components/TruncateWithTooltip.jsx";
import Pagination from '../../components/Pagination.jsx';

const ALL_COLUMNS = [
  { key: 'domain', label: 'Domain' },
  { key: 'env', label: 'Env' },
  { key: 'appId', label: 'App ID' },
  { key: 'requestId', label: 'Request ID' },
  { key: 'method', label: 'Method' },
  { key: 'path', label: 'Path' },
  { key: 'duration', label: 'Duration' },
  { key: 'status', label: 'Status' },
  { key: 'hostname', label: 'Hostname' },
  { key: 'version', label: 'Version' },
  { key: 'artifact', label: 'Artifact' },
  { key: 'start', label: 'Start' },
  { key: 'end', label: 'End' },
];

function AuditListPage({ domain, env }) {
  const [loading, setLoading] = useState(true);
  const [audits, setAudits] = useState([]);
  const [showColumns, setShowColumns] = useState([
    'appId', 'requestId', 'method', 'path', 'duration', 'start', 'status',
  ]);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [httpMethodFilter, setHttpMethodFilter] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(25);
  const [totalCount, setTotalCount] = useState(0);
  const [statusOptions, setStatusOptions] = useState([]);
  const [httpMethodOptions, setHttpMethodOptions] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    console.log("use effect1");
    loadAll(domain, env);
  }, [domain, env, currentPage, itemsPerPage]);

  function loadAll(domain, env) {
    console.log("loadAll")
    setLoading(true);
    if (search) {
      doSearchAudits();
    } else {
      fetchAuditReportsWithPaging(domain, env, itemsPerPage, (currentPage - 1) * itemsPerPage)
          .then((data) => {
            setAudits(data.items);
            setTotalCount(data.total_count);
//          setItemsPerPage(data.size);
            setLoading(false);
          });
    }
  }

  useEffect(() => {
    setStatusOptions(getUniqueValues('httpStatus', true));
    setHttpMethodOptions(getUniqueValues('httpMethod', true));
  }, [audits]);

  const getUniqueValues = (key, fromIndexed) => {
    if (fromIndexed) {
      const values = audits.flatMap(audit => (audit.indexedKeyValues || [])
          .filter(kv => kv.key === key)
          .map(kv => kv.value));
      return Array.from(new Set(values)).filter(Boolean);
    } else {
      return Array.from(new Set(audits.map(audit => audit[key]))).filter(Boolean);
    }
  };

  const handleViewDetails = (audit) => {
    navigate(`/audits/${audit.id}`);
  };

  if (loading) return <div>Loading...</div>;

  function fetchIndexedValue(audit, key) {
    if (audit.indexedKeyValues) {
      const found = audit.indexedKeyValues.find(kv => kv.key === key);
      return found ? found.value : undefined;
    }
  }

  function getHttStatus(audit) {
    return fetchIndexedValue(audit, 'httpStatus');
  }

  function duration(audit) {
    return audit.end - audit.start;
  }

  function dateFormat(start) {
    return new Date(start).toLocaleString();
  }

  function getPath(audit) {
    return fetchIndexedValue(audit, 'requestUri');
  }

  function getMethod(audit) {
    return fetchIndexedValue(audit, 'httpMethod');
  }

  const fetchPage = (page, pageSize) => {
    console.info("fetchPage");
    setLoading(true);
    if (search) {
      doSearchAudits();
    } else {
      fetchAuditReportsWithPaging(domain, env, pageSize, (page - 1) * pageSize).then((data) => {
        setAudits(data.items);
        setLoading(false);
      });
    }
  };

  const handlePageChange = (page, size) => {
    setItemsPerPage(size);
    setCurrentPage(page);
  };

  function doSearchAudits() {
    if (!search || search.trim().length <= 0) return;
    setLoading(true);
    searchAudits(search, domain, env, itemsPerPage, (currentPage - 1) * itemsPerPage).then(data => {
      setAudits(data.items);
      setTotalCount(data.total_count);
      //setItemsPerPage(data.size);
      setLoading(false);
    });
  }

  function CopyButton({ value, className = '' }) {
    const [copied, setCopied] = useState(false);
    const handleCopy = async () => {
      if (!value) return;
      try {
        await navigator.clipboard.writeText(value);
        setCopied(true);
        setTimeout(() => setCopied(false), 1200);
      } catch (err) {
        console.error('Failed to copy:', err);
      }
    };
    if (!value) return null;
    return (
        <button
            type="button"
            onClick={handleCopy}
            className={`ml-2 p-1 rounded-md hover:bg-gray-100 transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-gray-300 ${className}`}
            title="Copy to clipboard"
            aria-label="Copy to clipboard"
            disabled={copied}
        >
          {copied ? (
              <CheckIcon className="h-4 w-4 text-green-500" />
          ) : (
              <ClipboardDocumentIcon className="h-4 w-4 text-gray-500 hover:text-gray-700" />
          )}
        </button>
    );
  }

  return (
      <div className="px-2 py-2 sm:px-2 lg:px-2 bg-slate-50 min-h-screen">
        <div className="flex items-center mb-5">
          <div className="h-10 w-1 rounded bg-blue-600 mr-4" />
          <div>
            <h1 className="text-3xl font-bold text-slate-800 tracking-tight">Audits</h1>
            <p className="mt-1 text-base text-slate-500">Track and analyze system activities</p>
          </div>
        </div>
        <div className="mb-8">
          <div className="bg-white rounded-2xl shadow-lg px-8 py-6 flex flex-col gap-6 border-l-4 border-blue-600/20 border-slate-100">
            <div className="flex flex-col sm:flex-row sm:items-center gap-4">
              <div className="relative flex-1">
              <span className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <MagnifyingGlassIcon className="h-5 w-5 text-slate-400" />
              </span>
                <input
                    type="text"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    placeholder="Search everything..."
                    className="block w-full pl-10 pr-24 py-3 rounded-xl border border-slate-200 bg-slate-50 text-slate-800 placeholder-slate-400 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-base transition-all shadow-sm"
                />
                <button
                    onClick={doSearchAudits}
                    className="absolute right-2 top-1/2 -translate-y-1/2 px-5 py-2 rounded-lg bg-blue-600 text-white font-semibold shadow hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-400 transition-all"
                >
                  Search
                </button>
              </div>
            </div>
            <div className="flex flex-wrap gap-4 items-center mt-2">
            <span className="inline-flex items-center px-2 py-1 bg-blue-50 text-blue-600 rounded-md text-xs font-medium mr-2">
              <AdjustmentsHorizontalIcon className="h-4 w-4 mr-1" /> Filters
            </span>
              <select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                  className="min-w-[120px] rounded-lg border border-slate-200 py-2 pl-3 pr-8 text-sm bg-white text-slate-700 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 shadow-sm"
              >
                <option value="">All Statuses</option>
                {statusOptions.map(opt => (
                    <option key={opt} value={opt}>{opt}</option>
                ))}
              </select>
              <select
                  value={httpMethodFilter}
                  onChange={(e) => setHttpMethodFilter(e.target.value)}
                  className="min-w-[120px] rounded-lg border border-slate-200 py-2 pl-3 pr-8 text-sm bg-white text-slate-700 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 shadow-sm"
              >
                <option value="">All Methods</option>
                {httpMethodOptions.map(opt => (
                    <option key={opt} value={opt}>{opt}</option>
                ))}
              </select>
            </div>
          </div>
        </div>
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6 gap-4">
            <Pagination
                currentPage={currentPage}
                totalPages={Math.ceil(totalCount / itemsPerPage)}
                onPageChange={handlePageChange}
                itemsPerPage={itemsPerPage}
                totalItems={totalCount}
            />
        </div>
        <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-lg">
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50">
            <tr>
              {ALL_COLUMNS.filter(col => showColumns.includes(col.key)).map(col => (
                  <th
                      key={col.key}
                      scope="col"
                      className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider"
                  >
                    {col.label}
                  </th>
              ))}
              <th scope="col" className="relative px-6 py-3">
                <span className="sr-only">Actions</span>
              </th>
            </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
            {audits.map(audit => (
                <tr key={audit.id} className="hover:bg-blue-50/60 transition-colors">
                  {ALL_COLUMNS.filter(col => showColumns.includes(col.key)).map(col => (
                      <td key={col.key} className="whitespace-nowrap px-6 py-4 text-sm">
                        {col.key === 'requestId' && audit[col.key] ? (
                            <div className="flex items-center">
                              <TruncateWithTooltip text={audit[col.key]} maxLength={12} />
                              <CopyButton value={audit[col.key]} />
                            </div>
                        ) : col.key === 'path' ? (
                            <div className="flex items-center">
                              <TruncateWithTooltip text={getPath(audit)} maxLength={30} />
                              <CopyButton value={getPath(audit)} />
                            </div>
                        ) : col.key === 'status' ? (
                            <StatusBadge status={getHttStatus(audit)} />
                        ) : col.key === 'method' ? (
                            <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                                getMethod(audit) === 'GET'
                                    ? 'bg-green-100 text-green-800'
                                    : getMethod(audit) === 'POST'
                                        ? 'bg-blue-100 text-blue-800'
                                        : 'bg-purple-100 text-purple-800'
                            }`}>
                        {getMethod(audit)}
                      </span>
                        ) : col.key === 'duration' ? (
                            <span className="font-mono">{duration(audit)}ms</span>
                        ) : col.key === 'start' ? (
                            <span className="text-slate-600">{dateFormat(audit.start)}</span>
                        ) : (
                            <span className="text-slate-800">{audit[col.key]}</span>
                        )}
                      </td>
                  ))}
                  <td className="whitespace-nowrap px-6 py-4 text-right text-sm font-medium">
                    <button
                        onClick={() => handleViewDetails(audit)}
                        className="text-blue-600 hover:text-blue-800 flex items-center space-x-1 font-semibold"
                    >
                      <EyeIcon className="h-4 w-4" />
                      <span>View</span>
                    </button>
                  </td>
                </tr>
            ))}
            </tbody>
          </table>
        </div>
        {/* paginnation */}
        <div className="mt-8 flex justify-end">
          <Pagination
              currentPage={currentPage}
              totalPages={Math.ceil(totalCount / itemsPerPage)}
              onPageChange={handlePageChange}
              itemsPerPage={itemsPerPage}
              totalItems={totalCount}
          />
        </div>
      </div>
  );
}

export default AuditListPage;