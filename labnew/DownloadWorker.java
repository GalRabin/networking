package labnew;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.GZIPInputStream;


public class DownloadWorker implements Runnable {
    private ChunkBytes chunk;
    private RandomAccessFile downloadedFile;
    private DivideManager divider;
    private int workerID;

    public DownloadWorker(ChunkBytes chunk, RandomAccessFile downloadedFile, DivideManager divider, int workerID) {
        this.chunk = chunk;
        this.downloadedFile = downloadedFile;
        this.divider = divider;
        this.workerID = workerID;
    }

    public void run() {
        try {
            // Build range request message
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(chunk.get_mirror()).openConnection();
            String bytesRange = String.format("Bytes=%d-%d", this.chunk.get_startByte(), this.chunk.get_endByte());
            urlConnection.setRequestProperty("Range", bytesRange);
            urlConnection.connect();

            //open the input buffer stream
            InputStream inputStream = urlConnection.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            while (true) {
                byte[] temp = new byte[1024];
                int buffer_len = bufferedInputStream.read(temp);
                if (buffer_len == -1) {
                    break;
                }
                this.downloadedFile.seek(this.chunk.get_currentByte() +1);
                this.downloadedFile.write(temp);
                this.chunk.add_byte(buffer_len);
                this.divider.addDownloadedBytes(buffer_len);
            }

            System.out.println(String.format("[%d] Finished downloading", this.workerID));
        } catch(IOException e) {
            System.err.println(e);
        }
    }
}
