import React from 'react';
import { useLoading } from '../contexts/LoadingContext';
import Loader from './Loader';

const GlobalLoader: React.FC = () => {
  const { isLoading, loadingMessage } = useLoading();

  if (!isLoading) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[9999]">
      <div className="bg-white dark:bg-slate-800 rounded-2xl p-8 shadow-2xl">
        <Loader size="lg" text={loadingMessage} />
      </div>
    </div>
  );
};

export default GlobalLoader;
