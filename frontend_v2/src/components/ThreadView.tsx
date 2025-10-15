import React, { useState } from 'react';
import { IonIcon } from '@ionic/react';
import { 
  image, 
  videocam,
  chatbubbleOutline, 
  closeOutline, 
  checkmarkOutline,
  timeOutline
} from 'ionicons/icons';
import ThreadReply from './ThreadReply';
import { Thread, User } from '../types.d';
import { generateInitials, getInitialsBackgroundColor } from '../utils/profileUtils';
import { getProfileImageUrl } from '../utils/mediaUtils';

interface ThreadViewProps {
  mainPost: Thread;
  replies: Thread[];
  currentUser?: User | null;
  onLike: (postId: number) => Promise<void>;
  onRepost: (postId: number) => Promise<void>;
  onDelete: (postId: number) => Promise<void>;
  onReply: (parentPostId: number, content: string, mediaFile?: File) => Promise<void>;
}

const ThreadView: React.FC<ThreadViewProps> = ({
  mainPost,
  replies,
  currentUser,
  onLike,
  onRepost,
  onDelete,
  onReply
}) => {
  const [showReplyInput, setShowReplyInput] = useState(false);
  const [replyContent, setReplyContent] = useState('');
  const [selectedMedia, setSelectedMedia] = useState<File | null>(null);
  const [postingReply, setPostingReply] = useState(false);

  // Organize replies into a hierarchical structure
  const organizeReplies = (replies: Thread[]): Thread[] => {
    // Sort replies by thread path to maintain proper order
    const sortedReplies = [...replies].sort((a, b) => {
      if (a.threadPath && b.threadPath) {
        return a.threadPath.localeCompare(b.threadPath);
      }
      return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
    });

    return sortedReplies;
  };

  const handleReplySubmit = async (parentPostId?: number) => {
    if ((!replyContent.trim() && !selectedMedia) || postingReply) return;

    setPostingReply(true);
    try {
      // Use the provided parentPostId or default to mainPost.id
      const targetPostId = parentPostId || mainPost.id;
      await onReply(targetPostId, replyContent, selectedMedia || undefined);
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

  const handleReplySubmitClick = () => {
    handleReplySubmit();
  };

  const handleMediaSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedMedia(file);
    }
  };

  const renderReplies = (replies: Thread[]): React.ReactNode => {
    // Show direct replies to the current thread (where parentThreadId = mainPost.id)
    // This creates a Twitter/X-like experience where you see the main thread
    // and its direct replies, then click on replies to see their sub-replies
    const directReplies = replies.filter(reply => 
      reply.id !== mainPost.id && reply.parentThreadId === mainPost.id
    );
    const organizedReplies = organizeReplies(directReplies);
    
    return organizedReplies.map(reply => (
      <ThreadReply
        key={reply.id}
        post={reply}
        currentUser={currentUser}
        onLike={onLike}
        onRepost={onRepost}
        onDelete={onDelete}
        onReply={onReply}
        level={0} // Start at level 0
        maxLevel={0} // Don't show nested replies in this view
        isMainThread={false} // This is a reply, not a main thread
      />
    ));
  };

  return (
    <div className="max-w-2xl mx-auto">
      {/* Breadcrumb Navigation */}
      {mainPost.threadLevel > 0 && (
        <div className="mb-4 px-4 py-3 bg-gray-50 dark:bg-slate-800 rounded-lg border border-gray-200 dark:border-slate-700">
          <div className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
            <span className="font-medium">Thread</span>
            <span>•</span>
            <span>Reply to</span>
            <span className="font-semibold text-gray-800 dark:text-gray-200">@{mainPost.userName}</span>
            {mainPost.hashtags && mainPost.hashtags.length > 0 && (
              <>
                <span>•</span>
                <div className="flex items-center gap-1">
                  {mainPost.hashtags.slice(0, 2).map((hashtag, index) => (
                    <span key={index} className="text-blue-600 dark:text-blue-400 font-medium">
                      #{hashtag}
                    </span>
                  ))}
                  {mainPost.hashtags.length > 2 && (
                    <span className="text-gray-500">+{mainPost.hashtags.length - 2}</span>
                  )}
                </div>
              </>
            )}
          </div>
        </div>
      )}

      <div className="space-y-0">
        {/* Main Post */}
        <div className="relative">
          <ThreadReply
            post={mainPost}
            currentUser={currentUser}
            onLike={onLike}
            onRepost={onRepost}
            onDelete={onDelete}
            onReply={onReply}
            level={0}
            maxLevel={0}
            isMainThread={true}
          />
        </div>

        {/* Main Reply Input */}
        {!showReplyInput && (
          <div className="bg-white dark:bg-dark2 border-t border-gray-200 dark:border-slate-700 p-4">
            <div className="flex items-center gap-3">
              {currentUser?.picture ? (
                <img 
                  src={getProfileImageUrl(currentUser.picture)} 
                  alt={currentUser.name || "User"} 
                  className="w-10 h-10 rounded-full border-2 border-gray-200 dark:border-slate-600 object-cover" 
                />
              ) : (
                <div className="w-10 h-10 rounded-full bg-gray-100 dark:bg-slate-600 flex items-center justify-center">
                  <div className={`w-8 h-8 rounded-full ${getInitialsBackgroundColor(currentUser?.name || '')} flex items-center justify-center`}>
                    <span className="text-white text-sm font-semibold">
                      {generateInitials(currentUser?.name || 'உ')}
                    </span>
                  </div>
                </div>
              )}
              <div className="flex-1">
                <button
                  onClick={() => setShowReplyInput(true)}
                  className="w-full text-left px-4 py-3 bg-gray-50 dark:bg-slate-800 rounded-2xl text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-slate-700 transition-colors border border-gray-200 dark:border-slate-600 text-sm"
                >
                  Reply to {mainPost.userName}...
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Reply Input Form */}
        {showReplyInput && (
          <div className="bg-white dark:bg-dark2 border-t border-gray-200 dark:border-slate-700 p-4">
            <div className="flex items-start gap-3">
              {currentUser?.picture ? (
                <img 
                  src={getProfileImageUrl(currentUser.picture)} 
                  alt={currentUser.name || "User"} 
                  className="w-10 h-10 rounded-full border-2 border-gray-200 dark:border-slate-600 object-cover" 
                />
              ) : (
                <div className="w-10 h-10 rounded-full bg-gray-100 dark:bg-slate-600 flex items-center justify-center">
                  <div className={`w-8 h-8 rounded-full ${getInitialsBackgroundColor(currentUser?.name || '')} flex items-center justify-center`}>
                    <span className="text-white text-sm font-semibold">
                      {generateInitials(currentUser?.name || 'உ')}
                    </span>
                  </div>
                </div>
              )}
              <div className="flex-1">
                <textarea
                  value={replyContent}
                  onChange={(e) => setReplyContent(e.target.value)}
                  placeholder={`Reply to ${mainPost.userName}...`}
                  className="w-full bg-transparent border-none outline-none focus:ring-0 focus:border-transparent text-gray-700 dark:text-gray-300 placeholder-gray-500 dark:placeholder-gray-400 resize-none text-sm"
                  rows={3}
                  maxLength={2000}
                />
              
              <div className="flex items-center justify-between mt-2">
                <div className="flex items-center gap-2">
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleMediaSelect}
                    className="hidden"
                    id="main-reply-image-upload"
                  />
                  <label
                    htmlFor="main-reply-image-upload"
                    className="flex items-center gap-1.5 bg-blue-50 text-blue-600 rounded-full py-0.5 px-1.5 border border-blue-200 dark:bg-blue-950 dark:border-blue-800 hover:bg-blue-100 dark:hover:bg-blue-900 transition-colors cursor-pointer text-xs"
                  >
                    <IonIcon 
                      icon={selectedMedia ? checkmarkOutline : image} 
                      className="text-sm" 
                    />
                  </label>
                  
                  <input
                    type="file"
                    accept="video/*"
                    onChange={handleMediaSelect}
                    className="hidden"
                    id="main-reply-video-upload"
                  />
                  <label
                    htmlFor="main-reply-video-upload"
                    className="flex items-center gap-1.5 bg-purple-50 text-purple-600 rounded-full py-0.5 px-1.5 border border-purple-200 dark:bg-purple-950 dark:border-purple-800 hover:bg-purple-100 dark:hover:bg-purple-900 transition-colors cursor-pointer text-xs"
                  >
                    <IonIcon 
                      icon={videocam} 
                      className="text-sm" 
                    />
                  </label>
                </div>
                
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => setShowReplyInput(false)}
                    className="px-3 py-1.5 text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-slate-700 rounded-full transition-colors text-xs"
                  >
                    <IonIcon icon={closeOutline} className="text-sm" />
                  </button>
                  <button
                    onClick={handleReplySubmitClick}
                    disabled={(!replyContent.trim() && !selectedMedia) || postingReply}
                    className="bg-blue-500 text-white px-4 py-1.5 rounded-full text-xs font-medium hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
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

        {/* Replies Section */}
        {replies.length > 0 && (
          <div className="border-t border-gray-200 dark:border-slate-700">
            {/* Replies Header */}
            <div className="px-6 py-4 bg-gray-50 dark:bg-slate-800 border-b border-gray-200 dark:border-slate-700">
              <h2 className="text-lg font-semibold text-gray-800 dark:text-gray-200 flex items-center gap-2">
                <span className="w-2 h-2 bg-blue-500 rounded-full"></span>
                {replies.length} {replies.length === 1 ? 'Reply' : 'Replies'}
              </h2>
            </div>
            
            {/* Replies List */}
            <div className="divide-y divide-gray-100 dark:divide-slate-700">
              {renderReplies(replies)}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ThreadView;
