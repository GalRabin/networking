package lab;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The object DivideManager suppose to manage the division of single file between mirrors and multiple HTTP connections
 */
public class DivideManager implements Serializable {
    // All active chunks
    public List<ChunkBytes> chunks;
    // Downloaded file size
    private long fileSize;
    // Reference for last reported percentage
    public int bytesPercentage = -1;
    // Reference for size of file downloaded before download paused
    private long resumedDownloadSize = 0;

    /**
     * Constructor for DivideManager
     * @param fileSize file size to download
     */
    public DivideManager(long fileSize){
        this.fileSize = fileSize;
    }

    public long getBytesDownloaded(){
        long bytesDownloaded = 0;
        for (ChunkBytes chunk : chunks){
            bytesDownloaded += chunk.getBytesDownloaded();
        }
        return bytesDownloaded + this.resumedDownloadSize;
    }

    /**
     * The method only used if download not recovered or it is new download.
     * @param workers Maximum number of concurrent HTTP connections.
     * @param mirrors Array of of mirrors to download the file from.
     */
    public void divide(int workers, List<String> mirrors){
        // Calculate how many worker will be given to each mirror
        int[] workersPerMirror = splitWorkerMirrors(mirrors.size(), workers);
        // Calculate range of bytes to each worker
        long[] rangeBytes = rangeBytes(0, this.fileSize, workers);
        // Build chunks to be execute
        chunks = buildChunksDivide(mirrors, workersPerMirror, rangeBytes);
    }

    /**
     * The method only used if download recovered from pause.
     * @param workers Maximum number of concurrent HTTP connections.
     * @param mirrors Array of of mirrors to download the file from.
     */
    public void reDivide(int workers, List<String> mirrors){
        // Get status of recovered download
        this.resumedDownloadSize = getBytesDownloaded();
        // Calculate how many worker will be given to each mirror
        int[] workersPerMirror = splitWorkerMirrors(mirrors.size(), workers);
        // Calculate how many bytes per worker is the best - didn't found a better optimized solution
        long bytesPerWorker = (long)Math.ceil((double)(this.fileSize - this.resumedDownloadSize)/ workers);
        // Variable mark if all workers in use already
        boolean splitRandomly = false;
        // Creating new chunks to be execute
        int curMirror = 0;
        List<ChunkBytes> newChunks = new ArrayList<ChunkBytes>();
        for (ChunkBytes chunk : chunks){
            // Split chunk between new chunks until old chunk is empty
            while (chunk.getRemainBytes() != 0){
                if (chunk.getRemainBytes() > bytesPerWorker){
                    // If chunk can't take all last chunk inside
                    String mirror = mirrors.get(curMirror);
                    long startByte = chunk.getCurrentByte() + 1;
                    long endByte = (bytesPerWorker - 1) + startByte;
                    ChunkBytes newChunk = new ChunkBytes(mirror, startByte, endByte);
                    newChunks.add(newChunk);
                    chunk.addByte(bytesPerWorker);
                } else {
                    // If chunk can take all from last chunk
                    String mirror = mirrors.get(curMirror);
                    long startByte = chunk.getCurrentByte() + 1;
                    long endByte = chunk.getEndByte();
                    ChunkBytes newChunk = new ChunkBytes(mirror, startByte, endByte);
                    newChunks.add(newChunk);
                    long byteUsed = endByte - startByte;
                    chunk.addByte(byteUsed);
                }

                if (!splitRandomly) {
                    // If workers able to handle all missing parts
                    if (workersPerMirror[curMirror] != 0) {
                        workersPerMirror[curMirror]--;
                        if (workersPerMirror[curMirror] == 0) {
                            curMirror++;
                            if (curMirror == mirrors.size()){
                                splitRandomly = true;
                                curMirror = 0;
                            }
                        }
                    }
                } else {
                    // If workers not able to handle all missing parts
                    if (curMirror < mirrors.size() - 1){
                        curMirror++;
                    } else {
                        curMirror = 0;
                    }
                }
            }
        }
        chunks = newChunks;
    }

    /**
     * Calculate percentage of downloaded file from total file size
     * @return percentage of downloaded file from total file size if not change form last call return -1
     */
    public int getPercentage(){
        long byteDownloaded = getBytesDownloaded();
        int newPercentage = (int)(((double)byteDownloaded / fileSize) * 100);
        if (newPercentage != bytesPercentage && newPercentage != 100){
            bytesPercentage = newPercentage;
            return bytesPercentage;
        }
        return -1;
    }

    /**
     * Build chunks by given data
     * @param mirrors list of mirrors
     * @param workersPerMirror list of worker per mirror - see details in method "splitWorkerMirrors"
     * @param rangeBytes list of ranges per workers - see details in method "rangeBytes"
     * @return chunks list after division
     */
    public List<ChunkBytes> buildChunksDivide(List<String> mirrors , int[] workersPerMirror, long[] rangeBytes){
        chunks = new ArrayList<ChunkBytes>();
        int curMirror = 0;
        for (int i = 0; i < rangeBytes.length - 1; i++) {
            if (i != rangeBytes.length - 2) {
                chunks.add(new ChunkBytes(mirrors.get(curMirror), rangeBytes[i], (rangeBytes[i + 1] - 1)));
            } else {
                chunks.add(new ChunkBytes(mirrors.get(curMirror), rangeBytes[i], rangeBytes[i + 1]));
            }
            workersPerMirror[curMirror]--;
            if (workersPerMirror[curMirror] == 0){
                curMirror++;
            }
        }
        return chunks;
    }

    /**
     * Calculate how many worker every mirror will get - so it will be equally split (in order to not overload specific mirror)
     * @param mirrors mirror amount
     * @param workers workers amount
     * @return split in a list, e.g if workersPerMirror[0]=2 it means that mirror number one has 2 workers
     */
    public int[] splitWorkerMirrors(int mirrors, int workers){
        int[] workersPerMirror = new int[mirrors];
        int curMirror = 0;
        for (int i = 0; i < workers; i++) {
            workersPerMirror[curMirror]++;
            curMirror++;
            if (curMirror > mirrors - 1){
                curMirror = 0;
            }
        }

        return workersPerMirror;
    }

    /**
     * Split ranges between workers
     * @param start starting point of byte
     * @param end ending point of byte
     * @param workers amount of worker available
     * @return range split, e.g. if rangeBytes[0]=0 and rangeBytes[0]=100, then the first worker will perform range 0-100
     */
    public long[] rangeBytes(long start, long end, int workers){
        long bytesPerWorker = (long)Math.ceil((double)(end - start) / (double)workers);
        long[] rangeBytes = new long[workers + 1];
        rangeBytes[0] = start;
        long currentByte = start;
        for (int i = 1; i < rangeBytes.length; i++) {
            if (i == rangeBytes.length - 1){
                rangeBytes[i] = end;
            } else {
                rangeBytes[i] = currentByte + bytesPerWorker;
                currentByte += bytesPerWorker;
            }
        }

        return rangeBytes;
    }
}
