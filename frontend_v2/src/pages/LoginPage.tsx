import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { IonIcon } from '@ionic/react';
import { logoGoogle } from 'ionicons/icons';
import { isAuthenticated, setStoredToken, removeStoredToken, isTokenExpired } from '../utils/auth';
import { useNotification } from '../contexts/NotificationContext';

const API_BASE_URL = process.env.REACT_APP_API_URL;

const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { showSuccess, showError } = useNotification();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    // Check if user is already authenticated
    if (isAuthenticated()) {
      navigate('/feed');
      return;
    }

    // Handle OAuth callback
    const token_param = searchParams.get('token');
    if (token_param) {
      try {
        // Check if token is expired
        if (isTokenExpired(token_param)) {
          showError('Token Expired', 'Your session has expired. Please log in again.');
          removeStoredToken();
          return;
        }
        
        setStoredToken(token_param);
        showSuccess('Welcome Back!', 'You have been successfully logged in.');
        navigate('/feed');
      } catch (error) {
        console.error('Error processing token:', error);
        showError('Invalid Token', 'Invalid token received. Please try logging in again.');
        removeStoredToken();
      }
    }
  }, [navigate, searchParams, showError, showSuccess]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email,
          password,
        }),
      });

      const data = await response.json();

      if (data.success) {
        // Store token and user data
        setStoredToken(data.token);
        localStorage.setItem('user', JSON.stringify(data.user));
        
        // Show success message
        showSuccess('Welcome Back!', `Hello ${data.user.name}! You have been successfully logged in.`);
        
        // Navigate to feed
        navigate('/feed');
      } else {
        showError('Login Failed', data.message || 'Login failed. Please try again.');
      }
    } catch (err: any) {
      showError('Login Failed', 'Network error. Please check your connection and try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLogin = () => {
    // Redirect to Spring Boot OAuth2 endpoint
    if (!API_BASE_URL) {
      console.error('API_BASE_URL is not defined. Please check your environment configuration.');
      showError('Configuration Error', 'Application is not properly configured. Please refresh the page.');
      return;
    }
    const redirectUrl = `${API_BASE_URL.replace('/api', '')}/oauth2/authorization/google`;
    console.log('Redirecting to Google OAuth:', redirectUrl);
    window.location.href = redirectUrl;
  };

  return (
    <div className="sm:flex">
      {/* Left Side - Login Form */}
      <div className="relative lg:w-[580px] md:w-96 w-full p-10 min-h-screen bg-white shadow-xl flex items-center pt-10 dark:bg-slate-900 z-10">
        <div className="w-full lg:max-w-sm mx-auto space-y-10" uk-scrollspy="target: > *; cls: uk-animation-scale-up; delay: 100 ;repeat: true">
          
          {/* Logo */}
        <div className="flex justify-center mb-8">
          <div className="w-12 h-12 bg-black rounded-lg flex items-center justify-center">
            <span className="text-white font-bold text-2xl">U</span>
          </div>
        </div>

          {/* Title */}
          <div>
            <h2 className="text-2xl font-semibold mb-1.5"> Sign in to your account </h2>
            <p className="text-sm text-gray-700 font-normal">
              If you haven't signed up yet. <Link to="/register" className="text-blue-700">Register here!</Link>
            </p>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-7 text-sm text-black font-medium dark:text-white" uk-scrollspy="target: > *; cls: uk-animation-scale-up; delay: 100 ;repeat: true">
            
            {/* Error Message */}
            {error && (
              <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg text-sm">
                {error}
              </div>
            )}

            {/* Email */}
            <div>
              <label htmlFor="email" className="">Email address</label>
              <div className="mt-2.5">
                <input 
                  id="email" 
                  name="email" 
                  type="email" 
                  autoFocus 
                  placeholder="Email" 
                  required 
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="!w-full !rounded-lg !bg-transparent !shadow-sm !border-slate-200 dark:!border-slate-800 dark:!bg-white/5"
                /> 
              </div>
            </div>

            {/* Password */}
            <div>
              <label htmlFor="password" className="">Password</label>
              <div className="mt-2.5">
                <input 
                  id="password" 
                  name="password" 
                  type="password" 
                  placeholder="***" 
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="!w-full !rounded-lg !bg-transparent !shadow-sm !border-slate-200 dark:!border-slate-800 dark:!bg-white/5"
                />  
              </div>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                <input 
                  id="rememberme" 
                  name="rememberme" 
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                />
                <label htmlFor="rememberme" className="font-normal">Remember me</label>
              </div>
              <button type="button" className="text-blue-700 hover:underline">Forgot password</button>
            </div>

            {/* Submit Button */}
            <div>
              <button 
                type="submit" 
                disabled={loading}
                className="button bg-primary text-white w-full disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? 'Signing in...' : 'Sign in'}
              </button>
            </div>

            <div className="text-center flex items-center gap-6"> 
              <hr className="flex-1 border-slate-200 dark:border-slate-800" /> 
              Or continue with  
              <hr className="flex-1 border-slate-200 dark:border-slate-800" />
            </div> 

            {/* Social Login */}
            <div className="flex gap-2" uk-scrollspy="target: > *; cls: uk-animation-scale-up; delay: 400 ;repeat: true">
              <button 
                type="button"
                onClick={handleGoogleLogin}
                className="button flex-1 flex items-center justify-center gap-2 bg-red-600 text-white text-sm hover:bg-red-700"
              > 
                <IonIcon icon={logoGoogle} className="text-lg" /> Google  
              </button>
            </div>
            
          </form>

        </div>
      </div>

      {/* Right Side - Image Slider */}
      <div className="flex-1 relative bg-primary max-md:hidden">
        <div className="relative w-full h-full" tabIndex={-1} uk-slideshow="animation: slide; autoplay: true">
          <ul className="uk-slideshow-items w-full h-full"> 
            <li className="w-full">
              <img src="/assets/images/post/img-3.jpg" alt="" className="w-full h-full object-cover uk-animation-kenburns uk-animation-reverse uk-transform-origin-center-left" />
              <div className="absolute bottom-0 w-full uk-tr ansition-slide-bottom-small z-10">
                <div className="max-w-xl w-full mx-auto pb-32 px-5 z-30 relative" uk-scrollspy="target: > *; cls: uk-animation-scale-up; delay: 100 ;repeat: true"> 
                  <div className="w-12 h-12 bg-black rounded-lg flex items-center justify-center">
                    <span className="text-white font-bold text-lg">U</span>
                  </div>
                  <h4 className="!text-white text-2xl font-semibold mt-7" uk-slideshow-parallax="y: 600,0,0">  Connect With Friends </h4> 
                  <p className="!text-white text-lg mt-7 leading-8" uk-slideshow-parallax="y: 800,0,0;"> This phrase is more casual and playful. It suggests that you are keeping your friends updated on what's happening in your life.</p>   
                </div> 
              </div>
              <div className="w-full h-96 bg-gradient-to-t from-black absolute bottom-0 left-0"></div>
            </li>
            <li className="w-full">
              <img src="/assets/images/post/img-2.jpg" alt="" className="w-full h-full object-cover uk-animation-kenburns uk-animation-reverse uk-transform-origin-center-left" />
              <div className="absolute bottom-0 w-full uk-tr ansition-slide-bottom-small z-10">
                <div className="max-w-xl w-full mx-auto pb-32 px-5 z-30 relative" uk-scrollspy="target: > *; cls: uk-animation-scale-up; delay: 100 ;repeat: true"> 
                  <div className="w-12 h-12 bg-black rounded-lg flex items-center justify-center">
                    <span className="text-white font-bold text-lg">U</span>
                  </div>
                  <h4 className="!text-white text-2xl font-semibold mt-7" uk-slideshow-parallax="y: 800,0,0">  Share Your Moments </h4> 
                  <p className="!text-white text-lg mt-7 leading-8" uk-slideshow-parallax="y: 800,0,0;"> Share your life moments with friends and family. Connect, engage, and build lasting relationships.</p>   
                </div> 
              </div>
              <div className="w-full h-96 bg-gradient-to-t from-black absolute bottom-0 left-0"></div>
            </li>
          </ul>

          {/* Slide Nav */}
          <div className="flex justify-center">
            <ul className="inline-flex flex-wrap justify-center absolute bottom-8 gap-1.5 uk-dotnav uk-slideshow-nav"> </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;

