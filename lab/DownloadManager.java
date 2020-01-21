package lab;

import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.File;
import java.util.concurrent.TimeUnit;


// Import helper functions
import static lab.Helpers.*;



public class DownloadManager {

    static int TOO_SMALL = 5000;

    public static void downloadManager(String[] mirrors, int workers){
        // Declare how many connections
        System.out.println(String.format("Downloading using %d connections...", workers));
        // Define file size for percentage in FileHandler object
        String fileName = getFileNameFromURL(mirrors[0]);
        Path currentRelativePath = Paths.get("");
        String fullFilePath = currentRelativePath.toAbsolutePath().toString() + "/" + fileName;
        long fileSize = getFileSizeFromURL(mirrors[0]);
        System.out.println(String.format("Total file %s size %d Bytes", fileName, fileSize));
        FileManager file = new FileManager(fullFilePath, fileSize);

        // Spiting work load
        int[] workers_per_mirrors = split_worker_mirrors(mirrors.length, workers);
        long[] range_byte_split = range_bytes(0, fileSize, workers);

        // Tasks creation
        Runnable[] tasks = new Runnable[workers + 1];
        int current_mirror = 0;
        while (current_mirror < mirrors.length){
            int workers_per_mirror =  workers_per_mirrors[current_mirror];
            for (int i = 0; i < workers_per_mirror; i++) {
                Runnable new_task;
                if (i < workers - 1) {
                    new_task = new DownloadWorker(mirrors[current_mirror], range_byte_split[i], range_byte_split[i + 1] - 1, file);
                } else {
                    new_task = new DownloadWorker(mirrors[current_mirror], range_byte_split[i], range_byte_split[i + 1], file);
                }
                tasks[i] = new_task;
            }
            current_mirror++;
        }

        Runnable new_task = new Logger(file);
        tasks[workers] = new_task;

        // Execute tasks in pool
        ExecutorService pool = Executors.newFixedThreadPool(workers + 1);
        for (Runnable task : tasks) {
            pool.execute(task);
        }

        // Shutdown pool
        pool.shutdown();

        // Waits for all finished
        try {
            boolean finished = pool.awaitTermination(30, TimeUnit.MINUTES);
            System.out.println("Download succeeded");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Init arguments
        String[] urls;
        int workers = 0;
        // Create array of urls from file or single url
        if (new File(args[0]).isFile()){
            urls = readFile(args[0]);
        } else {
            urls = new String[1];
            urls[0] = args[0];
        }

        // Check if number of workers configured
        if (args.length == 2){
            workers = Integer.parseInt(args[1]);
        }

        // Start download manager
        downloadManager(urls, workers);
    }
}
