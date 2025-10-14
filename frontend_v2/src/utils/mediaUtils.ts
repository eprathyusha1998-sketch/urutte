/**
 * Utility functions for handling media URLs
 */

/**
 * Get the full URL for a media file
 * @param mediaUrl - The media URL from the backend
 * @returns The complete URL for the media file
 */
export const getMediaUrl = (mediaUrl: string): string => {
  if (mediaUrl.startsWith('http')) {
    return mediaUrl;
  }
  
  // Get the base URL without the /api suffix
  const baseUrl = process.env.REACT_APP_API_URL?.replace('/api', '');
  
  if (!baseUrl) {
    console.error('REACT_APP_API_URL is not defined. Cannot construct media URL.');
    return mediaUrl; // Return original URL as fallback
  }
  
  // Ensure the mediaUrl starts with / if it doesn't already
  const cleanUrl = mediaUrl.startsWith('/') ? mediaUrl : `/${mediaUrl}`;
  
  return `${baseUrl}${cleanUrl}`;
};

/**
 * Get the full URL for a profile image
 * @param profileUrl - The profile image URL from the backend
 * @returns The complete URL for the profile image
 */
export const getProfileImageUrl = (profileUrl: string): string => {
  if (profileUrl.startsWith('http')) {
    return profileUrl;
  }
  
  // Get the base URL without the /api suffix
  const baseUrl = process.env.REACT_APP_API_URL?.replace('/api', '');
  
  if (!baseUrl) {
    console.error('REACT_APP_API_URL is not defined. Cannot construct profile image URL.');
    return profileUrl; // Return original URL as fallback
  }
  
  // Ensure the profileUrl starts with / if it doesn't already
  const cleanUrl = profileUrl.startsWith('/') ? profileUrl : `/${profileUrl}`;
  
  return `${baseUrl}${cleanUrl}`;
};
