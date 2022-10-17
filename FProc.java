import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FProc {
    static final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public static void main(String[] args) throws IOException {
        var reader = new PipeReader("/home/meowster/pipes/f/in", queue);
        reader.start();

        while (true) {

        }
    }
}
