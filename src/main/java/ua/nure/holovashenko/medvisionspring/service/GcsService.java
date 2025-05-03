package ua.nure.holovashenko.medvisionspring.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

        return String.format("gs://%s/%s", BUCKET_NAME, objectName);
    }

    public byte[] downloadFile(String gcsUrl) throws IOException {
        String objectName = extractObjectName(gcsUrl);
        BlobId blobId = BlobId.of(BUCKET_NAME, objectName);
        return storage.readAllBytes(blobId);
    }

    private String extractObjectName(String gcsUrl) {
        return gcsUrl.replace("gs://" + BUCKET_NAME + "/", "");
    }
}
