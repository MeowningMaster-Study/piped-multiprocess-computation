import os.lab1.compfuncs.advanced.Conjunction;

import java.io.*;
import java.util.Optional;

public class Evaluator {
    public interface ThrowingFunction<I, O, E extends Throwable> {
        O apply(I input) throws E;
    }

    public static void main(String[] args) throws IOException {
        var type = args[0];
        var home = System.getProperty("user.home");

        var inPipePath = String.format("%s/pipes/%s/in", home, type);
        var outPipePath = String.format("%s/pipes/%s/out", home, type);

        var reader = new BufferedReader(new FileReader((inPipePath)));
        var x = Integer.parseInt(reader.readLine());
        reader.close();

        ThrowingFunction<Integer, Optional<Optional<Boolean>>, InterruptedException> function = switch (type) {
            case "f" -> Conjunction::trialF;
            case "g" -> Conjunction::trialG;
            default -> throw new RuntimeException(String.format("Incorrect type %s", type));
        };

        Optional<Optional<Boolean>> result;
        try {
            result = function.apply(x);
        } catch (InterruptedException e) {
            result = Optional.of(Optional.empty()); // hard fail
        }

        String output;
        if (result.isEmpty()) {
            output = "soft";
        } else {
            var inner = result.get();
            if (inner.isEmpty()) {
                output = "hard";
            } else {
                output = inner.get().toString();
            }
        }

        var writer = new BufferedWriter(new FileWriter(outPipePath));
        writer.write(String.format("%s\n", output));
        writer.close();
    }
}
