import React from 'react';

export interface TimeAgoProps {
  timestamp: string;
  className?: string;
  showRelative?: boolean;
}

const TimeAgo: React.FC<TimeAgoProps> = ({
  timestamp,
  className = '',
  showRelative = true
}) => {
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
      
      if (showRelative) {
        return time.toLocaleDateString();
      } else {
        return time.toLocaleString();
      }
    } catch (error) {
      console.error('Error formatting timestamp:', timestamp, error);
      return 'Invalid date';
    }
  };
  
  return (
    <span className={`text-gray-500 dark:text-gray-400 text-sm ${className}`}>
      {formatTimeAgo(timestamp)}
    </span>
  );
};

export default TimeAgo;
