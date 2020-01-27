package labnew;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DivideManager implements Serializable {
    public List<ChunkBytes> chunks;

    private long _fileSize;
    public long bytesDownloaded = 0;
    public int bytesPercentage = 0;

    public DivideManager(long fileSize, int workers, List<String> mirrors){
        _fileSize = fileSize;
        int[] workersPerMirror = split_worker_mirrors(mirrors.size(), workers);
        long[] rangeBytes = range_bytes(0, fileSize, workers);
        chunks = build_chunks(mirrors, workersPerMirror, rangeBytes);
    }

    public long getBytesDownloaded() {
        return bytesDownloaded;
    }

    public long get_fileSize() {
        return _fileSize;
    }

    public long get_remainToDownload() {
        return get_fileSize() - getBytesDownloaded();
    }


    public void re_divide(int workers, List<String> mirrors){
        // Split remain data by workers
        long[] rangeBytes = range_bytes(0,  get_remainToDownload(), workers);
        int[] workersPerMirror = split_worker_mirrors(mirrors.size(), workers);
        long bytesPerWorker = (long)Math.ceil((double)get_remainToDownload() / (double)workers);

        int curMirror = 0;
        List<ChunkBytes> newChunks = new ArrayList<ChunkBytes>();
        for (ChunkBytes chunk : chunks){
            while (chunk.get_remain_bytes() != 0){
                if (chunk.get_remain_bytes() > bytesPerWorker){
                    // If chunk can't take all last chunk inside
                    newChunks.add(new ChunkBytes(mirrors.get(curMirror),chunk.get_currentByte() + 1,
                            chunk.get_currentByte() + bytesPerWorker));
                    chunk.add_byte(bytesPerWorker);
                } else {
                    // If chunk can take all from last chunk
                    newChunks.add(new ChunkBytes(mirrors.get(curMirror),chunk.get_currentByte() + 1,
                            chunk.get_endByte()));
                    long byteUsed = chunk.get_endByte() - (chunk.get_currentByte() + 1);
                    chunk.add_byte(byteUsed);
                }

                // try to split until workers finish
                if (workersPerMirror[curMirror] != 0){
                    workersPerMirror[curMirror]--;
                    if (workersPerMirror[curMirror] == 0){
                        curMirror++;
                    }
                }
                // if workers finished just split one chunk to each of the mirrors so it will be equally distributed
                else {
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

    public List<ChunkBytes> build_chunks(List<String> mirrors , int[] workersPerMirror, long[] rangeBytes){
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

    public int[] split_worker_mirrors(int mirrors, int workers){
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

    public long[] range_bytes(long start, long end, int workers){
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
