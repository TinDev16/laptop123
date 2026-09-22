(() => {
  'use strict';
  const INPUTS = 'input[type="number"]:not([data-number-format="plain"]), input[data-number-input]';
  const HEADERS = ['so luong','don gia','gia nhap','gia xuat','tong tien','thanh tien','ton','ton kho','ton toi thieu','dinh muc','hao hut','nhu cau','thieu','truoc giao dich','sau giao dich','can dung','kha dung'];
  const norm = value => String(value ?? '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();
  const decimals = input => {
    const step = input.dataset.numberStep || input.getAttribute('step') || '1';
    if (step === 'any') return 6;
    const dot = step.indexOf('.');
    return dot < 0 ? 0 : Math.min(6, step.length - dot - 1);
  };
  const parseVi = value => {
    let text = String(value ?? '').trim().replace(/\s+/g, '');
    if (!text) return null;
    text = text.replace(/[^\d,.-]/g, '');
    if (text.includes(',')) text = text.replace(/\./g, '').replace(',', '.');
    else if ((text.match(/\./g) || []).length > 1 || /^-?\d{1,3}(?:\.\d{3})+$/.test(text)) text = text.replace(/\./g, '');
    const number = Number(text);
    return Number.isFinite(number) ? number : null;
  };
  const parseInitial = value => {
    const text = String(value ?? '').trim();
    if (!text) return null;
    if (/^-?\d+(?:\.\d+)?$/.test(text)) {
      const number = Number(text);
      return Number.isFinite(number) ? number : null;
    }
    return parseVi(text);
  };
  const parseDisplay = value => {
    const text = String(value ?? '').trim().replace(/\s+/g, '');
    if (/^-?\d{1,3}(?:[.,]\d{3})+$/.test(text)) {
      const number = Number(text.replace(/[.,]/g, ''));
      return Number.isFinite(number) ? number : null;
    }
    return parseVi(text);
  };
  const format = (value, maxFractionDigits = 6) => {
    const number = typeof value === 'number' ? value : parseInitial(value);
    if (number === null || !Number.isFinite(number)) return '';
    return new Intl.NumberFormat('vi-VN', { useGrouping: true, minimumFractionDigits: 0, maximumFractionDigits: maxFractionDigits }).format(number);
  };
  const raw = input => {
    const number = parseVi(input.value);
    if (number === null) return '';
    return decimals(input) === 0 ? String(Math.trunc(number)) : String(number);
  };
  const validate = input => {
    const number = parseVi(input.value);
    let message = '';
    if (input.required && number === null) message = 'Vui long nhap mot so hop le.';
    if (number !== null && input.dataset.numberMin !== '' && number < Number(input.dataset.numberMin)) message = `Gia tri nho nhat la ${format(input.dataset.numberMin)}.`;
    if (number !== null && input.dataset.numberMax !== '' && number > Number(input.dataset.numberMax)) message = `Gia tri lon nhat la ${format(input.dataset.numberMax)}.`;
    input.setCustomValidity(message);
  };
  const formatTyping = input => {
    const places = decimals(input);
    let text = input.value.replace(/\s+/g, '').replace(/[^\d,.-]/g, '');
    const negative = text.startsWith('-');
    text = text.replace(/-/g, '');
    let integer = text;
    let fraction = '';
    if (places > 0 && text.includes(',')) {
      const parts = text.split(',');
      integer = parts.shift();
      fraction = parts.join('').slice(0, places);
    }
    integer = integer.replace(/\./g, '').replace(/^0+(?=\d)/, '') || '0';
    integer = integer.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
    input.value = `${negative ? '-' : ''}${integer}${places > 0 && text.includes(',') ? ',' + fraction : ''}`;
    validate(input);
  };
  const enhanceInput = input => {
    if (input.__lap123NumberReady) return;
    const initial = input.value;
    input.__lap123NumberReady = true;
    input.dataset.numberReady = 'true';
    input.dataset.numberInput = 'true';
    input.dataset.numberMin = input.getAttribute('min') || '';
    input.dataset.numberMax = input.getAttribute('max') || '';
    input.dataset.numberStep = input.getAttribute('step') || '1';
    input.type = 'text';
    input.inputMode = decimals(input) > 0 ? 'decimal' : 'numeric';
    input.autocomplete = 'off';
    input.classList.add('number-input');
    const number = parseInitial(initial);
    input.value = number === null ? '' : format(number, decimals(input));
    validate(input);
    input.addEventListener('input', () => formatTyping(input));
    input.addEventListener('blur', () => {
      const current = parseVi(input.value);
      if (current !== null) input.value = format(current, decimals(input));
      validate(input);
    });
  };
  const formatElement = element => {
    if (!element || element.children.length || element.closest('script,style,input,textarea,select')) return;
    const original = element.textContent.trim();
    const match = original.match(/^(-?[\d.,]+)(\s*(?:VND|VNĐ|%|sp|chiếc|bộ|cái)?)$/i);
    if (!match) return;
    const number = element.hasAttribute('data-number') ? parseInitial(match[1]) : parseDisplay(match[1]);
    if (number === null) return;
    element.textContent = `${format(number, match[2].trim() === '%' ? 2 : 6)}${match[2]}`;
    element.classList.add('number-value');
  };
  const formatTable = table => {
    const headers = Array.from(table.querySelectorAll('thead th'));
    const columns = headers.map((header,index) => HEADERS.some(key => norm(header.textContent).includes(key)) ? index : -1).filter(index => index >= 0);
    if (!columns.length) return;
    table.querySelectorAll('tbody tr').forEach(row => {
      const cells = Array.from(row.children).filter(cell => cell.matches('td,th'));
      columns.forEach(index => {
        const cell = cells[index];
        if (!cell) return;
        if (!cell.children.length) formatElement(cell);
        else cell.querySelectorAll('span,strong,small').forEach(formatElement);
      });
    });
  };
  const enhance = root => {
    if (root.matches?.(INPUTS)) enhanceInput(root);
    root.querySelectorAll?.(INPUTS).forEach(enhanceInput);
    if (root.matches?.('[data-number],.number-value,.stat-value,.metric-value,.kpi-value')) formatElement(root);
    root.querySelectorAll?.('[data-number],.number-value,.stat-value,.metric-value,.kpi-value').forEach(formatElement);
    if (root.matches?.('table')) formatTable(root);
    root.querySelectorAll?.('table').forEach(formatTable);
  };
  const prepareForm = form => form.querySelectorAll('input[data-number-input]').forEach(input => { input.value = raw(input); validate(input); });
  const init = () => {
    enhance(document);
    document.addEventListener('submit', event => prepareForm(event.target), true);
    new MutationObserver(mutations => mutations.forEach(mutation => mutation.addedNodes.forEach(node => {
      if (node.nodeType === Node.ELEMENT_NODE) enhance(node);
    }))).observe(document.body, { childList: true, subtree: true });
  };
  window.Lap123Number = { format, parse: parseVi, raw, enhance, prepareForm };
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init);
  else init();
})();
