import React from 'react';
import { useNavigate } from 'react-router-dom';
import { DYNAMIC_ROUTES } from '../../constants';
import { renderEnhancedContent } from '../../utils/contentUtils';

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
    return renderEnhancedContent(
      text,
      (hashtag) => navigate(DYNAMIC_ROUTES.HASHTAG(hashtag)),
      (mention) => navigate(DYNAMIC_ROUTES.PROFILE_BY_USERNAME(mention))
    );
  };
  
  return (
    <div className={`whitespace-pre-wrap break-words ${className}`}>
      {renderContent(content)}
    </div>
  );
};

export default ContentRenderer;
