import React, { useState, useEffect } from 'react';
import BpmnViewer from '../BPMN/BpmnViewer.jsx';
import { jsonToBpmn } from '../../utils/bpmnConverter.jsx';

const RulesTab = ({ request }) => {
    const [bpmnXml, setBpmnXml] = useState(null);
    const [showRaw, setShowRaw] = useState(false);
    const [hasFlows, setHasFlows] = useState(false);

    useEffect(() => {
        const flows = request.audits?.find(a => a.type === "kengine.flows");
        setHasFlows(!!flows);

        if (flows) {
            try {
                const xml = jsonToBpmn(flows);
                setBpmnXml(xml);
            } catch (err) {
                console.error('Failed to convert to BPMN', err);
            }
        }
    }, [request]);

    if (!hasFlows) {
        return (
            <div className="bg-white shadow overflow-hidden sm:rounded-lg p-4">
                <p>No kengine.flows data available for this audit.</p>
            </div>
        );
    }

    return (
        <div className="bg-white shadow overflow-hidden sm:rounded-lg">
            <div className="px-4 py-5 sm:px-6 flex justify-between items-center">
                <h3 className="text-lg leading-6 font-medium text-gray-900">Process Flow</h3>
                <button
                    onClick={() => setShowRaw(!showRaw)}
                    className="inline-flex items-center px-3 py-1 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                >
                    {showRaw ? 'Show Diagram' : 'Show Raw Data'}
                </button>
            </div>
            <div className="border-t border-gray-200 px-4 py-5 sm:p-6">
                {showRaw ? (
                    <pre className="text-sm text-gray-900 bg-gray-50 p-4 rounded overflow-x-auto">
            {JSON.stringify(
                request.audits.find(a => a.type === "kengine.flows"),
                null, 2
            )}
          </pre>
                ) : bpmnXml ? (
                    <BpmnViewer xml={bpmnXml} />
                ) : (
                    <p>Loading diagram...</p>
                )}
            </div>
        </div>
    );
};

export default RulesTab;