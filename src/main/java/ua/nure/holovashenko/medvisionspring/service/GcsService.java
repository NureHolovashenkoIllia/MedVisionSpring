package ua.nure.holovashenko.medvisionspring.service;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;

@Service
@RequiredArgsConstructor
public class GcsService {

    private final Storage storage;
    private final String BUCKET_NAME = "BUCKET_NAME";

    public String uploadFile(File file, String objectName, String contentType) throws IOException {
        BlobId blobId = BlobId.of(BUCKET_NAME, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
        System.out.println("Service account email: " + storage.getOptions().getCredentials());

        try (FileInputStream inputStream = new FileInputStream(file);
             var writeChannel = storage.writer(blobInfo);
             var outputStream = Channels.newOutputStream(writeChannel)) {

            inputStream.transferTo(outputStream);
        }

        storage.createAcl(blobId, Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

        return String.format("https://storage.googleapis.com/%s/%s", BUCKET_NAME, objectName);
    }

    public byte[] downloadFileGcs(String gcsUrl) throws IOException {
        String objectName = extractObjectName(gcsUrl);
        BlobId blobId = BlobId.of(BUCKET_NAME, objectName);
        return storage.readAllBytes(blobId);
    }

    public byte[] downloadFile(String publicUrl) throws IOException {
        URI uri = URI.create(publicUrl);
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

    private String extractObjectName(String gcsUrl) {
        return gcsUrl.replace("gs://" + BUCKET_NAME + "/", "");
    }
}
