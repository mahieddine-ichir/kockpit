import React, {useEffect, useState} from 'react';
import {BrowserRouter, Navigate, Route, Routes} from 'react-router-dom';
import './index.css';
import Sidebar from "./sidebar/Sidebar.jsx";
import AuditListPage from "./audits/pages/AuditListPage.jsx";
import DetailsPage from "./audits/pages/DetailsPage.jsx";
import DomainEnv from "./components/DomainEnv.jsx";
import {login, exchangeCodeForTokens} from "./services/api.js";
import FeatureFlippingPage from "./feature-flipping/feature.jsx";
import AppIdDashboard from "./home/Home.jsx";
import HealthIndicatorsDashboard from "./health/Health.jsx";
import ConfigManager from "./dyna-config/dyna-config.jsx";
import ManifestPage from "./manifest/ManifestPage.jsx";

function App() {
    const [collapsed, setCollapsed] = useState(false);
    const [domain, setDomain] = useState(null);
    const [env, setEnv] = useState(null);
    const [configs, setConfigs] = useState({});
    const [config, setConfig] = useState();
    const [loading, setLoading] = useState(true);
    const [user, setUser] = useState('');
    const [configIndex, setConfigIndex] = useState(0);

    useEffect(() => {
        // Check if we're returning from Cognito OAuth
        const urlParams = new URLSearchParams(window.location.search);
        const authCode = urlParams.get('code');
        const error = urlParams.get('error');

        if (error) {
            console.error('OAuth error:', error);
            setLoading(false);
            return;
        }

        if (authCode) {
            // Clear the URL parameters after capturing them
            window.history.replaceState({}, document.title, window.location.pathname);

            // Exchange authorization code for tokens
            exchangeCodeForTokens(authCode).then(tokenData => {
                if (tokenData && tokenData.user) {
                    setUser(tokenData.user);
                } else {
                    console.error('Failed to get user info from token exchange');
                }
                setLoading(false);
            }).catch(error => {
                console.error('Token exchange failed:', error);
                setLoading(false);
            });
        } else {
            // Normal login flow
            login().then(logged => {
                setUser(logged);
                setLoading(false);
            })
        }
    }, []);

    function onDomainEnvChanged(domain, env, selectedIndex) {
        console.log(`domain ${domain} / env ${env} selected.`);
        setDomain(domain);
        setEnv(env);
        setConfigIndex(selectedIndex);

        let _cfg = configs[selectedIndex];
        if (_cfg.env === env && _cfg.domain === domain) {
            setConfig(_cfg);
        }
    }

    function onConfigLoaded(configs, selectedIndex) {
        setConfigs(configs);
        const cfg = configs[selectedIndex];
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
                            <DomainEnv domainEnvChanged={onDomainEnvChanged} onConfigLoaded={onConfigLoaded} selectedIndex={configIndex} onSelectedIndex={setConfigIndex} />
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
                                        <Route path='/audits' element={<AuditListPage domain={domain} env={env} config={config} selectedIdx={configIndex} />} />
                                        <Route path="/audits/:id" element={<DetailsPage domain={domain} env={env} selectedIdx={configIndex} />} />
                                        <Route path='/feature-flipping' element={<FeatureFlippingPage domain={domain} env={env} config={config} />} />
                                        <Route path='/feature-flipping/:service' element={<FeatureFlippingPage domain={domain} env={env} config={config} />} />
                                        <Route path='/dyna-config' element={<ConfigManager domain={domain} env={env} config={config} />} />
                                        <Route path='/dyna-config/:service' element={<ConfigManager domain={domain} env={env} config={config} />} />
                                        <Route path='/manifest' element={<ManifestPage />} />
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
