package labnew;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Helpers {
    public static DivideManager deSerializeObject(String fileName){
        DivideManager divider = null;
        try {
            FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            divider = (DivideManager) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException c) {
            System.out.println("Employee class not found");
            c.printStackTrace();
        }

        return divider;
    }

    public static DivideManager serializeObject(DivideManager divider ,String fileName){
        try {
            // Writing to temp copy of meta data
            String fileCopy = String.format("%s.copy", fileName);
            FileOutputStream fileOut = new FileOutputStream(fileCopy);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(divider);
            out.close();
            fileOut.close();

            // Rename file
            File fileCopyObj = new File(fileCopy);

            boolean boolRename = fileCopyObj.renameTo(new File(fileName));
            boolean boolDelete = fileCopyObj.delete();
        } catch (IOException i) {
            i.printStackTrace();
        }

        return divider;
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
