package com.guenbon.jochuckhub.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageService {

    private final String ROOT = "C:/project/jochuckhub_profiles/"; // 로컬 저장 경로

    // 프로필 이미지 저장 + 리사이징
    public String saveProfileImage(Long memberId, MultipartFile file) throws IOException {

        // 디렉토리 생성
        String dirPath = ROOT + memberId + "/";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 파일명 생성
        String uuid = UUID.randomUUID().toString();
        String ext = getExtension(file.getOriginalFilename());
        String filename = uuid + "." + ext;

        // 원본 이미지 읽기
        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        // 리사이징 (300 x 300)
        int targetWidth = 300;
        int targetHeight = 300;

        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        // 파일 저장
        File outputFile = new File(dirPath + filename);
        ImageIO.write(resizedImage, ext, outputFile);

        // URL 반환
        return "/images/profile/" + memberId + "/" + filename;
    }

    private String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}

