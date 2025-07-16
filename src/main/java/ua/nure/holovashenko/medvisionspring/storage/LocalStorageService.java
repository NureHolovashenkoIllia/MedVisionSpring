package ua.nure.holovashenko.medvisionspring.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.nio.file.*;

@Service
@RequiredArgsConstructor
public class LocalStorageService implements BlobStorageService {

    @Value("${local.storage.directory}")
    private String storageDirectory;

    private Path getTargetFilePath(String blobName) throws IOException {
        Path targetFile = Paths.get(storageDirectory, blobName);
        Path parentDir = targetFile.getParent();
        if (!Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        return targetFile;
    }

    @Override
    public String uploadFile(File file, String blobName, String contentType) throws IOException {
        Path targetFile = getTargetFilePath(blobName);

        try (InputStream inputStream = new FileInputStream(file)) {
            Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return targetFile.toUri().toString();
    }

    @Override
    public String uploadFileFromBytes(byte[] data, String blobName, String contentType) throws IOException {
        Path targetFile = getTargetFilePath(blobName);
        Files.write(targetFile, data);
        return targetFile.toAbsolutePath().toString();
    }

    @Override
    public byte[] downloadFileFromBlobUrl(String fileUrl) throws IOException {
        URI uri = URI.create(fileUrl);
        Path path = Paths.get(uri);
        return Files.readAllBytes(path);
    }

    @Override
    public byte[] downloadFileByName(String blobName) throws IOException {
        Path filePath = Paths.get(storageDirectory, blobName);
        return Files.readAllBytes(filePath);
    }

    @Override
    public InputStream downloadFileStream(String blobName) throws IOException {
        Path filePath = Paths.get(storageDirectory, blobName);
        return Files.newInputStream(filePath, StandardOpenOption.READ);
    }

    @Override
    public boolean deleteFile(String blobName) throws IOException {
        Path filePath = Paths.get(storageDirectory, blobName);
        return Files.deleteIfExists(filePath);
    }
}
