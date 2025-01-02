import { debounce } from "lodash";
import { useEffect, useState } from "react";

export function useContainerHeight(containerRef: React.MutableRefObject<HTMLDivElement | null>) {
  const [scrollHeight, setScrollHeight] = useState(0)

  const debounced = debounce((value: number) => setScrollHeight(value), 500)

  useEffect(() => {
    const onResized = () => {
      const { height = 0 } = containerRef.current?.getBoundingClientRect() ?? {}
      debounced(height)
    }

    window.addEventListener('resize', onResized)

    setTimeout(() => window.dispatchEvent(new Event('resize')), 1000)

    return () => {
      window.removeEventListener('resize', onResized)
    }
  }, [])

  return [scrollHeight]
}
