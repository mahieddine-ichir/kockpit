import React, {useEffect, useState} from "react";
import {getConfig} from "../services/api.js";
import {useSearchParams} from "react-router-dom";

function DomainEnv({onConfigLoaded, domainEnvChanged, selectedIndex = 0, onSelectedIndex}) {
    const [searchParams] = useSearchParams();
    const [options, setOptions] = useState([]);

    if (searchParams.has('selectedConfig')) {
        onSelectedIndex(parseInt(searchParams.get('selectedConfig')));
        selectedIndex = parseInt(searchParams.get('selectedConfig'));
    }
    useEffect(() => {
        getConfig().then(config => {
            onConfigLoaded(config, selectedIndex);
            let opts = [];
            let index = 0;
            config.forEach(cfg => cfg.configs.forEach(_cfg => {
                //console.log(`cfg ${JSON.stringify(_cfg)}`);
                opts.push({
                    name: cfg.name,
                    domain: _cfg.domain,
                    env: _cfg.env,
                    selected: index === selectedIndex
                });
                index++;
            }));
            setOptions(opts);

        }).catch(e => console.log('error loading config: '+e?.message || 'Failed to load config'))
    }, []);

    function onChange(e) {
        let split = e.target.value.split('/');
        let domain = split[0].trim();
        let env = split[1].trim();
        let index = options.findIndex(option => option.domain === domain && option.env === env);
        domainEnvChanged(domain, env, index);
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
                            <option selected={option.selected} key={option.domain+option.env+index} value={option.domain + '/' + option.env}>{option.domain} / {option.env} ({option.name})</option>
                        ))
                    }
                </select>
            </div>
        </div>
    )
}


export default DomainEnv;
