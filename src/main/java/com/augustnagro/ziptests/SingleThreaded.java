package com.augustnagro.ziptests;

import org.openjdk.jmh.annotations.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(10)
public class SingleThreaded {

    // where the zip archive & unziped files are kept
    public Path zipDirectory;

    // the zip archive itself
    public Path zipFile;

    @Setup(Level.Iteration)
    public void setup() throws IOException {
        zipDirectory = Files.createTempDirectory(null);
        zipFile = zipDirectory.resolve("ffmpeg.zip");
        Files.copy(getClass().getResourceAsStream("/ffmpeg.zip"), zipFile);
    }

    @Benchmark
    public void unzip() throws IOException {

        try (ZipFile zip = new ZipFile(zipFile.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                Path outputPath = zipDirectory.resolve(entry.getName());
                Files.createDirectories(outputPath.getParent());
                if (entry.isDirectory()) continue;

                try (BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                     BufferedOutputStream os = new BufferedOutputStream(Files.newOutputStream(outputPath))) {
                    is.transferTo(os);
                }
            }
        }
    }

    @TearDown(Level.Iteration)
    public void teardown() throws IOException {
        Files.walk(zipDirectory)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

}
