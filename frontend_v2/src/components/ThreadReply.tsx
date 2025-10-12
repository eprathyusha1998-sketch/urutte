import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { IonIcon } from '@ionic/react';
import { 
  image, 
  chatbubbleOutline, 
  closeOutline, 
  checkmarkOutline,
  attachOutline,
  timeOutline
} from 'ionicons/icons';
import PostCard from './PostCard';
import { Thread, User } from '../types.d';

// Type for PostCard compatibility
type PostCardPost = {
  id: number;
  content: string;
  mediaUrl?: string;
  mediaType?: string;
  media?: Array<{
    id: number;
    mediaUrl: string;
    mediaType: string;
    altText?: string;
  }>;
  userId: string;
  userName: string;
  userEmail: string;
  userPicture?: string;
  likes: number;
  retweets: number;
  replies: number;
  reposts: number;
  commentsCount?: number;
  isLiked: boolean;
  isReposted?: boolean;
  isBookmarked?: boolean;
  timestamp: string;
  parentPostId?: number;
  threadLevel?: number;
  threadPath?: string;
  rootPostId?: number;
  quotedPostId?: number;
  isQuoteRepost?: boolean;
  quotedPost?: PostCardPost;
};

interface ThreadReplyProps {
  post: Thread;
  currentUser?: User | null;
  onLike: (postId: number) => Promise<void>;
  onRepost: (postId: number) => Promise<void>;
  onDelete: (postId: number) => Promise<void>;
  onReply: (parentPostId: number, content: string, mediaFile?: File) => Promise<void>;
  level: number;
  maxLevel?: number;
  isMainThread?: boolean;
}

