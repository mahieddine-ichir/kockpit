import React, {useEffect, useState} from 'react';
import {advancedSearchAudits, fetchAuditReportsWithPaging, searchAudits} from '../../services/api.js';
import {EyeIcon, XMarkIcon,BookmarkIcon, CheckCircleIcon,FunnelIcon  } from '@heroicons/react/24/outline';
import {MagnifyingGlassIcon} from '@heroicons/react/20/solid';
import {useNavigate} from 'react-router-dom';
import StatusBadge from '../../components/StatusBadge.jsx';
import TruncateWithTooltip from "../../components/TruncateWithTooltip.jsx";
import Pagination from '../../components/Pagination.jsx';
import CopyButton from "../../components/CopyButton.jsx";
import SearchTerm from "./components/SearchTerm.jsx";

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

function fetchIndexedValue(audit, key) {
  if (audit['indexedKeyValues']) {
    const found = audit['indexedKeyValues'].find(kv => kv.key === key);
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

const HeaderLine = ({columns}) => {
  return (<tr>
    {columns.map(col => {
      return (
          <th
            key={col.key}
            scope="col"
            className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider"
          >
          {col.label}
      </th>
      )
    })}
    <th scope="col" className="relative px-6 py-3">
      <span className="sr-only">Actions</span>
    </th>
  </tr>)
}

const AuditLine = ({columns, audit, onClick}) => {
  return (
  <tr key={audit.id} className="hover:bg-blue-50/60 transition-colors">
    {columns.map(col => {
      return <td key={col.key} className="whitespace-nowrap px-6 py-4 text-sm">
      {col.key === 'requestId' && audit[col.key] ? (
          <div className="flex items-center">
            <TruncateWithTooltip text={audit[col.key]} maxLength={12}/>
            <CopyButton value={audit[col.key]}/>
          </div>
      ) : col.key === 'path' ? (
          <div className="flex items-center">
            <TruncateWithTooltip text={getPath(audit)} maxLength={30}/>
            <CopyButton value={getPath(audit)}/>
          </div>
      ) : col.key === 'status' ? (
          <StatusBadge status={getHttStatus(audit)}/>
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
    })}
    <td className="whitespace-nowrap px-6 py-4 text-right text-sm font-medium">
      <button
          onClick={() => onClick(audit)}
          className="text-blue-600 hover:text-blue-800 flex items-center space-x-1 font-semibold"
      >
        <EyeIcon className="h-4 w-4"/>
      </button>
    </td>
  </tr>
  )
}

const Filters = ({filters, onSelectedFilter, value}) => {
    const selectFilter = (selected) => {
        let selectedFilter = filters.find(val => val.name === selected);
        if (!selectedFilter) {
            return;
        }
        if (selectedFilter) {
            onSelectedFilter(selectedFilter);
        }
    }
    return (
        <div className="relative">
            <select
                value={value?.name}
                onChange={(e) => selectFilter(e.target.value)}
                className="min-w-[180px] rounded-xl border border-slate-300 py-2.5 pl-10 pr-8 text-sm bg-white text-slate-700 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 shadow-sm hover:border-slate-400 transition-colors"
            >
                <option value=""> Saved Filters</option>
                {filters.map(opt => (
                    <option key={opt.name} value={opt.name}>{opt.name}</option>
                ))}
            </select>
            <BookmarkIcon className="h-4 w-4 text-slate-500 absolute left-3 top-1/2 transform -translate-y-1/2" />
        </div>
    )
}

const SelectFilter = ({ placeholder, value, onChange, options }) => {
    return (
        <div className="relative">
            <select
                value={value ?? ''}
                onChange={(e) => onChange(e.target.value)}
                className="min-w-[140px] rounded-xl border border-slate-300 py-2.5 pl-3 pr-8 text-sm bg-white text-slate-700 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 shadow-sm hover:border-slate-400 transition-colors"
            >
                <option value="">{placeholder}</option>
                {options.map(opt => (
                    <option key={String(opt)} value={opt}>{opt}</option>
                ))}
            </select>
        </div>
    );
}

const SearchTermFactory = ({searchTerm, setTerm, clearTerm}) => {
    if (searchTerm.type === 'options') {
        const placeholder = searchTerm.name === 'httpStatus' ? 'All Statuses' : (searchTerm.name === 'httpMethod' ? 'All Methods' : `All ${searchTerm.name}`);
        const handleChange = (val) => {
            if (val === '' || val === undefined || val === null) {
                clearTerm(searchTerm);
            } else {
                setTerm(searchTerm, val);
            }
        };
        return (
            <SelectFilter
                placeholder={placeholder}
                value={undefined}
                onChange={handleChange}
                options={searchTerm.options || []}
            />
        );
    }
    return (
        <SearchTerm
            term={searchTerm}
            setTerm={(text) => setTerm(searchTerm, text)}
            clearTerm={() => clearTerm(searchTerm)}
        />
    );
}

const ActiveFilters = ({ searchTerms, clearTerm, clearAll }) => {
    if (searchTerms.length === 0) return null;

    return (
        <div className="flex flex-wrap items-center gap-2 p-3 bg-blue-50 rounded-lg border border-blue-200">
            <CheckCircleIcon className="h-4 w-4 text-blue-800" />
            <span className="text-sm font-medium text-blue-800 flex items-center">
              Active Filters:
          </span>

            {searchTerms.map((term, index) => (
                <span
                    key={`${term.name}-${index}`}
                    className="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-blue-100 text-blue-800 text-sm font-medium"
                >
                  <span className="font-semibold">{term.name}:</span> {term.value}
                    <button
                        onClick={() => clearTerm(term)}
                        className="hover:bg-blue-200 rounded-full p-0.5 transition-colors"
                    >
                      <XMarkIcon className="h-3 w-3" />
                  </button>
              </span>
            ))}
            <button
                onClick={clearAll}
                className="ml-2 text-sm text-blue-600 hover:text-blue-800 font-medium underline"
            >
                Clear All
            </button>
        </div>
    );
}

const DateRangeControl = ({ searchTerms, addTerm, clearTerm }) => {
    const pad = (n) => String(n).padStart(2, '0');
    const toLocalDT = (ms) => {
        if (!ms) return '';
        const d = new Date(ms);
        return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
    };

    const existingStart = searchTerms.find(t => t.name === 'start');
    const existingEnd = searchTerms.find(t => t.name === 'end');

    const [fromDT, setFromDT] = React.useState(toLocalDT(existingStart?.value));
    const [toDT, setToDT] = React.useState(toLocalDT(existingEnd?.value));

    React.useEffect(() => {
        setFromDT(toLocalDT(existingStart?.value));
        setToDT(toLocalDT(existingEnd?.value));
    }, [existingStart?.value, existingEnd?.value]);

    const updateFrom = (val) => {
        setFromDT(val);
        const ms = val ? new Date(val).getTime() : null;
        const startOverride = { name: 'start', path: 'start', type: 'date' };
        if (ms !== null) addTerm(startOverride, ms); else clearTerm(startOverride);
    };

    const updateTo = (val) => {
        setToDT(val);
        const ms = val ? new Date(val).getTime() : null;
        const endOverride = { name: 'end', path: 'end', type: 'date' };
        if (ms !== null) addTerm(endOverride, ms); else clearTerm(endOverride);
    };

    const setPreset = (msAgo, label) => {
        const now = new Date();
        const from = new Date(Date.now() - msAgo);
        const toStr = toLocalDT(now.getTime());
        const fromStr = toLocalDT(from.getTime());
        setFromDT(fromStr);
        setToDT(toStr);
        addTerm({ name: 'start', path: 'start', type: 'date' }, from.getTime());
        addTerm({ name: 'end', path: 'end', type: 'date' }, now.getTime());
    };

    const presets = [
        { label: '5m', ms: 5*60*1000 },
        { label: '10m', ms: 10*60*1000 },
        { label: '1h', ms: 60*60*1000 },
        { label: '24h', ms: 24*60*60*1000 },
        { label: '7d', ms: 7*24*60*60*1000 },
        { label: '14d', ms: 14*24*60*60*1000 },
        { label: '1mo', ms: 30*24*60*60*1000 },
        { label: '2mo', ms: 60*24*60*60*1000 }
    ];

    return (
        <div className="space-y-4">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                <div>
                    <label className="block text-sm font-medium text-slate-700 mb-2">From</label>
                    <input
                        type="datetime-local"
                        value={fromDT}
                        onChange={(e) => updateFrom(e.target.value)}
                        className="block w-full px-3 py-2.5 rounded-lg border border-slate-200 bg-white text-slate-800 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm transition-colors"
                    />
                </div>
                <div>
                    <label className="block text-sm font-medium text-slate-700 mb-2">To</label>
                    <input
                        type="datetime-local"
                        value={toDT}
                        onChange={(e) => updateTo(e.target.value)}
                        className="block w-full px-3 py-2.5 rounded-lg border border-slate-200 bg-white text-slate-800 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm transition-colors"
                    />
                </div>
            </div>

            <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">Quick Presets</label>
                <div className="flex flex-wrap gap-2">
                    {presets.map((preset) => (
                        <button
                            key={preset.label}
                            type="button"
                            onClick={() => setPreset(preset.ms, preset.label)}
                            className="px-3 py-2 text-sm rounded-lg border border-slate-200 bg-white text-slate-700 hover:bg-slate-50 hover:border-slate-300 transition-colors font-medium"
                        >
                            Last {preset.label}
                        </button>
                    ))}
                </div>
            </div>
        </div>
    );
};


const AuditListPage = ({ domain, env, config }) => {
  const [audits, setAudits] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(25);
  const [totalCount, setTotalCount] = useState(0);
  const navigate = useNavigate();
  const [searchTerms, setSearchTerms] = useState([]);
  const [filter, setFilter] = useState([]);
  const [showFilters, setShowFilters] = useState(false);

  let searchColumns = [];
  let columns = [];
  let filters = [];
  let label = '';
  if (config['services']) {
    let thisConfig = config['services'].find(service => service.name === 'audit');
    label = thisConfig.label;
    columns = thisConfig.config.columns
        .map(column => {
          return {
            key: column,
            label: formatLabel(column)
          }
        });
    searchColumns = thisConfig.config['search_columns'];
    if (thisConfig.config['saved_filters']) {
        filters = thisConfig.config['saved_filters'];
    }
  }

  useEffect(() => {
    loadData();
  }, [domain, env, currentPage, itemsPerPage, searchTerms]);

  const handleViewDetails = (audit) => {
    navigate(`/audits/${audit.id}`);
  };

  if (loading) return <div>Loading...</div>;

  const handlePageChange = (page, size) => {
    setItemsPerPage(size);
    setCurrentPage(page);
  };

  function loadData() {
      if (searchTerms.length > 0) {
          setLoading(true);
          console.info(`load data, searchTerms ${JSON.stringify(searchTerms)}`);
          advancedSearchAudits(searchTerms, domain, env, itemsPerPage, (currentPage - 1) * itemsPerPage).then(data => {
              setAudits(data.items);
              setTotalCount(data.total_count);
              setLoading(false);
          });
      } else if (search || search.trim().length > 0) {
          setLoading(true);
          console.info(`load data, search ${JSON.stringify(search)}`);
          searchAudits(search, domain, env, itemsPerPage, (currentPage - 1) * itemsPerPage).then(data => {
              setAudits(data.items);
              setTotalCount(data.total_count);
              setLoading(false);
          });
      } else {
          console.log(`loading all data: ${domain}, ${env}, ${itemsPerPage}, ${(currentPage - 1) * itemsPerPage}`);
          fetchAuditReportsWithPaging(domain, env, itemsPerPage, (currentPage - 1) * itemsPerPage)
              .then((data) => {
                  if (data && data.items && data.items.length > 0) {
                        console.log(`loaded ${data.items.length} items`)
                      setAudits(data.items);
                      setTotalCount(data.total_count);
                    } else {
                        console.log(`no data found!`)
                      //setAudits([]);
                      //setTotalCount(0);
                  }
                  setLoading(false);
              });
      }
  }

  const handleKeyPress = (event) => {
    if (event.key === 'Enter') {
      loadData();
    }
  }

    let addTerm = (searchTerm, text) => {
      console.log(`add filter ${JSON.stringify(text)}, searchTerm ${JSON.stringify(searchTerm)}`);
      let existing = searchTerms.find(kv => kv.name === searchTerm.name);
      if (existing) {
          existing.value = text;
          loadData();
      } else {
          let newEl = {
              name: searchTerm.name,
              path: searchTerm.path,
              value: text
          };
          setSearchTerms([...searchTerms, newEl]);
      }
    };

  let clearTerm = (searchTerm) => {
      let index = searchTerms.indexOf(searchTerm);
      if (index > -1) {
          setSearchTerms([
              ...searchTerms.slice(0, index),
              ...searchTerms.slice(index+1)
          ]);
      }
  }

    let clearAllFilters = () => {
        setSearchTerms([]);
        setFilter([]);
    }

  let onSelectedFilter = (filter) => {
      if (!filters) {
          return;
      }
      console.log(`selecting filter ${JSON.stringify(filter)}`)
      filter.filters.map(searchTerm => {
          addTerm({
              name: searchTerm.name,
              path: searchTerm.path
          }, searchTerm.values);
      })

      setFilter(filter);
  }

    const otherFilters = searchColumns.filter(sc => !(sc.type === 'date' && (sc.name === 'start' || sc.name === 'end')));
    const hasDateFilters = searchColumns.some(sc => sc.type === 'date' && (sc.name === 'start' || sc.name === 'end'));
    const hasOtherFilters = otherFilters.length > 0;

    return (
        <div className="px-2 py-2 sm:px-2 lg:px-2 bg-slate-50 min-h-screen">
            <div className="flex items-center justify-between mb-5">
                <div className="flex items-center">
                    <div className="h-10 w-1 rounded bg-blue-600 mr-4" />
                    <div>
                        <h1 className="text-3xl font-bold text-slate-800 tracking-tight">Audits</h1>
                        <p className="mt-1 text-base text-slate-500">{label}</p>
                    </div>
                </div>
                {filters && filters.length > 0 && (
                    <Filters filters={filters} onSelectedFilter={onSelectedFilter} value={filter} />
                )}
            </div>

            <div className="mb-8">
                <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
                    <div className="p-6 border-b border-slate-100">
                        <div className="flex flex-col lg:flex-row gap-4 items-start lg:items-center">
                            <div className="flex-1 w-full">
                                <div className="relative">
                                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                        <MagnifyingGlassIcon className="h-5 w-5 text-slate-400" />
                                    </div>
                                    <input
                                        type="text"
                                        value={search}
                                        onChange={(e) => setSearch(e.target.value)}
                                        onKeyUp={handleKeyPress}
                                        placeholder="Search by request ID, path, status, method, or any audit field..."
                                        className="block w-full pl-10 pr-32 py-3.5 rounded-xl border border-slate-200 bg-white text-slate-800 placeholder-slate-500 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-base transition-all shadow-sm hover:border-slate-300"
                                    />
                                    <div className="absolute right-2 top-1/2 -translate-y-1/2 flex items-center gap-2">
                                        <button
                                            onClick={loadData}
                                            className="px-6 py-2.5 rounded-lg bg-gradient-to-r from-blue-600 to-blue-700 text-white font-semibold shadow-lg hover:from-blue-700 hover:to-blue-800 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-all transform hover:scale-105 active:scale-95"
                                        >
                                            Search
                                        </button>
                                    </div>
                                </div>
                            </div>

                            {(hasDateFilters || hasOtherFilters) && (
                                <button
                                    onClick={() => setShowFilters(!showFilters)}
                                    className={`flex items-center gap-2 px-4 py-3.5 rounded-xl border transition-all ${
                                        showFilters
                                            ? 'bg-blue-50 border-blue-200 text-blue-700'
                                            : 'bg-white border-slate-200 text-slate-700 hover:bg-slate-50'
                                    }`}
                                >
                                    <FunnelIcon className="h-5 w-5" />
                                    <span className="font-medium">Filters</span>
                                    {searchTerms.length > 0 && (
                                        <span className="bg-blue-600 text-white text-xs font-bold rounded-full h-5 w-5 flex items-center justify-center">
                      {searchTerms.length}
                    </span>
                                    )}
                                </button>
                            )}
                        </div>
                    </div>

                    {showFilters && (hasDateFilters || hasOtherFilters) && (
                        <div className="p-6 bg-slate-50/50 border-b border-slate-100">
                            <div className="space-y-6">
                                {hasDateFilters && (
                                    <div className="space-y-3">
                                        <div className="flex items-center gap-2">
                                            <div className="w-1 h-4 bg-blue-500 rounded-full"></div>
                                            <h3 className="text-sm font-semibold text-slate-700">Date Range</h3>
                                        </div>
                                        <div className="bg-white rounded-xl border border-slate-200 p-4">
                                            <DateRangeControl
                                                searchTerms={searchTerms}
                                                addTerm={addTerm}
                                                clearTerm={clearTerm}
                                            />
                                        </div>
                                    </div>
                                )}

                                {hasOtherFilters && (
                                    <div className="space-y-3">
                                        <div className="flex items-center gap-2">
                                            <div className="w-1 h-4 bg-purple-500 rounded-full"></div>
                                            <h3 className="text-sm font-semibold text-slate-700">Advanced Filters</h3>
                                        </div>
                                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3">
                                            {otherFilters.map((searchTerm) => (
                                                <div key={searchTerm.name} className="bg-white rounded-lg border border-slate-200 p-3">
                                                    <label className="block text-xs font-medium text-slate-600 mb-2 uppercase tracking-wide">
                                                        {searchTerm.name}
                                                    </label>
                                                    <SearchTermFactory
                                                        searchTerm={searchTerm}
                                                        setTerm={addTerm}
                                                        clearTerm={clearTerm}
                                                    />
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>
                    )}

                    {searchTerms.length > 0 && (
                        <div className="p-4 bg-blue-50/80 border-t border-blue-100">
                            <ActiveFilters
                                searchTerms={searchTerms}
                                clearTerm={clearTerm}
                                clearAll={clearAllFilters}
                            />
                        </div>
                    )}
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
                    <HeaderLine columns={columns}/>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                    { audits ?
                        audits.map(audit => (
                            <AuditLine audit={audit} columns={columns} key={audit.id} onClick={handleViewDetails} />
                        )) : null
                    }
                    </tbody>
                </table>
            </div>
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
