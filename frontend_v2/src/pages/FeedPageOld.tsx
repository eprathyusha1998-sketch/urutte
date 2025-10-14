import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { IonIcon } from '@ionic/react';
import { heart, chatbubbleEllipses, paperPlaneOutline, shareOutline, chevronDownOutline, image, videocam, happy, location, ellipsisHorizontal, syncOutline, menuOutline, closeOutline, search, addCircleOutline, notificationsOutline, chatboxEllipsesOutline, checkmarkCircle, trash, calendar, repeat } from 'ionicons/icons';
import { authApi, postsApi, notificationsApi, messagesApi, usersApi } from '../services/api';
import { webSocketService } from '../services/websocket';
import Sidebar from '../components/Sidebar';
import PostCard from '../components/PostCard';

interface Post {
  id: number;
  content: string;
  mediaUrl?: string;
  mediaType?: string;
  userId: string;
  userName: string;
  userEmail: string;
  userPicture?: string;
  likes: number;
  retweets: number;
  replies: number;
  reposts: number; // Add reposts field
  commentsCount?: number; // Add comment count field
  isLiked: boolean;
  isReposted?: boolean; // Add reposted status
  timestamp: string;
  parentPostId?: number;
}

interface Comment {
  id: number;
  content: string;
  userId: string;
  userName: string;
  userPicture?: string;
  likesCount: number;
  repliesCount: number;
  isLiked: boolean;
  createdAt: string;
  parentCommentId?: number;
  replies?: Comment[];
}

interface User {
  id: string;
  name: string;
  email: string;
  picture?: string;
  isFollowing?: boolean;
  followersCount?: number;
  followingCount?: number;
}

