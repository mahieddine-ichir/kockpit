import React, { useEffect, useRef, useState } from 'react';
import { ZoomIn, ZoomOut, Maximize2, RotateCcw } from 'lucide-react';
import BpmnJS from 'bpmn-js/dist/bpmn-viewer.production.min.js';

const BpmnViewer = ({ xml }) => {
    const containerRef = useRef(null);
    const bpmnViewerRef = useRef(null);
    const [currentZoom, setCurrentZoom] = useState(100);
    const [canZoomIn, setCanZoomIn] = useState(true);
    const [canZoomOut, setCanZoomOut] = useState(true);

    const updateZoomState = (zoomLevel) => {
        const zoomPercentage = Math.round(zoomLevel * 100);
        setCurrentZoom(zoomPercentage);
        setCanZoomIn(zoomLevel < 4); // Max zoom 400%
        setCanZoomOut(zoomLevel > 0.1); // Min zoom 10%
    };

    const handleZoomIn = () => {
        if (bpmnViewerRef.current) {
            const canvas = bpmnViewerRef.current.get('canvas');
            const currentZoom = canvas.zoom();
            const newZoom = Math.min(currentZoom * 1.25, 4);
            canvas.zoom(newZoom);
            updateZoomState(newZoom);
        }
    };

    const handleZoomOut = () => {
        if (bpmnViewerRef.current) {
            const canvas = bpmnViewerRef.current.get('canvas');
            const currentZoom = canvas.zoom();
            const newZoom = Math.max(currentZoom * 0.8, 0.1);
            canvas.zoom(newZoom);
            updateZoomState(newZoom);
        }
    };

    const handleFitToView = () => {
        if (bpmnViewerRef.current) {
            const canvas = bpmnViewerRef.current.get('canvas');
            canvas.zoom('fit-viewport');
            setTimeout(() => {
                const newZoom = canvas.zoom();
                updateZoomState(newZoom);
            }, 100);
        }
    };

    const handleReset = () => {
        if (bpmnViewerRef.current) {
            const canvas = bpmnViewerRef.current.get('canvas');
            canvas.zoom('fit-viewport');
            setTimeout(() => {
                const currentZoom = canvas.zoom();
                const resetZoom = currentZoom * 0.8;
                canvas.zoom(resetZoom);
                canvas.scroll({ dx: 0, dy: 0 });
                updateZoomState(resetZoom);
            }, 100);
        }
    };

    useEffect(() => {
        if (!containerRef.current) return;

        bpmnViewerRef.current = new BpmnJS({
            container: containerRef.current,
            width: '100%',
            height: '500px',
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
                        const initialZoom = currentZoom * 0.8;
                        canvas.zoom(initialZoom);
                        canvas.scroll({ dx: 0, dy: 0 });
                        updateZoomState(initialZoom);
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
            {/* Zoom Controls */}
            <div className="flex items-center justify-between p-4 border-b border-gray-200 bg-gray-50">
                <div className="flex items-center gap-2">
                    <span className="text-sm font-medium text-gray-700">Zoom Controls</span>
                    <span className="text-xs text-gray-500 bg-white px-2 py-1 rounded border">
                        {currentZoom}%
                    </span>
                </div>
                <div className="flex items-center gap-1">
                    <button
                        onClick={handleZoomOut}
                        disabled={!canZoomOut}
                        className="p-2 text-gray-600 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-colors disabled:text-gray-400 disabled:hover:bg-transparent disabled:cursor-not-allowed"
                        title="Zoom Out"
                    >
                        <ZoomOut className="w-4 h-4" />
                    </button>
                    <button
                        onClick={handleZoomIn}
                        disabled={!canZoomIn}
                        className="p-2 text-gray-600 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-colors disabled:text-gray-400 disabled:hover:bg-transparent disabled:cursor-not-allowed"
                        title="Zoom In"
                    >
                        <ZoomIn className="w-4 h-4" />
                    </button>
                    <div className="w-px h-6 bg-gray-300 mx-1" />
                    <button
                        onClick={handleFitToView}
                        className="p-2 text-gray-600 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-colors"
                        title="Fit to View"
                    >
                        <Maximize2 className="w-4 h-4" />
                    </button>
                    <button
                        onClick={handleReset}
                        className="p-2 text-gray-600 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-colors"
                        title="Reset View"
                    >
                        <RotateCcw className="w-4 h-4" />
                    </button>
                </div>
            </div>

            {/* BPMN Diagram Container */}
            <div className="p-6">
                <div
                    ref={containerRef}
                    className="w-full bg-gray-50 rounded-lg border border-gray-100 shadow-inner"
                    style={{
                        minHeight: '500px',
                        height: '500px'
                    }}
                />
            </div>
        </div>
    );
};

export default BpmnViewer;