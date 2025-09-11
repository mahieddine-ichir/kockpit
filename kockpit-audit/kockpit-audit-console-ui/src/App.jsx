import React, {useEffect, useState} from 'react';
import {BrowserRouter, Navigate, Route, Routes} from 'react-router-dom';
import './index.css';
import Sidebar from "./sidebar/Sidebar.jsx";
import AuditListPage from "./audits/pages/AuditListPage.jsx";
import DetailsPage from "./audits/pages/DetailsPage.jsx";
import DomainEnv from "./components/DomainEnv.jsx";
import {login} from "./services/api.js";

function App() {
    const [collapsed, setCollapsed] = useState(false);
    const [domain, setDomain] = useState(null);
    const [env, setEnv] = useState(null);
    const [configs, setConfigs] = useState({});
    const [config, setConfig] = useState();
    const [loading, setLoading] = useState(true);
    const [user, setUser] = useState('');

    useEffect(() => {
        login().then(logged => {
            console.log(`logged ${JSON.stringify(logged)}`)
            setUser(logged);
            setLoading(false);
        })
    }, []);

    function onDomainEnvChanged(domain, env) {
        console.log(`domain ${domain} / env ${env} selected.`);
        setDomain(domain);
        setEnv(env);

        setConfig(configs.find(cfg => cfg.env === env));
    }

    function onConfigLoaded(configs) {
        console.log(`configs ${JSON.stringify(configs)} loaded`);
        setConfigs(configs);
        setConfig(configs[0]);

        setDomain(configs[0].domain);
        setEnv(configs[0].env);
    }

    if (loading) return <div>Loading ...</div>;

    return (
        <BrowserRouter>
        <div className="App">
                <div className="screens-container">
                    {
                        user ?
                            <Sidebar collapsed={collapsed} setCollapsed={setCollapsed} config={config} user={user} />
                            : null
                    }
                    <div>
                        <div className="flex justify-end p-2">
                            <DomainEnv domainEnvChanged={onDomainEnvChanged} onConfigLoaded={onConfigLoaded} />
                        </div>
                        <div className='screens-section-container'>
                            <div className="flex">
                                <div
                                    className={`${collapsed ? 'ml-16' : 'ml-64'} p-6 w-full transition-all duration-300`}>
                                    <Routes>
                                        <Route path='/audits' element={<AuditListPage domain={domain} env={env} config={config} />} />
                                        <Route path="/audits/:id" element={<DetailsPage domain={domain} env={env}/>} />
                                        <Route path="*" element={<Navigate to="/audits" replace={true}/>}/>
                                    </Routes>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
        </div>
        </BrowserRouter>
    );
}

export default App;
