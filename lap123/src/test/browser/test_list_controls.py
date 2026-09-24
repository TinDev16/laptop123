"""Run with: python -m unittest discover -s lap123/src/test/browser -v
Requires Selenium and Chrome. Uses only local HTML; no application/database needed.
"""
import unittest
from pathlib import Path
from urllib.parse import quote
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import Select, WebDriverWait

RESOURCES = Path(__file__).resolve().parents[2] / 'main' / 'resources'


class ListControlsTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        options = webdriver.ChromeOptions()
        options.add_argument('--headless=new')
        options.add_argument('--window-size=1280,900')
        cls.browser = webdriver.Chrome(options=options)

    @classmethod
    def tearDownClass(cls):
        cls.browser.quit()

    def setUp(self):
        rows = ''.join(f'<tr><td>NV{i:02}</td><td>Đặng Nguyễn {i}</td>'
                       f'<td>{"CHO_DUYET" if i % 2 else "DA_DUYET"}</td>'
                       f'<td><input name="quantity" required min="1" type="number" value="{i}"></td></tr>'
                       for i in range(1, 26))
        head = '<thead><tr><th>Mã</th><th>Tên</th><th>Trạng thái</th><th>Số lượng</th></tr></thead>'
        html = ('<meta charset="utf-8"><div class="search-filters"><div class="search-input">'
                '<input id="directory-search"></div></div><form id="entry">'
                '<div class="table-container"><table id="first">' + head + '<tbody>' + rows +
                '<tr class="empty-row" hidden><td colspan="4">Empty</td></tr></tbody></table></div></form>'
                '<table id="second">' + head + '<tbody>' + rows + '</tbody></table>'
                '<table id="empty">' + head + '<tbody></tbody></table>')
        self.browser.get('data:text/html;charset=utf-8,' + quote(html))
        for script in ('status-format.js', 'js/list-controls.js'):
            self.browser.execute_script((RESOURCES / 'static' / script).read_text(encoding='utf-8-sig'))

    def visible(self, table='first'):
        return self.browser.execute_script('return [...document.querySelectorAll("#' + table +
            ' tbody tr:not(.empty-row)")].filter(r=>!r.hidden && r.style.display!=="none").map(r=>r.cells[0].textContent)')

    def control(self, selector):
        return self.browser.find_element(By.CSS_SELECTOR, selector)

    def search(self, text):
        field = self.control('.list-controls input')
        field.clear()
        field.send_keys(text)

    def test_pagination_boundaries_and_independent_tables(self):
        self.assertEqual(len(self.visible()), 10)
        self.control('.list-pagination button:last-child').click()
        self.assertEqual(self.visible()[0], 'NV11')
        self.assertEqual(self.visible('second')[0], 'NV01')
        self.control('.list-pagination button:last-child').click()
        self.assertEqual(len(self.visible()), 5)
        self.assertFalse(self.control('.list-pagination button:last-child').is_enabled())
        self.control('.list-pagination button').click()
        self.assertEqual(self.visible()[0], 'NV11')

    def test_accent_insensitive_search_covers_other_pages(self):
        self.search('dang nguyen 25')
        self.assertEqual(self.visible(), ['NV25'])
        self.assertIn('1–1 / 1', self.control('.list-summary').text)

    def test_search_only_and_reset_to_first_page(self):
        self.assertFalse(self.browser.find_elements(By.CSS_SELECTOR, '.list-controls select,.list-controls button'))
        self.control('.list-pagination button:last-child').click()
        self.search('nguyen 1')
        self.assertEqual(self.visible(), ['NV01'] + [f'NV{i}' for i in range(10, 19)])
        self.control('.list-pagination button:last-child').click()
        self.assertEqual(self.visible(), ['NV19'])
        self.control('.list-controls input').clear()
        self.control('.list-controls input').send_keys(' ')
        self.assertEqual(len(self.visible()), 10)
        self.assertEqual(self.visible()[0], 'NV01')

    def test_empty_results_and_empty_table(self):
        self.search('no-such-record')
        self.assertEqual(self.visible(), [])
        self.assertTrue(self.control('#first .list-empty').is_displayed())
        self.assertTrue(self.control('#empty .list-empty').is_displayed())
        self.assertFalse(self.control('.list-pagination button:last-child').is_enabled())

    def test_dynamic_rows_and_form_values(self):
        self.assertEqual(self.browser.execute_script('return new FormData(document.querySelector("#entry")).getAll("quantity").length'), 25)
        self.browser.execute_script('document.querySelector("#first tbody").insertAdjacentHTML("beforeend", "<tr><td>NEW</td><td>Mới</td><td>Đã duyệt</td><td>1</td></tr>")')
        WebDriverWait(self.browser, 5).until(lambda d: 'tổng 26' in self.control('.list-summary').text)
        self.search('moi')
        self.assertEqual(self.visible(), ['NEW'])

    def test_invalid_input_reveals_its_page(self):
        self.browser.execute_script('const input=document.querySelectorAll("#first input")[22]; input.value=""; input.checkValidity()')
        self.assertEqual(self.visible()[0], 'NV21')

    def test_legacy_search_removed_and_reinitialization_is_safe(self):
        self.assertFalse(self.browser.find_elements(By.ID, 'directory-search'))
        self.browser.execute_script((RESOURCES / 'static/js/list-controls.js').read_text(encoding='utf-8-sig'))
        self.assertEqual(len(self.browser.find_elements(By.CSS_SELECTOR, '.list-controls')), 3)

    def test_role_search_uses_selected_option_only(self):
        self.browser.execute_script("""
            const cell = document.querySelector('#first tbody tr td:nth-child(2)');
            cell.innerHTML = '<select><option>Quản lý kho</option><option selected>Nhân viên</option></select>';
            cell.querySelector('select').dispatchEvent(new Event('change', {bubbles:true}));
        """)
        self.search('quan ly kho')
        self.assertEqual(self.visible(), [])
        self.search('nhan vien')
        self.assertEqual(self.visible(), ['NV01'])

    def test_page_selector_and_clamping_after_deletion(self):
        Select(self.control('.list-page-select select')).select_by_value('3')
        self.assertEqual(self.visible()[0], 'NV21')
        self.browser.execute_script('document.querySelectorAll("#first tbody tr:not(.empty-row)").forEach((row,i)=>{if(i>=10)row.remove()})')
        WebDriverWait(self.browser, 5).until(lambda d: len(self.visible()) == 10)
        self.assertEqual(self.visible()[0], 'NV01')
        self.assertFalse(self.control('.list-pagination button:last-child').is_enabled())

    def test_mobile_controls_fit_viewport(self):
        self.browser.execute_script('const style=document.createElement("style"); style.textContent=arguments[0]; document.head.append(style)',
                                    (RESOURCES / 'static/css/list-controls.css').read_text(encoding='utf-8-sig'))
        self.browser.set_window_size(390, 844)
        self.assertTrue(self.browser.execute_script('return [...document.querySelectorAll(".list-controls,.list-pagination")].every(e=>e.getBoundingClientRect().right<=innerWidth)'))
        self.browser.set_window_size(1280, 900)


if __name__ == '__main__':
    unittest.main()
