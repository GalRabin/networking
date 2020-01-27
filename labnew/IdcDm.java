package labnew;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class IdcDm {
        public static void main(String[] args) {
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
           divider = deSerializeObject(serialFileName, workers, mirrors);
        }

        // Serialized object if unable to recover or download didn't started yet
        if (divider == null){
            divider = serializeObject(serialFileName, fileSize, workers, mirrors);
        }




    }

    public static DivideManager deSerializeObject(String fileName, int workers, List<String> mirrors){
        DivideManager divider = null;
        try {
            FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            divider = (DivideManager) in.readObject();
            in.close();
            fileIn.close();
            divider.re_divide(workers, mirrors);
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException c) {
            System.out.println("Employee class not found");
            c.printStackTrace();
        }

        return divider;
    }

    public static DivideManager serializeObject(String fileName, long fileSize, int workers, List<String> mirrors){
        DivideManager divider = null;
        try {
            divider = new DivideManager(fileSize, workers, mirrors);
            divider.chunks.get(1).add_byte(40000);
            FileOutputStream fileOut = new FileOutputStream(fileName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(divider);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }

        return divider;
    }
}
