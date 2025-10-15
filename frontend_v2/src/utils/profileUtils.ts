/**
 * Utility functions for profile-related operations
 */

/**
 * Generate initials from a user's name
 * @param name - Full name of the user
 * @returns First letter of first name and first letter of last name in uppercase
 */
export const generateInitials = (name: string): string => {
  if (!name || name.trim() === '') {
    return 'உ'; // Default fallback
  }

  const nameParts = name.trim().split(' ');
  if (nameParts.length === 1) {
    // Only one name, return first letter
    return nameParts[0].charAt(0).toUpperCase();
  }

  // Multiple names, return first letter of first and last name
  const firstName = nameParts[0];
  const lastName = nameParts[nameParts.length - 1];
  
  return (firstName.charAt(0) + lastName.charAt(0)).toUpperCase();
};

/**
 * Get a consistent background color for profile initials based on name
 * @param name - Full name of the user
 * @returns CSS class for background color
 */
export const getInitialsBackgroundColor = (name: string): string => {
  const colors = [
    'bg-red-500',
    'bg-blue-500', 
    'bg-green-500',
    'bg-yellow-500',
    'bg-purple-500',
    'bg-pink-500',
    'bg-indigo-500',
    'bg-teal-500',
    'bg-orange-500',
    'bg-cyan-500'
  ];
  
  if (!name) return colors[0];
  
  // Use name to generate consistent color
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash);
  }
  
  return colors[Math.abs(hash) % colors.length];
};
