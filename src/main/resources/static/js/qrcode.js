// // Khai báo biến toàn cục nếu cần
// let html5QrCode;
//
// // Hàm khởi động quét QR
// function startQrScanner() {
//     const qrReader = document.getElementById("qr-reader");
//     qrReader.style.display = "block";
//
//     html5QrCode = new Html5Qrcode("qr-reader");
//     Html5Qrcode.getCameras().then(devices => {
//         if (devices && devices.length) {
//             const cameraId = devices[0].id;
//             html5QrCode.start(
//                 cameraId,
//                 { fps: 10, qrbox: 250 },
//                 qrCodeMessage => {
//                     // ✅ Xử lý mã QR được quét
//                     findProductByQRCode(qrCodeMessage);
//
//                     // ✅ Dừng camera
//                     html5QrCode.stop().then(() => {
//                         document.getElementById("qr-reader").style.display = "none";
//                     }).catch(err => {
//                         console.error("Lỗi khi dừng camera:", err);
//                     });
//                 },
//                 errorMessage => {
//                     // console.log(`Lỗi đọc QR: ${errorMessage}`);
//                 }
//             );
//         }
//     }).catch(err => {
//         alert("Không tìm thấy camera: " + err);
//     });
// }
//
// // Tìm sản phẩm theo mã QR và thêm vào giỏ
// function findProductByQRCode(qrCodeMessage) {
//     const allProducts = document.querySelectorAll(".product-card");
//     allProducts.forEach(product => {
//         if (product.getAttribute("data-id") === qrCodeMessage) {
//             // Scroll tới sản phẩm
//             product.scrollIntoView({ behavior: "smooth", block: "center" });
//
//             // Highlight viền xanh
//             product.classList.add("border", "border-success", "border-3");
//             setTimeout(() => {
//                 product.classList.remove("border", "border-success", "border-3");
//             }, 2000);
//
//             // ✅ Thêm vào giỏ
//             addToCart(parseInt(qrCodeMessage));
//
//             // ✅ Âm thanh quét (nếu muốn)
//             // playBeepSound();
//
//             // ✅ Toast thông báo
//             showToast("Đã thêm sản phẩm vào giỏ hàng từ QR!");
//         }
//     });
// }
//
// // Tùy chọn: hàm phát âm thanh (bật lại nếu bạn muốn)
// function playBeepSound() {
//     const beep = document.getElementById("qrBeep");
//     if (beep) beep.play();
// }
//
// // Hàm toast
// function showToast(message) {
//     const toastBody = document.getElementById("qrToastBody");
//     const toastEl = new bootstrap.Toast(document.getElementById("qrToast"));
//     toastBody.textContent = message;
//     toastEl.show();
// }
