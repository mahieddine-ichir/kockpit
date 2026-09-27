import React, { useEffect, useMemo, useRef, useState } from 'react';
import { GitBranch, Workflow, AlertCircle, CheckCircle, XCircle, ChevronDown, ChevronRight, ChevronsUpDown, ChevronsDownUp, Timer, Zap, HelpCircle, Database, FileText } from 'lucide-react';
import RuleFlowDiagram from './RuleFlowDiagram.jsx';
import { dateFormat } from './auditUtils.js';
import CopyButton from "../../components/CopyButton.jsx";

const VALID = 'VALID';

// Above this many steps, rules start collapsed in the timeline
const AUTO_EXPAND_MAX_STEPS = 30;

// Steps carry error=null when fine, rules/executions carry error="VALID"
const isError = (error) => error != null && error !== VALID;

// "com.acme.HelloRule.HelloDefault" -> "HelloDefault"; plain names are kept as is
const shortName = (name) => {
    if (!name || /\s/.test(name) || !name.includes('.')) return name;
    return name.substring(name.lastIndexOf('.') + 1);
};

const parseExecutions = (request) => {
    const flows = request.audits?.find(a => a.type === "kengine.flows");
    if (!flows?.events) return [];

    const events = Array.isArray(flows.events) ? flows.events : [flows.events];
    return events.map((event, index) => {
        const eventData = typeof event === "string" ? JSON.parse(event) : event;
        const execution = eventData.executionEDTDTO || {};
        const rulesMap = execution.rules || {};

        return {
            key: `e${index}`,
            name: eventData.executionName,
            uuid: execution.executionUUID,
            error: execution.error,
            errorMessage: execution.errorMessage,
            errorDetails: execution.errorDetails,
            date: execution.date ?? eventData.startTime,
            time: execution.time ?? (eventData.endTime != null && eventData.startTime != null ? eventData.endTime - eventData.startTime : null),
            registryId: execution.fullRegistryReferentialId || execution.registryId,
            referential: execution.referential,
            logs: execution.logs,
            rules: (execution.executionRules || []).map((rule, ruleIndex) => ({
                key: `e${index}-r${ruleIndex}`,
                name: rule.name,
                detail: rule.detail,
                error: rule.error,
                errorMessage: rule.errorMessage,
                errorDetails: rule.errorDetails,
                date: rule.date,
                time: rule.time,
                steps: (rulesMap[rule.name] || []).map((step, stepIndex) => ({
                    ...step,
                    key: `e${index}-r${ruleIndex}-s${stepIndex}`
                }))
            }))
        };
    });
};

// Time window covering every execution, rule and step, used to place the timeline bars
const computeTimeWindow = (executions) => {
    let start = Infinity;
    let end = -Infinity;
    const visit = (node) => {
        if (node.date == null) return;
        start = Math.min(start, node.date);
        end = Math.max(end, node.date + (node.time || 0));
    };
    executions.forEach(execution => {
        visit(execution);
        execution.rules.forEach(rule => {
            visit(rule);
            rule.steps.forEach(visit);
        });
    });
    if (start === Infinity) return null;
    return { start, span: Math.max(end - start, 1) };
};

const buildTree = (executions) => executions.map(execution => ({
    key: execution.key,
    kind: 'execution',
    data: execution,
    children: execution.rules.map(rule => ({
        key: rule.key,
        kind: 'rule',
        data: rule,
        execution,
        children: rule.steps.map((step, index) => ({
            key: step.key,
            kind: 'step',
            data: step,
            index,
            rule,
            children: []
        }))
    }))
}));

const collectKeys = (nodes, predicate) => nodes.flatMap(node => [
    ...(node.children.length > 0 && predicate(node) ? [node.key] : []),
    ...collectKeys(node.children, predicate)
]);

const getDurationColor = (duration) => {
    if (duration == null) return 'text-gray-600 bg-gray-50 border-gray-200';
    if (duration < 10) return 'text-green-600 bg-green-50 border-green-200';
    if (duration < 50) return 'text-blue-600 bg-blue-50 border-blue-200';
    if (duration < 100) return 'text-orange-600 bg-orange-50 border-orange-200';
    return 'text-red-600 bg-red-50 border-red-200';
};

