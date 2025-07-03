document.addEventListener("DOMContentLoaded", function () {
    const editModal = new bootstrap.Modal(document.getElementById("editProductModal"));
    const editForm = document.getElementById("editProductForm");

    // Lắng nghe sự kiện khi bấm nút "Sửa"
    document.querySelectorAll(".btn-edit").forEach(button => {
        button.addEventListener("click", function () {
            const productId = this.getAttribute("data-id");

            // Gửi yêu cầu để lấy thông tin sản phẩm
            fetch(`/acvstore/sanpham/get/${productId}`)
                .then(response => response.json())
                .then(product => {
                    // Điền dữ liệu vào modal
                    document.getElementById("editProductId").value = product.id;
                    document.getElementById("editProductName").value = product.name;
                    document.getElementById("editProductPrice").value = product.price;
                    document.getElementById("editProductStock").value = product.stockQuantity;
                    document.getElementById("editProductDescription").value = product.description;
                    document.getElementById("editProductImageUrl").value = product.imageUrl;

                    // Đổ danh mục vào select box
                    const categorySelect = document.getElementById("editProductCategory");
                    categorySelect.innerHTML = ""; // Xóa danh mục cũ
                    fetch("/acvstore/categories") // Lấy danh mục
                        .then(response => response.json())
                        .then(categories => {
                            categories.forEach(category => {
                                const option = document.createElement("option");
                                option.value = category.id;
                                option.textContent = category.name;
                                if (product.category && product.category.id === category.id) {
                                    option.selected = true;
                                }
                                categorySelect.appendChild(option);
                            });
                        });

                    // Hiển thị modal chỉnh sửa
                    editModal.show();
                });
        });
    });

    // Xử lý cập nhật sản phẩm khi submit
    editForm.addEventListener("submit", function (event) {
        event.preventDefault();
        const productId = document.getElementById("editProductId").value;

        const updatedProduct = {
            id: productId,
            name: document.getElementById("editProductName").value,
            price: document.getElementById("editProductPrice").value,
            stockQuantity: document.getElementById("editProductStock").value,
            description: document.getElementById("editProductDescription").value,
            imageUrl: document.getElementById("editProductImageUrl").value,
            category: { id: document.getElementById("editProductCategory").value }
        };

        fetch(`/acvstore/sanpham/edit/${productId}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(updatedProduct)
        })
            .then(response => {
                if (response.ok) {
                    location.reload(); // Reload lại trang sau khi cập nhật thành công
                } else {
                    alert("Cập nhật thất bại!");
                }
            })
            .catch(error => console.error("Lỗi:", error));
    });
});
