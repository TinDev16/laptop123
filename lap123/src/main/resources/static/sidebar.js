(() => {
  const sidebar = document.querySelector('.sidebar');
  if (!sidebar) return;

  const key = 'lap123.sidebar.scrollTop';
  const savePosition = () => {
    try { sessionStorage.setItem(key, String(sidebar.scrollTop)); } catch (_) { /* storage may be disabled */ }
  };

  try {
    const saved = sessionStorage.getItem(key);
    if (saved !== null) {
      sidebar.scrollTop = Math.max(0, Number(saved) || 0);
    } else {
      sidebar.querySelector('.menu-item.active')?.scrollIntoView({ block: 'nearest' });
    }
  } catch (_) { /* navigation still works without storage */ }

  sidebar.addEventListener('scroll', savePosition, { passive: true });
  window.addEventListener('pagehide', savePosition);
})();