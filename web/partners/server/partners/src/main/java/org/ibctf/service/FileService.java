package org.ibctf.service;

import org.ibctf.model.Avatar;
import org.ibctf.model.Partner;
import org.ibctf.repository.AvatarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class FileService {

    private static final String FILE_BIN_PATH = "/usr/bin/file";
    private static final String DIGEST_ALGORITHM = "SHA-256";
    private static final String PNG_FILE_TYPE_TELL = "PNG image data";
    private static final String JPEG_FILE_TYPE_TELL = "JPEG image data";
    private static final String RESULT_PNG = "png";
    private static final String RESULT_JPG = "jpeg";

    private final AvatarRepository avatarRepository;

    @Autowired
    public FileService(AvatarRepository avatarRepository) {
        this.avatarRepository = avatarRepository;
    }

    public Avatar fileToAvatar(MultipartFile file, Partner partner) throws Exception {
        File tf = tempStore(file);
        String checksum = calculateChecksum(file.getBytes());
        String b64Data = toBase64Data(file);
        String fileType = determineFileType(tf);

        if (!tf.delete()) {
            Files.delete(tf.toPath());
        }

        if (fileType == null) {
            throw new InvalidParameterException();
        }

        Avatar avatar = partner.getAvatar();
        if (avatar == null) {
            avatar = new Avatar(b64Data, checksum, fileType, partner);
        } else {
            avatar.setPartner(partner);
            avatar.setImage(b64Data);
            avatar.setChecksum(checksum);
            avatar.setFileType(fileType);
        }
        return avatarRepository.save(avatar);
    }

    public File tempStore(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename();
        Path path = Files.createTempFile(name, "");
        Path base = path.resolveSibling(name);
        Files.deleteIfExists(base);
        path = Files.move(path, base);
        File tf = path.toFile();
        FileOutputStream fos = new FileOutputStream(tf);
        fos.write(file.getBytes());
        return tf;
    }

    public String determineFileType(File file) throws IOException, InterruptedException {
        String output = run(FILE_BIN_PATH, file.getAbsolutePath());
        if (output.contains(PNG_FILE_TYPE_TELL)) {
            return RESULT_PNG;
        } else if (output.contains(JPEG_FILE_TYPE_TELL)) {
            return RESULT_JPG;
        } else {
            return null;
        }
    }

    public String run(String... args) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = processBuilder.start();
        int exit = process.waitFor();
        if (exit != 0) {
            return null;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String s;
        StringBuilder out = new StringBuilder();
        while ((s = in.readLine()) != null) {
            out.append(s);
        }
        return out.toString();
    }

    public String toBase64Data(MultipartFile file) throws IOException {
        return Base64.getEncoder().encodeToString(file.getBytes());
    }

    public String calculateChecksum(byte[] fileBytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);
        md.update(fileBytes);
        byte[] digest = md.digest();
        return Base64.getEncoder().encodeToString(digest);
    }
}