const StatusBadge = ({ error }) => {
    const failed = isError(error);
    return (
        <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-bold border-2 ${failed
            ? 'bg-red-100 text-red-800 border-red-300'
            : 'bg-green-100 text-green-800 border-green-300'}`}>
            {failed ? <XCircle className="w-3 h-3 mr-1" /> : <CheckCircle className="w-3 h-3 mr-1" />}
            {error || VALID}
        </span>
    );
};

const Duration = ({ value }) => value != null ? (
    <div className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-lg border ${getDurationColor(value)}`}>
        <Timer className="w-3.5 h-3.5" />
        <span className="text-xs font-bold">{value}ms</span>
    </div>
) : (
    <span className="text-xs text-gray-400">N/A</span>
);

const ErrorBlock = ({ message, details }) => {
    if (!message && !details) return null;
    const detailsText = typeof details === 'string' ? details : JSON.stringify(details, null, 2);
    return (
        <div className="bg-red-50 border border-red-200 rounded-lg p-3">
            <div className="flex items-start gap-2">
                <AlertCircle className="w-4 h-4 text-red-600 mt-0.5 shrink-0" />
                <div className="min-w-0 flex-1">
                    {message && <p className="text-sm font-medium text-red-800 break-words">{message}</p>}
                    {details && (
                        <pre className="mt-1 text-xs text-red-700 whitespace-pre-wrap break-words max-h-40 overflow-auto">{detailsText}</pre>
                    )}
                </div>
            </div>
        </div>
    );
};

// For predicates, condition is the evaluated result; it is meaningless on actions
const ConditionChip = ({ step }) => {
    if (step.actionPredicate !== 'PREDICATE' || step.condition == null) return null;
    return (
        <span className={`shrink-0 text-xs font-mono font-semibold px-2 py-0.5 rounded border ${step.condition
            ? 'bg-green-50 text-green-700 border-green-200'
            : 'bg-orange-50 text-orange-700 border-orange-200'}`}>
            → {String(step.condition)}
        </span>
    );
};

const StepTypeChip = ({ step }) => {
    const isPredicate = step.actionPredicate === 'PREDICATE';
    return (
        <span className={`shrink-0 inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded ${isPredicate
            ? 'bg-amber-100 text-amber-800'
            : 'bg-purple-100 text-purple-800'}`}>
            {isPredicate ? <HelpCircle className="w-3 h-3" /> : <Zap className="w-3 h-3" />}
            {step.actionPredicate || 'ACTION'}
        </span>
    );
};

const StepItem = ({ step, index, highlighted }) => {
    const ref = useRef(null);
    const name = step.name || step.detail || 'Unnamed step';
    const failed = isError(step.error);

    useEffect(() => {
        if (highlighted) ref.current?.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
    }, [highlighted]);

    return (
        <li ref={ref} className={`border rounded p-3 ${failed ? 'border-red-200 bg-red-50' : 'border-gray-100 bg-gray-50'} ${highlighted ? 'ring-2 ring-blue-400' : ''}`}>
            <div className="flex flex-wrap items-center gap-2">
                <span className="text-xs bg-blue-100 text-blue-800 px-2 py-0.5 rounded">#{index + 1}</span>
                <StepTypeChip step={step} />
                <ConditionChip step={step} />
                {failed && (
                    <span className="text-xs bg-red-100 text-red-800 px-2 py-0.5 rounded">{step.error}</span>
                )}
                <span className="ml-auto text-xs text-gray-500">{step.time != null ? `${step.time}ms` : ''}</span>
            </div>
            <div className="mt-2 flex items-center gap-1 min-w-0">
                <span className="text-sm font-medium text-gray-800 truncate" title={name}>{shortName(name)}</span>
                <CopyButton value={name} />
            </div>
            {(step.errorMessage || step.errorDetails) && (
                <div className="mt-2">
                    <ErrorBlock message={step.errorMessage} details={step.errorDetails} />
                </div>
            )}
        </li>
    );
};

const PanelHeader = ({ children, copyValue }) => (
    <div className="flex items-center justify-between px-4 py-2 bg-gray-50 border-b border-gray-200">
        <h4 className="flex items-center gap-2 text-xs font-semibold text-gray-700 uppercase tracking-wide">{children}</h4>
        {copyValue && <CopyButton value={copyValue} />}
    </div>
);

