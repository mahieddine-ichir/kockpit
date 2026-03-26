import React, {useEffect, useState} from "react";
import {getConfig} from "../services/api.js";
import {useSearchParams} from "react-router-dom";
import {getManifests} from "../services/manifestApi.js";

function DomainEnv({onConfigLoaded, domainEnvChanged, selectedIndex = 0, onSelectedIndex}) {
    const [searchParams] = useSearchParams();
    const [options, setOptions] = useState([]);
    const [configError, setConfigError] = useState(null);

    if (searchParams.has('selectedConfig')) {
        onSelectedIndex(parseInt(searchParams.get('selectedConfig')));
        selectedIndex = parseInt(searchParams.get('selectedConfig'));
    }
    useEffect(() => {
        getManifests().then(config => {
            if (!Array.isArray(config)) {
                setConfigError('Failed to load config: invalid response from server');
                return;
            }
            setConfigError(null);
            onConfigLoaded(config, selectedIndex);
            let opts = [];
            let index = 0;
            config.forEach(cfg => {
                opts.push({
                    name: cfg.name,
                    domain: cfg.domain,
                    env: cfg.env,
                    selected: index === selectedIndex
                });
                index++;
            });
            setOptions(opts);

        }).catch(e => setConfigError('Failed to load config: ' + (e?.message || 'unknown error')))
    }, []);

    function onChange(e) {
        let split = e.target.value.split('/');
        let domain = split[0].trim();
        let env = split[1].trim();
        let index = options.findIndex(option => option.domain === domain && option.env === env);
        onSelectedIndex(index);
        domainEnvChanged(domain, env, index);
    }

    const selectedValue = selectedIndex >= 0 && options.length > selectedIndex
        ? `${options[selectedIndex].domain}/${options[selectedIndex].env}`
        : '';

    if (configError) {
        return (
            <div className="px-2 py-2 sm:px-2 lg:px-2">
                <div className="text-red-600 text-sm font-medium bg-red-50 border border-red-200 rounded px-3 py-2">
                    {configError}
                </div>
            </div>
        );
    }

    return (
        <div className="px-2 py-2 sm:px-2 lg:px-2">
            <div className="flex flex-col min-w-[220px]">
                <select
                    onChange = {onChange}
                    value = {selectedValue}
                    className="w-full rounded-lg border border-gray-300 shadow focus:border-blue-500 focus:ring-2 focus:ring-blue-200 focus:outline-none transition sm:text-sm bg-white px-3 py-2"
                    style={{ minHeight: '44px' }}
                >
                    {
                        options.map((option, index) => (
                            <option key={option.domain+option.env+index} value={option.domain + '/' + option.env}>{option.domain} / {option.env} ({option.name})</option>
                        ))
                    }
                </select>
            </div>
        </div>
    )
}


export default DomainEnv;
