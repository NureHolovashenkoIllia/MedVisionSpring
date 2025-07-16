package ua.nure.holovashenko.medvisionspring.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface BlobStorageService {
    String uploadFile(File file, String blobName, String contentType) throws IOException;
    String uploadFileFromBytes(byte[] data, String blobName, String contentType) throws IOException;

    byte[] downloadFileFromBlobUrl(String blobUrl) throws IOException;
    byte[] downloadFileByName(String blobName) throws IOException;
    InputStream downloadFileStream(String blobName) throws IOException;

    boolean deleteFile(String blobName) throws IOException;
}
