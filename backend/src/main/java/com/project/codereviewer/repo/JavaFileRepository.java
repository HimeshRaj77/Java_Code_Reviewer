package com.project.codereviewer.repo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.springframework.stereotype.Repository;

@Repository
public class JavaFileRepository {
    public String loadFile(File file) throws IOException {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }
} 