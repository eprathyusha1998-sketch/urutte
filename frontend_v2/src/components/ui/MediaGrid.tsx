import React from 'react';
import { IonIcon } from '@ionic/react';
import { image, videocam, document } from 'ionicons/icons';
import { getMediaUrl } from '../../utils/mediaUtils';

export interface MediaItem {
  id: number;
  mediaUrl: string;
  mediaType: string;
  altText?: string;
}

export interface MediaGridProps {
  media: MediaItem[];
  className?: string;
  maxHeight?: string;
}

const MediaGrid: React.FC<MediaGridProps> = ({
  media,
  className = '',
  maxHeight = 'max-h-96'
}) => {
  if (!media || media.length === 0) return null;
  
  const getGridClasses = (count: number) => {
    switch (count) {
      case 1:
        return 'grid-cols-1';
      case 2:
        return 'grid-cols-2';
      case 3:
        return 'grid-cols-3';
      case 4:
        return 'grid-cols-2';
      default:
        return 'grid-cols-3';
    }
  };
  
  const getMediaIcon = (mediaType: string) => {
    const type = mediaType?.toLowerCase();
    switch (type) {
      case 'image':
        return <IonIcon icon={image} className="text-blue-500" />;
      case 'video':
        return <IonIcon icon={videocam} className="text-red-500" />;
      default:
        return <IonIcon icon={document} className="text-gray-500" />;
    }
  };
  
  const renderMediaItem = (item: MediaItem, index: number) => {
    const isSingle = media.length === 1;
    const mediaType = item.mediaType?.toLowerCase();
    
    if (mediaType === 'image') {
      return (
        <img
          key={item.id}
          src={getMediaUrl(item.mediaUrl)}
          alt={item.altText || 'Media'}
          className={`w-full rounded-lg object-cover ${
            isSingle ? maxHeight : 'aspect-square'
          }`}
          onError={(e) => {
            console.error('Media failed to load:', item.mediaUrl);
            (e.target as HTMLImageElement).style.display = 'none';
          }}
        />
      );
    }
    
    if (mediaType === 'video') {
      return (
        <video
          key={item.id}
          src={getMediaUrl(item.mediaUrl)}
          controls
          className={`w-full rounded-lg ${
            isSingle ? maxHeight : 'aspect-square'
          }`}
        />
      );
    }
    
    return (
      <div
        key={item.id}
        className="flex items-center justify-center p-3 bg-gray-100 dark:bg-slate-700 rounded-lg aspect-square"
      >
        <div className="text-center">
          {getMediaIcon(item.mediaType)}
          <span className="text-xs text-gray-600 dark:text-gray-300 block mt-1">
            {item.mediaType?.toUpperCase()}
          </span>
        </div>
      </div>
    );
  };
  
  return (
    <div className={`grid gap-1 ${getGridClasses(media.length)} ${className}`}>
      {media.map((item, index) => renderMediaItem(item, index))}
    </div>
  );
};

export default MediaGrid;
