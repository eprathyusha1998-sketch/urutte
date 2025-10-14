// JWT Token utility functions

export interface DecodedToken {
  userId: string;
  email: string;
  exp: number;
  iat: number;
}

export const decodeJWT = (token: string): DecodedToken | null => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (error) {
    console.error('Error decoding JWT:', error);
    return null;
  }
};

export const isTokenExpired = (token: string): boolean => {
  const decoded = decodeJWT(token);
  if (!decoded) return true;
  
  const currentTime = Date.now() / 1000;
  return decoded.exp < currentTime;
};

export const getStoredToken = (): string | null => {
  return localStorage.getItem('access_token');
};

export const setStoredToken = (token: string): void => {
  localStorage.setItem('access_token', token);
};

export const removeStoredToken = (): void => {
  localStorage.removeItem('access_token');
  localStorage.removeItem('user');
};

export const isAuthenticated = (): boolean => {
  const token = getStoredToken();
  if (!token) return false;
  
  return !isTokenExpired(token);
};

export const getAuthHeaders = (): HeadersInit => {
  const token = getStoredToken();
  if (!token || isTokenExpired(token)) {
    removeStoredToken();
    return {};
  }
  
  return {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  };
};
