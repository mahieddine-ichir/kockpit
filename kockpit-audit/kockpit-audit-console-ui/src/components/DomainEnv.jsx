import React, {useEffect, useState} from "react";
import {getConfig} from "../services/api.js";

function DomainEnv({onConfigLoaded, domainEnvChanged}) {
    const [options, setOptions] = useState([]);
    useEffect(() => {
        getConfig().then(config => {
            onConfigLoaded(config);
            let opts = [];
            config.forEach(cfg => cfg.configs.forEach(_cfg => {
                console.log(`cfg ${JSON.stringify(_cfg)}`);
                opts.push({
                    name: cfg.name,
                    domain: _cfg.domain,
                    env: _cfg.env
                });
            }));
            setOptions(opts);
        }).catch(e => console.log('error loading config: '+e?.message || 'Failed to load config'))
    }, []);

    function onChange(e) {
        console.log(`Domain changed ${JSON.stringify(e.target.value)}`);
        let split = e.target.value.split('/');
        domainEnvChanged(split[0].trim(), split[1].trim());
    }
    return (
        <div className="px-2 py-2 sm:px-2 lg:px-2">
            <div className="flex flex-col min-w-[220px]">
                <select
                    onChange = {onChange}
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
