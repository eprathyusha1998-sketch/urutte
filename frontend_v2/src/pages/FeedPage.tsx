import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { IonIcon } from '@ionic/react';
import { 
  closeOutline, 
  add
} from 'ionicons/icons';
import { authApi, threadsApi } from '../services/api';
import Sidebar from '../components/Sidebar';
import ThreadCard from '../components/ThreadCard';
import NewThreadModal from '../components/NewThreadModal';
import SuggestedUsers from '../components/SuggestedUsers';
import FollowRequests from '../components/FollowRequests';
import TopicSuggestionPanel from '../components/TopicSuggestionPanel';
import { Thread, User } from '../types.d';
import { generateInitials, getInitialsBackgroundColor } from '../utils/profileUtils';
import { useInfiniteScroll } from '../hooks';

const FeedPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [threads, setThreads] = useState<Thread[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [showRepostModal, setShowRepostModal] = useState(false);
  const [repostingPost, setRepostingPost] = useState<Thread | null>(null);
  const [quoteContent, setQuoteContent] = useState('');
  const [showNewThreadModal, setShowNewThreadModal] = useState(false);

  useEffect(() => {
    // Check for OAuth token from URL
    const tokenFromUrl = searchParams.get('token');
    if (tokenFromUrl) {
      localStorage.setItem('access_token', tokenFromUrl);
      // Remove token from URL
      const newUrl = new URL(window.location.href);
      newUrl.searchParams.delete('token');
      window.history.replaceState({}, '', newUrl.toString());
    }

    const token = localStorage.getItem('access_token');
    if (!token) {
      navigate('/');
      return;
    }

    const fetchData = async () => {
      try {
        const user = await authApi.getCurrentUser();
        setCurrentUser(user);

        const feedData = await threadsApi.getFeed(0, 30);
        const threadsData = feedData.content || feedData;
        setThreads(threadsData);
        
        // Check if there are more pages
        setHasMore(threadsData.length === 30);

        setLoading(false);
      } catch (error) {
        console.error('Error fetching data:', error);
        setLoading(false);
      }
    };

    fetchData();
  }, [navigate, searchParams]);

  const fetchFeed = async () => {
    try {
      const response = await threadsApi.getFeed(0, 30);
      const threadsData = response.content || response;
      setThreads(threadsData);
      setCurrentPage(0);
      setHasMore(threadsData.length === 30);
    } catch (error) {
      console.error('Error fetching feed:', error);
    }
  };

  const loadMoreThreads = useCallback(async () => {
    if (loadingMore || !hasMore) return;

    try {
      setLoadingMore(true);
      const nextPage = currentPage + 1;
      const response = await threadsApi.getFeed(nextPage, 30);
      const newThreads = response.content || response;
      
      if (newThreads.length > 0) {
        setThreads(prev => [...prev, ...newThreads]);
        setCurrentPage(nextPage);
        setHasMore(newThreads.length === 30);
      } else {
        setHasMore(false);
      }
    } catch (error) {
      console.error('Error loading more threads:', error);
    } finally {
      setLoadingMore(false);
    }
  }, [currentPage, hasMore, loadingMore]);

  // Infinite scroll hook
  const { loadingRef } = useInfiniteScroll({
    hasMore,
    loading: loadingMore,
    onLoadMore: loadMoreThreads,
  });

  const handleLogout = async () => {
    try {
      await authApi.logout();
      localStorage.removeItem('access_token');
      localStorage.removeItem('user');
      navigate('/login');
    } catch (error) {
      console.error('Logout error:', error);
      // Force logout even if API call fails
      localStorage.removeItem('access_token');
      localStorage.removeItem('user');
      navigate('/login');
    }
  };

  const handleLikeThread = async (threadId: number) => {
    try {
      await threadsApi.likeThread(threadId);
      // Refresh the feed to get updated counts
      fetchFeed();
    } catch (error) {
      console.error('Error liking thread:', error);
    }
  };

  const handleRepost = async (threadId: number, quoteContent?: string) => {
    try {
      await threadsApi.repostThread(threadId, quoteContent);
      // Refresh the feed to get updated counts
      fetchFeed();
    } catch (error) {
      console.error('Error reposting thread:', error);
    }
  };

  const handleDeleteThread = async (threadId: number) => {
    try {
      await threadsApi.deleteThread(threadId);
      setThreads(prev => prev.filter(t => t.id !== threadId));
    } catch (error) {
      console.error('Error deleting thread:', error);
      alert('Failed to delete thread. Please try again.');
    }
  };


  const handleQuoteRepost = async () => {
    if (!repostingPost || !quoteContent.trim()) return;

    try {
      const newThread: Thread = await threadsApi.createQuoteRepost(quoteContent, repostingPost.id);
      setThreads(prev => [newThread, ...prev]);
      setShowRepostModal(false);
      setRepostingPost(null);
      setQuoteContent('');
    } catch (error) {
      console.error('Error creating quote repost:', error);
      alert('Failed to create quote repost. Please try again.');
    }
  };

  const handleCreateNewThread = async (content: string, mediaFiles?: File[], replyPermission?: 'ANYONE' | 'FOLLOWERS' | 'FOLLOWING' | 'MENTIONED_ONLY') => {
    try {
      const newThread: Thread = await threadsApi.createThread(content, mediaFiles, undefined, replyPermission);
      setThreads(prev => [newThread, ...prev]);
    } catch (error) {
      console.error('Error creating thread:', error);
      alert('Failed to create thread. Please try again.');
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-gray-600">Loading feed...</p>
        </div>
      </div>
    );
  }

  return (
    <div id="wrapper" className="bg-gray-100 dark:bg-slate-900">
      {/* Sidebar */}
      <Sidebar 
        currentUser={currentUser}
        onToggleTheme={() => {
          document.documentElement.classList.toggle('dark');
        }}
        isDarkMode={document.documentElement.classList.contains('dark')}
        onLogout={handleLogout}
        onCreateThread={() => setShowNewThreadModal(true)}
        isCreateModalOpen={showNewThreadModal}
      />

      {/* Main Content */}
      <main id="site__main" className="p-2.5 h-[calc(100vh-var(--m-top))] mt-[--m-top] bg-white">
        <div className="max-w-6xl mx-auto flex gap-12" id="js-oversized">
          {/* Feed Content */}
          <div className="flex-1 max-w-2xl">
            {/* Header */}
            <div className="bg-white rounded-2xl border border-gray-200 p-6 mb-6 dark:bg-dark2 dark:border-slate-700">
              <div className="flex items-center gap-3">
                <h1 className="text-lg font-bold text-gray-900 dark:text-white">Uruttus for You! 🎯</h1>
              </div>
              <p className="text-gray-600 dark:text-gray-400 mt-2">Latest urutus from people you follow</p>
            </div>

          {/* Center Feed */}
          <div className="space-y-6">
            {/* Create Post Button */}
            <div className="bg-white rounded-2xl border border-gray-200 p-4 dark:bg-dark2 dark:border-slate-700">
              <button
                onClick={() => setShowNewThreadModal(true)}
                className="w-full flex items-center gap-3 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-slate-700 transition-colors"
              >
                <div className="w-10 h-10 rounded-full bg-gray-300 dark:bg-slate-600 flex items-center justify-center overflow-hidden">
                  {currentUser?.picture ? (
                    <img 
                      src={currentUser.picture} 
                      alt={currentUser.name}
                      className="w-full h-full object-cover"
                      onError={(e) => {
                        const target = e.target as HTMLImageElement;
                        target.style.display = 'none';
                        const fallback = target.nextElementSibling as HTMLElement;
                        if (fallback) fallback.style.display = 'flex';
                      }}
                    />
                  ) : null}
                  <div 
                    className={`w-full h-full ${getInitialsBackgroundColor(currentUser?.name || '')} flex items-center justify-center rounded-full`}
                    style={{ display: currentUser?.picture ? 'none' : 'flex' }}
                  >
                    <span className="text-white text-sm font-semibold">
                      {generateInitials(currentUser?.name || '')}
                    </span>
                  </div>
                </div>
                <div className="flex-1 text-left">
                  <span className="text-gray-500 dark:text-gray-400">Lets Urutte!</span>
                </div>
                <IonIcon icon={add} className="text-xl text-gray-400 dark:text-gray-500" />
              </button>
            </div>
                                  
            {/* Urutus Feed */}
            {threads.length === 0 ? (
              <div className="bg-white rounded-2xl border border-gray-200 p-8 text-center dark:bg-dark2 dark:border-slate-700">
                <p className="text-gray-500 dark:text-white/70">No urutus yet. Start the conversation!</p>
              </div>
            ) : (
              <>
                {threads.map((thread) => (
                  <ThreadCard
                    key={thread.id}
                    thread={thread}
                    currentUser={currentUser}
                    onLike={handleLikeThread}
                    onRepost={handleRepost}
                    onDelete={handleDeleteThread}
                    onBookmark={(threadId) => {
                      // Handle bookmark functionality
                      console.log('Bookmark thread:', threadId);
                    }}
                    onReaction={(threadId, reactionType) => {
                      // Handle reaction functionality
                      console.log('Add reaction:', threadId, reactionType);
                    }}
                  />
                ))}
                
                {/* Loading indicator for infinite scroll */}
                {hasMore && (
                  <div ref={loadingRef} className="flex justify-center py-8">
                    {loadingMore ? (
                      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                    ) : (
                      <div className="text-gray-500 dark:text-gray-400 text-sm">
                        Scroll to load more...
                      </div>
                    )}
                  </div>
                )}
                
                {!hasMore && threads.length > 0 && (
                  <div className="text-center py-8">
                    <p className="text-gray-500 dark:text-gray-400 text-sm">
                      You've reached the end of the feed
                    </p>
                  </div>
                )}
              </>
            )}
          </div>
          </div>
          
          {/* Right Sidebar */}
          <div className="w-[360px] flex-shrink-0">
            <div className="sticky top-4 space-y-4">
              <FollowRequests currentUser={currentUser} />
              <SuggestedUsers currentUser={currentUser} />
              <TopicSuggestionPanel currentUser={currentUser} />
            </div>
          </div>
        </div>
      </main>

      {/* Repost Modal */}
      {showRepostModal && repostingPost && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white dark:bg-slate-800 rounded-2xl p-6 max-w-md w-full mx-4">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Repost</h3>
              <button 
                onClick={() => {
                  setShowRepostModal(false);
                  setRepostingPost(null);
                  setQuoteContent('');
                }}
                className="p-2 rounded-full hover:bg-gray-100 dark:hover:bg-slate-700"
              >
                <IonIcon icon={closeOutline} className="text-xl" />
              </button>
            </div>

            {/* Original Post */}
            <div className="mb-4 p-3 bg-gray-50 dark:bg-slate-700 rounded-lg">
              <div className="flex items-center gap-2 mb-2">
                {repostingPost.userPicture ? (
                  <img 
                    src={repostingPost.userPicture} 
                    alt={repostingPost.userName} 
                    className="w-6 h-6 rounded-full object-cover" 
                  />
                ) : (
                  <div className="w-6 h-6 rounded-full bg-gray-100 dark:bg-slate-600 flex items-center justify-center">
                    <div className={`w-5 h-5 rounded-full ${getInitialsBackgroundColor(repostingPost.userName || '')} flex items-center justify-center`}>
                      <span className="text-white text-xs font-semibold">
                        {generateInitials(repostingPost.userName || 'உ')}
                      </span>
                    </div>
                  </div>
                )}
                <span className="font-semibold text-sm text-gray-900 dark:text-white">
                  {repostingPost.userName}
                </span>
              </div>
              <p className="text-sm text-gray-700 dark:text-gray-300">
                {repostingPost.content}
              </p>
            </div>

            {/* Quote Input */}
            <div className="mb-4">
              <textarea 
                className="w-full bg-transparent border-none outline-none text-gray-700 dark:text-gray-300 placeholder-gray-500 dark:placeholder-gray-400 resize-none"
                rows={4}
                placeholder="Add a comment..."
                value={quoteContent}
                onChange={(e) => setQuoteContent(e.target.value)}
                maxLength={2000}
              />
              <div className="flex justify-end mt-2">
                <span className={`text-xs ${
                  quoteContent.length > 1800 ? 'text-red-500' : 
                  quoteContent.length > 1500 ? 'text-yellow-500' : 
                  'text-gray-400'
                }`}>
                  {quoteContent.length}/2000
                </span>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex gap-3">
              <button 
                onClick={() => {
                  handleRepost(repostingPost.id);
                  setShowRepostModal(false);
                  setRepostingPost(null);
                  setQuoteContent('');
                }}
                className="flex-1 bg-gray-200 text-gray-700 dark:bg-gray-600 dark:text-gray-300 py-2 px-4 rounded-full text-sm font-medium hover:bg-gray-300 dark:hover:bg-gray-500 transition-colors"
              >
                Repost
              </button>
              <button 
                onClick={handleQuoteRepost}
                disabled={!quoteContent.trim()}
                className="flex-1 bg-black text-white dark:bg-white dark:text-black py-2 px-4 rounded-full text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-800 dark:hover:bg-gray-200 transition-colors"
              >
                Urutte Urutte!
              </button>
            </div>
          </div>
        </div>
      )}

      {/* New Thread Modal */}
      <NewThreadModal
        isOpen={showNewThreadModal}
        onClose={() => setShowNewThreadModal(false)}
        currentUser={currentUser}
        onSubmit={handleCreateNewThread}
      />
    </div>
  );
};

export default FeedPage;
