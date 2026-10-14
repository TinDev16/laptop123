(() => {
  'use strict';

  const normalize = value => String(value ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim();

  const isDateTimeHeader = text => {
    const header = normalize(text);
    return header.includes('thoi gian') ||
      header.startsWith('ngay') ||
      header.startsWith('han') ||
      header.includes('cap nhat');
  };

  const parseParts = value => {
    const text = String(value ?? '').trim();
    if (!text || text === '-' || text === '—') return null;

    let match = text.match(
      /^(\d{1,2})\/(\d{1,2})\/(\d{4})(?:\s+(\d{1,2}):(\d{1,2})(?::(\d{1,2}))?)?$/
    );
    if (match) {
      return [match[3], match[2], match[1], match[4] || 0, match[5] || 0, match[6] || 0];
    }

    match = text.match(
      /^(\d{4})-(\d{1,2})-(\d{1,2})(?:[T\s]+(\d{1,2}):(\d{1,2})(?::(\d{1,2}))?(?:\.\d+)?)?(?:Z|[+-]\d{2}:?\d{2})?$/
    );
    if (match) {
      return [match[1], match[2], match[3], match[4] || 0, match[5] || 0, match[6] || 0];
    }

    return null;
  };

  const format = value => {
    const parts = parseParts(value);
    if (!parts) return String(value ?? '').trim();
    const [year, month, day, hour, minute, second] = parts;
    const pad = number => String(number).padStart(2, '0');
    return `${pad(day)}/${pad(month)}/${year} ${pad(hour)}:${pad(minute)}:${pad(second)}`;
  };

  const formatElement = element => {
    if (!element || element.dataset.dateTimeReady === 'true') return;
    const formatted = format(element.textContent);
    if (!formatted) return;
    element.textContent = formatted;
    element.dataset.dateTimeReady = 'true';
  };

  const formatTable = table => {
    const headers = Array.from(table.querySelectorAll('thead th'));
    const columns = headers
      .map((header, index) => isDateTimeHeader(header.textContent) ? index : -1)
      .filter(index => index >= 0);

    if (!columns.length) return;

    table.querySelectorAll('tbody tr').forEach(row => {
      const cells = Array.from(row.children).filter(cell => cell.matches('td,th'));
      columns.forEach(index => {
        const cell = cells[index];
        if (cell) formatElement(cell);
      });
    });
  };

  const enhance = root => {
    if (root.matches?.('[data-date-time]')) formatElement(root);
    root.querySelectorAll?.('[data-date-time]').forEach(formatElement);
    if (root.matches?.('table')) formatTable(root);
    root.querySelectorAll?.('table').forEach(formatTable);
  };

  const init = () => {
    enhance(document);
    new MutationObserver(mutations => mutations.forEach(mutation =>
      mutation.addedNodes.forEach(node => {
        if (node.nodeType === Node.ELEMENT_NODE) enhance(node);
      })
    )).observe(document.body, { childList: true, subtree: true });
  };

  window.Lap123DateTime = { format, enhance };
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init);
  else init();
})();
