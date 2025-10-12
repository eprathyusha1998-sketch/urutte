import { Client, IFrame, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_URL = process.env.REACT_APP_WS_URL || 'http://localhost:8080/ws';

class WebSocketService {
  private client: Client | null = null;
  private subscriptions: Map<string, StompSubscription> = new Map();
  private connectionFailed = false;

  connect(userId: string, onConnected: () => void, onError: (error: any) => void) {
    // Don't try to connect if previous connection failed
    if (this.connectionFailed) {
      console.log('WebSocket connection previously failed, skipping connection attempt');
      return;
    }
    
    // Get auth token
    const token = localStorage.getItem('access_token');
    if (!token) {
      console.log('No auth token found, skipping WebSocket connection');
      return;
    }

    console.log('Attempting WebSocket connection with token:', token.substring(0, 20) + '...');

    this.client = new Client({
      brokerURL: WS_URL,
      
      // Use SockJS for better compatibility
      webSocketFactory: () => {
        return new SockJS(WS_URL);
      },

      // Add authentication headers
      connectHeaders: {
        Authorization: `Bearer ${token}`,
        'X-User-ID': userId,
      },

      // Connection timeout
      connectionTimeout: 10000,
      
      // Reconnect settings
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      // Disable debug logging in production
      debug: (str: string) => {
        if (process.env.NODE_ENV !== 'production') {
          console.log('STOMP:', str);
        }
      },

      onConnect: (frame: IFrame) => {
        console.log('WebSocket connected successfully');
        console.log('Connection frame:', frame);
        console.log('Connection headers:', frame.headers);
        onConnected();
      },

      onStompError: (frame: IFrame) => {
        console.error('STOMP error:', frame.headers['message']);
        console.error('Error details:', frame.body);
        this.connectionFailed = true;
        onError(frame);
      },

      onWebSocketError: (event: Event) => {
        console.warn('WebSocket connection error:', event);
        console.log('This is expected if WebSocket server is not fully configured');
        this.connectionFailed = true;
        // Don't call onError to avoid showing error to user
        // The app will use REST API as fallback
      },

      onDisconnect: () => {
        console.log('WebSocket disconnected');
      },
    });

    this.client.activate();
  }

  disconnect() {
    if (this.client) {
      this.subscriptions.forEach((subscription) => subscription.unsubscribe());
      this.subscriptions.clear();
      this.client.deactivate();
      this.client = null;
    }
  }

  subscribeToNotifications(userId: string, callback: (notification: any) => void) {
    if (!this.client || !this.client.connected) {
      console.error('WebSocket not connected');
      return;
    }

    const subscription = this.client.subscribe(
      `/user/${userId}/queue/notifications`,
      (message: IMessage) => {
        const notification = JSON.parse(message.body);
        callback(notification);
      }
    );

    this.subscriptions.set('notifications', subscription);
  }

  subscribeToMessages(userId: string, callback: (message: any) => void) {
    if (!this.client || !this.client.connected) {
      console.error('WebSocket not connected');
      return;
    }

    const subscription = this.client.subscribe(
      `/user/${userId}/queue/messages`,
      (message: IMessage) => {
        const msg = JSON.parse(message.body);
        callback(msg);
      }
    );

    this.subscriptions.set('messages', subscription);
  }

  sendMessage(senderId: string, receiverId: string, content: string, messageType = 'text', mediaUrl?: string) {
    if (!this.client || !this.client.connected) {
      console.error('WebSocket not connected');
      return;
    }

    this.client.publish({
      destination: '/app/chat.sendMessage',
      body: JSON.stringify({
        senderId,
        receiverId,
        content,
        messageType,
        mediaUrl,
      }),
    });
  }

  isConnected(): boolean {
    return this.client !== null && this.client.connected;
  }

  // Test WebSocket connection
  testConnection(): Promise<boolean> {
    return new Promise((resolve) => {
      if (this.connectionFailed) {
        resolve(false);
        return;
      }

      const token = localStorage.getItem('access_token');
      if (!token) {
        resolve(false);
        return;
      }

      const testClient = new Client({
        brokerURL: WS_URL,
        webSocketFactory: () => new SockJS(WS_URL),
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        connectionTimeout: 5000,
        onConnect: () => {
          console.log('WebSocket test connection successful');
          testClient.deactivate();
          resolve(true);
        },
        onStompError: (frame) => {
          console.error('WebSocket test connection failed:', frame);
          testClient.deactivate();
          resolve(false);
        },
        onWebSocketError: (event) => {
          console.warn('WebSocket test connection error:', event);
          testClient.deactivate();
          resolve(false);
        },
      });

      testClient.activate();
    });
  }
}

export const webSocketService = new WebSocketService();
export default webSocketService;

