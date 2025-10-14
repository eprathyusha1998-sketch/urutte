import React, { useState, useEffect } from 'react';
import { IonIcon } from '@ionic/react';
import { checkmark, close } from 'ionicons/icons';
import { generateInitials, getInitialsBackgroundColor } from '../utils/profileUtils';
import { getProfileImageUrl } from '../utils/mediaUtils';
import { useNotification } from '../contexts/NotificationContext';
import { usersApi } from '../services/api';

interface FollowRequest {
  id: number;
  requester: {
    id: string;
    name: string;
    username: string;
    picture?: string;
    bio?: string;
  };
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt: string;
}

interface FollowRequestsProps {
  currentUser?: {
    id: string;
    name: string;
    email: string;
    picture?: string;
  } | null;
}

const FollowRequests: React.FC<FollowRequestsProps> = ({ currentUser }) => {
  const [followRequests, setFollowRequests] = useState<FollowRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState<Set<number>>(new Set());
  const { showSuccess, showError } = useNotification();

  useEffect(() => {
    if (currentUser) {
      loadFollowRequests();
    }
  }, [currentUser]);

  const loadFollowRequests = async () => {
    try {
      setLoading(true);
      const response = await usersApi.getPendingFollowRequests();
      setFollowRequests(response);
    } catch (error) {
      console.error('Error loading follow requests:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (requestId: number) => {
    try {
      setProcessing(prev => new Set(prev).add(requestId));
      await usersApi.approveFollowRequest(requestId);
      
      // Remove the approved request from the list
      setFollowRequests(prev => prev.filter(req => req.id !== requestId));
      
      showSuccess('Success', 'Follow request approved!');
    } catch (error) {
      console.error('Error approving follow request:', error);
      showError('Error', 'Failed to approve follow request. Please try again.');
    } finally {
      setProcessing(prev => {
        const newSet = new Set(prev);
        newSet.delete(requestId);
        return newSet;
      });
    }
  };

  const handleReject = async (requestId: number) => {
    try {
      setProcessing(prev => new Set(prev).add(requestId));
      await usersApi.rejectFollowRequest(requestId);
      
      // Remove the rejected request from the list
      setFollowRequests(prev => prev.filter(req => req.id !== requestId));
      
      showSuccess('Success', 'Follow request rejected.');
    } catch (error) {
      console.error('Error rejecting follow request:', error);
      showError('Error', 'Failed to reject follow request. Please try again.');
    } finally {
      setProcessing(prev => {
        const newSet = new Set(prev);
        newSet.delete(requestId);
        return newSet;
      });
    }
  };

  if (loading) {
    return (
      <div className="bg-white dark:bg-slate-800 rounded-lg shadow-md p-4">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Follow Requests</h3>
        <div className="space-y-3">
          {[...Array(2)].map((_, index) => (
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

  if (followRequests.length === 0) {
    return null; // Don't show the component if there are no requests
  }

  return (
    <div className="bg-white dark:bg-slate-800 rounded-lg shadow-md p-4">
      <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
        Follow Requests ({followRequests.length})
      </h3>
      <div className="space-y-4">
        {followRequests.map((request) => (
          <div key={request.id} className="flex items-center space-x-3">
            {/* User Avatar */}
            <div className="w-10 h-10 rounded-full overflow-hidden flex-shrink-0">
              {request.requester.picture ? (
                <img 
                  src={getProfileImageUrl(request.requester.picture)} 
                  alt={request.requester.name}
                  className="w-full h-full object-cover"
                />
              ) : (
                <div className={`w-full h-full ${getInitialsBackgroundColor(request.requester.name)} flex items-center justify-center`}>
                  <span className="text-white text-sm font-semibold">
                    {generateInitials(request.requester.name)}
                  </span>
                </div>
              )}
            </div>

            {/* User Info */}
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-gray-900 dark:text-white truncate">
                {request.requester.name}
              </p>
              <p className="text-xs text-gray-500 dark:text-gray-400 truncate">
                @{request.requester.username}
              </p>
              {request.requester.bio && (
                <p className="text-xs text-gray-600 dark:text-gray-300 truncate mt-1">
                  {request.requester.bio}
                </p>
              )}
            </div>

            {/* Action Buttons */}
            <div className="flex-shrink-0 flex gap-2">
              <button
                onClick={() => handleApprove(request.id)}
                disabled={processing.has(request.id)}
                className="flex items-center justify-center w-8 h-8 bg-black text-white rounded-full hover:bg-gray-800 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                title="Approve"
              >
                {processing.has(request.id) ? (
                  <div className="w-3 h-3 border-2 border-white border-t-transparent rounded-full animate-spin" />
                ) : (
                  <IonIcon icon={checkmark} className="text-base text-white font-bold" />
                )}
              </button>
              <button
                onClick={() => handleReject(request.id)}
                disabled={processing.has(request.id)}
                className="flex items-center justify-center w-8 h-8 bg-red-600 text-white rounded-full hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                title="Reject"
              >
                {processing.has(request.id) ? (
                  <div className="w-3 h-3 border-2 border-white border-t-transparent rounded-full animate-spin" />
                ) : (
                  <IonIcon icon={close} className="text-base text-white font-bold" />
                )}
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default FollowRequests;
