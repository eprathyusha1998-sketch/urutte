import React from 'react';
import UrlTestComponent from '../components/UrlTestComponent';
import { renderEnhancedContent } from '../utils/contentUtils';

const UrlTestPage: React.FC = () => {
  const testExamples = [
    {
      title: "Technology News",
      content: `🚀 Exciting news in AI! Check out this breakthrough: https://openai.com/research/gpt-4
Also visit www.anthropic.com for more AI research.
Follow @elonmusk for updates and join the discussion with #AI #MachineLearning`
    },
    {
      title: "Cybersecurity",
      content: `🔒 Important security update! Read the full report at: https://www.cisa.gov/news-events/cybersecurity-advisories
For more info, visit www.owasp.org
Stay safe online! #Cybersecurity #InfoSec @securityexpert`
    },
    {
      title: "Startup News",
      content: `💰 Big funding round! Details here: https://techcrunch.com/2024/startup-funding
Also check out www.ycombinator.com for startup resources.
#StartupLife #VentureCapital @startupfounder`
    }
  ];

  return (
    <div className="min-h-screen bg-gray-100 dark:bg-slate-900 p-4">
      <div className="max-w-4xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">
            URL Link Feature Test
          </h1>
          <p className="text-gray-600 dark:text-gray-400">
            Test the enhanced content rendering with automatic URL detection and link generation.
          </p>
        </div>

        <div className="space-y-6">
          {testExamples.map((example, index) => (
            <div key={index} className="bg-white dark:bg-slate-800 rounded-lg border border-gray-200 dark:border-slate-700 p-6">
              <h2 className="text-xl font-semibold mb-4 text-gray-900 dark:text-white">
                {example.title}
              </h2>
              <div className="whitespace-pre-wrap break-words text-gray-700 dark:text-gray-300 mb-4">
                {renderEnhancedContent(
                  example.content,
                  (hashtag) => console.log('Hashtag clicked:', hashtag),
                  (mention) => console.log('Mention clicked:', mention)
                )}
              </div>
              <div className="text-sm text-gray-500 dark:text-gray-400 bg-gray-50 dark:bg-slate-700 p-3 rounded">
                <p><strong>Expected behavior:</strong></p>
                <ul className="list-disc list-inside mt-1 space-y-1">
                  <li>URLs should be blue and clickable (open in new tab)</li>
                  <li>Hashtags should be blue and clickable (same tab)</li>
                  <li>Mentions should be blue and clickable (same tab)</li>
                  <li>Check browser console for click events</li>
                </ul>
              </div>
            </div>
          ))}
        </div>

        <div className="mt-8 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-6">
          <h3 className="text-lg font-semibold text-blue-900 dark:text-blue-100 mb-2">
            How to Test
          </h3>
          <ol className="list-decimal list-inside text-blue-800 dark:text-blue-200 space-y-1">
            <li>Click on any URL - it should open in a new tab</li>
            <li>Click on hashtags (#AI, #Cybersecurity) - check console for events</li>
            <li>Click on mentions (@elonmusk, @securityexpert) - check console for events</li>
            <li>Verify URLs have proper styling (blue color, hover effects)</li>
            <li>Test on both light and dark themes</li>
          </ol>
        </div>
      </div>
    </div>
  );
};

export default UrlTestPage;
