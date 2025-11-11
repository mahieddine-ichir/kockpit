import React, {useEffect, useState} from 'react';
import {BrowserRouter, Navigate, Route, Routes} from 'react-router-dom';
import './index.css';
import Sidebar from "./sidebar/Sidebar.jsx";
import AuditListPage from "./audits/pages/AuditListPage.jsx";
import DetailsPage from "./audits/pages/DetailsPage.jsx";
import DomainEnv from "./components/DomainEnv.jsx";
import {login} from "./services/api.js";
import ConfigPage from "./Config/ConfigPage.jsx";
import FeatureFlippingPage from "./feature-flipping/feature.jsx";
import AppIdDashboard from "./home/Home.jsx";
import HealthIndicatorsDashboard from "./health/Health.jsx";

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
            setUser(logged);
            setLoading(false);
        })
    }, []);

    function onDomainEnvChanged(domain, env) {
        console.log(`domain ${domain} / env ${env} selected.`);
        setDomain(domain);
        setEnv(env);

        configs.forEach((cfg) => {
            cfg.configs.forEach(_cfg => {
                if (_cfg.env === env && _cfg.domain === domain) {
                    setConfig(_cfg);
                }
            });
        });
    }

    function onConfigLoaded(configs) {
        //console.log(`config loaded ${JSON.stringify(configs)}`);
        setConfigs(configs);

        const cfg = configs[0].configs[0];
        //console.log(`default ${JSON.stringify(cfg)}`);

        setConfig(cfg);
        setDomain(cfg.domain);
        setEnv(cfg.env);
    }

    if (loading) return <div>Loading ...</div>;

    return (
        <BrowserRouter>
        <div className="App">
                <div className="screens-container">
                    {
                        config && user ?
                            <Sidebar collapsed={collapsed} setCollapsed={setCollapsed} config={config} user={user} />
                            : null
                    }
                    <div>
                        <div className="flex justify-end p-2">
                            <DomainEnv domainEnvChanged={onDomainEnvChanged} onConfigLoaded={onConfigLoaded} />
                        </div>
                        <div className='screens-section-container'>
                    {
                        config && user ?
                            <div className="flex">
                                <div
                                    className={`${collapsed ? 'ml-16' : 'ml-64'} p-6 w-full transition-all duration-300`}>
                                    <Routes>
                                        <Route path='/home' element={<AppIdDashboard domain={domain} env={env} />} />
                                        <Route path='/health' element={<HealthIndicatorsDashboard domain={domain} env={env} />} />
                                        <Route path='/audit' element={<AuditListPage domain={domain} env={env} config={config} />} />
                                        <Route path='/config' element={<ConfigPage configs={configs} />} />
                                        <Route path='/feature-flipping' element={<FeatureFlippingPage domain={domain} env={env} config={config} />} />
                                        <Route path="/audits/:id" element={<DetailsPage domain={domain} env={env}/>} />
                                        <Route path="*" element={<Navigate to="/home" replace={true}/>}/>
                                    </Routes>
                                </div>
                            </div> : null
                    }
                        </div>
                    </div>
                </div>
        </div>
        </BrowserRouter>
    );
}

export default App;
