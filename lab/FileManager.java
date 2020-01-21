package lab;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.FileHandler;

public class FileManager {
    double FILE_BYTE_DOWNLOADED = 0;
    double FILE_BYTE_SIZE;
    String FILE_NAME;
    int LAST_PERCENTAGE = 0;

    public FileManager(String fileName, long fileSize){
        FILE_NAME = fileName;
        FILE_BYTE_SIZE = fileSize;
    }

    public void log_percentage(){
        double percentage = (FILE_BYTE_DOWNLOADED / FILE_BYTE_SIZE) * 100;
        int percentage_int = (int) Math.floor(percentage);
        if (LAST_PERCENTAGE != percentage_int){
            System.out.println("Downloaded " + (int)percentage + "%");
            LAST_PERCENTAGE = (int)percentage;
        }
    }

    public byte[] readFromFile(String filePath, int position, int size) throws IOException {
        RandomAccessFile file = new RandomAccessFile(filePath, "r");
        file.seek(position);
        byte[] bytes = new byte[size];
        file.read(bytes);
        file.close();
        return bytes;
    }

    public void writeToFile(byte data, int position) throws IOException {
        RandomAccessFile file = new RandomAccessFile(FILE_NAME, "rw");
        file.seek(position);
        file.write(data);
        FILE_BYTE_DOWNLOADED++;
        file.close();
    }
}