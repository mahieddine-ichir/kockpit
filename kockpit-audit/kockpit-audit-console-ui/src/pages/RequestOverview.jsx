import React, {useEffect, useRef, useState} from 'react';
import {fetchAuditReportsWithPaging, getConfig, searchAudits} from '../services/api';
import Sidebar from '../components/Sidebar/Sidebar';
import {CheckIcon, ClipboardDocumentIcon, EyeIcon} from '@heroicons/react/24/outline';
import {AdjustmentsHorizontalIcon, MagnifyingGlassCircleIcon} from '@heroicons/react/20/solid';
import {useNavigate, useSearchParams} from 'react-router-dom';
import StatusBadge from '../components/RequestOverview/StatusBadge.jsx';
import TruncateWithTooltip from "../components/TruncateWithTooltip.jsx";
import Pagination from '../components/Pagination/Pagination.jsx';

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

function getColumns() {
  //const saved = null; // fixme localStorage.getItem('selected_columns');
  /*if (saved !== null) {
  */ // return JSON.parse(saved);
//  } else {

  let defaultConfig = getConfig()
      .find(value => value.domain === 'default');
  let audit = defaultConfig.services
      .find(service => service.name === 'audit');

  console.log(audit.config.columns);
  return audit.config.columns;
//  }
}

function getDomainEnvConfig() {
  return getConfig()
      .filter(value => value.domain !== 'default')
      .map(value => {
        return {
          env: value.env,
          domain: value.domain
        };
      });
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
                <option key={option} value={option}>{option}</option>
            ))}
          </select>
        </div>
      </div>
  )
}

