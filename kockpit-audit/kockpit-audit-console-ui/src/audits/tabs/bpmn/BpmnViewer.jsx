import React, { useEffect, useRef } from 'react';
import BpmnJS from 'bpmn-js/dist/bpmn-viewer.production.min.js';

const BpmnViewer = ({ xml }) => {
    const containerRef = useRef(null);
    const bpmnViewerRef = useRef(null);

    useEffect(() => {
        if (!containerRef.current) return;

        bpmnViewerRef.current = new BpmnJS({
            container: containerRef.current,
            width: '100%',
            height: '400px',
            branding: false
        });

        if (xml) {
            bpmnViewerRef.current.importXML(xml)
                .then(() => {

                    const container = containerRef.current;
                    const logo = container.querySelector('.bjs-powered-by');
                    if (logo) logo.remove();

                    const canvas = bpmnViewerRef.current.get('canvas');

                    setTimeout(() => {

                        canvas.zoom('fit-viewport');

                        const currentZoom = canvas.zoom();
                        canvas.zoom(currentZoom * 0.8);

                        const viewbox = canvas.viewbox();
                        canvas.scroll({
                            dx: 0,
                            dy: 0
                        });
                    }, 200);
                })
                .catch(err => {
                    console.error('Failed to render BPMN diagram', err);
                });
        }

        return () => {
            if (bpmnViewerRef.current) {
                bpmnViewerRef.current.destroy();
            }
        };
    }, [xml]);

    return (
        <div className="w-full rounded-md border border-gray-200 bg-white">
            <div className="p-6">
                <div
                    ref={containerRef}
                    className="w-full bg-gray-50 rounded-lg border border-gray-100 shadow-inner"
                    style={{
                        minHeight: '400px',
                        height: '400px'
                    }}
                />
            </div>
        </div>
    );
};

export default BpmnViewer;