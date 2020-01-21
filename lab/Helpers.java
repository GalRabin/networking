package lab;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Helpers {
    public static int[] split_worker_mirrors(int mirrors, int workers){
        int steps = (int) Math.floor(workers / (double)mirrors);
        int[] split = new int[mirrors];

        int index = 0;
        int expected_max = mirrors * steps;
        int current_workers = steps;
        while (expected_max >= current_workers){
            if (current_workers == expected_max && mirrors > 1){
                split[index] = workers - split[index - 1];
            } else {
                split[index] = steps;
            }
            current_workers += steps;
            index++;
        }
        return split;
    }

    public static long[] range_bytes(long start, long end, int workers){
        int steps = (int) Math.floor(end / (double)workers);
        long[] range = new long[workers + 1];

        // Init ranges for each worker
        long expected_max = workers * steps;
        long current = start;
        int index = 0;
        while (expected_max >= current){
            if (current == expected_max && workers > 1){
                range[index] = end;
            } else {
                range[index] = current;
            }
            current += steps;
            index++;
        }

        return range;
    }

    /**
     * Get file size before start downloading
     * @param url url containing the file
     * @return file size in bytes
     */
    public static long getFileSizeFromURL(String url){
        long file_size = 0;
        try {
            URLConnection urlConnection = new URL(url).openConnection();
            urlConnection.connect();
            file_size = urlConnection.getContentLengthLong();
        }
        catch (IOException e){
            System.err.println(e);
        }
        return file_size;
    }

    /**
     * Get url and return the file which in the end, for example: http://ynet.co.il/index.html --> file is index.html
     * @param url url as string
     * @return file name
     */
    public static String getFileNameFromURL(String url){
        String[] url_splited = url.split("/");
        return url_splited[url_splited.length - 1];
    }

    /**
     * Get file content in to string array by lines
     * @param file file path
     * @return arrays of strings (each index new line)
     */
    public static String[] readFile(String file){
        String[] stringArray = new String[0];
        try {
            Path filePath = new File("file").toPath();
            List<String> stringList = Files.readAllLines(filePath);
            stringArray = stringList.toArray(new String[]{});
        }
        catch (IOException e){
            System.err.println("Not valid file supplied");
        }

        return  stringArray;
    }
}
