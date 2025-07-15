import React, {useEffect, useState} from 'react';
import {BrowserRouter, Navigate, Route, Routes} from 'react-router-dom';
import './index.css';
import Sidebar from "./components/Sidebar/Sidebar.jsx";
import RequestOverview from "./pages/RequestOverview.jsx";
import {getConfig} from "./services/api.js";
import RequestDetail from "./pages/RequestDetail.jsx";

function DomainEnv({domainEnvChanged, initDomainEnv}) {
    const [options, setOptions] = useState([]);
    const [config, setConfig] = useState(null);

    useEffect(() => {
        getConfig().then(value => {
            let map = value.map(option => {
                return {
                    domain: option.domain, env: option.env
                }
            });
            setOptions(map);
            setConfig(value);
            initDomainEnv(map, value.filter(c => c.domain === map[0].domain && c.env === map[0].env));
        });
    }, []);

    function onChange(e) {
        let split = e.target.value.split('/');
        let domain = split[0].trim();
        let env = split[1].trim();

        let cfg = config.filter(c => c.domain === domain && c.env === env);
        domainEnvChanged(domain, env, cfg);
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
    //const [logged, setLogged] = useState(true);
    const [collapsed, setCollapsed] = useState(false);
    const [domain, setDomain] = useState(null);
    const [env, setEnv] = useState(null);
    const [config, setConfig] = useState(null);

    function onDomainEnvChanged(domain, env, config) {
        console.log(`Domain ${domain} / env ${env} selected.`);
        setDomain(domain);
        setEnv(env);
        setConfig(config);
    }

    function initDomainEnv(options, config) {
        if (options.length === 0) {
            return;
        }
        setDomain(options[0].domain);
        setEnv(options[0].env);

        setConfig(config);
    }

    return (
        <div className="App">
            <BrowserRouter>
                <div className="screens-container">
                    <Sidebar collapsed={collapsed} setCollapsed={setCollapsed} />
                    <div>
                        <div className="flex justify-end pr-6 pt-6">
                            <DomainEnv domainEnvChanged={onDomainEnvChanged} initDomainEnv={initDomainEnv} />
                        </div>

                        <div className='screens-section-container'>
                            <div className="flex">
                                <div className={`${collapsed ? 'ml-16' : 'ml-64'} p-6 w-full transition-all duration-300`}>
                                <Routes>
                                    <Route path='/audits' element={
                                        (domain && env) ? <RequestOverview domain={domain} env={env} config={config} /> : null
                                    }
                                    />
                                    <Route path="/audits/:id" element={<RequestDetail domain={domain} env={env} />} />
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
        <Route path="/audits" element={<RequestOverview />} />
        <Route path="/audits/:id" element={<RequestDetail />} />
        <Route path="*" element={<Navigate to="/audits" replace={true} />} />
      </Routes>
    </Router>
  );
}
 */

export default App;