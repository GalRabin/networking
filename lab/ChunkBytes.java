package lab;

import java.io.Serializable;

/**
 * Chunk represent the different parts of the download
 */
public class ChunkBytes implements Serializable {
    // Chunk shared variables
    private String mirror;
    private long startByte;
    private long endByte;
    private long currentByte = -1;

    public ChunkBytes(String mirror, long startByte, long endByte){
         this.startByte = startByte;
         this.endByte = endByte;
         this.currentByte += startByte;
         this.mirror = mirror;
    }

    // Getters for chunk variables
    public String getMirror() {
        return this.mirror;
    }
    public long getStartByte() { return this.startByte; }
    public long getEndByte() {
        return this.endByte;
    }
    public long getCurrentByte() {
        return this.currentByte;
    }
    // Getter for amount of bytes downloaded
    public long getBytesDownloaded(){
        return (this.currentByte + 1) - this.startByte;
    }
    // Getter for amount of bytes to be downloaded
    public long getRemainBytes(){
        return this.endByte - ((this.currentByte + 1));
    }
    // Change the pointer of the current byte being write
    public void addByte(long bytes) {
        this.currentByte += bytes;
    }
}
