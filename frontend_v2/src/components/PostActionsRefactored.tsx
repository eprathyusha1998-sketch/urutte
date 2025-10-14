import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { IonIcon } from '@ionic/react';
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
import { postsApi } from '../services/api';
import { Button, Modal, Avatar, ContentRenderer } from './ui';
import { useThreadActions } from '../hooks';
import { ROUTES, DYNAMIC_ROUTES } from '../constants';

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
  const [showQuoteModal, setShowQuoteModal] = useState(false);
  const [quoteContent, setQuoteContent] = useState('');
  const [selectedQuoteMedia, setSelectedQuoteMedia] = useState<File | null>(null);
  const [quoteMediaPreview, setQuoteMediaPreview] = useState<string | null>(null);
  const { isActionLoading } = useThreadActions();

  const handleLike = async (e: React.MouseEvent) => {
    e.stopPropagation();
    await onLike(post.id);
  };

  const handleRepost = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (post.isReposted) {
      await onRepost(post.id);
    } else {
      setShowQuoteModal(true);
    }
  };

  const handleQuoteRepost = async () => {
    if (!quoteContent.trim() && !selectedQuoteMedia) return;
    
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
      navigate(DYNAMIC_ROUTES.THREAD_BY_ID(post.id));
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
          console.log('Link copied to clipboard');
        })
        .catch(console.error);
    }
  };

  const isLikeLoading = isActionLoading(`like-${post.id}`);
  const isRepostLoading = isActionLoading(`repost-${post.id}`);

  return (
    <>
      <div className={`flex items-center ${compact ? 'gap-4' : 'gap-6'}`}>
        {/* Like Button */}
        <Button 
          variant="ghost"
          size={compact ? 'sm' : 'md'}
          onClick={handleLike}
          disabled={isLikeLoading}
          loading={isLikeLoading}
          className={`${
            post.isLiked 
              ? 'text-red-500 hover:text-red-600' 
              : 'text-gray-500 hover:text-red-500'
          }`}
          title={post.isLiked ? 'Unlike' : 'Like'}
        >
          <IonIcon 
            icon={post.isLiked ? heart : heartOutline} 
            className={`${compact ? 'text-base' : 'text-lg'} ${post.isLiked ? 'fill-current' : ''}`} 
          />
          <span className={compact ? 'text-sm' : 'text-sm'}>{post.likes}</span>
        </Button>

        {/* Comment Button */}
        {showCommentButton && (
          <Button 
            variant="ghost"
            size={compact ? 'sm' : 'md'}
            onClick={handleComment}
            className="text-gray-500 hover:text-blue-500"
            title="Reply"
          >
            <IonIcon icon={chatbubbleEllipsesOutline} className={compact ? 'text-base' : 'text-lg'} />
            <span className={compact ? 'text-sm' : 'text-sm'}>{post.replies || 0}</span>
          </Button>
        )}

        {/* Repost Button */}
        <Button 
          variant="ghost"
          size={compact ? 'sm' : 'md'}
          disabled={isRepostLoading}
          loading={isRepostLoading}
          className={`${
            post.isReposted 
              ? 'text-green-500 hover:text-green-600' 
              : 'text-gray-500 hover:text-green-500'
          }`}
          title={post.isReposted ? 'Undo Repost' : 'Repost'}
          onClick={handleRepost}
        >
          <IonIcon 
            icon={post.isReposted ? repeat : repeatOutline} 
            className={compact ? 'text-base' : 'text-lg'} 
          />
          <span className={compact ? 'text-sm' : 'text-sm'}>{post.reposts || 0}</span>
        </Button>

        {/* Share Button */}
        <Button 
          variant="ghost"
          size={compact ? 'sm' : 'md'}
          onClick={handleShare}
          className="text-gray-500 hover:text-blue-500"
          title="Share"
        >
          <IonIcon icon={shareOutline} className={compact ? 'text-base' : 'text-lg'} />
        </Button>
      </div>

      {/* Quote Repost Modal */}
      <Modal
        isOpen={showQuoteModal}
        onClose={() => setShowQuoteModal(false)}
        title="Quote Repost"
        size="lg"
      >
        {/* Original Post */}
        <div className="border border-gray-200 dark:border-slate-700 rounded-lg p-4 mb-4 bg-gray-50 dark:bg-slate-800">
          <div className="flex items-center gap-3 mb-2">
            <Avatar
              src={post.userPicture}
              name={post.userName}
              size="sm"
            />
            <div>
              <span className="font-semibold text-gray-900 dark:text-white">{post.userName}</span>
              <span className="text-gray-500 dark:text-gray-400 text-sm ml-2">
                @{post.userEmail ? post.userEmail.split('@')[0] : 'user'}
              </span>
            </div>
          </div>
          <ContentRenderer content={post.content} className="text-gray-700 dark:text-gray-300" />
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
              <Button
                variant="ghost"
                size="sm"
                onClick={() => {
                  setSelectedQuoteMedia(null);
                  setQuoteMediaPreview(null);
                }}
                className="absolute top-2 right-2 bg-black bg-opacity-50 text-white rounded-full p-1 hover:bg-opacity-70"
              >
                <IonIcon icon={closeOutline} className="text-sm" />
              </Button>
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
              <Button
                variant="ghost"
                onClick={() => setShowQuoteModal(false)}
              >
                Cancel
              </Button>
              <Button
                variant="primary"
                onClick={handleQuoteRepost}
                disabled={(!quoteContent.trim() && !selectedQuoteMedia)}
              >
                Quote Repost
              </Button>
            </div>
          </div>
        </div>
      </Modal>
    </>
  );
};

export default PostActions;
