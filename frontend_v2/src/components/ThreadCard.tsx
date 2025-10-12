import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { IonIcon } from '@ionic/react';
import { 
  heart, 
  heartOutline, 
  chatbubbleEllipsesOutline, 
  repeatOutline, 
  shareOutline,
  bookmarkOutline,
  bookmark,
  closeOutline,
  image,
  happy,
  sad,
  flame,
  thumbsUp
} from 'ionicons/icons';
import { Thread, User } from '../types.d';

interface ThreadCardProps {
  thread: Thread;
  currentUser: User | null;
  onLike: (threadId: number) => void;
  onRepost: (threadId: number, quoteContent?: string) => void;
  onDelete: (threadId: number) => void;
  onReply?: (threadId: number) => void;
  onBookmark?: (threadId: number) => void;
  onReaction?: (threadId: number, reactionType: string) => void;
  isReply?: boolean;
  compact?: boolean;
  showCommentButton?: boolean;
}

const ThreadCard: React.FC<ThreadCardProps> = ({
  thread,
  currentUser,
  onLike,
  onRepost,
  onDelete,
  onReply,
  onBookmark,
  onReaction,
  isReply = false,
  compact = false,
  showCommentButton = true
}) => {
  const navigate = useNavigate();
  const [showReactionPicker, setShowReactionPicker] = useState(false);

  const handlePostClick = () => {
    navigate(`/thread/${thread.id}`);
  };

  const handleDelete = (e: React.MouseEvent) => {
    e.stopPropagation();
    onDelete(thread.id);
  };

  const handleLike = (e: React.MouseEvent) => {
    e.stopPropagation();
    onLike(thread.id);
  };

  const handleRepost = (e: React.MouseEvent) => {
    e.stopPropagation();
    onRepost(thread.id);
  };

  const handleReply = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (onReply) {
      onReply(thread.id);
    } else {
      // Navigate to thread page if no onReply handler is provided
      navigate(`/thread/${thread.id}`);
    }
  };

  const handleBookmark = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (onBookmark) {
      onBookmark(thread.id);
    }
  };

  const handleReaction = (reactionType: string) => {
    if (onReaction) {
      onReaction(thread.id, reactionType);
    }
    setShowReactionPicker(false);
  };

  const getMediaIcon = () => {
    if (!thread.media || thread.media.length === 0) return null;
    
    const firstMedia = thread.media[0];
    const mediaType = firstMedia.mediaType?.toLowerCase();
    switch (mediaType) {
      case 'image':
        return <IonIcon icon={image} className="text-blue-500" />;
      case 'video':
        return <IonIcon icon={image} className="text-red-500" />;
      case 'audio':
        return <IonIcon icon={image} className="text-green-500" />;
      default:
        return <IonIcon icon={image} className="text-gray-500" />;
    }
  };

  const formatTimeAgo = (timestamp: string) => {
    if (!timestamp) return 'Unknown time';
    
    try {
      const now = new Date();
      const time = new Date(timestamp);
      
      // Check if the date is valid
      if (isNaN(time.getTime())) {
        console.warn('Invalid timestamp:', timestamp);
        return 'Invalid date';
      }
      
      const diffInSeconds = Math.floor((now.getTime() - time.getTime()) / 1000);

      if (diffInSeconds < 60) return `${diffInSeconds}s`;
      if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}m`;
      if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}h`;
      if (diffInSeconds < 2592000) return `${Math.floor(diffInSeconds / 86400)}d`;
      return time.toLocaleDateString();
    } catch (error) {
      console.error('Error formatting timestamp:', timestamp, error);
      return 'Invalid date';
    }
  };

  const renderContent = (content: string) => {
    // Process hashtags and mentions
    const parts = content.split(/(#\w+|@\w+)/g);
    return parts.map((part, index) => {
      if (part.startsWith('#')) {
        return (
          <span key={index} className="text-blue-500 hover:underline cursor-pointer">
            {part}
          </span>
        );
      } else if (part.startsWith('@')) {
        return (
          <span key={index} className="text-blue-500 hover:underline cursor-pointer">
            {part}
          </span>
        );
      }
      return part;
    });
  };

  const cardClasses = `
    bg-white dark:bg-slate-800 rounded-lg shadow-sm border border-gray-200 dark:border-slate-700
    ${compact ? 'p-3' : 'p-4'}
    ${isReply ? 'ml-4 border-l-4 border-l-blue-200 dark:border-l-blue-800' : ''}
    hover:shadow-md transition-shadow duration-200 cursor-pointer
  `;

  return (
    <div className={cardClasses} onClick={handlePostClick}>
      {/* Header */}
      <div className="flex items-start justify-between mb-3">
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 rounded-full bg-gray-300 dark:bg-slate-600 flex items-center justify-center overflow-hidden">
            {thread.userPicture ? (
              <img
                src={thread.userPicture}
                alt={thread.userName}
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
              className="text-sm font-semibold text-gray-600 dark:text-gray-300 w-full h-full flex items-center justify-center"
              style={{ display: thread.userPicture ? 'none' : 'flex' }}
            >
              {thread.userName?.charAt(0)?.toUpperCase() || 'U'}
            </span>
          </div>
          <div>
            <div className="flex items-center space-x-1">
              <span className="font-semibold text-gray-900 dark:text-white">
                {thread.userName || 'Unknown User'}
              </span>
              {thread.isUserVerified && (
                <IonIcon icon={happy} className="text-blue-500 text-sm" />
              )}
            </div>
            <span className="text-gray-500 dark:text-gray-400 text-sm">
              @{thread.userEmail ? thread.userEmail.split('@')[0] : 'user'} · {formatTimeAgo(thread.createdAt)}
            </span>
          </div>
        </div>
        
        {currentUser?.id === thread.userId && (
          <button
            onClick={handleDelete}
            className="text-gray-400 hover:text-red-500 transition-colors"
            title="Delete thread"
          >
            <IonIcon icon={closeOutline} className="text-lg" />
          </button>
        )}
      </div>

      {/* Quoted Thread */}
      {thread.quotedThread && (
        <div 
          className="border border-gray-200 dark:border-slate-600 rounded-lg p-3 mb-3 bg-gray-50 dark:bg-slate-800 cursor-pointer hover:bg-gray-100 dark:hover:bg-slate-700 transition-colors"
          onClick={() => thread.quotedThread && navigate(`/thread/${thread.quotedThread.id}`)}
          title="View original post"
        >
          <div className="flex items-center gap-2 mb-2">
            <div className="w-6 h-6 rounded-full bg-gray-300 dark:bg-slate-600 flex items-center justify-center overflow-hidden">
              {thread.quotedThread.userPicture ? (
                <img
                  src={thread.quotedThread.userPicture}
                  alt={thread.quotedThread.userName}
                  className="w-full h-full object-cover"
                />
              ) : (
                <span className="text-xs font-semibold text-gray-600 dark:text-gray-300">
                  {thread.quotedThread.userName?.charAt(0)?.toUpperCase() || 'U'}
                </span>
              )}
            </div>
            <span className="font-semibold text-sm text-gray-900 dark:text-white">
              {thread.quotedThread.userName || 'Unknown User'}
            </span>
            <span className="text-gray-500 dark:text-gray-400 text-xs">
              @{thread.quotedThread.userEmail ? thread.quotedThread.userEmail.split('@')[0] : 'user'}
            </span>
            <span className="text-gray-400 dark:text-gray-500 text-xs ml-auto">
              Click to view original
            </span>
          </div>
          <p className="text-gray-800 dark:text-gray-200 text-sm whitespace-pre-wrap break-words">
            {renderContent(thread.quotedThread.content)}
          </p>
          {thread.quotedThread.media && thread.quotedThread.media.length > 0 && (
            <div className="mt-2 flex items-center gap-1 text-xs text-gray-500 dark:text-gray-400">
              {getMediaIcon()}
              <span>Media attached</span>
            </div>
          )}
        </div>
      )}

      {/* Content */}
      <div className="mb-3">
        <p className="text-gray-900 dark:text-white whitespace-pre-wrap break-words">
          {renderContent(thread.content)}
        </p>
      </div>

      {/* Media */}
      {thread.media && thread.media.length > 0 && (
        <div className="mb-3">
          <div className={`grid gap-1 ${
            thread.media.length === 1 ? 'grid-cols-1' :
            thread.media.length === 2 ? 'grid-cols-2' :
            thread.media.length === 3 ? 'grid-cols-3' :
            thread.media.length === 4 ? 'grid-cols-2' :
            'grid-cols-3'
          }`}>
            {thread.media.map((media, index) => (
              <div key={media.id} className="relative">
                {media.mediaType?.toLowerCase() === 'image' ? (
                  <img
                    src={media.mediaUrl.startsWith('http') ? media.mediaUrl : `http://localhost:8080/${media.mediaUrl}`}
                    alt={media.altText || 'Thread media'}
                    className={`w-full rounded-lg object-cover ${
                      thread.media && thread.media.length === 1 ? 'max-h-96' : 'aspect-square'
                    }`}
                    onError={(e) => {
                      console.error('Thread image failed to load:', media.mediaUrl);
                      (e.target as HTMLImageElement).style.display = 'none';
                    }}
                  />
                ) : media.mediaType?.toLowerCase() === 'video' ? (
                  <video
                    src={media.mediaUrl.startsWith('http') ? media.mediaUrl : `http://localhost:8080/${media.mediaUrl}`}
                    controls
                    className={`w-full rounded-lg ${
                      thread.media && thread.media.length === 1 ? 'max-h-96' : 'aspect-square'
                    }`}
                  />
                ) : (
                  <div className="flex items-center justify-center p-3 bg-gray-100 dark:bg-slate-700 rounded-lg aspect-square">
                    <div className="text-center">
                      {getMediaIcon()}
                      <span className="text-xs text-gray-600 dark:text-gray-300 block mt-1">
                        {media.mediaType.toUpperCase()}
                      </span>
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Poll */}
      {thread.poll && (
        <div className="mb-3 p-3 bg-gray-50 dark:bg-slate-700 rounded-lg">
          <h4 className="font-semibold text-gray-900 dark:text-white mb-2">
            {thread.poll.question}
          </h4>
          <div className="space-y-2">
            {thread.poll.options.map((option) => (
              <div key={option.id} className="flex items-center justify-between">
                <span className="text-sm text-gray-700 dark:text-gray-300">
                  {option.optionText}
                </span>
                <span className="text-xs text-gray-500 dark:text-gray-400">
                  {option.votesCount} votes
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Actions */}
      <div className="flex items-center justify-between pt-3 border-t border-gray-200 dark:border-slate-700">
        <div className="flex items-center space-x-6">
          {/* Like */}
          <button
            onClick={handleLike}
            className={`flex items-center space-x-1 text-gray-500 hover:text-red-500 transition-colors ${
              thread.isLiked ? 'text-red-500' : ''
            }`}
          >
            <IonIcon icon={thread.isLiked ? heart : heartOutline} className="text-lg" />
            <span className="text-sm">{thread.likesCount}</span>
          </button>

          {/* Comment */}
          {showCommentButton && (
            <button
              onClick={handleReply}
              className="flex items-center space-x-1 text-gray-500 hover:text-blue-500 transition-colors"
            >
              <IonIcon icon={chatbubbleEllipsesOutline} className="text-lg" />
              <span className="text-sm">{thread.repliesCount}</span>
            </button>
          )}

          {/* Repost */}
          <button
            onClick={handleRepost}
            className={`flex items-center space-x-1 text-gray-500 hover:text-green-500 transition-colors ${
              thread.isReposted ? 'text-green-500' : ''
            }`}
          >
            <IonIcon icon={repeatOutline} className="text-lg" />
            <span className="text-sm">{thread.repostsCount}</span>
          </button>

          {/* Share */}
          <button className="flex items-center space-x-1 text-gray-500 hover:text-blue-500 transition-colors">
            <IonIcon icon={shareOutline} className="text-lg" />
          </button>

          {/* Bookmark */}
          {onBookmark && (
            <button
              onClick={handleBookmark}
              className={`flex items-center space-x-1 text-gray-500 hover:text-yellow-500 transition-colors ${
                thread.isBookmarked ? 'text-yellow-500' : ''
              }`}
            >
              <IonIcon icon={thread.isBookmarked ? bookmark : bookmarkOutline} className="text-lg" />
            </button>
          )}

          {/* Reaction Picker */}
          {onReaction && (
            <div className="relative">
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  setShowReactionPicker(!showReactionPicker);
                }}
                className="flex items-center space-x-1 text-gray-500 hover:text-purple-500 transition-colors"
              >
                <IonIcon icon={happy} className="text-lg" />
              </button>
              
              {showReactionPicker && (
                <div className="absolute bottom-full left-0 mb-2 flex space-x-1 bg-white dark:bg-slate-800 rounded-full shadow-lg border border-gray-200 dark:border-slate-700 p-1">
                  {[
                    { type: 'like', icon: heart, color: 'text-red-500' },
                    { type: 'love', icon: heart, color: 'text-pink-500' },
                    { type: 'laugh', icon: happy, color: 'text-yellow-500' },
                    { type: 'angry', icon: flame, color: 'text-red-600' },
                    { type: 'sad', icon: sad, color: 'text-blue-500' },
                    { type: 'wow', icon: thumbsUp, color: 'text-purple-500' }
                  ].map((reaction) => (
                    <button
                      key={reaction.type}
                      onClick={(e) => {
                        e.stopPropagation();
                        handleReaction(reaction.type);
                      }}
                      className={`p-1 rounded-full hover:bg-gray-100 dark:hover:bg-slate-700 ${reaction.color}`}
                    >
                      <IonIcon icon={reaction.icon} className="text-sm" />
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Thread Level Indicator */}
        {thread.threadLevel > 0 && (
          <div className="text-xs text-gray-400">
            Level {thread.threadLevel}
          </div>
        )}
      </div>
    </div>
  );
};

export default ThreadCard;
