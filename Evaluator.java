import os.lab1.compfuncs.advanced.Conjunction;

import java.io.*;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

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

//    /**
//     * Own conjunction implementations for tests
//     */
//    @SuppressWarnings("DuplicateBranchesInSwitch")
//    public static class Conjunction {
//        static Optional<Optional<Boolean>> value(boolean v) {
//            return Optional.of(Optional.of(v));
//        }
//
//        static Optional<Optional<Boolean>> softFail() {
//            return Optional.of(Optional.empty());
//        }
//
//        static Optional<Optional<Boolean>> hardFail() {
//            return Optional.of(Optional.empty());
//        }
//
//        static Optional<Optional<Boolean>> undefined() {
//            try {
//                Thread.currentThread().join(); // waits forever
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            return Optional.empty();
//        }
//
//        public static Optional<Optional<Boolean>> trialF(int x) throws InterruptedException {
//            return switch (x) {
//                case 0 -> value(true);
//                case 1 -> value(false);
//                case 2 -> value(true);
//                case 3 -> undefined();
//                case 4 -> ThreadLocalRandom.current().nextBoolean() ? value(false) : softFail();
//                case 5 -> {
//                    Thread.sleep(4000);
//                    yield value(true);
//                }
//                default -> hardFail();
//            };
//        }
//
//        public static Optional<Optional<Boolean>> trialG(int x) {
//            return switch (x) {
//                case 0 -> value(true);
//                case 1 -> undefined();
//                case 2 -> undefined();
//                case 3 -> undefined();
//                case 4 -> undefined();
//                case 5 -> value(true);
//                default -> hardFail();
//            };
//        }
//    }
}
