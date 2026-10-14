(function () {
  'use strict';

  const archivedGroups = [
    {
      matches: text => /(SAN_PHAM|THANH_PHAM).*NGUNG_BAN/.test(text),
      showLabel: 'Xem danh sách ngừng bán',
      hideLabel: 'Ẩn danh sách ngừng bán'
    },
    {
      matches: text => /NHA_CUNG_CAP.*NGUNG_HOP_TAC/.test(text),
      showLabel: 'Xem nhà cung cấp ngừng hợp tác',
      hideLabel: 'Ẩn nhà cung cấp ngừng hợp tác'
    },
    {
      matches: text => /KHACH_HANG.*(KHONG_CON_SU_DUNG|NGUNG_HOAT_DONG)/.test(text),
      showLabel: 'Xem khách hàng không còn sử dụng',
      hideLabel: 'Ẩn khách hàng không còn sử dụng'
    },
    {
      matches: text => /THUONG_HIEU.*(NGUNG_SU_DUNG|NGUNG_HOAT_DONG)/.test(text),
      showLabel: 'Xem thương hiệu đã ngừng sử dụng',
      hideLabel: 'Ẩn thương hiệu đã ngừng sử dụng'
    },
    {
      matches: text => /NHAN_VIEN.*NGHI_VIEC/.test(text),
      showLabel: 'Xem nhân viên đã nghỉ việc',
      hideLabel: 'Ẩn nhân viên đã nghỉ việc'
    },
    {
      matches: text => /TAI_KHOAN.*KHOA/.test(text),
      showLabel: 'Xem tài khoản đã khóa',
      hideLabel: 'Ẩn tài khoản đã khóa'
    }
  ];

  const normalize = value => (value || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/đ/g, 'd')
    .replace(/Đ/g, 'D')
    .toUpperCase()
    .replace(/[^A-Z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '');

  const setExpanded = (section, button, config, expanded) => {
    section.hidden = !expanded;
    button.setAttribute('aria-expanded', String(expanded));
    button.classList.toggle('is-expanded', expanded);

    const icon = button.querySelector('i');
    const label = button.querySelector('span');
    if (icon) icon.className = expanded ? 'fas fa-eye-slash' : 'fas fa-eye';
    if (label) label.textContent = expanded ? config.hideLabel : config.showLabel;
  };

  const createToggle = (section, config, index) => {
    if (section.dataset.archiveToggleReady === 'true') return;

    section.dataset.archiveToggleReady = 'true';
    section.classList.add('archive-section');
    section.id = section.id || `archive-section-${index + 1}`;

    const toolbar = document.createElement('div');
    toolbar.className = 'archive-section-access';

    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'archive-section-toggle';
    button.setAttribute('aria-controls', section.id);
    button.innerHTML = '<i class="fas fa-eye" aria-hidden="true"></i><span></span><i class="fas fa-chevron-down archive-section-toggle__chevron" aria-hidden="true"></i>';

    button.addEventListener('click', () => {
      setExpanded(section, button, config, button.getAttribute('aria-expanded') !== 'true');
    });

    toolbar.appendChild(button);
    section.parentNode.insertBefore(toolbar, section);
    setExpanded(section, button, config, false);
  };

  const init = () => {
    const claimedSections = new Set();

    document.querySelectorAll('h1, h2, h3, h4').forEach((heading, index) => {
      const normalizedHeading = normalize(heading.textContent);
      const config = archivedGroups.find(group => group.matches(normalizedHeading));
      if (!config) return;

      const section = heading.closest('section');
      if (!section || claimedSections.has(section)) return;

      claimedSections.add(section);
      createToggle(section, config, index);
    });
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init, { once: true });
  } else {
    init();
  }
})();
