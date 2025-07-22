export function jsonToBpmn(flowsData) {
    const mainEvent = JSON.parse(flowsData.events[0]);
    const executionRules = mainEvent.executionEDTDTO.executionRules;
    const ruleMap = mainEvent.executionEDTDTO.rules;

    let xml = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
                  id="Definitions_1"
                  targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="Process_1" isExecutable="false">
    <bpmn:startEvent id="StartEvent_1" />`;

    let sequenceFlows = '';
    let edges = '';
    let shapes = `
      <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
        <dc:Bounds x="150" y="200" width="36" height="36" />
      </bpmndi:BPMNShape>`;

    let prevId = "StartEvent_1";
    let currentX = 250;
    let elementIndex = 1;


    executionRules.forEach((rule, ruleIndex) => {
        const ruleSteps = ruleMap[rule.name] || [];

        ruleSteps.forEach((step, stepIndex) => {
            const stepId = `Step_${elementIndex}`;
            const flowId = `Flow_${elementIndex}`;

            if (step.actionPredicate === "PREDICATE") {
                xml += `
    <bpmn:exclusiveGateway id="${stepId}" name="${step.name}" />`;

                shapes += `
      <bpmndi:BPMNShape id="${stepId}_di" bpmnElement="${stepId}">
        <dc:Bounds x="${currentX}" y="193" width="50" height="50" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="${currentX - 20}" y="250" width="90" height="27" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>`;
            } else {
                xml += `
    <bpmn:task id="${stepId}" name="${step.name}" />`;

                shapes += `
      <bpmndi:BPMNShape id="${stepId}_di" bpmnElement="${stepId}">
        <dc:Bounds x="${currentX}" y="178" width="100" height="80" />
      </bpmndi:BPMNShape>`;
            }

            sequenceFlows += `
    <bpmn:sequenceFlow id="${flowId}" sourceRef="${prevId}" targetRef="${stepId}" />`;

            let sourceX, sourceY, targetX, targetY;

            if (prevId === "StartEvent_1") {
                sourceX = 186;
                sourceY = 218;
            } else {
                const prevElement = document.getElementById(prevId + '_di');
                if (prevElement) {
                    sourceX = currentX - 150 + 100;
                } else {
                    sourceX = currentX - 150 + 100;
                }
                sourceY = 218;
            }

            if (step.actionPredicate === "PREDICATE") {
                targetX = currentX;
                targetY = 218;
            } else {
                targetX = currentX;
                targetY = 218;
            }

            edges += `
      <bpmndi:BPMNEdge id="${flowId}_di" bpmnElement="${flowId}">
        <di:waypoint x="${sourceX}" y="${sourceY}" />
        <di:waypoint x="${targetX}" y="${targetY}" />
      </bpmndi:BPMNEdge>`;

            prevId = stepId;
            currentX += 180;
            elementIndex++;
        });
    });

    const endId = "EndEvent_1";
    xml += `
    <bpmn:endEvent id="${endId}" />`;

    shapes += `
      <bpmndi:BPMNShape id="${endId}_di" bpmnElement="${endId}">
        <dc:Bounds x="${currentX}" y="200" width="36" height="36" />
      </bpmndi:BPMNShape>`;

    const finalFlowId = "Flow_final";
    sequenceFlows += `
    <bpmn:sequenceFlow id="${finalFlowId}" sourceRef="${prevId}" targetRef="${endId}" />`;

    let finalSourceX = currentX - 180 + 100;

    edges += `
      <bpmndi:BPMNEdge id="${finalFlowId}_di" bpmnElement="${finalFlowId}">
        <di:waypoint x="${finalSourceX}" y="218" />
        <di:waypoint x="${currentX}" y="218" />
      </bpmndi:BPMNEdge>`;


    xml += sequenceFlows;
    xml += `
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
${shapes}
${edges}
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`;

    return xml;
}

export function jsonToBpmnImproved(flowsData) {
    const mainEvent = JSON.parse(flowsData.events[0]);
    const executionRules = mainEvent.executionEDTDTO.executionRules;
    const ruleMap = mainEvent.executionEDTDTO.rules;

    let xml = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
                  id="Definitions_1"
                  targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="Process_1" isExecutable="false">
    <bpmn:startEvent id="StartEvent_1" />`;

    let sequenceFlows = '';
    let edges = '';
    let shapes = `
      <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
        <dc:Bounds x="150" y="200" width="36" height="36" />
      </bpmndi:BPMNShape>`;


    const elements = [
        { id: "StartEvent_1", x: 150, y: 200, width: 36, height: 36, type: "startEvent" }
    ];

    let elementIndex = 1;

    executionRules.forEach((rule, ruleIndex) => {
        const ruleSteps = ruleMap[rule.name] || [];

        ruleSteps.forEach((step, stepIndex) => {
            const stepId = `Step_${elementIndex}`;
            const flowId = `Flow_${elementIndex}`;

            const prevElement = elements[elements.length - 1];
            const currentX = prevElement.x + prevElement.width + 80;
            const currentY = 178;

            let width, height, elementType;

            if (step.actionPredicate === "PREDICATE") {
                width = 50;
                height = 50;
                elementType = "gateway";

                xml += `
    <bpmn:exclusiveGateway id="${stepId}" name="${step.name}" />`;

                shapes += `
      <bpmndi:BPMNShape id="${stepId}_di" bpmnElement="${stepId}">
        <dc:Bounds x="${currentX}" y="${currentY + 15}" width="${width}" height="${height}" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="${currentX - 20}" y="${currentY + 72}" width="90" height="27" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>`;
            } else {
                width = 100;
                height = 80;
                elementType = "task";

                xml += `
    <bpmn:task id="${stepId}" name="${step.name}" />`;

                shapes += `
      <bpmndi:BPMNShape id="${stepId}_di" bpmnElement="${stepId}">
        <dc:Bounds x="${currentX}" y="${currentY}" width="${width}" height="${height}" />
      </bpmndi:BPMNShape>`;
            }

            elements.push({
                id: stepId,
                x: currentX,
                y: currentY,
                width: width,
                height: height,
                type: elementType
            });

            sequenceFlows += `
    <bpmn:sequenceFlow id="${flowId}" sourceRef="${prevElement.id}" targetRef="${stepId}" />`;

            const sourceX = prevElement.x + prevElement.width;
            const sourceY = prevElement.y + (prevElement.height / 2);
            const targetX = currentX;
            const targetY = currentY + (height / 2);

            edges += `
      <bpmndi:BPMNEdge id="${flowId}_di" bpmnElement="${flowId}">
        <di:waypoint x="${sourceX}" y="${sourceY}" />
        <di:waypoint x="${targetX}" y="${targetY}" />
      </bpmndi:BPMNEdge>`;

            elementIndex++;
        });
    });

    const endId = "EndEvent_1";
    const lastElement = elements[elements.length - 1];
    const endX = lastElement.x + lastElement.width + 80;
    const endY = 200;

    xml += `
    <bpmn:endEvent id="${endId}" />`;

    shapes += `
      <bpmndi:BPMNShape id="${endId}_di" bpmnElement="${endId}">
        <dc:Bounds x="${endX}" y="${endY}" width="36" height="36" />
      </bpmndi:BPMNShape>`;

    const finalFlowId = "Flow_final";
    sequenceFlows += `
    <bpmn:sequenceFlow id="${finalFlowId}" sourceRef="${lastElement.id}" targetRef="${endId}" />`;

    edges += `
      <bpmndi:BPMNEdge id="${finalFlowId}_di" bpmnElement="${finalFlowId}">
        <di:waypoint x="${lastElement.x + lastElement.width}" y="${lastElement.y + (lastElement.height / 2)}" />
        <di:waypoint x="${endX}" y="${endY + 18}" />
      </bpmndi:BPMNEdge>`;

    xml += sequenceFlows;
    xml += `
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
${shapes}
${edges}
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`;

    return xml;
}