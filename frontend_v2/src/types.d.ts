declare namespace JSX {
  interface IntrinsicElements {
    'ion-icon': {
      name?: string;
      ios?: string;
      md?: string;
      class?: string;
      className?: string;
      style?: React.CSSProperties;
      onClick?: React.MouseEventHandler<HTMLElement>;
      [key: string]: any;
    };
  }
}

// Thread Types
export interface Thread {
  id: number;
  content: string;
  threadType: 'original' | 'reply' | 'quote' | 'retweet';
  
  // User information
  userId: string;
  userName: string;
  userEmail: string;
  userPicture?: string;
  isUserVerified?: boolean;
  
  // Thread hierarchy
  parentThreadId?: number;
  rootThreadId?: number;
  threadLevel: number;
  threadPath?: string;
  
  // Quote/Retweet specific fields
  quotedThreadId?: number;
  quoteContent?: string;
  quotedThread?: Thread; // Full quoted thread data
  
  // Engagement counts
  likesCount: number;
  repliesCount: number;
  repostsCount: number;
  sharesCount: number;
  viewsCount: number;
  bookmarksCount: number;
  
  // Thread status
  isDeleted: boolean;
  isEdited: boolean;
  isPinned: boolean;
  isSensitive: boolean;
  isPublic: boolean;
  
  // Reply permissions
  replyPermission: 'ANYONE' | 'FOLLOWERS' | 'FOLLOWING' | 'MENTIONED_ONLY';
  
  // Timestamps
  createdAt: string;
  updatedAt: string;
  editedAt?: string;
  
  // Media attachments
  media?: ThreadMedia[];
  
  // User engagement status
  isLiked?: boolean;
  isReposted?: boolean;
  isBookmarked?: boolean;
  userReaction?: 'like' | 'love' | 'laugh' | 'angry' | 'sad' | 'wow';
  
  // Hashtags and mentions
  hashtags?: string[];
  mentions?: string[];
  
  // Replies (for thread view)
  replies?: Thread[];
  
  // Poll information
  poll?: ThreadPoll;
}

export interface ThreadMedia {
  id: number;
  threadId: number;
  mediaType: 'image' | 'video' | 'gif' | 'audio' | 'document';
  mediaUrl: string;
  thumbnailUrl?: string;
  altText?: string;
  fileSize?: number;
  duration?: number; // for video/audio in seconds
  width?: number;
  height?: number;
  displayOrder: number;
  createdAt: string;
}

export interface ThreadPoll {
  id: number;
  threadId: number;
  question: string;
  isMultipleChoice: boolean;
  expiresAt?: string;
  totalVotes: number;
  createdAt: string;
  options: PollOption[];
  hasUserVoted?: boolean;
  userVotedOptionId?: number;
}

export interface PollOption {
  id: number;
  pollId: number;
  optionText: string;
  votesCount: number;
  displayOrder: number;
  createdAt: string;
  percentage?: number; // Calculated percentage of votes
}

// Legacy Post interface for backward compatibility
export interface Post {
  id: number;
  content: string;
  mediaUrl?: string;
  mediaType?: string;
  timestamp: string;
  userId: string;
  userName: string;
  userEmail: string;
  userPicture?: string;
  isLiked: boolean;
  likes: number;
  replies: number;
  reposts: number;
  retweets: number;
  isReposted: boolean;
  parentPostId?: number;
  rootPostId?: number;
  threadLevel?: number;
  threadPath?: string;
  quotedPostId?: number;
  isQuoteRepost?: boolean;
  quotedPost?: Post;
  nestedReplies?: Post[];
}

export interface User {
  id: string;
  name: string;
  email: string;
  picture?: string;
  bio?: string;
  location?: string;
  website?: string;
  isVerified?: boolean;
  isPrivate?: boolean;
  isActive?: boolean;
  followersCount?: number;
  followingCount?: number;
  threadsCount?: number;
  createdAt?: string;
  updatedAt?: string;
  lastSeenAt?: string;
}

export interface Message {
  id: number;
  senderId: string;
  receiverId: string;
  content: string;
  messageType: 'text' | 'image' | 'video' | 'file' | 'thread_share';
  threadId?: number;
  isEdited: boolean;
  isDeleted: boolean;
  createdAt: string;
  updatedAt: string;
  sender?: User;
  receiver?: User;
  thread?: Thread;
}

export interface Conversation {
  id: number;
  type: 'direct' | 'group';
  name?: string;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
  participants: ConversationParticipant[];
  lastMessage?: Message;
  unreadCount?: number;
}

export interface ConversationParticipant {
  id: number;
  conversationId: number;
  userId: string;
  joinedAt: string;
  leftAt?: string;
  isActive: boolean;
  user?: User;
}

export interface Notification {
  id: number;
  userId: string;
  type: 'like' | 'repost' | 'reply' | 'mention' | 'follow' | 'message' | 'system';
  title: string;
  message: string;
  isRead: boolean;
  relatedUserId?: string;
  relatedThreadId?: number;
  relatedMessageId?: number;
  createdAt: string;
  relatedUser?: User;
  relatedThread?: Thread;
  relatedMessage?: Message;
}

export interface Event {
  id: number;
  title: string;
  description: string;
  location: string;
  startDate: string;
  endDate: string;
  category: string;
  isFree: boolean;
  price?: number;
  maxAttendees?: number;
  currentAttendees: number;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
  creator?: User;
  isAttending?: boolean;
  attendees?: User[];
}

