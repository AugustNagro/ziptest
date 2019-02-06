package com.augustnagro.ziptests;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipEntryExtractor implements Callable<Void> {
    private final ZipFile  zip;
    private final ZipEntry entry;
    private final Path     zipDirectory;

    public ZipEntryExtractor(ZipFile zip, ZipEntry entry, Path zipDirectory) {
        this.zip = zip;
        this.entry = entry;
        this.zipDirectory = zipDirectory;
    }

    @Override
    public Void call() throws Exception {
        Path output = zipDirectory.resolve(entry.getName());
        Files.createDirectories(output.getParent());

        if (entry.isDirectory()) return null;

        try (BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
             BufferedOutputStream os = new BufferedOutputStream(Files.newOutputStream(output))) {

            is.transferTo(os);
        }

        return null;
    }
}