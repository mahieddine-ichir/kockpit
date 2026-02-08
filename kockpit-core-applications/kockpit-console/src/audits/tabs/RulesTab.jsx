import React, { useState, useEffect } from 'react';
import { GitBranch, Code, Eye, EyeOff, Workflow, AlertCircle, CheckCircle, Loader, ChevronDown, ChevronRight, Timer, PlayCircle } from 'lucide-react';
import BpmnViewer from './bpmn/BpmnViewer.jsx';
import { jsonToBpmn } from './bpmn/bpmnConverter.jsx';
import CopyButton from "../../components/CopyButton.jsx";

const RulesTab = ({ request }) => {
    const [rulesData, setRulesData] = useState([]);
    const [hasFlows, setHasFlows] = useState(false);
    const [loading, setLoading] = useState(true);
    const [openRows, setOpenRows] = useState([]);

    useEffect(() => {
        setLoading(true);

        const flows = request.audits?.find(a => a.type === "kengine.flows");
        setHasFlows(!!flows);

        if (flows) {
            try {
                const events = Array.isArray(flows.events) ? flows.events : [flows.events];
                const parsedEvents = events.map((event, index) => {
                    const eventData = typeof event === "string" ? JSON.parse(event) : event;

                    // Extract rule execution data
                    const executionRules = eventData.executionEDTDTO?.executionRules || [];
                    const rulesMap = eventData.executionEDTDTO?.rules || {};

                    const rules = executionRules.map(rule => ({
                        name: rule.name,
                        steps: rulesMap[rule.name] || [],
                        executed: rule.executed || false,
                        startTime: rule.startTime || null,
                        endTime: rule.endTime || null,
                        duration: rule.endTime && rule.startTime ? rule.endTime - rule.startTime : null
                    }));

                    return {
                        eventIndex: index,
                        rules: rules,
                        totalRules: rules.length,
                        executedRules: rules.filter(r => r.executed).length,
                        rawData: eventData
                    };
                }).filter(event => event.rules.length > 0);

                setRulesData(parsedEvents);
            } catch (err) {
                console.error('Failed to parse rules data', err);
                setRulesData([]);
            }
        }

        setLoading(false);
    }, [request]);

    const toggleRow = (eventIndex, ruleIndex) => {
        const rowId = `${eventIndex}-${ruleIndex}`;
        setOpenRows(prevState =>
            prevState.includes(rowId)
                ? prevState.filter(id => id !== rowId)
                : [...prevState, rowId]
        );
    };

    const getTotalStats = () => {
        return rulesData.reduce((acc, event) => ({
            totalRules: acc.totalRules + event.totalRules,
            executedRules: acc.executedRules + event.executedRules
        }), { totalRules: 0, executedRules: 0 });
    };

    const generateRuleDiagram = (rule) => {
        try {
            // Create a minimal flow structure for individual rule
            const mockFlow = {
                events: [{
                    executionEDTDTO: {
                        executionRules: [{ name: rule.name }],
                        rules: { [rule.name]: rule.steps }
                    }
                }]
            };
            return jsonToBpmn(mockFlow);
        } catch (err) {
            console.error('Failed to generate rule diagram', err);
            return null;
        }
    };

    const getRuleStatusColor = (executed) => {
        return executed
            ? 'bg-green-100 text-green-800 border-green-300'
            : 'bg-gray-100 text-gray-800 border-gray-300';
    };

    const getDurationColor = (duration) => {
        if (!duration) return 'text-gray-600 bg-gray-50 border-gray-200';
        if (duration < 10) return 'text-green-600 bg-green-50 border-green-200';
        if (duration < 50) return 'text-blue-600 bg-blue-50 border-blue-200';
        if (duration < 100) return 'text-orange-600 bg-orange-50 border-orange-200';
        return 'text-red-600 bg-red-50 border-red-200';
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center py-12">
                <Loader className="w-6 h-6 text-blue-600 animate-spin" />
                <span className="ml-3 text-gray-600">Loading rules data...</span>
            </div>
        );
    }

    if (!hasFlows || rulesData.length === 0) {
        return (
            <div className="space-y-4">
                <div className="bg-gradient-to-r from-orange-50 to-yellow-50 border border-orange-200 rounded-lg p-6">
                    <div className="flex items-center gap-4">
                        <AlertCircle className="w-8 h-8 text-orange-600" />
                        <div>
                            <h3 className="text-lg font-semibold text-orange-900">No Rules Execution Data</h3>
                            <p className="text-orange-700 mt-1">No business rules were executed for this audit request.</p>
                        </div>
                    </div>
                </div>

                <div className="bg-white border border-gray-200 rounded-lg p-6">
                    <div className="text-center py-8">
                        <GitBranch className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                        <h4 className="text-gray-600 font-medium">Rules Engine Not Active</h4>
                        <p className="text-gray-500 text-sm mt-2">This request did not trigger any business rules or workflows.</p>
                    </div>
                </div>
            </div>
        );
    }

    const stats = getTotalStats();

    return (
        <div className="bg-white shadow-sm overflow-hidden sm:rounded-lg border border-gray-200">
            {/* Header */}
            <div className="px-6 py-4 border-b border-gray-200 bg-gradient-to-r from-blue-50 to-indigo-50">
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <Workflow className="w-5 h-5 text-blue-600" />
                        <h3 className="text-lg font-semibold text-gray-900">Rules Execution</h3>
                    </div>
                    <div className="flex items-center gap-3">
                        <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                            {stats.totalRules} {stats.totalRules === 1 ? 'rule' : 'rules'}
                        </span>
                        <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                            {stats.executedRules} executed
                        </span>
                    </div>
                </div>
            </div>

            {/* Rules Table */}
            <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                        <tr>
                            <th className="w-10 px-3 py-3"></th>
                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Rule Name</th>
                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Steps</th>
                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Duration</th>
                        </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                        {rulesData.flatMap((event, eventIndex) =>
                            event.rules.map((rule, ruleIndex) => {
                                const rowId = `${eventIndex}-${ruleIndex}`;
                                const isOpen = openRows.includes(rowId);

                                return (
                                    <React.Fragment key={rowId}>
                                        <tr className={`hover:bg-gray-50 transition-colors duration-150 ${isOpen ? 'bg-blue-50/50' : ''}`}>
                                            <td className="px-3 py-4 text-center">
                                                <button
                                                    onClick={() => toggleRow(eventIndex, ruleIndex)}
                                                    className="p-1.5 rounded-md hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors"
                                                    aria-expanded={isOpen}
                                                    aria-label={isOpen ? 'Collapse details' : 'Expand details'}
                                                >
                                                    {isOpen ? (
                                                        <ChevronDown className="h-4 w-4 text-gray-600" />
                                                    ) : (
                                                        <ChevronRight className="h-4 w-4 text-gray-600" />
                                                    )}
                                                </button>
                                            </td>
                                            <td className="px-4 py-4">
                                                <div className="flex items-center gap-2">
                                                    <GitBranch className="w-4 h-4 text-gray-500" />
                                                    <span className="font-medium text-gray-900">{rule.name}</span>
                                                    <CopyButton value={rule.name} />
                                                </div>
                                            </td>
                                            <td className="px-4 py-4">
                                                <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-bold border-2 ${getRuleStatusColor(rule.executed)}`}>
                                                    {rule.executed ? (
                                                        <>
                                                            <CheckCircle className="w-3 h-3 mr-1" />
                                                            Executed
                                                        </>
                                                    ) : (
                                                        <>
                                                            <PlayCircle className="w-3 h-3 mr-1" />
                                                            Ready
                                                        </>
                                                    )}
                                                </span>
                                            </td>
                                            <td className="px-4 py-4">
                                                <span className="text-sm font-medium text-gray-700">
                                                    {rule.steps.length} {rule.steps.length === 1 ? 'step' : 'steps'}
                                                </span>
                                            </td>
                                            <td className="px-4 py-4">
                                                {rule.duration !== null ? (
                                                    <div className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-lg border ${getDurationColor(rule.duration)}`}>
                                                        <Timer className="w-3.5 h-3.5" />
                                                        <span className="text-xs font-bold">{rule.duration}ms</span>
                                                    </div>
                                                ) : (
                                                    <span className="text-xs text-gray-400">N/A</span>
                                                )}
                                            </td>
                                        </tr>
                                        {isOpen && (
                                            <tr>
                                                <td colSpan={5} className="bg-gradient-to-r from-blue-50 to-indigo-50">
                                                    <div className="p-6">
                                                        <div className="space-y-6">
                                                            {/* Rule Details */}
                                                            <div>
                                                                <div className="flex items-center gap-2 pb-2 border-b-2 border-blue-300 mb-4">
                                                                    <Workflow className="w-5 h-5 text-blue-600" />
                                                                    <h3 className="text-base font-bold text-blue-900 uppercase tracking-wide">Rule Details</h3>
                                                                </div>

                                                                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                                                                    {/* Rule Steps */}
                                                                    <div className="bg-white rounded-lg shadow-sm border border-gray-200">
                                                                        <div className="flex items-center justify-between px-4 py-2 bg-gray-50 border-b border-gray-200">
                                                                            <h4 className="text-xs font-semibold text-gray-700 uppercase tracking-wide">
                                                                                Rule Steps ({rule.steps.length})
                                                                            </h4>
                                                                            <CopyButton value={JSON.stringify(rule.steps, null, 2)} />
                                                                        </div>
                                                                        <div className="p-4 max-h-64 overflow-auto">
                                                                            {rule.steps.length > 0 ? (
                                                                                <div className="space-y-2">
                                                                                    {rule.steps.map((step, index) => (
                                                                                        <div key={index} className="border border-gray-100 rounded p-3 bg-gray-50">
                                                                                            <div className="flex items-center gap-2 mb-2">
                                                                                                <span className="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded">
                                                                                                    Step {index + 1}
                                                                                                </span>
                                                                                                <span className="text-xs bg-purple-100 text-purple-800 px-2 py-1 rounded">
                                                                                                    {step.actionPredicate || 'ACTION'}
                                                                                                </span>
                                                                                            </div>
                                                                                            <div className="text-sm font-medium text-gray-800">
                                                                                                {step.name || step.detail || 'Unnamed step'}
                                                                                            </div>
                                                                                            {step.condition && (
                                                                                                <div className="text-xs text-gray-600 mt-1">
                                                                                                    Condition: {step.condition}
                                                                                                </div>
                                                                                            )}
                                                                                        </div>
                                                                                    ))}
                                                                                </div>
                                                                            ) : (
                                                                                <span className="text-gray-400 italic text-sm">No steps defined</span>
                                                                            )}
                                                                        </div>
                                                                    </div>

                                                                    {/* Rule Diagram */}
                                                                    <div className="bg-white rounded-lg shadow-sm border border-gray-200">
                                                                        <div className="px-4 py-2 bg-gray-50 border-b border-gray-200">
                                                                            <h4 className="text-xs font-semibold text-gray-700 uppercase tracking-wide">Process Flow Diagram</h4>
                                                                        </div>
                                                                        <div className="p-4">
                                                                            {(() => {
                                                                                const diagram = generateRuleDiagram(rule);
                                                                                return diagram ? (
                                                                                    <div className="overflow-x-auto">
                                                                                        <div style={{ minWidth: '600px' }}>
                                                                                            <BpmnViewer xml={diagram} />
                                                                                        </div>
                                                                                    </div>
                                                                                ) : (
                                                                                    <div className="text-center py-8">
                                                                                        <GitBranch className="w-8 h-8 text-gray-400 mx-auto mb-2" />
                                                                                        <p className="text-sm text-gray-500">Unable to generate diagram</p>
                                                                                    </div>
                                                                                );
                                                                            })()}
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </td>
                                            </tr>
                                        )}
                                    </React.Fragment>
                                );
                            })
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default RulesTab;