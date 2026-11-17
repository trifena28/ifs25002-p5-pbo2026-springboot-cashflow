package org.delcom.ifs25002springbootcashflow.adapter.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileStorage {

    private final Path baseDirectory;

    public FileStorage(String uploadDir) {
        this.baseDirectory = Paths.get(uploadDir);
        try {
            Files.createDirectories(this.baseDirectory);
        } catch (IOException ex) {
            throw new IllegalStateException("Direktori penyimpanan gagal dibuat: " + uploadDir, ex);
        }
    }

    public String store(InputStream inputStream, String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Nama berkas tidak boleh kosong.");
        }
        String generatedName = UUID.randomUUID() + extractExtension(originalFilename);
        Path targetPath = baseDirectory.resolve(Paths.get(generatedName)).normalize().toAbsolutePath();

        if (!targetPath.getParent().equals(baseDirectory.toAbsolutePath())) {
            throw new SecurityException("Lokasi penyimpanan berkas di luar direktori yang diizinkan.");
        }

        try {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return generatedName;
        } catch (IOException ex) {
            throw new IllegalStateException("Terjadi kegagalan saat menyimpan berkas.", ex);
        }
    }

    public byte[] loadAsBytes(String filename) {
        try {
            Path targetPath = baseDirectory.resolve(filename).normalize().toAbsolutePath();
            if (Files.exists(targetPath) && Files.isReadable(targetPath)) {
                return Files.readAllBytes(targetPath);
            }
            return null;
        } catch (IOException ex) {
            return null;
        }
    }

    public void delete(String filename) {
        if (filename == null || filename.isBlank()) {
            return;
        }
        try {
            Path targetPath = baseDirectory.resolve(filename).normalize().toAbsolutePath();
            Files.deleteIfExists(targetPath);
        } catch (IOException ignored) {
        }
    }

    private String extractExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }
}