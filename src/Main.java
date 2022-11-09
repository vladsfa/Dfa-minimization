import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        try {
            Dfa dfa = Dfa.ReadDfa(GetScanner("test.txt"));
            Dfa.Minimize(dfa);
            Print(dfa);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void Print(Dfa dfa)
    {
        System.out.println(dfa.alphabet.size());
        System.out.println(dfa.states.size());
        System.out.println(dfa.startState);
        System.out.print(dfa.finalStates.size() + " ");
        for (var st : dfa.finalStates)
            System.out.print(st + " ");
        System.out.println();
        for (var st : dfa.transition.keySet())
            for (var entry: dfa.transition.get(st).entrySet())
                System.out.println(st + " " + entry.getKey() + " " + entry.getValue());

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