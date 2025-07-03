package com.example.AsmGD1.controller;

import com.example.AsmGD1.repository.CategoryRepository;
import com.example.AsmGD1.service.CustomerHomepageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {



    @GetMapping("/acvstore/dashboard")
    public String homePageAdmin() {
        return "WebQuanLy/dashboard";
    }

//    // Điều hướng đến các trang quản lý khác
//    @GetMapping("/acvstore/hoadon")
//    public String hoaDonPage() {
//        return "WebQuanLy/hoadon";
//    }

//    @GetMapping("/acvstore/sanpham")
//    public String sanPhamPage(Model model) {
//        List<Product> products = adminProductService.getAllProducts();
//        model.addAttribute("products", products);
//        return "WebQuanLy/sanpham";
//    }

    @GetMapping("/acvstore/khuyenmai")
    public String khuyenMaiPage() {
        return "WebQuanLy/khuyenmai";
    }





    @GetMapping("/acvstore/phanquyen")
    public String phanQuyenPage() {
        return "WebQuanLy/phanquyen";
    }



    @GetMapping("/acvstore/danhmuc")
    public String danhMucPage() {
        return "WebQuanLy/danhmuc";
    }

    @GetMapping("/acvstore/thongbao")
    public String thongBaoPage() {
        return "WebQuanLy/thongbao";
    }


    // Dashboard nhân viên
    @GetMapping("/employee/dashboard")
    public String employeeDashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        return "WebNhanVien/nv_dashboard";
    }

    // Các trang quản lý khác cho nhân viên
    @GetMapping("/employee/hoadon")
    public String employeeHoaDon(Model model) {
        model.addAttribute("activePage", "hoadon");
        return "WebNhanVien/nv_hoadon";
    }

    @GetMapping("/employee/sanpham")
    public String employeeSanPham(Model model) {
        model.addAttribute("activePage", "sanpham");
        return "WebNhanVien/nv_sanpham";
    }

    @GetMapping("/employee/khuyenmai")
    public String employeeKhuyenMai(Model model) {
        model.addAttribute("activePage", "khuyenmai");
        return "WebNhanVien/nv_khuyenmai";
    }

    @GetMapping("/employee/thongke")
    public String employeeThongKe(Model model) {
        model.addAttribute("activePage", "thongke");
        return "WebNhanVien/nv_thongke";
    }

    @GetMapping("/employee/khachhang")
    public String employeeKhachHang(Model model) {
        model.addAttribute("activePage", "khachhang");
        return "WebNhanVien/nv_khachhang";
    }

    @GetMapping("/employee/danhmuc")
    public String employeeDanhMuc(Model model) {
        model.addAttribute("activePage", "danhmuc");
        return "WebNhanVien/nv_danhmuc";
    }

    @GetMapping("/employee/profile")
    public String employeeProfile(Model model) {
        model.addAttribute("activePage", "profile");
        return "WebNhanVien/nv_profile";
    }

    @GetMapping("/employee/thongbao")
    public String employeeThongBao(Model model) {
        model.addAttribute("activePage", "thongbao");
        return "WebNhanVien/nv_thongbao";
    }

    @GetMapping("/employee/chatbox")
    public String employeeChatBox(Model model) {
        model.addAttribute("activePage", "chatbox");
        return "WebNhanVien/nv_chatbox";
    }


    @GetMapping("/")
    public String homePage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            // Người dùng đã đăng nhập
            if (authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                return "redirect:/acvstore/dashboard";
            } else if (authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_EMPLOYEE"))) {
                return "redirect:/employee/dashboard";
            } else if (authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_CUSTOMER"))) {
                return "redirect:/customer/index";
            }
        }
        // Người dùng chưa đăng nhập hoặc không có vai trò phù hợp
        return "redirect:/customer/index"; // Mặc định cho khách
    }


//    @GetMapping("/access-denied")
//    public String accessDenied() {
//        return "access-denied";  // Hiển thị trang lỗi
//    }

    @GetMapping("/customer/index")
    public String indexPage() {
        return "WebKhachHang/kh_index";
    }

    @GetMapping("/customer/donhang")
    public String donHangPage() {
        return "WebKhachHang/kh_donhang";
    }

    @GetMapping("/customer/giohang")
    public String gioHangPage() {
        return "WebKhachHang/kh_giohang";
    }

//    @GetMapping("/customer/login")
//    public String loginPage() {
//        return "WebKhachHang/kh_login";
//    }

//    @GetMapping("/khachhang/register")
//    public String registerPage() {
//        return "register";
//    }

    @GetMapping("/customer/sanpham")
    public String sanPhamPageKH() {
        return "WebKhachHang/kh_sanpham";
    }

    @GetMapping("/customer/chitietsanpham")
    public String chiTietSanPhamPage() {
        return "WebKhachHang/kh_chitietsanpham";
    }

    @GetMapping("/customer/thanhtoan")
    public String thanhToanPage() {
        return "WebKhachHang/kh_thanhtoan";
    }

    @GetMapping("/customer/chatbox")
    public String chatBoxPage() {
        return "WebKhachHang/kh_chatbox";
    }

    @Autowired
    public CustomerHomepageService homepageService;

    @Autowired
    public CategoryRepository categoryRepository;

    @GetMapping("/trangchu")
    public String showHomePage(Model model) {
        model.addAttribute("newProducts", homepageService.getNewestProducts());
        model.addAttribute("flashSaleProducts", homepageService.getFlashSaleProducts());
        model.addAttribute("bestSellers", homepageService.getBestSellingProducts());
        model.addAttribute("categories", categoryRepository.findAll());
        return "WebKhachHang/kh_index";
    }

}
