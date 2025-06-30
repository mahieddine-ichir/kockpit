import React from 'react';


function RulesTab({ request }) {
    let exchangesAudits = request?.audits?.filter(audit => audit.type === 'kengine.flows');
    let exchanges = [];
    exchangesAudits?.forEach(audit => {
        audit?.events?.forEach(exchange => {
            exchanges.push(JSON.parse(exchange));
        })
    });

    function prettyPrintBody(body) {
        if (!body) return '';
        try {
            const json = typeof body === 'string' ? JSON.parse(body) : body;
            return JSON.stringify(json, null, 2);
        } catch {
            return body;
        }
    }

    function rule({flow}) {
        return (
            <div className="mb-4">
                <h4 className="text-sm font-medium text-gray-500">Rule {name}</h4>
                <pre className="mt-1 text-sm text-gray-900 bg-gray-50 p-2 rounded overflow-x-auto">
                    {prettyPrintBody(flow)}
                </pre>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div className="bg-white shadow overflow-hidden sm:rounded-lg">
                <div className="px-4 py-5 sm:p-6">
                            {
                                exchanges.map(flow => {
                                    rule(flow)
                                })
                            }
                </div>
            </div>
        </div>
        )
}

export default RulesTab;
