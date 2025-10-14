import React from 'react';
import { useNavigate } from 'react-router-dom';
import { DYNAMIC_ROUTES } from '../../constants';

export interface ContentRendererProps {
  content: string;
  className?: string;
}

const ContentRenderer: React.FC<ContentRendererProps> = ({
  content,
  className = ''
}) => {
  const navigate = useNavigate();
  
  const renderContent = (text: string) => {
    if (!text) return '';
    
    // Split content by hashtags and mentions, preserving the delimiters
    const parts = text.split(/(#\w+|@\w+)/g);
    
    return parts.map((part, index) => {
      if (part.startsWith('#')) {
        return (
          <span 
            key={index} 
            className="text-blue-600 dark:text-blue-400 font-medium hover:underline cursor-pointer"
            onClick={(e) => {
              e.stopPropagation();
              navigate(DYNAMIC_ROUTES.HASHTAG(part.substring(1)));
            }}
          >
            {part}
          </span>
        );
      } else if (part.startsWith('@')) {
        return (
          <span 
            key={index} 
            className="text-blue-600 dark:text-blue-400 font-medium hover:underline cursor-pointer"
            onClick={(e) => {
              e.stopPropagation();
              navigate(DYNAMIC_ROUTES.PROFILE_BY_USERNAME(part.substring(1)));
            }}
          >
            {part}
          </span>
        );
      }
      return part;
    });
  };
  
  return (
    <div className={`whitespace-pre-wrap break-words ${className}`}>
      {renderContent(content)}
    </div>
  );
};

export default ContentRenderer;
