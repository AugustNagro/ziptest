package com.augustnagro.ziptests;

import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(10)
public class FixedThreadPool {

    // where the zip archive & unziped files are kept
    public Path zipDirectory;

    // the zip archive itself
    public Path zipFile;

    public ExecutorService executor;

    @Setup(Level.Iteration)
    public void setup() throws IOException {
        zipDirectory = Files.createTempDirectory(null);
        zipFile = zipDirectory.resolve("ffmpeg.zip");
        Files.copy(getClass().getResourceAsStream("/ffmpeg.zip"), zipFile);

        int parallelism = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(parallelism);
    }

    @Benchmark
    public void unzip() throws IOException, InterruptedException {

        try (ZipFile zip = new ZipFile(zipFile.toFile())) {

            List<ZipEntryExtractor> callables = zip.stream()
                    .map(entry -> new ZipEntryExtractor(zip, entry, zipDirectory))
                    .collect(Collectors.toList());

            executor.invokeAll(callables);
        }
    }

    @TearDown(Level.Iteration)
    public void teardown() throws IOException, InterruptedException {
        Files.walk(zipDirectory)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);

        executor.shutdown();
        boolean terminated = executor.awaitTermination(5, TimeUnit.SECONDS);
        if (!terminated) throw new RuntimeException("Executor not terminated");
    }
}
