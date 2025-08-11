export function escapeXml(unsafe) {
    return unsafe.replace(/[<>&'"]/g, function (c) {
        switch (c) {
            case '<': return '&lt;';
            case '>': return '&gt;';
            case '&': return '&amp;';
            case '\'': return '&apos;';
            case '"': return '&quot;';
            default: return c;
        }
    });
}

export function jsonToBpmn(flowsData) {
    try {
        const data = typeof flowsData === 'string' ? JSON.parse(flowsData) : flowsData;

        const events = data.events.map(event => {
            try {
                return typeof event === 'string' ? JSON.parse(event) : event;
            } catch (e) {
                console.error("Failed to parse event:", event);
                return null;
            }
        }).filter(event => event !== null);

        let xml = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  id="Definitions_1"
                  targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="Process_1" isExecutable="false">
    <bpmn:startEvent id="StartEvent_1" />`;

        let sequenceFlows = '';
        let edges = '';
        let shapes = `
    <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
      <dc:Bounds x="100" y="100" width="36" height="36" />
    </bpmndi:BPMNShape>`;

        const state = {
            elementIndex: 1,
            nodeIndex: 1,
            yPosition: 100,
            prevId: "StartEvent_1",
            prevX: 100,
            prevY: 100,
            currentLane: 1,
            stepSpacing: 180,
            laneSpacing: 250
        };


        function getOutgoingPoint(type, x, y, width, height) {
            if (type === 'Gateway') return [x + width, y + height / 2];
            if (type === 'Task') return [x + width, y + height / 2];
            if (type === 'StartEvent') return [x + width, y + height / 2];
            return [x + width, y + height / 2];
        }
        function getIncomingPoint(type, x, y, width, height) {
            if (type === 'Gateway') return [x, y + height / 2];
            if (type === 'Task') return [x, y + height / 2];
            if (type === 'EndEvent') return [x, y + height / 2];
            return [x, y + height / 2];
        }


        const addTask = (step, state) => {
            const taskId = `Task_${state.nodeIndex++}`;
            const taskWidth = 120;
            const taskHeight = 80;
            const nextX = state.prevX + state.stepSpacing;

            const name = escapeXml(step.name || step.detail);
            const taskName = `${name}`;

            xml += `\n    <bpmn:task id="${taskId}" name="${taskName}" />`;

            shapes += `
    <bpmndi:BPMNShape id="${taskId}_di" bpmnElement="${taskId}">
      <dc:Bounds x="${nextX}" y="${state.prevY}" width="${taskWidth}" height="${taskHeight}" />
    </bpmndi:BPMNShape>`;

            sequenceFlows += `
    <bpmn:sequenceFlow id="Flow_${state.elementIndex++}" sourceRef="${state.prevId}" targetRef="${taskId}" />`;


            let prevType = 'StartEvent';
            let prevYForEdge = state.prevY;
            if (state.prevId.startsWith('Task')) prevType = 'Task';
            else if (state.prevId.startsWith('Gateway')) { prevType = 'Gateway'; prevYForEdge += 15; }
            else if (state.prevId.startsWith('StartEvent')) prevType = 'StartEvent';
            const prevWidth = prevType === 'Task' ? 120 : (prevType === 'Gateway' ? 50 : 36);
            const prevHeight = prevType === 'Task' ? 80 : (prevType === 'Gateway' ? 50 : 36);
            const [sourceX, sourceY] = getOutgoingPoint(prevType, state.prevX, prevYForEdge, prevWidth, prevHeight);
            const [targetX, targetY] = getIncomingPoint('Task', nextX, state.prevY, taskWidth, taskHeight);

            edges += `
    <bpmndi:BPMNEdge id="Flow_${state.elementIndex - 1}_di" bpmnElement="Flow_${state.elementIndex - 1}">
      <di:waypoint x="${sourceX}" y="${sourceY}" />
      <di:waypoint x="${targetX}" y="${targetY}" />
    </bpmndi:BPMNEdge>`;

            state.prevId = taskId;
            state.prevX = nextX;
            return taskId;
        };

        const addGateway = (step, state) => {
            const gatewayId = `Gateway_${state.nodeIndex++}`;
            const gatewayWidth = 50;
            const gatewayHeight = 50;
            const nextX = state.prevX + state.stepSpacing;
            const gatewayY = state.prevY + 15;

            const name = escapeXml(step.name || step.detail);
            const condition = step.condition !== undefined ? ` [${step.condition}]` : '';
            const gatewayName = `${name}${condition}`;

            xml += `\n    <bpmn:exclusiveGateway id="${gatewayId}" name="${gatewayName}" />`;

            shapes += `
    <bpmndi:BPMNShape id="${gatewayId}_di" bpmnElement="${gatewayId}">
      <dc:Bounds x="${nextX}" y="${gatewayY}" width="${gatewayWidth}" height="${gatewayHeight}" />
    </bpmndi:BPMNShape>`;

            sequenceFlows += `
    <bpmn:sequenceFlow id="Flow_${state.elementIndex++}" sourceRef="${state.prevId}" targetRef="${gatewayId}" />`;


            let prevType = 'StartEvent';
            let prevYForEdge = state.prevY;
            if (state.prevId.startsWith('Task')) prevType = 'Task';
            else if (state.prevId.startsWith('Gateway')) { prevType = 'Gateway'; prevYForEdge += 15; }
            else if (state.prevId.startsWith('StartEvent')) prevType = 'StartEvent';
            const prevWidth = prevType === 'Task' ? 120 : (prevType === 'Gateway' ? 50 : 36);
            const prevHeight = prevType === 'Task' ? 80 : (prevType === 'Gateway' ? 50 : 36);
            const [sourceX, sourceY] = getOutgoingPoint(prevType, state.prevX, prevYForEdge, prevWidth, prevHeight);
            const [targetX, targetY] = getIncomingPoint('Gateway', nextX, gatewayY, gatewayWidth, gatewayHeight);

            edges += `
    <bpmndi:BPMNEdge id="Flow_${state.elementIndex - 1}_di" bpmnElement="Flow_${state.elementIndex - 1}">
      <di:waypoint x="${sourceX}" y="${sourceY}" />
      <di:waypoint x="${targetX}" y="${targetY}" />
    </bpmndi:BPMNEdge>`;

            state.prevId = gatewayId;
            state.prevX = nextX;
            state.prevY = state.prevY;
            state.lastGatewayY = gatewayY;
            return gatewayId;
        };


        for (const event of events) {
            if (!event.executionEDTDTO) continue;

            const executionRules = event.executionEDTDTO.executionRules || [];
            const ruleMap = event.executionEDTDTO.rules || {};

            for (const rule of executionRules) {
                const steps = ruleMap[rule.name] || [];

                for (const step of steps) {
                    if (step.actionPredicate === "PREDICATE") {
                        addGateway(step, state);
                    } else {
                        addTask(step, state);
                    }
                }
            }


            const endId = `EndEvent_${state.currentLane}`;
            const endX = state.prevX + state.stepSpacing;
            xml += `\n    <bpmn:endEvent id="${endId}" />`;
            shapes += `
    <bpmndi:BPMNShape id="${endId}_di" bpmnElement="${endId}">
      <dc:Bounds x="${endX}" y="${state.prevY}" width="36" height="36" />
    </bpmndi:BPMNShape>`;

            sequenceFlows += `
    <bpmn:sequenceFlow id="Flow_${state.elementIndex}" sourceRef="${state.prevId}" targetRef="${endId}" />`;

            let prevType = 'Task';
            let prevYForEdge = state.prevY;
            if (state.prevId.startsWith('Task')) prevType = 'Task';
            else if (state.prevId.startsWith('Gateway')) { prevType = 'Gateway'; prevYForEdge += 15; }
            else if (state.prevId.startsWith('StartEvent')) prevType = 'StartEvent';
            const prevWidth = prevType === 'Task' ? 120 : (prevType === 'Gateway' ? 50 : 36);
            const prevHeight = prevType === 'Task' ? 80 : (prevType === 'Gateway' ? 50 : 36);
            const [sourceX, sourceY] = getOutgoingPoint(prevType, state.prevX, prevYForEdge, prevWidth, prevHeight);
            const [targetX, targetY] = getIncomingPoint('EndEvent', endX, state.prevY, 36, 36);

            edges += `
    <bpmndi:BPMNEdge id="Flow_${state.elementIndex}_di" bpmnElement="Flow_${state.elementIndex}">
      <di:waypoint x="${sourceX}" y="${sourceY}" />
      <di:waypoint x="${targetX}" y="${targetY}" />
    </bpmndi:BPMNEdge>`;
            state.elementIndex++;

            state.currentLane++;
            state.yPosition += state.laneSpacing;
            state.prevY = state.yPosition;
            state.prevX = 100;

            if (event !== events[events.length - 1]) {
                const nextStartId = `StartEvent_${state.currentLane}`;
                xml += `\n    <bpmn:startEvent id="${nextStartId}" />`;
                shapes += `
        <bpmndi:BPMNShape id="${nextStartId}_di" bpmnElement="${nextStartId}">
          <dc:Bounds x="${state.prevX}" y="${state.prevY}" width="36" height="36" />
        </bpmndi:BPMNShape>`;
                state.prevId = nextStartId;
            }
        }

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
    } catch (error) {
        console.error("Error converting JSON to BPMN:", error);
        throw new Error("Failed to convert flow data to BPMN diagram");
    }
}