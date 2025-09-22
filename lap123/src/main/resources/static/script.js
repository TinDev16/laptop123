

    let currentModal = null;
    let editingItem = null;

    // Initialize
    document.addEventListener('DOMContentLoaded', function() {
    // Set current date
    document.getElementById('current-date').textContent = new Date().toLocaleDateString('vi-VN');

    // Setup navigation
    setupNavigation();

    // Render initial data
    renderProducts();
    renderImportSlips();
    renderExportSlips();
    renderCustomers();
    renderSuppliers();
    renderEmployees();
    renderInventory();

    // Setup brand filter
    setupBrandFilter();

    // Khởi tạo trạng thái nút
    document.getElementById('login-btn').classList.remove('hidden');
    document.getElementById('logout-btn').classList.add('hidden');
});

    // Navigation
    function setupNavigation() {
    const navTabs = document.querySelectorAll('.nav-tab');
    const tabContents = document.querySelectorAll('.tab-content');

    navTabs.forEach(tab => {
    tab.addEventListener('click', () => {
    const tabId = tab.getAttribute('data-tab');

    // Update active tab
    navTabs.forEach(t => t.classList.remove('active'));
    tab.classList.add('active');

    // Update active content
    tabContents.forEach(content => {
    content.classList.remove('active');
    if (content.id === tabId) {
    content.classList.add('active');
    if (tabId === 'inventory') renderInventory();
}
});
});
});
}

    // Utility functions
    function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
}).format(amount);
}

    function formatDate(dateString) {
    return new Date(dateString).toLocaleDateString('vi-VN');
}

    function generateId(prefix, array) {
    const maxId = array.reduce((max, item) => {
    const num = parseInt(item.id.replace(prefix, ''));
    return num > max ? num : max;
}, 0);
    return `${prefix}${String(maxId + 1).padStart(3, '0')}`;
}

    // Admin login/logout
    let isAdmin = false;

    function adminLogin() {
    document.getElementById('login-modal').classList.add('show');
}

    function closeLoginModal() {
    document.getElementById('login-modal').classList.remove('show');
    document.getElementById('admin-login-form').reset();
}



    document.getElementById('login-modal').addEventListener('click', function(e) {
    if (e.target === this) closeLoginModal();
});



    function setupBrandFilter() {
    const brandFilter = document.getElementById('brand-filter');
    const brands = [...new Set(data.products.map(p => p.brand))].filter(Boolean);

    brandFilter.innerHTML = '<option value="">Tất cả thương hiệu</option>';
    brands.forEach(brand => {
    const option = document.createElement('option');
    option.value = brand;
    option.textContent = brand;
    brandFilter.appendChild(option);
});
}

    // Import Slips functions
    function renderImportSlips() {
        const tbody = document.getElementById('import-slips-table');
        tbody.innerHTML = '';

        data.importSlips.forEach((slip, index) => {
            const row = document.createElement('tr');
            row.innerHTML = `
                    <td>${index + 1}</td>
                    <td class="font-semibold">${slip.id}</td>
                    <td>${slip.supplier}</td>
                    <td>${slip.employee}</td>
                    <td>${formatDate(slip.date)}</td>
                    <td class="price buy">${formatCurrency(slip.total)}</td>
                    <td>
                        <div class="table-actions">
                            <button class="btn btn-icon btn-outline" onclick="viewImportSlip('${slip.id}')" title="Xem chi tiết">
                                <i class="fas fa-eye"></i>
                            </button>
                            <button class="btn btn-icon btn-outline" onclick="editImportSlip('${slip.id}')" title="Sửa">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-icon btn-outline text-red-600" onclick="deleteImportSlip('${slip.id}')" title="Hủy phiếu">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </td>
                `;
            tbody.appendChild(row);
        });
    }

    function viewImportSlip(id) {
    const slip = data.importSlips.find(s => s.id === id);
    if (slip) {
    openModal('import-view', slip);
}
}

    function editImportSlip(id) {
    const slip = data.importSlips.find(s => s.id === id);
    if (slip) {
    openModal('import-edit', slip);
}
}

    function deleteImportSlip(id) {
    const slip = data.importSlips.find(s => s.id === id);
    if (slip) {
    openConfirmModal(`Bạn có chắc chắn muốn hủy phiếu nhập "${slip.id}" không?`, () => {
    data.importSlips = data.importSlips.filter(s => s.id !== id);
    renderImportSlips();
});
}
}

    // Export Slips functions
    function renderExportSlips() {
    const tbody = document.getElementById('export-slips-table');
    tbody.innerHTML = '';

    data.exportSlips.forEach((slip, index) => {
    const row = document.createElement('tr');
    row.innerHTML = `
                    <td>${index + 1}</td>
                    <td class="font-semibold">${slip.id}</td>
                    <td>${slip.customer}</td>
                    <td>${slip.employee}</td>
                    <td>${formatDate(slip.date)}</td>
                    <td class="price sell">${formatCurrency(slip.total)}</td>
                    <td>
                        <div class="table-actions">
                            <button class="btn btn-icon btn-outline" onclick="viewExportSlip('${slip.id}')" title="Xem chi tiết">
                                <i class="fas fa-eye"></i>
                            </button>
                            <button class="btn btn-icon btn-outline" onclick="editExportSlip('${slip.id}')" title="Sửa">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-icon btn-outline text-red-600" onclick="deleteExportSlip('${slip.id}')" title="Hủy phiếu">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </td>
                `;
    tbody.appendChild(row);
});
}

    function viewExportSlip(id) {
    const slip = data.exportSlips.find(s => s.id === id);
    if (slip) {
    openModal('export-view', slip);
}
}

    function editExportSlip(id) {
    const slip = data.exportSlips.find(s => s.id === id);
    if (slip) {
    openModal('export-edit', slip);
}
}

    function deleteExportSlip(id) {
    const slip = data.exportSlips.find(s => s.id === id);
    if (slip) {
    openConfirmModal(`Bạn có chắc chắn muốn hủy phiếu xuất "${slip.id}" không?`, () => {
    data.exportSlips = data.exportSlips.filter(s => s.id !== id);
    renderExportSlips();
});
}
}

    // Customers functions
    function renderCustomers() {
    const tbody = document.getElementById('customers-table');
    tbody.innerHTML = '';

    data.customers.forEach(customer => {
    const row = document.createElement('tr');
    row.innerHTML = `
                    <td class="font-semibold">${customer.id}</td>
                    <td>${customer.name}</td>
                    <td>${customer.phone}</td>
                    <td>${customer.address}</td>
                    <td>${formatDate(customer.joinDate)}</td>
                    <td>
                        <div class="table-actions">
                            <button class="btn btn-icon btn-outline" onclick="viewCustomer('${customer.id}')" title="Xem chi tiết">
                                <i class="fas fa-eye"></i>
                            </button>
                            <button class="btn btn-icon btn-outline" onclick="editCustomer('${customer.id}')" title="Sửa">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-icon btn-outline text-red-600" onclick="deleteCustomer('${customer.id}')" title="Xóa">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </td>
                `;
    tbody.appendChild(row);
});
}

    function viewCustomer(id) {
    const customer = data.customers.find(c => c.id === id);
    if (customer) {
    openModal('customer-view', customer);
}
}

    function editCustomer(id) {
    const customer = data.customers.find(c => c.id === id);
    if (customer) {
    openModal('customer-edit', customer);
}
}

    function deleteCustomer(id) {
    const customer = data.customers.find(c => c.id === id);
    if (customer) {
    openConfirmModal(`Bạn có chắc chắn muốn xóa khách hàng "${customer.name}" không?`, () => {
    data.customers = data.customers.filter(c => c.id !== id);
    renderCustomers();
});
}
}

    // Suppliers functions
    function renderSuppliers() {
    const tbody = document.getElementById('suppliers-table');
    tbody.innerHTML = '';

    data.suppliers.forEach(supplier => {
    const row = document.createElement('tr');
    row.innerHTML = `
                    <td class="font-semibold">${supplier.id}</td>
                    <td>${supplier.name}</td>
                    <td>${supplier.phone}</td>
                    <td>${supplier.address}</td>
                    <td>
                        <div class="table-actions">
                            <button class="btn btn-icon btn-outline" onclick="viewSupplier('${supplier.id}')" title="Xem chi tiết">
                                <i class="fas fa-eye"></i>
                            </button>
                            <button class="btn btn-icon btn-outline" onclick="editSupplier('${supplier.id}')" title="Sửa">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-icon btn-outline text-red-600" onclick="deleteSupplier('${supplier.id}')" title="Xóa">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </td>
                `;
    tbody.appendChild(row);
});
}

    function viewSupplier(id) {
    const supplier = data.suppliers.find(s => s.id === id);
    if (supplier) {
    openModal('supplier-view', supplier);
}
}

    function editSupplier(id) {
    const supplier = data.suppliers.find(s => s.id === id);
    if (supplier) {
    openModal('supplier-edit', supplier);
}
}

    function deleteSupplier(id) {
    const supplier = data.suppliers.find(s => s.id === id);
    if (supplier) {
    openConfirmModal(`Bạn có chắc chắn muốn xóa nhà cung cấp "${supplier.name}" không?`, () => {
    data.suppliers = data.suppliers.filter(s => s.id !== id);
    renderSuppliers();
});
}
}

    // Employees functions
    function renderEmployees() {
    const tbody = document.getElementById('employees-table');
    tbody.innerHTML = '';

    data.employees.forEach(employee => {
    const row = document.createElement('tr');
    row.innerHTML = `
                    <td class="font-semibold">${employee.id}</td>
                    <td>${employee.name}</td>
                    <td>${employee.gender}</td>
                    <td>${employee.birthDate ? formatDate(employee.birthDate) : ''}</td>
                    <td>${employee.phone}</td>
                    <td>${formatDate(employee.joinDate)}</td>
                    <td>
                        <div class="table-actions">
                            <button class="btn btn-icon btn-outline" onclick="viewEmployee('${employee.id}')" title="Xem chi tiết">
                                <i class="fas fa-eye"></i>
                            </button>
                            <button class="btn btn-icon btn-outline" onclick="editEmployee('${employee.id}')" title="Sửa">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-icon btn-outline text-red-600" onclick="deleteEmployee('${employee.id}')" title="Xóa">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </td>
                `;
    tbody.appendChild(row);
});
}

    function viewEmployee(id) {
    const employee = data.employees.find(e => e.id === id);
    if (employee) {
    openModal('employee-view', employee);
}
}

    function editEmployee(id) {
    const employee = data.employees.find(e => e.id === id);
    if (employee) {
    openModal('employee-edit', employee);
}
}

    function deleteEmployee(id) {
    const employee = data.employees.find(e => e.id === id);
    if (employee) {
    openConfirmModal(`Bạn có chắc chắn muốn xóa nhân viên "${employee.name}" không?`, () => {
    data.employees = data.employees.filter(e => e.id !== id);
    renderEmployees();
});
}
}

    // Modal functions
    function openModal(type, item = null) {
    currentModal = type;
    editingItem = item;

    const modal = document.getElementById('modal');
    const modalTitle = document.getElementById('modal-title');
    const modalBody = document.getElementById('modal-body');
    const modalFooter = document.getElementById('modal-footer');

    // Set modal size
    if (type.includes('product')) {
    modal.classList.add('large');
} else {
    modal.classList.remove('large');
}

    // Set title and content based on type
    if (type === 'product-add') {
    modalTitle.textContent = 'Thêm sản phẩm';
    modalBody.innerHTML = getProductForm();
    modalFooter.innerHTML = getSaveFooter();
} else if (type === 'product-edit') {
    modalTitle.textContent = 'Sửa sản phẩm';
    modalBody.innerHTML = getProductForm(item);
    modalFooter.innerHTML = getSaveFooter();
} else if (type === 'product-view') {
    modalTitle.textContent = 'Chi tiết sản phẩm';
    modalBody.innerHTML = getProductDetail(item);
    modalFooter.innerHTML = '';
} else if (type === 'customer-add') {
    modalTitle.textContent = 'Thêm khách hàng';
    modalBody.innerHTML = getCustomerForm();
    modalFooter.innerHTML = getSaveFooter();
} else if (type === 'customer-edit') {
    modalTitle.textContent = 'Sửa khách hàng';
    modalBody.innerHTML = getCustomerForm(item);
    modalFooter.innerHTML = getSaveFooter();
} else if (type === 'customer-view') {
    modalTitle.textContent = 'Chi tiết khách hàng';
    modalBody.innerHTML = getCustomerDetail(item);
    modalFooter.innerHTML = '';
} else if (type === 'supplier-add') {
    modalTitle.textContent = 'Thêm nhà cung cấp';
    modalBody.innerHTML = getSupplierForm();
    modalFooter.innerHTML = getSaveFooter();
} else if (type === 'supplier-edit') {
    modalTitle.textContent = 'Sửa nhà cung cấp';
    modalBody.innerHTML = getSupplierForm(item);
    modalFooter.innerHTML = getSaveFooter();
} else if (type === 'supplier-view') {
    modalTitle.textContent = 'Chi tiết nhà cung cấp';
    modalBody.innerHTML = getSupplierDetail(item);
    modalFooter.innerHTML = '';
} else if (type === 'employee-add') {
    modalTitle.textContent = 'Thêm nhân viên';
    modalBody.innerHTML = getEmployeeForm();
    modalFooter.innerHTML = getSaveFooter();
} else if (type === 'employee-edit') {
    modalTitle.textContent = 'Sửa nhân viên';
    modalBody.innerHTML = getEmployeeForm(item);
    modalFooter.innerHTML = getSaveFooter();
} else if (type === 'employee-view') {
    modalTitle.textContent = 'Chi tiết nhân viên';
    modalBody.innerHTML = getEmployeeDetail(item);
    modalFooter.innerHTML = '';
}

    document.getElementById('modal-overlay').classList.add('show');
}

    function closeModal() {
    document.getElementById('modal-overlay').classList.remove('show');
    currentModal = null;
    editingItem = null;
}

    function saveModal() {
    if (currentModal === 'product-add' || currentModal === 'product-edit') {
    saveProduct();
} else if (currentModal === 'customer-add' || currentModal === 'customer-edit') {
    saveCustomer();
} else if (currentModal === 'supplier-add' || currentModal === 'supplier-edit') {
    saveSupplier();
} else if (currentModal === 'employee-add' || currentModal === 'employee-edit') {
    saveEmployee();
}
}

    function getSaveFooter() {
    return `
                <button class="btn btn-outline" onclick="closeModal()">Hủy</button>
                <button class="btn btn-primary" onclick="saveModal()">Lưu</button>
            `;
}

    function getProductDetail(product) {
        return `
                ${product.image ? `<img src="${product.image}" alt="${product.name}" class="product-detail-image">` : ''}
                <div class="detail-grid">
                    <div class="detail-row">
                        <span class="detail-label">Mã sản phẩm:</span>
                        <span class="detail-value">${product.id}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Tên sản phẩm:</span>
                        <span class="detail-value">${product.name}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Xuất xứ:</span>
                        <span class="detail-value">${product.origin || 'Chưa cập nhật'}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Chip xử lý:</span>
                        <span class="detail-value">${product.processor || 'Chưa cập nhật'}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Hệ điều hành:</span>
                        <span class="detail-value">${product.os || 'Chưa cập nhật'}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Kích thước màn hình:</span>
                        <span class="detail-value">${product.screenSize || 'Chưa cập nhật'}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Thương hiệu:</span>
                        <span class="detail-value">${product.brand || 'Chưa cập nhật'}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Nhà cung cấp:</span>
                        <span class="detail-value">${product.supplier || 'Chưa cập nhật'}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Giá nhập:</span>
                        <span class="detail-value text-red-600">${formatCurrency(product.buyPrice)}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Giá bán:</span>
                        <span class="detail-value text-green-600">${formatCurrency(product.sellPrice)}</span>
                    </div>
                </div>
            `;
    }

    // Customer forms and details
    function getCustomerForm(customer = null) {
    return `
                <form id="customer-form">
                    <div class="form-group">
                        <label class="form-label required">Tên khách hàng</label>
                        <input type="text" id="customer-name" class="form-input" value="${customer ? customer.name : ''}" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label required">Số điện thoại</label>
                        <input type="tel" id="customer-phone" class="form-input" value="${customer ? customer.phone : ''}" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Địa chỉ</label>
                        <textarea id="customer-address" class="form-textarea" rows="3">${customer ? customer.address : ''}</textarea>
                    </div>
                </form>
            `;
}

    function getCustomerDetail(customer) {
    return `
                <div class="detail-grid">
                    <div class="detail-row">
                        <span class="detail-label">Mã khách hàng:</span>
                        <span class="detail-value">${customer.id}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Tên khách hàng:</span>
                        <span class="detail-value">${customer.name}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Số điện thoại:</span>
                        <span class="detail-value">${customer.phone}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Địa chỉ:</span>
                        <span class="detail-value">${customer.address || 'Chưa cập nhật'}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Ngày tham gia:</span>
                        <span class="detail-value">${formatDate(customer.joinDate)}</span>
                    </div>
                </div>
            `;
}

    function saveCustomer() {
    const form = {
    name: document.getElementById('customer-name').value,
    phone: document.getElementById('customer-phone').value,
    address: document.getElementById('customer-address').value,
    joinDate: editingItem ? editingItem.joinDate : new Date().toISOString().split('T')[0]
};

    if (!form.name || !form.phone) {
    alert('Vui lòng nhập đầy đủ thông tin bắt buộc');
    return;
}

    if (currentModal === 'customer-add') {
    form.id = generateId('KH', data.customers);
    data.customers.push(form);
} else if (currentModal === 'customer-edit') {
    const index = data.customers.findIndex(c => c.id === editingItem.id);
    if (index !== -1) {
    data.customers[index] = { ...form, id: editingItem.id };
}
}

    renderCustomers();
    closeModal();
}

    // Supplier forms and details
    function getSupplierForm(supplier = null) {
    return `
                <form id="supplier-form">
                    <div class="form-group">
                        <label class="form-label required">Tên nhà cung cấp</label>
                        <input type="text" id="supplier-name" class="form-input" value="${supplier ? supplier.name : ''}" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label required">Số điện thoại</label>
                        <input type="tel" id="supplier-phone" class="form-input" value="${supplier ? supplier.phone : ''}" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Địa chỉ</label>
                        <textarea id="supplier-address" class="form-textarea" rows="3">${supplier ? supplier.address : ''}</textarea>
                    </div>
                </form>
            `;
}

    function getSupplierDetail(supplier) {
    return `
                <div class="detail-grid">
                    <div class="detail-row">
                        <span class="detail-label">Mã nhà cung cấp:</span>
                        <span class="detail-value">${supplier.id}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Tên nhà cung cấp:</span>
                        <span class="detail-value">${supplier.name}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Số điện thoại:</span>
                        <span class="detail-value">${supplier.phone}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Địa chỉ:</span>
                        <span class="detail-value">${supplier.address || 'Chưa cập nhật'}</span>
                    </div>
                </div>
            `;
}

    function saveSupplier() {
    const form = {
    name: document.getElementById('supplier-name').value,
    phone: document.getElementById('supplier-phone').value,
    address: document.getElementById('supplier-address').value
};

    if (!form.name || !form.phone) {
    alert('Vui lòng nhập đầy đủ thông tin bắt buộc');
    return;
}

    if (currentModal === 'supplier-add') {
    form.id = generateId('NCC', data.suppliers);
    data.suppliers.push(form);
} else if (currentModal === 'supplier-edit') {
    const index = data.suppliers.findIndex(s => s.id === editingItem.id);
    if (index !== -1) {
    data.suppliers[index] = { ...form, id: editingItem.id };
}
}

    renderSuppliers();
    closeModal();
}

    // Employee forms and details
    function getEmployeeForm(employee = null) {
    return `
                <form id="employee-form">
                    <div class="form-group">
                        <label class="form-label required">Họ tên</label>
                        <input type="text" id="employee-name" class="form-input" value="${employee ? employee.name : ''}" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Giới tính</label>
                        <select id="employee-gender" class="form-select">
                            <option value="">Chọn giới tính</option>
                            <option value="Nam" ${employee && employee.gender === 'Nam' ? 'selected' : ''}>Nam</option>
                            <option value="Nữ" ${employee && employee.gender === 'Nữ' ? 'selected' : ''}>Nữ</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Ngày sinh</label>
                        <input type="date" id="employee-birth-date" class="form-input" value="${employee ? employee.birthDate : ''}">
                    </div>
                    <div class="form-group">
                        <label class="form-label required">Số điện thoại</label>
                        <input type="tel" id="employee-phone" class="form-input" value="${employee ? employee.phone : ''}" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Ngày tham gia</label>
                        <input type="date" id="employee-join-date" class="form-input" value="${employee ? employee.joinDate : new Date().toISOString().split('T')[0]}">
                    </div>
                </form>
            `;
}

    function getEmployeeDetail(employee) {
    return `
                <div class="detail-grid">
                    <div class="detail-row">
                        <span class="detail-label">Mã nhân viên:</span>
                        <span class="detail-value">${employee.id}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Họ tên:</span>
                        <span class="detail-value">${employee.name}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Giới tính:</span>
                        <span class="detail-value">${employee.gender || 'Chưa cập nhật'}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Ngày sinh:</span>
                        <span class="detail-value">${employee.birthDate ? formatDate(employee.birthDate) : 'Chưa cập nhật'}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Số điện thoại:</span>
                        <span class="detail-value">${employee.phone}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Ngày tham gia:</span>
                        <span class="detail-value">${formatDate(employee.joinDate)}</span>
                    </div>
                </div>
            `;
}

    function saveEmployee() {
    const form = {
    name: document.getElementById('employee-name').value,
    gender: document.getElementById('employee-gender').value,
    birthDate: document.getElementById('employee-birth-date').value,
    phone: document.getElementById('employee-phone').value,
    joinDate: document.getElementById('employee-join-date').value
};

    if (!form.name || !form.phone) {
    alert('Vui lòng nhập đầy đủ thông tin bắt buộc');
    return;
}

    if (currentModal === 'employee-add') {
    form.id = generateId('NV', data.employees);
    data.employees.push(form);
} else if (currentModal === 'employee-edit') {
    const index = data.employees.findIndex(e => e.id === editingItem.id);
    if (index !== -1) {
    data.employees[index] = { ...form, id: editingItem.id };
}
}

    renderEmployees();
    closeModal();
}

    // Confirm delete modal
    function openConfirmModal(message, onConfirm) {
    document.getElementById('confirm-message').textContent = message;
    document.getElementById('confirm-delete-btn').onclick = () => {
    onConfirm();
    closeConfirmModal();
};
    document.getElementById('confirm-modal').classList.add('show');
}

    function closeConfirmModal() {
    document.getElementById('confirm-modal').classList.remove('show');
}

    // Import/Export functions
    function importExcel(type) {
    alert(`Tính năng nhập Excel cho ${type} đang được phát triển`);
}

    function exportExcel(type) {
    alert(`Tính năng xuất Excel cho ${type} đang được phát triển`);
}

    function exportPDF(type) {
    alert(`Tính năng xuất PDF cho ${type} đang được phát triển`);
}

    // Close modal when clicking outside
    document.getElementById('modal-overlay').addEventListener('click', function(e) {
    if (e.target === this) {
    closeModal();
}
});

    document.getElementById('confirm-modal').addEventListener('click', function(e) {
    if (e.target === this) {
    closeConfirmModal();
}
});

    // Handle keyboard shortcuts
    document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
    if (document.getElementById('modal-overlay').classList.contains('show')) {
    closeModal();
}
    if (document.getElementById('confirm-modal').classList.contains('show')) {
    closeConfirmModal();
}
}
});

    function renderInventory() {
    const tbody = document.getElementById('inventory-table');
    tbody.innerHTML = '';
    data.products.forEach(product => {
    // Tính tồn kho: tổng nhập - tổng xuất
    const imported = data.importSlips.reduce((sum, slip) => {
    return sum + slip.items.filter(i => i.productId === product.id)
    .reduce((s, i) => s + (i.quantity || 0), 0);
}, 0);
    const exported = data.exportSlips.reduce((sum, slip) => {
    return sum + slip.items.filter(i => i.productId === product.id)
    .reduce((s, i) => s + (i.quantity || 0), 0);
}, 0);
    const stock = imported - exported;
    const row = document.createElement('tr');
    row.innerHTML = `
                    <td class="font-semibold">${product.id}</td>
                    <td>${product.name}</td>
                    <td>${product.brand}</td>
                    <td>${stock}</td>
                    <td class="price buy">${formatCurrency(product.buyPrice)}</td>
                    <td class="price sell">${formatCurrency(product.sellPrice)}</td>
                `;
    tbody.appendChild(row);
});
}
