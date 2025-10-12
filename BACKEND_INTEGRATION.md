# Backend Integration Guide

## Overview
The `frontend_v2` is now fully integrated with the Spring Boot backend for authentication and real-time features.

## Environment Setup

### Frontend Environment Variables
Create a `.env` file in `frontend_v2/` directory with:

```env
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_WS_URL=http://localhost:8080/ws
```

### Backend Configuration
Make sure your `backend/src/main/resources/application.properties` has:

```properties
# Google OAuth2 Configuration
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8080/login/oauth2/code/google
spring.security.oauth2.client.registration.google.scope=profile,email

# Frontend URL for OAuth redirect
app.oauth2.redirect-uri=http://localhost:3001/login?token={token}

# CORS Configuration
app.cors.allowed-origins=http://localhost:3001
```

## Features Implemented

### 1. Authentication (LoginPage)
- **Google OAuth2 Integration**: Users can login with Google
- **Auto-redirect**: Already logged-in users are redirected to feed
- **Token handling**: OAuth token is received via URL parameter and stored in localStorage
- **Error handling**: Display error messages for failed authentication

### 2. Feed Page Integration
- **User Authentication Check**: Redirects to login if not authenticated
- **Real-time Data Fetching**:
  - Current user profile
  - Posts feed with pagination
  - Notification count (unread)
  - Message count (unread)
- **WebSocket Connection**: Real-time notifications and messages
- **Post Creation**: Create new posts with content
- **Like Posts**: Like/unlike posts with real-time count updates
- **User Profile Display**: Shows current user info in sidebar and header
- **Logout Functionality**: Clear session and redirect to login

### 3. API Service (`services/api.ts`)
Centralized API calls with:
- **Axios interceptors**: Auto-inject auth token
- **Error handling**: Auto-redirect to login on 401 errors
- **Endpoints**:
  - Auth: getCurrentUser, logout
  - Posts: getFeed, createPost, likePost, getComments, addComment
  - Users: getProfile, followUser, searchUsers
  - Notifications: getNotifications, getUnreadCount, markAsRead
  - Messages: getConversation, getUnreadCount, markAsRead

### 4. WebSocket Service (`services/websocket.ts`)
Real-time communication with:
- **STOMP over SockJS**: WebSocket protocol
- **Subscriptions**:
  - `/user/{userId}/queue/notifications` - Real-time notifications
  - `/user/{userId}/queue/messages` - Real-time messages
- **Send Messages**: `/app/chat.sendMessage` endpoint

## Running the Application

### 1. Start Backend
```bash
cd backend
./gradlew bootRun
```
Backend will run on: `http://localhost:8080`

### 2. Start Frontend
```bash
cd frontend_v2
npm install  # if not already installed
npm start
```
Frontend will run on: `http://localhost:3001`

### 3. Access the Application
1. Open browser: `http://localhost:3001`
2. You'll be redirected to login page
3. Click "Sign in" or any social login button (currently redirects to Google OAuth)
4. After successful authentication, you'll be redirected to the feed page

## Authentication Flow

1. **User clicks login** → Frontend redirects to `/oauth2/authorization/google`
2. **Google authentication** → User logs in with Google
3. **Backend processes OAuth** → Creates/updates user in database
4. **Backend redirects** → `http://localhost:3001/login?token=JWT_TOKEN`
5. **Frontend stores token** → Saves to localStorage
6. **Frontend redirects to feed** → `/feed` page with authenticated user

## Data Flow

### Creating a Post
1. User types content in "Create Status" modal
2. Click "Create" button
3. Frontend sends POST to `/api/posts` with content
4. Backend saves post and returns created post
5. Frontend adds new post to the top of feed
6. Modal closes automatically

### Liking a Post
1. User clicks heart icon on a post
2. Frontend sends POST to `/api/posts/{postId}/like`
3. Backend toggles like and returns updated post
4. Frontend updates the post in state with new like count

### Real-time Notifications
1. WebSocket connects on feed page load
2. Subscribes to `/user/{userId}/queue/notifications`
3. When notification arrives, increment notification count badge
4. Display notification in UI (can be extended)

## Troubleshooting

### CORS Errors
- Ensure `app.cors.allowed-origins=http://localhost:3001` in backend
- Check `WebConfig.java` CORS configuration
- Clear browser cache and restart both servers

### Authentication Issues
- Verify Google OAuth credentials in `application.properties`
- Check Google Cloud Console redirect URIs include `http://localhost:8080/login/oauth2/code/google`
- Ensure backend redirect URI matches frontend port: `http://localhost:3001/login?token={token}`

### WebSocket Connection Fails
- Check backend WebSocket endpoint: `http://localhost:8080/ws`
- Verify CORS configuration allows WebSocket connections
- Check browser console for WebSocket errors

### API Calls Return 401
- Check if token is stored in localStorage: `localStorage.getItem('access_token')`
- Verify token is valid (not expired)
- Check backend JWT configuration

## Next Steps

### Additional Features to Implement
1. **Comments**: Full comment thread functionality
2. **Reposts**: Share/repost functionality
3. **User Profiles**: View and edit user profiles
4. **Follow System**: Follow/unfollow users
5. **Search**: Search users and posts
6. **Media Upload**: Upload images/videos for posts
7. **Notifications Panel**: Full notification center UI
8. **Messages Page**: Complete messaging interface

### Security Enhancements
1. Implement token refresh mechanism
2. Add CSRF protection
3. Implement rate limiting
4. Add input validation and sanitization

### Performance Optimizations
1. Implement infinite scroll for feed
2. Add caching for user data
3. Optimize WebSocket reconnection logic
4. Implement lazy loading for images

## API Endpoints Reference

### Authentication
- `GET /api/users/me` - Get current user
- `POST /api/auth/logout` - Logout user

### Posts
- `GET /api/posts/feed?page=0&size=20` - Get feed
- `POST /api/posts` - Create post
- `POST /api/posts/{postId}/like` - Like/unlike post
- `GET /api/posts/{postId}/comments` - Get comments
- `POST /api/posts/{postId}/comments` - Add comment

### Notifications
- `GET /api/notifications?page=0&size=20` - Get notifications
- `GET /api/notifications/unread/count` - Get unread count
- `PUT /api/notifications/{id}/read` - Mark as read
- `PUT /api/notifications/read-all` - Mark all as read

### Messages
- `GET /api/messages/conversation/{userId}` - Get conversation
- `GET /api/messages/unread/count` - Get unread count
- `PUT /api/messages/read/{senderId}` - Mark as read

### WebSocket
- `CONNECT /ws` - Connect to WebSocket
- `SUBSCRIBE /user/{userId}/queue/notifications` - Subscribe to notifications
- `SUBSCRIBE /user/{userId}/queue/messages` - Subscribe to messages
- `SEND /app/chat.sendMessage` - Send message

