import { useEffect, useRef, useState } from 'react';

interface UseInfiniteScrollOptions {
  hasMore: boolean;
  loading: boolean;
  onLoadMore: () => Promise<void>;
  threshold?: number;
}

export const useInfiniteScroll = ({ 
  hasMore, 
  loading, 
  onLoadMore, 
  threshold = 100 
}: UseInfiniteScrollOptions) => {
  const [isFetching, setIsFetching] = useState(false);
  const observerRef = useRef<IntersectionObserver | null>(null);
  const loadingRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    // Always create observer, but only trigger if conditions are met
    const observer = new IntersectionObserver(
      (entries) => {
        const target = entries[0];
        if (target.isIntersecting && hasMore && !loading && !isFetching) {
          setIsFetching(true);
          onLoadMore().finally(() => {
            setIsFetching(false);
          });
        }
      },
      {
        threshold: 0.1,
        rootMargin: `${threshold}px`,
      }
    );

    observerRef.current = observer;

    // Use a longer delay to ensure the loading element is rendered
    const attachObserver = () => {
      if (loadingRef.current) {
        observer.observe(loadingRef.current);
        
        // Check visibility after a short delay
        setTimeout(() => {
          if (loadingRef.current) {
            const rect = loadingRef.current.getBoundingClientRect();
            const isVisible = rect.top < window.innerHeight && rect.bottom > 0;
            
            // If element is already visible and conditions are met, trigger immediately
            if (isVisible && hasMore && !loading && !isFetching) {
              setIsFetching(true);
              onLoadMore().finally(() => {
                setIsFetching(false);
              });
            } else {
            }
          }
        }, 50);
      } else {
        setTimeout(attachObserver, 100);
      }
    };

    // Start trying to attach the observer
    attachObserver();

    return () => {
      if (observerRef.current) {
        observerRef.current.disconnect();
      }
    };
  }, [hasMore, loading, threshold]); // Removed isFetching to prevent observer recreation

  return { loadingRef, isFetching };
};
