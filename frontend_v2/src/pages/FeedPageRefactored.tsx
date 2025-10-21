import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { threadsApi, postsApi } from '../services/api';
import { Thread, User } from '../types.d';
import { useAuth, useThreadActions, useInfiniteScroll } from '../hooks';
import Sidebar from '../components/SidebarRefactored';
import ThreadCard from '../components/ThreadCardRefactored';
import NewThreadModal from '../components/NewThreadModal';
import SuggestedUsers from '../components/SuggestedUsers';
import FollowRequests from '../components/FollowRequests';
import TopicSuggestionPanel from '../components/TopicSuggestionPanel';
import { ROUTES, DYNAMIC_ROUTES } from '../constants';

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
      console.log('Fetching initial feed...');
      const feedData = await threadsApi.getFeed(0, 30);
      const threadsData = feedData.content || feedData;
      console.log('Initial feed loaded:', threadsData.length, 'threads');
      setThreads(threadsData);
      setCurrentPage(0);
      setHasMore(threadsData.length === 30);
      console.log('Initial hasMore set to:', threadsData.length === 30);
    } catch (error) {
      console.error('Error fetching feed:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadMoreThreads = useCallback(async () => {
    console.log('🔄 loadMoreThreads called', { loadingMore, hasMore, currentPage });
    if (loadingMore || !hasMore) {
      console.log('⏭️ Skipping loadMoreThreads - already loading or no more data');
      return;
    }

    try {
      setLoadingMore(true);
      const nextPage = currentPage + 1;
      console.log('📄 Loading page:', nextPage);
      const response = await threadsApi.getFeed(nextPage, 30);
      console.log('📊 API Response:', response);
      const newThreads = response.content || response;
      console.log('📝 Loaded threads:', newThreads.length, 'new threads');
      
      if (newThreads.length > 0) {
        setThreads(prev => {
          const updated = [...prev, ...newThreads];
          console.log('📈 Total threads after update:', updated.length);
          return updated;
        });
        setCurrentPage(nextPage);
        setHasMore(newThreads.length === 30);
        console.log('✅ Updated threads, hasMore:', newThreads.length === 30);
      } else {
        setHasMore(false);
        console.log('🏁 No more threads available');
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

  const handleLikePost = async (threadId: number) => {
    try {
      const thread = threads.find(t => t.id === threadId);
      if (!thread) return;

      const updatedThread = await handleLike(threadId, thread);
      setThreads(prev => 
        prev.map(t => t.id === threadId ? updatedThread : t)
      );
    } catch (error) {
      console.error('Error liking thread:', error);
    }
  };

  const handleRepostPost = async (threadId: number, quoteContent?: string) => {
    try {
      const thread = threads.find(t => t.id === threadId);
      if (!thread) return;

      if (quoteContent) {
        // Handle quote repost
        await postsApi.quoteRepost(quoteContent, threadId);
        // Refresh feed to show the new quote repost
        fetchFeed();
      } else {
        const updatedThread = await handleRepost(threadId, thread);
        setThreads(prev => 
          prev.map(t => t.id === threadId ? updatedThread : t)
        );
      }
    } catch (error) {
      console.error('Error reposting thread:', error);
    }
  };

  const handleDeletePost = async (threadId: number) => {
    try {
      await handleDelete(threadId);
      setThreads(prev => prev.filter(t => t.id !== threadId));
    } catch (error) {
      console.error('Error deleting thread:', error);
    }
  };

  const handleReplyToPost = (threadId: number) => {
    // Navigate to the thread page where user can reply
    navigate(DYNAMIC_ROUTES.THREAD_BY_ID(threadId));
  };

  if (authLoading || loading) {
    return (
      <div className="min-h-screen bg-gray-100 dark:bg-slate-900 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!currentUser) {
    return null;
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
        onLogout={logout}
        onCreateThread={() => setShowNewThreadModal(true)}
        isCreateModalOpen={showNewThreadModal}
      />

      {/* Main Content */}
      <main id="site__main" className="p-2.5 h-[calc(100vh-var(--m-top))] mt-[--m-top] bg-white">
        <div className="max-w-6xl mx-auto flex gap-12">
          {/* Feed Content */}
          <div className="flex-1 max-w-2xl">
            <div className="space-y-4">
              {threads.length === 0 ? (
                <div className="text-center py-12">
                  <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">
                    No threads yet
                  </h3>
                  <p className="text-gray-500 dark:text-gray-400">
                    Start by creating your first thread!
                  </p>
                </div>
              ) : (
                <>
                  {threads.map((thread) => (
                    <ThreadCard
                      key={thread.id}
                      thread={thread}
                      currentUser={currentUser}
                      onLike={handleLikePost}
                      onRepost={handleRepostPost}
                      onDelete={handleDeletePost}
                      onReply={handleReplyToPost}
                    />
                  ))}
                  
                  {/* Loading indicator for infinite scroll */}
                  {hasMore && (
                    <div ref={loadingRef} className="flex justify-center py-8 min-h-[100px] border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg">
                      {loadingMore ? (
                        <div className="flex flex-col items-center gap-2">
                          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                          <div className="text-gray-500 dark:text-gray-400 text-sm">Loading more...</div>
                        </div>
                      ) : (
                        <div className="flex flex-col items-center gap-2">
                          <div className="text-gray-500 dark:text-gray-400 text-sm">
                            Scroll to load more...
                          </div>
                          <button 
                            onClick={loadMoreThreads}
                            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                          >
                            Load More (Manual)
                          </button>
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
                  
                  {/* Debug information */}
                  <div className="text-xs text-gray-400 p-2 bg-gray-100 dark:bg-gray-800 rounded">
                    Debug: Threads: {threads.length}, Page: {currentPage}, HasMore: {hasMore.toString()}, Loading: {loadingMore.toString()}
                  </div>
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
