(function () {
  'use strict';

  const normalize = value => (value || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/đ/g, 'd')
    .replace(/Đ/g, 'D')
    .toUpperCase()
    .replace(/[^A-Z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '');

  const actionType = control => {
    const label = normalize([
      control.textContent,
      control.getAttribute('title'),
      control.getAttribute('aria-label')
    ].filter(Boolean).join(' '));

    if (/(^|_)MO_KHOA(_|$)/.test(label)) {
      return { name: 'restore', icon: 'fas fa-lock-open', label: 'Mở khóa' };
    }

    if (/(^|_)KHOI_PHUC(_|$)/.test(label)) {
      return { name: 'restore', icon: 'fas fa-rotate-left', label: 'Khôi phục' };
    }

    if (/(^|_)CAP_QUYEN(_|$)/.test(label)) {
      return { name: 'permission', icon: 'fas fa-user-shield', label: 'Cấp quyền' };
    }

    if (/(^|_)PHIEN_BAN(_|$)/.test(label)) {
      return { name: 'reference', icon: 'fas fa-layer-group', label: 'Phiên bản' };
    }

    if (/(^|_)LICH_SU(_|$)/.test(label)) {
      return { name: 'reference', icon: 'fas fa-clock-rotate-left', label: 'Lịch sử' };
    }

    if (/(^|_)(PHE_DUYET|DUYET)(_|$)/.test(label)) {
      return { name: 'approve', icon: 'fas fa-check', label: 'Duyệt' };
    }

    if (/(^|_)TU_CHOI(_|$)/.test(label)) {
      return { name: 'danger', icon: 'fas fa-xmark', label: 'Từ chối' };
    }

    if (/(^|_)XOA(_|$)/.test(label)) {
      return { name: 'danger', icon: 'fas fa-trash-can', label: 'Xóa' };
    }

    if (/(^|_)HUY(_|$)/.test(label)) {
      return { name: 'danger', icon: 'fas fa-ban', label: 'Hủy' };
    }

    if (/(NGUNG_BAN|NGUNG_HOP_TAC|NGUNG_SU_DUNG|CHO_NGHI_VIEC)/.test(label)) {
      return { name: 'danger', icon: 'fas fa-circle-stop', label: 'Ngừng hoạt động' };
    }

    if (/(^|_)KHOA(_|$)/.test(label)) {
      return { name: 'danger', icon: 'fas fa-lock', label: 'Khóa' };
    }

    if (/(^|_)(SUA|CHINH_SUA)(_|$)/.test(label)) {
      return { name: 'edit', icon: 'fas fa-pen', label: 'Sửa' };
    }

    if (/(^|_)(XEM|CHI_TIET)(_|$)/.test(label)) {
      return { name: 'view', icon: 'fas fa-eye', label: 'Xem chi tiết' };
    }

    return null;
  };

  const enhanceControl = control => {
    if (control.dataset.tableActionReady === 'true') return;

    const type = actionType(control);
    if (!type) return;

    control.dataset.tableActionReady = 'true';
    control.dataset.tableActionKind = type.name;
    control.classList.add('table-action-button', `table-action-button--${type.name}`);

    control.querySelectorAll('i.table-action-button__icon, i.fas, i.fa, i.fa-solid')
      .forEach(existingIcon => existingIcon.remove());

    const icon = document.createElement('i');
    icon.className = `${type.icon} table-action-button__icon`;
    icon.setAttribute('aria-hidden', 'true');
    control.insertBefore(icon, control.firstChild);

    if (!control.getAttribute('aria-label')) {
      control.setAttribute('aria-label', type.label);
    }
  };

  const enhanceCell = cell => {
    cell.classList.add('table-action-cell');
    cell.querySelectorAll('a, button').forEach(enhanceControl);
  };

  const enhanceTable = table => {
    const headerRows = table.querySelectorAll('thead tr');
    if (!headerRows.length) return;

    const headers = Array.from(headerRows[headerRows.length - 1].querySelectorAll('th'));
    const actionColumns = headers
      .map((header, index) => {
        const text = normalize(header.textContent);
        return text.includes('THAO_TAC') || text.includes('HANH_DONG') || text === 'CHI_TIET'
          ? index
          : -1;
      })
      .filter(index => index >= 0);

    if (!actionColumns.length) return;

    table.querySelectorAll('tbody tr').forEach(row => {
      const cells = Array.from(row.children).filter(child => child.matches('td, th'));
      actionColumns.forEach(index => {
        if (cells[index]) enhanceCell(cells[index]);
      });
    });
  };

  const enhance = root => {
    if (root.matches?.('table')) enhanceTable(root);
    root.querySelectorAll?.('table').forEach(enhanceTable);

    const actionGroups = '.table-actions, .directory-actions, .customer-actions, .brand-actions';
    if (root.matches?.(actionGroups)) root.querySelectorAll('a, button').forEach(enhanceControl);
    root.querySelectorAll?.(actionGroups).forEach(group => {
      group.querySelectorAll('a, button').forEach(enhanceControl);
    });
  };

  const init = () => {
    enhance(document);
    new MutationObserver(mutations => mutations.forEach(mutation =>
      mutation.addedNodes.forEach(node => {
        if (node.nodeType === Node.ELEMENT_NODE) enhance(node);
      })
    )).observe(document.body, { childList: true, subtree: true });
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init, { once: true });
  } else {
    init();
  }
})();
