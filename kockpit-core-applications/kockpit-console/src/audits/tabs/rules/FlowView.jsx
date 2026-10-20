import React, { useEffect, useMemo, useState } from 'react';
import { GitBranch, Workflow, XCircle, ChevronDown, ChevronRight, ChevronsUpDown, ChevronsDownUp, Zap, HelpCircle, Play, Flag } from 'lucide-react';
import { FailedIcon, StatusBadge, TimelineBar } from './RulesCommon.jsx';
import { FAILED_BORDER } from './failedStyle.js';
import { count, initiallyCollapsedRules, isError, ruleFailed, shortName, stepName } from './rulesModel.js';

// Vertical flow of the whole request, from START to END: each rule is a block named after it,
// its steps chained inside, a predicate's outcome written on the arrow leaving it, and a
// timeline column alongside where every rule and step has its bar on the same time scale.

// Each rule block gets its own tone (cycled), so consecutive rules stay apart in the flow and
// their timeline bars match their block; a failed rule is always red, dashed and marked with an X.
const RULE_TONES = [
    { box: 'border-blue-200 bg-blue-50/40', header: 'bg-blue-100/80 text-blue-900', bar: 'bg-blue-500' },
    { box: 'border-violet-200 bg-violet-50/40', header: 'bg-violet-100/80 text-violet-900', bar: 'bg-violet-500' },
    { box: 'border-teal-200 bg-teal-50/40', header: 'bg-teal-100/80 text-teal-900', bar: 'bg-teal-500' },
    { box: 'border-amber-200 bg-amber-50/40', header: 'bg-amber-100/80 text-amber-900', bar: 'bg-amber-500' },
    { box: 'border-cyan-200 bg-cyan-50/40', header: 'bg-cyan-100/80 text-cyan-900', bar: 'bg-cyan-500' },
    { box: 'border-fuchsia-200 bg-fuchsia-50/40', header: 'bg-fuchsia-100/80 text-fuchsia-900', bar: 'bg-fuchsia-500' },
];
const ERROR_TONE = { box: `${FAILED_BORDER} bg-red-50/40`, header: 'bg-red-100 text-red-900', bar: 'bg-red-500' };

const ROW_GRID = 'grid grid-cols-[minmax(0,1fr)_11rem] gap-x-4 items-center';

// The flow is a flat list of rows - terminals, connectors, rule headers, steps, rule ends - so
// every row shares the same two columns and the timeline bars line up across rule blocks.
const buildRows = (executions, collapsed) => {
    const rows = [{ kind: 'start', key: 'start' }];
    const showExecutions = executions.length > 1;
    let ruleIndex = 0;

    executions.forEach(execution => {
        if (showExecutions) {
            rows.push({ kind: 'connector', key: `${execution.key}-in` });
            rows.push({ kind: 'execution', key: execution.key, execution });
        }
        execution.rules.forEach(rule => {
            const tone = ruleFailed(rule) ? ERROR_TONE : RULE_TONES[ruleIndex++ % RULE_TONES.length];
            const isCollapsed = collapsed.has(rule.key);
            rows.push({ kind: 'connector', key: `${rule.key}-in` });
            rows.push({ kind: 'rule', key: rule.key, rule, tone, collapsed: isCollapsed });
            if (!isCollapsed) {
                rule.steps.forEach((step, index) => {
                    if (index > 0) {
                        const previous = rule.steps[index - 1];
                        const outcome = previous.actionPredicate === 'PREDICATE' ? previous.condition : null;
                        rows.push({ kind: 'stepConnector', key: `${step.key}-in`, tone, outcome, failed: isError(previous.error) });
                    }
                    rows.push({ kind: 'step', key: step.key, step, index, rule, tone });
                });
            }
            rows.push({ kind: 'ruleEnd', key: `${rule.key}-end`, rule, tone, collapsed: isCollapsed });
        });
    });

    rows.push({ kind: 'connector', key: 'end-in' });
    rows.push({ kind: 'end', key: 'end', failed: executions.some(e => isError(e.error) || e.rules.some(ruleFailed)) });
    return rows;
};

