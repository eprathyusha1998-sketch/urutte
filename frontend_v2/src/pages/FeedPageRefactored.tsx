import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { IonIcon } from '@ionic/react';
import { add } from 'ionicons/icons';
import { threadsApi, postsApi } from '../services/api';
import { Thread, User } from '../types.d';
import { useAuth, useThreadActions, useInfiniteScroll } from '../hooks';
import Sidebar from '../components/Sidebar';
import ThreadCard from '../components/ThreadCard';
import NewThreadModal from '../components/NewThreadModal';
import SuggestedUsers from '../components/SuggestedUsers';
import FollowRequests from '../components/FollowRequests';
import TopicSuggestionPanel from '../components/TopicSuggestionPanel';
import { ROUTES, DYNAMIC_ROUTES } from '../constants';
import { generateInitials, getInitialsBackgroundColor } from '../utils/profileUtils';

const FeedPage: React.FC = () => {
  const navigate = useNavigate();
  const { currentUser, loading: authLoading, logout } = useAuth();
  const { handleLike, handleRepost, handleDelete, isActionLoading } = useThreadActions();
  
  const [threads, setThreads] = useState<Thread[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [showNewThreadModal, setShowNewThreadModal] = useState(false);

  useEffect(() => {
    if (!authLoading && !currentUser) {
      navigate(ROUTES.LOGIN);
      return;
    }

    if (currentUser) {
      fetchFeed();
    }
  }, [currentUser, authLoading, navigate]);

  const fetchFeed = async () => {
    try {
      setLoading(true);
      const feedData = await threadsApi.getFeed(0, 30);
      const threadsData = feedData.content || feedData;
      setThreads(threadsData);
      setCurrentPage(0);
      setHasMore(threadsData.length === 30);
    } catch (error) {
      console.error('Error fetching feed:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadMoreThreads = useCallback(async () => {
    if (loadingMore || !hasMore) {
      return;
    }

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
      console.error('❌ Error loading more threads:', error);
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
      await logout();
      navigate(ROUTES.LOGIN);
    } catch (error) {
      console.error('Logout error:', error);
      // Force logout even if API call fails
      navigate(ROUTES.LOGIN);
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

  const handleRepostThread = async (threadId: number, quoteContent?: string) => {
    try {
      if (quoteContent) {
        // Handle quote repost
        await postsApi.quoteRepost(quoteContent, threadId);
        // Refresh feed to show the new quote repost
        fetchFeed();
      } else {
        // Handle simple repost
        await threadsApi.repostThread(threadId);
        // Refresh the feed to get updated counts
        fetchFeed();
      }
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
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-100 dark:bg-slate-900 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
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
                    onRepost={handleRepostThread}
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

      {/* New Thread Modal */}
      {showNewThreadModal && (
        <NewThreadModal
          isOpen={showNewThreadModal}
          onClose={() => setShowNewThreadModal(false)}
          currentUser={currentUser}
          onSubmit={async (content: string, mediaFiles?: File[]) => {
            try {
              await threadsApi.createThread(content, mediaFiles?.[0]);
              setShowNewThreadModal(false);
              fetchFeed(); // Refresh feed
            } catch (error) {
              console.error('Error creating thread:', error);
            }
          }}
        />
      )}
    </div>
  );
};

export default FeedPage;