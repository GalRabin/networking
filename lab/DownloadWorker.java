package lab;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.GZIPInputStream;

import static lab.Helpers.getFileSizeFromURL;

public class DownloadWorker implements Runnable {
    private String _mirror;
    private long _start_byte;
    private long _end_byte;
    private FileManager _file_manager;

    /**
     * Request target url with Range method
     * @param mirror url to download from
     * @param start_byte starting byte in range
     * @param end_byte starting byte in range
     */
    public DownloadWorker(String mirror, long start_byte, long end_byte, FileManager file) {
        _mirror = mirror;
        _start_byte = start_byte;
        _end_byte = end_byte;
        _file_manager = file;
    }

    public void run() {
        try {
            // Build range request message
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(_mirror).openConnection();
            String bytesRange = String.format("Bytes=%d-%d", _start_byte, _end_byte);
            System.out.println(String.format("Start downloading range %s from:", bytesRange));
            System.out.println(_mirror);
            urlConnection.setRequestProperty("Range", bytesRange);
            urlConnection.connect();


            Reader reader = null;
            reader = new InputStreamReader(urlConnection.getInputStream());
            long current_index_in_file = _start_byte;
            while (true) {
                int current_byte = reader.read();
                if (current_byte == -1) {
                    break;
                }
                _file_manager.writeToFile((byte) current_byte, (int)current_index_in_file);
                current_index_in_file++;
            }

            System.out.println("Finished downloading");
        } catch(IOException e) {
            System.err.println(e);
        }
    }

    public FileManager get_file_manager() {
        return _file_manager;
    }

    public void set_file_manager(FileManager _file_manager) {
        this._file_manager = _file_manager;
    }
}
