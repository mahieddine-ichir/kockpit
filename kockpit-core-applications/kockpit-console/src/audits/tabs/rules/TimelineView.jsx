import React, { useEffect, useMemo, useState } from 'react';
import { GitBranch, Workflow, ChevronDown, ChevronRight, ChevronsUpDown, ChevronsDownUp } from 'lucide-react';
import { ConditionChip, FailedIcon, StatusBadge, StepTypeChip, TimelineBar } from './RulesCommon.jsx';
import { FAILED_BORDER } from './failedStyle.js';
import { AUTO_EXPAND_MAX_STEPS, isError, ruleFailed, shortName, stepName } from './rulesModel.js';

// Compact tree of executions > rules > steps, one line each, with a Gantt-like bar placing
// every node in the request's time window: the densest view, for large rule sets.

const buildTree = (executions) => executions.map(execution => ({
    key: execution.key,
    kind: 'execution',
    data: execution,
    children: execution.rules.map(rule => ({
        key: rule.key,
        kind: 'rule',
        data: rule,
        children: rule.steps.map(step => ({
            key: step.key,
            kind: 'step',
            data: step,
            children: []
        }))
    }))
}));

const collectKeys = (nodes, predicate) => nodes.flatMap(node => [
    ...(node.children.length > 0 && predicate(node) ? [node.key] : []),
    ...collectKeys(node.children, predicate)
]);

const initiallyExpanded = (tree) => new Set(collectKeys(tree, node =>
    node.kind === 'execution' || node.children.length <= AUTO_EXPAND_MAX_STEPS));

const ROW_GRID = 'grid grid-cols-[minmax(0,1fr)_minmax(8rem,35%)_7rem_5.5rem] items-center gap-4';

const nodeLabel = (node) => {
    switch (node.kind) {
        case 'execution':
            return (
                <>
                    <Workflow className="w-4 h-4 shrink-0 text-sky-300" />
                    <span className="shrink-0 text-[10px] font-semibold uppercase tracking-wider text-slate-400">Flow</span>
                    <span className="truncate font-semibold">{node.data.name || 'Unnamed execution'}</span>
                </>
            );
        case 'rule':
            return (
                <>
                    {ruleFailed(node.data) ? <FailedIcon /> : <GitBranch className="w-4 h-4 shrink-0 text-gray-500" />}
                    <span className="truncate" title={node.data.name}>{shortName(node.data.name)}</span>
                </>
            );
        default: {
            const name = stepName(node.data);
            return (
                <>
                    {isError(node.data.error) && <FailedIcon />}
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
    // a flow row is dark and solid, as in the Flow view: it reads as the heading of its rules
    // without relying on a color nuance
    const isFlow = node.kind === 'execution';
    // a failed rule or step gets a dashed frame and an X icon, not only a red badge
    const failed = !isFlow && (node.kind === 'rule' ? ruleFailed(node.data) : isError(node.data.error));

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
            className={`${ROW_GRID} px-4 py-2 rounded-md cursor-pointer transition-colors focus:outline-none focus:ring-2 focus:ring-blue-400 ${isFlow
                ? `bg-slate-800 hover:bg-slate-700 shadow-sm ${isSelected ? 'ring-2 ring-blue-500 ring-offset-2' : ''}`
                : `border-2 ${failed ? FAILED_BORDER : 'border-transparent'} ${isSelected ? 'bg-blue-50' : failed ? 'bg-red-50/60 hover:bg-red-50' : 'hover:bg-gray-50'}`}`}
        >
            <div className="flex items-center gap-2 min-w-0" style={{ paddingLeft: `${depth * 1.5}rem` }}>
                {hasChildren ? (
                    <button
                        type="button"
                        tabIndex={-1}
                        onClick={(e) => { e.stopPropagation(); onToggle(node.key); }}
                        className={`p-0.5 rounded ${isFlow ? 'hover:bg-slate-600' : 'hover:bg-gray-200'}`}
                        aria-label={isOpen ? 'Collapse' : 'Expand'}
                    >
                        {isOpen
                            ? <ChevronDown className={`w-4 h-4 ${isFlow ? 'text-slate-300' : 'text-gray-600'}`} />
                            : <ChevronRight className={`w-4 h-4 ${isFlow ? 'text-slate-300' : 'text-gray-600'}`} />}
                    </button>
                ) : (
                    <span className="w-5 shrink-0" />
                )}
                <div className={`flex items-center gap-2 min-w-0 text-sm ${isFlow ? 'text-white' : 'text-gray-900'} ${isSelected ? 'font-semibold' : ''}`}>
                    {nodeLabel(node)}
                </div>
            </div>
            {isFlow
                ? <TimelineBar node={node.data} timeWindow={timeWindow} tone="bg-sky-300" className="h-2.5 bg-slate-600" />
                : <TimelineBar node={node.data} timeWindow={timeWindow} className="h-2.5 bg-gray-200" />}
            <div><StatusBadge error={node.data.error} /></div>
            <div className={`text-right text-xs font-semibold ${isFlow ? 'text-slate-200' : 'text-gray-700'}`}>{node.data.time != null ? `${node.data.time} ms` : 'N/A'}</div>
        </div>
    );
};

const TimelineView = ({ executions, timeWindow, nodes, selected, onSelect }) => {
    const tree = useMemo(() => buildTree(executions), [executions]);
    const allKeys = useMemo(() => collectKeys(tree, () => true), [tree]);
    const [expanded, setExpanded] = useState(() => initiallyExpanded(tree));

    useEffect(() => {
        setExpanded(initiallyExpanded(tree));
    }, [tree]);

    // A node selected elsewhere (details panel, another view) opens its ancestors
    useEffect(() => {
        const node = nodes.get(selected);
        if (!node || node.kind === 'execution') return;
        const ancestors = node.kind === 'step' ? [node.execution.key, node.rule.key] : [node.execution.key];
        setExpanded(prev => ancestors.every(key => prev.has(key)) ? prev : new Set([...prev, ...ancestors]));
    }, [selected, nodes]);

    const toggle = (key) => setExpanded(prev => {
        const next = new Set(prev);
        if (next.has(key)) next.delete(key);
        else next.add(key);
        return next;
    });

    const allExpanded = allKeys.length > 0 && allKeys.every(key => expanded.has(key));
    const toggleAll = () => setExpanded(allExpanded ? new Set() : new Set(allKeys));

    const renderNodes = (list, depth) => list.map(node => (
        <React.Fragment key={node.key}>
            <TimelineRow node={node} depth={depth} timeWindow={timeWindow} expanded={expanded} selected={selected} onToggle={toggle} onSelect={onSelect} />
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
                            onClick={toggleAll}
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

export default TimelineView;
