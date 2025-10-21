import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { usersApi } from '../services/api';
import { User } from '../types.d';
import { useAuth } from '../hooks';
import Sidebar from '../components/Sidebar';
import { ROUTES } from '../constants';
import { IonIcon } from '@ionic/react';
import { person, personAdd, personRemove, arrowBack } from 'ionicons/icons';
import { useNotification } from '../contexts/NotificationContext';

const FollowingPage: React.FC = () => {
  const navigate = useNavigate();
  const { userId } = useParams<{ userId: string }>();
  const { currentUser, loading: authLoading } = useAuth();
  const { showSuccess, showError } = useNotification();
  
  const [profileUser, setProfileUser] = useState<User | null>(null);
  const [following, setFollowing] = useState<User[]>([]);
  const [followers, setFollowers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'following' | 'followers'>('following');
  const [processing, setProcessing] = useState<Set<string>>(new Set());

  useEffect(() => {
    if (!authLoading && !currentUser) {
      navigate(ROUTES.LOGIN);
      return;
    }

    if (currentUser && userId) {
      loadUserData();
    }
  }, [currentUser, authLoading, navigate, userId]);

  const loadUserData = async () => {
    try {
      setLoading(true);
      
      // Load profile user data
      const userData = await usersApi.getProfile(userId!);
      setProfileUser(userData);
      
      // Load following and followers
      const [followingData, followersData] = await Promise.all([
        usersApi.getFollowing(userId!),
        usersApi.getFollowers(userId!)
      ]);
      
      setFollowing(followingData);
      setFollowers(followersData);
    } catch (error) {
      console.error('Error loading user data:', error);
      showError('Error', 'Failed to load user data. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleFollow = async (targetUserId: string) => {
    try {
      setProcessing(prev => new Set(prev).add(targetUserId));
      await usersApi.followUser(targetUserId);
      
      // Update the followers list if we're viewing followers
      if (activeTab === 'followers') {
        setFollowers(prev => 
          prev.map(user => 
            user.id === targetUserId 
              ? { ...user, isFollowing: true }
              : user
          )
        );
      }
      
      showSuccess('Success', 'You are now following this user!');
    } catch (error) {
      console.error('Error following user:', error);
      showError('Error', 'Failed to follow user. Please try again.');
    } finally {
      setProcessing(prev => {
        const newSet = new Set(prev);
        newSet.delete(targetUserId);
        return newSet;
      });
    }
  };

  const handleUnfollow = async (targetUserId: string) => {
    try {
      setProcessing(prev => new Set(prev).add(targetUserId));
      await usersApi.unfollowUser(targetUserId);
      
      // Update the lists
      if (activeTab === 'following') {
        setFollowing(prev => prev.filter(user => user.id !== targetUserId));
      } else if (activeTab === 'followers') {
        setFollowers(prev => 
          prev.map(user => 
            user.id === targetUserId 
              ? { ...user, isFollowing: false }
              : user
          )
        );
      }
      
      showSuccess('Success', 'You have unfollowed this user.');
    } catch (error) {
      console.error('Error unfollowing user:', error);
      showError('Error', 'Failed to unfollow user. Please try again.');
    } finally {
      setProcessing(prev => {
        const newSet = new Set(prev);
        newSet.delete(targetUserId);
        return newSet;
      });
    }
  };

  const renderUserCard = (user: User) => {
    const isProcessing = processing.has(user.id);
    const isCurrentUser = user.id === currentUser?.id;
    const canUnfollow = activeTab === 'following' && !isCurrentUser;
    const canFollow = activeTab === 'followers' && !isCurrentUser && !user.isFollowing;

    return (
      <div key={user.id} className="flex items-center justify-between p-4 bg-white dark:bg-slate-800 rounded-lg border border-gray-200 dark:border-slate-700">
        <div className="flex items-center gap-3">
          <div className="relative w-12 h-12 shrink-0">
            <img
              src={user.picture || '/default-avatar.png'}
              alt={user.name}
              className="object-cover w-full h-full rounded-full"
            />
            {user.isVerified && (
              <div className="absolute -bottom-1 -right-1 w-4 h-4 bg-blue-500 rounded-full flex items-center justify-center">
                <IonIcon icon={person} className="text-white text-xs" />
              </div>
            )}
          </div>
          
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2">
              <h3 className="text-sm font-semibold text-gray-900 dark:text-white truncate">
                {user.name}
              </h3>
              {user.isVerified && (
                <IonIcon icon={person} className="text-blue-500 text-sm" />
              )}
            </div>
            <p className="text-sm text-gray-500 dark:text-gray-400 truncate">
              @{user.username}
            </p>
          </div>
        </div>

        <div className="flex-shrink-0">
          {isCurrentUser ? (
            <span className="px-3 py-1.5 text-xs text-gray-500 dark:text-gray-400 bg-gray-100 dark:bg-slate-700 rounded-full">
              You
            </span>
          ) : canUnfollow ? (
            <button
              onClick={() => handleUnfollow(user.id)}
              disabled={isProcessing}
              className="flex items-center gap-1 px-3 py-1.5 text-xs bg-red-100 dark:bg-red-900 text-red-700 dark:text-red-300 rounded-full hover:bg-red-200 dark:hover:bg-red-800 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <IonIcon icon={personRemove} className="text-sm" />
              {isProcessing ? 'Unfollowing...' : 'Unfollow'}
            </button>
          ) : canFollow ? (
            <button
              onClick={() => handleFollow(user.id)}
              disabled={isProcessing}
              className="flex items-center gap-1 px-3 py-1.5 text-xs bg-blue-600 text-white rounded-full hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <IonIcon icon={personAdd} className="text-sm" />
              {isProcessing ? 'Following...' : 'Follow'}
            </button>
          ) : user.isFollowing ? (
            <span className="px-3 py-1.5 text-xs text-gray-500 dark:text-gray-400 bg-gray-100 dark:bg-slate-700 rounded-full">
              Following
            </span>
          ) : null}
        </div>
      </div>
    );
  };

  if (authLoading || loading) {
    return (
      <div className="min-h-screen bg-gray-100 dark:bg-slate-900 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!currentUser || !profileUser) {
    return null;
  }

  const currentList = activeTab === 'following' ? following : followers;
  const isOwnProfile = profileUser.id === currentUser.id;

  return (
    <div id="wrapper" className="bg-gray-100 dark:bg-slate-900">
      {/* Sidebar */}
      <Sidebar 
        currentUser={currentUser}
        onToggleTheme={() => {
          document.documentElement.classList.toggle('dark');
        }}
        isDarkMode={document.documentElement.classList.contains('dark')}
        onLogout={() => navigate(ROUTES.LOGIN)}
        onCreateThread={() => {}}
        isCreateModalOpen={false}
      />

      {/* Main Content */}
      <main id="site__main" className="p-2.5 h-[calc(100vh-var(--m-top))] mt-[--m-top] bg-white">
        <div className="max-w-4xl mx-auto p-4">
          {/* Header */}
          <div className="flex items-center gap-4 mb-6">
            <button
              onClick={() => navigate(-1)}
              className="p-2 hover:bg-gray-100 dark:hover:bg-slate-800 rounded-full transition-colors"
            >
              <IonIcon icon={arrowBack} className="text-xl text-gray-600 dark:text-gray-400" />
            </button>
            
            <div>
              <h1 className="text-xl font-bold text-gray-900 dark:text-white">
                {isOwnProfile ? 'Your' : `${profileUser.name}'s`} {activeTab === 'following' ? 'Following' : 'Followers'}
              </h1>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                {currentList.length} {activeTab === 'following' ? 'following' : 'followers'}
              </p>
            </div>
          </div>

          {/* Tabs */}
          <div className="flex gap-1 mb-6 bg-gray-100 dark:bg-slate-800 rounded-lg p-1">
            <button
              onClick={() => setActiveTab('following')}
              className={`flex-1 py-2 px-4 text-sm font-medium rounded-md transition-colors ${
                activeTab === 'following'
                  ? 'bg-white dark:bg-slate-700 text-gray-900 dark:text-white shadow-sm'
                  : 'text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300'
              }`}
            >
              Following ({following.length})
            </button>
            <button
              onClick={() => setActiveTab('followers')}
              className={`flex-1 py-2 px-4 text-sm font-medium rounded-md transition-colors ${
                activeTab === 'followers'
                  ? 'bg-white dark:bg-slate-700 text-gray-900 dark:text-white shadow-sm'
                  : 'text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300'
              }`}
            >
              Followers ({followers.length})
            </button>
          </div>

          {/* Users List */}
          <div className="space-y-3">
            {currentList.length === 0 ? (
              <div className="text-center py-12">
                <IonIcon 
                  icon={activeTab === 'following' ? personAdd : person} 
                  className="text-4xl text-gray-400 dark:text-gray-600 mx-auto mb-4" 
                />
                <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">
                  No {activeTab} yet
                </h3>
                <p className="text-gray-500 dark:text-gray-400">
                  {isOwnProfile 
                    ? `You haven't ${activeTab === 'following' ? 'followed anyone' : 'gained any followers'} yet.`
                    : `${profileUser.name} hasn't ${activeTab === 'following' ? 'followed anyone' : 'gained any followers'} yet.`
                  }
                </p>
              </div>
            ) : (
              currentList.map(renderUserCard)
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default FollowingPage;
