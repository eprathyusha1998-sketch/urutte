import { useState } from 'react';
import { threadsApi } from '../services/api';
import { Thread } from '../types.d';

export const useThreadActions = () => {
  const [loading, setLoading] = useState<{ [key: string]: boolean }>({});

  const setActionLoading = (action: string, isLoading: boolean) => {
    setLoading(prev => ({ ...prev, [action]: isLoading }));
  };

  const handleLike = async (threadId: number, currentThread: Thread) => {
    const actionKey = `like-${threadId}`;
    setActionLoading(actionKey, true);
    
    try {
      // The API likely toggles the like state, so we just call likeThread
      await threadsApi.likeThread(threadId);
      
      return {
        ...currentThread,
        isLiked: !currentThread.isLiked,
        likesCount: currentThread.isLiked 
          ? currentThread.likesCount - 1 
          : currentThread.likesCount + 1
      };
    } catch (error) {
      console.error('Error toggling like:', error);
      throw error;
    } finally {
      setActionLoading(actionKey, false);
    }
  };

  const handleRepost = async (threadId: number, currentThread: Thread) => {
    const actionKey = `repost-${threadId}`;
    setActionLoading(actionKey, true);
    
    try {
      // The API likely toggles the repost state, so we just call repostThread
      await threadsApi.repostThread(threadId);
      
      return {
        ...currentThread,
        isReposted: !currentThread.isReposted,
        repostsCount: currentThread.isReposted 
          ? currentThread.repostsCount - 1 
          : currentThread.repostsCount + 1
      };
    } catch (error) {
      console.error('Error toggling repost:', error);
      throw error;
    } finally {
      setActionLoading(actionKey, false);
    }
  };

  const handleBookmark = async (threadId: number, currentThread: Thread) => {
    const actionKey = `bookmark-${threadId}`;
    setActionLoading(actionKey, true);
    
    try {
      // The API likely toggles the bookmark state, so we just call bookmarkThread
      await threadsApi.bookmarkThread(threadId);
      
      return {
        ...currentThread,
        isBookmarked: !currentThread.isBookmarked
      };
    } catch (error) {
      console.error('Error toggling bookmark:', error);
      throw error;
    } finally {
      setActionLoading(actionKey, false);
    }
  };

  const handleDelete = async (threadId: number) => {
    const actionKey = `delete-${threadId}`;
    setActionLoading(actionKey, true);
    
    try {
      await threadsApi.deleteThread(threadId);
      return true;
    } catch (error) {
      console.error('Error deleting thread:', error);
      throw error;
    } finally {
      setActionLoading(actionKey, false);
    }
  };

  const isActionLoading = (action: string) => loading[action] || false;

  return {
    handleLike,
    handleRepost,
    handleBookmark,
    handleDelete,
    isActionLoading
  };
};
