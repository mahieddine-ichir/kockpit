import React, {useEffect, useRef, useState} from 'react';
import {fetchAuditReportsWithPaging, searchAudits} from '../../services/api.js';
import {ClipboardDocumentIcon, EyeIcon, CheckIcon, ChartPieIcon, ListBulletIcon} from '@heroicons/react/24/outline';
import {AdjustmentsHorizontalIcon, MagnifyingGlassIcon} from '@heroicons/react/20/solid';
import {useNavigate, useSearchParams} from 'react-router-dom';
import StatusBadge from '../../components/StatusBadge.jsx';
import TruncateWithTooltip from "../../components/TruncateWithTooltip.jsx";
import Pagination from '../../components/Pagination.jsx';
import Dashboard from './Dashboard.jsx'
import { getHttStatus, getMethod, getPath, dateFormat} from '../tabs/auditUtils.js'


function formatLabel(col) {
  let label = '';
  for (let i = 0; i < col.length; i++) {
    if (col[i].match(/[A-Z]/) != null) {
      label += ' ' + col[i];
    } else {
      label += col[i];
    }
  }
  return label.trim();
}

function DomainEnv({options, filter, onchange}) {
  return (
      <div className="flex">
        <div className="mb-4 flex items-center">
          <label className="block text-sm font-medium text-gray-700 mr-2">Domain / Env:</label>
          <select
              value={filter}
              onChange={e=> onchange(e.target.value)}
              className="rounded border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-200 focus:outline-none sm:text-sm bg-white"
          >
            <option value="">All</option>
            {options.map(option => (
                <option key={`${option.domain}-${option.env}`}>{option.domain} / {option.env}</option>
            ))}
          </select>
        </div>
      </div>
  )
}

