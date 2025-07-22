import React, { useEffect, useRef } from 'react';
import BpmnJS from 'bpmn-js/dist/bpmn-viewer.production.min.js';

const BpmnViewer = ({ xml }) => {
    const containerRef = useRef(null);
    const bpmnViewerRef = useRef(null);

    useEffect(() => {
        bpmnViewerRef.current = new BpmnJS({
            container: containerRef.current,
            width: '100%',
            height: '500px',
            branding: false
        });

        if (xml) {
            bpmnViewerRef.current.importXML(xml)
                .then(() => {
                    const logo = containerRef.current.querySelector('.bjs-powered-by');
                    if (logo) logo.remove();
                })
                .catch(err => {
                    console.error('Failed to render BPMN diagram', err);
                });
        }

        return () => {
            bpmnViewerRef.current.destroy();
        };
    }, [xml]);

    return <div ref={containerRef} style={{ height: '500px', border: '1px solid #eee' }} />;
};

export default BpmnViewer;