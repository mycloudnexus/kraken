import { useCallback, useEffect, useRef, useState } from "react";

interface Size {
  width: number;
  height: number;
}

export default function useSize(
  targetRef: React.RefObject<HTMLElement>
): Size | undefined {
  const [size, setSize] = useState<Size>();

  const observer = useRef<ResizeObserver | null>(null);

  const updateSize = useCallback((entries: ResizeObserverEntry[]) => {
    if (!Array.isArray(entries)) return;
    const entry = entries[0];
    if (entry) {
      const { width, height } = entry.contentRect;
      setSize({ width, height });
    }
  }, []);

  useEffect(() => {
    if (observer.current) {
      observer.current.disconnect();
    }

    observer.current = new ResizeObserver(updateSize);

    const element = targetRef.current;
    if (element) {
      observer.current.observe(element);
    }

    // 清理 observer
    return () => {
      if (observer.current) {
        observer.current.disconnect();
      }
    };
  }, [targetRef, updateSize]);

  return size;
}