const Arrow = ({ failed, height = 32 }) => {
    const stroke = failed ? '#f87171' : '#94a3b8';
    return (
        <svg width="16" height={height} viewBox={`0 0 16 ${height}`} className="overflow-visible" aria-hidden="true">
            <line x1="8" y1="0" x2="8" y2={height - 8} stroke={stroke} strokeWidth="2" strokeDasharray="4 4" className="flow-dash" />
            <path d={`M3 ${height - 10} L8 ${height - 2} L13 ${height - 10}`} fill="none" stroke={stroke} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
    );
};

const Terminal = ({ label, icon, tone }) => (
    <div className="flex justify-center py-1">
        <div className={`inline-flex items-center gap-2 px-4 py-1.5 rounded-full text-xs font-bold tracking-wider shadow-sm ${tone}`}>
            {icon}
            {label}
        </div>
    </div>
);

const StepCard = ({ step, index, selected, onSelect }) => {
    const isPredicate = step.actionPredicate === 'PREDICATE';
    const failed = isError(step.error);
    const name = stepName(step);

    return (
        <button
            type="button"
            onClick={() => onSelect(step.key)}
            title={name}
            aria-pressed={selected}
            className={`w-full max-w-md mx-auto flex items-center gap-3 px-3 py-2 rounded-lg text-left shadow-sm transition-all hover:shadow-md focus:outline-none focus:ring-2 focus:ring-blue-400 ${failed
                ? `border-2 ${FAILED_BORDER} bg-red-50`
                : 'border border-gray-200 bg-white'} ${selected ? 'ring-2 ring-blue-500 ring-offset-1' : ''}`}
        >
            {isPredicate ? (
                // Diamond marks a decision, like a flowchart gateway
                <span className="shrink-0 w-7 h-7 flex items-center justify-center">
                    <span className={`w-5 h-5 rotate-45 rounded-[3px] border-2 flex items-center justify-center ${failed ? 'bg-red-100 border-red-300' : 'bg-amber-100 border-amber-300'}`}>
                        <HelpCircle className={`w-3 h-3 -rotate-45 ${failed ? 'text-red-700' : 'text-amber-700'}`} />
                    </span>
                </span>
            ) : (
                <span className={`shrink-0 w-7 h-7 rounded-md flex items-center justify-center ${failed ? 'bg-red-100 text-red-700' : 'bg-purple-100 text-purple-700'}`}>
                    <Zap className="w-3.5 h-3.5" />
                </span>
            )}
            <span className="min-w-0 flex-1">
                <span className="block text-[10px] font-semibold uppercase tracking-wider text-gray-400">
                    #{index + 1} · {step.actionPredicate || 'ACTION'}
                </span>
                <span className="block text-sm font-medium text-gray-900 truncate">{shortName(name)}</span>
                {failed && (
                    <span className="mt-0.5 flex items-center gap-1 text-xs text-red-700 truncate">
                        <XCircle className="w-3 h-3 shrink-0" />
                        {step.errorMessage || step.error}
                    </span>
                )}
            </span>
            <span className="shrink-0 text-xs font-semibold text-gray-500 tabular-nums">
                {step.time != null ? `${step.time} ms` : ''}
            </span>
        </button>
    );
};

