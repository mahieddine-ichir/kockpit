import React from 'react';
import {BrowserRouter as Router, Routes, Route, Navigate} from 'react-router-dom';
import RequestOverview from './pages/RequestOverview';
import RequestDetail from './pages/RequestDetail';
import AuditRequestsPage from './pages/AuditRequestsPage';
import './index.css';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/audits" element={<RequestOverview />} />
        <Route path="/audits/:id" element={<RequestDetail />} />
        <Route path="*"  element={<Navigate to="/audits" replace={true} />} />
      </Routes>
    </Router>
  );
}

export default App;