package ua.nure.holovashenko.medvisionspring.storage;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class AzureBlobStorageService implements BlobStorageService {

    private final BlobServiceClient blobServiceClient;

    @Value("${azure.storage.container-name}")
    private String containerName;

    @Override
    public String uploadFile(File file, String blobName, String contentType) throws IOException {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        if (!containerClient.exists()) {
            containerClient.create();
        }

        BlobClient blobClient = containerClient.getBlobClient(blobName);

        try (InputStream inputStream = new FileInputStream(file)) {
            blobClient.upload(inputStream, file.length(), true);
            blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));
        }

        // Generate a public URL (assuming container access level is set to Blob or Container)
        return blobClient.getBlobUrl();
    }

    @Override
    public String uploadFileFromBytes(byte[] data, String blobName, String contentType) throws IOException {
        File temp = File.createTempFile("upload", ".tmp");
        try (FileOutputStream fos = new FileOutputStream(temp)) {
            fos.write(data);
        }
        return uploadFile(temp, blobName, contentType);
    }

    @Override
    public byte[] downloadFileFromBlobUrl(String blobUrl) throws IOException {
        URI uri = URI.create(blobUrl);
        URL url = uri.toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (InputStream inputStream = connection.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        }
    }

    @Override
    public byte[] downloadFileByName(String blobName) throws IOException {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blobClient.download(outputStream);

        return outputStream.toByteArray();
    }

    @Override
    public InputStream downloadFileStream(String blobName) throws IOException {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        return blobClient.openInputStream();
    }

    @Override
    public boolean deleteFile(String blobName) throws IOException {
        blobServiceClient.getBlobContainerClient(containerName)
                .getBlobClient(blobName)
                .delete();
        return true;
    }
}
