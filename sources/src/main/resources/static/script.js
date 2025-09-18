
    function showSection(sectionId) {
    // Hide all sections
    const sections = document.querySelectorAll('.content-section');
    sections.forEach(section => section.classList.remove('active'));

    // Show selected section
    document.getElementById(sectionId).classList.add('active');

    // Update navigation
    const navLinks = document.querySelectorAll('.nav-link');
    navLinks.forEach(link => link.classList.remove('active'));
    event.target.closest('.nav-link').classList.add('active');
}

    function addProduct() {
    alert('Tính năng thêm sản phẩm sẽ được triển khai trong phiên bản tiếp theo!');
}

    function addCustomer() {
    alert('Tính năng thêm khách hàng sẽ được triển khai trong phiên bản tiếp theo!');
}

    function exportOrders() {
    alert('Đang xuất báo cáo đơn hàng...');
}

    // Auto-refresh stats every 30 seconds
    setInterval(() => {
    // Simulate real-time updates
    const stats = document.querySelectorAll('.stat-value');
    stats.forEach((stat, index) => {
    if (Math.random() > 0.7) {
    stat.style.transform = 'scale(1.05)';
    setTimeout(() => {
    stat.style.transform = 'scale(1)';
}, 200);
}
});
}, 30000);

    // Add smooth transitions to all interactive elements
    document.addEventListener('DOMContentLoaded', function() {
    const interactiveElements = document.querySelectorAll('button, .nav-link, .stat-card');
    interactiveElements.forEach(element => {
    element.style.transition = 'all 0.3s ease';
});
});