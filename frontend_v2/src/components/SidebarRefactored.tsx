import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { IonIcon } from '@ionic/react';
import { 
  home, 
  notifications, 
  person, 
  search,
  add,
  heart,
  logOut,
  settings,
  documentText
} from 'ionicons/icons';
import { Avatar, Button, Card } from './ui';
import { useClickOutside } from '../hooks';
import { ROUTES } from '../constants';

interface SidebarProps {
  currentUser?: {
    id: string;
    name: string;
    email: string;
    picture?: string;
  } | null;
  onToggleTheme?: () => void;
  isDarkMode?: boolean;
  onLogout?: () => void;
  onCreateThread?: () => void;
  isCreateModalOpen?: boolean;
}

interface NavigationItem {
  path: string;
  icon: string;
  label: string;
}

const navigationItems: NavigationItem[] = [
  { path: ROUTES.FEED, icon: home, label: 'Home' },
  { path: ROUTES.SEARCH, icon: search, label: 'Search' },
  { path: ROUTES.LIKES, icon: heart, label: 'Likes' },
  { path: ROUTES.MY_POSTS, icon: documentText, label: 'My Posts' },
  { path: ROUTES.NOTIFICATIONS, icon: notifications, label: 'Notifications' }
];

const Sidebar: React.FC<SidebarProps> = ({ 
  currentUser, 
  onToggleTheme, 
  isDarkMode = false, 
  onLogout, 
  onCreateThread, 
  isCreateModalOpen = false 
}) => {
  const navigate = useNavigate();
  const location = useLocation();
  const [showProfileDropdown, setShowProfileDropdown] = useState(false);
  const dropdownRef = useClickOutside(() => setShowProfileDropdown(false));

  const handleNavigation = (path: string) => {
    navigate(path);
  };

  const handleLogout = () => {
    if (onLogout) {
      onLogout();
    } else {
      localStorage.removeItem('token');
      navigate(ROUTES.LOGIN);
    }
    setShowProfileDropdown(false);
  };

  const isActiveRoute = (path: string) => {
    return location.pathname === path && !isCreateModalOpen;
  };

  const NavigationButton: React.FC<{ item: NavigationItem }> = ({ item }) => (
    <Button
      variant="ghost"
      size="lg"
      onClick={() => handleNavigation(item.path)}
      className={`w-12 h-12 rounded-full transition-colors ${
        isActiveRoute(item.path)
          ? 'bg-gray-100 dark:bg-slate-600' 
          : 'hover:bg-gray-50 dark:hover:bg-slate-700'
      }`}
      title={item.label}
    >
      <IonIcon 
        icon={item.icon} 
        className={`text-2xl ${
          isActiveRoute(item.path)
            ? 'text-black dark:text-white' 
            : 'text-gray-400 dark:text-gray-500'
        }`} 
      />
    </Button>
  );

  return (
    <div className="fixed top-0 left-0 z-[99] pt-[--m-top] transition-transform xl:duration-500 max-xl:w-full max-xl:-translate-x-full">
      <div className="p-4 max-xl:bg-transparent shadow-sm w-20 h-[calc(100vh-64px)] relative z-30 max-lg:border-r dark:max-xl:bg-transparent dark:border-slate-700 flex flex-col items-center bg-transparent">
        <nav className="flex flex-col items-center space-y-6 w-full">
          {/* Logo/Brand */}
          <div className="mb-8">
            <Button
              variant="primary"
              size="sm"
              onClick={() => navigate(ROUTES.FEED)}
              className="w-8 h-8 bg-black rounded-lg flex items-center justify-center p-0"
            >
              <span className="text-white font-bold text-lg">உ</span>
            </Button>
          </div>

          {/* Navigation Items */}
          <div className="flex flex-col items-center space-y-6 flex-1">
            {navigationItems.map((item) => (
              <NavigationButton key={item.path} item={item} />
            ))}

            {/* Create/Add Button - Prominent */}
            <Button
              variant="secondary"
              size="lg"
              onClick={onCreateThread}
              className="w-12 h-12 bg-gray-200 dark:bg-slate-600 hover:bg-gray-300 dark:hover:bg-slate-500 rounded-lg flex items-center justify-center p-0"
              title="Create Thread"
            >
              <IonIcon icon={add} className="text-2xl text-gray-900 dark:text-white" />
            </Button>

            {/* Profile */}
            <div className="relative" ref={dropdownRef}>
              <Button
                variant="ghost"
                size="lg"
                onClick={() => setShowProfileDropdown(!showProfileDropdown)}
                className={`w-12 h-12 rounded-full transition-colors ${
                  location.pathname === ROUTES.PROFILE 
                    ? 'bg-gray-100 dark:bg-slate-600' 
                    : 'hover:bg-gray-50 dark:hover:bg-slate-700'
                }`}
                title="Profile"
              >
                <IonIcon 
                  icon={person} 
                  className={`text-2xl ${
                    location.pathname === ROUTES.PROFILE 
                      ? 'text-black dark:text-white' 
                      : 'text-gray-400 dark:text-gray-500'
                  }`} 
                />
              </Button>

              {/* Profile Dropdown */}
              {showProfileDropdown && (
                <Card className="absolute left-16 top-0 rounded-xl shadow-2xl py-3 min-w-[220px] max-w-[320px] z-[9999]">
                  <div className="px-3 py-2 border-b border-gray-100 dark:border-slate-700">
                    <div className="flex items-center gap-3">
                      <Avatar
                        src={currentUser?.picture}
                        name={currentUser?.name || 'User'}
                        size="sm"
                      />
                      <div>
                        <p className="text-sm font-medium text-gray-900 dark:text-white">
                          {currentUser?.name}
                        </p>
                      </div>
                    </div>
                  </div>
                  
                  <Button
                    variant="ghost"
                    onClick={() => {
                      handleNavigation(ROUTES.PROFILE);
                      setShowProfileDropdown(false);
                    }}
                    className="w-full px-4 py-3 text-left text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-slate-700 flex items-center gap-3 transition-colors justify-start"
                  >
                    <IonIcon icon={person} className="text-lg" />
                    View Profile
                  </Button>
                  
                  <Button
                    variant="ghost"
                    onClick={() => {
                      handleNavigation(ROUTES.SETTINGS);
                      setShowProfileDropdown(false);
                    }}
                    className="w-full px-4 py-3 text-left text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-slate-700 flex items-center gap-3 transition-colors justify-start"
                  >
                    <IonIcon icon={settings} className="text-lg" />
                    Settings
                  </Button>
                  
                  <div className="border-t border-gray-100 dark:border-slate-700 mt-1">
                    <Button
                      variant="ghost"
                      onClick={handleLogout}
                      className="w-full px-4 py-3 text-left text-sm text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 flex items-center gap-3 transition-colors justify-start"
                    >
                      <IonIcon icon={logOut} className="text-lg" />
                      Logout
                    </Button>
                  </div>
                </Card>
              )}
            </div>
          </div>

          {/* More Options - Hamburger Menu at Bottom */}
          <div className="mb-4">
            <Button
              variant="ghost"
              size="lg"
              onClick={() => navigate(ROUTES.SETTINGS)}
              className="w-12 h-12 rounded-full hover:bg-gray-50 dark:hover:bg-slate-700 transition-colors p-0"
              title="More Options"
            >
              <div className="flex flex-col space-y-1">
                <div className="w-4 h-0.5 bg-gray-600 dark:bg-gray-300"></div>
                <div className="w-4 h-0.5 bg-gray-600 dark:bg-gray-300"></div>
              </div>
            </Button>
          </div>
        </nav>
      </div>
    </div>
  );
};

export default Sidebar;