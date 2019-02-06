package com.augustnagro.ziptests;

import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

/**
 * Requies `7z.exe` on Windows and `unzip` otherwise.
 */
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(10)
public class DelegateToProcess {

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
    public int unzip() throws IOException, InterruptedException {
        ProcessBuilder pb;

        if (System.getProperty("os.name").toLowerCase().contains("win"))
            pb = new ProcessBuilder("7z.exe", "x", zipFile.toString());
        else
            pb = new ProcessBuilder("unzip", zipFile.toString());

        pb.directory(zipDirectory.toFile());

        return pb.start().waitFor();
    }

    @TearDown(Level.Iteration)
    public void teardown() throws IOException {
        Files.walk(zipDirectory)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

}
