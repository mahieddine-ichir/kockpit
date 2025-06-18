# Rule Designer

A modern web application for creating, visualizing, and managing flow diagrams with a synchronized JSON representation following a standardized schema.

## Overview

Rule Designer is an intuitive and interactive web application that allows users to design rule-based diagrams through a visual interface. The application automatically synchronizes changes between the diagram canvas, JSON editor, and other components to provide a seamless user experience. All diagrams are represented using a standardized JSON format that enables consistent rule definitions.

## Key Features

- **Visual Rule Design**: Create and modify flow diagrams using an intuitive drag-and-drop interface
- **Real-time Synchronization**: Changes in any component automatically update across the entire application
- **Multiple Visualization Options**: View your diagrams in both BPMN and Mermaid formats
- **JSON Editor**: Directly edit the underlying JSON structure with syntax highlighting and validation
- **Properties Inspector**: Examine and modify properties of selected diagram elements
- **Problem Detection**: Automatic identification and reporting of errors in diagrams or JSON
- **Rule Management**: Create, store, and manage multiple rules (diagrams) with local browser storage
- **Flexible Interface**: Customizable layout with resizable and repositionable components
- **Standardized Format**: Uses a custom JSON schema for representing rules and flow diagrams

## Components

The application consists of six main components:

1. **Canvas**: The primary workspace for creating and editing diagrams using BPMN.js
2. **JSON Editor**: A code editor with syntax highlighting for directly modifying the JSON representation
3. **Properties Inspector**: A panel for viewing and editing properties of selected diagram elements
4. **Mermaid Diagram**: Alternative visualization of the diagram using Mermaid.js
5. **Problems Tab**: Shows errors, warnings, and other issues detected in the diagram or JSON
6. **Sidebar**: Manages rules (diagrams) with creation, deletion, and selection functionality

## Tools and Capabilities

- **Canvas Tools**: Undo/redo, auto-layout, image export, zoom controls, and reset
- **JSON Editor Tools**: Undo/redo, file upload/download, copy, format, and reset
- **Interface Flexibility**: Resize, reposition, maximize components, or group them under tabs
- **Data Persistence**: Local storage using IndexedDB

## Technologies

This application is built with the following key technologies:

- **React**: Frontend library for building the user interface
- **TypeScript**: Type-safe JavaScript for robust code
- **CodeMirror**: Code editor for JSON with syntax highlighting
- **BPMN.js**: Library for creating and editing BPMN diagrams
- **Mermaid.js**: Alternative diagram visualization
- **TailwindCSS**: Utility-first CSS framework for styling
- **Jotai**: State management for React
- **Dexie.js**: IndexedDB wrapper for client-side storage
- **FlexLayout**: Component for the customizable application layout

## Development

### Prerequisites

- Node.js (v18 or higher recommended)
- pnpm (v8 or higher)

### Getting Started

1. Clone the repository
2. Install dependencies:
   ```
   pnpm install
   ```
3. Start the development server:
   ```
   pnpm dev
   ```
4. Open your browser and navigate to http://localhost:5173

### Building for Production

```
pnpm build
```

## Demo

You can try the latest version of Rule Designer at:
https://rule-designer-dev.hbscience.com/
