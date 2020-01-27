package labnew;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.GZIPInputStream;


public class Worker implements Runnable {
    private String _mirror;
    private long _start_byte;
    private long _end_byte;

    /**
     * Request target url with Range method
     * @param mirror url to download from
     * @param start_byte starting byte in range
     * @param end_byte starting byte in range
     */
    public Worker(String mirror, long start_byte, long end_byte) {
        _mirror = mirror;
        _start_byte = start_byte;
        _end_byte = end_byte;
    }

    public void run() {
        try {
            // Build range request message
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(_mirror).openConnection();
            String bytesRange = String.format("Bytes=%d-%d", _start_byte, _end_byte);
            System.out.println(String.format("Start downloading range %s from:", bytesRange));
            urlConnection.setRequestProperty("Range", bytesRange);
            urlConnection.connect();


            Reader reader = null;
//            reader = new InputStreamReader(urlConnection.getInputStream());
//            long current_index_in_file = _start_byte;
//            while (true) {
//                int current_byte = reader.read();
//                if (current_byte == -1) {
//                    break;
//                }
//                _file_manager.writeToFile((byte) current_byte, (int)current_index_in_file);
//                current_index_in_file++;
//            }

            System.out.println("Finished downloading");
        } catch(IOException e) {
            System.err.println(e);
        }
    }
}
