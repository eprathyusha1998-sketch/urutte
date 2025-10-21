import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { NotificationProvider } from './contexts/NotificationContext';
import { LoadingProvider } from './contexts/LoadingContext';
import GlobalLoader from './components/GlobalLoader';
import HomePage from './pages/HomePage';
import FeedPage from './pages/FeedPage';
import MessagesPage from './pages/MessagesPage';
import ThreadPage from './pages/ThreadPage';
import ProfilePage from './pages/ProfilePage';
import LikesPage from './pages/LikesPage';
import SearchPage from './pages/SearchPage';
import MyPostsPage from './pages/MyPostsPage';
import FollowingPage from './pages/FollowingPage';

function App() {
  return (
    <NotificationProvider>
      <LoadingProvider>
        <Router>
          <GlobalLoader />
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/feed" element={<FeedPage />} />
            <Route path="/messages" element={<MessagesPage />} />
            <Route path="/thread/:threadId" element={<ThreadPage />} />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/likes" element={<LikesPage />} />
            <Route path="/mythread" element={<MyPostsPage />} />
            <Route path="/search" element={<SearchPage />} />
            <Route path="/following/:userId" element={<FollowingPage />} />
            {/* Redirect all other routes to home */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Router>
      </LoadingProvider>
    </NotificationProvider>
  );
}

export default App;
