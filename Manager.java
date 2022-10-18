import sun.misc.Signal;

import java.io.*;
import java.util.Scanner;

public class Manager {
    static String cwd = System.getProperty("user.dir");
    static String home = System.getProperty("user.home");

    static int readX() {
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter x: ");
        int x = scan.nextInt();
        scan.close();
        return x;
    }

    enum TaskType {
        F("f"), G("g");

        public final String text;

        TaskType(String text) {
            this.text = text;
        }
    }

    static Process createProcess(TaskType type) throws IOException {
        var builder = new ProcessBuilder();
        builder.directory(new File(cwd));
        builder.inheritIO();
        builder.command(
                "java",
                "-classpath", String.format("%s:%s/lab1.jar", cwd, home), // import this project and Lab1 library
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

    static String fResult, gResult;

    static void setResult(TaskType type, String result) {
        switch (type) {
            case F -> fResult = result;
            case G -> gResult = result;
        }
    }

    static void startEvaluatorManager(TaskType type, int x) throws IOException {
        for (int i = 0; i < softRepeatBound; i += 1) {
            createProcess(type);
            writeX(type, x);
            var result = readResult(type);
            switch (result) {
                case "soft":
                    continue;
                case "hard":
                    break;
                default:
                    setResult(type, result);
                    return;
            }
        }
        setResult(type, "hard");
    }

    static boolean isBoolean(String s) {
        return s.equals("true") || s.equals("false");
    }

    /**
     * Prints result. Result is evaluated when possible
     */
    static void printResult() {
        String result = null;
        if (isBoolean(fResult) && isBoolean(gResult)) {
            result = String.valueOf(Boolean.parseBoolean(fResult) && Boolean.parseBoolean(gResult));
        } else if (fResult.equals("false") || gResult.equals("false")) {
            result = "false";
        }
        if (result != null) {
            System.out.printf("%s && %s = %s", fResult, gResult, result);
        } else {
            System.out.printf("%s && %s", fResult, gResult);
        }
    }

    public static void main(String[] args) throws IOException {
        var x = readX();

        Signal.handle(new Signal("INT"), signal -> {
            if (fResult == null) {
                fResult = "cancelled";
            }
            if (gResult == null) {
                gResult = "cancelled";
            }
            printResult();
            System.exit(0);
        });

        startEvaluatorManager(TaskType.F, x);
        startEvaluatorManager(TaskType.G, x);

        while (fResult == null || gResult == null) {}
        printResult();
    }
}
