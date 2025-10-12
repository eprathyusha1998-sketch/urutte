import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { IonIcon } from '@ionic/react';
import { 
  home, 
  notifications, 
  person, 
  search,
  add,
  heart,
  menu,
  logOut
} from 'ionicons/icons';

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
}

const Sidebar: React.FC<SidebarProps> = ({ currentUser, onToggleTheme, isDarkMode = false, onLogout, onCreateThread }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const [showProfileDropdown, setShowProfileDropdown] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const handleNavigation = (path: string) => {
    navigate(path);
  };

  const handleLogout = () => {
    if (onLogout) {
      onLogout();
    } else {
      // Default logout behavior
      localStorage.removeItem('token');
      navigate('/login');
    }
    setShowProfileDropdown(false);
  };

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setShowProfileDropdown(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  return (
    <div className="fixed top-0 left-0 z-[99] pt-[--m-top] overflow-hidden transition-transform xl:duration-500 max-xl:w-full max-xl:-translate-x-full">
      <div className="p-4 max-xl:bg-white shadow-sm w-20 h-[calc(100vh-64px)] relative z-30 max-lg:border-r dark:max-xl:bg-slate-700 dark:border-slate-700 flex flex-col items-center">
        <nav className="flex flex-col items-center space-y-6 w-full">
          {/* Logo/Brand - Distinctive Tamil 'உ' icon */}
          <div className="mt-4 mb-8">
            <div className="w-8 h-8 bg-black rounded-full flex items-center justify-center cursor-pointer"
                 onClick={() => navigate('/feed')}>
              <span className="text-white font-bold text-lg">உ</span>
            </div>
          </div>

          {/* Navigation Items - Icon only */}
          <div className="flex flex-col items-center space-y-6 flex-1">
            {/* Home */}
            <button
              onClick={() => handleNavigation('/feed')}
              className={`w-12 h-12 flex items-center justify-center rounded-full transition-colors ${
                location.pathname === '/feed' 
                  ? 'bg-gray-100 dark:bg-slate-600' 
                  : 'hover:bg-gray-50 dark:hover:bg-slate-700'
              }`}
            >
              <IonIcon 
                icon={home} 
                className={`text-2xl ${
                  location.pathname === '/feed' 
                    ? 'text-black dark:text-white' 
                    : 'text-gray-400 dark:text-gray-500'
                }`} 
              />
            </button>

            {/* Search */}
            <button
              onClick={() => handleNavigation('/search')}
              className={`w-12 h-12 flex items-center justify-center rounded-full transition-colors ${
                location.pathname === '/search' 
                  ? 'bg-gray-100 dark:bg-slate-600' 
                  : 'hover:bg-gray-50 dark:hover:bg-slate-700'
              }`}
            >
              <IonIcon 
                icon={search} 
                className={`text-2xl ${
                  location.pathname === '/search' 
                    ? 'text-black dark:text-white' 
                    : 'text-gray-400 dark:text-gray-500'
                }`} 
              />
            </button>

            {/* Create/Add Button - Prominent */}
            <button
              onClick={onCreateThread}
              className="w-12 h-12 bg-gray-200 dark:bg-slate-600 hover:bg-gray-300 dark:hover:bg-slate-500 rounded-lg flex items-center justify-center transition-colors"
            >
              <IonIcon icon={add} className="text-2xl text-gray-900 dark:text-white" />
            </button>

            {/* Likes/Notifications */}
            <button
              onClick={() => handleNavigation('/notifications')}
              className={`w-12 h-12 flex items-center justify-center rounded-full transition-colors ${
                location.pathname === '/notifications' 
                  ? 'bg-gray-100 dark:bg-slate-600' 
                  : 'hover:bg-gray-50 dark:hover:bg-slate-700'
              }`}
            >
              <IonIcon 
                icon={heart} 
                className={`text-2xl ${
                  location.pathname === '/notifications' 
                    ? 'text-black dark:text-white' 
                    : 'text-gray-400 dark:text-gray-500'
                }`} 
              />
            </button>

            {/* Profile */}
            <div className="relative" ref={dropdownRef}>
              <button
                onClick={() => setShowProfileDropdown(!showProfileDropdown)}
                className={`w-12 h-12 flex items-center justify-center rounded-full transition-colors ${
                  location.pathname === '/profile' 
                    ? 'bg-gray-100 dark:bg-slate-600' 
                    : 'hover:bg-gray-50 dark:hover:bg-slate-700'
                }`}
              >
                {currentUser?.picture ? (
                  <img 
                    src={currentUser.picture} 
                    alt={currentUser.name}
                    className="w-8 h-8 rounded-full object-cover"
                    onError={(e) => {
                      const target = e.target as HTMLImageElement;
                      target.style.display = 'none';
                      const fallback = target.nextElementSibling as HTMLElement;
                      if (fallback) fallback.style.display = 'flex';
                    }}
                  />
                ) : null}
                <div 
                  className={`w-8 h-8 rounded-full bg-gray-900 dark:bg-white flex items-center justify-center ${
                    currentUser?.picture ? 'hidden' : 'flex'
                  }`}
                >
                  <IonIcon 
                    icon={person} 
                    className={`text-lg ${
                      location.pathname === '/profile' 
                        ? 'text-white dark:text-gray-900' 
                        : 'text-white dark:text-gray-900'
                    }`} 
                  />
                </div>
              </button>

              {/* Profile Dropdown */}
              {showProfileDropdown && (
                <div className="absolute left-16 top-0 bg-white dark:bg-slate-800 rounded-lg shadow-lg border border-gray-200 dark:border-slate-700 py-2 min-w-[160px] z-50">
                  <button
                    onClick={() => {
                      handleNavigation('/profile');
                      setShowProfileDropdown(false);
                    }}
                    className="w-full px-4 py-2 text-left text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-slate-700 flex items-center gap-2"
                  >
                    <IonIcon icon={person} className="text-lg" />
                    Profile
                  </button>
                  <button
                    onClick={handleLogout}
                    className="w-full px-4 py-2 text-left text-sm text-red-600 dark:text-red-400 hover:bg-gray-50 dark:hover:bg-slate-700 flex items-center gap-2"
                  >
                    <IonIcon icon={logOut} className="text-lg" />
                    Logout
                  </button>
                </div>
              )}
            </div>
          </div>

          {/* More Options - Hamburger Menu at Bottom */}
          <div className="mb-4">
            <button
              onClick={() => navigate('/settings')}
              className="w-12 h-12 flex items-center justify-center rounded-full hover:bg-gray-50 dark:hover:bg-slate-700 transition-colors"
            >
              <div className="flex flex-col space-y-1">
                <div className="w-4 h-0.5 bg-gray-600 dark:bg-gray-300"></div>
                <div className="w-4 h-0.5 bg-gray-600 dark:bg-gray-300"></div>
              </div>
            </button>
          </div>
        </nav>
      </div>
    </div>
  );
};

export default Sidebar;
