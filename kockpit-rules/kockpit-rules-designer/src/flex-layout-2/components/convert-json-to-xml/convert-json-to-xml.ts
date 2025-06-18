import {
  JSON3,
  Step,
} from '@/flex-layout-2/util/xml-json-utils/convert-json2-to-json3'
import { El, SequenceFlow, wrap } from './util'
// @ts-expect-error TODO: fix this TS error.
import { layoutProcess } from '@marstamm/bpmn-auto-layout'
import { strc } from '@/flex-layout-2/util/xml-json-utils/xml-str-strings'
import { createSeededIdGenerator } from '@/flex-layout-2/util/xml-json-utils/util'

export async function convertJsonToXml(json: JSON3): Promise<string | null> {
  const gen = createSeededIdGenerator('5678');
  const elements: El[] = [];
  const flows: SequenceFlow[] = [];

  const gatewayTargets = new Set<string>();

  const createElements = (steps: Step[]) => {
    steps.forEach(step => {

      const elType = step.type === 'action' ? 'task' :
          step.type === 'predicate' ? 'exclusiveGateway' :
              'ComplexGateway';

      const el = new El({
        id: step.id || `${elType}_${gen()}`,
        type: elType,
        incomingFlowsIDs: [],
        outgoingFlowsIDs: [],
        name: step.name,
        description: step.description,
      });
      elements.push(el);


      if (step.type === 'ComplexGateway') {
        step.cases.forEach(c => gatewayTargets.add(c.targetId));
        if (step.defaultId) gatewayTargets.add(step.defaultId);
      }


      if (step.type === 'predicate') {
        createElements(step.true);
        createElements(step.false);
      }
    });
  };
  createElements(json.steps);

  const createConnections = (steps: Step[], parentId: string | null = null, branchType: string | null = null) => {
    for (let i = 0; i < steps.length; i++) {
      const current = steps[i];
      const currentEl = elements.find(el => el.id === current.id);
      if (!currentEl) continue;

      // Connect to parent if exists
      if (parentId) {
        const parentEl = elements.find(el => el.id === parentId);
        if (parentEl) {
          const flowName = branchType ||
              (parentEl.type === 'ComplexGateway' ? currentEl.name : undefined);

          const flow = new SequenceFlow({
            sourceRef: parentId,
            targetRef: current.id,
            name: flowName
          });
          flows.push(flow);
          parentEl.outgoingFlowsIDs.push(flow.id);
          currentEl.incomingFlowsIDs.push(flow.id);
        }
      }


      switch (current.type) {
        case 'action':

          if (i < steps.length - 1 && !gatewayTargets.has(steps[i + 1].id)) {
            const next = steps[i + 1];
            const nextEl = elements.find(el => el.id === next.id);
            if (nextEl) {
              const flow = new SequenceFlow({
                sourceRef: current.id,
                targetRef: next.id
              });
              flows.push(flow);
              currentEl.outgoingFlowsIDs.push(flow.id);
              nextEl.incomingFlowsIDs.push(flow.id);
            }
          }
          break;

        case 'predicate':

          if (current.true && current.true.length > 0) {
            const trueFirst = current.true[0];
            const trueEl = elements.find(el => el.id === trueFirst.id);
            if (trueEl) {
              const flow = new SequenceFlow({
                sourceRef: current.id,
                targetRef: trueFirst.id,
                name: 'true'
              });
              flows.push(flow);
              currentEl.outgoingFlowsIDs.push(flow.id);
              trueEl.incomingFlowsIDs.push(flow.id);


              if (current.true.length > 1) {
                for (let j = 0; j < current.true.length - 1; j++) {
                  const source = current.true[j];
                  const target = current.true[j + 1];
                  const sourceEl = elements.find(el => el.id === source.id);
                  const targetEl = elements.find(el => el.id === target.id);
                  if (sourceEl && targetEl) {
                    const innerFlow = new SequenceFlow({
                      sourceRef: source.id,
                      targetRef: target.id
                    });
                    flows.push(innerFlow);
                    sourceEl.outgoingFlowsIDs.push(innerFlow.id);
                    targetEl.incomingFlowsIDs.push(innerFlow.id);
                  }
                }
              }
            }
          }


          if (current.false && current.false.length > 0) {
            const falseFirst = current.false[0];
            const falseEl = elements.find(el => el.id === falseFirst.id);
            if (falseEl) {
              const flow = new SequenceFlow({
                sourceRef: current.id,
                targetRef: falseFirst.id,
                name: 'false'
              });
              flows.push(flow);
              currentEl.outgoingFlowsIDs.push(flow.id);
              falseEl.incomingFlowsIDs.push(flow.id);

              if (current.false.length > 1) {
                for (let j = 0; j < current.false.length - 1; j++) {
                  const source = current.false[j];
                  const target = current.false[j + 1];
                  const sourceEl = elements.find(el => el.id === source.id);
                  const targetEl = elements.find(el => el.id === target.id);
                  if (sourceEl && targetEl) {
                    const innerFlow = new SequenceFlow({
                      sourceRef: source.id,
                      targetRef: target.id
                    });
                    flows.push(innerFlow);
                    sourceEl.outgoingFlowsIDs.push(innerFlow.id);
                    targetEl.incomingFlowsIDs.push(innerFlow.id);
                  }
                }
              }
            }
          }
          break;


        case 'ComplexGateway':

          current.cases.forEach(caseItem => {
            const targetEl = elements.find(el => el.id === caseItem.targetId);
            if (targetEl) {
              const flow = new SequenceFlow({
                sourceRef: current.id,
                targetRef: caseItem.targetId,
                name: caseItem.condition
              });
              flows.push(flow);
              currentEl.outgoingFlowsIDs.push(flow.id);
              targetEl.incomingFlowsIDs.push(flow.id);
            }
          });

          if (current.defaultId) {
            const defaultEl = elements.find(el => el.id === current.defaultId);
            if (defaultEl) {
              const flow = new SequenceFlow({
                sourceRef: current.id,
                targetRef: current.defaultId,
                name: 'default'
              });
              flows.push(flow);
              currentEl.outgoingFlowsIDs.push(flow.id);
              defaultEl.incomingFlowsIDs.push(flow.id);
            }
          }
          break;
      }
    }
  };
  createConnections(json.steps);

  const xmlContent = [...elements, ...flows]
      .map(element => element.toXmlTag())
      .join('\n');

  const wrapped = wrap({
    xml: xmlContent,
    id: json.id ?? 'Rule_' + gen(),
    name: json.name ? strc.json2xml(json.name) : null,
    description: json.description ? strc.json2xml(json.description) : null,
    className: json.className ? strc.json2xml(json.className) : null,
  });

  try {
    return await layoutProcess(wrapped);
  } catch (err) {
    console.error('Layout error:', err);
    return wrapped;
  }
}



