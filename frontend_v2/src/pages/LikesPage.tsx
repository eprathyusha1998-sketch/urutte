import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { IonIcon } from '@ionic/react';
import { 
  heart,
  closeOutline
} from 'ionicons/icons';
import { authApi, threadsApi } from '../services/api';
import Sidebar from '../components/Sidebar';
import ThreadCard from '../components/ThreadCard';
import SuggestedUsers from '../components/SuggestedUsers';
import FollowRequests from '../components/FollowRequests';
import { Thread, User } from '../types.d';
import { generateInitials, getInitialsBackgroundColor } from '../utils/profileUtils';

const LikesPage: React.FC = () => {
  const navigate = useNavigate();
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [likedThreads, setLikedThreads] = useState<Thread[]>([]);
  const [loading, setLoading] = useState(true);
  const [showRepostModal, setShowRepostModal] = useState(false);
  const [repostingPost, setRepostingPost] = useState<Thread | null>(null);
  const [quoteContent, setQuoteContent] = useState('');

  useEffect(() => {
    const token = localStorage.getItem('access_token');
    if (!token) {
      navigate('/login');
      return;
    }

    const fetchData = async () => {
      try {
        const user = await authApi.getCurrentUser();
        setCurrentUser(user);

        const likedData = await threadsApi.getLikedThreads(10);
        setLikedThreads(likedData);
      } catch (error) {
        console.error('Error fetching data:', error);
        navigate('/login');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [navigate]);

  const handleLikeThread = async (threadId: number) => {
    try {
      await threadsApi.likeThread(threadId);
      // Update the local state to reflect the like/unlike
      setLikedThreads(prev => 
        prev.map(thread => 
          thread.id === threadId 
            ? { ...thread, isLiked: !thread.isLiked, likesCount: thread.isLiked ? thread.likesCount - 1 : thread.likesCount + 1 }
            : thread
        )
      );
    } catch (error) {
      console.error('Error liking thread:', error);
    }
  };

  const handleRepost = (threadId: number, quoteContent?: string) => {
    const thread = likedThreads.find(t => t.id === threadId);
    if (thread) {
      setRepostingPost(thread);
      setShowRepostModal(true);
    }
  };

  const handleRepostSubmit = async () => {
    if (!repostingPost) return;
    
    try {
      await threadsApi.repostThread(repostingPost.id, quoteContent);
      setShowRepostModal(false);
      setRepostingPost(null);
      setQuoteContent('');
    } catch (error) {
      console.error('Error reposting:', error);
    }
  };

  const handleDeleteThread = async (threadId: number) => {
    try {
      await threadsApi.deleteThread(threadId);
      setLikedThreads(prev => prev.filter(thread => thread.id !== threadId));
    } catch (error) {
      console.error('Error deleting thread:', error);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  if (loading) {
    return (
      <div id="wrapper" className="bg-gray-100 dark:bg-slate-900">
        <Sidebar 
          currentUser={currentUser}
          onToggleTheme={() => {
            document.documentElement.classList.toggle('dark');
          }}
          isDarkMode={document.documentElement.classList.contains('dark')}
          onLogout={handleLogout}
        />
        <main id="site__main" className="p-2.5 h-[calc(100vh-var(--m-top))] mt-[--m-top] bg-white">
          <div className="max-w-6xl mx-auto flex gap-12">
            <div className="flex-1 max-w-2xl">
              <div className="space-y-4">
                {[...Array(5)].map((_, index) => (
                  <div key={index} className="bg-white rounded-2xl border border-gray-200 p-6 animate-pulse dark:bg-dark2 dark:border-slate-700">
                    <div className="flex space-x-3">
                      <div className="w-10 h-10 bg-gray-200 dark:bg-slate-600 rounded-full"></div>
                      <div className="flex-1">
                        <div className="h-4 bg-gray-200 dark:bg-slate-600 rounded w-1/4 mb-2"></div>
                        <div className="h-4 bg-gray-200 dark:bg-slate-600 rounded w-3/4 mb-2"></div>
                        <div className="h-4 bg-gray-200 dark:bg-slate-600 rounded w-1/2"></div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
            <div className="w-[360px] flex-shrink-0">
              <div className="sticky top-4 space-y-4">
                <div className="bg-white dark:bg-slate-800 rounded-lg shadow-md p-4 animate-pulse">
                  <div className="h-6 bg-gray-200 dark:bg-slate-600 rounded w-1/2 mb-4"></div>
                  <div className="space-y-3">
                    <div className="flex items-center space-x-3">
                      <div className="w-10 h-10 bg-gray-200 dark:bg-slate-600 rounded-full"></div>
                      <div className="flex-1">
                        <div className="h-4 bg-gray-200 dark:bg-slate-600 rounded w-3/4 mb-2"></div>
                        <div className="h-3 bg-gray-200 dark:bg-slate-600 rounded w-1/2"></div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </main>
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
      />

      {/* Main Content */}
      <main id="site__main" className="p-2.5 h-[calc(100vh-var(--m-top))] mt-[--m-top] bg-white">
        <div className="max-w-6xl mx-auto flex gap-12">
          {/* Liked Threads Content */}
          <div className="flex-1 max-w-2xl">
            {/* Header */}
            <div className="bg-white rounded-2xl border border-gray-200 p-6 mb-6 dark:bg-dark2 dark:border-slate-700">
              <div className="flex items-center gap-3">
                <IonIcon icon={heart} className="text-2xl text-red-500" />
                <h1 className="text-lg font-bold text-gray-900 dark:text-white">Liked Threads</h1>
              </div>
              <p className="text-gray-600 dark:text-gray-400 mt-2">
                Your recently liked threads
              </p>
            </div>
                                  
            {/* Liked Threads Feed */}
            {likedThreads.length === 0 ? (
              <div className="bg-white rounded-2xl border border-gray-200 p-8 text-center dark:bg-dark2 dark:border-slate-700">
                <IonIcon icon={heart} className="text-6xl text-gray-300 dark:text-gray-600 mx-auto mb-4" />
                <p className="text-gray-500 dark:text-white/70">No liked threads yet. Start liking some threads!</p>
              </div>
            ) : (
              likedThreads.map((thread) => (
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
              ))
            )}
          </div>
          
          {/* Right Sidebar */}
          <div className="w-[360px] flex-shrink-0">
            <div className="sticky top-4 space-y-4">
              <FollowRequests currentUser={currentUser} />
              <SuggestedUsers currentUser={currentUser} />
            </div>
          </div>
        </div>
      </main>

      {/* Repost Modal */}
      {showRepostModal && repostingPost && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white dark:bg-slate-800 rounded-2xl p-6 w-full max-w-md mx-4">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Repost Thread</h3>
              <button
                onClick={() => setShowRepostModal(false)}
                className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
              >
                <IonIcon icon={closeOutline} className="text-xl" />
              </button>
            </div>
            
            <div className="mb-4 p-4 bg-gray-50 dark:bg-slate-700 rounded-lg">
              <div className="flex items-center gap-3 mb-2">
                {repostingPost.userPicture ? (
                  <img 
                    src={repostingPost.userPicture} 
                    alt={repostingPost.userName}
                    className="w-8 h-8 rounded-full object-cover"
                  />
                ) : (
                  <div className={`w-8 h-8 rounded-full ${getInitialsBackgroundColor(repostingPost.userName)} flex items-center justify-center`}>
                    <span className="text-white text-sm font-semibold">
                      {generateInitials(repostingPost.userName)}
                    </span>
                  </div>
                )}
                <span className="font-medium text-gray-900 dark:text-white">{repostingPost.userName}</span>
              </div>
              <p className="text-gray-700 dark:text-gray-300">{repostingPost.content}</p>
            </div>
            
            <textarea
              value={quoteContent}
              onChange={(e) => setQuoteContent(e.target.value)}
              placeholder="Add a comment..."
              className="w-full p-3 border border-gray-300 dark:border-slate-600 rounded-lg resize-none dark:bg-slate-700 dark:text-white"
              rows={3}
            />
            
            <div className="flex gap-3 mt-4">
              <button
                onClick={() => setShowRepostModal(false)}
                className="flex-1 px-4 py-2 text-sm border border-gray-300 dark:border-slate-600 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-slate-700 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleRepostSubmit}
                className="flex-1 px-4 py-2 text-sm bg-black text-white rounded-lg hover:bg-gray-800 transition-colors"
              >
                Repost
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default LikesPage;
