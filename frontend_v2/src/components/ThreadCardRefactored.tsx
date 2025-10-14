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
  happy,
  sad,
  flame,
  thumbsUp
} from 'ionicons/icons';
import { Thread, User } from '../types.d';
import { useThreadActions } from '../hooks';
import { 
  Avatar, 
  Button, 
  Card, 
  ContentRenderer, 
  MediaGrid, 
  TimeAgo 
} from './ui';
import { ROUTES, DYNAMIC_ROUTES } from '../constants';

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
  const { isActionLoading } = useThreadActions();

  const handlePostClick = () => {
    navigate(DYNAMIC_ROUTES.THREAD_BY_ID(thread.id));
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
      navigate(DYNAMIC_ROUTES.THREAD_BY_ID(thread.id));
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

  const handleUserClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    navigate(DYNAMIC_ROUTES.PROFILE_BY_USERNAME(thread.userEmail?.split('@')[0] || 'user'));
  };

  const handleQuotedThreadClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (thread.quotedThread) {
      navigate(DYNAMIC_ROUTES.THREAD_BY_ID(thread.quotedThread.id));
    }
  };

  const isLikeLoading = isActionLoading(`like-${thread.id}`);
  const isRepostLoading = isActionLoading(`repost-${thread.id}`);

  return (
    <Card
      className={`${compact ? 'p-3' : 'p-4'} ${
        isReply ? 'ml-4 border-l-4 border-l-blue-200 dark:border-l-blue-800' : ''
      } hover:shadow-md transition-shadow duration-200 cursor-pointer`}
      onClick={handlePostClick}
      hover
    >
      {/* Header */}
      <div className="flex items-start justify-between mb-3">
        <div className="flex items-center space-x-3">
          <Avatar
            src={thread.userPicture}
            name={thread.userName || 'Unknown User'}
            size={compact ? 'sm' : 'md'}
            onClick={handleUserClick}
          />
          <div>
            <div className="flex items-center space-x-1">
              <button
                onClick={handleUserClick}
                className="font-semibold text-gray-900 dark:text-white hover:underline"
              >
                {thread.userName || 'Unknown User'}
              </button>
              {thread.isUserVerified && (
                <IonIcon icon={happy} className="text-blue-500 text-sm" />
              )}
            </div>
            <div className="flex items-center space-x-1">
              <span className="text-gray-500 dark:text-gray-400 text-sm">
                @{thread.userEmail ? thread.userEmail.split('@')[0] : 'user'}
              </span>
              <span className="text-gray-400 dark:text-gray-500">·</span>
              <TimeAgo timestamp={thread.createdAt} />
            </div>
          </div>
        </div>
        
        {currentUser?.id === thread.userId && (
          <Button
            variant="ghost"
            size="sm"
            onClick={handleDelete}
            className="text-gray-400 hover:text-red-500 transition-colors p-1"
            title="Delete thread"
          >
            <IonIcon icon={closeOutline} className="text-lg" />
          </Button>
        )}
      </div>

      {/* Quoted Thread */}
      {thread.quotedThread && (
        <Card
          className="p-3 mb-3 bg-gray-50 dark:bg-slate-800 cursor-pointer hover:bg-gray-100 dark:hover:bg-slate-700 transition-colors"
          onClick={handleQuotedThreadClick}
          title="View original post"
        >
          <div className="flex items-center gap-2 mb-2">
            <Avatar
              src={thread.quotedThread.userPicture}
              name={thread.quotedThread.userName || 'Unknown User'}
              size="xs"
            />
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
          <ContentRenderer 
            content={thread.quotedThread.content}
            className="text-gray-800 dark:text-gray-200 text-sm"
          />
          {thread.quotedThread.media && thread.quotedThread.media.length > 0 && (
            <div className="mt-2 flex items-center gap-1 text-xs text-gray-500 dark:text-gray-400">
              <IonIcon icon={heart} className="text-blue-500" />
              <span>Media attached</span>
            </div>
          )}
        </Card>
      )}

      {/* Content */}
      <div className="mb-3">
        <ContentRenderer 
          content={thread.content}
          className="text-gray-900 dark:text-white"
        />
      </div>

      {/* Media */}
      {thread.media && thread.media.length > 0 && (
        <div className="mb-3">
          <MediaGrid media={thread.media} />
        </div>
      )}

      {/* Poll */}
      {thread.poll && (
        <Card className="p-3 mb-3 bg-gray-50 dark:bg-slate-700">
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
        </Card>
      )}

      {/* Actions */}
      <div className="flex items-center justify-between pt-3 border-t border-gray-200 dark:border-slate-700">
        <div className="flex items-center space-x-6">
          {/* Like */}
          <Button
            variant="ghost"
            size="sm"
            onClick={handleLike}
            disabled={isLikeLoading}
            className={`flex items-center space-x-1 ${
              thread.isLiked ? 'text-red-500' : 'text-gray-500 hover:text-red-500'
            }`}
          >
            <IonIcon 
              icon={thread.isLiked ? heart : heartOutline} 
              className="text-lg" 
            />
            <span className="text-sm">{thread.likesCount}</span>
          </Button>

          {/* Comment */}
          {showCommentButton && (
            <Button
              variant="ghost"
              size="sm"
              onClick={handleReply}
              className="flex items-center space-x-1 text-gray-500 hover:text-blue-500"
            >
              <IonIcon icon={chatbubbleEllipsesOutline} className="text-lg" />
              <span className="text-sm">{thread.repliesCount}</span>
            </Button>
          )}

          {/* Repost */}
          <Button
            variant="ghost"
            size="sm"
            onClick={handleRepost}
            disabled={isRepostLoading}
            className={`flex items-center space-x-1 ${
              thread.isReposted ? 'text-green-500' : 'text-gray-500 hover:text-green-500'
            }`}
          >
            <IonIcon icon={repeatOutline} className="text-lg" />
            <span className="text-sm">{thread.repostsCount}</span>
          </Button>

          {/* Share */}
          <Button
            variant="ghost"
            size="sm"
            className="flex items-center space-x-1 text-gray-500 hover:text-blue-500"
          >
            <IonIcon icon={shareOutline} className="text-lg" />
          </Button>

          {/* Bookmark */}
          {onBookmark && (
            <Button
              variant="ghost"
              size="sm"
              onClick={handleBookmark}
              className={`flex items-center space-x-1 ${
                thread.isBookmarked ? 'text-yellow-500' : 'text-gray-500 hover:text-yellow-500'
              }`}
            >
              <IonIcon 
                icon={thread.isBookmarked ? bookmark : bookmarkOutline} 
                className="text-lg" 
              />
            </Button>
          )}

          {/* Reaction Picker */}
          {onReaction && (
            <div className="relative">
              <Button
                variant="ghost"
                size="sm"
                onClick={(e) => {
                  e.stopPropagation();
                  setShowReactionPicker(!showReactionPicker);
                }}
                className="flex items-center space-x-1 text-gray-500 hover:text-purple-500"
              >
                <IonIcon icon={happy} className="text-lg" />
              </Button>
              
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
                    <Button
                      key={reaction.type}
                      variant="ghost"
                      size="sm"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleReaction(reaction.type);
                      }}
                      className={`p-1 rounded-full hover:bg-gray-100 dark:hover:bg-slate-700 ${reaction.color}`}
                    >
                      <IonIcon icon={reaction.icon} className="text-sm" />
                    </Button>
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
    </Card>
  );
};

export default ThreadCard;
