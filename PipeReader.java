import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class PipeReader extends Thread {
    String path;
    BlockingQueue<String> queue;

    PipeReader(String path, BlockingQueue<String> queue) {
        this.path = path;
        this.queue = queue;
    }

    @Override
    public void run() {
        Logger log = Logger.getLogger(PipeReader.class.getName());
        while (!this.isInterrupted()) {
            try {
                log.info("Opened");
                var reader = new BufferedReader(new FileReader((path)));
                String msg;
                while ((msg = reader.readLine()) != null) {
                    log.info(String.format("Message: %s", msg));
                    queue.put(msg);
                }
                log.info("OEF");
            } catch (IOException | InterruptedException e) {
                log.warning(String.format("Warning: %s", e.getMessage()));
            }
        }
    }
}
