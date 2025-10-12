import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { IonIcon } from '@ionic/react';
import { arrowBack, closeOutline } from 'ionicons/icons';
import { threadsApi, authApi } from '../services/api';
import Sidebar from '../components/Sidebar';
import ThreadView from '../components/ThreadView';
import { Thread, User } from '../types.d';

const ThreadPage: React.FC = () => {
  const { threadId } = useParams<{ threadId: string }>();
  const navigate = useNavigate();
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [mainPost, setMainPost] = useState<Thread | null>(null);
  const [replies, setReplies] = useState<Thread[]>([]);
  const [loading, setLoading] = useState(true);
  const [showRepostModal, setShowRepostModal] = useState(false);
  const [repostingPost, setRepostingPost] = useState<Thread | null>(null);
  const [quoteContent, setQuoteContent] = useState('');

  const fetchThreadData = async () => {
    try {
      console.log('Fetching thread data for ID:', threadId);

      // Get current user
      const userData = await authApi.getCurrentUser();
      setCurrentUser(userData);

      // Get the main thread by ID
      const mainThreadData = await threadsApi.getThreadById(parseInt(threadId!));
      console.log('Main thread data:', mainThreadData);

      setMainPost(mainThreadData);

      // Get replies using the dedicated API
      console.log('Fetching replies for thread ID:', parseInt(threadId!));
      const threadReplies = await threadsApi.getThreadReplies(parseInt(threadId!));
      console.log('Thread replies response:', threadReplies);
      setReplies(threadReplies);

      setLoading(false);
    } catch (error: any) {
      console.error('Error fetching thread data:', error);
      console.error('Error details:', error.response?.data || error.message);
      setLoading(false);
    }
  };

  useEffect(() => {
    const token = localStorage.getItem('access_token');
    if (!token) {
      navigate('/login');
      return;
    }

    fetchThreadData();
  }, [threadId, navigate]);

  const handleLikePost = async (postId: number) => {
    try {
      await threadsApi.likeThread(postId);
      // Refresh the thread data to get updated counts
      fetchThreadData();
    } catch (error) {
      console.error('Error liking thread:', error);
    }
  };


  const handleReplyToPost = async (parentPostId: number, content: string, mediaFile?: File) => {
    try {
      await threadsApi.createThread(content, mediaFile, parentPostId);
      
      // Refresh the thread data to get updated replies
      fetchThreadData();
    } catch (error) {
      console.error('Error creating reply:', error);
      alert('Failed to create reply. Please try again.');
    }
  };


  const handleDeletePost = async (postId: number) => {
    try {
      await threadsApi.deleteThread(postId);
      if (mainPost && mainPost.id === postId) {
        navigate('/feed');
      } else {
        // Refresh the thread data to get updated replies
        fetchThreadData();
      }
    } catch (error) {
      console.error('Error deleting thread:', error);
      alert('Failed to delete thread. Please try again.');
    }
  };

  const handleRepost = async (postId: number) => {
    try {
      await threadsApi.repostThread(postId);
      // Refresh the thread data to get updated counts
      fetchThreadData();
    } catch (error) {
      console.error('Error reposting:', error);
    }
  };

  const handleQuoteRepost = async () => {
    if (!repostingPost || !quoteContent.trim()) return;

    try {
      await threadsApi.createQuoteRepost(quoteContent, repostingPost.id);
      setShowRepostModal(false);
      setRepostingPost(null);
      setQuoteContent('');
      // Navigate to feed to see the new quote post
      navigate('/feed');
    } catch (error) {
      console.error('Error creating quote repost:', error);
      alert('Failed to create quote repost. Please try again.');
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };


  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-gray-600">Loading thread...</p>
        </div>
      </div>
    );
  }

  if (!mainPost) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <p className="text-gray-600">Thread not found</p>
          <button 
            onClick={() => navigate('/feed')}
            className="mt-4 text-blue-500 hover:text-blue-600"
          >
            Back to Feed
          </button>
        </div>
      </div>
    );
  }


  return (
    <div className="min-h-screen bg-gray-50 dark:bg-slate-900">
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
      <main className="max-w-2xl mx-auto p-4 space-y-4">
        {/* Thread View */}
        <ThreadView
          mainPost={mainPost}
          replies={replies}
          currentUser={currentUser}
          onLike={handleLikePost}
          onRepost={handleRepost}
          onDelete={handleDeletePost}
          onReply={handleReplyToPost}
        />

      </main>

      {/* Repost Modal */}
      {showRepostModal && repostingPost && (
        <div 
          className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
          onClick={(e) => {
            if (e.target === e.currentTarget) {
              setShowRepostModal(false);
              setRepostingPost(null);
              setQuoteContent('');
            }
          }}
        >
          <div className="bg-white dark:bg-dark2 rounded-2xl w-full max-w-md p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-black dark:text-white">Repost</h2>
              <button 
                onClick={() => {
                  setShowRepostModal(false);
                  setRepostingPost(null);
                  setQuoteContent('');
                }}
                className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
              >
                <IonIcon icon={closeOutline} className="text-xl" />
              </button>
            </div>

            {/* Original Post Preview */}
            <div className="bg-gray-50 dark:bg-slate-700 rounded-lg p-3 mb-4 border-l-4 border-blue-500">
              <div className="flex items-center gap-2 mb-2">
                <img 
                  src={repostingPost.userPicture || "/assets/images/avatars/avatar-3.jpg"} 
                  alt={repostingPost.userName} 
                  className="w-6 h-6 rounded-full" 
                />
                <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
                  {repostingPost.userName}
                </span>
              </div>
              <p className="text-sm text-gray-600 dark:text-gray-400 line-clamp-3">
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
                Quote Repost
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ThreadPage;