const RuleDetails = ({ rule, highlightedStep, onSelectStep }) => (
    <div className="space-y-4">
        <ErrorBlock message={rule.errorMessage} details={rule.errorDetails} />

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div className="bg-white rounded-lg border border-gray-200">
                <PanelHeader copyValue={JSON.stringify(rule.steps, null, 2)}>
                    Execution Trace ({rule.steps.length})
                </PanelHeader>
                <div className="p-4 max-h-[44rem] overflow-auto">
                    {rule.steps.length > 0 ? (
                        <ol className="space-y-2">
                            {rule.steps.map((step, index) => (
                                <StepItem key={step.key} step={step} index={index} highlighted={highlightedStep === index} />
                            ))}
                        </ol>
                    ) : (
                        <span className="text-gray-400 italic text-sm">No steps recorded</span>
                    )}
                </div>
            </div>

            <div className="bg-white rounded-lg border border-gray-200">
                <PanelHeader>Execution Flow</PanelHeader>
                <div className="p-4 max-h-[44rem] overflow-auto bg-[radial-gradient(circle,_#e5e7eb_1px,_transparent_1px)] [background-size:16px_16px]">
                    <RuleFlowDiagram rule={rule} highlightedStep={highlightedStep} onSelectStep={onSelectStep} />
                </div>
            </div>
        </div>
    </div>
);

const JsonSection = ({ title, icon, value }) => {
    if (value == null) return null;
    const text = typeof value === 'string' ? value : JSON.stringify(value, null, 2);
    return (
        <div className="bg-white rounded-lg border border-gray-200">
            <PanelHeader copyValue={text}>{icon}{title}</PanelHeader>
            <pre className="p-4 text-xs text-gray-800 whitespace-pre-wrap break-words max-h-64 overflow-auto">{text}</pre>
        </div>
    );
};

const ExecutionDetails = ({ execution }) => (
    <div className="space-y-4">
        <dl className="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-2 text-sm">
            <div className="flex items-center gap-1 min-w-0">
                <dt className="text-gray-500">Execution ID:</dt>
                <dd className="font-mono text-gray-800 truncate">{execution.uuid || 'N/A'}</dd>
                <CopyButton value={execution.uuid} />
            </div>
            <div className="flex items-center gap-1 min-w-0">
                <dt className="text-gray-500">Registry:</dt>
                <dd className="font-mono text-gray-800 truncate">{execution.registryId || 'N/A'}</dd>
                <CopyButton value={execution.registryId ? String(execution.registryId) : null} />
            </div>
            <div className="flex items-center gap-1">
                <dt className="text-gray-500">Started:</dt>
                <dd className="text-gray-800">{execution.date ? dateFormat(execution.date) : 'N/A'}</dd>
            </div>
            <div className="flex items-center gap-1">
                <dt className="text-gray-500">Rules:</dt>
                <dd className="text-gray-800">{execution.rules.map(r => r.name).join(', ') || 'None'}</dd>
            </div>
        </dl>

        <ErrorBlock message={execution.errorMessage} details={execution.errorDetails} />

        {(execution.logs != null || execution.referential != null) && (
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                <JsonSection title="Logs" icon={<FileText className="w-3.5 h-3.5" />} value={execution.logs} />
                <JsonSection title="Referential" icon={<Database className="w-3.5 h-3.5" />} value={execution.referential} />
            </div>
        )}
    </div>
);

const TimelineBar = ({ node, timeWindow }) => {
    const { date, time, error } = node.data;
    if (!timeWindow || date == null) return <div className="h-2" />;

    const offset = date - timeWindow.start;
    const left = (offset / timeWindow.span) * 100;
    // Keep sub-millisecond work visible as a thin marker
    const width = Math.max(((time || 0) / timeWindow.span) * 100, 0.75);

    return (
        <div className="relative h-2.5 rounded-full bg-gray-200" title={`+${offset}ms → ${offset + (time || 0)}ms (${time ?? 0}ms)`}>
            <div
                className={`absolute inset-y-0 rounded-full ${isError(error) ? 'bg-red-500' : 'bg-green-500'}`}
                style={{ left: `${Math.min(left, 100 - width)}%`, width: `${width}%` }}
            />
        </div>
    );
};

