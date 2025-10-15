import React, { useState, useEffect } from 'react';
import { IonIcon } from '@ionic/react';
import { heart, heartOutline, refresh } from 'ionicons/icons';
import { topicsApi } from '../services/api';

interface Topic {
  id: string;
  name: string;
  description: string;
  aiPrompt: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

interface TopicSuggestionPanelProps {
  currentUser: any;
}

const TopicSuggestionPanel: React.FC<TopicSuggestionPanelProps> = ({ currentUser }) => {
  const [topics, setTopics] = useState<Topic[]>([]);
  const [loading, setLoading] = useState(true);
  const [likedTopics, setLikedTopics] = useState<Set<string>>(new Set());
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    if (currentUser) {
      fetchTopicSuggestions();
      fetchLikedTopics();
    }
  }, [currentUser]);

  const fetchTopicSuggestions = async () => {
    try {
      setLoading(true);
      const response = await topicsApi.getTopicSuggestions(5);
      setTopics(response);
    } catch (error) {
      console.error('Error fetching topic suggestions:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchLikedTopics = async () => {
    try {
      const response = await topicsApi.getLikedTopics();
      const likedIds = new Set<string>(response.map((topic: Topic) => topic.id));
      setLikedTopics(likedIds);
    } catch (error) {
      console.error('Error fetching liked topics:', error);
    }
  };

  const handleLikeTopic = async (topicId: string) => {
    try {
      const isCurrentlyLiked = likedTopics.has(topicId);
      
      if (isCurrentlyLiked) {
        await topicsApi.unlikeTopic(topicId);
        setLikedTopics(prev => {
          const newSet = new Set(prev);
          newSet.delete(topicId);
          return newSet;
        });
      } else {
        await topicsApi.likeTopic(topicId);
        setLikedTopics(prev => {
          const newSet = new Set(prev);
          newSet.add(topicId);
          return newSet;
        });
      }
    } catch (error) {
      console.error('Error toggling topic like:', error);
    }
  };

  const handleRefresh = async () => {
    setRefreshing(true);
    await fetchTopicSuggestions();
    setRefreshing(false);
  };

  if (!currentUser) {
    return null;
  }

  return (
    <div className="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-gray-200 dark:border-slate-700 overflow-hidden">
      {/* Header */}
      <div className="px-4 py-3 border-b border-gray-100 dark:border-slate-700">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
            Topics for You
          </h3>
          <button
            onClick={handleRefresh}
            disabled={refreshing}
            className="p-1.5 rounded-full hover:bg-gray-100 dark:hover:bg-slate-700 transition-colors disabled:opacity-50"
            title="Refresh topics"
          >
            <IonIcon 
              icon={refresh} 
              className={`text-lg text-gray-500 dark:text-gray-400 ${refreshing ? 'animate-spin' : ''}`} 
            />
          </button>
        </div>
        <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
          Like topics to personalize your feed (max 5)
        </p>
      </div>

      {/* Content */}
      <div className="p-4">
        {loading ? (
          <div className="space-y-3">
            {[...Array(3)].map((_, index) => (
              <div key={index} className="animate-pulse">
                <div className="flex items-center justify-between p-3 bg-gray-100 dark:bg-slate-700 rounded-lg">
                  <div className="flex-1">
                    <div className="h-4 bg-gray-200 dark:bg-slate-600 rounded w-3/4 mb-2"></div>
                    <div className="h-3 bg-gray-200 dark:bg-slate-600 rounded w-full"></div>
                  </div>
                  <div className="w-8 h-8 bg-gray-200 dark:bg-slate-600 rounded-full ml-3"></div>
                </div>
              </div>
            ))}
          </div>
        ) : topics.length === 0 ? (
          <div className="text-center py-6">
            <p className="text-gray-500 dark:text-gray-400 text-sm">
              No more topics available. You've liked all available topics!
            </p>
          </div>
        ) : (
          <div className="space-y-3">
            {topics.map((topic) => {
              const isLiked = likedTopics.has(topic.id);
              return (
                <div
                  key={topic.id}
                  className="flex items-center justify-between p-3 bg-gray-50 dark:bg-slate-700 rounded-lg hover:bg-gray-100 dark:hover:bg-slate-600 transition-colors"
                >
                  <div className="flex-1 min-w-0">
                    <h4 className="text-sm font-medium text-gray-900 dark:text-white truncate">
                      {topic.name}
                    </h4>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mt-1 line-clamp-2">
                      {topic.description}
                    </p>
                  </div>
                  <button
                    onClick={() => handleLikeTopic(topic.id)}
                    className={`ml-3 p-2 rounded-full transition-colors ${
                      isLiked
                        ? 'text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20'
                        : 'text-gray-400 hover:text-red-500 hover:bg-gray-100 dark:hover:bg-slate-600'
                    }`}
                    title={isLiked ? 'Unlike topic' : 'Like topic'}
                  >
                    <IonIcon 
                      icon={isLiked ? heart : heartOutline} 
                      className="text-lg" 
                    />
                  </button>
                </div>
              );
            })}
          </div>
        )}

        {/* Show current liked topics count */}
        {likedTopics.size > 0 && (
          <div className="mt-4 pt-3 border-t border-gray-100 dark:border-slate-700">
            <p className="text-xs text-gray-500 dark:text-gray-400 text-center">
              You've liked {likedTopics.size} topic{likedTopics.size !== 1 ? 's' : ''}
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default TopicSuggestionPanel;
