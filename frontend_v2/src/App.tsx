import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import FeedPage from './pages/FeedPage';
import MessagesPage from './pages/MessagesPage';
import ThreadPage from './pages/ThreadPage';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/feed" element={<FeedPage />} />
        <Route path="/messages" element={<MessagesPage />} />
        <Route path="/thread/:threadId" element={<ThreadPage />} />
        {/* Redirect all other routes to feed */}
        <Route path="*" element={<Navigate to="/feed" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
