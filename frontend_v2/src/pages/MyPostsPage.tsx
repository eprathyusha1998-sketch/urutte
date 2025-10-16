import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { IonIcon } from '@ionic/react';
import { 
  create, 
  close,
  documentText,
  eye,
  lockClosed,
  heart
} from 'ionicons/icons';
import { threadsApi, authApi } from '../services/api';
import { Thread, User } from '../types';
import { useNotification } from '../contexts/NotificationContext';
import { generateInitials, getInitialsBackgroundColor } from '../utils/profileUtils';
import Sidebar from '../components/Sidebar';
import ThreadCard from '../components/ThreadCard';
import SuggestedUsers from '../components/SuggestedUsers';
import FollowRequests from '../components/FollowRequests';

const MyPostsPage: React.FC = () => {
  const navigate = useNavigate();
  const { showSuccess, showError } = useNotification();
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [myThreads, setMyThreads] = useState<Thread[]>([]);
  const [loading, setLoading] = useState(true);
  const [showRepostModal, setShowRepostModal] = useState(false);
  const [repostingPost, setRepostingPost] = useState<Thread | null>(null);
  const [quoteContent, setQuoteContent] = useState('');
  const [showEditModal, setShowEditModal] = useState(false);
  const [editingThread, setEditingThread] = useState<Thread | null>(null);
  const [editContent, setEditContent] = useState('');

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

        const response = await threadsApi.getMyThreads(0, 100);
        setMyThreads(response.content);
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
      setMyThreads(prev => 
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
    const thread = myThreads.find(t => t.id === threadId);
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
    if (!window.confirm('Are you sure you want to delete this post?')) {
      return;
    }

    try {
      await threadsApi.deleteThread(threadId);
      setMyThreads(prev => prev.filter(thread => thread.id !== threadId));
      showSuccess('Success', 'Post deleted successfully');
    } catch (error) {
      console.error('Error deleting thread:', error);
      showError('Error', 'Failed to delete post');
    }
  };

  const handleEditThread = (thread: Thread) => {
    setEditingThread(thread);
    setEditContent(thread.content);
    setShowEditModal(true);
  };

  const handleSaveEdit = async () => {
    if (!editingThread || !editContent.trim()) return;

    try {
      const updatedThread = await threadsApi.editThread(editingThread.id, editContent);
      setMyThreads(prev => 
        prev.map(thread => 
          thread.id === editingThread.id ? updatedThread : thread
        )
      );
      setShowEditModal(false);
      setEditingThread(null);
      setEditContent('');
      showSuccess('Success', 'Post updated successfully');
    } catch (error) {
      console.error('Error editing thread:', error);
      showError('Error', 'Failed to edit post');
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
          {/* My Threads Content */}
          <div className="flex-1 max-w-2xl">
            {/* Header */}
            <div className="bg-white rounded-2xl border border-gray-200 p-6 mb-6 dark:bg-dark2 dark:border-slate-700">
              <div className="flex items-center gap-3">
                <IonIcon icon={documentText} className="text-2xl text-blue-500" />
                <h1 className="text-lg font-bold text-gray-900 dark:text-white">My Best Uruttus! 🏆</h1>
              </div>
              <p className="text-gray-600 dark:text-gray-400 mt-2">
                All your posts and threads
              </p>
            </div>

            {/* Statistics */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
              <div className="bg-white rounded-2xl border border-gray-200 p-4 dark:bg-dark2 dark:border-slate-700">
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-blue-100 dark:bg-blue-900 rounded-lg">
                    <IonIcon icon={create} className="text-lg text-blue-600 dark:text-blue-400" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total</p>
                    <p className="text-xl font-bold text-gray-900 dark:text-white">{myThreads.length}</p>
                  </div>
                </div>
              </div>
              
              <div className="bg-white rounded-2xl border border-gray-200 p-4 dark:bg-dark2 dark:border-slate-700">
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-green-100 dark:bg-green-900 rounded-lg">
                    <IonIcon icon={eye} className="text-lg text-green-600 dark:text-green-400" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Public</p>
                    <p className="text-xl font-bold text-gray-900 dark:text-white">
                      {myThreads.filter(t => t.replyPermission === 'ANYONE').length}
                    </p>
                  </div>
                </div>
              </div>
              
              <div className="bg-white rounded-2xl border border-gray-200 p-4 dark:bg-dark2 dark:border-slate-700">
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-purple-100 dark:bg-purple-900 rounded-lg">
                    <IonIcon icon={lockClosed} className="text-lg text-purple-600 dark:text-purple-400" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Private</p>
                    <p className="text-xl font-bold text-gray-900 dark:text-white">
                      {myThreads.filter(t => t.replyPermission !== 'ANYONE').length}
                    </p>
                  </div>
                </div>
              </div>
              
              <div className="bg-white rounded-2xl border border-gray-200 p-4 dark:bg-dark2 dark:border-slate-700">
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-red-100 dark:bg-red-900 rounded-lg">
                    <IonIcon icon={heart} className="text-lg text-red-600 dark:text-red-400" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Likes</p>
                    <p className="text-xl font-bold text-gray-900 dark:text-white">
                      {myThreads.reduce((total, thread) => total + (thread.likesCount || 0), 0)}
                    </p>
                  </div>
                </div>
              </div>
            </div>
                                  
            {/* My Threads Feed */}
            {myThreads.length === 0 ? (
              <div className="bg-white rounded-2xl border border-gray-200 p-8 text-center dark:bg-dark2 dark:border-slate-700">
                <IonIcon icon={create} className="text-6xl text-gray-300 dark:text-gray-600 mx-auto mb-4" />
                <p className="text-gray-500 dark:text-white/70">No posts yet. Start creating some threads!</p>
              </div>
            ) : (
              myThreads.map((thread) => (
                <ThreadCard
                  key={thread.id}
                  thread={thread}
                  currentUser={currentUser}
                  onLike={handleLikeThread}
                  onRepost={handleRepost}
                  onDelete={handleDeleteThread}
                  onEdit={handleEditThread}
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
                <IonIcon icon={close} className="text-xl" />
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

      {/* Edit Modal */}
      {showEditModal && editingThread && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white dark:bg-slate-800 rounded-2xl p-6 w-full max-w-2xl mx-4">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Edit Post</h3>
              <button
                onClick={() => setShowEditModal(false)}
                className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
              >
                <IonIcon icon={close} className="text-xl" />
              </button>
            </div>
            
            <textarea
              value={editContent}
              onChange={(e) => setEditContent(e.target.value)}
              placeholder="What's new?"
              className="w-full p-4 border border-gray-300 dark:border-slate-600 rounded-lg resize-none dark:bg-slate-700 dark:text-white min-h-[120px]"
              rows={4}
              maxLength={2000}
            />
            
            <div className="flex gap-3 mt-4">
              <button
                onClick={() => setShowEditModal(false)}
                className="flex-1 px-4 py-2 text-sm border border-gray-300 dark:border-slate-600 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-slate-700 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleSaveEdit}
                disabled={!editContent.trim()}
                className={`flex-1 px-4 py-2 text-sm bg-black text-white rounded-lg hover:bg-gray-800 transition-colors ${
                  !editContent.trim() ? 'opacity-50 cursor-not-allowed' : ''
                }`}
              >
                Save Changes
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MyPostsPage;
