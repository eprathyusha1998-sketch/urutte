import api from './api';

export interface Message {
  id: number;
  content: string;
  senderId: string;
  senderName: string;
  senderAvatar: string;
  receiverId: string;
  receiverName: string;
  receiverAvatar: string;
  messageType: string;
  isRead: boolean;
  createdAt: string;
}

export interface User {
  id: string;
  name: string;
  picture: string;
  email: string;
}

class SimpleChatService {
  private eventSource: EventSource | null = null;
  private onMessageCallback: ((message: Message) => void) | null = null;

  // Send a message
  async sendMessage(receiverId: string, content: string, messageType = 'text', mediaUrl?: string): Promise<Message> {
    const response = await api.post('/chat/send', {
      receiverId,
      content,
      messageType,
      mediaUrl,
    });
    return response.data;
  }

  // Get conversation messages
  async getConversation(userId: string, page = 0, size = 50): Promise<Message[]> {
    const response = await api.get(`/chat/conversation/${userId}`, {
      params: { page, size },
    });
    return response.data;
  }

  // Get conversation partners
  async getConversationPartners(): Promise<User[]> {
    const response = await api.get('/chat/conversations');
    return response.data;
  }

  // Get unread message count
  async getUnreadCount(): Promise<number> {
    const response = await api.get('/chat/unread-count');
    return response.data.count;
  }

  // Mark messages as read
  async markMessagesAsRead(senderId: string): Promise<void> {
    await api.put(`/chat/mark-read/${senderId}`);
  }

  // Connect to real-time stream
  connectToStream(onMessage: (message: Message) => void): void {
    this.onMessageCallback = onMessage;
    
    // Close existing connection
    if (this.eventSource) {
      this.eventSource.close();
    }

    // Get auth token
    const token = localStorage.getItem('access_token');
    if (!token) {
      console.log('No auth token found, cannot connect to chat stream');
      return;
    }

    // Create EventSource for Server-Sent Events
    const streamUrl = `${process.env.REACT_APP_API_URL || 'http://localhost:8080'}/api/chat/stream`;
    this.eventSource = new EventSource(streamUrl, {
      withCredentials: true,
    });

    // Add authorization header (EventSource doesn't support custom headers directly)
    // We'll need to handle this differently - for now, we'll rely on cookies or session

    this.eventSource.onopen = () => {
      console.log('Connected to chat stream');
    };

    this.eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        if (data && this.onMessageCallback) {
          this.onMessageCallback(data);
        }
      } catch (error) {
        console.error('Error parsing SSE message:', error);
      }
    };

    this.eventSource.addEventListener('message', (event) => {
      try {
        const message = JSON.parse(event.data);
        if (this.onMessageCallback) {
          this.onMessageCallback(message);
        }
      } catch (error) {
        console.error('Error parsing message event:', error);
      }
    });

    this.eventSource.addEventListener('connected', (event) => {
      console.log('Chat stream connected:', event.data);
    });

    this.eventSource.onerror = (error) => {
      console.error('Chat stream error:', error);
      // Attempt to reconnect after 5 seconds
      setTimeout(() => {
        if (this.eventSource?.readyState === EventSource.CLOSED) {
          console.log('Attempting to reconnect to chat stream...');
          this.connectToStream(onMessage);
        }
      }, 5000);
    };
  }

  // Disconnect from stream
  disconnect(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }
    this.onMessageCallback = null;
  }

  // Check if connected
  isConnected(): boolean {
    return this.eventSource !== null && this.eventSource.readyState === EventSource.OPEN;
  }
}

export const simpleChatService = new SimpleChatService();

