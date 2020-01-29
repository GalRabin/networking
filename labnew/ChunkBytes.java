package labnew;

import java.io.Serializable;

public class ChunkBytes implements Serializable {
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

    public String get_mirror() {
        return this.mirror;
    }
    public long get_startByte() { return this.startByte; }
    public long get_endByte() {
        return this.endByte;
    }
    public long get_currentByte() {
        return this.currentByte;
    }
    public long get_bytes_downloaded(){ return this.currentByte  - this.startByte + 1;}
    public long get_remain_bytes(){
        return this.endByte - ((this.currentByte + 1));
    }
    public void add_byte(long bytes) {
        this.currentByte += bytes;
    }
}
