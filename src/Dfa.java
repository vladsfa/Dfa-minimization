import com.sun.jdi.Value;

import java.util.*;

public class Dfa {
    public List<Character> alphabet;
    public List<Integer> states;
    public Integer startState;
    public List<Integer> finalStates;
    public Map<Integer, Map<Character, Integer>> transition;

    private Dfa(int nAlphabet, int nStates, int startState) throws Exception {
        if (startState > nStates)
            throw new Exception("Початковий стан не належить множині станів");
        if (nAlphabet > 26)
            throw new Exception("Кількість букв в алфавіті більше допустимого: >26");

        alphabet = new ArrayList<>();
        for (int i = 0; i < nAlphabet; i++)
            alphabet.add((char) (97 + i));

        states = new ArrayList<>();
        for (int i = 0; i < nStates; i++)
            states.add(i);

        transition = new HashMap<>();
        this.startState = startState;
        finalStates = new ArrayList<>();
    }
    public static Dfa ReadDfa(Scanner scanner) throws Exception {
        int nAlphabet = Integer.parseInt(scanner.nextLine());
        int nStates = Integer.parseInt(scanner.nextLine());
        int startState = Integer.parseInt(scanner.nextLine());
        Dfa dfa = new Dfa(nAlphabet, nStates, startState);

        String[] lineFinalStates = scanner.nextLine().split(" ");
        int nFinalStates = Integer.parseInt(lineFinalStates[0]);
        for (int i = 0; i < nFinalStates; i++)
        {
            Integer finalState = Integer.parseInt(lineFinalStates[i + 1]);
            if (finalState > nStates)
                throw new Exception("Фінальний стан не належить множині станів");
            dfa.finalStates.add(finalState);
        }

        while (scanner.hasNextLine())
        {
            String[] transition = scanner.nextLine().split(" ");
            Integer from = Integer.parseInt(transition[0]);
            Character letter = transition[1].charAt(0);
            Integer to = Integer.parseInt(transition[2]);
            if (from > nStates || to > nStates)
                throw new Exception("Стани з функцій переходу не належать множині станів");
            if ((int)letter > 96 + nAlphabet)
                throw new Exception("Букви з функцій переходу не належать множині букв");

            if (!dfa.transition.containsKey(from))
                dfa.transition.put(from, new HashMap<>());

            if (dfa.transition.get(from).containsKey(letter))
                throw new Exception("Недетермінований автомат неможливо мінімізувати");

            dfa.transition.get(from).put(letter, to);
        }
        return dfa;
    }
}
