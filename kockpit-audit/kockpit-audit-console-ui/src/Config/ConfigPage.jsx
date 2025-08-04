import React, {useState} from 'react';
import ReactJson from 'react-json-view';

const ConfigPage = (cfg) => {
    const [config] = useState(cfg);
    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold mb-4 text-slate-100">Config</h1>
            <div className="bg-slate-800 p-4 rounded-lg overflow-x-auto">
                <ReactJson src={config} theme="ocean" collapsed={2} displayDataTypes={false} enableClipboard={true} />
            </div>
        </div>
    );
};

export default ConfigPage;