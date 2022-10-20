public class Updater {
    private boolean awaits = true;

    public synchronized void await() {
        while (awaits) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        awaits = true;
        notifyAll();
    }

    public synchronized void update() {
        while (!awaits) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        awaits = false;
        notifyAll();
    }
}
