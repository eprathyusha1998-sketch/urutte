import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { IonIcon } from '@ionic/react';
import Sidebar from '../components/Sidebar';
import {
  menuOutline,
  closeOutline,
  search,
  addOutline,
  notificationsOutline,
  personOutline,
  homeOutline,
  chatbubbleOutline,
  videocamOutline,
  calendarOutline,
  documentTextOutline,
  peopleOutline,
  storefrontOutline,
  libraryOutline,
  gameControllerOutline,
  settingsOutline,
  checkmarkOutline,
  volumeMuteOutline,
  chevronBackOutline,
  callOutline,
  informationCircleOutline,
  heartOutline,
  image,
  imagesOutline,
  documentText,
  giftOutline,
  happyOutline,
  sendOutline,
  close,
  notificationsOffOutline,
  flagOutline,
  stopCircleOutline,
  trashOutline
} from 'ionicons/icons';
import { authApi, usersApi } from '../services/api';
import { simpleChatService, Message, User } from '../services/simpleChat';


interface Conversation {
  user: User;
  lastMessage: Message;
  unreadCount: number;
}

const MessagesPage: React.FC = () => {
  const navigate = useNavigate();
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [selectedConversation, setSelectedConversation] = useState<Conversation | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [showRightPanel, setShowRightPanel] = useState(false);
  const [followers, setFollowers] = useState<User[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [showFollowers, setShowFollowers] = useState(false);
  const [searchResults, setSearchResults] = useState<User[]>([]);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const searchTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    const initializeUser = async () => {
      try {
        const user = await authApi.getCurrentUser();
        setCurrentUser(user);
      } catch (error) {
        console.error('Error getting current user:', error);
      }
    };

    initializeUser();
  }, []);

  useEffect(() => {
    if (currentUser) {
      loadConversations();
      loadFollowers();
      connectToChatStream();
    }
  }, [currentUser]);

  useEffect(() => {
    if (selectedConversation) {
      loadMessages(selectedConversation.user.id);
    }
  }, [selectedConversation]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // Cleanup timeout on unmount
  useEffect(() => {
    return () => {
      if (searchTimeoutRef.current) {
        clearTimeout(searchTimeoutRef.current);
      }
    };
  }, []);

  const loadConversations = async () => {
    if (!currentUser) return;
    
    try {
      const partners = await simpleChatService.getConversationPartners();
      // Transform User[] to Conversation[] with placeholder data
      const conversations: Conversation[] = partners.slice(0, 10).map(user => ({
        user,
        lastMessage: {
          id: 0,
          content: '',
          senderId: '',
          senderName: '',
          senderAvatar: '',
          receiverId: '',
          receiverName: '',
          receiverAvatar: '',
          messageType: 'text',
          isRead: true,
          createdAt: new Date().toISOString()
        },
        unreadCount: 0
      }));
      setConversations(conversations);
    } catch (error) {
      console.error('Error loading conversations:', error);
    }
  };

  const loadFollowers = async () => {
    if (!currentUser) return;
    
    try {
      const followersData = await usersApi.getFollowers(currentUser.id);
      setFollowers(followersData);
    } catch (error) {
      console.error('Error loading followers:', error);
    }
  };

  const loadMessages = async (userId: string) => {
    try {
      const messages = await simpleChatService.getConversation(userId);
      setMessages(messages);
    } catch (error) {
      console.error('Error loading messages:', error);
    }
  };

  const connectToChatStream = () => {
    if (!currentUser) return;

    console.log('Connecting to chat stream...');
    simpleChatService.connectToStream((message) => {
      console.log('New message received via SSE:', message);
      handleNewMessage(message);
    });
  };

  const handleNewMessage = (message: any) => {
    setMessages(prev => [...prev, message]);

    setConversations(prev => prev.map(conv => {
      if (conv.user.id === message.senderId || conv.user.id === message.receiverId) {
        return {
          ...conv,
          lastMessage: message,
          unreadCount: message.receiverId === currentUser?.id ? conv.unreadCount + 1 : conv.unreadCount
        };
      }
      return conv;
    }));
  };

  const handleSendMessage = async () => {
    if (!newMessage.trim() || !selectedConversation || !currentUser) return;

    const messageContent = newMessage.trim();
    setNewMessage(''); // Clear input immediately for better UX

    // Create temporary message for immediate UI update
    const tempMessage: Message = {
      id: Date.now(),
      content: messageContent,
      senderId: currentUser.id,
      senderName: currentUser.name,
      senderAvatar: currentUser.picture,
      receiverId: selectedConversation.user.id,
      receiverName: selectedConversation.user.name,
      receiverAvatar: selectedConversation.user.picture,
      messageType: 'text',
      isRead: false,
      createdAt: new Date().toISOString(),
    };

    // Add message to UI immediately
    setMessages(prev => [...prev, tempMessage]);

    try {
      // Send message via simple chat service
      console.log('Sending message via simple chat service');
      const sentMessage = await simpleChatService.sendMessage(
        selectedConversation.user.id,
        messageContent
      );
      
      // Update the temporary message with the real one from server
      setMessages(prev => prev.map(msg => 
        msg.id === tempMessage.id ? sentMessage : msg
      ));

      // Update conversation list with new last message
      setConversations(prev => prev.map(conv => 
        conv.user.id === selectedConversation.user.id 
          ? { ...conv, lastMessage: tempMessage }
          : conv
      ));

    } catch (error) {
      console.error('Error sending message:', error);
      // Remove the temporary message if sending failed
      setMessages(prev => prev.filter(msg => msg.id !== tempMessage.id));
      setNewMessage(messageContent); // Restore the message content
      alert('Failed to send message. Please try again.');
    }
  };

  const scrollToBottom = () => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  };

  const formatTime = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString();
  };

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const query = e.target.value;
    setSearchQuery(query);
    setShowFollowers(query.length > 0);
    
    // Clear previous timeout
    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }
    
    // If we have a search query, search for users with debounce
    if (query.length > 0) {
      searchTimeoutRef.current = setTimeout(async () => {
        try {
          console.log('Searching for users with query:', query);
          const searchResults = await usersApi.searchUsers(query);
          console.log('Search results:', searchResults);
          setSearchResults(searchResults);
        } catch (error) {
          console.error('Error searching users:', error);
          setSearchResults([]);
        }
      }, 300); // 300ms debounce
    } else {
      setSearchResults([]);
    }
  };

  // Combine followers and search results, removing duplicates
  const allUsers = [...followers, ...searchResults.filter(user => 
    !followers.some(follower => follower.id === user.id)
  )];

  const filteredFollowers = allUsers.filter(user =>
    user.name.toLowerCase().includes(searchQuery.toLowerCase()) && 
    user.id !== currentUser?.id // Don't show current user in search
  );

  const startConversation = async (follower: User) => {
    try {
      // Create a new conversation with the follower
      const newConversation: Conversation = {
        user: follower,
        lastMessage: {
          id: 0,
          content: 'Start a conversation...',
          senderId: currentUser!.id,
          senderName: currentUser!.name,
          senderAvatar: currentUser!.picture,
          receiverId: follower.id,
          receiverName: follower.name,
          receiverAvatar: follower.picture,
          messageType: 'text',
          isRead: false,
          createdAt: new Date().toISOString(),
        },
        unreadCount: 0
      };
      
      setSelectedConversation(newConversation);
      setSearchQuery('');
      setShowFollowers(false);
      
      // Load messages for this conversation
      await loadMessages(follower.id);
    } catch (error) {
      console.error('Error starting conversation:', error);
    }
  };

  const selectConversation = async (conversation: Conversation) => {
    setSelectedConversation(conversation);
    // Load messages for this conversation
    await loadMessages(conversation.user.id);
  };

  if (!currentUser) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-slate-900">

      {/* Sidebar */}
      <Sidebar 
        currentUser={currentUser}
        onToggleTheme={() => {
          document.documentElement.classList.toggle('dark');
        }}
        isDarkMode={document.documentElement.classList.contains('dark')}
        onLogout={handleLogout}
      />

      {/* Main Content */}
      <main id="site__main" className="2xl:ml-[--w-side] xl:ml-[--w-side-sm] p-2.5 h-[calc(100vh-var(--m-top))] mt-[--m-top]">
        <div className="relative overflow-hidden border -m-2.5 dark:border-slate-700 rounded-lg">
          <div className="flex bg-white dark:bg-slate-800 h-full">
            
            {/* Chat sidebar */}
            <div className="w-[360px] relative border-r dark:border-slate-700">
              <div className="h-full bg-white dark:bg-slate-800">
                
                {/* Chat heading */}
                <div className="p-4 border-b dark:border-slate-700">
                  <div className="flex items-center justify-between">
                    <h2 className="text-2xl font-bold text-black dark:text-white">Chats</h2>
                    
                    <div className="flex items-center gap-2.5">
                      <button className="group">
                        <IonIcon icon={settingsOutline} className="text-2xl flex group-aria-expanded:rotate-180" />
                      </button>
                      <button className="">
                        <IonIcon icon={checkmarkOutline} className="text-2xl flex" />
                      </button>
                      <button type="button" className="md:hidden">
                        <IonIcon icon={chevronBackOutline} />
                      </button>
                    </div>
                  </div>
                  
                  {/* Search */}
                  <div className="relative mt-4">
                    <div className="absolute left-3 top-1/2 -translate-y-1/2 flex">
                      <IonIcon icon={search} className="text-xl" />
                    </div>
                    <input
                      type="text"
                      placeholder="Search followers to chat"
                      value={searchQuery}
                      onChange={handleSearchChange}
                      className="w-full pl-10 py-2 rounded-lg border-0 outline-none bg-gray-100 dark:bg-slate-700 dark:text-white"
                    />
                  </div>
                </div>
                
                {/* Users list */}
                <div className="space-y-2 p-2 overflow-y-auto h-[calc(100vh-204px)]">
                  {showFollowers ? (
                    // Show filtered followers when searching
                    filteredFollowers.map((follower) => (
                      <a
                        key={follower.id}
                        href="#"
                        onClick={(e) => {
                          e.preventDefault();
                          startConversation(follower);
                        }}
                        className="relative flex items-center gap-4 p-2 duration-200 rounded-xl hover:bg-gray-100 dark:hover:bg-slate-600"
                      >
                        <div className="relative w-14 h-14 shrink-0">
                          <img
                            src={follower.picture}
                            alt=""
                            className="object-cover w-full h-full rounded-full"
                          />
                          <div className="w-4 h-4 absolute bottom-0 right-0 bg-green-500 rounded-full border border-white dark:border-slate-800"></div>
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 mb-1.5">
                            <div className="mr-auto text-sm text-black dark:text-white font-medium">
                              {follower.name}
                            </div>
                            <div className="text-xs font-light text-gray-500 dark:text-white/70">
                              Follower
                            </div>
                          </div>
                          <div className="font-medium overflow-hidden text-ellipsis text-sm whitespace-nowrap text-gray-600 dark:text-gray-300">
                            Click to start conversation
                          </div>
                        </div>
                      </a>
                    ))
                  ) : (
                    // Show existing conversations
                    conversations.map((conversation) => (
                      <a
                        key={conversation.user.id}
                        href="#"
                        onClick={(e) => {
                          e.preventDefault();
                          selectConversation(conversation);
                        }}
                        className={`relative flex items-center gap-4 p-2 duration-200 rounded-xl hover:bg-gray-100 dark:hover:bg-slate-600 ${
                          selectedConversation?.user.id === conversation.user.id ? 'bg-blue-50 dark:bg-blue-900/20' : ''
                        }`}
                      >
                        <div className="relative w-14 h-14 shrink-0">
                          <img
                            src={conversation.user.picture}
                            alt=""
                            className="object-cover w-full h-full rounded-full"
                          />
                          <div className="w-4 h-4 absolute bottom-0 right-0 bg-green-500 rounded-full border border-white dark:border-slate-800"></div>
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 mb-1.5">
                            <div className="mr-auto text-sm text-black dark:text-white font-medium">
                              {conversation.user.name}
                            </div>
                            <div className="text-xs font-light text-gray-500 dark:text-white/70">
                              {formatTime(conversation.lastMessage.createdAt)}
                            </div>
                            {conversation.unreadCount > 0 && (
                              <div className="w-2.5 h-2.5 bg-blue-600 rounded-full dark:bg-slate-700"></div>
                            )}
                          </div>
                          <div className="font-medium overflow-hidden text-ellipsis text-sm whitespace-nowrap text-gray-600 dark:text-gray-300">
                            {conversation.lastMessage.content}
                          </div>
                        </div>
                      </a>
                    ))
                  )}
                </div>
              </div>
            </div>
            
            {/* Message center */}
            <div className="flex-1">
              
              {/* Chat heading */}
              <div className="flex items-center justify-between gap-2 px-6 py-3.5 z-10 border-b dark:border-slate-700">
                <div className="flex items-center sm:gap-4 gap-2">
                  <button type="button" className="md:hidden">
                    <IonIcon icon={chevronBackOutline} className="text-2xl -ml-4" />
                  </button>
                  
                  {selectedConversation ? (
                    <>
                      <div className="relative cursor-pointer max-md:hidden">
                        <img
                          src={selectedConversation.user.picture}
                          alt=""
                          className="w-8 h-8 rounded-full shadow"
                        />
                        <div className="w-2 h-2 bg-teal-500 rounded-full absolute right-0 bottom-0 m-px"></div>
                      </div>
                      <div className="cursor-pointer">
                        <div className="text-base font-bold text-gray-800 dark:text-white">
                          {selectedConversation.user.name}
                        </div>
                        <div className="text-xs text-green-500 font-semibold">Online</div>
                      </div>
                    </>
                  ) : (
                    <div className="text-base font-bold text-gray-800 dark:text-white">
                      Select a conversation
                    </div>
                  )}
                </div>
                
                <div className="flex items-center gap-2">
                  <button type="button" className="p-1.5 rounded-full hover:bg-gray-100 dark:hover:bg-slate-600">
                    <IonIcon icon={callOutline} className="w-6 h-6" />
                  </button>
                  <button type="button" className="p-1.5 rounded-full hover:bg-gray-100 dark:hover:bg-slate-600">
                    <IonIcon icon={videocamOutline} className="w-6 h-6" />
                  </button>
                  <button
                    type="button"
                    className="p-1.5 rounded-full hover:bg-gray-100 dark:hover:bg-slate-600"
                    onClick={() => setShowRightPanel(!showRightPanel)}
                  >
                    <IonIcon icon={informationCircleOutline} className="w-6 h-6" />
                  </button>
                </div>
              </div>
              
              {/* Chat messages */}
              <div className="w-full p-5 py-10 overflow-y-auto h-[calc(100vh-204px)]">
                {selectedConversation ? (
                  <>
                    {messages.length === 0 ? (
                      <div className="py-10 text-center text-sm lg:pt-8">
                        <img
                          src={selectedConversation.user.picture}
                          className="w-24 h-24 rounded-full mx-auto mb-3"
                          alt=""
                        />
                        <div className="mt-8">
                          <div className="md:text-xl text-base font-medium text-black dark:text-white">
                            {selectedConversation.user.name}
                          </div>
                          <div className="text-gray-500 text-sm dark:text-white/80">
                            @{selectedConversation.user.name.toLowerCase().replace(/\s+/g, '')}
                          </div>
                        </div>
                        <div className="mt-3.5">
                          <a
                            href="#"
                            className="inline-block rounded-lg px-4 py-1.5 text-sm font-semibold bg-gray-100 dark:bg-slate-700 text-gray-700 dark:text-white"
                          >
                            View profile
                          </a>
                        </div>
                      </div>
                    ) : (
                      <div className="text-sm font-medium space-y-6">
                        {messages.map((message, index) => {
                          const isCurrentUser = message.senderId === currentUser.id;
                          const showDate = index === 0 || 
                            formatDate(messages[index - 1].createdAt) !== formatDate(message.createdAt);
                          
                          return (
                            <div key={message.id}>
                              {showDate && (
                                <div className="flex justify-center">
                                  <div className="font-medium text-gray-500 text-sm dark:text-white/70">
                                    {formatDate(message.createdAt)}
                                  </div>
                                </div>
                              )}
                              
                              {isCurrentUser ? (
                                <div className="flex gap-2 flex-row-reverse items-end">
                                  <img
                                    src={message.senderAvatar}
                                    alt=""
                                    className="w-5 h-5 rounded-full shadow"
                                  />
                                  <div className="px-4 py-2 rounded-[20px] max-w-sm bg-gradient-to-tr from-sky-500 to-blue-500 text-white shadow">
                                    {message.content}
                                  </div>
                                </div>
                              ) : (
                                <div className="flex gap-3">
                                  <img
                                    src={message.senderAvatar}
                                    alt=""
                                    className="w-9 h-9 rounded-full shadow"
                                  />
                                  <div className="px-4 py-2 rounded-[20px] max-w-sm bg-gray-100 dark:bg-slate-700 text-gray-800 dark:text-white">
                                    {message.content}
                                  </div>
                                </div>
                              )}
                            </div>
                          );
                        })}
                        <div ref={messagesEndRef} />
                      </div>
                    )}
                  </>
                ) : (
                  <div className="flex items-center justify-center h-full">
                    <div className="text-center">
                      <IonIcon icon={chatbubbleOutline} className="text-6xl text-gray-300 dark:text-gray-600 mb-4" />
                      <h3 className="text-lg font-medium text-gray-500 dark:text-gray-400 mb-2">
                        No conversation selected
                      </h3>
                      <p className="text-sm text-gray-400 dark:text-gray-500">
                        Choose a conversation from the sidebar to start messaging
                      </p>
                    </div>
                  </div>
                )}
              </div>
              
              {/* Message input area */}
              {selectedConversation && (
                <div className="flex items-center md:gap-4 gap-2 md:p-3 p-2 overflow-hidden border-t dark:border-slate-700">
                  <div className="flex items-center gap-2 h-full dark:text-white -mt-1.5">
                    <button type="button" className="shrink-0">
                      <IonIcon className="text-3xl flex" icon={addOutline} />
                    </button>
                    <button type="button" className="shrink-0">
                      <IonIcon className="text-3xl flex" icon={happyOutline} />
                    </button>
                  </div>
                  
                  <div className="relative flex-1">
                    <textarea
                      placeholder="Write your message"
                      rows={1}
                      value={newMessage}
                      onChange={(e) => setNewMessage(e.target.value)}
                      onKeyPress={(e) => {
                        if (e.key === 'Enter' && !e.shiftKey) {
                          e.preventDefault();
                          handleSendMessage();
                        }
                      }}
                      className="w-full resize-none bg-gray-100 dark:bg-slate-700 rounded-full px-4 p-2 border-0 outline-none text-gray-800 dark:text-white"
                    />
                    
                    <button
                      type="button"
                      onClick={handleSendMessage}
                      className="text-white shrink-0 p-2 absolute right-0.5 top-0"
                    >
                      <IonIcon className="text-xl flex" icon={sendOutline} />
                    </button>
                  </div>
                  
                  <button type="button" className="flex h-full dark:text-white">
                    <IonIcon className="text-3xl flex -mt-3" icon={heartOutline} />
                  </button>
                </div>
              )}
            </div>
            
            {/* Right profile panel */}
            {showRightPanel && selectedConversation && (
              <div className="w-[360px] border-l shadow-lg h-screen bg-white absolute right-0 top-0 z-50 dark:bg-slate-800 dark:border-slate-700">
                <div className="w-full h-1.5 bg-gradient-to-r to-purple-500 via-red-500 from-pink-500 -mt-px"></div>
                
                <div className="py-10 text-center text-sm pt-20">
                  <img
                    src={selectedConversation.user.picture}
                    className="w-24 h-24 rounded-full mx-auto mb-3"
                    alt=""
                  />
                  <div className="mt-8">
                    <div className="md:text-xl text-base font-medium text-black dark:text-white">
                      {selectedConversation.user.name}
                    </div>
                    <div className="text-gray-500 text-sm mt-1 dark:text-white/80">
                      @{selectedConversation.user.name.toLowerCase().replace(/\s+/g, '')}
                    </div>
                  </div>
                  <div className="mt-5">
                    <a
                      href="#"
                      className="inline-block rounded-full px-4 py-1.5 text-sm font-semibold bg-gray-100 dark:bg-slate-700 text-gray-700 dark:text-white"
                    >
                      View profile
                    </a>
                  </div>
                </div>
                
                <hr className="opacity-80 dark:border-slate-700" />
                
                <ul className="text-base font-medium p-3">
                  <li>
                    <div className="flex items-center gap-5 rounded-md p-3 w-full hover:bg-gray-100 dark:hover:bg-slate-600">
                      <IonIcon icon={notificationsOffOutline} className="text-2xl" />
                      Mute Notification
                      <label className="switch cursor-pointer ml-auto">
                        <input type="checkbox" defaultChecked />
                        <span className="switch-button relative"></span>
                      </label>
                    </div>
                  </li>
                  <li>
                    <button type="button" className="flex items-center gap-5 rounded-md p-3 w-full hover:bg-gray-100 dark:hover:bg-slate-600 text-gray-700 dark:text-white">
                      <IonIcon icon={flagOutline} className="text-2xl" />
                      Report
                    </button>
                  </li>
                  <li>
                    <button type="button" className="flex items-center gap-5 rounded-md p-3 w-full hover:bg-gray-100 dark:hover:bg-slate-600 text-gray-700 dark:text-white">
                      <IonIcon icon={settingsOutline} className="text-2xl" />
                      Ignore messages
                    </button>
                  </li>
                  <li>
                    <button type="button" className="flex items-center gap-5 rounded-md p-3 w-full hover:bg-gray-100 dark:hover:bg-slate-600 text-gray-700 dark:text-white">
                      <IonIcon icon={stopCircleOutline} className="text-2xl" />
                      Block
                    </button>
                  </li>
                  <li>
                    <button type="button" className="flex items-center gap-5 rounded-md p-3 w-full hover:bg-red-50 text-red-500 dark:hover:bg-red-900/20">
                      <IonIcon icon={trashOutline} className="text-2xl" />
                      Delete Chat
                    </button>
                  </li>
                </ul>
                
                <button
                  type="button"
                  className="absolute top-0 right-0 m-4 p-2 bg-gray-100 dark:bg-slate-700 rounded-full"
                  onClick={() => setShowRightPanel(false)}
                >
                  <IonIcon icon={close} className="text-2xl flex" />
                </button>
              </div>
            )}
            
            {/* Right panel overlay */}
            {showRightPanel && (
              <div
                className="bg-slate-100/40 backdrop-blur absolute w-full h-full dark:bg-slate-800/40"
                onClick={() => setShowRightPanel(false)}
              />
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default MessagesPage;