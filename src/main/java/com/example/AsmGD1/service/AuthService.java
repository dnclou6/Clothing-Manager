package com.example.AsmGD1.service;

import com.example.AsmGD1.entity.User;
import com.example.AsmGD1.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.IntBuffer;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    private LBPHFaceRecognizer faceRecognizer;
    private CascadeClassifier faceDetector;

    private final String faceStoragePath = "faces/";
    private final String cascadePath = "src/main/resources/haarcascade_frontalface_default.xml";

    @PostConstruct
    public void init() {
        faceRecognizer = LBPHFaceRecognizer.create();
        faceDetector = new CascadeClassifier(cascadePath);

        File faceDir = new File(faceStoragePath);
        if (!faceDir.exists()) {
            faceDir.mkdirs();
        }

        trainFaceRecognizer();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean registerFace(User user, String base64Image) {
        try {
            if (!"ADMIN".equalsIgnoreCase(user.getRole())) return false;

            Mat faceMat = extractFaceMatFromBase64(base64Image);
            if (faceMat == null) return false;

            String filePath = faceStoragePath + user.getId() + ".jpg";
            opencv_imgcodecs.imwrite(filePath, faceMat);

            user.setFaceId(String.valueOf(user.getId()));
            userRepository.save(user);

            trainFaceRecognizer();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public User findUserByFaceImage(String base64Image) {
        try {
            Mat faceMat = extractFaceMatFromBase64(base64Image);
            if (faceMat == null) return null;

            int[] label = new int[1];
            double[] confidence = new double[1];
            faceRecognizer.predict(faceMat, label, confidence);

            return confidence[0] <= 100
                    ? userRepository.findByFaceId(String.valueOf(label[0])).orElse(null)
                    : null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isFaceMatched(String username, String base64Image) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || user.getFaceId() == null) return false;

        Mat faceMat = extractFaceMatFromBase64(base64Image);
        if (faceMat == null) return false;

        int[] label = new int[1];
        double[] confidence = new double[1];
        faceRecognizer.predict(faceMat, label, confidence);

        return confidence[0] <= 100 && String.valueOf(label[0]).equals(user.getFaceId());
    }

    private Mat extractFaceMatFromBase64(String imageBase64) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
            Mat image = opencv_imgcodecs.imdecode(new Mat(new BytePointer(imageBytes)), opencv_imgcodecs.IMREAD_COLOR);
            Mat gray = new Mat();
            opencv_imgproc.cvtColor(image, gray, opencv_imgproc.COLOR_BGR2GRAY);

            RectVector faces = new RectVector();
            faceDetector.detectMultiScale(gray, faces);
            if (faces.size() == 0) return null;

            Rect face = faces.get(0);
            Mat faceMat = new Mat(gray, face);
            opencv_imgproc.resize(faceMat, faceMat, new Size(100, 100));
            return faceMat;
        } catch (Exception e) {
            return null;
        }
    }

    private void trainFaceRecognizer() {
        try {
            List<Mat> images = new ArrayList<>();
            List<Integer> labels = new ArrayList<>();

            List<User> admins = userRepository.findByRoleAndIsDeletedFalse("ADMIN");
            for (User user : admins) {
                if (user.getFaceId() != null) {
                    String filePath = faceStoragePath + user.getFaceId() + ".jpg";
                    File file = new File(filePath);
                    if (file.exists()) {
                        Mat faceMat = opencv_imgcodecs.imread(filePath, opencv_imgcodecs.IMREAD_GRAYSCALE);
                        if (!faceMat.empty()) {
                            images.add(faceMat);
                            labels.add(Integer.parseInt(user.getFaceId()));
                        }
                    }
                }
            }

            if (!images.isEmpty()) {
                MatVector matVector = new MatVector(images.size());
                Mat labelsMat = new Mat(images.size(), 1, opencv_core.CV_32SC1);
                IntBuffer labelsBuf = labelsMat.createBuffer();

                for (int i = 0; i < images.size(); i++) {
                    matVector.put(i, images.get(i));
                    labelsBuf.put(i, labels.get(i));
                }

                faceRecognizer.train(matVector, labelsMat);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ====== OTP & PASSWORD =======

    public void sendOtp(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String otp = generateOtp();
            user.setResetOtp(otp);
            user.setOtpExpiry(LocalDateTime.now().plusMinutes(3));
            userRepository.save(user);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Mã OTP đặt lại mật khẩu");
            message.setText("Mã OTP của bạn là: " + otp);
            mailSender.send(message);
        });
    }

    public String generateOtp() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }

    public String verifyOtp(String email, String otp) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) return "not_found";

        User user = userOptional.get();
        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) return "expired";
        return otp.equals(user.getResetOtp()) ? "valid" : "invalid";
    }

    public void resetPassword(String email, String newPassword) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setPassword(newPassword);
            user.setResetOtp(null);
            user.setOtpExpiry(null);
            userRepository.save(user);
        });
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