const RuleHeader = ({ row, selected, onSelect, onToggle }) => {
    const { rule, tone, collapsed } = row;
    return (
        <div className={`rounded-t-xl border-2 border-b-0 ${tone.box}`}>
            <div
                role="button"
                tabIndex={0}
                onClick={() => onSelect(rule.key)}
                onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault();
                        onSelect(rule.key);
                    }
                }}
                aria-pressed={selected}
                title={rule.name}
                className={`flex items-center gap-2 px-3 py-2 rounded-t-[10px] cursor-pointer focus:outline-none focus:ring-2 focus:ring-blue-400 ${tone.header} ${selected ? 'ring-2 ring-inset ring-blue-500' : ''}`}
            >
                <button
                    type="button"
                    onClick={(e) => { e.stopPropagation(); onToggle(rule.key); }}
                    className="p-0.5 rounded hover:bg-white/60"
                    aria-label={collapsed ? 'Expand rule' : 'Collapse rule'}
                    aria-expanded={!collapsed}
                >
                    {collapsed ? <ChevronRight className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                </button>
                {ruleFailed(rule) ? <FailedIcon /> : <GitBranch className="w-4 h-4 shrink-0 opacity-70" />}
                <span className="text-sm font-semibold truncate">{shortName(rule.name)}</span>
                <span className="shrink-0 text-[11px] opacity-70">{count(rule.steps.length, 'step')}</span>
                <span className="ml-auto flex items-center gap-2 shrink-0">
                    <StatusBadge error={rule.error} small />
                    <span className="text-xs font-semibold tabular-nums">{rule.time != null ? `${rule.time} ms` : ''}</span>
                </span>
            </div>
        </div>
    );
};

const FlowRow = ({ row, timeWindow, selected, onSelect, onToggle }) => {
    let flow = null;
    let timeline = null;

    switch (row.kind) {
        case 'start':
            flow = <Terminal label="START" icon={<Play className="w-3.5 h-3.5" />} tone="bg-blue-600 text-white" />;
            break;
        case 'end':
            flow = <Terminal
                label={row.failed ? 'END · ERROR' : 'END'}
                icon={row.failed ? <XCircle className="w-3.5 h-3.5" /> : <Flag className="w-3.5 h-3.5" />}
                tone={row.failed ? 'bg-red-600 text-white' : 'bg-green-600 text-white'} />;
            break;
        case 'connector':
            flow = <div className="flex justify-center"><Arrow height={28} /></div>;
            break;
        case 'execution': {
            const { execution } = row;
            flow = (
                <button
                    type="button"
                    onClick={() => onSelect(execution.key)}
                    aria-pressed={selected === execution.key}
                    // dark and solid, unlike the light rule blocks under it: a flow reads as the
                    // heading of the rules it runs
                    className={`w-full flex items-center gap-2 px-3 py-2 rounded-lg bg-slate-800 text-left shadow-sm hover:bg-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-400 ${selected === execution.key ? 'ring-2 ring-blue-500 ring-offset-2' : ''}`}
                >
                    <Workflow className="w-4 h-4 shrink-0 text-sky-300" />
                    <span className="shrink-0 text-[10px] font-semibold uppercase tracking-wider text-slate-400">Flow</span>
                    <span className="text-sm font-semibold text-white truncate">{execution.name || 'Unnamed execution'}</span>
                    <span className="ml-auto flex items-center gap-2 shrink-0">
                        <StatusBadge error={execution.error} small />
                        <span className="text-xs font-semibold text-slate-200 tabular-nums">{execution.time != null ? `${execution.time} ms` : ''}</span>
                    </span>
                </button>
            );
            timeline = <TimelineBar node={execution} timeWindow={timeWindow} tone="bg-slate-800" />;
            break;
        }
        case 'rule':
            flow = <RuleHeader row={row} selected={selected === row.rule.key} onSelect={onSelect} onToggle={onToggle} />;
            timeline = <TimelineBar node={row.rule} timeWindow={timeWindow} tone={row.tone.bar} />;
            break;
        case 'stepConnector':
            flow = (
                <div className={`relative flex justify-center border-x-2 ${row.tone.box}`}>
                    <Arrow failed={row.failed} height={24} />
                    {row.outcome != null && (
                        <span className={`absolute left-1/2 top-1/2 -translate-y-1/2 ml-4 text-[11px] font-mono font-semibold px-1.5 py-0.5 rounded border ${row.outcome
                            ? 'bg-green-50 text-green-700 border-green-200'
                            : 'bg-orange-50 text-orange-700 border-orange-200'}`}>
                            {String(row.outcome)}
                        </span>
                    )}
                </div>
            );
            break;
        case 'step':
            flow = (
                <div className={`border-x-2 px-4 ${row.index === 0 ? 'pt-3' : ''} ${row.tone.box}`}>
                    <StepCard step={row.step} index={row.index} selected={selected === row.step.key} onSelect={onSelect} />
                </div>
            );
            timeline = <TimelineBar node={row.step} timeWindow={timeWindow} tone={row.tone.bar} />;
            break;
        case 'ruleEnd':
            flow = (
                <div className={`rounded-b-xl border-2 border-t-0 ${row.tone.box} ${row.collapsed ? 'px-3 py-2' : 'h-3'}`}>
                    {row.collapsed && (
                        <button
                            type="button"
                            onClick={() => onToggle(row.rule.key)}
                            className="w-full text-xs text-gray-500 hover:text-gray-800 text-center"
                        >
                            {row.rule.steps.length > 0 ? `${count(row.rule.steps.length, 'step')} hidden - show` : 'No steps recorded'}
                        </button>
                    )}
                </div>
            );
            break;
        default:
            break;
    }

    return (
        <div className={ROW_GRID}>
            <div className="min-w-0">{flow}</div>
            <div>{timeline}</div>
        </div>
    );
};

