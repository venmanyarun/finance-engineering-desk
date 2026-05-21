package com.finance.tracker.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleDriveService {

    private static final String APPLICATION_NAME = "Finance Tracker";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public String uploadJsonToDrive(String jsonContent, String filename, String accessToken) throws IOException, GeneralSecurityException {
        // In a real implementation, use the authorized Drive service from the OAuth2 security context
        // This is a simplified placeholder for the logic
        Drive service = new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .build();

        File fileMetadata = new File();
        fileMetadata.setName(filename);
        fileMetadata.setMimeType("application/json");

        java.io.File tempFile = java.io.File.createTempFile("export", ".json");
        java.nio.file.Files.writeString(tempFile.toPath(), jsonContent);

        FileContent mediaContent = new FileContent("application/json", tempFile);
        File file = service.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        tempFile.delete();
        return file.getId();
    }
}