const AuditListPage = ({ domain, env, config }) => {
  const [audits, setAudits] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showColumns, setShowColumns] = useState(() => {
    const saved = localStorage.getItem('selectedcolumns');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch (e) {
        console.error('Error parsing saved columns', e);
      }
    }
    if (config) {
      const auditService = config.services.find(s => s.name === 'audit');
      if (auditService) {
        return auditService.config.columns;
      }
    }
    return [];
  });
  const [showDropdown, setShowDropdown] = useState(false);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [httpMethodFilter, setHttpMethodFilter] = useState('');
  const [columns, setColumns] = useState([]);

  const [domainEnvFilter, setDomainEnvFilter] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(25);
  const [totalCount, setTotalCount] = useState(0);
  // const [allColumns, setAllColumns] = useState([]);
  // const [defaultColumns, setDefaultColumns] = useState([]);
  const dropdownRef = useRef(null);
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const [showKengineOnly, setShowKengineOnly] = useState(false);
  const [activeTab, setActiveTab] = useState('dashboard');


  useEffect(() => {
    if (config && config.services) {
      const auditService = config.services.find(s => s.name === 'audit');
      if (
          auditService &&
          auditService.config &&
          Array.isArray(auditService.config.columns) &&
          auditService.config.columns.length > 0
      ) {
        const cols = auditService.config.columns.map(col => ({
          key: col,
          label: formatLabel(col)
        }));
        setColumns(cols);
        setShowColumns(auditService.config.columns);
        return;
      }
    }
    const fallbackCols = [
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
    setColumns(fallbackCols);
    setShowColumns(fallbackCols.map(c => c.key));
  }, [config]);

  useEffect(() => {
    setSearch(searchParams.get('search') || '');
    setStatusFilter(searchParams.get('status') || '');
    setHttpMethodFilter(searchParams.get('httpMethod') || '');
    setDomainEnvFilter(searchParams.get('domainEnv') || '');
  }, []);




  const updateSearchParams = (params) => {
    const newParams = new URLSearchParams(searchParams);
    Object.entries(params).forEach(([key, value]) => {
      if (value) {
        newParams.set(key, value);
      } else {
        newParams.delete(key);
      }
    });
    setSearchParams(newParams, { replace: true });
  };


  useEffect(() => {
    updateSearchParams({
      search,
      status: statusFilter,
      httpMethod: httpMethodFilter,
      domainEnv: domainEnvFilter,
    });
  }, [search, statusFilter, httpMethodFilter, domainEnvFilter]);

  function loadAll() {
    console.log("load All data");
    const currentDomain = domain || '';
    const currentEnv = env || '';

    fetchAuditReportsWithPaging(currentDomain, currentEnv, itemsPerPage, (currentPage - 1) * itemsPerPage)
        .then((data) => {
          setAudits(data.items);
          setTotalCount(data.total_count);
          setItemsPerPage(data.size);
          setLoading(false);
        })
        .catch((error) => {
          console.error('Error loading audits:', error);
          setLoading(false);
        });
  }

  useEffect(() => {
    loadAll();
  }, [currentPage, itemsPerPage, domain, env]);

  useEffect(() => {
    localStorage.setItem('selectedcolumns', JSON.stringify(showColumns));
  }, [showColumns]);


  function getDomainEnvOptions(audits) {
    const options = [];
    audits.forEach(audit => {
      const domain = audit.domain || '';
      const env = audit.env || '';
      const domainenv = `${domain} / ${env}`;

      if (!options.includes(domainenv)) {
        options.push(domainenv);
      }
    });

    return options.filter(option => option !== ' / ');
  }

  const getUniqueValues = (key, fromIndexed) => {
    if (fromIndexed) {
      const values = audits.flatMap(audit => (audit.indexedKeyValues || []).filter(kv => kv.key === key).map(kv => kv.value));
      return Array.from(new Set(values)).filter(Boolean);
    } else {
      return Array.from(new Set(audits.map(audit => audit[key]))).filter(Boolean);
    }
  };

  function matchesSearch(audit, searchTerm) {
    if (!searchTerm) return true;
    const lower = searchTerm.toLowerCase();

    for (const key in audit) {
      if (typeof audit[key] === 'string' && audit[key].toLowerCase().includes(lower)) return true;
      if (typeof audit[key] === 'number' && audit[key].toString().includes(lower)) return true;
    }
    if (audit.indexedKeyValues) {

      for (const kv of audit.indexedKeyValues) {
        if ((kv.key && kv.key.toLowerCase().includes(lower)) || (kv.value && kv.value.toLowerCase().includes(lower))) return true;
      }
    }
    return false;
  }

  useEffect(() => {
    let filtered = audits.filter(audit => {
      if (domainEnvFilter) {
        const domain = audit.domain || '';
        const env = audit.env || '';
        const domainEnv = `${domain} / ${env}`;

        if (domainEnv !== domainEnvFilter) {
          return false;
        }
      }
      if (!matchesSearch(audit, search)) return false;
      if (statusFilter && getHttStatus(audit) !== statusFilter) return false;
      if (httpMethodFilter && getMethod(audit) !== httpMethodFilter) return false;
      return true;
    });
    setAudits(filtered);
  }, [statusFilter, httpMethodFilter, domainEnvFilter]);

  useEffect(() => {
    function handleClickOutside(event) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setShowDropdown(false);
      }
    }
    if (showDropdown) {
      document.addEventListener('mousedown', handleClickOutside);
    } else {
      document.removeEventListener('mousedown', handleClickOutside);
    }
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [showDropdown]);

  const handleViewDetails = (audit) => {
    navigate(`/audits/${audit.id}`);
  };

  const handleColumnToggle = (key) => {
    setShowColumns(cols =>
        cols.includes(key) ? cols.filter(col => col !== key) : [...cols, key]
    );
  };

  if (loading) return <div>Loading...</div>;

  const statusOptions = getUniqueValues('httpStatus', true);
  const httpMethodOptions = getUniqueValues('httpMethod', true);
  const domainEnvOptions = getDomainEnvOptions(audits);

  const fetchPage = (page, pageSize) => {
    setLoading(true);
    const currentDomain = domain || '';
    const currentEnv = env || '';

    if (search) {
      doSearchAudits();
    } else {
      fetchAuditReportsWithPaging(currentDomain, currentEnv, pageSize, pageSize * (page - 1)).then((data) => {
        setAudits(data.items);
        setLoading(false);
      }).catch((error) => {
        console.error('Error fetching page:', error);
        setLoading(false);
      });
    }
  };

  const handlePageChange = (page, size) => {
    setItemsPerPage(size);
    setCurrentPage(page);
    fetchPage(page, size);
  };

  const totalPages = Math.ceil(totalCount / itemsPerPage);

  function doSearchAudits() {
    if (!search || search.trim().length <= 0) {
      return;
    }
    setLoading(true);
    const currentDomain = domain || '';
    const currentEnv = env || '';

    searchAudits(search, currentDomain, currentEnv, itemsPerPage, itemsPerPage * (currentPage - 1)).then(data => {
      setAudits(data.items);
      setTotalCount(data.total_count);
      setItemsPerPage(data.size);
      setLoading(false);
    }).catch((error) => {
      console.error('Error searching audits:', error);
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

  const filteredAudits = showKengineOnly
      ? audits.filter(audit =>
          Array.isArray(audit.audits) &&
          audit.audits.some(a => a.type === "kengine.flows")
      )
      : audits;

  if (!columns || columns.length === 0) {
    return <div>No columns configured for this environment.</div>;
  }

  return (
      <div className="px-4 py-8 sm:px-8 lg:px-16 bg-slate-50 min-h-screen">
        <div className="flex items-center mb-10">
          <div className="h-10 w-1 rounded bg-blue-600 mr-4" />
          <div>
            <h1 className="text-3xl font-bold text-slate-800 tracking-tight">Audit Logs</h1>
            <p className="mt-1 text-base text-slate-500">Track and analyze system activities</p>
          </div>
        </div>
        <div className="mb-8">
          <div className="border-b border-gray-200">
            <nav className="-mb-px flex space-x-8">
              <button
                  onClick={() => setActiveTab('dashboard')}
                  className={`py-2 px-1 border-b-2 font-medium text-sm transition-colors duration-200 ${
                      activeTab === 'dashboard'
                          ? 'border-blue-500 text-blue-600'
                          : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }`}
              >
                <div className="flex items-center space-x-2">
                  <ChartPieIcon className="h-5 w-5" />
                  <span>Dashboard</span>
                </div>
              </button>
              <button
                  onClick={() => setActiveTab('list')}
                  className={`py-2 px-1 border-b-2 font-medium text-sm transition-colors duration-200 ${
                      activeTab === 'list'
                          ? 'border-blue-500 text-blue-600'
                          : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }`}
              >
                <div className="flex items-center space-x-2">
                  <ListBulletIcon className="h-5 w-5" />
                  <span>List Audits</span>
                </div>
              </button>
            </nav>
          </div>
        </div>
        {activeTab === 'dashboard' ? (
            <Dashboard  audits={audits}
                        getHttStatus={getHttStatus}
                        getMethod={getMethod}
            />
        ) : (
            <>
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
                          placeholder="Search audits..."
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
                        value={domainEnvFilter}
                        onChange={(e) => setDomainEnvFilter(e.target.value)}
                        className="min-w-[160px] rounded-lg border border-slate-200 py-2 pl-3 pr-8 text-sm bg-white text-slate-700 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 shadow-sm"
                    >
                      <option value="">All Domains</option>
                      {domainEnvOptions.map(option => (
                          <option key={option} value={option}>{option}</option>
                      ))}
                    </select>
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
                    <button
                        onClick={() => setShowKengineOnly(!showKengineOnly)}
                        className={`ml-2 px-3 py-2 rounded-md text-sm font-medium border transition-colors duration-150 ${
                            showKengineOnly
                                ? 'bg-blue-50 text-blue-700 border-blue-200'
                                : 'bg-slate-100 text-slate-700 border-slate-200 hover:bg-slate-200'
                        }`}
                    >
                      {showKengineOnly ? 'All Logs' : 'kengine Only'}
                    </button>
                  </div>
                </div>
              </div>
              <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6 gap-4">
                <div className="text-sm text-slate-600">
                  Showing <span className="font-semibold text-blue-700">{filteredAudits.length}</span> of{' '}
                  <span className="font-semibold text-blue-700">{totalCount}</span> results
                </div>
                <div className="flex items-center space-x-3">
                  <div className="relative" ref={dropdownRef}>
                    <button
                        onClick={() => setShowDropdown(!showDropdown)}
                        className="flex items-center space-x-1 px-3 py-2 border border-slate-200 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-100"
                    >
                      <AdjustmentsHorizontalIcon className="h-4 w-4" />
                      <span>Columns</span>
                    </button>
                    {showDropdown && (
                        <div className="absolute right-0 z-10 mt-2 w-56 origin-top-right rounded-lg bg-white shadow-lg ring-1 ring-black ring-opacity-5 p-3">
                          <p className="text-sm font-medium text-slate-700 mb-2">Visible Columns</p>
                          <div className="space-y-2">
                            {columns.map(col => (
                                <label key={col.key} className="flex items-center space-x-2">
                                  <input
                                      type="checkbox"
                                      checked={showColumns.includes(col.key)}
                                      onChange={() => handleColumnToggle(col.key)}
                                      className="h-4 w-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                                  />
                                  <span className="text-sm text-slate-700">{col.label}</span>
                                </label>
                            ))}
                          </div>
                        </div>
                    )}
                  </div>
                  <Pagination
                      currentPage={currentPage}
                      totalPages={totalPages}
                      onPageChange={handlePageChange}
                      itemsPerPage={itemsPerPage}
                      totalItems={totalCount}
                  />
                </div>
              </div>
              <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-lg">
                <table className="min-w-full divide-y divide-slate-200">
                  <thead className="bg-slate-50">
                  <tr>
                    {columns.filter(col => showColumns.includes(col.key)).map(col => (
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
                  {filteredAudits.slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage).map(audit => (
                      <tr key={audit.id} className="hover:bg-blue-50/60 transition-colors">
                        {columns.filter(col => showColumns.includes(col.key)).map(col => (
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
                                              : getMethod(audit) === 'PUT'
                                                  ? 'bg-yellow-100 text-yellow-800'
                                                  : getMethod(audit) === 'DELETE'
                                                      ? 'bg-red-100 text-red-800'
                                                      : 'bg-purple-100 text-purple-800'
                                  }`}>
                              {getMethod(audit)}
                            </span>
                              ) : col.key === 'duration' ? (
                                  <span className="font-mono">{audit.end - audit.start}ms</span>
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
              <div className="mt-8 flex justify-end">
                <Pagination
                    currentPage={currentPage}
                    totalPages={totalPages}
                    onPageChange={handlePageChange}
                    itemsPerPage={itemsPerPage}
                    totalItems={totalCount}
                />
              </div>
            </>
        )}
      </div>
  );
};

export default AuditListPage;