import React, { useEffect, useRef } from 'react';
import { Play, Flag, HelpCircle, Zap, XCircle } from 'lucide-react';

const VALID = 'VALID';

const isError = (error) => error != null && error !== VALID;

const shortName = (name) => {
    if (!name || /\s/.test(name) || !name.includes('.')) return name;
    return name.substring(name.lastIndexOf('.') + 1);
};

// Vertical arrow between two nodes; a predicate's outcome is written on the arrow leaving it
const Connector = ({ outcome, failed }) => {
    const stroke = failed ? '#f87171' : '#93c5fd';
    return (
        <div className="relative flex justify-center h-12" aria-hidden="true">
            <svg width="16" height="48" viewBox="0 0 16 48" className="overflow-visible">
                <line x1="8" y1="0" x2="8" y2="40" stroke={stroke} strokeWidth="2" strokeDasharray="4 4" className="flow-dash" />
                <path d="M3 38 L8 46 L13 38" fill="none" stroke={stroke} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
            {outcome != null && (
                <span className={`absolute left-1/2 top-1/2 -translate-y-1/2 ml-4 text-[11px] font-mono font-semibold px-1.5 py-0.5 rounded border ${outcome
                    ? 'bg-green-50 text-green-700 border-green-200'
                    : 'bg-orange-50 text-orange-700 border-orange-200'}`}>
                    {String(outcome)}
                </span>
            )}
        </div>
    );
};

const Terminal = ({ label, icon, tone }) => (
    <div className="flex justify-center">
        <div className={`inline-flex items-center gap-2 px-4 py-1.5 rounded-full text-xs font-bold tracking-wider shadow-sm ${tone}`}>
            {icon}
            {label}
        </div>
    </div>
);

const StepNode = ({ step, index, share, highlighted, onSelect }) => {
    const ref = useRef(null);
    const isPredicate = step.actionPredicate === 'PREDICATE';
    const failed = isError(step.error);
    const name = step.name || step.detail || 'Unnamed step';

    useEffect(() => {
        if (highlighted) ref.current?.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
    }, [highlighted]);

    const accent = failed
        ? 'border-red-300 bg-red-50'
        : isPredicate ? 'border-amber-200 bg-white' : 'border-purple-200 bg-white';

    return (
        <button
            ref={ref}
            type="button"
            onClick={() => onSelect?.(index)}
            title={name}
            className={`group relative w-full max-w-sm mx-auto flex items-center gap-3 px-4 pt-3 pb-4 rounded-xl border-2 text-left shadow-sm transition-all hover:shadow-md hover:-translate-y-0.5 focus:outline-none focus:ring-2 focus:ring-blue-400 ${accent} ${highlighted ? 'ring-2 ring-blue-500 ring-offset-2' : ''}`}
        >
            {isPredicate ? (
                // Diamond marks a decision, like a flowchart gateway
                <span className="shrink-0 w-8 h-8 flex items-center justify-center">
                    <span className={`w-6 h-6 rotate-45 rounded-[4px] border-2 flex items-center justify-center ${failed ? 'bg-red-100 border-red-300' : 'bg-amber-100 border-amber-300'}`}>
                        <HelpCircle className={`w-3.5 h-3.5 -rotate-45 ${failed ? 'text-red-700' : 'text-amber-700'}`} />
                    </span>
                </span>
            ) : (
                <span className={`shrink-0 w-8 h-8 rounded-lg flex items-center justify-center ${failed ? 'bg-red-100 text-red-700' : 'bg-purple-100 text-purple-700'}`}>
                    <Zap className="w-4 h-4" />
                </span>
            )}

            <span className="min-w-0 flex-1">
                <span className="block text-[10px] font-semibold uppercase tracking-wider text-gray-400">
                    #{index + 1} · {step.actionPredicate || 'ACTION'}
                </span>
                <span className="block text-sm font-semibold text-gray-900 truncate">{shortName(name)}</span>
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

            {share > 0 && (
                <span className="absolute left-4 right-4 bottom-1.5 h-1 rounded-full bg-gray-100 overflow-hidden" title={`${Math.round(share)}% of the rule time`}>
                    <span className="block h-full rounded-full bg-blue-400" style={{ width: `${share}%` }} />
                </span>
            )}
        </button>
    );
};

const RuleFlowDiagram = ({ rule, highlightedStep, onSelectStep }) => {
    const steps = rule.steps;
    const total = Math.max(rule.time || 0, ...steps.map(s => s.time || 0), 1);
    const failed = isError(rule.error) || steps.some(s => isError(s.error));

    return (
        <div className="py-2">
            <Terminal label="START" icon={<Play className="w-3.5 h-3.5" />} tone="bg-blue-600 text-white" />
            {steps.length > 0 ? steps.map((step, index) => {
                const previous = steps[index - 1];
                const outcome = previous?.actionPredicate === 'PREDICATE' ? previous.condition : null;
                return (
                    <React.Fragment key={step.key ?? index}>
                        <Connector outcome={outcome} failed={isError(previous?.error)} />
                        <StepNode
                            step={step}
                            index={index}
                            share={((step.time || 0) / total) * 100}
                            highlighted={highlightedStep === index}
                            onSelect={onSelectStep}
                        />
                    </React.Fragment>
                );
            }) : null}
            <Connector
                outcome={steps.at(-1)?.actionPredicate === 'PREDICATE' ? steps.at(-1).condition : null}
                failed={isError(steps.at(-1)?.error)}
            />
            <Terminal
                label={failed ? 'END · ERROR' : 'END'}
                icon={failed ? <XCircle className="w-3.5 h-3.5" /> : <Flag className="w-3.5 h-3.5" />}
                tone={failed ? 'bg-red-600 text-white' : 'bg-green-600 text-white'}
            />
        </div>
    );
};

export default RuleFlowDiagram;
