import React, {useEffect, useState} from 'react';
import {Link, useParams} from 'react-router-dom';
import DetailTabs from '../components/RequestDetail/DetailTabs';
import {fetchAuditById} from "../services/api.js";

const RequestDetail = ({domain, env}) => {
  const { id} = useParams();
  const [request, setRequest] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
      fetchAuditById(id, domain, env)
          .then(audit => {
              console.log(audit);
              setRequest(audit);
              setLoading(false);
          });
  }, [])

  if (loading) return <div>Loading...</div>;
  return (
    <div>
      <h1 className="text-xl font-bold mb-6">
      <nav>
        <Link to="/audits" className="hover:underline">Audits</Link> / {' '}
        {id}
      </nav>
      </h1>
      <DetailTabs request={request} />
    </div>
  );
};

export default RequestDetail;