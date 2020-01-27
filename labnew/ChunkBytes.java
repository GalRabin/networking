package labnew;

import java.io.Serializable;

public class ChunkBytes implements Serializable {
    public String _mirror;
    public long _startByte;
    public long _endByte;
    public long _currentByte = -1;

    public ChunkBytes(String mirror, long startByte, long endByte){
         this._startByte = startByte;
         this._endByte = endByte;
         this._currentByte += startByte;
         this._mirror = mirror;
    }

    public long get_endByte() {
        return this._endByte;
    }


    public long get_currentByte() {
        return this._currentByte;
    }

    public long get_remain_bytes(){
        return this._endByte - ((this._currentByte + 1));
    }
    public void add_byte(long bytes) {
        this._currentByte += bytes;
    }
}
