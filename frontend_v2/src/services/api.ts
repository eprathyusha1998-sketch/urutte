import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Create axios instance with default config
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Important for sending cookies with requests
});

// Add request interceptor to include auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Unauthorized - redirect to login
      localStorage.removeItem('access_token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authApi = {
  getCurrentUser: async () => {
    const response = await api.get('/users/me');
    return response.data;
  },
  
  login: (email: string, password: string) => {
    // For OAuth, we'll redirect to the OAuth endpoint
    window.location.href = `${API_BASE_URL.replace('/api', '')}/oauth2/authorization/google`;
  },
  
  logout: async () => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('user');
    await api.post('/auth/logout');
  },
};

// Threads API (New comprehensive API)
export const threadsApi = {
  // Get main threads (feed)
  getFeed: async (page = 0, size = 20) => {
    const response = await api.get('/threads', {
      params: { page, size },
    });
    return response.data;
  },

  // Get thread by ID
  getThreadById: async (threadId: number) => {
    const response = await api.get(`/threads/${threadId}`);
    return response.data;
  },

  // Create a new thread
  createThread: async (content: string, mediaFileOrFiles?: File | File[], parentThreadId?: number, replyPermission: 'ANYONE' | 'FOLLOWERS' | 'FOLLOWING' | 'MENTIONED_ONLY' = 'ANYONE') => {
    // Convert single file to array for consistent handling
    const mediaFiles = mediaFileOrFiles ? (Array.isArray(mediaFileOrFiles) ? mediaFileOrFiles : [mediaFileOrFiles]) : undefined;
    
    // If multiple media files, use the new endpoint
    if (mediaFiles && mediaFiles.length > 1) {
      return threadsApi.createThreadWithMultipleMedia(content, mediaFiles, parentThreadId, replyPermission);
    }
    
    // Single media file or no media - use existing endpoint
    const formData = new FormData();
    formData.append('content', content);
    if (mediaFiles && mediaFiles.length === 1) {
      formData.append('media', mediaFiles[0]);
    }
    if (parentThreadId) {
      formData.append('parentThreadId', parentThreadId.toString());
    }
    formData.append('replyPermission', replyPermission);
    
    const response = await api.post('/threads', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },
  
  // Create a new thread with multiple media files
  createThreadWithMultipleMedia: async (content: string, mediaFiles: File[], parentThreadId?: number, replyPermission: 'ANYONE' | 'FOLLOWERS' | 'FOLLOWING' | 'MENTIONED_ONLY' = 'ANYONE') => {
    const formData = new FormData();
    formData.append('content', content);
    
    // Add all media files
    mediaFiles.forEach((file) => {
      formData.append('media', file);
    });
    
    if (parentThreadId) {
      formData.append('parentThreadId', parentThreadId.toString());
    }
    formData.append('replyPermission', replyPermission);
    
    const response = await api.post('/threads/multiple-media', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  // Create a quote repost
  createQuoteRepost: async (content: string, quotedThreadId: number, mediaFile?: File) => {
    const formData = new FormData();
    formData.append('content', content);
    formData.append('quotedThreadId', quotedThreadId.toString());
    if (mediaFile) {
      formData.append('media', mediaFile);
    }
    
    const response = await api.post('/threads/quote-repost', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  // Get replies for a thread
  getThreadReplies: async (threadId: number) => {
    const response = await api.get(`/threads/${threadId}/replies`);
    return response.data;
  },

  // Like a thread
  likeThread: async (threadId: number) => {
    const response = await api.post(`/threads/${threadId}/like`);
    return response.data;
  },

  // Repost a thread
  repostThread: async (threadId: number, quoteContent?: string) => {
    const formData = new FormData();
    if (quoteContent) {
      formData.append('quoteContent', quoteContent);
    }
    
    const response = await api.post(`/threads/${threadId}/repost`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  // Bookmark a thread
  bookmarkThread: async (threadId: number) => {
    const response = await api.post(`/threads/${threadId}/bookmark`);
    return response.data;
  },

  // Add reaction to thread
  addReaction: async (threadId: number, reactionType: string) => {
    const response = await api.post(`/threads/${threadId}/reaction`, null, {
      params: { reactionType },
    });
    return response.data;
  },

  // Delete a thread
  deleteThread: async (threadId: number) => {
    const response = await api.delete(`/threads/${threadId}`);
    return response.data;
  },

  // Search threads
  searchThreads: async (keyword: string, page = 0, size = 20) => {
    const response = await api.get('/threads/search', {
      params: { q: keyword, page, size },
    });
    return response.data;
  },

  // Get threads by hashtag
  getThreadsByHashtag: async (hashtag: string, page = 0, size = 20) => {
    const response = await api.get(`/threads/hashtag/${hashtag}`, {
      params: { page, size },
    });
    return response.data;
  },

  // Get trending threads
  getTrendingThreads: async (page = 0, size = 20) => {
    const response = await api.get('/threads/trending', {
      params: { page, size },
    });
    return response.data;
  },
};

// Legacy Posts API (for backward compatibility)
export const postsApi = {
  getFeed: async (page = 0, size = 10) => {
    return threadsApi.getFeed(page, size);
  },

  getPostById: async (postId: number) => {
    return threadsApi.getThreadById(postId);
  },

  createPost: async (content: string, mediaUrl?: string, mediaType?: string, parentPostId?: number) => {
    return threadsApi.createThread(content, undefined, parentPostId);
  },

  createPostWithMedia: async (content: string, mediaFile?: File, parentPostId?: number) => {
    return threadsApi.createThread(content, mediaFile, parentPostId);
  },

  likePost: async (postId: number) => {
    return threadsApi.likeThread(postId);
  },

  deletePost: async (postId: number) => {
    return threadsApi.deleteThread(postId);
  },

  getComments: async (postId: number) => {
    return threadsApi.getThreadReplies(postId);
  },

  addComment: async (postId: number, content: string) => {
    return threadsApi.createThread(content, undefined, postId);
  },

  deleteComment: async (commentId: number) => {
    return threadsApi.deleteThread(commentId);
  },

  likeComment: async (commentId: number) => {
    return threadsApi.likeThread(commentId);
  },

  addReply: async (commentId: number, content: string) => {
    return threadsApi.createThread(content, undefined, commentId);
  },

  getReplies: async (commentId: number) => {
    return threadsApi.getThreadReplies(commentId);
  },

  getPostReplies: async (postId: number) => {
    return threadsApi.getThreadReplies(postId);
  },

  repost: async (postId: number) => {
    return threadsApi.repostThread(postId);
  },

  quoteRepost: async (content: string, quotedPostId: number, mediaUrl?: string, mediaType?: string) => {
    return threadsApi.createQuoteRepost(content, quotedPostId);
  },
};

// Messages API
export const messagesApi = {
  getConversation: async (userId: string, page = 0, size = 50) => {
    const response = await api.get(`/messages/conversation/${userId}`, {
      params: { page, size },
    });
    return response.data;
  },

  getConversationPartners: async () => {
    const response = await api.get('/messages/conversations');
    return response.data;
  },

  getUnreadMessages: async () => {
    const response = await api.get('/messages/unread');
    return response.data;
  },

  getUnreadCount: async () => {
    const response = await api.get('/messages/unread/count');
    return response.data;
  },

  markMessagesAsRead: async (senderId: string) => {
    const response = await api.put(`/messages/mark-read/${senderId}`);
    return response.data;
  },

  sendMessage: async (receiverId: string, content: string, messageType = 'text', mediaUrl?: string) => {
    const response = await api.post('/messages', {
      receiverId,
      content,
      messageType,
      mediaUrl,
    });
    return response.data;
  },
};

// Events API
export const eventsApi = {
  getAllEvents: async (page = 0, size = 20) => {
    const response = await api.get('/events', {
      params: { page, size },
    });
    return response.data;
  },

  getEventById: async (eventId: number) => {
    const response = await api.get(`/events/${eventId}`);
    return response.data;
  },

  getUpcomingEvents: async (page = 0, size = 20) => {
    const response = await api.get('/events/upcoming', {
      params: { page, size },
    });
    return response.data;
  },

  getEventsByCategory: async (category: string, page = 0, size = 20) => {
    const response = await api.get(`/events/category/${category}`, {
      params: { page, size },
    });
    return response.data;
  },

  getFreeEvents: async (page = 0, size = 20) => {
    const response = await api.get('/events/free', {
      params: { page, size },
    });
    return response.data;
  },

  getPaidEvents: async (page = 0, size = 20) => {
    const response = await api.get('/events/paid', {
      params: { page, size },
    });
    return response.data;
  },

  searchEvents: async (query: string, page = 0, size = 20) => {
    const response = await api.get('/events/search', {
      params: { q: query, page, size },
    });
    return response.data;
  },

  getTrendingEvents: async (page = 0, size = 20) => {
    const response = await api.get('/events/trending', {
      params: { page, size },
    });
    return response.data;
  },

  toggleAttendance: async (eventId: number) => {
    const response = await api.post(`/events/${eventId}/attend`);
    return response.data;
  },

  createEvent: async (eventData: any) => {
    const response = await api.post('/events', eventData);
    return response.data;
  },
};

// Users API
export const usersApi = {
  getProfile: async (userId: string) => {
    const response = await api.get(`/users/${userId}`);
    return response.data;
  },

  getPeopleYouMayKnow: async (limit = 5) => {
    const response = await api.get(`/users/suggestions?limit=${limit}`);
    return response.data;
  },

  followUser: async (userId: string) => {
    const response = await api.post(`/users/${userId}/follow`);
    return response.data;
  },

  searchUsers: async (query: string) => {
    const response = await api.get(`/users/search?q=${encodeURIComponent(query)}`);
    return response.data;
  },

  getFollowers: async (userId: string) => {
    const response = await api.get(`/users/${userId}/followers`);
    return response.data;
  },

  getFollowRequests: async () => {
    const response = await api.get('/users/follow-requests');
    return response.data;
  },

  approveFollowRequest: async (followRequestId: number) => {
    const response = await api.post(`/users/follow-requests/${followRequestId}/approve`);
    return response.data;
  },

  rejectFollowRequest: async (followRequestId: number) => {
    const response = await api.post(`/users/follow-requests/${followRequestId}/reject`);
    return response.data;
  },
};

// Notifications API
export const notificationsApi = {
  getNotifications: async (page = 0, size = 20) => {
    const response = await api.get('/notifications', {
      params: { page, size },
    });
    return response.data;
  },

  getUnreadCount: async () => {
    const response = await api.get('/notifications/unread/count');
    return response.data;
  },

  markAsRead: async (notificationId: number) => {
    const response = await api.put(`/notifications/${notificationId}/read`);
    return response.data;
  },

  markAllAsRead: async () => {
    const response = await api.put('/notifications/read-all');
    return response.data;
  },
};

export const hashtagApi = {
  getSuggestions: async (query: string, limit = 8) => {
    const response = await api.get('/hashtags/suggestions', {
      params: { q: query, limit },
    });
    return response.data;
  },

  getTrending: async (limit = 10) => {
    const response = await api.get('/hashtags/trending', {
      params: { limit },
    });
    return response.data;
  },

  getPopular: async (limit = 10) => {
    const response = await api.get('/hashtags/popular', {
      params: { limit },
    });
    return response.data;
  },
};

export default api;

