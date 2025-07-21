import React, {useEffect, useState} from 'react';
import {BrowserRouter, Navigate, Route, Routes} from 'react-router-dom';
import './index.css';
import Sidebar from "./sidebar/Sidebar.jsx";
import AuditListPage from "./audits/pages/AuditListPage.jsx";
import {getConfig} from "./services/api.js";
import DetailsPage from "./audits/pages/DetailsPage.jsx";
import { useAuth } from './auth/AuthContext.jsx';
import LoginPage from './auth/LoginPage.jsx';

function DomainEnv({domainEnvChanged, initDomainEnv}) {
    const [options, setOptions] = useState([]);
    useEffect(() => {
        getConfig().then(value => {
            let map = value.map(option => {
                return {
                    domain: option.domain, env: option.env
                }
            });
            setOptions(map);
            initDomainEnv(map);
        });
    }, []);

    function onChange(e) {
        let split = e.target.value.split('/');
        domainEnvChanged(split[0].trim(), split[1].trim());
    }
    return (
        <div className="flex flex-col min-w-[220px]">
            <label className="block text-sm font-medium text-gray-700 mb-1">Domain / Env</label>
            <select
                onChange = {onChange}
                className="w-full rounded-lg border border-gray-300 shadow focus:border-blue-500 focus:ring-2 focus:ring-blue-200 focus:outline-none transition sm:text-sm bg-white px-3 py-2"
                style={{ minHeight: '44px' }}
            >
                {
                    options.map(option => (
                        <option>{option.domain} / {option.env}</option>
                    ))
                }
            </select>
        </div>
    )
}

function App() {
    const { user, loading } = useAuth();
    const [collapsed, setCollapsed] = useState(false);
    const [domain, setDomain] = useState(null);
    const [env, setEnv] = useState(null);

    if (loading) return <div>Loading...</div>;

    function onDomainEnvChanged(domain, env) {
        console.log(`Domain ${domain} / env ${env} selected.`);
        setDomain(domain);
        setEnv(env);
    }

    function initDomainEnv(options) {
        if (options.length === 0) {
            return;
        }
        setDomain(options[0].domain);
        setEnv(options[0].env);
    }

    if (!user) {
        return (
            <BrowserRouter>
                <Routes>
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="*" element={<Navigate to="/login" replace />} />
                </Routes>
            </BrowserRouter>
        );
    }

    return (
        <div className="App">
            <BrowserRouter>
                <div className="screens-container">
                    <Sidebar collapsed={collapsed} setCollapsed={setCollapsed} />
                    <div>
                        <div className="flex justify-end p-2">
                            <DomainEnv domainEnvChanged={onDomainEnvChanged} initDomainEnv={initDomainEnv} />
                        </div>
                        <div className='screens-section-container'>
                            <div className="flex">
                                <div className={`${collapsed ? 'ml-16' : 'ml-64'} p-6 w-full transition-all duration-300`}>
                                    <Routes>
                                        <Route path='/audits' element={
                                            (domain && env) ? <AuditListPage domain={domain} env={env} /> : null
                                        } />
                                        <Route path="/audits/:id" element={<DetailsPage domain={domain} env={env} />} />
                                        <Route path="*" element={<Navigate to="/audits" replace={true} />} />
                                    </Routes>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </BrowserRouter>
        </div>
    );
}



/*
function App() {
  return (
    <Router>
      <Routes>
        <Route path="/audits" element={<AuditListPage />} />
        <Route path="/audits/:id" element={<DetailsPage />} />
        <Route path="*" element={<Navigate to="/audits" replace={true} />} />
      </Routes>
    </Router>
  );
}
 */

export default App;