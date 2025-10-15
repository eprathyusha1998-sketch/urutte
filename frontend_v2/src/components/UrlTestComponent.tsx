import React from 'react';
import { renderEnhancedContent } from '../utils/contentUtils';

const UrlTestComponent: React.FC = () => {
  const testContent = `🚀 Exciting news in AI! Check out this breakthrough: https://openai.com/research/gpt-4
Also visit www.anthropic.com for more AI research.
Follow @elonmusk for updates and join the discussion with #AI #MachineLearning`;

  return (
    <div className="p-4 bg-white dark:bg-slate-800 rounded-lg border border-gray-200 dark:border-slate-700">
      <h3 className="text-lg font-semibold mb-3 text-gray-900 dark:text-white">
        URL Link Feature Test
      </h3>
      <div className="whitespace-pre-wrap break-words text-gray-700 dark:text-gray-300">
        {renderEnhancedContent(
          testContent,
          (hashtag) => console.log('Hashtag clicked:', hashtag),
          (mention) => console.log('Mention clicked:', mention)
        )}
      </div>
      <div className="mt-4 text-sm text-gray-500 dark:text-gray-400">
        <p>✅ URLs should be clickable and open in new tabs</p>
        <p>✅ Hashtags and mentions should be clickable</p>
        <p>✅ Check browser console for click events</p>
      </div>
    </div>
  );
};

export default UrlTestComponent;
