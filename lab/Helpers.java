package lab;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Helpers {
    /**
     * Deserialize DivideManager from file
     * @param fileName fileName the object serialized in.
     * @return DivideManager from last run, if not able to parse due to corrupted file return null
     */
    public static DivideManager deSerializeDivideManager(String fileName) {
        DivideManager divider = null;
        try {
            // Deserialize DivideManager from file
            FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            divider = (DivideManager) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Unable to resume past download due to corrupted metadata file, Start download form the beginning");
        }

        return divider;
    }


    /**
     * Serialize DivideManager object to file system (In simple words - writing object to file system)
     * @param divider DivideManager object to be write in file system
     * @param fileName fileName the object serialized in.
     */
    public static void serializeDivideManager(DivideManager divider, String fileName) {
        try {
            // Writing to temp copy of meta data - In order to not loose data in system error
            String fileCopy = String.format("%s.copy", fileName);
            FileOutputStream fileOut = new FileOutputStream(fileCopy);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(divider);
            out.close();
            fileOut.close();
            // Exchanging files after finish serialize
            File fileCopyObj = new File(fileCopy);
            // Rename file to original serialized file name
            fileCopyObj.renameTo(new File(fileName));
            // Delete copy file
            fileCopyObj.delete();
        } catch (IOException i) {
            System.err.println("Unable to serialize DivideManager object - please check file permissions!!!");
            System.exit(1);
        }
    }


    /**
     * Execute command if possible if not return null
     * @param command command to be execute
     * @return output of the command
     */
    public static String executeCommand(String command) {
        String output = null;
        try {
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            output = stdInput.readLine();
        } catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
        }

        return output;
    }


    /**
     * Get file size before start downloading
     *
     * @param url url containing the file
     * @return file size in bytes
     */
    public static long getFileSizeFromURL(String url) {
        long file_size = 0;
        try {
            URLConnection urlConnection = new URL(url).openConnection();
            urlConnection.connect();
            file_size = urlConnection.getContentLengthLong();
        } catch (IOException e) {
            System.err.println(e);
        }
        return file_size;
    }

    /**
     * Get url and return the file which in the end, for example: http://ynet.co.il/index.html --> file is index.html
     *
     * @param url url as string
     * @return file name
     */
    public static String getFileNameFromURL(String url) {
        String[] url_splited = url.split("/");
        return url_splited[url_splited.length - 1];
    }

    /**
     * Get file content in to string array by lines
     *
     * @param file file path
     * @return arrays of strings (each index new line)
     */
    public static List<String> readFile(String file) {
        List<String> stringList = new ArrayList<String>();
        try {
            Path filePath = new File(file).toPath();
            stringList = Files.readAllLines(filePath);
        } catch (IOException e) {
            System.err.println("Not valid file supplied");
        }

        return stringList;
    }
}
