import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { NotificationProvider } from './contexts/NotificationContext';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import FeedPage from './pages/FeedPage';
import MessagesPage from './pages/MessagesPage';
import ThreadPage from './pages/ThreadPage';
import ProfilePage from './pages/ProfilePage';
import LikesPage from './pages/LikesPage';
import SearchPage from './pages/SearchPage';
import MyPostsPage from './pages/MyPostsPage';

function App() {
  return (
    <NotificationProvider>
      <Router>
        <Routes>
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/feed" element={<FeedPage />} />
          <Route path="/messages" element={<MessagesPage />} />
          <Route path="/thread/:threadId" element={<ThreadPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/likes" element={<LikesPage />} />
          <Route path="/mythread" element={<MyPostsPage />} />
          <Route path="/search" element={<SearchPage />} />
          {/* Redirect all other routes to feed */}
          <Route path="*" element={<Navigate to="/feed" replace />} />
        </Routes>
      </Router>
    </NotificationProvider>
  );
}

export default App;
