import React, { useState, useEffect, useRef } from 'react';
import { IonIcon } from '@ionic/react';
import { 
  close,
  image,
  happy,
  location,
  videocam
} from 'ionicons/icons';
import { hashtagApi } from '../services/api';
import { useNotification } from '../contexts/NotificationContext';
import { generateInitials, getInitialsBackgroundColor } from '../utils/profileUtils';
import { getProfileImageUrl } from '../utils/mediaUtils';

interface NewThreadModalProps {
  isOpen: boolean;
  onClose: () => void;
  currentUser?: {
    id: string;
    name: string;
    email: string;
    picture?: string;
  } | null;
  onSubmit: (content: string, mediaFiles?: File[], replyPermission?: 'ANYONE' | 'FOLLOWERS' | 'FOLLOWING' | 'MENTIONED_ONLY') => void;
}

const NewThreadModal: React.FC<NewThreadModalProps> = ({
  isOpen,
  onClose,
  currentUser,
  onSubmit
}) => {
  const { showSuccess, showError } = useNotification();
  const [content, setContent] = useState('');
  const [selectedMedia, setSelectedMedia] = useState<File[]>([]);
  const [mediaPreviews, setMediaPreviews] = useState<string[]>([]);
  const [isPosting, setIsPosting] = useState(false);
  const [topic, setTopic] = useState('');
  const [showTopicSuggestions, setShowTopicSuggestions] = useState(false);
  const [topicSuggestions, setTopicSuggestions] = useState<string[]>([]);
  const [replyPermission, setReplyPermission] = useState<'ANYONE' | 'FOLLOWERS' | 'FOLLOWING' | 'MENTIONED_ONLY'>('ANYONE');
  const [showReplyPermissionDropdown, setShowReplyPermissionDropdown] = useState(false);
  const [showEmojiPicker, setShowEmojiPicker] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  // Handle emoji selection
  const handleEmojiSelect = (emoji: string) => {
    setContent(prev => prev + emoji);
    setShowEmojiPicker(false);
  };

  // Common emojis for the picker
  const commonEmojis = [
    '😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣',
    '😊', '😇', '🙂', '🙃', '😉', '😌', '😍', '🥰',
    '😘', '😗', '😙', '😚', '😋', '😛', '😝', '😜',
    '🤪', '🤨', '🧐', '🤓', '😎', '🤩', '🥳', '😏',
    '😒', '😞', '😔', '😟', '😕', '🙁', '☹️', '😣',
    '😖', '😫', '😩', '🥺', '😢', '😭', '😤', '😠',
    '😡', '🤬', '🤯', '😳', '🥵', '🥶', '😱', '😨',
    '😰', '😥', '😓', '🤗', '🤔', '🤭', '🤫', '🤥',
    '😶', '😐', '😑', '😬', '🙄', '😯', '😦', '😧',
    '😮', '😲', '🥱', '😴', '🤤', '😪', '😵', '🤐',
    '🥴', '🤢', '🤮', '🤧', '😷', '🤒', '🤕', '🤑',
    '🤠', '😈', '👿', '👹', '👺', '🤡', '💩', '👻',
    '💀', '☠️', '👽', '👾', '🤖', '🎃', '😺', '😸',
    '😹', '😻', '😼', '😽', '🙀', '😿', '😾', '👶',
    '👧', '🧒', '👦', '👩', '🧑', '👨', '👱', '👱‍♀️',
    '👱‍♂️', '🧔', '👵', '🧓', '👴', '👲', '👳', '👳‍♀️',
    '👳‍♂️', '🧕', '👮', '👮‍♀️', '👮‍♂️', '👷', '👷‍♀️', '👷‍♂️',
    '💂', '💂‍♀️', '💂‍♂️', '🕵️', '🕵️‍♀️', '🕵️‍♂️', '👩‍⚕️', '👨‍⚕️',
    '👩‍🌾', '👨‍🌾', '👩‍🍳', '👨‍🍳', '👩‍🎓', '👨‍🎓', '👩‍🎤', '👨‍🎤',
    '👩‍🏫', '👨‍🏫', '👩‍🏭', '👨‍🏭', '👩‍💻', '👨‍💻', '👩‍💼', '👨‍💼',
    '👩‍🔧', '👨‍🔧', '👩‍🔬', '👨‍🔬', '👩‍🎨', '👨‍🎨', '👩‍🚒', '👨‍🚒',
    '👩‍✈️', '👨‍✈️', '👩‍🚀', '👨‍🚀', '👩‍⚖️', '👨‍⚖️', '👰', '🤵',
    '👸', '🤴', '🦸', '🦸‍♀️', '🦸‍♂️', '🦹', '🦹‍♀️', '🦹‍♂️',
    '🤶', '🎅', '🧙', '🧙‍♀️', '🧙‍♂️', '🧝', '🧝‍♀️', '🧝‍♂️',
    '🧛', '🧛‍♀️', '🧛‍♂️', '🧟', '🧟‍♀️', '🧟‍♂️', '🧞', '🧞‍♀️',
    '🧞‍♂️', '🧜', '🧜‍♀️', '🧜‍♂️', '🧚', '🧚‍♀️', '🧚‍♂️', '👼',
    '🤰', '🤱', '🙇', '🙇‍♀️', '🙇‍♂️', '💁', '💁‍♀️', '💁‍♂️',
    '🙅', '🙅‍♀️', '🙅‍♂️', '🙆', '🙆‍♀️', '🙆‍♂️', '🙋', '🙋‍♀️',
    '🙋‍♂️', '🧏', '🧏‍♀️', '🧏‍♂️', '🤦', '🤦‍♀️', '🤦‍♂️', '🤷',
    '🤷‍♀️', '🤷‍♂️', '🙎', '🙎‍♀️', '🙎‍♂️', '🙍', '🙍‍♀️', '🙍‍♂️',
    '💇', '💇‍♀️', '💇‍♂️', '💆', '💆‍♀️', '💆‍♂️', '🧖', '🧖‍♀️',
    '🧖‍♂️', '💅', '🤳', '💃', '🕺', '👯', '👯‍♀️', '👯‍♂️',
    '🕴', '👩‍🦽', '👨‍🦽', '👩‍🦼', '👨‍🦼', '🚶', '🚶‍♀️', '🚶‍♂️',
    '👩‍🦯', '👨‍🦯', '🧎', '🧎‍♀️', '🧎‍♂️', '🏃', '🏃‍♀️', '🏃‍♂️',
    '🧍', '🧍‍♀️', '🧍‍♂️', '👫', '👬', '👭', '💑', '👩‍❤️‍👩',
    '👨‍❤️‍👨', '💏', '👩‍❤️‍💋‍👩', '👨‍❤️‍💋‍👨', '👪', '👨‍👩‍👧', '👨‍👩‍👧‍👦', '👨‍👩‍👦‍👦',
    '👨‍👩‍👧‍👧', '👨‍👨‍👦', '👨‍👨‍👧', '👨‍👨‍👧‍👦', '👨‍👨‍👦‍👦', '👨‍👨‍👧‍👧', '👩‍👩‍👦', '👩‍👩‍👧',
    '👩‍👩‍👧‍👦', '👩‍👩‍👦‍👦', '👩‍👩‍👧‍👧', '👨‍👦', '👨‍👦‍👦', '👨‍👧', '👨‍👧‍👦', '👨‍👧‍👧',
    '👩‍👦', '👩‍👦‍👦', '👩‍👧', '👩‍👧‍👦', '👩‍👧‍👧', '🗣', '👤', '👥',
    '👋', '🤚', '🖐', '✋', '🖖', '👌', '🤏', '✌️',
    '🤞', '🤟', '🤘', '🤙', '👈', '👉', '👆', '🖕',
    '👇', '☝️', '👍', '👎', '👊', '✊', '🤛', '🤜',
    '👏', '🙌', '👐', '🤲', '🤝', '🙏', '✍️', '💅',
    '🤳', '💪', '🦾', '🦿', '🦵', '🦶', '👂', '🦻',
    '👃', '🧠', '🦷', '🦴', '👀', '👁', '👅', '👄',
    '💋', '🩸', '❤️', '🧡', '💛', '💚', '💙', '💜',
    '🖤', '🤍', '🤎', '💔', '❣️', '💕', '💞', '💓',
    '💗', '💖', '💘', '💝', '💟', '☮️', '✝️', '☪️',
    '🕉', '☸️', '✡️', '🔯', '🕎', '☯️', '☦️', '🛐',
    '⛎', '♈', '♉', '♊', '♋', '♌', '♍', '♎',
    '♏', '♐', '♑', '♒', '♓', '🆔', '⚛️', '🉑',
    '☢️', '☣️', '📴', '📳', '🈶', '🈚', '🈸', '🈺',
    '🈷️', '✴️', '🆚', '💮', '🉐', '㊙️', '㊗️', '🈴',
    '🈵', '🈹', '🈲', '🅰️', '🅱️', '🆎', '🆑', '🅾️',
    '🆘', '❌', '⭕', '🛑', '⛔', '📛', '🚫', '💯',
    '💢', '♨️', '🚷', '🚯', '🚳', '🚱', '🔞', '📵',
    '🚭', '❗', '❕', '❓', '❔', '‼️', '⁉️', '🔅',
    '🔆', '〽️', '⚠️', '🚸', '🔱', '⚜️', '🔰', '♻️',
    '✅', '🈯', '💹', '❇️', '✳️', '❎', '🌐', '💠',
    'Ⓜ️', '🌀', '💤', '🏧', '🚾', '♿', '🅿️', '🈳',
    '🈂️', '🛂', '🛃', '🛄', '🛅', '🚹', '🚺', '🚼',
    '🚻', '🚮', '🎦', '📶', '🈁', '🔣', 'ℹ️', '🔤',
    '🔡', '🔠', '🆖', '🆗', '🆙', '🆒', '🆕', '🆓',
    '0️⃣', '1️⃣', '2️⃣', '3️⃣', '4️⃣', '5️⃣', '6️⃣', '7️⃣',
    '8️⃣', '9️⃣', '🔟', '🔢', '#️⃣', '*️⃣', '⏏️', '▶️',
    '⏸', '⏯', '⏹', '⏺', '⏭', '⏮', '⏩', '⏪',
    '⏫', '⏬', '◀️', '🔼', '🔽', '➡️', '⬅️', '⬆️',
    '⬇️', '↗️', '↘️', '↙️', '↖️', '↕️', '↔️', '↩️',
    '↪️', '⤴️', '⤵️', '🔃', '🔄', '🔙', '🔚', '🔛',
    '🔜', '🔝', '🛐', '⚛️', '🕉', '✡️', '☸️', '☯️',
    '✝️', '☦️', '☪️', '☮️', '🕎', '🔯', '♈', '♉',
    '♊', '♋', '♌', '♍', '♎', '♏', '♐', '♑',
    '♒', '♓', '⛎', '🔀', '🔁', '🔂', '▶️', '⏩',
    '⏭', '⏯', '⏸', '⏹', '⏺', '⏮', '⏪', '🔽',
    '🔼', '⏫', '⏬', '➡️', '⬅️', '⬆️', '⬇️', '↗️',
    '↘️', '↙️', '↖️', '↕️', '↔️', '↩️', '↪️', '⤴️',
    '⤵️', '🔃', '🔄', '🔙', '🔚', '🔛', '🔜', '🔝'
  ];

  // Handle click outside to close dropdowns
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setShowReplyPermissionDropdown(false);
      }
      // Close emoji picker if clicking outside
      const emojiPicker = document.querySelector('.emoji-picker');
      if (emojiPicker && !emojiPicker.contains(event.target as Node)) {
        setShowEmojiPicker(false);
      }
    };

    if (showReplyPermissionDropdown || showEmojiPicker) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showReplyPermissionDropdown, showEmojiPicker]);

  const handleMediaSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    if (files.length > 0) {
      const newMedia = [...selectedMedia, ...files].slice(0, 10); // Limit to 10 files
      setSelectedMedia(newMedia);
      
      // Create previews for new files
      const newPreviews: string[] = [];
      files.forEach((file) => {
        const reader = new FileReader();
        reader.onload = (e) => {
          newPreviews.push(e.target?.result as string);
          if (newPreviews.length === files.length) {
            setMediaPreviews([...mediaPreviews, ...newPreviews]);
          }
        };
        reader.readAsDataURL(file);
      });
    }
  };

  const removeMedia = (index: number) => {
    const newMedia = selectedMedia.filter((_, i) => i !== index);
    const newPreviews = mediaPreviews.filter((_, i) => i !== index);
    setSelectedMedia(newMedia);
    setMediaPreviews(newPreviews);
  };

  // Get topic suggestions from backend
  const getTopicSuggestions = async (input: string) => {
    try {
      if (!input.trim()) {
        // Get trending hashtags if no input
        const trending = await hashtagApi.getTrending(8);
        return trending;
      }
      
      const suggestions = await hashtagApi.getSuggestions(input, 8);
      return suggestions;
    } catch (error) {
      console.error('Error fetching hashtag suggestions:', error);
      return [];
    }
  };

  const handleTopicChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setTopic(value);
    
    if (value.trim()) {
      const suggestions = await getTopicSuggestions(value);
      setTopicSuggestions(suggestions);
      setShowTopicSuggestions(suggestions.length > 0);
    } else {
      // Show trending hashtags when input is empty
      const trending = await getTopicSuggestions('');
      setTopicSuggestions(trending);
      setShowTopicSuggestions(trending.length > 0);
    }
  };

  const handleTopicSelect = (selectedTopic: string) => {
    setTopic(selectedTopic);
    setShowTopicSuggestions(false);
    setTopicSuggestions([]);
  };

  const handleSubmit = async () => {
    if (!content.trim() && !selectedMedia) return;
    
    setIsPosting(true);
    try {
      // Combine topic with content if topic is provided
      const finalContent = topic.trim() 
        ? `#${topic} ${content}`.trim()
        : content;
      
      await onSubmit(finalContent, selectedMedia.length > 0 ? selectedMedia : undefined, replyPermission);
      setContent('');
      setTopic('');
      setSelectedMedia([]);
      setMediaPreviews([]);
      setShowTopicSuggestions(false);
      setTopicSuggestions([]);
      showSuccess('Thread Created!', 'Your thread has been posted successfully.');
      onClose();
    } catch (error) {
      console.error('Error creating thread:', error);
      showError('Thread Creation Failed', 'Failed to create thread. Please try again.');
    } finally {
      setIsPosting(false);
    }
  };

  const handleClose = () => {
    setContent('');
    setTopic('');
    setSelectedMedia([]);
    setMediaPreviews([]);
    setShowTopicSuggestions(false);
    setTopicSuggestions([]);
    onClose();
  };

  // Handle body scroll lock when modal is open
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'unset';
    }
    
    // Cleanup on unmount
    return () => {
      document.body.style.overflow = 'unset';
    };
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div 
      className="fixed inset-0 flex items-center justify-center z-50 p-4"
      style={{ 
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        backdropFilter: 'blur(4px)'
      }}
      onClick={(e) => {
        if (e.target === e.currentTarget) {
          handleClose();
        }
      }}
    >
      <div 
        className="bg-white dark:bg-slate-800 rounded-2xl w-full max-w-2xl max-h-[90vh] relative shadow-2xl"
        style={{
          border: '1px solid rgba(0, 0, 0, 0.1)',
          boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)'
        }}
      >
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-gray-200 dark:border-slate-700">
          <button
            onClick={handleClose}
            className="text-black dark:text-white text-sm font-medium hover:bg-gray-100 dark:hover:bg-slate-700 px-3 py-1 rounded-lg transition-colors"
          >
            Cancel
          </button>
          
          <h2 className="text-lg font-bold text-black dark:text-white">
            New Urutte! 🚀
          </h2>
          
          <button
            onClick={handleClose}
            className="p-2 hover:bg-gray-100 dark:hover:bg-slate-700 rounded-lg transition-colors"
          >
            <IonIcon icon={close} className="text-xl text-black dark:text-white" />
          </button>
        </div>

        {/* Main Content */}
        <div className="p-4 relative">
          {/* User Info and Thread Input */}
          <div className="flex gap-3">
            <div className="w-10 h-10 rounded-full overflow-hidden flex-shrink-0">
              {currentUser?.picture ? (
                <img 
                  src={getProfileImageUrl(currentUser.picture)} 
                  alt={currentUser.name}
                  className="w-full h-full object-cover"
                  onError={(e) => {
                    const target = e.target as HTMLImageElement;
                    target.style.display = 'none';
                    const fallback = target.nextElementSibling as HTMLElement;
                    if (fallback) fallback.style.display = 'flex';
                  }}
                />
              ) : null}
              <div 
                className={`w-full h-full ${getInitialsBackgroundColor(currentUser?.name || '')} flex items-center justify-center`}
                style={{ display: currentUser?.picture ? 'none' : 'flex' }}
              >
                <span className="text-white text-sm font-semibold">
                  {generateInitials(currentUser?.name || '')}
                </span>
              </div>
            </div>
            
            <div className="flex-1 relative">
              {/* Topic Input */}
              <div className="mb-2 relative">
                <input
                  type="text"
                  value={topic}
                  onChange={handleTopicChange}
                  placeholder="Add a topic"
                  className="bg-transparent border-none outline-none focus:ring-0 focus:border-transparent text-gray-400 dark:text-gray-500 placeholder-gray-400 dark:placeholder-gray-500 text-sm w-full"
                  onFocus={async () => {
                    const suggestions = await getTopicSuggestions(topic);
                    setTopicSuggestions(suggestions);
                    setShowTopicSuggestions(suggestions.length > 0);
                  }}
                  onBlur={() => {
                    // Delay hiding suggestions to allow clicking on them
                    setTimeout(() => setShowTopicSuggestions(false), 200);
                  }}
                />
                
                {/* Topic Suggestions Dropdown */}
                {showTopicSuggestions && topicSuggestions.length > 0 && (
                  <div className="absolute top-full left-0 right-0 mt-1 bg-white dark:bg-slate-800 rounded-lg shadow-lg border border-gray-200 dark:border-slate-700 z-50 max-h-48 overflow-y-auto">
                    {topicSuggestions.map((suggestion, index) => (
                      <button
                        key={index}
                        onClick={() => handleTopicSelect(suggestion)}
                        className="w-full px-3 py-1.5 text-left text-xs text-gray-800 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-slate-700 first:rounded-t-lg last:rounded-b-lg"
                      >
                        {suggestion}
                      </button>
                    ))}
                  </div>
                )}
              </div>

              {/* Thread Input */}
              <textarea
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder="What's new?"
                className="w-full bg-transparent border-none outline-none focus:ring-0 focus:border-transparent text-lg text-black dark:text-white placeholder-gray-400 dark:placeholder-gray-500 resize-none min-h-[120px]"
                rows={4}
                maxLength={2000}
              />
            </div>
          </div>

          {/* Media Preview */}
          {mediaPreviews.length > 0 && (
            <div className="mb-4 ml-13">
              <div className={`grid gap-2 ${
                mediaPreviews.length === 1 ? 'grid-cols-1 max-w-xs' :
                mediaPreviews.length === 2 ? 'grid-cols-2' :
                mediaPreviews.length === 3 ? 'grid-cols-3' :
                mediaPreviews.length === 4 ? 'grid-cols-2' :
                'grid-cols-3'
              }`}>
                {mediaPreviews.map((preview, index) => (
                  <div key={index} className="relative group">
                    <div className={`bg-gray-100 dark:bg-gray-700 rounded-lg overflow-hidden ${
                      mediaPreviews.length === 1 ? 'aspect-video max-w-xs' : 'aspect-square'
                    }`}>
                      <img 
                        src={preview} 
                        alt={`Preview ${index + 1}`} 
                        className="w-full h-full object-cover"
                      />
                    </div>
                    <button
                      onClick={() => removeMedia(index)}
                      className="absolute top-2 right-2 bg-black bg-opacity-50 text-white rounded-full w-6 h-6 flex items-center justify-center hover:bg-opacity-70 z-10 opacity-0 group-hover:opacity-100 transition-opacity"
                    >
                      <IonIcon icon={close} className="text-sm" />
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Attachment Icons */}
          <div className="flex items-center gap-4 mb-4 ml-13">
            <input
              type="file"
              accept="image/*,video/*"
              onChange={handleMediaSelect}
              className="hidden"
              id="media-upload"
              multiple
            />
            <label htmlFor="media-upload" className="cursor-pointer">
              <IonIcon icon={image} className="text-2xl text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-400 transition-colors" />
            </label>
            
            <input type="file" accept="image/gif" onChange={handleMediaSelect} className="hidden" id="gif-upload" />
            <label htmlFor="gif-upload" className="cursor-pointer">
              <IonIcon icon={videocam} className="text-2xl text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-400 transition-colors" />
            </label>
            
            <button 
              onClick={() => setShowEmojiPicker(!showEmojiPicker)} 
              className="hover:bg-gray-100 dark:hover:bg-slate-700 p-1 rounded transition-colors relative"
            >
              <IonIcon icon={happy} className="text-2xl text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-400 transition-colors" />
            </button>
            
            <button className="hover:bg-gray-100 dark:hover:bg-slate-700 p-1 rounded transition-colors">
              <div className="w-6 h-6 flex flex-col justify-center">
                <div className="w-full h-0.5 bg-gray-400 dark:bg-gray-500 mb-1"></div>
                <div className="w-full h-0.5 bg-gray-400 dark:bg-gray-500 mb-1"></div>
                <div className="w-full h-0.5 bg-gray-400 dark:bg-gray-500"></div>
              </div>
            </button>
            
            <button className="hover:bg-gray-100 dark:hover:bg-slate-700 p-1 rounded transition-colors">
              <IonIcon icon={location} className="text-2xl text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-400 transition-colors" />
            </button>
          </div>
          
          {/* Emoji Picker */}
          {showEmojiPicker && (
            <div className="emoji-picker absolute top-12 left-0 bg-white dark:bg-slate-800 border border-gray-200 dark:border-slate-700 rounded-lg shadow-xl p-4 z-50 max-h-80 overflow-y-auto min-w-[280px] max-w-[320px]">
              <div className="grid grid-cols-8 gap-2">
                {commonEmojis.map((emoji, index) => (
                  <button
                    key={index}
                    onClick={() => handleEmojiSelect(emoji)}
                    className="w-8 h-8 flex items-center justify-center hover:bg-gray-100 dark:hover:bg-slate-700 rounded-md transition-colors text-lg hover:scale-110"
                    title={emoji}
                  >
                    {emoji}
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="flex items-center justify-between p-4 border-t border-gray-200 dark:border-slate-700 relative">
          <div className="relative" ref={dropdownRef}>
            <button
              onClick={() => setShowReplyPermissionDropdown(!showReplyPermissionDropdown)}
              className="text-sm text-gray-600 dark:text-gray-300 hover:text-gray-800 dark:hover:text-gray-100 transition-colors"
            >
              {replyPermission === 'ANYONE' && 'Anyone can reply & quote'}
              {replyPermission === 'FOLLOWERS' && 'Your followers can reply & quote'}
              {replyPermission === 'FOLLOWING' && 'Profiles you follow can reply & quote'}
              {replyPermission === 'MENTIONED_ONLY' && 'Mentioned only can reply & quote'}
            </button>
            
            {/* Reply Permission Dropdown */}
            {showReplyPermissionDropdown && (
              <div className="absolute top-full left-0 mt-2 bg-white dark:bg-slate-800 rounded-lg shadow-lg border border-gray-200 dark:border-slate-700 py-1 min-w-[200px] z-50">
                <button
                  onClick={() => {
                    setReplyPermission('ANYONE');
                    setShowReplyPermissionDropdown(false);
                  }}
                  className={`w-full px-3 py-1.5 text-left text-xs text-gray-800 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-slate-700 transition-colors ${
                    replyPermission === 'ANYONE' ? 'bg-gray-100 dark:bg-slate-600' : ''
                  }`}
                >
                  Anyone
                </button>
                <button
                  onClick={() => {
                    setReplyPermission('FOLLOWERS');
                    setShowReplyPermissionDropdown(false);
                  }}
                  className={`w-full px-3 py-1.5 text-left text-xs text-gray-800 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-slate-700 transition-colors ${
                    replyPermission === 'FOLLOWERS' ? 'bg-gray-100 dark:bg-slate-600' : ''
                  }`}
                >
                  Your followers
                </button>
                <button
                  onClick={() => {
                    setReplyPermission('FOLLOWING');
                    setShowReplyPermissionDropdown(false);
                  }}
                  className={`w-full px-3 py-1.5 text-left text-xs text-gray-800 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-slate-700 transition-colors ${
                    replyPermission === 'FOLLOWING' ? 'bg-gray-100 dark:bg-slate-600' : ''
                  }`}
                >
                  Profiles you follow
                </button>
                <button
                  onClick={() => {
                    setReplyPermission('MENTIONED_ONLY');
                    setShowReplyPermissionDropdown(false);
                  }}
                  className={`w-full px-3 py-1.5 text-left text-xs text-gray-800 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-slate-700 transition-colors ${
                    replyPermission === 'MENTIONED_ONLY' ? 'bg-gray-100 dark:bg-slate-600' : ''
                  }`}
                >
                  Mentioned only
                </button>
              </div>
            )}
          </div>
          
          <button
            onClick={handleSubmit}
            disabled={(!content.trim() && selectedMedia.length === 0) || isPosting}
            className={`px-6 py-2 rounded-full text-sm font-medium transition-colors ${
              (content.trim() || selectedMedia.length > 0) && !isPosting
                ? 'bg-black dark:bg-white text-white dark:text-black hover:bg-gray-800 dark:hover:bg-gray-200'
                : 'bg-gray-200 dark:bg-slate-600 text-gray-400 dark:text-gray-500 cursor-not-allowed'
            }`}
          >
            {isPosting ? 'Posting...' : 'Post'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default NewThreadModal;
