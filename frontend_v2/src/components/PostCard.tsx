import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { IonIcon } from '@ionic/react';
import { 
  ellipsisHorizontal, 
  trash, 
  flag,
  image,
  videocam,
  document
} from 'ionicons/icons';
import PostActions from './PostActions';

interface Post {
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
  // Quote repost fields
  quotedPostId?: number;
  isQuoteRepost?: boolean;
  quotedPost?: Post;
}

interface User {
  id: string;
  name: string;
  email: string;
  picture?: string;
}

interface PostCardProps {
  post: Post;
  currentUser?: User | null;
  onLike: (postId: number) => Promise<void>;
  onRepost: (postId: number) => Promise<void>;
  onDelete?: (postId: number) => Promise<void>;
  onBookmark?: (postId: number) => Promise<void>;
  onShare?: (post: Post) => void;
  onCommentClick?: (postId: number) => void;
  showActions?: boolean;
  compact?: boolean;
  isReply?: boolean;
  showCommentButton?: boolean;
}

const PostCard: React.FC<PostCardProps> = ({
  post,
  currentUser,
  onLike,
  onRepost,
  onDelete,
  onBookmark,
  onShare,
  onCommentClick,
  showActions = true,
  compact = false,
  isReply = false,
  showCommentButton = true
}) => {
  const navigate = useNavigate();
  const [showOptions, setShowOptions] = useState(false);

  const formatTimeAgo = (timestamp: string) => {
    const now = new Date();
    const postTime = new Date(timestamp);
    const diffInSeconds = Math.floor((now.getTime() - postTime.getTime()) / 1000);

    if (diffInSeconds < 60) return 'now';
    if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}m`;
    if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}h`;
    if (diffInSeconds < 2592000) return `${Math.floor(diffInSeconds / 86400)}d`;
    return postTime.toLocaleDateString();
  };

  const handlePostClick = () => {
    navigate(`/thread/${post.id}`);
  };

  const renderContentWithHashtags = (content: string) => {
    if (!content) return '';
    
    // Split content by hashtags and mentions, preserving the delimiters
    const parts = content.split(/(#\w+|@\w+)/g);
    
    return parts.map((part, index) => {
      if (part.startsWith('#')) {
        return (
          <span 
            key={index} 
            className="text-blue-600 dark:text-blue-400 font-medium hover:underline cursor-pointer"
            onClick={(e) => {
              e.stopPropagation();
              navigate(`/hashtag/${part.substring(1)}`);
            }}
          >
            {part}
          </span>
        );
      } else if (part.startsWith('@')) {
        return (
          <span 
            key={index} 
            className="text-blue-600 dark:text-blue-400 font-medium hover:underline cursor-pointer"
            onClick={(e) => {
              e.stopPropagation();
              navigate(`/profile/${part.substring(1)}`);
            }}
          >
            {part}
          </span>
        );
      }
      return part;
    });
  };

  const handleUserClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    navigate(`/profile/${post.userId}`);
  };

  const handleDelete = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (onDelete && window.confirm('Are you sure you want to delete this post?')) {
      await onDelete(post.id);
    }
    setShowOptions(false);
  };

  const getMediaIcon = () => {
    if (!post.mediaUrl) return null;
    
    switch (post.mediaType) {
      case 'image':
        return <IonIcon icon={image} className="text-blue-500" />;
      case 'video':
        return <IonIcon icon={videocam} className="text-red-500" />;
      default:
        return <IonIcon icon={document} className="text-gray-500" />;
    }
  };

  return (
    <div 
      className={`bg-white dark:bg-dark2 
        ${isReply ? 'border-l-4 border-blue-200 dark:border-blue-800' : 'rounded-2xl border border-gray-200 dark:border-slate-700'} 
        ${compact ? 'p-3' : 'p-4'} 
        ${!isReply ? 'mb-4' : 'mb-0'}
        hover:bg-gray-50 dark:hover:bg-slate-800 transition-colors cursor-pointer`}
      onClick={handlePostClick}
    >
      {/* Post Header */}
      <div className="flex items-start gap-3 mb-3">
        {/* User Avatar */}
        <div 
          className="w-10 h-10 rounded-full bg-gray-300 dark:bg-slate-600 flex items-center justify-center overflow-hidden cursor-pointer flex-shrink-0"
          onClick={handleUserClick}
        >
          {post.userPicture ? (
            <img 
              src={post.userPicture} 
              alt={post.userName || 'User'}
              className="w-full h-full object-cover"
              onError={(e) => {
                const target = e.target as HTMLImageElement;
                target.style.display = 'none';
                const fallback = target.nextElementSibling as HTMLElement;
                if (fallback) fallback.style.display = 'flex';
              }}
            />
          ) : null}
          <span 
            className="text-gray-600 dark:text-gray-300 font-semibold w-full h-full flex items-center justify-center"
            style={{ display: post.userPicture ? 'none' : 'flex' }}
          >
            {post.userName?.charAt(0)?.toUpperCase() || 'U'}
          </span>
        </div>

        {/* Post Info */}
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-1">
            <button
              onClick={handleUserClick}
              className="font-semibold text-gray-900 dark:text-white hover:underline"
            >
              {post.userName || 'Unknown User'}
            </button>
            <span className="text-gray-500 dark:text-gray-400 text-sm">
              @{post.userEmail ? post.userEmail.split('@')[0] : 'user'}
            </span>
            <span className="text-gray-400 dark:text-gray-500">·</span>
            <span className="text-gray-500 dark:text-gray-400 text-sm">
              {formatTimeAgo(post.timestamp)}
            </span>
            {post.mediaUrl && (
              <>
                <span className="text-gray-400 dark:text-gray-500">·</span>
                {getMediaIcon()}
              </>
            )}
          </div>
        </div>

        {/* Options Menu */}
        <div className="relative">
          <button
            onClick={(e) => {
              e.stopPropagation();
              setShowOptions(!showOptions);
            }}
            className="p-2 rounded-full hover:bg-gray-100 dark:hover:bg-slate-700 transition-colors"
          >
            <IonIcon icon={ellipsisHorizontal} className="text-gray-500" />
          </button>

          {showOptions && (
            <div className="absolute right-0 top-10 bg-white dark:bg-slate-700 border border-gray-200 dark:border-slate-600 rounded-lg shadow-lg z-10 min-w-[160px]">
              {currentUser?.id === post.userId && onDelete && (
                <button
                  onClick={handleDelete}
                  className="w-full px-4 py-2 text-left text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 flex items-center gap-2"
                >
                  <IonIcon icon={trash} className="text-sm" />
                  Delete
                </button>
              )}
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  setShowOptions(false);
                }}
                className="w-full px-4 py-2 text-left text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-slate-600 flex items-center gap-2"
              >
                <IonIcon icon={flag} className="text-sm" />
                Report
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Media */}
      {post.media && post.media.length > 0 ? (
        <div className="mb-3">
          <div className={`grid gap-1 ${
            post.media.length === 1 ? 'grid-cols-1' :
            post.media.length === 2 ? 'grid-cols-2' :
            post.media.length === 3 ? 'grid-cols-3' :
            post.media.length === 4 ? 'grid-cols-2' :
            'grid-cols-3'
          }`}>
            {post.media.map((media, index) => (
              <div key={media.id} className="relative">
                {media.mediaType === 'image' ? (
                  <img 
                    src={media.mediaUrl.startsWith('http') ? media.mediaUrl : `http://localhost:8080/${media.mediaUrl}`} 
                    alt={media.altText || 'Post media'}
                    className={`w-full rounded-lg object-cover ${
                      post.media && post.media.length === 1 ? 'max-h-96' : 'aspect-square'
                    }`}
                    onError={(e) => {
                      console.error('Image failed to load:', media.mediaUrl);
                      (e.target as HTMLImageElement).style.display = 'none';
                    }}
                  />
                ) : media.mediaType === 'video' ? (
                  <video 
                    src={media.mediaUrl.startsWith('http') ? media.mediaUrl : `http://localhost:8080/${media.mediaUrl}`}
                    controls
                    className={`w-full rounded-lg ${
                      post.media && post.media.length === 1 ? 'max-h-96' : 'aspect-square'
                    }`}
                  />
                ) : (
                  <div className="flex items-center justify-center p-3 bg-gray-100 dark:bg-slate-700 rounded-lg aspect-square">
                    <div className="text-center">
                      <IonIcon icon={document} className="text-2xl text-gray-500 mb-2" />
                      <span className="text-xs text-gray-600 dark:text-gray-300 block">
                        {media.mediaType.toUpperCase()}
                      </span>
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      ) : post.mediaUrl ? (
        <div className="mb-3 rounded-xl overflow-hidden">
          {post.mediaType === 'image' ? (
            <img 
              src={post.mediaUrl.startsWith('http') ? post.mediaUrl : `http://localhost:8080/${post.mediaUrl}`} 
              alt="Post media"
              className="w-full max-h-96 object-cover"
              onError={(e) => {
                console.error('Image failed to load:', post.mediaUrl);
                (e.target as HTMLImageElement).style.display = 'none';
              }}
            />
          ) : post.mediaType === 'video' ? (
            <video 
              src={post.mediaUrl.startsWith('http') ? post.mediaUrl : `http://localhost:8080/${post.mediaUrl}`}
              controls
              className="w-full max-h-96"
            />
          ) : (
            <div className="p-4 bg-gray-100 dark:bg-slate-700 rounded-lg">
              <a 
                href={post.mediaUrl} 
                target="_blank" 
                rel="noopener noreferrer"
                className="text-blue-500 hover:underline flex items-center gap-2"
                onClick={(e) => e.stopPropagation()}
              >
                <IonIcon icon={document} />
                View File
              </a>
            </div>
          )}
        </div>
      ) : null}

      {/* Post Content */}
      <div className="mb-3">
        <p className="text-gray-900 dark:text-white whitespace-pre-wrap break-words">
          {renderContentWithHashtags(post.content)}
        </p>
      </div>

      {/* Quoted Post */}
      {post.isQuoteRepost && post.quotedPost && (
        <div className="border border-gray-200 dark:border-slate-600 rounded-lg p-3 mb-3 bg-gray-50 dark:bg-slate-800">
          <div className="flex items-center gap-2 mb-2">
            <div className="w-6 h-6 rounded-full bg-gray-300 dark:bg-slate-600 flex items-center justify-center overflow-hidden">
              {post.quotedPost.userPicture ? (
                <img 
                  src={post.quotedPost.userPicture} 
                  alt={post.quotedPost.userName} 
                  className="w-full h-full object-cover"
                />
              ) : (
                <span className="text-xs font-semibold text-gray-600 dark:text-gray-300">
                  {post.quotedPost.userName?.charAt(0)?.toUpperCase() || 'U'}
                </span>
              )}
            </div>
            <span className="font-semibold text-sm text-gray-900 dark:text-white">
              {post.quotedPost.userName || 'Unknown User'}
            </span>
            <span className="text-gray-500 dark:text-gray-400 text-xs">
              @{post.quotedPost.userEmail ? post.quotedPost.userEmail.split('@')[0] : 'user'}
            </span>
          </div>
            <p className="text-gray-800 dark:text-gray-200 text-sm whitespace-pre-wrap break-words">
              {renderContentWithHashtags(post.quotedPost.content)}
            </p>
          {post.quotedPost.mediaUrl && (
            <div className="mt-2 flex items-center gap-1 text-xs text-gray-500 dark:text-gray-400">
              {getMediaIcon()}
              <span>Media attached</span>
            </div>
          )}
        </div>
      )}

      {/* Post Actions */}
      {showActions && (
        <div className="pt-2 border-t border-gray-100 dark:border-slate-700">
          <PostActions
            post={post}
            onLike={onLike}
            onRepost={onRepost}
            onBookmark={onBookmark}
            onShare={onShare}
            onCommentClick={onCommentClick}
            compact={compact}
            showLabels={false}
            showCommentButton={showCommentButton}
          />
        </div>
      )}
    </div>
  );
};

export default PostCard;
