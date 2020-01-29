package labnew;

import javax.print.DocFlavor;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IdcDm {
    public static void main(String[] args) throws InterruptedException {
        // Start timer for program running time
        long startTime = System.nanoTime();

        // Parsing given arguments
        List<String> mirrors = new ArrayList<String>();
        int workers = 1;
        try {
            // Handling xfile of mirrors or single url
            if (new File(args[0]).isFile()) {
                mirrors = Helpers.readFile(args[0]);
            } else {
                ;
                mirrors.add(args[0]);
            }
            // Handling workers amount.
            if (args.length > 1) {
                workers = Integer.parseInt(args[1]);
            }
        } catch (Exception e) {
            System.err.println("Please enter valid command: IdcDM <file with mirrors or urls> <Number of concurrent http>");
        }

        // Gather info regarding file
        String fileName = Helpers.getFileNameFromURL(mirrors.get(0));
        long fileSize = Helpers.getFileSizeFromURL(mirrors.get(0));

        // Checking if file is too little - configure 1 worker
        if (fileSize < 10000) {
            workers = 1;
        }

        // Check if download has stop
        DivideManager divider = null;
        String serialFileName = String.format("%s.tmp", fileName);
        // Deserialize object if exists and valid structure
        if (new File(serialFileName).isFile()) {
            divider = Helpers.deSerializeObject(serialFileName);
            divider.re_divide(workers, mirrors);
        }
        // Serialized object if unable to recover or download didn't started yet
        if (divider == null) {
            divider = Helpers.serializeObject(new DivideManager(fileSize), serialFileName);
            divider.divide(workers, mirrors);
            Helpers.serializeObject(divider, serialFileName);
        }

        // Create shared object for writing the file
        RandomAccessFile downloadedFile  = null;
        try {
            downloadedFile = new RandomAccessFile(fileName, "rwd");
        } catch (FileNotFoundException e) {
            System.err.println("Unable to write to file system - Please check permissions !!!");
        }

        // Create tasks and Executors pool
        ExecutorService pool = Executors.newFixedThreadPool(workers + 1);
        int workerID = 0;
        for (ChunkBytes chunk : divider.chunks) {
            String bytesRange = String.format("Bytes=%d-%d", chunk.get_startByte(), chunk.get_endByte());
            System.out.println(String.format("%s [%d] Start downloading range %s from:%s\n %s", ConsoleColors.PURPLE_BOLD,
                    workerID, bytesRange, ConsoleColors.RESET, chunk.get_mirror()));
            DownloadWorker newWorker = new DownloadWorker(chunk, downloadedFile.getChannel(), divider, workerID);
            pool.execute(newWorker);
            workerID++;
        }
        long pid = ProcessHandle.current().pid();
        CommandWorker connectionTask = new CommandWorker(String.format("lsof -i | grep java | grep -v localhost | grep %d | wc -l", (int)pid), 20000);
        pool.execute(connectionTask);
        pool.shutdown();


        // Print percentage until termination
        boolean poolState = pool.isTerminated();
        while (!poolState) {
            int percentage = divider.getPercentage();
            if (percentage != -1) {
                System.out.println("Downloaded " + ConsoleColors.BLUE_BOLD + percentage + "%" + ConsoleColors.RESET);
                Helpers.serializeObject(divider, serialFileName);
            }
            poolState = pool.isTerminated();
        }

        // Download closing
        long endTime   = System.nanoTime();
        System.out.println("Downloaded " + ConsoleColors.BLUE_BOLD + "100%" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.GREEN_BOLD + "Download succeeded" + ConsoleColors.RESET + "\n");
        try {
            downloadedFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Stop time program running time
        String connections = connectionTask.getOutPut().replace(" ", "");
        System.out.println(ConsoleColors.RED_BACKGROUND + "Program summary:" + ConsoleColors.RESET);
        System.out.println("Program running time: " + ConsoleColors.RED_BOLD +  (endTime - startTime) / 1e+9 + " seconds" + ConsoleColors.RESET);
        System.out.println("Number of open https connections while program run: " + ConsoleColors.RED_BOLD + connections + ConsoleColors.RESET);
        System.out.println("File downloaded path: " + ConsoleColors.RED_BOLD + new File(fileName).getPath() + ConsoleColors.RESET);
        System.out.println("Size of downloaded file: "  + ConsoleColors.RED_BOLD + divider.getBytesDownloaded() + " Bytes" + ConsoleColors.RESET);
        System.out.println("Original file size: " + ConsoleColors.RED_BOLD + fileSize + " Bytes" + ConsoleColors.RESET);
        System.out.println("Difference in file size: " + ConsoleColors.RED_BOLD + (fileSize - divider.getBytesDownloaded()) + " Bytes" + ConsoleColors.RESET);
        System.out.println("MD5 checksum of Downloaded file: " + ConsoleColors.RED_BOLD + Helpers.executeCommand("md5 " + new File(fileName).getPath()) + ConsoleColors.RESET);
    }
}
