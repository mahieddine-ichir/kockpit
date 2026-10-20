import React, { useEffect, useMemo, useState } from 'react';
import { AlertCircle, ChartGantt, LayoutGrid, Workflow } from 'lucide-react';
import FlowView from './rules/FlowView.jsx';
import TimelineView from './rules/TimelineView.jsx';
import MapView from './rules/MapView.jsx';
import DetailsPanel from './rules/DetailsPanel.jsx';
import { ErrorBlock } from './rules/RulesCommon.jsx';
import { byStartTime, computeTimeWindow, count, indexNodes, isError, parseExecutions, ruleFailed } from './rules/rulesModel.js';

// The views of a rules execution, picked at the top right of the tab. Each one gets the same
// props - { executions, timeWindow, nodes, selected, onSelect } - and keeps its own display
// state (collapsed rules, expanded nodes); the selection and the details panel are shared, so
// switching views keeps what is selected. Adding a view = one component + one entry here.
const VIEWS = [
    { id: 'flow', label: 'Flow', icon: <Workflow className="w-3.5 h-3.5" />, title: 'Vertical flow: rules as blocks, steps chained inside', component: FlowView },
    { id: 'timeline', label: 'Timeline', icon: <ChartGantt className="w-3.5 h-3.5" />, title: 'Compact tree with a Gantt bar per execution, rule and step', component: TimelineView },
    { id: 'map', label: 'Map', icon: <LayoutGrid className="w-3.5 h-3.5" />, title: 'Overview: rules as tiles sized and colored by duration, a mark per step', component: MapView },
];

const VIEW_STORAGE_KEY = 'kockpit_rules_view';

// Shown until the user picks another view (then remembered): the overview reads best first
const DEFAULT_VIEW = 'map';

const loadView = () => {
    try {
        const stored = localStorage.getItem(VIEW_STORAGE_KEY);
        return VIEWS.some(view => view.id === stored) ? stored : DEFAULT_VIEW;
    } catch {
        return DEFAULT_VIEW;
    }
};

const saveView = (id) => {
    try {
        localStorage.setItem(VIEW_STORAGE_KEY, id);
    } catch {
        // storage unavailable: the view just isn't remembered
    }
};

const ViewSwitcher = ({ value, onChange }) => (
    <div role="radiogroup" aria-label="Rules view" className="inline-flex rounded-lg border border-gray-200 bg-gray-50 p-0.5 shadow-sm">
        {VIEWS.map(({ id, label, icon, title }) => {
            const active = id === value;
            return (
                <button
                    key={id}
                    type="button"
                    role="radio"
                    aria-checked={active}
                    title={title}
                    onClick={() => onChange(id)}
                    className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-blue-400 ${active
                        ? 'bg-white text-blue-700 shadow-sm'
                        : 'text-gray-500 hover:text-gray-800'}`}
                >
                    {icon}
                    {label}
                </button>
            );
        })}
    </div>
);

const Summary = ({ executions }) => {
    const rules = executions.flatMap(e => e.rules);
    const steps = rules.reduce((n, rule) => n + rule.steps.length, 0);
    const failed = rules.filter(ruleFailed).length + executions.filter(e => isError(e.error)).length;
    return (
        <div className="text-sm text-gray-600">
            {executions.length > 1 && <>{count(executions.length, 'execution')} · </>}
            {count(rules.length, 'rule')} · {count(steps, 'step')}
            {failed > 0 && <span className="ml-2 font-semibold text-red-700">{failed} in error</span>}
        </div>
    );
};

const RulesTab = ({ request }) => {
    const { executions, parseError } = useMemo(() => {
        try {
            // every view shows the executions in the order they started
            return { executions: byStartTime(parseExecutions(request)), parseError: null };
        } catch (err) {
            console.error('Failed to parse rules data', err);
            return { executions: [], parseError: err };
        }
    }, [request]);

    const timeWindow = useMemo(() => computeTimeWindow(executions), [executions]);
    const nodes = useMemo(() => indexNodes(executions), [executions]);

    const [view, setView] = useState(loadView);
    const [selected, setSelected] = useState(null);

    // Reset the selection whenever another audit is displayed
    useEffect(() => {
        setSelected(executions[0]?.rules[0]?.key ?? executions[0]?.key ?? null);
    }, [executions]);

    const changeView = (id) => {
        saveView(id);
        setView(id);
    };

    if (parseError) {
        return (
            <ErrorBlock message="Unable to parse the rules execution data" details={parseError.message} />
        );
    }

    if (executions.length === 0) {
        return (
            <div className="bg-gradient-to-r from-orange-50 to-yellow-50 border border-orange-200 rounded-lg p-6">
                <div className="flex items-center gap-4">
                    <AlertCircle className="w-8 h-8 text-orange-600" />
                    <div>
                        <h3 className="text-lg font-semibold text-orange-900">No Rules Execution Data</h3>
                        <p className="text-orange-700 mt-1">This request did not trigger any business rules.</p>
                    </div>
                </div>
            </div>
        );
    }

    const View = (VIEWS.find(v => v.id === view) || VIEWS.find(v => v.id === DEFAULT_VIEW)).component;

    return (
        <div className="space-y-4">
            <div className="flex flex-wrap items-center justify-between gap-3">
                <Summary executions={executions} />
                <ViewSwitcher value={view} onChange={changeView} />
            </div>
            <div className="grid grid-cols-1 xl:grid-cols-[minmax(0,1fr)_24rem] gap-6 items-start">
                <View executions={executions} timeWindow={timeWindow} nodes={nodes} selected={selected} onSelect={setSelected} />
                <div className="xl:sticky xl:top-4">
                    <DetailsPanel node={nodes.get(selected)} timeWindow={timeWindow} onSelect={setSelected} />
                </div>
            </div>
        </div>
    );
};

export default RulesTab;
