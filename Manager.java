import java.io.*;
import java.util.concurrent.*;

public class Manager {
    static String cwd = System.getProperty("user.dir");
    static String home = System.getProperty("user.home");

    static class Results {
        String f, g;

        void set(TaskType type, String result) {
            switch (type) {
                case F -> f = result;
                case G -> g = result;
            }
            updater.update();
        }
    }

    static Results results = new Results();
    static Updater updater = new Updater();

    static int readX() throws IOException {
        System.out.print("Enter x: ");
        return Integer.parseInt(scanner.readLine());
    }

    enum TaskType {
        F("f"), G("g");

        public final String text;

        TaskType(String text) {
            this.text = text;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    static Process createProcess(TaskType type) throws IOException {
        var builder = new ProcessBuilder();
        builder.directory(new File(cwd));
        // builder.inheritIO();
        builder.command(
                "java",
                "-classpath", String.format("%s/lab1.jar", cwd), // import this project and Lab1 library
                "Evaluator.java", type.text // start evaluator of specific task type
        );
        return builder.start();
    }

    static void writeX(TaskType type, int x) throws IOException {
        var inPipePath = String.format("%s/pipes/%s/in", home, type.text);
        var writer = new BufferedWriter(new FileWriter(inPipePath));
        writer.write(String.format("%s\n", x));
        writer.close();
    }

    static String readResult(TaskType type) throws IOException {
        var outPipePath = String.format("%s/pipes/%s/out", home, type.text);
        var reader = new BufferedReader(new FileReader((outPipePath)));
        var result = reader.readLine();
        reader.close();
        return result;
    }

    final static int softRepeatBound = 3;

    static void startEvaluatorManager(TaskType type, int x) {
        Runnable manager = () -> {
            try {
                for (int i = 0; i < softRepeatBound; i += 1) {
                    createProcess(type);
                    writeX(type, x);
                    String result = readResult(type);
                    switch (result) {
                        case "soft":
                            continue;
                        case "hard":
                            break;
                        default:
                            results.set(type, result);
                            return;
                    }
                }
                results.set(type, "hard");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        var executor = Executors.newSingleThreadExecutor();
        executor.submit(manager);
    }

    static boolean isBoolean(String s) {
        return s.equals("true") || s.equals("false");
    }

    /**
     * Prints operands. Result is calculated when possible
     */
    static void reportAndExit() {
        if (activeDialog) {
            System.out.println("\nOverriden by system");
        }
        if (results.f == null) {
            results.f = "cancelled";
        }
        if (results.g == null) {
            results.g = "cancelled";
        }
        String result = null;
        if (isBoolean(results.f) && isBoolean(results.g)) {
            result = String.valueOf(Boolean.parseBoolean(results.f) && Boolean.parseBoolean(results.g));
        } else if (results.f.equals("false") || results.g.equals("false")) {
            result = "false";
        }
        if (result != null) {
            System.out.printf("%s && %s = %s", results.f, results.g, result);
        } else {
            System.out.printf("%s && %s", results.f, results.g);
        }
        System.exit(0);
    }

    static boolean activeDialog = false;
    static final BufferedReader scanner =  new BufferedReader(new InputStreamReader(System.in));

    static void dialogWaiter() {
        ExecutorService service = Executors.newSingleThreadExecutor();

        service.submit(() -> {
            for (;;) {
                ExecutorService subservice = Executors.newSingleThreadExecutor();
                Future<String> inputFuture = subservice.submit(waitNextLine);
                try {
                    var answer = inputFuture.get();
                    if (answer.equals("exit")) {
                        showCancelDialog();
                        break;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e.toString());
                }
            }
        });
    }

    static Callable<String> waitNextLine = () -> {
        synchronized (scanner) {
            while (!scanner.ready()) { Thread.sleep(50); };
            return scanner.readLine();
        }
    };

    static void showCancelDialog() {
        if (activeDialog) {
            return;
        }

        activeDialog = true;

        ExecutorService service = Executors.newSingleThreadExecutor();
        System.out.print("Please confirm that computation should be stopped y(es, stop)/n(ot yet) [n] ");
        Future<String> inputFuture = service.submit(waitNextLine);

        try {
            var answer = inputFuture.get(5, TimeUnit.SECONDS);
            if (answer.equals("y")) {
                reportAndExit();
            }
            System.out.println("Keep calculating");
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e.toString());
        } catch (TimeoutException e) {
            System.out.println("\nAction is not confirmed within 5 seconds");
        }

        activeDialog = false;

        dialogWaiter();
    }

    public static void main(String[] args) throws IOException {
        var x = readX();

        dialogWaiter();

        startEvaluatorManager(TaskType.F, x);
        startEvaluatorManager(TaskType.G, x);

        for (;;) {
            updater.await();
            if (
                // all evaluated
                (results.f != null && results.g != null) ||
                // result can be calculated by one operand
                (results.f != null && results.f.equals("false")) ||
                (results.g != null && results.g.equals("false"))
            ) {
                reportAndExit();
                break;
            }
        }
    }
}
