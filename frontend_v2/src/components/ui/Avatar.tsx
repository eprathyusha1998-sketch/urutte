import React from 'react';
import { generateInitials, getInitialsBackgroundColor } from '../../utils/profileUtils';
import { getProfileImageUrl } from '../../utils/mediaUtils';

export interface AvatarProps {
  src?: string;
  alt?: string;
  name?: string;
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
  className?: string;
  onClick?: (e: React.MouseEvent) => void;
  showOnlineStatus?: boolean;
  isOnline?: boolean;
}

const Avatar: React.FC<AvatarProps> = ({
  src,
  alt,
  name = 'User',
  size = 'md',
  className = '',
  onClick,
  showOnlineStatus = false,
  isOnline = false
}) => {
  const sizeClasses = {
    xs: 'w-6 h-6 text-xs',
    sm: 'w-8 h-8 text-sm',
    md: 'w-10 h-10 text-sm',
    lg: 'w-12 h-12 text-base',
    xl: 'w-16 h-16 text-lg'
  };
  
  const baseClasses = 'rounded-full bg-gray-300 dark:bg-slate-600 flex items-center justify-center overflow-hidden relative';
  const clickableClasses = onClick ? 'cursor-pointer hover:opacity-80 transition-opacity' : '';
  
  const classes = `${baseClasses} ${sizeClasses[size]} ${clickableClasses} ${className}`;
  
  const handleImageError = (e: React.SyntheticEvent<HTMLImageElement>) => {
    const target = e.target as HTMLImageElement;
    target.style.display = 'none';
    const fallback = target.nextElementSibling as HTMLElement;
    if (fallback) fallback.style.display = 'flex';
  };
  
  return (
    <div className={classes} onClick={onClick}>
      {src ? (
        <img
          src={getProfileImageUrl(src)}
          alt={alt || name}
          className="w-full h-full object-cover"
          onError={handleImageError}
        />
      ) : null}
      <div 
        className="w-full h-full bg-gray-100 dark:bg-slate-600 flex items-center justify-center"
        style={{ display: src ? 'none' : 'flex' }}
      >
        <div className={`rounded-full ${getInitialsBackgroundColor(name)} flex items-center justify-center`}>
          <span className="text-white font-semibold">
            {generateInitials(name)}
          </span>
        </div>
      </div>
      
      {/* Online Status Indicator */}
      {showOnlineStatus && (
        <div className={`absolute bottom-0 right-0 w-3 h-3 rounded-full border-2 border-white dark:border-slate-800 ${
          isOnline ? 'bg-green-500' : 'bg-gray-400'
        }`} />
      )}
    </div>
  );
};

export default Avatar;
