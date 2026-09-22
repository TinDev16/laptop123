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


  document.querySelectorAll('a').forEach(link => {
    const label = link.textContent.replace(/\s+/g, ' ').trim().toLowerCase();
    if (/^(về danh sách|quay lại|quay lại danh sách|trở lại|trở về)/.test(label)) {
      link.classList.add('btn', 'btn-back');
      if (!link.querySelector('i')) {
        const icon = document.createElement('i');
        icon.className = 'fas fa-arrow-left';
        icon.setAttribute('aria-hidden', 'true');
        link.prepend(icon);
      }
    }
  });
})();