const ROW_GRID = 'grid grid-cols-[minmax(0,1fr)_minmax(8rem,35%)_7rem_5.5rem] items-center gap-4';

const nodeLabel = (node) => {
    switch (node.kind) {
        case 'execution':
            return <><Workflow className="w-4 h-4 shrink-0 text-blue-600" /><span className="truncate">{node.data.name || 'Unnamed execution'}</span></>;
        case 'rule':
            return <><GitBranch className="w-4 h-4 shrink-0 text-gray-500" /><span className="truncate">{node.data.name}</span></>;
        default: {
            const name = node.data.name || node.data.detail || 'Unnamed step';
            return (
                <>
                    <StepTypeChip step={node.data} />
                    <span className="truncate" title={name}>{shortName(name)}</span>
                    <ConditionChip step={node.data} />
                </>
            );
        }
    }
};

const TimelineRow = ({ node, depth, timeWindow, expanded, selected, onToggle, onSelect }) => {
    const hasChildren = node.children.length > 0;
    const isOpen = expanded.has(node.key);
    const isSelected = selected === node.key;

    const handleKeyDown = (e) => {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            onSelect(node.key);
        } else if (hasChildren && e.key === 'ArrowRight' && !isOpen) {
            onToggle(node.key);
        } else if (hasChildren && e.key === 'ArrowLeft' && isOpen) {
            onToggle(node.key);
        }
    };

    return (
        <div
            role="treeitem"
            aria-level={depth + 1}
            aria-expanded={hasChildren ? isOpen : undefined}
            aria-selected={isSelected}
            tabIndex={0}
            onClick={() => onSelect(node.key)}
            onKeyDown={handleKeyDown}
            className={`${ROW_GRID} px-4 py-2 rounded-md cursor-pointer transition-colors focus:outline-none focus:ring-2 focus:ring-blue-400 ${isSelected ? 'bg-blue-50' : 'hover:bg-gray-50'}`}
        >
            <div className="flex items-center gap-2 min-w-0" style={{ paddingLeft: `${depth * 1.5}rem` }}>
                {hasChildren ? (
                    <button
                        type="button"
                        tabIndex={-1}
                        onClick={(e) => { e.stopPropagation(); onToggle(node.key); }}
                        className="p-0.5 rounded hover:bg-gray-200"
                        aria-label={isOpen ? 'Collapse' : 'Expand'}
                    >
                        {isOpen ? <ChevronDown className="w-4 h-4 text-gray-600" /> : <ChevronRight className="w-4 h-4 text-gray-600" />}
                    </button>
                ) : (
                    <span className="w-5 shrink-0" />
                )}
                <div className={`flex items-center gap-2 min-w-0 text-sm text-gray-900 ${isSelected ? 'font-semibold' : ''}`}>
                    {nodeLabel(node)}
                </div>
            </div>
            <TimelineBar node={node} timeWindow={timeWindow} />
            <div><StatusBadge error={node.data.error} /></div>
            <div className="text-right text-xs font-semibold text-gray-700">{node.data.time != null ? `${node.data.time} ms` : 'N/A'}</div>
        </div>
    );
};

const TimelineTree = ({ tree, timeWindow, expanded, selected, onToggle, onSelect, onToggleAll, allExpanded }) => {
    const renderNodes = (nodes, depth) => nodes.map(node => (
        <React.Fragment key={node.key}>
            <TimelineRow node={node} depth={depth} timeWindow={timeWindow} expanded={expanded} selected={selected} onToggle={onToggle} onSelect={onSelect} />
            {expanded.has(node.key) && renderNodes(node.children, depth + 1)}
        </React.Fragment>
    ));

    return (
        <div className="bg-white shadow-sm sm:rounded-lg border border-gray-200 overflow-x-auto">
            <div className="min-w-[44rem]">
                <div className={`${ROW_GRID} px-4 py-3 border-b border-gray-200 bg-gray-50 text-xs font-medium text-gray-500 uppercase tracking-wider`}>
                    <div className="flex items-center gap-2">
                        <button
                            type="button"
                            onClick={onToggleAll}
                            className="p-0.5 rounded hover:bg-gray-200 normal-case"
                            title={allExpanded ? 'Collapse all' : 'Expand all'}
                            aria-label={allExpanded ? 'Collapse all' : 'Expand all'}
                        >
                            {allExpanded ? <ChevronsDownUp className="w-4 h-4 text-gray-600" /> : <ChevronsUpDown className="w-4 h-4 text-gray-600" />}
                        </button>
                        Execution
                    </div>
                    <div className="flex justify-between normal-case">
                        <span>0 ms</span>
                        <span>{timeWindow ? `${timeWindow.span} ms` : ''}</span>
                    </div>
                    <div>Status</div>
                    <div className="text-right">Duration</div>
                </div>
                <div role="tree" className="p-2">
                    {renderNodes(tree, 0)}
                </div>
            </div>
        </div>
    );
};

