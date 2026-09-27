(() => {
  'use strict';

  const PAGE_SIZE = 10;

  const normalize = value =>
      String(value)
          .normalize('NFD')
          .replace(/[\u0300-\u036f]/g, '')
          .replace(/đ/gi, 'd')
          .toLocaleLowerCase('vi')
          .replace(/\s+/g, ' ')
          .trim();

  const cellText = cell => {
    if (!cell) return '';

    const copy = cell.cloneNode(true);

    copy.querySelectorAll('select').forEach((select, index) => {
      const original = cell.querySelectorAll('select')[index];

      select.replaceWith(
          original?.selectedOptions?.[0]?.textContent || ''
      );
    });

    copy.querySelectorAll('input,textarea').forEach((input, index) => {
      const original = cell.querySelectorAll('input,textarea')[index];

      input.replaceWith(
          original?.type === 'hidden'
              ? ''
              : (original?.value || '')
      );
    });

    copy.querySelectorAll('button,script').forEach(node => node.remove());

    return copy.textContent.replace(/\s+/g, ' ').trim();
  };

  const create = (tag, className, text) => {
    const node = document.createElement(tag);

    node.className = className;

    if (text) {
      node.textContent = text;
    }

    return node;
  };


  /* ============================================================
     XÁC ĐỊNH DÒNG DỮ LIỆU THẬT
     ============================================================ */

  function isBomVersionRow(row) {
    return row?.classList?.contains('bom-version-row');
  }

  function isEmptyRow(row) {
    return row?.classList?.contains('empty-row')
        || row?.classList?.contains('directory-empty');
  }

  function isDataRow(row) {
    return row?.classList?.contains('directory-row')
        && !isBomVersionRow(row)
        && !isEmptyRow(row);
  }


  /* ============================================================
     ENHANCE TABLE
     ============================================================ */

  function enhance(table) {

    if (
        table.dataset.listReady ||
        !table.tHead ||
        !table.tBodies.length
    ) {
      return;
    }

    table.dataset.listReady = 'true';

    const body = table.tBodies[0];

    const headers =
        Array.from(
            table.tHead.rows[0].cells
        );

    const columns =
        headers
            .map((header, index) => ({
              index,
              label: header.textContent.trim()
            }))
            .filter(column =>
                column.label &&
                !/^(thao tac|hanh dong|hinh anh|anh|chon|xoa|stt|chi tiet)$/
                    .test(normalize(column.label))
            );

    if (!columns.length) {
      return;
    }


    /* ============================================================
       TOOLBAR
       ============================================================ */

    const toolbar =
        create(
            'div',
            'list-controls'
        );

    const searchLabel =
        create(
            'label',
            'list-control',
            'Tìm kiếm'
        );

    const search =
        create(
            'input',
            'form-control'
        );

    search.type = 'search';

    search.placeholder =
        'Nhập từ khóa (có hoặc không dấu)...';

    searchLabel.append(search);

    toolbar.append(searchLabel);


    /* ============================================================
       PAGINATION
       ============================================================ */

    const pager =
        create(
            'nav',
            'list-pagination'
        );

    pager.setAttribute(
        'aria-label',
        'Phân trang danh sách'
    );

    const summary =
        create(
            'span',
            'list-summary'
        );

    summary.setAttribute(
        'aria-live',
        'polite'
    );

    const previous =
        create(
            'button',
            'btn btn-outline',
            'Trước'
        );

    const next =
        create(
            'button',
            'btn btn-outline',
            'Sau'
        );

    previous.type =
        next.type =
            'button';

    const pageLabel =
        create(
            'label',
            'list-page-select',
            'Trang '
        );

    const pageSelect =
        create(
            'select',
            'form-control'
        );

    pageLabel.append(pageSelect);

    pager.append(
        summary,
        previous,
        pageLabel,
        next
    );


    /* ============================================================
       ĐẶT TOOLBAR / PAGINATION
       ============================================================ */

    const wrapper =
        table.closest(
            '.table-container,.table-responsive'
        ) || table;

    wrapper.before(toolbar);
    wrapper.after(pager);


    /* ============================================================
       EMPTY ROW
       ============================================================ */

    const empty =
        create(
            'tr',
            'empty-row directory-empty list-empty'
        );

    const emptyCell =
        create(
            'td',
            '',
            'Không có dữ liệu phù hợp.'
        );

    emptyCell.colSpan =
        headers.length;

    empty.append(emptyCell);


    /* ============================================================
       STATE
       ============================================================ */

    let rows = [];

    let page = 1;

    let matched = [];

    let indexed = [];


    /* ============================================================
       RENDER
       ============================================================ */

    function render() {

      const term =
          normalize(search.value);

      matched =
          indexed
              .filter(item =>
                  item.search.includes(term)
              )
              .map(item => item.row);


      const pages =
          Math.max(
              1,
              Math.ceil(
                  matched.length / PAGE_SIZE
              )
          );


      page =
          Math.min(
              Math.max(page, 1),
              pages
          );


      const visible =
          new Set(
              matched.slice(
                  (page - 1) * PAGE_SIZE,
                  page * PAGE_SIZE
              )
          );


      /* ==========================================================
         HIỆN / ẨN PRODUCT ROW
         ========================================================== */

      rows.forEach(row => {

        const shouldShow =
            visible.has(row);

        row.hidden =
            !shouldShow;

        row.style.display =
            shouldShow
                ? ''
                : 'none';


        /* ========================================================
           BOM ROW

           BOM KHÔNG PHẢI LÀ ROW PHÂN TRANG.

           Nó chỉ đi theo product row tương ứng.
           ======================================================== */

        if (isDataRow(row)) {

          const productId =
              row.getAttribute(
                  'data-product-id'
              );

          if (!productId) {
            return;
          }


          const bomRow =
              body.querySelector(
                  `.bom-version-row[data-product-id="${CSS.escape(productId)}"]`
              );


          if (!bomRow) {
            return;
          }


          /*
           * Nếu product bị ẩn
           * thì BOM cũng phải ẩn.
           */

          if (!shouldShow) {

            bomRow.hidden = true;

            bomRow.style.display =
                'none';

          }

          /*
           * Nếu product hiện nhưng BOM
           * đang đóng thì vẫn ẩn.
           */

          else if (
              !bomRow.classList.contains('bom-version-open')
          ) {

            bomRow.hidden = true;
            bomRow.style.display = 'none';

          }

          else {

            bomRow.hidden = false;
            bomRow.style.removeProperty('display');

          }

        }

      });


      /* ==========================================================
         EMPTY ROW
         ========================================================== */

      empty.hidden =
          matched.length > 0;

      empty.style.display =
          empty.hidden
              ? 'none'
              : '';


      /* ==========================================================
         SUMMARY
         ========================================================== */

      summary.textContent =
          `${
              matched.length
                  ? (page - 1) * PAGE_SIZE + 1
                  : 0
          }–${
              Math.min(
                  page * PAGE_SIZE,
                  matched.length
              )
          } / ${
              matched.length
          } dòng (tổng ${
              rows.length
          }) · Tối đa 10 dòng/trang`;


      /* ==========================================================
         PAGE SELECT
         ========================================================== */

      pageSelect.replaceChildren();

      for (
          let number = 1;
          number <= pages;
          number++
      ) {

        pageSelect.add(
            new Option(
                `${number} / ${pages}`,
                number
            )
        );

      }

      pageSelect.value =
          String(page);


      previous.disabled =
          page === 1;

      next.disabled =
          page === pages;

    }


    /* ============================================================
       SEARCH INDEX
       ============================================================ */

    function updateSearchIndex() {

      indexed =
          rows.map(row => {

            const cells =
                Array.from(
                    row.cells,
                    cellText
                );

            return {
              row,

              search:
                  normalize(
                      columns
                          .map(
                              column =>
                                  cells[column.index] || ''
                          )
                          .join(' ')
                  )
            };

          });

    }


    /* ============================================================
       REFRESH
       ============================================================ */

    function refresh() {

      observer.disconnect();


      /* ==========================================================
         XÓA EMPTY ROW CŨ

         QUAN TRỌNG:
         KHÔNG XÓA BOM ROW
         ========================================================== */

      Array.from(body.rows)
          .filter(row =>
              row !== empty &&
              isEmptyRow(row)
          )
          .forEach(row =>
              row.remove()
          );


      /* ==========================================================
         CHỈ LẤY PRODUCT ROW LÀM DATA ROW

         BOM ROW KHÔNG ĐƯỢC ĐƯA VÀO PAGINATION.
         ========================================================== */

      rows =
          Array.from(body.rows)
              .filter(isDataRow);


      /* ==========================================================
         EMPTY ROW LUÔN Ở CUỐI TABLE
         ========================================================== */

      body.append(empty);


      /* ==========================================================
         INDEX
         ========================================================== */

      updateSearchIndex();


      /* ==========================================================
         RENDER
         ========================================================== */

      render();


      /* ==========================================================
         THEO DÕI THAY ĐỔI DOM

         QUAN TRỌNG:
         observer chỉ theo dõi thêm/xóa node,
         không tự phá BOM row.
         ========================================================== */

      observer.observe(
          body,
          {
            childList: true
          }
      );

    }


    /* ============================================================
       MUTATION OBSERVER
       ============================================================ */

    const observer =
        new MutationObserver(
            mutations => {

              /*
               * Chỉ refresh khi thực sự có
               * node được thêm/xóa.
               */

              const hasChildMutation =
                  mutations.some(
                      mutation =>
                          mutation.type ===
                          'childList'
                  );

              if (hasChildMutation) {
                refresh();
              }

            }
        );


    /* ============================================================
       SEARCH
       ============================================================ */

    search.addEventListener(
        'input',
        () => {

          page = 1;

          render();

        }
    );


    /* ============================================================
       PREVIOUS
       ============================================================ */

    previous.addEventListener(
        'click',
        () => {

          page--;

          render();

        }
    );


    /* ============================================================
       NEXT
       ============================================================ */

    next.addEventListener(
        'click',
        () => {

          page++;

          render();

        }
    );


    /* ============================================================
       PAGE SELECT
       ============================================================ */

    pageSelect.addEventListener(
        'change',
        () => {

          page =
              Number(
                  pageSelect.value
              );

          render();

        }
    );


    /* ============================================================
       NATIVE VALIDATION
       ============================================================ */

    table.addEventListener(
        'invalid',
        event => {

          const row =
              event.target.closest('tr');

          /*
           * Chỉ xử lý product row.
           */

          if (!isDataRow(row)) {
            return;
          }

          search.value = '';

          page =
              Math.floor(
                  rows.indexOf(row) /
                  PAGE_SIZE
              ) + 1;

          render();

        },
        true
    );


    /* ============================================================
       CHANGE
       ============================================================ */

    table.addEventListener(
        'change',
        () => {

          updateSearchIndex();

          render();

        }
    );


    /* ============================================================
       INITIAL REFRESH
       ============================================================ */

    refresh();

  }


  /* ==============================================================
     INIT
     ============================================================== */

  function init() {

    /*
     * Xóa search handler cũ.
     */

    document
        .querySelectorAll(
            '#directory-search,#product-search,#account-search'
        )
        .forEach(input => {

          const container =
              input.closest(
                  '.search-filters'
              );

          if (
              container &&
              !container.querySelector(
                  'select,button'
              )
          ) {

            container.remove();

          }
          else {

            (
                input.closest(
                    '.search-input'
                ) || input
            ).remove();

          }

        });


    /*
     * Enhance tất cả table.
     */

    document
        .querySelectorAll('table')
        .forEach(enhance);

  }


  /* ==============================================================
     DOM READY
     ============================================================== */

  if (
      document.readyState ===
      'loading'
  ) {

    document.addEventListener(
        'DOMContentLoaded',
        init
    );

  }
  else {

    init();

  }

})();