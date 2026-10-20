import React from 'react';
import { GitBranch, Workflow, Zap, HelpCircle, Database, FileText } from 'lucide-react';
import CopyButton from "../../../components/CopyButton.jsx";
import { dateFormat } from '../auditUtils.js';
import { ConditionChip, Duration, ErrorBlock, JsonSection, StatusBadge, StepTypeChip } from './RulesCommon.jsx';
import { VALID, count, isError, shortName, stepName, withoutKey } from './rulesModel.js';

// Details of what is selected in the current view - shared by every view, so the selection
// and its details stay put when switching views.

const Field = ({ label, children }) => (
    <div className="flex items-start justify-between gap-3 text-sm">
        <dt className="text-gray-500 shrink-0">{label}</dt>
        <dd className="text-gray-800 text-right min-w-0 break-words">{children}</dd>
    </div>
);

const offsetLabel = (node, timeWindow) =>
    timeWindow && node.date != null ? `+${node.date - timeWindow.start} ms` : 'N/A';

const StepDetails = ({ step, index, rule, timeWindow, onSelect }) => {
    const name = stepName(step);
    return (
        <div className="space-y-4">
            <div className="flex items-start gap-1 min-w-0">
                <span className="text-sm font-mono text-gray-800 break-all">{name}</span>
                <CopyButton value={name} />
            </div>
            <div className="flex flex-wrap items-center gap-2">
                <StepTypeChip step={step} />
                <ConditionChip step={step} />
                {isError(step.error) && <span className="text-xs bg-red-100 text-red-800 px-2 py-0.5 rounded">{step.error}</span>}
            </div>
            <dl className="space-y-1.5">
                <Field label="Rule">
                    <button type="button" onClick={() => onSelect(rule.key)} className="text-blue-700 hover:underline text-right" title={rule.name}>
                        {shortName(rule.name)}
                    </button>
                </Field>
                <Field label="Position">#{index + 1} of {rule.steps.length}</Field>
                <Field label="Started">{offsetLabel(step, timeWindow)}</Field>
                <Field label="Duration">{step.time != null ? `${step.time} ms` : 'N/A'}</Field>
            </dl>
            <ErrorBlock message={step.errorMessage} details={step.errorDetails} />
            <JsonSection title="Step" icon={<FileText className="w-3.5 h-3.5" />} value={withoutKey(step)} />
        </div>
    );
};

const RuleSummary = ({ rule, timeWindow }) => {
    const predicates = rule.steps.filter(s => s.actionPredicate === 'PREDICATE').length;
    const failedSteps = rule.steps.filter(s => isError(s.error)).length;
    return (
        <div className="space-y-4">
            <div className="flex items-start gap-1 min-w-0">
                <span className="text-sm font-mono text-gray-800 break-all">{rule.name}</span>
                <CopyButton value={rule.name} />
            </div>
            <dl className="space-y-1.5">
                <Field label="Steps">{rule.steps.length} ({count(predicates, 'predicate')}, {count(rule.steps.length - predicates, 'action')})</Field>
                {failedSteps > 0 && <Field label="Failed steps"><span className="text-red-700 font-semibold">{failedSteps}</span></Field>}
                <Field label="Started">{offsetLabel(rule, timeWindow)}</Field>
                <Field label="Duration">{rule.time != null ? `${rule.time} ms` : 'N/A'}</Field>
                {rule.detail && <Field label="Detail">{rule.detail}</Field>}
            </dl>
            <ErrorBlock message={rule.errorMessage} details={rule.errorDetails} />
            {rule.steps.length > 0 && (
                <JsonSection title={`Execution trace (${rule.steps.length})`} icon={<FileText className="w-3.5 h-3.5" />} value={rule.steps.map(withoutKey)} />
            )}
        </div>
    );
};

const ExecutionDetails = ({ execution }) => (
    <div className="space-y-4">
        <dl className="space-y-1.5">
            <Field label="Execution ID">
                <span className="inline-flex items-center gap-1 font-mono break-all">{execution.uuid || 'N/A'}<CopyButton value={execution.uuid} /></span>
            </Field>
            <Field label="Registry">
                <span className="inline-flex items-center gap-1 font-mono break-all">{execution.registryId || 'N/A'}<CopyButton value={execution.registryId ? String(execution.registryId) : null} /></span>
            </Field>
            <Field label="Started">{execution.date ? dateFormat(execution.date) : 'N/A'}</Field>
            <Field label="Rules">{execution.rules.length}</Field>
        </dl>
        <ErrorBlock message={execution.errorMessage} details={execution.errorDetails} />
        <JsonSection title="Logs" icon={<FileText className="w-3.5 h-3.5" />} value={execution.logs} />
        <JsonSection title="Referential" icon={<Database className="w-3.5 h-3.5" />} value={execution.referential} />
    </div>
);

const DetailsPanel = ({ node, timeWindow, onSelect }) => {
    if (!node) {
        return (
            <div className="bg-white shadow-sm sm:rounded-lg border border-gray-200 p-6 text-sm text-gray-500">
                Select an execution, a rule or a step to see its details.
            </div>
        );
    }

    let icon, title, error, time, body;
    if (node.kind === 'execution') {
        icon = <Workflow className="w-5 h-5 text-blue-600" />;
        title = node.execution.name || 'Unnamed execution';
        ({ error, time } = node.execution);
        body = <ExecutionDetails execution={node.execution} />;
    } else if (node.kind === 'rule') {
        icon = <GitBranch className="w-5 h-5 text-blue-600" />;
        title = shortName(node.rule.name);
        ({ error, time } = node.rule);
        body = <RuleSummary rule={node.rule} timeWindow={timeWindow} />;
    } else {
        const isPredicate = node.step.actionPredicate === 'PREDICATE';
        icon = isPredicate ? <HelpCircle className="w-5 h-5 text-amber-600" /> : <Zap className="w-5 h-5 text-purple-600" />;
        title = shortName(stepName(node.step));
        error = isError(node.step.error) ? node.step.error : VALID;
        time = node.step.time;
        body = <StepDetails step={node.step} index={node.index} rule={node.rule} timeWindow={timeWindow} onSelect={onSelect} />;
    }

    return (
        <div className="bg-white shadow-sm sm:rounded-lg border border-gray-200">
            <div className="px-5 py-4 border-b border-gray-200 bg-gradient-to-r from-blue-50 to-indigo-50 space-y-2">
                <div className="flex items-center gap-2 min-w-0">
                    {icon}
                    <h3 className="text-base font-semibold text-gray-900 truncate" title={title}>{title}</h3>
                </div>
                <div className="flex flex-wrap items-center gap-2">
                    <StatusBadge error={error} />
                    <Duration value={time} />
                </div>
            </div>
            <div className="p-5">{body}</div>
        </div>
    );
};

export default DetailsPanel;
