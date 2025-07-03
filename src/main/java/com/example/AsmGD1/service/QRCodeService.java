package com.example.AsmGD1.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
public class QRCodeService {

    public void generateQRCodeToFile(String text, int width, int height, String filePath) throws WriterException, IOException {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);

            Path path = Paths.get(filePath);
            File parentDir = path.getParent().toFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
                System.out.println("Created directory: " + parentDir.getAbsolutePath());
            }

            MatrixToImageWriter.writeToPath(matrix, "PNG", path);
            System.out.println("QR code generated at: " + filePath);
        } catch (IOException | WriterException e) {
            System.err.println("Error generating QR code: " + e.getMessage());
            throw e;
        }
    }

    public String generateQRCodeBase64(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}
