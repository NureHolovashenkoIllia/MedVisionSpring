package ua.nure.holovashenko.medvisionspring.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;

@Service
@Slf4j
@Primary
@RequiredArgsConstructor
public class ResilientBlobStorageService implements BlobStorageService {

    private final AzureBlobStorageService azure;
    private final LocalStorageService local;

    @Override
    public String uploadFile(File file, String blobName, String contentType) throws IOException {
//        try {
//            return azure.uploadFile(file, blobName, contentType);
//        } catch (Exception e) {
//            log.warn("Azure upload failed, switching to local. Reason: {}", e.getMessage());
//            try {
//                return local.uploadFile(file, blobName, contentType);
//            } catch (Exception ex) {
//                log.error("Local upload also failed: {}", ex.getMessage());
//                throw new RuntimeException("Both Azure and Local upload failed");
//            }
//        }
        log.info("Тимчасово використовується лише локальне сховище для uploadFile");
        return local.uploadFile(file, blobName, contentType);
    }

    @Override
    public String uploadFileFromBytes(byte[] data, String blobName, String contentType) throws IOException {
//        try {
//            return azure.uploadFileFromBytes(data, blobName, contentType);
//        } catch (IOException e) {
//            return local.uploadFileFromBytes(data, blobName, contentType);
//        }
        log.info("Тимчасово використовується лише локальне сховище для uploadFileFromBytes");
        return local.uploadFileFromBytes(data, blobName, contentType);
    }

    @Override
    public byte[] downloadFileFromBlobUrl(String blobUrl) throws IOException {
//        URI uri = URI.create(blobUrl);
//
//        if (uri.getScheme().startsWith("http")) {
//            try {
//                return azure.downloadFileFromBlobUrl(blobUrl);
//            } catch (IOException e) {
//                return local.downloadFileFromBlobUrl(blobUrl);
//            }
//        } else if (uri.getScheme().equals("file")) {
//            return local.downloadFileFromBlobUrl(blobUrl);
//        } else {
//            throw new IOException("Невідомий тип сховища: " + blobUrl);
//        }
        log.info("Тимчасово використовується лише локальне сховище для downloadFileFromBlobUrl");
        return local.downloadFileFromBlobUrl(blobUrl);
    }

    @Override
    public byte[] downloadFileByName(String blobName) throws IOException {
//        try {
//            return azure.downloadFileByName(blobName);
//        } catch (IOException e) {
//            log.warn("Не вдалося завантажити метрики з Azure: {}. Пробуємо з локального сховища...", blobName, e);
//            return local.downloadFileByName(blobName);
//        }
        log.info("Тимчасово використовується лише локальне сховище для downloadFileByName");
        return local.downloadFileByName(blobName);
    }

    @Override
    public InputStream downloadFileStream(String blobName) throws IOException {
//        try {
//            return azure.downloadFileStream(blobName);
//        } catch (IOException e) {
//            return local.downloadFileStream(blobName);
//        }
        log.info("Тимчасово використовується лише локальне сховище для downloadFileStream");
        return local.downloadFileStream(blobName);
    }

    @Override
    public boolean deleteFile(String blobName) throws IOException {
//        try {
//            return azure.deleteFile(blobName);
//        } catch (IOException e) {
//            return local.deleteFile(blobName);
//        }
        log.info("Тимчасово використовується лише локальне сховище для deleteFile");
        return local.deleteFile(blobName);
    }
}
