import { debounce } from 'lodash';
import { useCallback, useRef } from 'react';

type CallbackFunction = (...args: any[]) => void;

const useDebouncedCallback = (callback: CallbackFunction, delay: number) => {
  const callbackRef = useRef(callback);

  callbackRef.current = callback;

  const debouncedFunction = useCallback(
    debounce((...args) => {
      callbackRef.current?.(...args);
    }, delay),
    [delay]
  );

  return debouncedFunction;
};

export default useDebouncedCallback;
