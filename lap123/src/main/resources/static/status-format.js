(() => {
  'use strict';

  const LABELS = {
    CHO_DUYET: 'Chờ duyệt',
    CHO_XAC_NHAN: 'Chờ xác nhận',
    NHAP_NHAP: 'Bản nháp',
    DA_DUYET: 'Đã duyệt',
    DA_GIAO: 'Đã giao',
    DANG_SAN_XUAT: 'Đang sản xuất',
    HOAN_THANH: 'Hoàn thành',
    DA_XUAT: 'Đã xuất kho',
    DA_NHAP: 'Đã nhập kho',
    DA_NHAP_KHO: 'Đã nhập kho',
    TU_CHOI: 'Từ chối',
    DA_TU_CHOI: 'Đã từ chối',
    HIEU_LUC: 'Hiệu lực',
    NGUNG_SU_DUNG: 'Ngừng sử dụng',
    HOAT_DONG: 'Hoạt động',
    NGUNG_HOAT_DONG: 'Ngừng hoạt động'
  };

  const normalize = value => String(value ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/Đ/g, 'D')
    .replace(/đ/g, 'd')
    .trim()
    .toUpperCase()
    .replace(/[\s-]+/g, '_');

  const variant = normalized => {
    const success = ['HIEU_LUC', 'HOAT_DONG', 'DANG_BAN', 'MO_BAN', 'DANG_HOP_TAC',
      'DANG_LAM_VIEC', 'HOAN_THANH', 'HOAN_TAT', 'DA_DUYET', 'DA_XUAT', 'DA_XUAT_KHO',
      'DA_NHAP', 'DA_NHAP_KHO'];
    const warning = ['CHO_DUYET', 'CHO_XAC_NHAN', 'DANG_XU_LY', 'DANG_SAN_XUAT',
      'BAN_NHAP', 'NHAP_NHAP', 'SAP_HET_HANG'];
    const danger = ['TU_CHOI', 'DA_TU_CHOI', 'LOI', 'THAT_BAI', 'HUY', 'DA_HUY'];
    const neutral = ['NGUNG_SU_DUNG', 'NGUNG_HOAT_DONG', 'DA_NGUNG_BAN', 'NGUNG_BAN',
      'DA_NGUNG_HOP_TAC', 'DA_NGHI_VIEC', 'NGHI_VIEC', 'DONG', 'DA_KHOA', 'BI_KHOA'];
    const info = ['DA_GIAO', 'MOI_TAO'];

    if (neutral.some(key => normalized.includes(key))) return 'neutral';
    if (danger.some(key => normalized.includes(key))) return 'danger';
    if (warning.some(key => normalized.includes(key))) return 'warning';
    if (success.some(key => normalized.includes(key))) return 'success';
    if (info.some(key => normalized.includes(key))) return 'info';
    return 'info';
  };

  const semanticVariant = element => {
    const groups = {
      success: ['status-success', 'success', 'badge-hoantat', 'badge-open',
        'directory-state--active', 'account-status-active'],
      warning: ['status-warning', 'pending', 'badge-xuly'],
      danger: ['status-danger', 'danger'],
      neutral: ['status-neutral', 'badge-closed', 'directory-state--archived',
        'account-status-locked'],
      info: ['status-info', 'info']
    };

    return Object.entries(groups)
      .find(([, classes]) => classes.some(className => element.classList.contains(className)))?.[0];
  };

  const decorate = element => {
    if (!element || element.dataset.statusReady === 'true') return;
    const original = element.textContent.trim();
    if (!original) return;
    const normalized = normalize(original);
    if (LABELS[normalized]) element.textContent = LABELS[normalized];
    element.dataset.statusReady = 'true';
    element.classList.add('status-badge', 'status-' + (semanticVariant(element) || variant(normalized)));
  };

  const decorateTable = table => {
    const headers = Array.from(table.querySelectorAll('thead th'));
    const columns = headers
      .map((header, index) => normalize(header.textContent).includes('TRANG_THAI') ? index : -1)
      .filter(index => index >= 0);

    table.querySelectorAll('tbody tr').forEach(row => {
      const cells = Array.from(row.children).filter(cell => cell.matches('td,th'));
      columns.forEach(index => {
        const cell = cells[index];
        if (!cell) return;
        const existing = cell.querySelector('.status-pill,.directory-state,.directory-status,.badge-status,[data-status]');
        if (existing) {
          decorate(existing);
          return;
        }
        if (cell.textContent.trim()) {
          const badge = document.createElement('span');
          badge.textContent = cell.textContent.trim();
          cell.replaceChildren(badge);
          decorate(badge);
        }
      });
    });
  };

  const enhance = root => {
    const selector = '.status-pill,.directory-state,.directory-status,.badge-status,[data-status]';
    if (root.matches?.(selector)) decorate(root);
    root.querySelectorAll?.(selector).forEach(decorate);
    if (root.matches?.('table')) decorateTable(root);
    root.querySelectorAll?.('table').forEach(decorateTable);
  };

  const init = () => {
    enhance(document);
    new MutationObserver(mutations => mutations.forEach(mutation =>
      mutation.addedNodes.forEach(node => {
        if (node.nodeType === Node.ELEMENT_NODE) enhance(node);
      })
    )).observe(document.body, { childList: true, subtree: true });
  };

  window.Lap123Status = { enhance };
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init);
  else init();
})();
