import React, {useEffect, useState} from 'react';
import {Link, useParams} from 'react-router-dom';
import DetailTabs from '../tabs/DetailTabs.jsx';
import {fetchAuditById} from '../../services/api.js';

const DetailsPage = ({domain, env}) => {
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
        <div className="bg-slate-50 min-h-screen px-4 py-8 sm:px-8 lg:px-16">
            <div className="bg-white rounded-xl shadow-md border-l-4 border-blue-600/60 mb-8 p-6 flex flex-col gap-2">
                <nav className="text-sm text-slate-500 mb-1">
                    <Link to="/audits" className="hover:underline text-blue-600 font-medium">Audits</Link> <span className="mx-1">/</span> <span className="text-slate-400">{id}</span>
                </nav>
                <h1 className="text-2xl font-bold text-slate-800">Audit Details</h1>
            </div>
            <DetailTabs request={request} />
        </div>
    );
};

export default DetailsPage;