const SelectionDetails = ({ node, onSelect }) => {
    if (!node) return null;

    const rule = node.kind === 'rule' ? node.data : node.rule;
    const title = node.kind === 'execution'
        ? (node.data.name || 'Unnamed execution')
        : rule.name;

    return (
        <div className="bg-white shadow-sm sm:rounded-lg border border-gray-200">
            <div className="flex flex-wrap items-center justify-between gap-3 px-6 py-4 border-b border-gray-200 bg-gradient-to-r from-blue-50 to-indigo-50">
                <div className="flex items-center gap-3 min-w-0">
                    {node.kind === 'execution'
                        ? <Workflow className="w-5 h-5 text-blue-600" />
                        : <GitBranch className="w-5 h-5 text-blue-600" />}
                    <h3 className="text-lg font-semibold text-gray-900 truncate">{title}</h3>
                    <StatusBadge error={node.kind === 'execution' ? node.data.error : rule.error} />
                </div>
                <Duration value={node.kind === 'execution' ? node.data.time : rule.time} />
            </div>
            <div className="p-6">
                {node.kind === 'execution'
                    ? <ExecutionDetails execution={node.data} />
                    : <RuleDetails
                        rule={rule}
                        highlightedStep={node.kind === 'step' ? node.index : null}
                        onSelectStep={(index) => onSelect(rule.steps[index].key)}
                    />}
            </div>
        </div>
    );
};

const findNode = (nodes, key) => {
    for (const node of nodes) {
        if (node.key === key) return node;
        const found = findNode(node.children, key);
        if (found) return found;
    }
    return null;
};

const RulesTab = ({ request }) => {
    const { executions, parseError } = useMemo(() => {
        try {
            return { executions: parseExecutions(request), parseError: null };
        } catch (err) {
            console.error('Failed to parse rules data', err);
            return { executions: [], parseError: err };
        }
    }, [request]);

    const tree = useMemo(() => buildTree(executions), [executions]);
    const timeWindow = useMemo(() => computeTimeWindow(executions), [executions]);
    const allKeys = useMemo(() => collectKeys(tree, () => true), [tree]);

    const [expanded, setExpanded] = useState(new Set());
    const [selected, setSelected] = useState(null);

    // Reset the view whenever another audit is displayed
    useEffect(() => {
        setExpanded(new Set(collectKeys(tree, node =>
            node.kind === 'execution' || node.children.length <= AUTO_EXPAND_MAX_STEPS)));
        setSelected(tree[0]?.children[0]?.key ?? tree[0]?.key ?? null);
    }, [tree]);

    const toggle = (key) => setExpanded(prev => {
        const next = new Set(prev);
        if (next.has(key)) next.delete(key);
        else next.add(key);
        return next;
    });

    const allExpanded = allKeys.length > 0 && allKeys.every(key => expanded.has(key));
    const toggleAll = () => setExpanded(allExpanded ? new Set() : new Set(allKeys));

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

    return (
        <div className="space-y-6">
            <TimelineTree
                tree={tree}
                timeWindow={timeWindow}
                expanded={expanded}
                selected={selected}
                onToggle={toggle}
                onSelect={setSelected}
                onToggleAll={toggleAll}
                allExpanded={allExpanded}
            />
            <SelectionDetails node={findNode(tree, selected)} onSelect={setSelected} />
        </div>
    );
};

export default RulesTab;