const ThreadReply: React.FC<ThreadReplyProps> = ({
  post,
  currentUser,
  onLike,
  onRepost,
  onDelete,
  onReply,
  level,
  maxLevel = 5,
  isMainThread = true
}) => {
  const navigate = useNavigate();
  const [showReplyInput, setShowReplyInput] = useState(false);
  const [replyContent, setReplyContent] = useState('');
  const [selectedMedia, setSelectedMedia] = useState<File | null>(null);
  const [postingReply, setPostingReply] = useState(false);

  // Convert Thread to Post format for PostCard compatibility
  const convertThreadToPost = (thread: Thread): PostCardPost => {
    return {
      id: thread.id,
      content: thread.content,
      mediaUrl: thread.media && thread.media.length > 0 ? thread.media[0].mediaUrl : undefined,
      mediaType: thread.media && thread.media.length > 0 ? thread.media[0].mediaType?.toLowerCase() : undefined,
      media: thread.media ? thread.media.map(m => ({
        id: m.id,
        mediaUrl: m.mediaUrl,
        mediaType: m.mediaType?.toLowerCase() || 'image',
        altText: m.altText
      })) : undefined,
      userId: thread.userId,
      userName: thread.userName,
      userEmail: thread.userEmail,
      userPicture: thread.userPicture,
      likes: thread.likesCount,
      retweets: thread.repostsCount, // Map reposts to retweets for compatibility
      replies: thread.repliesCount,
      reposts: thread.repostsCount,
      commentsCount: thread.repliesCount,
      isLiked: thread.isLiked || false,
      isReposted: thread.isReposted || false,
      isBookmarked: thread.isBookmarked || false,
      timestamp: thread.createdAt,
      parentPostId: thread.parentThreadId,
      threadLevel: thread.threadLevel,
      threadPath: thread.threadPath,
      rootPostId: thread.rootThreadId,
      quotedPostId: thread.quotedThreadId,
      isQuoteRepost: thread.threadType === 'quote',
      quotedPost: thread.quotedThread ? convertThreadToPost(thread.quotedThread) : undefined
    };
  };

  const handleReplySubmit = async () => {
    if ((!replyContent.trim() && !selectedMedia) || postingReply) return;

    setPostingReply(true);
    try {
      // Always reply to the current post (this is correct for nested replies)
      await onReply(post.id, replyContent, selectedMedia || undefined);
      setReplyContent('');
      setSelectedMedia(null);
      setShowReplyInput(false);
    } catch (error) {
      console.error('Error creating reply:', error);
      alert('Failed to create reply. Please try again.');
    } finally {
      setPostingReply(false);
    }
  };

  const handleMediaSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedMedia(file);
    }
  };

  const handleCommentClick = (postId?: number) => {
    // Navigate to the reply's thread page when comment button is clicked
    // This creates a Twitter/Threads-like experience where clicking comment
    // takes you to that reply's individual page
    navigate(`/thread/${post.id}`);
  };

  return (
    <div className={`${level > 0 ? 'relative' : ''}`}>
      {/* Thread Connection Line */}
      {level > 0 && (
        <div className="absolute left-0 top-0 bottom-0 w-px bg-gray-200 dark:bg-slate-600"></div>
      )}
      
      {/* Reply Content */}
      <div className={`${level > 0 ? 'ml-8 pl-4 relative' : ''}`}>
        {level > 0 && (
          <div className="absolute -left-2 top-6 w-3 h-3 bg-white dark:bg-slate-800 border-2 border-gray-200 dark:border-slate-600 rounded-full"></div>
        )}
        
        <PostCard
          post={convertThreadToPost(post)}
          currentUser={currentUser}
          onLike={onLike}
          onRepost={onRepost}
          onDelete={onDelete}
          onCommentClick={handleCommentClick}
          isReply={level > 0}
          compact={level > 0}
          showCommentButton={true}
        />
      </div>

      {/* Reply Input */}
      {showReplyInput && level < maxLevel && (
        <div className="mt-2 bg-gray-50 dark:bg-slate-800 rounded-lg p-2">
          <div className="flex items-start gap-2">
            <img 
              src={currentUser?.picture || "/assets/images/avatars/avatar-2.jpg"} 
              alt={currentUser?.name || "User"} 
              className="w-6 h-6 rounded-full" 
            />
            <div className="flex-1">
              <textarea
                value={replyContent}
                onChange={(e) => setReplyContent(e.target.value)}
                placeholder={`Reply to ${post.userName}...`}
                className="w-full bg-transparent border-none outline-none focus:ring-0 focus:border-transparent text-gray-700 dark:text-gray-300 placeholder-gray-500 dark:placeholder-gray-400 resize-none text-xs"
                rows={2}
                maxLength={2000}
              />
              
              <div className="flex items-center justify-between mt-1">
                <div className="flex items-center gap-1">
                  <input
                    type="file"
                    accept="image/*,video/*"
                    onChange={handleMediaSelect}
                    className="hidden"
                    id={`media-upload-${post.id}`}
                  />
                  <label
                    htmlFor={`media-upload-${post.id}`}
                    className="flex items-center gap-1 bg-blue-50 text-blue-600 rounded-full py-0.5 px-1.5 border border-blue-200 dark:bg-blue-950 dark:border-blue-800 hover:bg-blue-100 dark:hover:bg-blue-900 transition-colors cursor-pointer text-xs"
                  >
                    <IonIcon 
                      icon={selectedMedia ? checkmarkOutline : attachOutline} 
                      className="text-sm" 
                    />
                  </label>
                </div>
                
                <div className="flex items-center gap-1">
                  <button
                    onClick={() => setShowReplyInput(false)}
                    className="px-2 py-1 text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-slate-700 rounded-full transition-colors text-xs"
                  >
                    <IonIcon icon={closeOutline} className="text-sm" />
                  </button>
                  <button
                    onClick={handleReplySubmit}
                    disabled={(!replyContent.trim() && !selectedMedia) || postingReply}
                    className="bg-blue-500 text-white px-3 py-1 rounded-full text-xs font-medium hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  >
                    <IonIcon 
                      icon={postingReply ? timeOutline : chatbubbleOutline} 
                      className="text-sm" 
                    />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Note: Nested replies are now handled by the parent ThreadView component */}
    </div>
  );
};

export default ThreadReply;
