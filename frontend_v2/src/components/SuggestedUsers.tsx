import React, { useState, useEffect } from 'react';
import { IonIcon } from '@ionic/react';
import { personAdd, person } from 'ionicons/icons';
import { generateInitials, getInitialsBackgroundColor } from '../utils/profileUtils';
import { getProfileImageUrl } from '../utils/mediaUtils';
import { useNotification } from '../contexts/NotificationContext';
import { usersApi } from '../services/api';

interface SuggestedUser {
  id: string;
  name: string;
  username: string;
  picture?: string;
  bio?: string;
  followersCount: number;
  isFollowing: boolean;
  followRequestStatus?: 'PENDING' | 'APPROVED' | 'REJECTED';
}

interface SuggestedUsersProps {
  currentUser?: {
    id: string;
    name: string;
    email: string;
    picture?: string;
  } | null;
}

const SuggestedUsers: React.FC<SuggestedUsersProps> = ({ currentUser }) => {
  const [suggestedUsers, setSuggestedUsers] = useState<SuggestedUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [following, setFollowing] = useState<Set<string>>(new Set());
  const [pendingRequests, setPendingRequests] = useState<Set<string>>(new Set());
  const { showSuccess, showError } = useNotification();

  useEffect(() => {
    if (currentUser) {
      loadSuggestedUsers();
    }
  }, [currentUser]);

  const loadSuggestedUsers = async () => {
    try {
      setLoading(true);
      const response = await usersApi.getPeopleYouMayKnow(5);
      setSuggestedUsers(response);
    } catch (error) {
      console.error('Error loading suggested users:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleFollow = async (userId: string) => {
    try {
      await usersApi.sendFollowRequest(userId);
      setPendingRequests(prev => new Set(prev).add(userId));
      setSuggestedUsers(prev => 
        prev.map(user => 
          user.id === userId 
            ? { ...user, followRequestStatus: 'PENDING' }
            : user
        )
      );
      showSuccess('Success', 'Follow request sent!');
    } catch (error) {
      console.error('Error sending follow request:', error);
      showError('Error', 'Failed to send follow request. Please try again.');
    }
  };

  const handleUnfollow = async (userId: string) => {
    try {
      await usersApi.unfollowUser(userId);
      setFollowing(prev => {
        const newSet = new Set(prev);
        newSet.delete(userId);
        return newSet;
      });
      setSuggestedUsers(prev => 
        prev.map(user => 
          user.id === userId 
            ? { ...user, isFollowing: false, followersCount: user.followersCount - 1 }
            : user
        )
      );
      showSuccess('Success', 'You have unfollowed this user.');
    } catch (error) {
      console.error('Error unfollowing user:', error);
      showError('Error', 'Failed to unfollow user. Please try again.');
    }
  };

  if (loading) {
    return (
      <div className="bg-white dark:bg-slate-800 rounded-lg shadow-md p-4">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Suggested for you</h3>
        <div className="space-y-3">
          {[...Array(3)].map((_, index) => (
            <div key={index} className="flex items-center space-x-3 animate-pulse">
              <div className="w-10 h-10 bg-gray-200 dark:bg-slate-600 rounded-full"></div>
              <div className="flex-1">
                <div className="h-4 bg-gray-200 dark:bg-slate-600 rounded w-3/4 mb-2"></div>
                <div className="h-3 bg-gray-200 dark:bg-slate-600 rounded w-1/2"></div>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  if (suggestedUsers.length === 0) {
    return (
      <div className="bg-white dark:bg-slate-800 rounded-lg shadow-md p-4">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Suggested for you</h3>
        <p className="text-gray-500 dark:text-gray-400 text-sm">No suggestions available at the moment.</p>
      </div>
    );
  }

  return (
    <div className="bg-white dark:bg-slate-800 rounded-lg shadow-md p-4">
      <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Suggested for you</h3>
      <div className="space-y-4">
        {suggestedUsers.map((user) => (
          <div key={user.id} className="flex items-center space-x-3">
            {/* User Avatar */}
            <div className="w-10 h-10 rounded-full overflow-hidden flex-shrink-0">
              {user.picture ? (
                <img 
                  src={getProfileImageUrl(user.picture)} 
                  alt={user.name}
                  className="w-full h-full object-cover"
                />
              ) : (
                <div className={`w-full h-full ${getInitialsBackgroundColor(user.name)} flex items-center justify-center`}>
                  <span className="text-white text-sm font-semibold">
                    {generateInitials(user.name)}
                  </span>
                </div>
              )}
            </div>

            {/* User Info */}
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-gray-900 dark:text-white truncate">
                {user.name}
              </p>
              <p className="text-xs text-gray-500 dark:text-gray-400 truncate">
                @{user.username}
              </p>
              {user.bio && (
                <p className="text-xs text-gray-600 dark:text-gray-300 truncate mt-1">
                  {user.bio}
                </p>
              )}
              <p className="text-xs text-gray-500 dark:text-gray-400">
                {user.followersCount} followers
              </p>
            </div>

            {/* Follow Button */}
            <div className="flex-shrink-0">
              {user.isFollowing || following.has(user.id) ? (
                <button
                  onClick={() => handleUnfollow(user.id)}
                  className="flex items-center gap-1 px-3 py-1.5 text-xs bg-gray-200 dark:bg-slate-600 text-gray-700 dark:text-gray-300 rounded-full hover:bg-gray-300 dark:hover:bg-slate-500 transition-colors"
                >
                  <IonIcon icon={person} className="text-sm" />
                  Following
                </button>
              ) : user.followRequestStatus === 'PENDING' || pendingRequests.has(user.id) ? (
                <button
                  disabled
                  className="flex items-center gap-1 px-3 py-1.5 text-xs bg-yellow-100 dark:bg-yellow-900 text-yellow-700 dark:text-yellow-300 rounded-full cursor-not-allowed"
                >
                  <IonIcon icon={personAdd} className="text-sm" />
                  Pending
                </button>
              ) : (
                <button
                  onClick={() => handleFollow(user.id)}
                  className="flex items-center gap-1 px-3 py-1.5 text-xs bg-blue-600 text-white rounded-full hover:bg-blue-700 transition-colors"
                >
                  <IonIcon icon={personAdd} className="text-sm" />
                  Follow
                </button>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default SuggestedUsers;
