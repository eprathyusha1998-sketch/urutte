import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { IonIcon } from '@ionic/react';
import { 
  search, 
  addCircleOutline, 
  notificationsOutline, 
  chatboxEllipsesOutline, 
  checkmarkCircle, 
  menuOutline, 
  closeOutline,
  calendar,
  location,
  time,
  people,
  heart,
  shareOutline,
  ellipsisHorizontal,
  filter,
  grid,
  list,
  star,
  trendingUp,
  ticket,
  map,
  image,
  videocam,
  happy
} from 'ionicons/icons';
import { authApi, eventsApi } from '../services/api';
import { generateInitials, getInitialsBackgroundColor } from '../utils/profileUtils';

interface User {
  id: string;
  name: string;
  email: string;
  picture?: string;
}

interface Event {
  id: number;
  title: string;
  description: string;
  eventDate: string;
  endDate?: string;
  location: string;
  category: string;
  imageUrl?: string;
  price?: number;
  maxAttendees?: number;
  organizer: User;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  attendeesCount: number;
  isAttending: boolean;
}

const EventsPage: React.FC = () => {
  const navigate = useNavigate();
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [events, setEvents] = useState<Event[]>([]);
  const [filteredEvents, setFilteredEvents] = useState<Event[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [selectedFilter, setSelectedFilter] = useState('all');
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [isLoading, setIsLoading] = useState(true);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [showCreateEvent, setShowCreateEvent] = useState(false);

  const categories = [
    'all', 'technology', 'business', 'education', 'entertainment', 
    'sports', 'health', 'food', 'travel', 'art', 'music', 'other'
  ];

  const filters = [
    { value: 'all', label: 'All Events' },
    { value: 'upcoming', label: 'Upcoming' },
    { value: 'free', label: 'Free Events' },
    { value: 'paid', label: 'Paid Events' },
    { value: 'trending', label: 'Trending' }
  ];

  useEffect(() => {
    // Check authentication
    const token = localStorage.getItem('access_token');
    if (!token) {
      navigate('/login');
      return;
    }

    const initializePage = async () => {
      try {
        // Get current user
        const user = await authApi.getCurrentUser();
        setCurrentUser(user);

        // Load events
        await loadEvents();

      } catch (error) {
        console.error('Error initializing events page:', error);
      } finally {
        setIsLoading(false);
      }
    };

    initializePage();
  }, [navigate]);

  const loadEvents = async () => {
    try {
      let eventsData: Event[] = [];
      
      switch (selectedFilter) {
        case 'upcoming':
          const upcomingResponse = await eventsApi.getUpcomingEvents();
          eventsData = upcomingResponse.content || upcomingResponse;
          break;
        case 'free':
          const freeResponse = await eventsApi.getFreeEvents();
          eventsData = freeResponse.content || freeResponse;
          break;
        case 'paid':
          const paidResponse = await eventsApi.getPaidEvents();
          eventsData = paidResponse.content || paidResponse;
          break;
        case 'trending':
          const trendingResponse = await eventsApi.getTrendingEvents();
          eventsData = trendingResponse.content || trendingResponse;
          break;
        default:
          const allResponse = await eventsApi.getAllEvents();
          eventsData = allResponse.content || allResponse;
      }
      
      setEvents(eventsData);
      setFilteredEvents(eventsData);
    } catch (error) {
      console.error('Error loading events:', error);
    }
  };

  useEffect(() => {
    loadEvents();
  }, [selectedFilter]);

  useEffect(() => {
    let filtered = events;

    // Filter by search query
    if (searchQuery) {
      filtered = filtered.filter(event =>
        event.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        event.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
        event.location.toLowerCase().includes(searchQuery.toLowerCase()) ||
        event.category.toLowerCase().includes(searchQuery.toLowerCase())
      );
    }

    // Filter by category
    if (selectedCategory !== 'all') {
      filtered = filtered.filter(event =>
        event.category.toLowerCase() === selectedCategory.toLowerCase()
      );
    }

    setFilteredEvents(filtered);
  }, [events, searchQuery, selectedCategory]);

  const handleToggleAttendance = async (eventId: number) => {
    try {
      const updatedEvent = await eventsApi.toggleAttendance(eventId);
      
      setEvents(prev => prev.map(event => 
        event.id === eventId ? updatedEvent : event
      ));
      
      setFilteredEvents(prev => prev.map(event => 
        event.id === eventId ? updatedEvent : event
      ));
    } catch (error) {
      console.error('Error toggling attendance:', error);
    }
  };

  const handleLogout = async () => {
    try {
      await authApi.logout();
      navigate('/login');
    } catch (error) {
      console.error('Error logging out:', error);
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      weekday: 'short',
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatPrice = (price?: number) => {
    if (!price || price === 0) return 'Free';
    return `$${price.toFixed(2)}`;
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-dark flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
          <p className="mt-4 text-gray-600 dark:text-gray-400">Loading events...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-dark">
      {/* Header */}
      <header className="bg-white dark:bg-dark2 border-b border-gray-200 dark:border-slate-700">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <button
                onClick={() => navigate('/feed')}
                className="text-2xl font-bold text-blue-500 hover:text-blue-600"
              >
                urutte
              </button>
            </div>
            
            <div className="flex items-center space-x-4">
              <button
                onClick={() => navigate('/messages')}
                className="p-2 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
              >
                <IonIcon icon={chatboxEllipsesOutline} className="text-xl" />
              </button>
              
              <button
                onClick={() => navigate('/notifications')}
                className="p-2 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
              >
                <IonIcon icon={notificationsOutline} className="text-xl" />
              </button>
              
              <div className="relative">
                <button
                  onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                  className="p-2 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
                >
                  <IonIcon icon={isMobileMenuOpen ? closeOutline : menuOutline} className="text-xl" />
                </button>
                
                {isMobileMenuOpen && (
                  <div className="absolute right-0 mt-2 w-48 bg-white dark:bg-dark2 rounded-md shadow-lg py-1 z-50 border border-gray-200 dark:border-slate-700">
                    <button
                      onClick={handleLogout}
                      className="block w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-slate-600"
                    >
                      Logout
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {/* Page Header */}
        <div className="mb-8">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Events</h1>
              <p className="mt-2 text-gray-600 dark:text-gray-400">
                Discover and join amazing events happening around you
              </p>
            </div>
            
            <button
              onClick={() => setShowCreateEvent(true)}
              className="flex items-center space-x-2 bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition-colors"
            >
              <IonIcon icon={addCircleOutline} className="text-lg" />
              <span>Create Event</span>
            </button>
          </div>
        </div>

        {/* Filters and Search */}
        <div className="mb-6 space-y-4">
          {/* Search Bar */}
          <div className="relative">
            <IonIcon 
              icon={search} 
              className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-lg" 
            />
            <input
              type="text"
              placeholder="Search events..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-3 border border-gray-300 dark:border-slate-600 rounded-lg bg-white dark:bg-slate-700 text-gray-900 dark:text-white placeholder-gray-500 dark:placeholder-gray-400 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          {/* Filters */}
          <div className="flex flex-wrap items-center gap-4">
            {/* Filter Tabs */}
            <div className="flex space-x-1 bg-gray-100 dark:bg-slate-700 rounded-lg p-1">
              {filters.map((filter) => (
                <button
                  key={filter.value}
                  onClick={() => setSelectedFilter(filter.value)}
                  className={`px-4 py-2 text-sm font-medium rounded-md transition-colors ${
                    selectedFilter === filter.value
                      ? 'bg-white dark:bg-slate-600 text-blue-600 dark:text-blue-400 shadow-sm'
                      : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
                  }`}
                >
                  {filter.label}
                </button>
              ))}
            </div>

            {/* Category Filter */}
            <select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              className="px-4 py-2 border border-gray-300 dark:border-slate-600 rounded-lg bg-white dark:bg-slate-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              {categories.map((category) => (
                <option key={category} value={category}>
                  {category.charAt(0).toUpperCase() + category.slice(1)}
                </option>
              ))}
            </select>

            {/* View Mode Toggle */}
            <div className="flex space-x-1 bg-gray-100 dark:bg-slate-700 rounded-lg p-1">
              <button
                onClick={() => setViewMode('grid')}
                className={`p-2 rounded-md transition-colors ${
                  viewMode === 'grid'
                    ? 'bg-white dark:bg-slate-600 text-blue-600 dark:text-blue-400 shadow-sm'
                    : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
                }`}
              >
                <IonIcon icon={grid} className="text-lg" />
              </button>
              <button
                onClick={() => setViewMode('list')}
                className={`p-2 rounded-md transition-colors ${
                  viewMode === 'list'
                    ? 'bg-white dark:bg-slate-600 text-blue-600 dark:text-blue-400 shadow-sm'
                    : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
                }`}
              >
                <IonIcon icon={list} className="text-lg" />
              </button>
            </div>
          </div>
        </div>

        {/* Events Grid/List */}
        {filteredEvents.length === 0 ? (
          <div className="text-center py-12">
            <IonIcon icon={calendar} className="text-6xl text-gray-400 mb-4" />
            <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">No events found</h3>
            <p className="text-gray-600 dark:text-gray-400">
              {searchQuery || selectedCategory !== 'all' 
                ? 'Try adjusting your search or filters' 
                : 'Be the first to create an event!'}
            </p>
          </div>
        ) : (
          <div className={`${
            viewMode === 'grid' 
              ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6' 
              : 'space-y-4'
          }`}>
            {filteredEvents.map((event) => (
              <div
                key={event.id}
                className={`bg-white dark:bg-dark2 rounded-lg shadow-sm border border-gray-200 dark:border-slate-700 overflow-hidden hover:shadow-md transition-shadow ${
                  viewMode === 'list' ? 'flex' : ''
                }`}
              >
                {/* Event Image */}
                <div className={`${viewMode === 'list' ? 'w-48 h-32' : 'h-48'} relative`}>
                  <img
                    src={event.imageUrl || "/assets/images/demos/event-2.png"}
                    alt={event.title}
                    className="w-full h-full object-cover"
                    onError={(e) => {
                      (e.target as HTMLImageElement).src = "/assets/images/demos/event-2.png";
                    }}
                  />
                  <div className="absolute top-3 left-3">
                    <span className="bg-blue-500 text-white text-xs px-2 py-1 rounded-full">
                      {event.category}
                    </span>
                  </div>
                  <div className="absolute top-3 right-3">
                    <span className={`text-xs px-2 py-1 rounded-full ${
                      event.price && event.price > 0 
                        ? 'bg-green-500 text-white' 
                        : 'bg-gray-500 text-white'
                    }`}>
                      {formatPrice(event.price)}
                    </span>
                  </div>
                </div>

                {/* Event Content */}
                <div className={`p-4 ${viewMode === 'list' ? 'flex-1' : ''}`}>
                  <div className="flex items-start justify-between mb-2">
                    <h3 className="text-lg font-semibold text-gray-900 dark:text-white line-clamp-2">
                      {event.title}
                    </h3>
                    <button className="p-1 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300">
                      <IonIcon icon={ellipsisHorizontal} className="text-lg" />
                    </button>
                  </div>

                  <p className="text-gray-600 dark:text-gray-400 text-sm mb-3 line-clamp-2">
                    {event.description}
                  </p>

                  {/* Event Details */}
                  <div className="space-y-2 mb-4">
                    <div className="flex items-center text-sm text-gray-600 dark:text-gray-400">
                      <IonIcon icon={time} className="text-lg mr-2" />
                      <span>{formatDate(event.eventDate)}</span>
                    </div>
                    
                    <div className="flex items-center text-sm text-gray-600 dark:text-gray-400">
                      <IonIcon icon={location} className="text-lg mr-2" />
                      <span>{event.location}</span>
                    </div>
                    
                    <div className="flex items-center text-sm text-gray-600 dark:text-gray-400">
                      <IonIcon icon={people} className="text-lg mr-2" />
                      <span>{event.attendeesCount} attending</span>
                      {event.maxAttendees && (
                        <span className="ml-1">/ {event.maxAttendees} max</span>
                      )}
                    </div>
                  </div>

                  {/* Organizer */}
                  <div className="flex items-center mb-4">
                    {event.organizer.picture ? (
                      <img
                        src={event.organizer.picture}
                        alt={event.organizer.name}
                        className="w-6 h-6 rounded-full mr-2 object-cover"
                      />
                    ) : (
                      <div className="w-6 h-6 rounded-full mr-2 bg-gray-100 dark:bg-slate-600 flex items-center justify-center">
                        <div className={`w-5 h-5 rounded-full ${getInitialsBackgroundColor(event.organizer.name || '')} flex items-center justify-center`}>
                          <span className="text-white text-xs font-semibold">
                            {generateInitials(event.organizer.name || 'உ')}
                          </span>
                        </div>
                      </div>
                    )}
                    <span className="text-sm text-gray-600 dark:text-gray-400">
                      by {event.organizer.name}
                    </span>
                  </div>

                  {/* Actions */}
                  <div className="flex items-center justify-between">
                    <button
                      onClick={() => handleToggleAttendance(event.id)}
                      className={`flex items-center space-x-2 px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                        event.isAttending
                          ? 'bg-green-500 text-white hover:bg-green-600'
                          : 'bg-blue-500 text-white hover:bg-blue-600'
                      }`}
                    >
                      <IonIcon icon={event.isAttending ? checkmarkCircle : ticket} className="text-lg" />
                      <span>{event.isAttending ? 'Attending' : 'Attend'}</span>
                    </button>

                    <div className="flex items-center space-x-2">
                      <button className="p-2 text-gray-400 hover:text-red-500 transition-colors">
                        <IonIcon icon={heart} className="text-lg" />
                      </button>
                      <button className="p-2 text-gray-400 hover:text-blue-500 transition-colors">
                        <IonIcon icon={shareOutline} className="text-lg" />
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default EventsPage;
