package kopo.poly.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class MultipartFileAdapter implements MultipartFile {

    private final byte[] fileContent;
    private final String originalFilename;

    public MultipartFileAdapter(InputStream inputStream, String originalFilename) throws IOException {
        this.fileContent = inputStream.readAllBytes();
        this.originalFilename = originalFilename;
    }

    @Override
    public String getName() {
        return originalFilename;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return "image/jpeg"; // 필요시 변경
    }

    @Override
    public boolean isEmpty() {
        return fileContent.length == 0;
    }

    @Override
    public long getSize() {
        return fileContent.length;
    }

    @Override
    public byte[] getBytes() {
        return fileContent;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(fileContent);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(fileContent);
        }
    }
}
