package lab;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Runnable thread which perform HTTP range request
 */
public class DownloadWorker implements Runnable {
    // DownloadWorker shared variables
    private ChunkBytes chunk;
    private FileChannel downloadedFile;
    private int workerID;
    // BUFFER to read from (will influence on the rhythm of writing the file and percentage shown to the user - if
    // too big partly percentage will be show, if too little the speed will be slowdown)
    private int BUFFER_SIZE = 1024;

    /**
     * Constructor to thread of DownloadWorker
     * @param chunk chunks to be execute
     * @param downloadedFile file channel that bytes will be write in.
     * @param workerID worker ID (simply thread ID)
     */
    public DownloadWorker(ChunkBytes chunk, FileChannel downloadedFile, int workerID) {
        this.chunk = chunk;
        this.downloadedFile = downloadedFile;
        this.workerID = workerID;
    }

    @Override
    public void run() {
        try {
            // Build range request message
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(chunk.getMirror()).openConnection();
            String bytesRange = String.format("Bytes=%d-%d", this.chunk.getStartByte(), this.chunk.getEndByte());
            urlConnection.setRequestProperty("Range", bytesRange);
            urlConnection.connect();

            //open the input buffer stream
            InputStream inputStream = urlConnection.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            // Write to file and read from buffer until no data available in buffer
            while (true) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int buffer_len = bufferedInputStream.read(buffer);
                if (buffer_len == -1) {
                    break;
                }
                this.downloadedFile.write(ByteBuffer.wrap(buffer, 0, buffer_len), (int)(this.chunk.getCurrentByte() + 1));
                this.chunk.addByte(buffer_len);
            }
            // Close buffer to loose resources
            bufferedInputStream.close();
            System.out.println(String.format("%s[%d] Finished downloading%s", ConsoleColors.GREEN_BOLD, this.workerID, ConsoleColors.RESET));
        } catch(IOException e) {

        }
    }
}
