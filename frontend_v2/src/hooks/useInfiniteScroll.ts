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
        console.log('👁️ Intersection observer triggered', { 
          isIntersecting: target.isIntersecting, 
          hasMore, 
          loading, 
          isFetching 
        });
        if (target.isIntersecting && hasMore && !loading && !isFetching) {
          console.log('🚀 Infinite scroll triggered - loading more content');
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

    if (loadingRef.current) {
      observer.observe(loadingRef.current);
      console.log('Intersection observer attached to loading element');
    } else {
      console.log('⚠️ loadingRef.current is null - observer not attached');
    }

    return () => {
      if (observerRef.current) {
        observerRef.current.disconnect();
      }
    };
  }, [hasMore, loading, threshold, isFetching]); // Removed onLoadMore to prevent observer recreation

  return { loadingRef, isFetching };
};
