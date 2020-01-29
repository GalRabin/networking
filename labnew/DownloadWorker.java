package labnew;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;



public class DownloadWorker implements Runnable {
    private ChunkBytes chunk;
    private FileChannel downloadedFile;
    private DivideManager divider;
    private int workerID;

    public DownloadWorker(ChunkBytes chunk, FileChannel downloadedFile, DivideManager divider, int workerID) {
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
                byte[] buffer = new byte[1024];
                int buffer_len = bufferedInputStream.read(buffer);
                if (buffer_len == -1) {
                    break;
                }
                this.downloadedFile.write(ByteBuffer.wrap(buffer, 0, buffer_len), (int)(this.chunk.get_currentByte() + 1));
                this.chunk.add_byte(buffer_len);
            }
            bufferedInputStream.close();
            System.out.println(String.format("%s[%d] Finished downloading%s", ConsoleColors.GREEN_BOLD, this.workerID, ConsoleColors.RESET));
        } catch(IOException e) {
            System.err.println(e);
        }
    }
}
