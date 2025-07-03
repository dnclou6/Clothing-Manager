document.addEventListener("DOMContentLoaded", function () {
    const currentPath = window.location.pathname;

    // B1: Xử lý active cho link
    document.querySelectorAll(".nav-link:not([data-bs-toggle='collapse'])").forEach(link => {
        const linkPath = new URL(link.href, window.location.origin).pathname;
        if (currentPath === linkPath) {
            link.classList.add("active");

            // Tìm submenu cha (nếu có) và mở
            const submenu = link.closest(".collapse");
            if (submenu) {
                const parentToggle = document.querySelector(`[href="#${submenu.id}"]`);
                if (parentToggle) {
                    new bootstrap.Collapse(submenu, { toggle: false }).show();
                    parentToggle.classList.add("active-parent");

                    const icon = parentToggle.querySelector('.toggle-icon');
                    if (icon) {
                        icon.classList.replace('fa-chevron-down', 'fa-chevron-up');
                    }
                }
            }
        }
    });

    // B2: Xử lý toggle icon khi click vào dropdown
    document.querySelectorAll('[data-bs-toggle="collapse"]').forEach(toggle => {
        const icon = toggle.querySelector('.toggle-icon');
        const targetId = toggle.getAttribute('href');
        const targetEl = document.querySelector(targetId);

        if (!targetEl) return;

        // Đảm bảo không tự toggle khi init
        const collapse = new bootstrap.Collapse(targetEl, { toggle: false });

        toggle.addEventListener("click", () => {
            // Mũi tên sẽ xoay thông qua sự kiện bên dưới
        });

        targetEl.addEventListener('show.bs.collapse', () => {
            icon?.classList.replace('fa-chevron-down', 'fa-chevron-up');
            toggle.classList.add('active-parent');
        });

        targetEl.addEventListener('hide.bs.collapse', () => {
            icon?.classList.replace('fa-chevron-up', 'fa-chevron-down');
            toggle.classList.remove('active-parent');
        });
    });
});
