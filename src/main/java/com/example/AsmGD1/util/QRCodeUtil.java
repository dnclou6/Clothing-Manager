package com.example.AsmGD1.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class QRCodeUtil {

    private static final String BASE_DIR = "C:/DATN/uploads/qr/";

    public static void generateQRCodeImage(String text, int width, int height, String filePath)
            throws WriterException, IOException {

        // Kiểm tra và tạo thư mục nếu chưa tồn tại
        Path directoryPath = Paths.get(BASE_DIR);
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
    }

    // Shortcut tạo mã QR cho 1 productId (mặc định kích thước 250x250)
    public static void generateQRCodeForProduct(Integer productDetailId)
            throws WriterException, IOException {
        String text = productDetailId.toString();
        int width = 250;
        int height = 250;
        String filePath = "C:/DATN/uploads/qr/qr_" + productDetailId + ".png";
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        Path path = new File(filePath).toPath();
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
    }

}
