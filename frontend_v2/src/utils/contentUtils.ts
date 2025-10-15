/**
 * Utility functions for content rendering and parsing
 */

import React from 'react';

/**
 * Enhanced content renderer that detects and renders URLs, hashtags, and mentions
 * @param content - The text content to render
 * @param onHashtagClick - Callback for hashtag clicks
 * @param onMentionClick - Callback for mention clicks
 * @param urlClassName - CSS class for URL links
 * @param hashtagClassName - CSS class for hashtag links
 * @param mentionClassName - CSS class for mention links
 * @returns Array of React elements
 */
export const renderEnhancedContent = (
  content: string,
  onHashtagClick?: (hashtag: string) => void,
  onMentionClick?: (mention: string) => void,
  urlClassName: string = "text-blue-600 dark:text-blue-400 hover:underline cursor-pointer break-all",
  hashtagClassName: string = "text-blue-600 dark:text-blue-400 font-medium hover:underline cursor-pointer",
  mentionClassName: string = "text-blue-600 dark:text-blue-400 font-medium hover:underline cursor-pointer"
): React.ReactNode[] => {
  if (!content) return [];

  // Enhanced regex to match URLs, hashtags, and mentions
  // URL regex: matches http/https URLs and www. URLs
  const urlRegex = /(https?:\/\/[^\s]+|www\.[^\s]+)/g;
  const hashtagMentionRegex = /(#\w+|@\w+)/g;

  // First split by URLs, then by hashtags/mentions
  const urlParts = content.split(urlRegex);

  return urlParts.map((urlPart, urlIndex) => {
    // Check if this part is a URL
    if (urlRegex.test(urlPart)) {
      // Ensure the URL has a protocol
      const fullUrl = urlPart.startsWith('http') ? urlPart : `https://${urlPart}`;

      return React.createElement(
        'a',
        {
          key: `url-${urlIndex}`,
          href: fullUrl,
          target: '_blank',
          rel: 'noopener noreferrer',
          className: urlClassName,
          onClick: (e: React.MouseEvent) => e.stopPropagation()
        },
        urlPart
      );
    }

    // For non-URL parts, split by hashtags and mentions
    const parts = urlPart.split(hashtagMentionRegex);

    return parts.map((part, index) => {
      if (part.startsWith('#')) {
        const hashtag = part.substring(1);
        return React.createElement(
          'span',
          {
            key: `hashtag-${urlIndex}-${index}`,
            className: hashtagClassName,
            onClick: (e: React.MouseEvent) => {
              e.stopPropagation();
              onHashtagClick?.(hashtag);
            }
          },
          part
        );
      } else if (part.startsWith('@')) {
        const mention = part.substring(1);
        return React.createElement(
          'span',
          {
            key: `mention-${urlIndex}-${index}`,
            className: mentionClassName,
            onClick: (e: React.MouseEvent) => {
              e.stopPropagation();
              onMentionClick?.(mention);
            }
          },
          part
        );
      }
      return part;
    });
  });
};

/**
 * Check if a string contains URLs
 * @param content - The text content to check
 * @returns boolean indicating if URLs are present
 */
export const containsUrls = (content: string): boolean => {
  const urlRegex = /(https?:\/\/[^\s]+|www\.[^\s]+)/g;
  return urlRegex.test(content);
};

/**
 * Extract URLs from content
 * @param content - The text content to extract URLs from
 * @returns Array of URLs found in the content
 */
export const extractUrls = (content: string): string[] => {
  const urlRegex = /(https?:\/\/[^\s]+|www\.[^\s]+)/g;
  return content.match(urlRegex) || [];
};

/**
 * Truncate URLs for display while keeping them functional
 * @param url - The URL to truncate
 * @param maxLength - Maximum length for display
 * @returns Truncated URL string
 */
export const truncateUrl = (url: string, maxLength: number = 50): string => {
  if (url.length <= maxLength) return url;
  
  // Remove protocol for display
  const displayUrl = url.replace(/^https?:\/\//, '');
  
  if (displayUrl.length <= maxLength) return displayUrl;
  
  // Truncate and add ellipsis
  return displayUrl.substring(0, maxLength - 3) + '...';
};
