package labnew;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Helpers {
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
    public static List<String> readFile(String file){
        List<String> stringList = new ArrayList<String>();
        try {
            Path filePath = new File(file).toPath();
            stringList = Files.readAllLines(filePath);
        }
        catch (IOException  e){
            System.err.println("Not valid file supplied");
        }

        return stringList;
    }
}
