import React, {useEffect, useState} from 'react';
import {Link, useParams} from 'react-router-dom';
import {fetchAuditDetails, fetchAuditReportById} from '../services/api';
import DetailTabs from '../components/RequestDetail/DetailTabs';
import Sidebar from '../components/Sidebar/Sidebar';

const RequestDetail = () => {
  const { id, traceId } = useParams();
  const [request, setRequest] = useState(null);
  const [loading, setLoading] = useState(true);
  const [collapsed, setCollapsed] = useState(false);

  useEffect(() => {
    fetchAuditReportById(id).then(audit => {
      const currentAudit = audit;
      try {
        if (traceId) {
          fetchAuditDetails(id, traceId).then(data => {
            const combinedData = {
              ...currentAudit,
              ...data,
              indexedKeyValues: currentAudit.indexedKeyValues || []
            };
            setRequest(combinedData);
            setLoading(false);
          });
        } else {
          fetchAuditDetails(id, "none").then(data => {
            const combinedData = {
              ...currentAudit,
              ...data,
              indexedKeyValues: currentAudit.indexedKeyValues || []
            };
            setRequest(combinedData);
            setLoading(false);
          });
        }
      } catch (err) {
        console.error('Error fetching data:', err);
        setLoading(false);
      }
    });
  }, [id, traceId]);

  if (loading) return <div>Loading...</div>;

  return (
    <div className="flex">
      <Sidebar collapsed={collapsed} setCollapsed={setCollapsed} />
      <div className={`${collapsed ? 'ml-16' : 'ml-64'} p-6 w-full transition-all duration-300`}>
        {request?.httpAuditedRequest ?
          <h1 className="text-2xl font-bold mb-6">
            Request Detail: {request.httpAuditedRequest.uri}
          </h1> : null
        }
        <nav className="text-sm mb-2 text-gray-500">
          <Link to="/" className="hover:underline text-blue-600">Home</Link> &gt;{' '}
          <Link to={`/audit/${id}/requests`} className="hover:underline text-blue-600">Requests</Link> &gt;{' '}
          Details
        </nav>
        <DetailTabs request={request} />
      </div>
    </div>
  );
};

export default RequestDetail;