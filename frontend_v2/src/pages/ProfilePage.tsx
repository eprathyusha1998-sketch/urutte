import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { IonIcon } from '@ionic/react';
import { 
  camera,
  checkmark,
  person,
  eye,
  eyeOff,
  createOutline
} from 'ionicons/icons';
import { generateInitials, getInitialsBackgroundColor } from '../utils/profileUtils';
import { getProfileImageUrl } from '../utils/mediaUtils';
import { useNotification } from '../contexts/NotificationContext';
import { authApi, threadsApi } from '../services/api';
import { isAuthenticated } from '../utils/auth';
import { User } from '../types';
import Sidebar from '../components/Sidebar';
import NewThreadModal from '../components/NewThreadModal';

interface ProfilePageProps {
  currentUser?: User;
  onUpdateUser?: (user: User) => void;
}

const ProfilePage: React.FC<ProfilePageProps> = ({ currentUser: propCurrentUser, onUpdateUser }) => {
  const navigate = useNavigate();
  const { showSuccess, showError } = useNotification();
  const [currentUser, setCurrentUser] = useState<User | null>(propCurrentUser || null);
  const [activeTab, setActiveTab] = useState(0);
  const [isEditing, setIsEditing] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showNewThreadModal, setShowNewThreadModal] = useState(false);
  
  // Fetch current user if not provided as prop
  useEffect(() => {
    const fetchCurrentUser = async () => {
      if (!currentUser) {
        // First check if user is authenticated
        if (!isAuthenticated()) {
          console.log('User not authenticated, redirecting to login');
          navigate('/login');
          return;
        }
        
        try {
          const userData = await authApi.getCurrentUser();
          setCurrentUser(userData);
        } catch (error) {
          console.error('Failed to fetch current user:', error);
          // User is not authenticated, redirect to login
          navigate('/login');
        }
      }
    };
    
    fetchCurrentUser();
  }, [currentUser, navigate]);
  
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    username: '',
    bio: '',
    location: '',
    website: '',
    phoneNumber: '',
    dateOfBirth: '',
    gender: '',
    isPrivate: false,
    password: '',
    confirmPassword: ''
  });
  
  // Update form data when currentUser changes
  useEffect(() => {
    if (currentUser) {
      setFormData(prev => ({
        ...prev,
        name: currentUser.name || '',
        email: currentUser.email || '',
        username: currentUser.username || '',
        bio: currentUser.bio || '',
        location: currentUser.location || '',
        website: currentUser.website || '',
        phoneNumber: currentUser.phoneNumber || '',
        dateOfBirth: currentUser.dateOfBirth || '',
        gender: currentUser.gender || '',
        isPrivate: currentUser.isPrivate || false
      }));
    }
  }, [currentUser]);

  const [profileImage, setProfileImage] = useState<File | null>(null);
  const [profileImagePreview, setProfileImagePreview] = useState<string | null>(null);

  const profileImageInputRef = useRef<HTMLInputElement>(null);

  const tabs = [
    { id: 0, name: 'Personal Info', icon: person },
    { id: 1, name: 'Profile Picture', icon: camera },
    { id: 2, name: 'Privacy', icon: eye }
  ];

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? (e.target as HTMLInputElement).checked : value
    }));
  };

  const handleProfileImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setProfileImage(file);
      const reader = new FileReader();
      reader.onload = (e) => {
        setProfileImagePreview(e.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  };


  const handleSave = async () => {
    setIsLoading(true);
    try {
      console.log('Saving profile data:', formData);
      
      // Create FormData for multipart upload
      const formDataToSend = new FormData();
      
      // Add text fields
      if (formData.name) formDataToSend.append('name', formData.name);
      if (formData.username) formDataToSend.append('username', formData.username);
      if (formData.bio) formDataToSend.append('bio', formData.bio);
      if (formData.location) formDataToSend.append('location', formData.location);
      if (formData.website) formDataToSend.append('website', formData.website);
      if (formData.phoneNumber) formDataToSend.append('phoneNumber', formData.phoneNumber);
      if (formData.dateOfBirth) formDataToSend.append('dateOfBirth', formData.dateOfBirth);
      if (formData.gender) formDataToSend.append('gender', formData.gender);
      if (formData.isPrivate !== undefined) formDataToSend.append('isPrivate', formData.isPrivate.toString());
      
      // Add image files
      if (profileImage) {
        formDataToSend.append('profileImage', profileImage);
      }
      
      console.log('FormData being sent:', Array.from(formDataToSend.entries()));
      
      // Make API call
      const response = await authApi.updateProfile(formDataToSend);
      
      console.log('API response:', response);
      
      if (response.success) {
        const updatedUser = {
          ...currentUser,
          ...response.user,
          picture: response.user.picture || currentUser?.picture
        };

        // Update local state
        setCurrentUser(updatedUser as User);
        
        // Update form data with the response
        setFormData(prev => ({
          ...prev,
          ...response.user
        }));
        
        if (onUpdateUser) {
          onUpdateUser(updatedUser as User);
        }

        showSuccess('Profile Updated', 'Your profile has been updated successfully!');
        setIsEditing(false);
        
        // Clear the image previews and files
        setProfileImage(null);
        setProfileImagePreview(null);
      } else {
        showError('Update Failed', response.message || 'Failed to update profile. Please try again.');
      }
    } catch (error: any) {
      console.error('Profile update error:', error);
      showError('Update Failed', error.response?.data?.message || 'Failed to update profile. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleCancel = () => {
    setFormData({
      name: currentUser?.name || '',
      email: currentUser?.email || '',
      username: currentUser?.username || '',
      bio: currentUser?.bio || '',
      location: currentUser?.location || '',
      website: currentUser?.website || '',
      phoneNumber: currentUser?.phoneNumber || '',
      dateOfBirth: currentUser?.dateOfBirth || '',
      gender: currentUser?.gender || '',
      isPrivate: currentUser?.isPrivate || false,
      password: '',
      confirmPassword: ''
    });
    setProfileImage(null);
    setProfileImagePreview(null);
    setIsEditing(false);
  };

  const handleLogout = () => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  const handleCreateNewThread = async (content: string, mediaFiles?: File[], replyPermission?: 'ANYONE' | 'FOLLOWERS' | 'FOLLOWING' | 'MENTIONED_ONLY') => {
    try {
      const response = await threadsApi.createThread(content, mediaFiles, undefined, replyPermission);
      
      if (response.success) {
        setShowNewThreadModal(false);
        // Navigate to feed to see the new thread
        navigate('/feed');
      }
    } catch (error) {
      console.error('Error creating new thread:', error);
    }
  };

  if (!currentUser) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="text-center">
          <p className="text-gray-500">Please log in to view your profile.</p>
          <button 
            onClick={() => navigate('/login')}
            className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            Go to Login
          </button>
        </div>
      </div>
    );
  }

  return (
    <div id="wrapper" className="bg-gray-100 dark:bg-slate-900">
      {/* Sidebar */}
      <Sidebar 
        currentUser={currentUser}
        onToggleTheme={() => {
          document.documentElement.classList.toggle('dark');
        }}
        isDarkMode={document.documentElement.classList.contains('dark')}
        onLogout={handleLogout}
        onCreateThread={() => setShowNewThreadModal(true)}
        isCreateModalOpen={showNewThreadModal}
      />


      {/* Main Content */}
      <main id="site__main" className="p-2.5 h-[calc(100vh-var(--m-top))] mt-[--m-top] bg-white">
        <div className="max-w-4xl mx-auto p-4">
        {/* Profile Header */}
        <div className="bg-white dark:bg-slate-800 rounded-lg shadow-md mb-6 p-6">
          <div className="flex items-center gap-4">
            {/* Profile Picture */}
            <div className="relative">
              <div className="w-20 h-20 rounded-full border-2 border-gray-200 dark:border-slate-600 overflow-hidden">
                {profileImagePreview ? (
                  <img 
                    src={profileImagePreview} 
                    alt="Profile" 
                    className="w-full h-full object-cover"
                  />
                ) : currentUser.picture ? (
                  <img 
                    src={getProfileImageUrl(currentUser.picture)} 
                    alt="Profile" 
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <div className={`w-full h-full ${getInitialsBackgroundColor(currentUser.name)} flex items-center justify-center`}>
                    <span className="text-white text-xl font-semibold">
                      {generateInitials(currentUser.name)}
                    </span>
                  </div>
                )}
              </div>
              <button
                onClick={() => profileImageInputRef.current?.click()}
                className="absolute -bottom-1 -right-1 p-1.5 bg-blue-600 text-white rounded-full hover:bg-blue-700 transition-colors shadow-lg"
              >
                <IonIcon icon={camera} className="text-sm" />
              </button>
              <input
                ref={profileImageInputRef}
                type="file"
                accept="image/*"
                onChange={handleProfileImageChange}
                className="hidden"
              />
            </div>

            <div className="flex-1">
              <h2 className="text-2xl font-bold text-gray-900 dark:text-white">{currentUser.name}</h2>
              <p className="text-gray-500 dark:text-gray-400">@{currentUser.username || 'username'}</p>
              {currentUser.bio && (
                <p className="text-gray-600 dark:text-gray-300 mt-2">{currentUser.bio}</p>
              )}
              
              {/* Profile Stats */}
              <div className="flex gap-6 mt-4">
                <button
                  onClick={() => navigate(`/following/${currentUser.id}`)}
                  className="flex items-center gap-1 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors"
                >
                  <span className="font-semibold">{currentUser.followingCount || 0}</span>
                  <span className="text-sm">Following</span>
                </button>
                <button
                  onClick={() => navigate(`/following/${currentUser.id}`)}
                  className="flex items-center gap-1 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors"
                >
                  <span className="font-semibold">{currentUser.followersCount || 0}</span>
                  <span className="text-sm">Followers</span>
                </button>
                <div className="flex items-center gap-1 text-gray-600 dark:text-gray-400">
                  <span className="font-semibold">{currentUser.postsCount || 0}</span>
                  <span className="text-sm">Posts</span>
                </div>
              </div>
            </div>

            <div className="flex gap-2">
              {isEditing ? (
                <>
                  <button
                    onClick={handleCancel}
                    className="px-4 py-2 text-sm bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors shadow-md"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={handleSave}
                    disabled={isLoading}
                    className="px-4 py-2 text-sm bg-black text-white rounded-lg hover:bg-gray-800 disabled:opacity-50 transition-colors flex items-center gap-2 shadow-md"
                  >
                    {isLoading ? (
                      <div className="w-3 h-3 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    ) : (
                      <IonIcon icon={checkmark} className="text-xs text-white" />
                    )}
                    Save
                  </button>
                </>
              ) : (
                <button
                  onClick={() => setIsEditing(true)}
                  className="px-4 py-2 text-sm bg-black text-white rounded-lg hover:bg-gray-800 transition-colors shadow-md"
                >
                  Edit Profile
                </button>
              )}
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="bg-white dark:bg-slate-800 rounded-lg shadow-md">
          <div className="border-b border-gray-200 dark:border-slate-700">
            <nav className="flex">
              {tabs.map((tab) => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`flex items-center gap-2 px-6 py-4 text-sm font-medium border-b-2 transition-colors ${
                    activeTab === tab.id
                      ? 'border-blue-600 text-blue-600 dark:text-blue-400'
                      : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300'
                  }`}
                >
                  <IonIcon icon={tab.icon} className="text-lg" />
                  {tab.name}
                </button>
              ))}
            </nav>
          </div>

          <div className="p-6">
            {/* Personal Info Tab */}
            {activeTab === 0 && (
              <div className="space-y-6">
                <div className="grid md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2 flex items-center gap-2">
                      Full Name
                      {!isEditing && (
                        <button
                          onClick={() => setIsEditing(true)}
                          className="p-1.5 bg-gray-800 text-white rounded-full hover:bg-gray-900 transition-colors shadow-sm"
                        >
                          <IonIcon icon={createOutline} className="text-sm" />
                        </button>
                      )}
                    </label>
                    <input
                      type="text"
                      name="name"
                      value={formData.name}
                      onChange={handleInputChange}
                      disabled={!isEditing}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-slate-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-slate-700 dark:text-white disabled:bg-gray-50 dark:disabled:bg-slate-600"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                      Username
                    </label>
                    <input
                      type="text"
                      name="username"
                      value={formData.username}
                      onChange={handleInputChange}
                      disabled={!isEditing}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-slate-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-slate-700 dark:text-white disabled:bg-gray-50 dark:disabled:bg-slate-600"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Email <span className="text-xs text-gray-500">(Cannot be changed)</span>
                  </label>
                  <input
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleInputChange}
                    disabled={true}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-slate-600 rounded-lg bg-gray-100 dark:bg-slate-600 text-gray-500 dark:text-gray-400 cursor-not-allowed"
                    title="Email address cannot be changed"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2 flex items-center gap-2">
                    Bio
                    {!isEditing && (
                      <button
                        onClick={() => setIsEditing(true)}
                        className="p-1.5 bg-gray-800 text-white rounded-full hover:bg-gray-900 transition-colors shadow-sm"
                      >
                        <IonIcon icon={createOutline} className="text-sm" />
                      </button>
                    )}
                  </label>
                  <textarea
                    name="bio"
                    value={formData.bio}
                    onChange={handleInputChange}
                    disabled={!isEditing}
                    rows={4}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-slate-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-slate-700 dark:text-white disabled:bg-gray-50 dark:disabled:bg-slate-600"
                    placeholder="Tell us about yourself..."
                  />
                </div>

                <div className="grid md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                      Location
                    </label>
                    <input
                      type="text"
                      name="location"
                      value={formData.location}
                      onChange={handleInputChange}
                      disabled={!isEditing}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-slate-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-slate-700 dark:text-white disabled:bg-gray-50 dark:disabled:bg-slate-600"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                      Website
                    </label>
                    <input
                      type="url"
                      name="website"
                      value={formData.website}
                      onChange={handleInputChange}
                      disabled={!isEditing}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-slate-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-slate-700 dark:text-white disabled:bg-gray-50 dark:disabled:bg-slate-600"
                    />
                  </div>
                </div>

                <div className="grid md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                      Phone Number
                    </label>
                    <input
                      type="tel"
                      name="phoneNumber"
                      value={formData.phoneNumber}
                      onChange={handleInputChange}
                      disabled={!isEditing}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-slate-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-slate-700 dark:text-white disabled:bg-gray-50 dark:disabled:bg-slate-600"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                      Date of Birth
                    </label>
                    <input
                      type="date"
                      name="dateOfBirth"
                      value={formData.dateOfBirth}
                      onChange={handleInputChange}
                      disabled={!isEditing}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-slate-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-slate-700 dark:text-white disabled:bg-gray-50 dark:disabled:bg-slate-600"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Gender
                  </label>
                  <select
                    name="gender"
                    value={formData.gender}
                    onChange={handleInputChange}
                    disabled={!isEditing}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-slate-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-slate-700 dark:text-white disabled:bg-gray-50 dark:disabled:bg-slate-600"
                  >
                    <option value="">Select Gender</option>
                    <option value="male">Male</option>
                    <option value="female">Female</option>
                    <option value="other">Other</option>
                    <option value="prefer-not-to-say">Prefer not to say</option>
                  </select>
                </div>
              </div>
            )}

            {/* Profile Picture Tab */}
            {activeTab === 1 && (
              <div className="text-center">
                <div className="w-32 h-32 mx-auto mb-6 rounded-full border-4 border-gray-200 dark:border-slate-600 overflow-hidden">
                  {profileImagePreview ? (
                    <img 
                      src={profileImagePreview} 
                      alt="Profile Preview" 
                      className="w-full h-full object-cover"
                    />
                  ) : currentUser.picture ? (
                    <img 
                      src={getProfileImageUrl(currentUser.picture)} 
                      alt="Current Profile" 
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <div className={`w-full h-full ${getInitialsBackgroundColor(currentUser.name)} flex items-center justify-center`}>
                      <span className="text-white text-3xl font-semibold">
                        {generateInitials(currentUser.name)}
                      </span>
                    </div>
                  )}
                </div>
                <button
                  onClick={() => profileImageInputRef.current?.click()}
                  className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                >
                  Change Profile Picture
                </button>
                <p className="text-sm text-gray-500 dark:text-gray-400 mt-2">
                  JPG, PNG or GIF. Max size 2MB.
                </p>
              </div>
            )}

            {/* Privacy Tab */}
            {activeTab === 2 && (
              <div className="space-y-6">
                <div className="flex items-center justify-between p-4 border border-gray-200 dark:border-slate-600 rounded-lg">
                  <div>
                    <h3 className="font-medium text-gray-900 dark:text-white">Private Account</h3>
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                      When your account is private, only people you approve can see your posts and profile information.
                    </p>
                  </div>
                  <label className="relative inline-flex items-center cursor-pointer">
                    <input
                      type="checkbox"
                      name="isPrivate"
                      checked={formData.isPrivate}
                      onChange={handleInputChange}
                      disabled={!isEditing}
                      className="sr-only peer"
                    />
                    <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-blue-600"></div>
                  </label>
                </div>

                {isEditing && (
                  <div className="space-y-4">
                    <h3 className="font-medium text-gray-900 dark:text-white">Change Password</h3>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        New Password
                      </label>
                      <div className="relative">
                        <input
                          type={showPassword ? "text" : "password"}
                          name="password"
                          value={formData.password}
                          onChange={handleInputChange}
                          className="w-full px-3 py-2 pr-10 border border-gray-300 dark:border-slate-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-slate-700 dark:text-white"
                        />
                        <button
                          type="button"
                          onClick={() => setShowPassword(!showPassword)}
                          className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                        >
                          <IonIcon icon={showPassword ? eyeOff : eye} className="text-lg" />
                        </button>
                      </div>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        Confirm New Password
                      </label>
                      <input
                        type="password"
                        name="confirmPassword"
                        value={formData.confirmPassword}
                        onChange={handleInputChange}
                        className="w-full px-3 py-2 border border-gray-300 dark:border-slate-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-slate-700 dark:text-white"
                      />
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
        </div>
      </main>

      {/* New Thread Modal */}
      <NewThreadModal
        isOpen={showNewThreadModal}
        onClose={() => setShowNewThreadModal(false)}
        currentUser={currentUser}
        onSubmit={handleCreateNewThread}
      />
    </div>
  );
};

export default ProfilePage;
