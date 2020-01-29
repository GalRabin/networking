package lab;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IdcDm {
    // Variable define when to use only one worker - 1 MB
    private static int LOWSIZE = 1000000;

    public static void main(String[] args) {
        // Start timer for program running time
        long startTime = System.nanoTime();

        // Parsing given argument - <mirrors_file/single_mirror> <workers>
        List<String> mirrors = new ArrayList<String>();
        int workers = 1;
        try {
            // Check if <mirrors_file/single_mirror> is file or url
            if (new File(args[0]).isFile()) {
                mirrors = Helpers.readFile(args[0]);
            } else {
                mirrors.add(args[0]);
            }
            // Check if workers defined
            if (args.length > 1) {
                workers = Integer.parseInt(args[1]);
            }
        } catch (Exception e) {
            System.err.println("Please enter valid command: IdcDM <file with mirrors or urls> <Number of concurrent http> - aborting please check permissions !!!\"");
            System.exit(1);
        }

        // Gather info regarding file requested to download
        String fileName = Helpers.getFileNameFromURL(mirrors.get(0));
        long fileSize = Helpers.getFileSizeFromURL(mirrors.get(0));

        // Checking if file is too little - if too little the download  will use 1 worker
        if (fileSize < LOWSIZE) {
            workers = 1;
        }

        DivideManager divider = null;
        String serialFileName = String.format("%s.tmp", fileName);
        // Deserialize object if exists - if it is exists the download suppose to be resumed
        if (new File(serialFileName).isFile()) {
            divider = Helpers.deSerializeDivideManager(serialFileName);
            try{
                divider.reDivide(workers, mirrors);
            } catch (NullPointerException e){
                System.err.println("Unable to serialize DivideManager object - metadata corrupted!!!");
                divider = null;
            }
        }
        
        // Serialized object if corrupted recover file or download performed for the first time
        if (divider == null) {
            divider = new DivideManager(fileSize);
            divider.divide(workers, mirrors);
            Helpers.serializeDivideManager(divider, serialFileName);
        }

        // Create shared object for writing the file
        RandomAccessFile downloadedFile  = null;
        try {
            downloadedFile = new RandomAccessFile(fileName, "rwd");
        } catch (FileNotFoundException e) {
            System.err.println("Unable to write to file system - aborting please check permissions !!!");
            System.exit(1);
        }

        // Prints amount of workers in use
        System.out.println("Downloading using " + ConsoleColors.GREEN_BOLD + workers + ConsoleColors.RESET + " connections");

        // Create tasks and Executors pool
        ExecutorService pool = Executors.newFixedThreadPool(workers);
        int workerID = 0;
        for (ChunkBytes chunk : divider.chunks) {
            String bytesRange = String.format("Bytes=%d-%d", chunk.getStartByte(), chunk.getEndByte());
            System.out.println(String.format("%s [%d] Start downloading range %s from:%s\n %s", ConsoleColors.PURPLE_BOLD,
                    workerID, bytesRange, ConsoleColors.RESET, chunk.getMirror()));
            DownloadWorker newWorker = new DownloadWorker(chunk, downloadedFile.getChannel(), workerID);
            pool.execute(newWorker);
            workerID++;
        }
        pool.shutdown();

        // Print percentage until termination of execution pool
        boolean poolState = pool.isTerminated();
        while (!poolState) {
            int percentage = divider.getPercentage();
            if (percentage != -1) {
                System.out.println("Downloaded " + ConsoleColors.BLUE_BOLD + percentage + "%" + ConsoleColors.RESET);
                // Rewrite divider object to tmp file - its located here because the user seen last percent here
                Helpers.serializeDivideManager(divider, serialFileName);
            }
            poolState = pool.isTerminated();
        }

        // Download closing - end timer for program running time
        long endTime = System.nanoTime();
        System.out.println("Downloaded " + ConsoleColors.BLUE_BOLD + "100%" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.GREEN_BOLD + "Download succeeded" + ConsoleColors.RESET + "\n");

        // Close random access stream, delete metadata file and get downloaded file MD5 checksum
        String md5_checkSum = null;
        try {
            downloadedFile.close();
            new File(serialFileName).delete();
            md5_checkSum = ConsoleColors.RED_BOLD + Helpers.executeCommand("md5 " + new File(fileName).getPath()).split("=")[1].replace(" ", "");;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Program summary for validation
        System.out.println(ConsoleColors.RED_BACKGROUND + "Program summary:" + ConsoleColors.RESET);
        System.out.println("Program running time: " + ConsoleColors.RED_BOLD +  (endTime - startTime) / 1e+9 + " seconds" + ConsoleColors.RESET);
        System.out.println("File downloaded path: " + ConsoleColors.RED_BOLD + new File(fileName).getPath() + ConsoleColors.RESET);
        System.out.println("Size of downloaded file: "  + ConsoleColors.RED_BOLD + divider.getBytesDownloaded() + " Bytes" + ConsoleColors.RESET);
        System.out.println("Original file size: " + ConsoleColors.RED_BOLD + fileSize + " Bytes" + ConsoleColors.RESET);
        System.out.println("Difference in file size: " + ConsoleColors.RED_BOLD + (fileSize - divider.getBytesDownloaded()) + " Bytes" + ConsoleColors.RESET);

        // Prints md5 if succeed to get it by cmd
        if (md5_checkSum != null){
            System.out.println("MD5 checksum of Downloaded file: " + ConsoleColors.RED_BOLD + md5_checkSum + ConsoleColors.RESET);
        }


    }
}