const FeedPage: React.FC = () => {
  const navigate = useNavigate();
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);
  const [notificationCount, setNotificationCount] = useState(0);
  const [messageCount, setMessageCount] = useState(0);
  const [newPostContent, setNewPostContent] = useState('');
  const [postingContent, setPostingContent] = useState(false);
  const [selectedMedia, setSelectedMedia] = useState<File | null>(null);
  const [mediaPreview, setMediaPreview] = useState<string | null>(null);
  const [replyingTo, setReplyingTo] = useState<Post | null>(null);
  const [showComments, setShowComments] = useState<{ [postId: number]: boolean }>({});
  const [comments, setComments] = useState<{ [postId: number]: Comment[] }>({});
  const [newComment, setNewComment] = useState<{ [postId: number]: string }>({});
  const [newReply, setNewReply] = useState<{ [commentId: number]: string }>({});
  const [showReplies, setShowReplies] = useState<{ [commentId: number]: boolean }>({});
  const [peopleSuggestions, setPeopleSuggestions] = useState<User[]>([]);
  const [followRequests, setFollowRequests] = useState<any[]>([]);
  const [showRepostModal, setShowRepostModal] = useState(false);
  const [repostingPost, setRepostingPost] = useState<Post | null>(null);
  const [quoteContent, setQuoteContent] = useState('');
  useEffect(() => {
    // Check authentication
    const token = localStorage.getItem('access_token');
    if (!token) {
      navigate('/login');
      return;
    }

    // Fetch current user and initial data
    const fetchInitialData = async () => {
      try {
        const user = await authApi.getCurrentUser();
        setCurrentUser(user);

        // Fetch feed
        const feedData = await postsApi.getFeed(0, 20);
        setPosts(feedData.content || feedData);

        // Fetch notification count
        const notifCount = await notificationsApi.getUnreadCount();
        setNotificationCount(notifCount);

        // Fetch message count
        const msgCount = await messagesApi.getUnreadCount();
        setMessageCount(msgCount);

        // Fetch people suggestions
        const suggestions = await usersApi.getPeopleYouMayKnow(5);
        setPeopleSuggestions(suggestions);

        // Fetch follow requests
        const requests = await usersApi.getFollowRequests();
        setFollowRequests(requests);

        // Connect WebSocket
        webSocketService.connect(
          user.id,
          () => {
            console.log('WebSocket connected');
            // Subscribe to notifications
            webSocketService.subscribeToNotifications(user.id, (notification) => {
              setNotificationCount(prev => prev + 1);
            });
            // Subscribe to messages
            webSocketService.subscribeToMessages(user.id, (message) => {
              setMessageCount(prev => prev + 1);
            });
          },
          (error) => {
            console.error('WebSocket connection error:', error);
          }
        );

        setLoading(false);
      } catch (error) {
        console.error('Error fetching initial data:', error);
        localStorage.removeItem('access_token');
        navigate('/login');
      }
    };

    fetchInitialData();

    // No UIkit script needed - using React event handlers
    
    // WebSocket connection for real-time features (only if user is authenticated)
    if (currentUser?.id) {
      // Add a small delay to ensure backend is ready
      const connectWebSocket = () => {
        webSocketService.connect(
          currentUser.id,
          () => {
            console.log('WebSocket connected');
            // Subscribe to notifications
            webSocketService.subscribeToNotifications(currentUser.id, (notification) => {
              console.log('New notification:', notification);
              setNotificationCount(prev => prev + 1);
            });
            
            // Subscribe to messages
            webSocketService.subscribeToMessages(currentUser.id, (message) => {
              console.log('New message:', message);
              setMessageCount(prev => prev + 1);
            });
          },
          (error) => {
            console.warn('WebSocket connection failed (this is expected if WebSocket server is not fully implemented):', error);
            // Don't show error to user, just log it
          }
        );
      };

      // Try to connect after a short delay
      const timeoutId = setTimeout(connectWebSocket, 2000);
      
      return () => {
        clearTimeout(timeoutId);
        webSocketService.disconnect();
      };
    }

    // Add click outside handler to close dropdown menus
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as HTMLElement;
      // Don't close if clicking on menu buttons or menus themselves
      if (!target.closest('[id^="post-menu-"]') && 
          !target.closest('[id^="comment-menu-"]')) {
        // Close all dropdown menus
        document.querySelectorAll('[id^="post-menu-"], [id^="comment-menu-"]').forEach(menu => {
          (menu as HTMLElement).style.display = 'none';
        });
      }
    };

    document.addEventListener('click', handleClickOutside);
    
    return () => {
      webSocketService.disconnect();
      document.removeEventListener('click', handleClickOutside);
    };
  }, [navigate]);

  const handleCreatePost = async () => {
    console.log('handleCreatePost called');
    console.log('newPostContent:', newPostContent);
    console.log('postingContent:', postingContent);
    console.log('selectedMedia:', selectedMedia);
    
    if (!newPostContent.trim() || postingContent) {
      console.log('Early return - no content or already posting');
      return;
    }

    console.log('Starting to create post...');
    setPostingContent(true);
    try {
      let newPost: Post;
      if (selectedMedia) {
        // Create post with media
        console.log('Creating post with media:', selectedMedia.name, selectedMedia.size);
        const response = await postsApi.createPostWithMedia(newPostContent, selectedMedia);
        console.log('Media post response:', response);
        if (response.success) {
          newPost = response.post;
        } else {
          throw new Error(response.error || 'Failed to create post with media');
        }
      } else {
        // Create text-only post
        console.log('Creating text-only post');
        newPost = await postsApi.createPost(newPostContent, undefined, undefined, replyingTo?.id);
        console.log('Text post response:', newPost);
      }
      
      if (replyingTo) {
        // If replying, navigate to the thread view
        navigate(`/thread/${replyingTo.id}`);
      } else {
        // If creating a new post, add to feed
        setPosts(prev => [newPost, ...prev]);
      }
      setNewPostContent('');
      setSelectedMedia(null);
      setMediaPreview(null);
      setReplyingTo(null);
      
      // Close modal
      const modal = document.querySelector('#create-status');
      if (modal) {
        (modal as HTMLElement).style.display = 'none';
      }
    } catch (error: any) {
      console.error('Error creating post:', error);
      console.error('Error details:', error.response?.data);
      console.error('Error status:', error.response?.status);
      alert('Failed to create post. Please try again. Error: ' + (error.response?.data?.error || error.message));
    } finally {
      setPostingContent(false);
    }
  };

  const handleLikePost = async (postId: number) => {
    try {
      const updatedPost = await postsApi.likePost(postId);
      setPosts(prev => prev.map(p => p.id === postId ? updatedPost : p));
    } catch (error) {
      console.error('Error liking post:', error);
    }
  };

  const handleMediaSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      // Check file size (3MB max)
      if (file.size > 3 * 1024 * 1024) {
        alert('File size must be less than 3MB');
        return;
      }
      
      // Check file type
      if (!file.type.startsWith('image/') && !file.type.startsWith('video/')) {
        alert('Only images and videos are allowed');
        return;
      }
      
      setSelectedMedia(file);
      
      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        setMediaPreview(e.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleRemoveMedia = () => {
    setSelectedMedia(null);
    setMediaPreview(null);
  };

  const handleToggleComments = async (postId: number) => {
    const isShowing = showComments[postId];
    setShowComments(prev => ({ ...prev, [postId]: !isShowing }));
    
    if (!isShowing && !comments[postId]) {
      try {
        const commentsData = await postsApi.getComments(postId);
        setComments(prev => ({ ...prev, [postId]: commentsData.content || commentsData }));
      } catch (error) {
        console.error('Error fetching comments:', error);
      }
    }
  };

  const handleAddComment = async (postId: number) => {
    const content = newComment[postId];
    if (!content?.trim()) return;

    try {
      const newCommentData = await postsApi.addComment(postId, content);
      setComments(prev => ({
        ...prev,
        [postId]: [newCommentData, ...(prev[postId] || [])]
      }));
      setNewComment(prev => ({ ...prev, [postId]: '' }));
      
      // Update post comments count
      setPosts(prev => prev.map(p => 
        p.id === postId 
          ? { ...p, replies: p.replies + 1, commentsCount: (p.commentsCount || 0) + 1 }
          : p
      ));
    } catch (error) {
      console.error('Error adding comment:', error);
      alert('Failed to add comment. Please try again.');
    }
  };

  const handleDeletePost = async (postId: number) => {
    try {
      await postsApi.deletePost(postId);
      setPosts(prev => prev.filter(p => p.id !== postId));
    } catch (error) {
      console.error('Error deleting post:', error);
      alert('Failed to delete post. Please try again.');
    }
  };

  const handleDeleteComment = async (commentId: number, postId: number) => {
    try {
      await postsApi.deleteComment(commentId);
      
      // Refresh comments to get updated structure and counts
      const updatedComments = await postsApi.getComments(postId);
      setComments(prev => ({
        ...prev,
        [postId]: updatedComments.content || updatedComments
      }));
      
      // Update post comment count
      setPosts(prev => prev.map(p => 
        p.id === postId 
          ? { ...p, commentsCount: Math.max(0, (p.commentsCount || 0) - 1) }
          : p
      ));
      
      // Clean up showReplies state for deleted comment
      setShowReplies(prev => {
        const newState = { ...prev };
        delete newState[commentId];
        return newState;
      });
    } catch (error) {
      console.error('Error deleting comment:', error);
      alert('Failed to delete comment. Please try again.');
    }
  };

  const handleLikeComment = async (commentId: number, postId: number) => {
    try {
      const updatedComment = await postsApi.likeComment(commentId);
      setComments(prev => ({
        ...prev,
        [postId]: (prev[postId] || []).map(c => {
          if (c.id === commentId) {
            return updatedComment;
          }
          // Also update replies if the liked comment is a reply
          if (c.replies && c.replies.length > 0) {
            return {
              ...c,
              replies: c.replies.map(reply =>
                reply.id === commentId ? updatedComment : reply
              )
            };
          }
          return c;
        })
      }));
      
      // Preserve showReplies state - don't let it get reset during like operations
      // This ensures that if replies were visible before liking, they stay visible
    } catch (error) {
      console.error('Error liking comment:', error);
    }
  };

  const handleAddReply = async (commentId: number, postId: number) => {
    const content = newReply[commentId];
    if (!content?.trim()) return;

    try {
      await postsApi.addReply(commentId, content);
      
      // Refresh comments to get updated structure and counts
      const updatedComments = await postsApi.getComments(postId);
      setComments(prev => ({
        ...prev,
        [postId]: updatedComments.content || updatedComments
      }));
      
      // Ensure replies section stays open after adding reply
      setShowReplies(prev => ({ ...prev, [commentId]: true }));
      
      setNewReply(prev => ({ ...prev, [commentId]: '' }));
    } catch (error) {
      console.error('Error adding reply:', error);
      alert('Failed to add reply. Please try again.');
    }
  };

  const handleToggleReplies = async (commentId: number, postId: number) => {
    const isCurrentlyVisible = showReplies[commentId];
    
    if (!isCurrentlyVisible) {
      // If we're opening replies, make sure we have the latest replies data
      try {
        const replies = await postsApi.getReplies(commentId);
        setComments(prev => ({
          ...prev,
          [postId]: (prev[postId] || []).map(c => 
            c.id === commentId 
              ? { ...c, replies: replies }
              : c
          )
        }));
      } catch (error) {
        console.error('Error fetching replies:', error);
      }
    }
    
    setShowReplies(prev => ({ ...prev, [commentId]: !isCurrentlyVisible }));
  };

  const handleFollowUser = async (userId: string) => {
    try {
      const updatedUser = await usersApi.followUser(userId);
      setPeopleSuggestions(prev => 
        prev.map(user => 
          user.id === userId 
            ? { ...user, isFollowing: updatedUser.isFollowing }
            : user
        )
      );
    } catch (error) {
      console.error('Error following user:', error);
    }
  };

  const handleApproveFollowRequest = async (followRequestId: number) => {
    try {
      await usersApi.approveFollowRequest(followRequestId);
      setFollowRequests(prev => prev.filter(req => req.id !== followRequestId));
      
      // Refresh follow requests
      const requests = await usersApi.getFollowRequests();
      setFollowRequests(requests);
    } catch (error) {
      console.error('Error approving follow request:', error);
    }
  };

  const handleRejectFollowRequest = async (followRequestId: number) => {
    try {
      await usersApi.rejectFollowRequest(followRequestId);
      setFollowRequests(prev => prev.filter(req => req.id !== followRequestId));
    } catch (error) {
      console.error('Error rejecting follow request:', error);
    }
  };

  const handleRepost = async (postId: number) => {
    try {
      const updatedPost = await postsApi.repost(postId);
      setPosts(prev => prev.map(p => p.id === postId ? updatedPost : p));
    } catch (error) {
      console.error('Error reposting:', error);
    }
  };

  const handleQuoteRepost = async () => {
    if (!repostingPost || !quoteContent.trim()) return;

    try {
      // Create a new post with quote content and reference to original post
      const quotePost = await postsApi.createPost(quoteContent);
      setPosts(prev => [quotePost, ...prev]);
      setShowRepostModal(false);
      setRepostingPost(null);
      setQuoteContent('');
    } catch (error) {
      console.error('Error creating quote repost:', error);
      alert('Failed to create quote repost. Please try again.');
    }
  };

  const handleLogout = async () => {
    try {
      await authApi.logout();
      navigate('/login');
    } catch (error) {
      console.error('Error logging out:', error);
      localStorage.removeItem('access_token');
      navigate('/login');
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
    <div id="wrapper">
      {/* Sidebar */}
      <Sidebar 
        currentUser={currentUser}
        onToggleTheme={() => {
          // Toggle dark mode logic here
          document.documentElement.classList.toggle('dark');
        }}
        isDarkMode={document.documentElement.classList.contains('dark')}
      />

      {/* Header */}
      <header className="z-[100] h-[--m-top] fixed top-0 left-0 w-full flex items-center bg-white/80 backdrop-blur-xl border-b border-slate-200 dark:bg-dark2 dark:border-slate-800">
        <div className="flex items-center justify-between w-full max-w-2xl mx-auto px-4">
          {/* Logo */}
          <div id="logo">
            <a href="/feed" className="flex items-center">
              <div className="w-24 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-lg">Urutte</span>
              </div>
            </a>
          </div>
          
          {/* Profile */}
          <div className="flex items-center gap-3">
            <button 
              type="button" 
              onClick={() => navigate('/messages')}
              className="p-2 rounded-full hover:bg-gray-100 dark:hover:bg-slate-600 relative"
            >
              <IonIcon icon={chatboxEllipsesOutline} className="text-2xl" />
              {messageCount > 0 && (
                <div className="absolute top-0 right-0 -m-1 bg-red-600 text-white text-xs px-1 rounded-full">{messageCount}</div>
              )}
            </button>
            
            <div className="rounded-full relative cursor-pointer" onClick={handleLogout} title="Logout">
              <img 
                src={currentUser?.picture || "/assets/images/avatars/avatar-2.jpg"} 
                alt={currentUser?.name || "User"} 
                className="w-8 h-8 rounded-full shadow" 
                onError={(e) => {
                  (e.target as HTMLImageElement).src = "/assets/images/avatars/avatar-2.jpg";
                }}
              />
            </div>
          </div>
        </div>
      </header>


      {/* Main Content */}
      <main id="site__main" className="p-2.5 h-[calc(100vh-var(--m-top))] mt-[--m-top]">
        <div className="max-w-2xl mx-auto" id="js-oversized">
          {/* Center Feed */}
          <div className="space-y-6">
            {/* Create Post */}
            <div className="bg-white rounded-2xl border border-gray-200 p-4 dark:bg-dark2 dark:border-slate-700">
              <div className="flex items-center gap-3">
                <img 
                  src={currentUser?.picture || "/assets/images/avatars/avatar-2.jpg"} 
                  alt={currentUser?.name || "User"} 
                  className="w-10 h-10 rounded-full" 
                />
                <div 
                  className="flex-1 bg-gray-50 hover:bg-gray-100 transition-all rounded-full cursor-pointer dark:bg-slate-700 dark:hover:bg-slate-600" 
                  onClick={() => {
                    const modal = document.querySelector('#create-status');
                    if (modal) {
                      (modal as HTMLElement).style.display = 'block';
                    }
                  }}
                >
                  <div className="py-3 px-4 text-gray-500 dark:text-gray-300"> Start a thread... </div>
                </div>
              </div>
            </div>

            {/* Posts Feed */}
            {posts.length === 0 ? (
              <div className="bg-white rounded-2xl border border-gray-200 p-8 text-center dark:bg-dark2 dark:border-slate-700">
                <p className="text-gray-500 dark:text-white/70">No threads yet. Start the conversation!</p>
              </div>
            ) : (
              posts.map((post) => {
                console.log('Rendering post:', post);
                return (
                <div 
                  key={post.id} 
                  className="bg-white rounded-2xl border border-gray-200 dark:bg-dark2 dark:border-slate-700 cursor-pointer hover:shadow-md transition-shadow"
                  onClick={() => navigate(`/thread/${post.id}`)}
                >
                  {/* Post Header */}
                  <div className="flex gap-3 sm:p-4 p-2.5 text-sm font-medium">
                    <a href="#"> 
                      <img 
                        src={post.userPicture || "/assets/images/avatars/avatar-3.jpg"} 
                        alt={post.userName} 
                        className="w-9 h-9 rounded-full" 
                        onError={(e) => {
                          (e.target as HTMLImageElement).src = "/assets/images/avatars/avatar-3.jpg";
                        }}
                      />
                    </a>
                    <div className="flex-1">
                      <a href="#"> <h4 className="text-black dark:text-white"> {post.userName} </h4> </a>
                      <div className="text-xs text-gray-500 dark:text-white/80"> 
                        {new Date(post.timestamp).toLocaleDateString()} at {new Date(post.timestamp).toLocaleTimeString()}
                      </div>
                    </div>
                    <div className="-mr-1">
                      <div className="relative">
                        <button 
                          type="button" 
                          className="button-icon w-8 h-8"
                          onClick={(e) => {
                            e.stopPropagation();
                            console.log('Post menu button clicked for post:', post.id);
                            const menu = document.getElementById(`post-menu-${post.id}`);
                            console.log('Menu element found:', menu);
                            if (menu) {
                              const currentDisplay = menu.style.display;
                              console.log('Current display:', currentDisplay);
                              menu.style.display = currentDisplay === 'block' ? 'none' : 'block';
                              console.log('New display:', menu.style.display);
                            }
                          }}
                        >
                          <IonIcon icon={ellipsisHorizontal} className="text-xl" />
                        </button>
                        
                        {/* Dropdown menu - only show for post owner */}
                        {currentUser && post.userId === currentUser.id && (
                          <div 
                            id={`post-menu-${post.id}`}
                            className="absolute right-0 top-8 bg-white dark:bg-dark2 border border-gray-200 dark:border-slate-700 rounded-lg shadow-lg py-1 z-10"
                            style={{ display: 'none', minWidth: '120px' }}
                          >
                            <button
                              type="button"
                              className="w-full px-4 py-2 text-left text-sm text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 flex items-center gap-2"
                              onClick={() => {
                                handleDeletePost(post.id);
                                const menu = document.getElementById(`post-menu-${post.id}`);
                                if (menu) {
                                  menu.style.display = 'none';
                                }
                              }}
                            >
                              <IonIcon icon={trash} className="text-sm" />
                              Delete
                            </button>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Post Content */}
                  <div className="sm:px-4 p-2.5 pt-0">
                    <p className="font-normal">{post.content}</p>
                  </div>

                  {/* Post Image/Video */}
                  {post.mediaUrl && (
                    <div className="relative w-full lg:h-96 h-full sm:px-4">
                      {post.mediaType?.startsWith('image') ? (
                        <img 
                          src={post.mediaUrl.startsWith('http') ? post.mediaUrl : `http://localhost:8080/api/upload/${post.mediaUrl}`} 
                          alt="" 
                          className="sm:rounded-lg w-full h-full object-cover" 
                          onError={(e) => {
                            console.error('Image failed to load:', post.mediaUrl);
                            (e.target as HTMLImageElement).style.display = 'none';
                          }}
                        />
                      ) : post.mediaType?.startsWith('video') ? (
                        <video 
                          src={post.mediaUrl.startsWith('http') ? post.mediaUrl : `http://localhost:8080/api/upload/${post.mediaUrl}`} 
                          controls 
                          className="sm:rounded-lg w-full h-full object-cover" 
                        />
                      ) : null}
                    </div>
                  )}

                  {/* Post Actions */}
                  <div className="px-4 pb-4 flex items-center justify-between text-sm">
                    <div className="flex items-center gap-6">
                      <button 
                        type="button" 
                        onClick={(e) => {
                          e.stopPropagation();
                          handleLikePost(post.id);
                        }}
                        className={`flex items-center gap-2 hover:bg-gray-100 dark:hover:bg-slate-700 px-3 py-2 rounded-full transition-colors ${post.isLiked ? 'text-red-500' : 'text-gray-500'}`}
                      >
                        <IonIcon icon={heart} className="text-lg" />
                        <span>{post.likes}</span>
                      </button>
                      <button 
                        type="button" 
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate(`/thread/${post.id}`);
                        }}
                        className="flex items-center gap-2 hover:bg-gray-100 dark:hover:bg-slate-700 px-3 py-2 rounded-full transition-colors text-gray-500"
                      >
                        <IonIcon icon={chatbubbleEllipses} className="text-lg" />
                        <span>{post.commentsCount || 0}</span>
                      </button>
                      <button 
                        type="button" 
                        onClick={(e) => {
                          e.stopPropagation();
                          setRepostingPost(post);
                          setShowRepostModal(true);
                        }}
                        className="flex items-center gap-2 hover:bg-gray-100 dark:hover:bg-slate-700 px-3 py-2 rounded-full transition-colors text-gray-500"
                      >
                        <IonIcon icon={repeat} className="text-lg" />
                        <span>{post.reposts || 0}</span>
                      </button>
                      <button 
                        type="button" 
                        onClick={(e) => {
                          e.stopPropagation();
                          if (navigator.share) {
                            navigator.share({
                              title: 'Check out this thread',
                              text: post.content,
                              url: window.location.origin + `/thread/${post.id}`
                            });
                          } else {
                            // Fallback: copy to clipboard
                            navigator.clipboard.writeText(window.location.origin + `/thread/${post.id}`);
                            alert('Link copied to clipboard!');
                          }
                        }}
                        className="flex items-center gap-2 hover:bg-gray-100 dark:hover:bg-slate-700 px-3 py-2 rounded-full transition-colors text-gray-500"
                      >
                        <IonIcon icon={shareOutline} className="text-lg" />
                      </button>
                    </div>
                  </div>

                  {/* Comments Section */}
                  {showComments[post.id] && (
                    <div className="border-t border-gray-100 dark:border-slate-700/40">
                      {/* Display Comments */}
                      {comments[post.id] && comments[post.id].length > 0 && (
                        <div className="p-4 space-y-3">
                          {comments[post.id].map((comment: Comment) => (
                            <div key={comment.id} className="space-y-2">
                              {/* Main Comment */}
                              <div className="flex gap-3">
                                <img 
                                  src={comment.userPicture || "/assets/images/avatars/avatar-3.jpg"} 
                                  alt={comment.userName} 
                                  className="w-8 h-8 rounded-full" 
                                  onError={(e) => {
                                    (e.target as HTMLImageElement).src = "/assets/images/avatars/avatar-3.jpg";
                                  }}
                                />
                                <div className="flex-1">
                                  <div className="bg-gray-100 dark:bg-slate-700 rounded-xl p-3">
                                    <div className="flex justify-between items-start mb-1">
                                      <div className="font-medium text-sm text-gray-800 dark:text-gray-200">{comment.userName}</div>
                                      {/* Menu button - only show for comment owner */}
                                      {currentUser && comment.userId === currentUser.id && (
                                        <div className="relative">
                                          <button 
                                            type="button" 
                                            className="text-gray-500 hover:text-gray-700 text-xs p-1 rounded-full hover:bg-gray-200 dark:hover:bg-slate-600"
                                            onClick={(e) => {
                                              e.stopPropagation();
                                              const menu = document.getElementById(`comment-menu-${comment.id}`);
                                              if (menu) {
                                                const currentDisplay = menu.style.display;
                                                menu.style.display = currentDisplay === 'block' ? 'none' : 'block';
                                              }
                                            }}
                                          >
                                            <IonIcon icon={ellipsisHorizontal} className="text-sm" />
                                          </button>
                                          
                                          {/* Dropdown menu */}
                                          <div 
                                            id={`comment-menu-${comment.id}`}
                                            className="absolute right-0 top-6 bg-white dark:bg-dark2 border border-gray-200 dark:border-slate-700 rounded-lg shadow-lg py-1 z-10"
                                            style={{ display: 'none', minWidth: '100px' }}
                                          >
                                            <button
                                              type="button"
                                              className="w-full px-3 py-1 text-left text-xs text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 flex items-center gap-1"
                                              onClick={() => {
                                                handleDeleteComment(comment.id, post.id);
                                                const menu = document.getElementById(`comment-menu-${comment.id}`);
                                                if (menu) {
                                                  menu.style.display = 'none';
                                                }
                                              }}
                                            >
                                              <IonIcon icon={trash} className="text-xs" />
                                              Delete
                                            </button>
                                          </div>
                                        </div>
                                      )}
                                    </div>
                                    <div className="text-sm text-gray-700 dark:text-gray-300 mb-2">{comment.content}</div>
                                  </div>
                                  
                                  {/* Comment Actions */}
                                  <div className="flex items-center gap-4 mt-2 text-xs">
                                    <button 
                                      type="button" 
                                      onClick={(e) => {
                                        e.stopPropagation();
                                        handleLikeComment(comment.id, post.id);
                                      }}
                                      className={`flex items-center gap-1 hover:bg-gray-100 dark:hover:bg-slate-600 px-2 py-1 rounded-full transition-colors ${comment.isLiked ? 'text-red-500' : 'text-gray-500'}`}
                                    >
                                      <IonIcon icon={heart} className="text-sm" />
                                      <span>{comment.likesCount}</span>
                                    </button>
                                    
                                    <button 
                                      type="button" 
                                      onClick={(e) => {
                                        e.stopPropagation();
                                        handleToggleReplies(comment.id, post.id);
                                      }}
                                      className="flex items-center gap-1 text-gray-500 hover:bg-gray-100 dark:hover:bg-slate-600 px-2 py-1 rounded-full transition-colors"
                                    >
                                      <IonIcon icon={chatbubbleEllipses} className="text-sm" />
                                      <span>{comment.repliesCount}</span>
                                    </button>
                                    
                                    <div className="text-xs text-gray-500 dark:text-gray-400">
                                      {new Date(comment.createdAt).toLocaleString()}
                                    </div>
                                  </div>
                                  
                                </div>
                              </div>
                              
                              {/* Replies and Reply Input - Only show when toggled */}
                              {showReplies[comment.id] && (
                                <>
                                  {/* Replies */}
                                  {comment.replies && comment.replies.length > 0 && (
                                    <div className="ml-11 mt-3 space-y-3">
                                      {comment.replies.map((reply: Comment) => (
                                        <div key={reply.id} className="flex gap-3">
                                          <img 
                                            src={reply.userPicture || "/assets/images/avatars/avatar-3.jpg"} 
                                            alt={reply.userName} 
                                            className="w-7 h-7 rounded-full" 
                                          />
                                          <div className="flex-1">
                                            <div className="bg-gray-50 dark:bg-slate-600 rounded-xl p-3 border-l-2 border-blue-200 dark:border-blue-800">
                                              <div className="flex justify-between items-start mb-1">
                                                <div className="font-medium text-sm text-gray-800 dark:text-gray-200">{reply.userName}</div>
                                                {currentUser && reply.userId === currentUser.id && (
                                                  <button 
                                                    type="button" 
                                                    onClick={() => handleDeleteComment(reply.id, post.id)}
                                                    className="text-red-500 hover:text-red-700 text-xs p-1 rounded-full hover:bg-red-50 dark:hover:bg-red-900/20"
                                                    title="Delete reply"
                                                  >
                                                    <IonIcon icon={trash} className="text-xs" />
                                                  </button>
                                                )}
                                              </div>
                                              <div className="text-sm text-gray-700 dark:text-gray-300 mb-2">{reply.content}</div>
                                              <div className="flex items-center gap-4 text-xs">
                                            <button 
                                              type="button" 
                                              onClick={(e) => {
                                                e.stopPropagation();
                                                handleLikeComment(reply.id, post.id);
                                              }}
                                              className={`flex items-center gap-1 hover:bg-gray-100 dark:hover:bg-slate-500 px-2 py-1 rounded-full transition-colors ${reply.isLiked ? 'text-red-500' : 'text-gray-500'}`}
                                            >
                                              <IonIcon icon={heart} className="text-sm" />
                                              <span>{reply.likesCount}</span>
                                            </button>
                                                <div className="text-gray-500 dark:text-gray-400">
                                                  {new Date(reply.createdAt).toLocaleString()}
                                                </div>
                                              </div>
                                            </div>
                                          </div>
                                        </div>
                                      ))}
                                    </div>
                                  )}
                                  
                                  {/* Reply Input - At the end of replies */}
                                  <div className="ml-11 mt-3 flex items-center gap-2 bg-gray-50 dark:bg-slate-600 rounded-full px-3 py-2">
                                    <img 
                                      src={currentUser?.picture || "/assets/images/avatars/avatar-7.jpg"} 
                                      alt={currentUser?.name || "User"} 
                                      className="w-6 h-6 rounded-full" 
                                    />
                                    <input 
                                      type="text" 
                                      placeholder="Reply to this comment..." 
                                      value={newReply[comment.id] || ''}
                                      onChange={(e) => setNewReply(prev => ({ ...prev, [comment.id]: e.target.value }))}
                                      className="flex-1 text-sm bg-transparent border-none outline-none text-gray-700 dark:text-gray-300 placeholder-gray-500 dark:placeholder-gray-400"
                                      onKeyPress={(e) => {
                                        if (e.key === 'Enter') {
                                          handleAddReply(comment.id, post.id);
                                        }
                                      }}
                                    />
                                    <button 
                                      type="button" 
                                      onClick={() => handleAddReply(comment.id, post.id)}
                                      disabled={!newReply[comment.id]?.trim()}
                                      className="text-sm text-blue-500 hover:text-blue-600 disabled:opacity-50 disabled:cursor-not-allowed font-medium px-3 py-1 rounded-full hover:bg-blue-50 dark:hover:bg-blue-900/20 transition-colors"
                                    >
                                      Reply
                                    </button>
                                  </div>
                                </>
                              )}
                            </div>
                          ))}
                        </div>
                      )}
                      
                      {/* Add Comment */}
                      <div className="sm:px-4 sm:py-3 p-2.5 border-t border-gray-100 flex items-center gap-1 dark:border-slate-700/40">
                        <img 
                          src={currentUser?.picture || "/assets/images/avatars/avatar-7.jpg"} 
                          alt={currentUser?.name || "User"} 
                          className="w-6 h-6 rounded-full" 
                          onError={(e) => {
                            (e.target as HTMLImageElement).src = "/assets/images/avatars/avatar-7.jpg";
                          }}
                        />
                        
                        <div className="flex-1 relative overflow-hidden h-10">
                          <textarea 
                            placeholder="Add Comment...." 
                            rows={1} 
                            value={newComment[post.id] || ''}
                            onChange={(e) => setNewComment(prev => ({ ...prev, [post.id]: e.target.value }))}
                            className="w-full resize-none !bg-transparent px-4 py-2 focus:!border-transparent focus:!ring-transparent"
                          ></textarea>
                        </div>

                        <button 
                          type="button" 
                          onClick={() => handleAddComment(post.id)}
                          disabled={!newComment[post.id]?.trim()}
                          className="text-sm rounded-full py-1.5 px-3.5 bg-secondery disabled:opacity-50 disabled:cursor-not-allowed"
                        > 
                          Reply
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              );
              })
            )}
          </div>
        </div>
      </main>

      {/* Create Status Modal */}
      <div 
        id="create-status" 
        style={{ 
          display: 'none',
          position: 'fixed',
          top: 0,
          left: 0,
          width: '100%',
          height: '100%',
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          zIndex: 9999,
          padding: '20px'
        }}
        onClick={(e) => {
          if (e.target === e.currentTarget) {
            (e.currentTarget as HTMLElement).style.display = 'none';
            setReplyingTo(null);
            setNewPostContent('');
            setSelectedMedia(null);
            setMediaPreview(null);
          }
        }}
      >
        <div className="tt relative overflow-hidden mx-auto bg-white shadow-xl rounded-2xl md:w-[520px] w-full dark:bg-dark2" style={{ marginTop: '50px' }}>
          <div className="text-center py-4 border-b mb-0 dark:border-slate-700">
            <h2 className="text-lg font-semibold text-black dark:text-white">
              {replyingTo ? 'Reply to Thread' : 'New Thread'}
            </h2>
            <button 
              type="button" 
              className="button-icon absolute top-0 right-0 m-2.5"
              onClick={() => {
                const modal = document.querySelector('#create-status');
                if (modal) {
                  (modal as HTMLElement).style.display = 'none';
                }
                setReplyingTo(null);
                setNewPostContent('');
                setSelectedMedia(null);
                setMediaPreview(null);
              }}
            >
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor" className="w-6 h-6">
                <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          <div className="space-y-5 mt-3 p-2">
            {/* Reply Context */}
            {replyingTo && (
              <div className="bg-gray-50 dark:bg-slate-700 rounded-lg p-3 border-l-4 border-blue-500">
                <div className="flex items-center gap-2 mb-2">
                  <img 
                    src={replyingTo.userPicture || "/assets/images/avatars/avatar-3.jpg"} 
                    alt={replyingTo.userName} 
                    className="w-6 h-6 rounded-full" 
                  />
                  <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
                    {replyingTo.userName}
                  </span>
                </div>
                <p className="text-sm text-gray-600 dark:text-gray-400 line-clamp-2">
                  {replyingTo.content}
                </p>
              </div>
            )}
            
            <div className="relative">
              <textarea 
                className="w-full !text-black placeholder:!text-gray-500 !bg-white !border-transparent focus:!border-transparent focus:!ring-transparent !font-normal !text-lg dark:!text-white dark:placeholder:!text-gray-400 dark:!bg-slate-800" 
                rows={6} 
                placeholder={replyingTo ? "Add a reply..." : "Start a thread..."}
                value={newPostContent}
                onChange={(e) => setNewPostContent(e.target.value)}
                maxLength={2000}
              ></textarea>
              <div className={`absolute bottom-2 right-2 text-xs ${
                newPostContent.length > 1800 ? 'text-red-500' : 
                newPostContent.length > 1500 ? 'text-yellow-500' : 
                'text-gray-400'
              }`}>
                {newPostContent.length}/2000
              </div>
            </div>
            
            {/* Media Preview */}
            {mediaPreview && (
              <div className="relative">
                {selectedMedia?.type.startsWith('image/') ? (
                  <img src={mediaPreview} alt="Preview" className="w-full max-h-64 object-cover rounded-lg" />
                ) : selectedMedia?.type.startsWith('video/') ? (
                  <video src={mediaPreview} controls className="w-full max-h-64 object-cover rounded-lg" />
                ) : null}
                <button
                  type="button"
                  onClick={handleRemoveMedia}
                  className="absolute top-2 right-2 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-sm hover:bg-red-600"
                >
                  ×
                </button>
              </div>
            )}
          </div>

          <div className="flex items-center gap-2 text-sm py-2 px-4 font-medium flex-wrap">
            <label className="flex items-center gap-1.5 bg-sky-50 text-sky-600 rounded-full py-1 px-2 border-2 border-sky-100 dark:bg-sky-950 dark:border-sky-900 cursor-pointer hover:bg-sky-100 dark:hover:bg-sky-900 transition-colors">
              <IonIcon icon={image} className="text-base" />
              Image
              <input
                type="file"
                accept="image/*"
                onChange={handleMediaSelect}
                className="hidden"
              />
            </label>
            <label className="flex items-center gap-1.5 bg-teal-50 text-teal-600 rounded-full py-1 px-2 border-2 border-teal-100 dark:bg-teal-950 dark:border-teal-900 cursor-pointer hover:bg-teal-100 dark:hover:bg-teal-900 transition-colors">
              <IonIcon icon={videocam} className="text-base" />
              Video
              <input
                type="file"
                accept="video/*"
                onChange={handleMediaSelect}
                className="hidden"
              />
            </label>
            <button 
              type="button" 
              onClick={() => {
                const link = prompt('Enter a link to share:');
                if (link && link.trim()) {
                  setNewPostContent(prev => prev + (prev ? '\n' : '') + link.trim());
                }
              }}
              className="flex items-center gap-1.5 bg-purple-50 text-purple-600 rounded-full py-1 px-2 border-2 border-purple-100 dark:bg-purple-950 dark:border-purple-900 hover:bg-purple-100 dark:hover:bg-purple-900 transition-colors"
            >
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor" className="w-4 h-4">
                <path strokeLinecap="round" strokeLinejoin="round" d="M13.19 8.688a4.5 4.5 0 011.242 7.244l-4.5 4.5a4.5 4.5 0 01-6.364-6.364l1.757-1.757m13.35-.622l1.757-1.757a4.5 4.5 0 00-6.364-6.364l-4.5 4.5a4.5 4.5 0 001.242 7.244" />
              </svg>
              Link
            </button>
            <button type="button" className="flex items-center gap-1.5 bg-orange-50 text-orange-600 rounded-full py-1 px-2 border-2 border-orange-100 dark:bg-yellow-950 dark:border-yellow-900 hover:bg-orange-100 dark:hover:bg-yellow-900 transition-colors">
              <IonIcon icon={happy} className="text-base" />
              Feeling
            </button>
            <button type="button" className="flex items-center gap-1.5 bg-red-50 text-red-600 rounded-full py-1 px-2 border-2 border-rose-100 dark:bg-rose-950 dark:border-rose-900 hover:bg-red-100 dark:hover:bg-rose-900 transition-colors">
              <IonIcon icon={location} className="text-base" />
              Check in
            </button>
            <button type="button" className="grid place-items-center w-8 h-8 text-xl rounded-full bg-secondery hover:bg-gray-200 dark:hover:bg-slate-600 transition-colors">
              <IonIcon icon={ellipsisHorizontal} />
            </button>
          </div>

          <div className="p-5 flex justify-between items-center">
            <div>
              <button className="inline-flex items-center py-1 px-2.5 gap-1 font-medium text-sm rounded-full bg-slate-50 border-2 border-slate-100 group aria-expanded:bg-slate-100 dark:text-white dark:bg-slate-700 dark:border-slate-600" type="button">
                Everyone
                <IonIcon icon={chevronDownOutline} className="text-base duration-500 group-aria-expanded:rotate-180" />
              </button>
            </div>
            <button 
              type="button" 
              onClick={handleCreatePost}
              disabled={postingContent || !newPostContent.trim() || newPostContent.length > 2000}
              className={`py-2 px-8 rounded-full text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed transition-colors ${
                newPostContent.length > 1800 
                  ? 'bg-red-500 text-white hover:bg-red-600' 
                  : 'bg-black text-white hover:bg-gray-800 dark:bg-white dark:text-black dark:hover:bg-gray-200'
              }`}
            > 
              {postingContent ? 'Posting...' : (replyingTo ? 'Reply' : 'Post')}
            </button>
          </div>
        </div>
      </div>

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

export default FeedPage;