const RequestOverview = () => {
  const [audits, setAudits] = useState([]);
  const [loading, setLoading] = useState(true);

  const [showColumns, setShowColumns] = useState(() => {
    return getColumns()
  });

  const [showDropdown, setShowDropdown] = useState(false);
  const [collapsed, setCollapsed] = useState(false);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [httpMethodFilter, setHttpMethodFilter] = useState('');

  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(25);
  const [totalCount, setTotalCount] = useState(0);

  const dropdownRef = useRef(null);
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  // Domain Env management
  // fixme get from backend
  const [domainEnvFilter, setDomainEnvFilter] = useState('');
  const [config, setConfig] = useState(getDomainEnvConfig());

  const [selectedDomain, setSelectedDomain] = useState('');
  const [selectedEnv, setSelectedEnv] = useState('');

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
    let domain = selectedDomain;
    let env = selectedEnv;
    if (selectedDomain.length === 0 || selectedEnv.length === 0) {
      const defaultConfig = getDomainEnvConfig()[0];
      domain = defaultConfig.domain;
      env = defaultConfig.env;
    }
    console.log(`loading for ${domain} / ${env}`);
    fetchAuditReportsWithPaging(domain, env, itemsPerPage, currentPage * itemsPerPage)
        .then((data) => {
          setAudits(data.items);
          setTotalCount(data.total_count);
          setItemsPerPage(data.size);
          setLoading(false);
        });
  }

  useEffect(() => {
    loadAll();
  }, []);

  /*
  useEffect(() => {
    localStorage.setItem('selected_columns', JSON.stringify(showColumns));
  }, [showColumns]);
   */


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
    loadAll();
  }, [selectedDomain, selectedEnv]);

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

  const statusOptions = getUniqueValues('httpStatus', true);
  const httpMethodOptions = getUniqueValues('httpMethod', true);

  const fetchPage = (page, pageSize) => {
    setLoading(true);
    fetchAuditReportsWithPaging(selectedDomain, selectedEnv, pageSize, pageSize * page).then((data) => {
      setAudits(data.items);
      setLoading(false);
    });
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
    searchAudits(search).then(data => {
      setAudits(data.items);
      setTotalCount(data.total_count);
      setItemsPerPage(data.size);
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

  function domainEnvChanged(e) {
    let value = e.target.value;
    let domain = value.substring(0, value.indexOf("/")).trim();
    let env = value.substring(value.indexOf("/")+1).trim();

    if (domain !== selectedDomain) {
      setSelectedDomain(domain);
    }
    if (env !== selectedEnv) {
      setSelectedEnv(env);
    }
  }

  return (
      <div className="flex">
        <Sidebar collapsed={collapsed} setCollapsed={setCollapsed} />
        <div className={`${collapsed ? 'ml-16' : 'ml-64'} p-6 w-full transition-all duration-300`}>
          <h1 className="text-2xl font-bold mb-6">Audits</h1>


          <div className="border border-gray-300 rounded-xl bg-gray-50 p-6 mb-8 shadow-sm">
            <div className="flex flex-col md:flex-row md:items-end gap-4 mb-4">
              <div className="flex-1 flex items-center">
                <input
                    type="text"
                    value={search}
                    onChange={event => setSearch(event.target.value)}
                    placeholder="Search everything..."
                    className="w-full px-4 py-2 rounded-l-full border border-gray-300 shadow focus:border-blue-500 focus:ring-2 focus:ring-blue-200 focus:outline-none text-base bg-white"
                    style={{ minHeight: '44px' }}
                />
                <button
                    type="button"
                    onClick={doSearchAudits}
                    className="px-4 py-2.5 rounded-r-full bg-blue-600 text-white text-base font-semibold shadow hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-400"
                    aria-label="Search"
                    style={{ minHeight: '44px' }}
                >
                  <MagnifyingGlassCircleIcon className="h-5 w-5" />
                </button>
              </div>
              <div className="flex flex-col min-w-[220px]">
                <label className="block text-sm font-medium text-gray-700 mb-1">Domain / Env</label>
                <select
                    onChange = {domainEnvChanged}
                    className="w-full rounded-lg border border-gray-300 shadow focus:border-blue-500 focus:ring-2 focus:ring-blue-200 focus:outline-none transition sm:text-sm bg-white px-3 py-2"
                    style={{ minHeight: '44px' }}
                >
                  {
                    config.map(option => (
                      <option>{option.domain} / {option.env}</option>
                    ))
                  }
                </select>
              </div>
            </div>
            <div className="flex flex-col md:flex-row gap-4">
              <div className="flex-1">
                <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
                <select
                    value={statusFilter}
                    onChange={e => setStatusFilter(e.target.value)}
                    className="block w-full rounded-lg border border-gray-300 shadow focus:border-blue-500 focus:ring-2 focus:ring-blue-200 focus:outline-none transition sm:text-sm bg-white px-3 py-2"
                >
                  <option value="">All</option>
                  {statusOptions.map(opt => (
                      <option key={opt} value={opt}>{opt}</option>
                  ))}
                </select>
              </div>
              <div className="flex-1">
                <label className="block text-sm font-medium text-gray-700 mb-1">HTTP Method</label>
                <select
                    value={httpMethodFilter}
                    onChange={e => setHttpMethodFilter(e.target.value)}
                    className="block w-full rounded-lg border border-gray-300 shadow focus:border-blue-500 focus:ring-2 focus:ring-blue-200 focus:outline-none transition sm:text-sm bg-white px-3 py-2"
                >
                  <option value="">All</option>
                  {httpMethodOptions.map(opt => (
                      <option key={opt} value={opt}>{opt}</option>
                  ))}
                </select>
              </div>
            </div>
          </div>

          <div className="flex justify-end mb-1">
            <div className="relative py-2" ref={dropdownRef}>
              <button
                  type="button"
                  className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md shadow-sm bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                  onClick={() => setShowDropdown(v => !v)}
                  aria-label="Customize columns"
              >
                <AdjustmentsHorizontalIcon className="h-3 w-3 text-gray-500" />
              </button>
              {showDropdown && (
                  <div className="absolute right-0 mt-2 w-56 bg-white border border-gray-200 rounded shadow-lg z-20 p-3">
                    <div className="font-semibold mb-2 text-sm">Show Columns</div>
                    {ALL_COLUMNS.map(col => (
                        <label key={col.key} className="flex items-center space-x-2 mb-1">
                          <input
                              type="checkbox"
                              checked={showColumns.includes(col.key)}
                              onChange={() => handleColumnToggle(col.key)}
                              className="form-checkbox"
                          />
                          <span>{col.label}</span>
                        </label>
                    ))}
                  </div>
              )}
            </div>
          </div>
          <div className="flex items-center justify-end mb-2">
            <Pagination
                currentPage={currentPage}
                totalPages={totalPages}
                onPageChange={handlePageChange}
                itemsPerPage={itemsPerPage}
                totalItems={totalCount}
            />
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
              <tr>
                {ALL_COLUMNS.filter(col => showColumns.includes(col.key)).map(col => (
                    <th key={col.key} className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{col.label}</th>
                ))}
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
              {audits.map(audit => (
                  <tr key={audit.id} className="hover:bg-gray-50">
                    {ALL_COLUMNS.filter(col => showColumns.includes(col.key)).map(col => (
                        <td key={col.key} className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          {col.key === 'requestId' && audit[col.key] ? (
                              <span className="flex items-center">
                                <TruncateWithTooltip text={audit[col.key]} maxLength={10} />
                                <CopyButton value={audit[col.key]} className="ml-2" />
                              </span>
                          ) : col.key === 'path' ? (
                              <span className="flex items-center">
                                <TruncateWithTooltip text={getPath(audit)} maxLength={40} />
                                <CopyButton value={getPath(audit)} className="ml-2" />
                              </span>
                          ) : col.key === 'status' ? (
                              <StatusBadge status={getHttStatus(audit)} />
                          ) : col.key === 'method' ? (
                              getMethod(audit)
                          ) : col.key === 'duration' ? (
                              duration(audit) + ' ms'
                          ) : col.key === 'start' ? (
                              dateFormat(audit.start)
                          ) : (
                              audit[col.key]
                          )}
                        </td>
                    ))}
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <button
                          type="button"
                          onClick={() => handleViewDetails(audit)}
                          className="text-blue-600 hover:text-blue-900"
                      >
                        <EyeIcon className="h-5 w-5" />
                      </button>
                    </td>
                  </tr>
              ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
  );
};

export default RequestOverview;