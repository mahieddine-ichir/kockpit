import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { fetchAuditRequests, fetchAuditReports } from '../../services/api.js';
import Sidebar from '../../Sidebar/Sidebar.jsx';
import RequestTable from '../components/RequestTable.jsx';
import SearchFilters from '../components/SearchFilters.jsx';

const AuditRequestsPage = () => {
  const { id } = useParams();
  const [requests, setRequests] = useState([]);
  const [filteredRequests, setFilteredRequests] = useState([]);
  const [audit, setAudit] = useState(null);
  const [loading, setLoading] = useState(true);
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    async function fetchData() {
      try {
        const [requestsData, auditsData] = await Promise.all([
          fetchAuditRequests(id),
          fetchAuditReports()
        ]);
        setRequests(requestsData);
        setFilteredRequests(requestsData);
        const foundAudit = auditsData.find(a => a.id === id);
        setAudit(foundAudit);
      } catch (error) {
        console.error('Error fetching data:', error);
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, [id]);

  const handleRowClick = (request) => {
    navigate(`/audit/${id}/requests/${request.traceId}`);
  };

  const handleFilter = (filters) => {
    let filtered = requests.filter(r => {
      return (
        (!filters.method || r.method === filters.method) &&
        (!filters.status || r.status.toString() === filters.status) &&
        (!filters.traceId || r.traceId.includes(filters.traceId)) &&
        (!filters.path || r.uri.includes(filters.path))
      );
    });
    setFilteredRequests(filtered);
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="flex">
      <Sidebar collapsed={collapsed} setCollapsed={setCollapsed} />
      <div className={`${collapsed ? 'ml-16' : 'ml-64'} p-6 w-full transition-all duration-300`}>
        <h1 className="text-2xl font-bold mb-2">
          Requests for Audit {audit ? audit.domain : id}
        </h1>
        <nav className="text-sm mb-4 text-gray-500">
          <Link to="/" className="hover:underline text-blue-600">Home</Link> &gt; Requests
        </nav>
        <SearchFilters onFilter={handleFilter} />
        <RequestTable
          requests={filteredRequests}
          onRowClick={handleRowClick}
        />
      </div>
    </div>
  );
};

export default AuditRequestsPage; 