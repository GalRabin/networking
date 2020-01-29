package labnew;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandWorker implements Runnable {
    private ProcessBuilder processBuilder;
    private String command;
    private String output;
    private int delay;

    public CommandWorker(String command, int delay) {
        this.processBuilder = new ProcessBuilder();
        this.command = command;
        this.delay = delay;
        processBuilder.command("bash", "-c", this.command);
    }

    @Override
    public void run() {
        this.output = null;
        try {
            Thread.sleep(this.delay);
            Process p = processBuilder.start();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            // wait for termination
            int exitCode = p.waitFor();
            if (exitCode == 0){
                // read the output from the command
                this.output = stdInput.readLine();
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error occurred when trying to get established connections");
        }
    }

    public String getOutPut() {
        return this.output;
    }
}
