export const isElementInViewport = (
  el: HTMLElement,
  customTop = 0
): boolean => {
  const rect = el.getBoundingClientRect();
  return (
    rect.top >= customTop &&
    rect.left >= 0 &&
    rect.bottom <=
      (window.innerHeight || document.documentElement.clientHeight) &&
    rect.right <= (window.innerWidth || document.documentElement.clientWidth)
  );
};
