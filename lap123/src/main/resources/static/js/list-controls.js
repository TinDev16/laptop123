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
            select.replaceWith(
                cell.querySelectorAll('select')[index]
                    .selectedOptions[0]?.textContent || ''
            );
        });

        copy.querySelectorAll('input,textarea').forEach((input, index) => {
            const original =
                cell.querySelectorAll('input,textarea')[index];

            input.replaceWith(
                original.type === 'hidden'
                    ? ''
                    : original.value
            );
        });

        copy.querySelectorAll('button,script').forEach(node => {
            node.remove();
        });

        return copy.textContent
            .replace(/\s+/g, ' ')
            .trim();
    };

    const create = (tag, className, text) => {
        const node = document.createElement(tag);

        node.className = className;

        if (text) {
            node.textContent = text;
        }

        return node;
    };


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
                        .test(
                            normalize(column.label)
                        )
                );

        if (!columns.length) {
            return;
        }


        /* ===================================================== */
        /* THANH TÌM KIẾM */
        /* ===================================================== */

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


        /* ===================================================== */
        /* LẤY TỪ KHÓA TỪ URL */
        /* ===================================================== */

        const urlSearch =
            new URLSearchParams(
                window.location.search
            ).get('search');

        if (urlSearch) {
            search.value = urlSearch;
        }


        searchLabel.append(search);

        toolbar.append(searchLabel);


        /* ===================================================== */
        /* PHÂN TRANG */
        /* ===================================================== */

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


        /* ===================================================== */
        /* CHÈN TOOLBAR + PAGINATION */
        /* ===================================================== */

        const wrapper =
            table.closest(
                '.table-container,.table-responsive'
            ) || table;

        wrapper.before(toolbar);

        wrapper.after(pager);


        /* ===================================================== */
        /* DÒNG EMPTY */
        /* ===================================================== */

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


        let rows = [];
        let page = 1;
        let matched = [];
        let indexed = [];


        /* ===================================================== */
        /* RENDER */
        /* ===================================================== */

        function render() {

            const term =
                normalize(search.value);

            matched =
                indexed
                    .filter(item =>
                        item.search.includes(term)
                    )
                    .map(item =>
                        item.row
                    );

            const pages =
                Math.max(
                    1,
                    Math.ceil(
                        matched.length /
                        PAGE_SIZE
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

            rows.forEach(row => {

                row.hidden =
                    !visible.has(row);

                row.style.display =
                    row.hidden
                        ? 'none'
                        : '';

            });


            empty.hidden =
                matched.length > 0;

            empty.style.display =
                empty.hidden
                    ? 'none'
                    : '';


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


        /* ===================================================== */
        /* SEARCH INDEX */
        /* ===================================================== */

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


        /* ===================================================== */
        /* REFRESH */
        /* ===================================================== */

        function refresh() {

            observer.disconnect();

            Array.from(body.rows)
                .filter(row =>
                    row !== empty &&
                    (
                        row.matches(
                            '.empty-row,.directory-empty'
                        ) ||
                        (
                            row.cells.length === 1 &&
                            row.cells[0].colSpan > 1
                        )
                    )
                )
                .forEach(row =>
                    row.remove()
                );

            rows =
                Array.from(body.rows)
                    .filter(
                        row => row !== empty
                    );

            body.append(empty);

            updateSearchIndex();

            render();

            observer.observe(
                body,
                {
                    childList: true
                }
            );
        }


        const observer =
            new MutationObserver(
                refresh
            );


        /* ===================================================== */
        /* SEARCH EVENT */
        /* ===================================================== */

        search.addEventListener(
            'input',
            () => {

                page = 1;

                render();

            }
        );


        /* ===================================================== */
        /* PREVIOUS */
        /* ===================================================== */

        previous.addEventListener(
            'click',
            () => {

                page--;

                render();

            }
        );


        /* ===================================================== */
        /* NEXT */
        /* ===================================================== */

        next.addEventListener(
            'click',
            () => {

                page++;

                render();

            }
        );


        /* ===================================================== */
        /* PAGE SELECT */
        /* ===================================================== */

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


        /*
         * Hidden rows keep their form values
         * and must be revealed for native validation.
         */

        table.addEventListener(
            'invalid',
            event => {

                const row =
                    event.target.closest('tr');

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


        /* ===================================================== */
        /* TABLE CHANGE */
        /* ===================================================== */

        table.addEventListener(
            'change',
            () => {

                updateSearchIndex();

                render();

            }
        );


        /* ===================================================== */
        /* KHỞI TẠO */
        /* ===================================================== */

        refresh();
    }


    function init() {

        /*
         * Retire legacy search handlers
         * which would otherwise override pagination.
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

                } else {

                    (
                        input.closest(
                            '.search-input'
                        ) || input
                    ).remove();

                }

            });


        document
            .querySelectorAll('table')
            .forEach(enhance);
    }


    if (
        document.readyState ===
        'loading'
    ) {

        document.addEventListener(
            'DOMContentLoaded',
            init
        );

    } else {

        init();

    }

})();