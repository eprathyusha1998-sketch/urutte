import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import LoginModal from '../components/LoginModal';
import RegisterModal from '../components/RegisterModal';

const HomePage: React.FC = () => {
  const [showLoginModal, setShowLoginModal] = useState(false);
  const [showRegisterModal, setShowRegisterModal] = useState(false);
  const [loading, setLoading] = useState(true);
  const [threads, setThreads] = useState<any[]>([]);
  const [topics, setTopics] = useState<any[]>([]);
  const [users, setUsers] = useState<any[]>([]);
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  useEffect(() => {
    // Check for OAuth token from URL
    const tokenFromUrl = searchParams.get('token');
    if (tokenFromUrl) {
      localStorage.setItem('access_token', tokenFromUrl);
      // Remove token from URL
      const newUrl = new URL(window.location.href);
      newUrl.searchParams.delete('token');
      window.history.replaceState({}, '', newUrl.toString());
      navigate('/feed');
      return;
    }

    // Check for OAuth error
    const errorParam = searchParams.get('error');
    const messageParam = searchParams.get('message');
    if (errorParam === 'true') {
      console.error('OAuth Error:', messageParam);
      // Remove error params from URL
      const newUrl = new URL(window.location.href);
      newUrl.searchParams.delete('error');
      newUrl.searchParams.delete('message');
      window.history.replaceState({}, '', newUrl.toString());
    }

    // Check if user is already logged in
    const token = localStorage.getItem('access_token');
    if (token) {
      navigate('/feed');
      return;
    }

    // Load sample data
    loadSampleData();
  }, [navigate, searchParams]);

  const loadSampleData = () => {
    // Sample topics
    const sampleTopics = [
      { id: 1, name: 'Technology', color: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200' },
      { id: 2, name: 'Design', color: 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200' },
      { id: 3, name: 'Business', color: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' },
      { id: 4, name: 'Lifestyle', color: 'bg-pink-100 text-pink-800 dark:bg-pink-900 dark:text-pink-200' },
      { id: 5, name: 'Travel', color: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200' },
      { id: 6, name: 'Food', color: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200' },
      { id: 7, name: 'Sports', color: 'bg-indigo-100 text-indigo-800 dark:bg-indigo-900 dark:text-indigo-200' },
      { id: 8, name: 'Music', color: 'bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200' },
      { id: 9, name: 'Art', color: 'bg-teal-100 text-teal-800 dark:bg-teal-900 dark:text-teal-200' },
      { id: 10, name: 'Science', color: 'bg-cyan-100 text-cyan-800 dark:bg-cyan-900 dark:text-cyan-200' }
    ];

    // Sample users
    const sampleUsers = [
      { id: 1, name: 'Alex Chen', username: 'alexchen', avatar: null, followers: 1250 },
      { id: 2, name: 'Sarah Johnson', username: 'sarahj', avatar: null, followers: 890 },
      { id: 3, name: 'Mike Rodriguez', username: 'miker', avatar: null, followers: 2100 },
      { id: 4, name: 'Emma Wilson', username: 'emmaw', avatar: null, followers: 1560 },
      { id: 5, name: 'David Kim', username: 'davidk', avatar: null, followers: 980 }
    ];

    // Sample threads
    const sampleThreads = [
      {
        id: 1,
        content: "Just discovered this amazing new feature in Urutte! The community here is incredible. 🚀",
        userName: "Alex Chen",
        userAvatar: null,
        createdAt: new Date(Date.now() - 1 * 60 * 60 * 1000), // 1 hour ago
        likesCount: 24,
        repliesCount: 8,
        repostsCount: 3,
        image: "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=500&h=300&fit=crop"
      },
      {
        id: 2,
        content: "Sharing some thoughts on the future of social media. What do you think about the direction we're heading?",
        userName: "Sarah Johnson",
        userAvatar: null,
        createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000), // 2 hours ago
        likesCount: 45,
        repliesCount: 12,
        repostsCount: 7,
        image: "https://images.unsplash.com/photo-1611224923853-80b023f02d71?w=500&h=300&fit=crop"
      },
      {
        id: 3,
        content: "The design community on Urutte is so supportive! Thanks for all the feedback on my latest project. 💜",
        userName: "Mike Rodriguez",
        userAvatar: null,
        createdAt: new Date(Date.now() - 3 * 60 * 60 * 1000), // 3 hours ago
        likesCount: 67,
        repliesCount: 15,
        repostsCount: 9,
        image: "https://images.unsplash.com/photo-1558655146-d09347e92766?w=500&h=300&fit=crop"
      },
      {
        id: 4,
        content: "Working on some exciting new features for the platform. Can't wait to share them with everyone! The future of social media is here. 🌟",
        userName: "Emma Wilson",
        userAvatar: null,
        createdAt: new Date(Date.now() - 4 * 60 * 60 * 1000), // 4 hours ago
        likesCount: 89,
        repliesCount: 23,
        repostsCount: 12,
        image: "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=500&h=300&fit=crop"
      },
      {
        id: 5,
        content: "Just finished reading an amazing book about productivity and time management. The insights are game-changing! 📚",
        userName: "David Kim",
        userAvatar: null,
        createdAt: new Date(Date.now() - 5 * 60 * 60 * 1000), // 5 hours ago
        likesCount: 156,
        repliesCount: 34,
        repostsCount: 18,
        image: "https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=500&h=300&fit=crop"
      },
      {
        id: 6,
        content: "Beautiful sunset from my window today. Sometimes we need to pause and appreciate the simple moments in life. 🌅",
        userName: "Lisa Park",
        userAvatar: null,
        createdAt: new Date(Date.now() - 6 * 60 * 60 * 1000), // 6 hours ago
        likesCount: 203,
        repliesCount: 45,
        repostsCount: 28,
        image: "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=500&h=300&fit=crop"
      }
    ];

    setTopics(sampleTopics);
    setUsers(sampleUsers);
    setThreads(sampleThreads);
    setLoading(false);
  };

  const handleLoginSuccess = () => {
    setShowLoginModal(false);
    navigate('/feed');
  };

  const handleRegisterSuccess = () => {
    setShowRegisterModal(false);
    navigate('/feed');
  };

  const handleSwitchToRegister = () => {
    setShowLoginModal(false);
    setShowRegisterModal(true);
  };

  const handleSwitchToLogin = () => {
    setShowRegisterModal(false);
    setShowLoginModal(true);
  };

  const formatDate = (date: Date) => {
    const now = new Date();
    const diffInHours = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60));
    
    if (diffInHours < 1) return 'Just now';
    if (diffInHours === 1) return '1 hour ago';
    if (diffInHours < 24) return `${diffInHours} hours ago`;
    
    const diffInDays = Math.floor(diffInHours / 24);
    if (diffInDays === 1) return '1 day ago';
    return `${diffInDays} days ago`;
  };

  const generateInitials = (name: string) => {
    return name.split(' ').map(n => n[0]).join('').toUpperCase();
  };

  const getInitialsBackgroundColor = (name: string) => {
    return 'bg-black text-white';
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-100 dark:bg-slate-900 flex items-center justify-center">
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-black dark:border-white border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-black dark:text-white text-lg font-semibold animate-pulse">Loading Urutte...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 dark:bg-slate-900">
      {/* Header */}
      <header className="bg-white dark:bg-slate-800 shadow-sm border-b border-gray-200 dark:border-slate-700">
        <div className="max-w-4xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <Link to="/" className="flex items-center">
              <div className="w-8 h-8 sm:w-10 sm:h-10 bg-black dark:bg-white rounded-lg flex items-center justify-center mr-2 sm:mr-3">
                <span className="text-white dark:text-black font-bold text-sm sm:text-lg">உ</span>
              </div>
              <div className="text-xl sm:text-2xl font-bold text-black dark:text-white">Urutte</div>
            </Link>
            <div className="flex items-center space-x-2 sm:space-x-4">
              <button
                onClick={() => setShowLoginModal(true)}
                className="text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white px-2 sm:px-4 py-2 rounded-lg hover:bg-gray-100 dark:hover:bg-slate-700 transition-colors text-sm sm:text-base"
              >
                Sign In
              </button>
              <button
                onClick={() => setShowRegisterModal(true)}
                className="bg-black dark:bg-white text-white dark:text-black px-3 sm:px-6 py-2 rounded-lg hover:bg-gray-800 dark:hover:bg-gray-200 transition-colors text-sm sm:text-base"
              >
                Join Urutte
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-4xl mx-auto px-4 py-6 sm:py-8">
        <div className="text-center mb-6 sm:mb-8">
              <h1 className="text-2xl sm:text-3xl font-bold text-gray-900 dark:text-white mb-3 sm:mb-4">
                Latest Uruttus
              </h1>
          <p className="text-gray-600 dark:text-gray-300 mb-4 sm:mb-6 text-sm sm:text-base px-4">
            Discover what's happening on Urutte. Join the conversation by signing up!
          </p>
          <button 
            onClick={() => setShowRegisterModal(true)}
            className="bg-black dark:bg-white text-white dark:text-black px-4 sm:px-6 py-2 sm:py-3 rounded-lg font-semibold hover:bg-gray-800 dark:hover:bg-gray-200 transition-colors text-sm sm:text-base"
          >
            Lets Urutte!
          </button>
        </div>

        {/* Trending Topics */}
        <div className="bg-white dark:bg-slate-800 rounded-2xl border border-gray-200 dark:border-slate-700 p-4 sm:p-6 mb-6 sm:mb-8">
          <h2 className="text-lg sm:text-xl font-bold text-gray-900 dark:text-white mb-3 sm:mb-4">Trending Topics</h2>
          <div className="flex flex-wrap gap-2">
            {topics.map((topic) => (
              <span
                key={topic.id}
                className={`px-3 py-1 rounded-full text-sm font-medium ${topic.color}`}
              >
                #{topic.name}
              </span>
            ))}
          </div>
        </div>

        {/* Featured Users */}
        <div className="bg-white dark:bg-slate-800 rounded-2xl border border-gray-200 dark:border-slate-700 p-4 sm:p-6 mb-6 sm:mb-8">
          <h2 className="text-lg sm:text-xl font-bold text-gray-900 dark:text-white mb-3 sm:mb-4">Featured Users</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 sm:gap-4">
            {users.map((user) => (
              <div key={user.id} className="flex items-center gap-3 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-slate-700 transition-colors">
                <div className="w-10 h-10 rounded-full bg-gray-300 dark:bg-slate-600 flex items-center justify-center overflow-hidden flex-shrink-0">
                  {user.avatar ? (
                    <img 
                      src={user.avatar} 
                      alt={user.name}
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <div className={`w-full h-full ${getInitialsBackgroundColor(user.name)} flex items-center justify-center rounded-full`}>
                      <span className="text-white text-sm font-semibold">
                        {generateInitials(user.name)}
                      </span>
                    </div>
                  )}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="font-semibold text-gray-900 dark:text-white text-sm sm:text-base truncate">
                    {user.name}
                  </p>
                  <p className="text-gray-500 dark:text-gray-400 text-xs sm:text-sm">
                    @{user.username} • {user.followers.toLocaleString()} followers
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>

                {/* Urutus Feed */}
                {threads.length === 0 ? (
                  <div className="bg-white dark:bg-slate-800 rounded-2xl border border-gray-200 dark:border-slate-700 p-6 sm:p-8 text-center">
                    <p className="text-gray-500 dark:text-gray-400 text-sm sm:text-base">No urutus available at the moment.</p>
                  </div>
                ) : (
                  <div className="space-y-3 sm:space-y-4">
                    {threads.map((thread) => (
              <div key={thread.id} className="bg-white dark:bg-slate-800 rounded-2xl border border-gray-200 dark:border-slate-700 p-4 sm:p-6">
                {/* User Info */}
                <div className="flex items-center gap-3 mb-3 sm:mb-4">
                  <div className="w-8 h-8 sm:w-10 sm:h-10 rounded-full bg-gray-300 dark:bg-slate-600 flex items-center justify-center overflow-hidden flex-shrink-0">
                    {thread.userAvatar ? (
                      <img 
                        src={thread.userAvatar} 
                        alt={thread.userName}
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <div className={`w-full h-full ${getInitialsBackgroundColor(thread.userName)} flex items-center justify-center rounded-full`}>
                        <span className="text-white text-xs sm:text-sm font-semibold">
                          {generateInitials(thread.userName)}
                        </span>
                      </div>
                    )}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-1 sm:gap-2">
                      <span className="font-semibold text-gray-900 dark:text-white text-sm sm:text-base truncate">
                        {thread.userName}
                      </span>
                      <span className="text-gray-500 dark:text-gray-400 text-xs sm:text-sm flex-shrink-0">
                        {formatDate(thread.createdAt)}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Urutu Content */}
                <div className="mb-3 sm:mb-4">
                  <p className="text-gray-900 dark:text-white leading-relaxed whitespace-pre-wrap text-sm sm:text-base">
                    {thread.content}
                  </p>
                  
                  {/* Urutu Image */}
                  {thread.image && (
                    <div className="mt-3">
                      <img 
                        src={thread.image} 
                        alt="Urutu content"
                        className="w-full h-48 sm:h-64 object-cover rounded-xl"
                        onError={(e) => {
                          const target = e.target as HTMLImageElement;
                          target.style.display = 'none';
                        }}
                      />
                    </div>
                  )}
                </div>

                {/* Urutu Stats */}
                <div className="flex items-center gap-4 sm:gap-6 text-gray-500 dark:text-gray-400 text-xs sm:text-sm">
                  <div className="flex items-center gap-1">
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                    </svg>
                    <span>{thread.repliesCount}</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                    </svg>
                    <span>{thread.repostsCount}</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                    </svg>
                    <span>{thread.likesCount}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* CTA Section */}
        <div className="mt-8 sm:mt-12 text-center">
          <div className="bg-white dark:bg-slate-800 rounded-2xl border border-gray-200 dark:border-slate-700 p-6 sm:p-8">
            <h2 className="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white mb-3 sm:mb-4">
              Ready to Join the Urutte Community?
            </h2>
                    <p className="text-gray-600 dark:text-gray-300 mb-4 sm:mb-6 text-sm sm:text-base px-4">
                      Sign up to create your own urutus, like posts, and connect with the community.
                    </p>
            <div className="flex flex-col sm:flex-row gap-3 sm:gap-4 justify-center">
              <button 
                onClick={() => setShowRegisterModal(true)}
                className="bg-black dark:bg-white text-white dark:text-black px-6 sm:px-8 py-2 sm:py-3 rounded-lg font-semibold hover:bg-gray-800 dark:hover:bg-gray-200 transition-colors text-sm sm:text-base"
              >
                Create Account
              </button>
              <button 
                onClick={() => setShowLoginModal(true)}
                className="border-2 border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 px-6 sm:px-8 py-2 sm:py-3 rounded-lg font-semibold hover:bg-gray-50 dark:hover:bg-slate-700 transition-colors text-sm sm:text-base"
              >
                Sign In
              </button>
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-white dark:bg-slate-800 border-t border-gray-200 dark:border-slate-700 py-6 sm:py-8 mt-8 sm:mt-12">
        <div className="max-w-4xl mx-auto px-4">
          <div className="flex flex-col lg:flex-row justify-between items-center space-y-3 lg:space-y-0">
            <div className="flex items-center">
              <div className="w-6 h-6 sm:w-8 sm:h-8 bg-black dark:bg-white rounded-lg flex items-center justify-center mr-2">
                <span className="text-white dark:text-black font-bold text-xs sm:text-sm">உ</span>
              </div>
              <div className="text-base sm:text-lg font-bold text-black dark:text-white">Urutte</div>
            </div>
            <div className="flex flex-wrap items-center justify-center gap-4 lg:gap-6 text-xs sm:text-sm text-gray-500 dark:text-gray-400">
              <a href="/privacy.html" className="hover:text-gray-700 dark:hover:text-gray-300 transition-colors whitespace-nowrap">Privacy Policy</a>
              <a href="/terms.html" className="hover:text-gray-700 dark:hover:text-gray-300 transition-colors whitespace-nowrap">Terms of Service</a>
              <span className="whitespace-nowrap">© 2025 Urutte. All rights reserved.</span>
            </div>
          </div>
        </div>
      </footer>

      {/* Modals */}
      <LoginModal
        isOpen={showLoginModal}
        onClose={() => setShowLoginModal(false)}
        onSuccess={handleLoginSuccess}
        onSwitchToRegister={handleSwitchToRegister}
      />

      <RegisterModal
        isOpen={showRegisterModal}
        onClose={() => setShowRegisterModal(false)}
        onSuccess={handleRegisterSuccess}
        onSwitchToLogin={handleSwitchToLogin}
      />
    </div>
  );
};

export default HomePage;
