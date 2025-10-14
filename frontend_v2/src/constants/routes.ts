// Route Constants
export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  FEED: '/feed',
  SEARCH: '/search',
  PROFILE: '/profile',
  THREAD: '/thread',
  MESSAGES: '/messages',
  LIKES: '/likes',
  NOTIFICATIONS: '/notifications',
  SETTINGS: '/settings'
} as const;

export const DYNAMIC_ROUTES = {
  THREAD_BY_ID: (id: string | number) => `/thread/${id}`,
  PROFILE_BY_USERNAME: (username: string) => `/profile/${username}`,
  HASHTAG: (tag: string) => `/hashtag/${tag}`
} as const;
