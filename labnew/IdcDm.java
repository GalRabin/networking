package labnew;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IdcDm {
        public static void main(String[] args) throws InterruptedException {
        // Parsing given arguments
        List<String> mirrors = new ArrayList<String>();
        int workers = 1;
        try {
            // Handling xfile of mirrors or single url
            if (new File(args[0]).isFile()) {
                mirrors = Helpers.readFile(args[0]);
            } else { ;
                mirrors.add(args[0]);
            }
            // Handling workers amount.
            if (args.length > 1){
                workers = Integer.parseInt(args[1]);
            }
        }
        catch (Exception e) {
            System.err.println("Please enter valid command: IdcDM <file with mirrors or urls> <Number of concurrent http>");
        }

        // Gather info regarding file
        String fileName = Helpers.getFileNameFromURL(mirrors.get(0));
        long fileSize = Helpers.getFileSizeFromURL(mirrors.get(0));

        // Check if download has stop
        DivideManager divider = null;
        String serialFileName = String.format("%s.tmp", fileName);
        // Deserialize object if exists and valid structure
        if (new File(serialFileName).isFile()){
           divider = Helpers.deSerializeObject(serialFileName);
           divider.re_divide(workers, mirrors);
        }
        // Serialized object if unable to recover or download didn't started yet
        if (divider == null){
            divider = Helpers.serializeObject(new DivideManager(fileSize), serialFileName);
            divider.divide(workers, mirrors);
            Helpers.serializeObject(divider, serialFileName);
        }

        // Create shared object for writing the file
        RandomAccessFile downloadedFile = null;
        try {
            downloadedFile = new RandomAccessFile(fileName, "rwd");
        } catch (FileNotFoundException e) {
            System.err.println("Unable to write to file system - Please check permissions !!!");
        }


        ExecutorService pool = Executors.newFixedThreadPool(workers);
        int workerID = 0;
        for (ChunkBytes chunk : divider.chunks) {
            String bytesRange = String.format("Bytes=%d-%d", chunk.get_startByte(), chunk.get_endByte());
            System.out.println(String.format("[%d] Start downloading range %s from:", workerID, bytesRange));
            System.out.println(chunk.get_mirror());
            DownloadWorker newWorker = new DownloadWorker(chunk, downloadedFile, divider, workerID);
            pool.execute(newWorker);
            workerID++;
        }
        pool.shutdown();

        // Print percentage until termination
        boolean poolState = pool.isTerminated();
        while (!poolState){
            int percentage = divider.getPercentage();
            if (percentage != -1){
                System.out.println("Downloaded " + percentage + "%");
                Helpers.serializeObject(divider,serialFileName);
            }
            Thread.sleep(50);
            poolState = pool.isTerminated();
        }
        System.out.println("Downloaded 100%");
        System.out.println("Download succeeded");
    }
}
