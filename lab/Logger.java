package lab;

public class Logger implements Runnable {
    private FileManager _file;

    public Logger (FileManager file){
        _file = file;
    }

    public void run() {
        while (_file.LAST_PERCENTAGE != 100){
            try {
                Thread.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            _file.log_percentage();
        }
    }
}