const FlowView = ({ executions, timeWindow, nodes, selected, onSelect }) => {
    const ruleKeys = useMemo(() => executions.flatMap(e => e.rules.map(r => r.key)), [executions]);
    const [collapsed, setCollapsed] = useState(() => new Set(initiallyCollapsedRules(executions)));

    useEffect(() => {
        setCollapsed(new Set(initiallyCollapsedRules(executions)));
    }, [executions]);

    // A step selected elsewhere (details panel, another view) opens its rule
    useEffect(() => {
        const node = nodes.get(selected);
        if (node?.kind !== 'step') return;
        setCollapsed(prev => {
            if (!prev.has(node.rule.key)) return prev;
            const next = new Set(prev);
            next.delete(node.rule.key);
            return next;
        });
    }, [selected, nodes]);

    const rows = useMemo(() => buildRows(executions, collapsed), [executions, collapsed]);

    const toggle = (key) => setCollapsed(prev => {
        const next = new Set(prev);
        if (next.has(key)) next.delete(key);
        else next.add(key);
        return next;
    });

    const allExpanded = collapsed.size === 0;
    const toggleAll = () => setCollapsed(allExpanded ? new Set(ruleKeys) : new Set());

    return (
        <div className="bg-white shadow-sm sm:rounded-lg border border-gray-200 overflow-x-auto">
            <div className="min-w-[36rem]">
                <div className={`${ROW_GRID} px-4 py-3 border-b border-gray-200 bg-gray-50 text-xs font-medium text-gray-500`}>
                    <div className="flex items-center gap-2">
                        <button
                            type="button"
                            onClick={toggleAll}
                            className="p-0.5 rounded hover:bg-gray-200"
                            title={allExpanded ? 'Collapse all rules' : 'Expand all rules'}
                            aria-label={allExpanded ? 'Collapse all rules' : 'Expand all rules'}
                        >
                            {allExpanded ? <ChevronsDownUp className="w-4 h-4 text-gray-600" /> : <ChevronsUpDown className="w-4 h-4 text-gray-600" />}
                        </button>
                        <span className="uppercase tracking-wider">Flow</span>
                    </div>
                    <div>
                        <div className="uppercase tracking-wider mb-0.5">Timeline</div>
                        <div className="flex justify-between text-gray-400 tabular-nums">
                            <span>0 ms</span>
                            <span>{timeWindow ? `${timeWindow.span} ms` : ''}</span>
                        </div>
                    </div>
                </div>
                <div className="px-4 py-4 bg-[radial-gradient(circle,_#eef0f3_1px,_transparent_1px)] [background-size:16px_16px]">
                    {rows.map(row => (
                        <FlowRow key={row.key} row={row} timeWindow={timeWindow} selected={selected} onSelect={onSelect} onToggle={toggle} />
                    ))}
                </div>
            </div>
        </div>
    );
};

export default FlowView;
