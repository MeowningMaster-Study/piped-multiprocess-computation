import os.lab1.compfuncs.advanced.Conjunction;

import java.util.logging.Logger;

public class Manager {
    public static void main(String[] args) {
        Logger log = Logger.getLogger(PipeReader.class.getName());
        try {
            var res = Conjunction.trialF(0);
            System.out.println(res);
        } catch (InterruptedException e) {
            System.out.println("Exception");
            throw new RuntimeException(e);
        }
    }
}
