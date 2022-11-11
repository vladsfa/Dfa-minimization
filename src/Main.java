import Dfa.*;

import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        try {
            Dfa dfa = Dfa.ReadDfa(GetScanner("Dfa.txt"));
            dfa = Dfa.Minimize(dfa);
            dfa.Print();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static Scanner GetScanner(String path) throws Exception {
        File file = new File(path);
        if (!file.exists())
            throw new Exception();
        if (!file.canRead())
            throw new Exception();
        return new Scanner(file);
    }
}