package de.unitrier.bicanic;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CounterParallel {

    private static int threadCount = 8;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        assert (args.length == 2);
        long start = System.currentTimeMillis();
        System.out.println(countLinesInAllFiles(args[0], args[1]) + " lines Counted");
        long duration = System.currentTimeMillis() - start;
        System.out.println(duration + "ms");
    }

    public static long countLines(String fileName) throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
            return lines.count();
        }
    }

    public static long countLinesInAllFiles(String folderPath, String regex) throws IOException, ExecutionException, InterruptedException {
        Pattern pattern = Pattern.compile(regex);
        List<Path> files;
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            files = paths
                    .filter(path -> pattern.matcher(path.toString()).matches())
                    .toList();
        }

        ExecutorService handler = Executors.newFixedThreadPool(threadCount);

        ArrayList<Future<Long>> dokumentZeilen = new ArrayList<>();

        for(Path file : files){
            dokumentZeilen.add(handler.submit(() -> countLines(file.toString())));
        }

        long zeilenSumme = 0;
        for(Future<Long> zeilen : dokumentZeilen){
            zeilenSumme = zeilenSumme + zeilen.get();
        }
        handler.shutdown();
        return zeilenSumme;
    }

}

