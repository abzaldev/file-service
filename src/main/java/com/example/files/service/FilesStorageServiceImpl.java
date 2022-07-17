package com.example.files.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;


@Service
public class FilesStorageServiceImpl implements FilesStorageService {


    @Value("${file-path}")
    private String filePath;

    private Path getPath() {
        String userDirectory = new File("").getAbsolutePath();
        return Paths.get(userDirectory + "/" + filePath);
    }

    @Override
    public void init() {
        try {
            Files.createDirectory(getPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create upload folder!");
        }
    }

    @Override
    public void save(MultipartFile file, String fileName) {

        if (isContentTypeAllowed(file.getContentType())) {
            try {
                Files.copy(file.getInputStream(), getPath().resolve(fileName));
            } catch (Exception e) {
                throw new RuntimeException("Error: Failed to save file. Error: " + e.getMessage());
            }
        } else {
            throw new RuntimeException(" Error: Not allowed file format");
        }
    }

    @Override
    public Resource load(String filename) {
        try {
            Path file = getPath().resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(getPath().toFile());
    }

    public boolean deleteFileByName(String filename) {
        Path path = getPath().resolve(filename);
        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(getPath(), 1).filter(path -> !path.equals(getPath())).map(getPath()::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Could not load the files!");
        }
    }


    private boolean isContentTypeAllowed(String contentType) {
        return !(StringUtils.isEmpty(contentType)
                || !(contentType.equals("image/gif")
                || contentType.equals("image/png")
                || contentType.equals("image/jpg")
                || contentType.equals("image/jpeg")
                || contentType.equals("image/pjpeg")
                || contentType.equals("image/webp")
        ));
    }


}
