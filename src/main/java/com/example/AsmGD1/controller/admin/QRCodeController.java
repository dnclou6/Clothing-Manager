package com.example.AsmGD1.controller.admin;

import com.example.AsmGD1.util.QRCodeUtil;
import com.google.zxing.WriterException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/admin/qr")
public class QRCodeController {

    @GetMapping("/generate/{productDetailId}")
    public ResponseEntity<Resource> generateQRCode(@PathVariable("productDetailId") Integer productDetailId) {
        try {
            String filePath = "C:/DATN/uploads/qr/qr_" + productDetailId + ".png";
            QRCodeUtil.generateQRCodeImage(productDetailId.toString(), 250, 250, filePath);

            File qrFile = new File(filePath);
            if (!qrFile.exists()) return ResponseEntity.notFound().build();

            Resource fileResource = new FileSystemResource(qrFile);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(fileResource);

        } catch (IOException | WriterException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
