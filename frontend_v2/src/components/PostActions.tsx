import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { IonIcon } from '@ionic/react';
import { postsApi } from '../services/api';
import { generateInitials, getInitialsBackgroundColor } from '../utils/profileUtils';
import { getProfileImageUrl } from '../utils/mediaUtils';
import { 
  heart,
  heartOutline,
  chatbubbleEllipsesOutline, 
  repeat, 
  repeatOutline, 
  shareOutline,
  closeOutline,
  image
} from 'ionicons/icons';

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
  reposts: number;
  commentsCount?: number;
  isLiked: boolean;
  isReposted?: boolean;
  isBookmarked?: boolean;
  timestamp: string;
  parentPostId?: number;
}

interface PostActionsProps {
  post: Post;
  onLike: (postId: number) => Promise<void>;
  onRepost: (postId: number) => Promise<void>;
  onBookmark?: (postId: number) => Promise<void>;
  onShare?: (post: Post) => void;
  onCommentClick?: (postId: number) => void;
  showLabels?: boolean;
  compact?: boolean;
  showCommentButton?: boolean;
}

const PostActions: React.FC<PostActionsProps> = ({
  post,
  onLike,
  onRepost,
  onBookmark,
  onShare,
  onCommentClick,
  showLabels = true,
  compact = false,
  showCommentButton = true
}) => {
  const navigate = useNavigate();
  const [isLiking, setIsLiking] = useState(false);
  const [isReposting, setIsReposting] = useState(false);
  const [showQuoteModal, setShowQuoteModal] = useState(false);
  const [quoteContent, setQuoteContent] = useState('');
  const [selectedQuoteMedia, setSelectedQuoteMedia] = useState<File | null>(null);
  const [quoteMediaPreview, setQuoteMediaPreview] = useState<string | null>(null);

  const handleLike = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (isLiking) return;
    
    setIsLiking(true);
    try {
      await onLike(post.id);
    } catch (error) {
      console.error('Error liking post:', error);
    } finally {
      setIsLiking(false);
    }
  };

  const handleRepost = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (isReposting) return;
    
    setIsReposting(true);
    try {
      await onRepost(post.id);
    } catch (error) {
      console.error('Error reposting:', error);
    } finally {
      setIsReposting(false);
    }
  };

  const handleQuoteRepost = async () => {
    if (!quoteContent.trim() && !selectedQuoteMedia) return;
    
    setIsReposting(true);
    try {
      let mediaUrl, mediaType;
      if (selectedQuoteMedia) {
        // Upload media first
        const formData = new FormData();
        formData.append('content', quoteContent);
        formData.append('media', selectedQuoteMedia);
        
        const uploadResponse = await fetch('/api/posts/with-media', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('access_token')}`
          },
          body: formData
        });
        
        const uploadData = await uploadResponse.json();
        if (uploadData.success) {
          mediaUrl = uploadData.post.mediaUrl;
          mediaType = uploadData.post.mediaType;
        }
      }
      
      await postsApi.quoteRepost(quoteContent, post.id, mediaUrl, mediaType);
      setShowQuoteModal(false);
      setQuoteContent('');
      setSelectedQuoteMedia(null);
      setQuoteMediaPreview(null);
    } catch (error) {
      console.error('Error creating quote repost:', error);
      alert('Failed to create quote repost. Please try again.');
    } finally {
      setIsReposting(false);
    }
  };

  const handleQuoteMediaSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedQuoteMedia(file);
      const reader = new FileReader();
      reader.onload = (e) => {
        setQuoteMediaPreview(e.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  };


  const handleComment = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (onCommentClick) {
      onCommentClick(post.id);
    } else {
      navigate(`/thread/${post.id}`);
    }
  };

  const handleShare = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (onShare) {
      onShare(post);
    } else if (navigator.share) {
      navigator.share({
        title: `Post by ${post.userName}`,
        text: post.content,
        url: `${window.location.origin}/thread/${post.id}`
      }).catch(console.error);
    } else {
      // Fallback: copy to clipboard
      navigator.clipboard.writeText(`${window.location.origin}/thread/${post.id}`)
        .then(() => {
          // You could show a toast notification here
          console.log('Link copied to clipboard');
        })
        .catch(console.error);
    }
  };

  const actionClasses = compact 
    ? "flex items-center gap-1 hover:bg-gray-100 dark:hover:bg-slate-700 px-2 py-1 rounded-full transition-colors"
    : "flex items-center gap-2 hover:bg-gray-100 dark:hover:bg-slate-700 px-3 py-2 rounded-full transition-colors";

  const iconClasses = compact ? "text-base" : "text-lg";
  const textClasses = compact ? "text-sm" : "text-sm";

  return (
    <div className={`flex items-center ${compact ? 'gap-4' : 'gap-6'}`}>
      {/* Like Button */}
      <button 
        type="button" 
        onClick={handleLike}
        disabled={isLiking}
        className={`${actionClasses} ${
          post.isLiked 
            ? 'text-red-500 hover:text-red-600' 
            : 'text-gray-500 hover:text-red-500'
        } ${isLiking ? 'opacity-50 cursor-not-allowed' : ''}`}
        title={post.isLiked ? 'Unlike' : 'Like'}
      >
        <IonIcon 
          icon={post.isLiked ? heart : heartOutline} 
          className={`${iconClasses} ${post.isLiked ? 'fill-current' : ''}`} 
        />
        <span className={textClasses}>{post.likes}</span>
      </button>

      {/* Comment Button */}
      {showCommentButton && (
        <button 
          type="button" 
          onClick={handleComment}
          className={`${actionClasses} text-gray-500 hover:text-blue-500`}
          title="Reply"
        >
          <IonIcon icon={chatbubbleEllipsesOutline} className={iconClasses} />
          <span className={textClasses}>{post.replies || 0}</span>
        </button>
      )}

      {/* Repost Button with Dropdown */}
      <div className="relative">
        <button 
          type="button" 
          disabled={isReposting}
          className={`${actionClasses} ${
            post.isReposted 
              ? 'text-green-500 hover:text-green-600' 
              : 'text-gray-500 hover:text-green-500'
          } ${isReposting ? 'opacity-50 cursor-not-allowed' : ''}`}
          title={post.isReposted ? 'Undo Repost' : 'Repost'}
          onClick={(e) => {
            e.stopPropagation();
            if (post.isReposted) {
              handleRepost(e);
            } else {
              setShowQuoteModal(true);
            }
          }}
        >
          <IonIcon 
            icon={post.isReposted ? repeat : repeatOutline} 
            className={iconClasses} 
          />
          <span className={textClasses}>{post.reposts || 0}</span>
        </button>
      </div>

      {/* Share Button */}
      <button 
        type="button" 
        onClick={handleShare}
        className={`${actionClasses} text-gray-500 hover:text-blue-500`}
        title="Share"
      >
        <IonIcon icon={shareOutline} className={iconClasses} />
      </button>

      {/* Quote Repost Modal */}
      {showQuoteModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white dark:bg-dark2 rounded-2xl p-6 w-full max-w-2xl mx-4">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Urutte Urutte!</h3>
              <button
                onClick={() => setShowQuoteModal(false)}
                className="p-2 hover:bg-gray-100 dark:hover:bg-slate-700 rounded-full"
              >
                <IonIcon icon={closeOutline} className="text-xl" />
              </button>
            </div>
            
            {/* Original Post */}
            <div className="border border-gray-200 dark:border-slate-700 rounded-lg p-4 mb-4 bg-gray-50 dark:bg-slate-800">
              <div className="flex items-center gap-3 mb-2">
                {post.userPicture ? (
                  <img 
                    src={getProfileImageUrl(post.userPicture)} 
                    alt={post.userName} 
                    className="w-8 h-8 rounded-full object-cover" 
                  />
                ) : (
                  <div className="w-8 h-8 rounded-full bg-gray-100 dark:bg-slate-600 flex items-center justify-center">
                    <div className={`w-6 h-6 rounded-full ${getInitialsBackgroundColor(post.userName || '')} flex items-center justify-center`}>
                      <span className="text-white text-xs font-semibold">
                        {generateInitials(post.userName || 'உ')}
                      </span>
                    </div>
                  </div>
                )}
                <div>
                  <span className="font-semibold text-gray-900 dark:text-white">{post.userName}</span>
                  <span className="text-gray-500 dark:text-gray-400 text-sm ml-2">@{post.userEmail ? post.userEmail.split('@')[0] : 'user'}</span>
                </div>
              </div>
              <p className="text-gray-700 dark:text-gray-300">{post.content}</p>
            </div>
            
            {/* Quote Content */}
            <div className="space-y-4">
              {quoteMediaPreview && (
                <div className="relative">
                  <img 
                    src={quoteMediaPreview} 
                    alt="Preview" 
                    className="w-full max-h-64 object-cover rounded-lg"
                  />
                  <button
                    onClick={() => {
                      setSelectedQuoteMedia(null);
                      setQuoteMediaPreview(null);
                    }}
                    className="absolute top-2 right-2 bg-black bg-opacity-50 text-white rounded-full p-1 hover:bg-opacity-70"
                  >
                    <IonIcon icon={closeOutline} className="text-sm" />
                  </button>
                </div>
              )}
              
              <textarea
                value={quoteContent}
                onChange={(e) => setQuoteContent(e.target.value)}
                placeholder="Add a comment..."
                className="w-full bg-transparent border-none outline-none text-gray-700 dark:text-gray-300 placeholder-gray-500 dark:placeholder-gray-400 resize-none"
                rows={4}
                maxLength={2000}
              />
              
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <input
                    type="file"
                    accept="image/*,video/*"
                    onChange={handleQuoteMediaSelect}
                    className="hidden"
                    id="quote-media-upload"
                  />
                  <label
                    htmlFor="quote-media-upload"
                    className="flex items-center gap-1.5 bg-blue-50 text-blue-600 rounded-full py-1 px-2 border-2 border-blue-100 dark:bg-blue-950 dark:border-blue-900 hover:bg-blue-100 dark:hover:bg-blue-900 transition-colors cursor-pointer"
                  >
                    <IonIcon icon={image} className="text-base" />
                    Media
                  </label>
                </div>
                
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => setShowQuoteModal(false)}
                    className="px-4 py-2 text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-slate-700 rounded-full transition-colors"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={handleQuoteRepost}
                    disabled={(!quoteContent.trim() && !selectedQuoteMedia) || isReposting}
                    className="bg-green-500 text-white px-6 py-2 rounded-full text-sm font-medium hover:bg-green-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  >
                    {isReposting ? 'Posting...' : 'Urutte Urutte!'}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PostActions;
