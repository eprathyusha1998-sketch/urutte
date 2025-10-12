import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { IonIcon } from '@ionic/react';
import { logoFacebook, logoTwitter, logoGoogle } from 'ionicons/icons';

const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    acceptTerms: false
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validation
    if (formData.password !== formData.confirmPassword) {
      alert('Passwords do not match!');
      return;
    }
    
    if (!formData.acceptTerms) {
      alert('Please accept the terms of use');
      return;
    }

    // TODO: Implement actual registration logic
    console.log('Register:', formData);
    // For now, just navigate to feed page
    navigate('/feed');
  };

  const handleGoogleSignup = () => {
    // TODO: Implement Google OAuth
    console.log('Google signup');
    navigate('/feed');
  };

  return (
    <div className="sm:flex">
      {/* Left Side - Register Form */}
      <div className="relative lg:w-[580px] md:w-96 w-full p-10 min-h-screen bg-white shadow-xl flex items-center pt-10 dark:bg-slate-900 z-10">
        <div className="w-full lg:max-w-sm mx-auto space-y-10" uk-scrollspy="target: > *; cls: uk-animation-scale-up; delay: 100 ;repeat: true">
          
          {/* Logo */}
          <a href="#"> <img src="/assets/images/logo.png" className="w-28 absolute top-10 left-10 dark:hidden" alt="" /></a>
          <a href="#"> <img src="/assets/images/logo-light.png" className="w-28 absolute top-10 left-10 hidden dark:!block" alt="" /></a>

          {/* Title */}
          <div>
            <h2 className="text-2xl font-semibold mb-1.5"> Sign up to get started </h2>
            <p className="text-sm text-gray-700 font-normal">
              If you already have an account, <Link to="/login" className="text-blue-700">Login here!</Link>
            </p>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-7 text-sm text-black font-medium dark:text-white" uk-scrollspy="target: > *; cls: uk-animation-scale-up; delay: 100 ;repeat: true">
            
            <div className="grid grid-cols-2 gap-4 gap-y-7">
              
              {/* First Name */}
              <div>
                <label htmlFor="firstName" className="">First name</label>
                <div className="mt-2.5">
                  <input 
                    id="firstName" 
                    name="firstName" 
                    type="text" 
                    autoFocus 
                    placeholder="First name" 
                    required 
                    value={formData.firstName}
                    onChange={handleChange}
                    className="!w-full !rounded-lg !bg-transparent !shadow-sm !border-slate-200 dark:!border-slate-800 dark:!bg-white/5"
                  /> 
                </div>
              </div>

              {/* Last Name */}
              <div>
                <label htmlFor="lastName" className="">Last name</label>
                <div className="mt-2.5">
                  <input 
                    id="lastName" 
                    name="lastName" 
                    type="text" 
                    placeholder="Last name" 
                    required 
                    value={formData.lastName}
                    onChange={handleChange}
                    className="!w-full !rounded-lg !bg-transparent !shadow-sm !border-slate-200 dark:!border-slate-800 dark:!bg-white/5"
                  /> 
                </div>
              </div>

              {/* Email */}
              <div className="col-span-2">
                <label htmlFor="email" className="">Email address</label>
                <div className="mt-2.5">
                  <input 
                    id="email" 
                    name="email" 
                    type="email" 
                    placeholder="Email" 
                    required 
                    value={formData.email}
                    onChange={handleChange}
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
                    value={formData.password}
                    onChange={handleChange}
                    className="!w-full !rounded-lg !bg-transparent !shadow-sm !border-slate-200 dark:!border-slate-800 dark:!bg-white/5"
                  />  
                </div>
              </div>

              {/* Confirm Password */}
              <div>
                <label htmlFor="confirmPassword" className="">Confirm Password</label>
                <div className="mt-2.5">
                  <input 
                    id="confirmPassword" 
                    name="confirmPassword" 
                    type="password" 
                    placeholder="***" 
                    required
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    className="!w-full !rounded-lg !bg-transparent !shadow-sm !border-slate-200 dark:!border-slate-800 dark:!bg-white/5"
                  />  
                </div>
              </div>

              {/* Terms Checkbox */}
              <div className="col-span-2">
                <label className="inline-flex items-center" htmlFor="acceptTerms">
                  <input 
                    type="checkbox" 
                    id="acceptTerms" 
                    name="acceptTerms"
                    checked={formData.acceptTerms}
                    onChange={handleChange}
                    className="!rounded-md accent-red-800" 
                  />
                  <span className="ml-2">you agree to our <a href="#" className="text-blue-700 hover:underline">terms of use </a> </span>
                </label>
              </div>

              {/* Submit Button */}
              <div className="col-span-2">
                <button type="submit" className="button bg-primary text-white w-full">Get Started</button>
              </div>

            </div>

            <div className="text-center flex items-center gap-6"> 
              <hr className="flex-1 border-slate-200 dark:border-slate-800" /> 
              Or continue with  
              <hr className="flex-1 border-slate-200 dark:border-slate-800" />
            </div> 

            {/* Social Signup */}
            <div className="flex gap-2" uk-scrollspy="target: > *; cls: uk-animation-scale-up; delay: 400 ;repeat: true">
              <button 
                type="button"
                onClick={handleGoogleSignup}
                className="button flex-1 flex items-center justify-center gap-2 bg-red-600 text-white text-sm hover:bg-red-700"
              > 
                <IonIcon icon={logoGoogle} className="text-lg" /> Google  
              </button>
              <a href="#" className="button flex-1 flex items-center justify-center gap-2 bg-primary text-white text-sm"> 
                <IonIcon icon={logoFacebook} className="text-lg" /> Facebook  
              </a>
              <a href="#" className="button flex-1 flex items-center justify-center gap-2 bg-sky-600 text-white text-sm"> 
                <IonIcon icon={logoTwitter} /> Twitter  
              </a>
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
                  <img className="w-12" src="/assets/images/logo-icon.png" alt="Socialite" />
                  <h4 className="!text-white text-2xl font-semibold mt-7" uk-slideshow-parallax="y: 600,0,0">  Connect With Friends </h4> 
                  <p className="!text-white text-lg mt-7 leading-8" uk-slideshow-parallax="y: 800,0,0;"> Join millions of people sharing their moments and connecting with friends around the world.</p>   
                </div> 
              </div>
              <div className="w-full h-96 bg-gradient-to-t from-black absolute bottom-0 left-0"></div>
            </li>
            <li className="w-full">
              <img src="/assets/images/post/img-2.jpg" alt="" className="w-full h-full object-cover uk-animation-kenburns uk-animation-reverse uk-transform-origin-center-left" />
              <div className="absolute bottom-0 w-full uk-tr ansition-slide-bottom-small z-10">
                <div className="max-w-xl w-full mx-auto pb-32 px-5 z-30 relative" uk-scrollspy="target: > *; cls: uk-animation-scale-up; delay: 100 ;repeat: true"> 
                  <img className="w-12" src="/assets/images/logo-icon.png" alt="Socialite" />
                  <h4 className="!text-white text-2xl font-semibold mt-7" uk-slideshow-parallax="y: 800,0,0">  Share Your Story </h4> 
                  <p className="!text-white text-lg mt-7 leading-8" uk-slideshow-parallax="y: 800,0,0;"> Create your profile, share your experiences, and discover what's happening around you.</p>   
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

export default RegisterPage;