const convertJson3StepsToXXX = ({
                                  steps,
                                  previousLevelFlowId,
                                  elArray,
                                  seqFlowArray,
                                }: {
  steps: Step[]
  previousLevelFlowId: string | null
  elArray: El[]
  seqFlowArray: SequenceFlow[]
}) => {
  if (steps.length === 0) {
    return
  }
  // length >= 1
  let currentStep = steps[0]
  let currentEl = new El({
    id: currentStep.id,
    type: currentStep.type === 'action' ? 'task' :
        currentStep.type ==='predicate'? 'exclusiveGateway': 'ComplexGateway',
    incomingFlowsIDs: previousLevelFlowId !== null ? [previousLevelFlowId] : [],
    outgoingFlowsIDs: [],
    name: currentStep.name,
    description: currentStep.description,
  })
  elArray.push(currentEl)
  for (let i = 0; i < steps.length - 1; i += 1) {
    const nextStep = steps[i + 1]
    const nextEl = new El({
      id: nextStep.id,
      type: nextStep.type === 'action' ? 'task' :
          nextStep.type === 'predicate'? 'exclusiveGateway' : 'ComplexGateway',
      incomingFlowsIDs: [],
      outgoingFlowsIDs: [],
      name: nextStep.name,
      description: nextStep.description,
    })
    elArray.push(nextEl)

    const sf = new SequenceFlow({
      sourceRef: currentEl.id,
      targetRef: nextEl.id,
    })
    seqFlowArray.push(sf)

    currentEl.outgoingFlowsIDs.push(sf.id)
    nextEl.incomingFlowsIDs.push(sf.id)
    currentEl = nextEl
    currentStep = nextStep
  }
  if (currentStep.type === 'action') {
    return
  }
  if (currentStep.type !== 'predicate') {
    return
  }
  // currentStep.type === 'predicate'
  ;(['true', 'false'] as const).forEach((value) => {
    if (currentStep[value].length === 0) {
      return
    }

    const sf = new SequenceFlow({
      name: value,
      sourceRef: currentEl.id,
      targetRef: currentStep[value][0].id,
    })
    seqFlowArray.push(sf)
    currentEl.outgoingFlowsIDs.push(sf.id)
    convertJson3StepsToXXX({
      steps: currentStep[value],
      previousLevelFlowId: sf.id,
      elArray,
      seqFlowArray,
    })
  })
}


// ...

//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
// const step = steps[i]
// const el = new El({
//   id: step.id,
//   type: step.type === 'action' ? 'task' : 'exclusiveGateway',
//   incomingFlowsIDs: [],
//   outgoingFlowsIDs: [],
//   name: step.name,
//   description: step.description,
// })
// if (i === 0) {
//   if (previousLevelFlowId !== null) {
//     el.incomingFlowsIDs.push(previousLevelFlowId)
//   }
//   if (steps.length > 1) {
//     el.outgoingFlowsIDs.push(steps[i + 1].id)
//     const sf = new SequenceFlow({
//       name: step.name,
//       description: step.description,
//       sourceRef: el.id,
//       targetRef: el.id,
//     })
//   }
// } else if (i === steps.length - 1) {
//   el.incomingFlowsIDs.push(steps[i - 1].id)
//   if (step.type === 'predicate') {
//     if (step.true.length > 0) {
//       el.outgoingFlowsIDs.push(step.true[0].id)
//       convertJson3StepsToXXX(step.true, step, elArray, seqFlowArray)
//     }
//     if (step.false.length > 0) {
//       el.outgoingFlowsIDs.push(step.false[0].id)
//       convertJson3StepsToXXX(step.false, step, elArray, seqFlowArray)
//     }
//   }
// } else {
//   el.incomingFlowsIDs.push(steps[i - 1].id)
//   el.outgoingFlowsIDs.push(steps[i + 1].id)
// }
// elArray.push(el)
