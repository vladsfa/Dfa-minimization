import java.util.*;

public class Dfa {
    public Set<Character> alphabet;
    public Set<Integer> states;
    public Integer startState;
    public Set<Integer> finalStates;
    public Map<Integer, Map<Character, Integer>> transition;

    private Dfa(int nAlphabet, int nStates, int startState) throws Exception {
        if (startState > nStates)
            throw new Exception("Початковий стан не належить множині станів");
        if (nAlphabet > 26)
            throw new Exception("Кількість букв в алфавіті більше допустимого: >26");

        alphabet = new HashSet<>();
        for (int i = 0; i < nAlphabet; i++)
            alphabet.add((char) (97 + i));

        states = new HashSet<>();
        for (int i = 0; i < nStates; i++)
            states.add(i);

        transition = new HashMap<>();
        this.startState = startState;
        finalStates = new HashSet<>();
    }
    public static Dfa ReadDfa(Scanner scanner) throws Exception {
        int nAlphabet = Integer.parseInt(scanner.nextLine());
        int nStates = Integer.parseInt(scanner.nextLine());
        int startState = Integer.parseInt(scanner.nextLine());
        Dfa dfa = new Dfa(nAlphabet, nStates, startState);

        String[] lineFinalStates = scanner.nextLine().split(" ");
        int nFinalStates = Integer.parseInt(lineFinalStates[0]);
        for (int i = 0; i < nFinalStates; i++) {
            int finalState = Integer.parseInt(lineFinalStates[i + 1]);
            if (finalState >= nStates)
                throw new Exception("Фінальний стан не належить множині станів");
            dfa.finalStates.add(finalState);
        }

        while (scanner.hasNextLine()) {
            String[] transition = scanner.nextLine().split(" ");
            Integer from = Integer.parseInt(transition[0]);
            Character letter = transition[1].charAt(0);
            int to = Integer.parseInt(transition[2]);
            if (from >= nStates || to >= nStates)
                throw new Exception("Стани з функцій переходу не належать множині станів");
            if ((int) letter > 96 + nAlphabet)
                throw new Exception("Букви з функцій переходу не належать множині букв");

            if (!dfa.transition.containsKey(from))
                dfa.transition.put(from, new HashMap<>());

            if (dfa.transition.get(from).containsKey(letter))
                throw new Exception("Недетермінований автомат неможливо мінімізувати");

            dfa.transition.get(from).put(letter, to);
        }
        return dfa;
    }
    private void DeleteStates(Set<Integer> states)
    {
        if (states.size() == 0)
            return;
        var newNumberStates = GetNewNumberStates(new ArrayList<>(states));

        var newStates = new HashSet<Integer>();
        var newFinalStates = new HashSet<Integer>();
        for (var infoPair : newNumberStates.values())
        {
            newStates.add(infoPair.state);
            if (infoPair.isFinal)
                newFinalStates.add(infoPair.state);
        }
        var newTransition = CleanTransition(states, newNumberStates);

        if (states.contains(this.startState))
            this.startState = -1;
        this.states = newStates;
        this.finalStates = newFinalStates;
        this.transition = newTransition;
        this.alphabet = GetReachableLetters();
    }
    private Set<Character> GetNotReachableLetters(Set<Character> reachableLetters)
    {
        var notReachableLetters = new HashSet<Character>(this.alphabet);
        notReachableLetters.removeAll(reachableLetters);
        return notReachableLetters;
    }
    private Set<Character> GetReachableLetters()
    {
        var reachableLetters = new HashSet<Character>();
        for (var fromSt : transition.keySet())
            reachableLetters.addAll(transition.get(fromSt).keySet());
        return reachableLetters;
    }
    private HashMap<Integer, Map<Character, Integer>> CleanTransition(
            Set<Integer> states, Map<Integer, InfoStatePair> newNumber)
    {
        var newTransition = new HashMap<Integer, Map<Character, Integer>>();

        for (var state : states)
            this.transition.remove(state);
        for (var fromEntry : transition.entrySet())
        {
            for (var toEntry : transition.get(fromEntry.getKey()).entrySet())
                if (!states.contains(toEntry.getValue()))
                {
                    if (!newTransition.containsKey(newNumber.get(fromEntry.getKey()).state))
                        newTransition.put(newNumber.get(fromEntry.getKey()).state, new HashMap<Character, Integer>());
                    newTransition.get(newNumber.get(fromEntry.getKey()).state)
                            .put(toEntry.getKey(), newNumber.get(toEntry.getValue()).state);
                }
        }
        return newTransition;
    }
    private Map<Integer, InfoStatePair> GetNewNumberStates(List<Integer> states)
    {
        Collections.sort(states);
        var newNumber = new HashMap<Integer, InfoStatePair>();
        Integer counter = 0;
        for (var state : this.states)
        {
            if (counter < states.size() && states.get(counter).equals(state))
            {
                counter += 1;
                continue;
            }
            var infoStatePair = new InfoStatePair(
                    state - counter, this.finalStates.contains(state));
            newNumber.put(state, infoStatePair);
        }
        return newNumber;
    }
    public static Dfa DeleteDeadStates (Dfa dfa)
    {
        Set<Integer> deadStates = dfa.GetDeadStates();
        dfa.DeleteStates(deadStates);
        return dfa;
    }
    private Set<Integer> GetDeadStates()
    {
        Set<Integer> deadStates = new HashSet<>(states);
        deadStates.removeAll(GetNotDeadStates());
        return deadStates;
    }
    private Set<Integer> GetNotDeadStates()
    {
        Set<Integer> notDeadStates = new HashSet<>(finalStates);
        Set<Integer> workStates = new HashSet<>(finalStates);
        while (!workStates.isEmpty())
        {
            Set<Integer> temp = new HashSet<>();
            for (var workState : workStates)
                for (var entry : transition.entrySet())
                    if (entry.getValue().containsValue(workState))
                    {
                        if (!notDeadStates.contains(entry.getKey()))
                            temp.add(entry.getKey());
                        notDeadStates.add(entry.getKey());
                    }
            workStates = temp;
        }
        return notDeadStates;
    }

    public static void DeleteNotReachableStates(Dfa dfa)
    {
        Set<Integer> notReachableStates = dfa.GetNotReachableStates();
        dfa.DeleteStates(notReachableStates);
    }
    private Set<Integer> GetNotReachableStates()
    {
        Set<Integer> NotReachableStates = new HashSet<>(states);
        NotReachableStates.removeAll(GetReachableStates());
        return NotReachableStates;
    }
    private Set<Integer> GetReachableStates()
    {
        Set<Integer> ReachableStates = new HashSet<>(Collections.singletonList(startState));
        Set<Integer> WorkStates = new HashSet<>(Collections.singletonList(startState));
        while (!WorkStates.isEmpty())
        {
            Set<Integer> temp = new HashSet<>();
            for (Integer fromState : WorkStates) {
                if(!transition.containsKey(fromState))
                    continue;
                List<Integer> toStates = new ArrayList<>(transition.get(fromState).values());
                toStates.removeAll(ReachableStates);
                ReachableStates.addAll(toStates);
                temp.addAll(toStates);
            }
            WorkStates = new HashSet<>(temp);
        }
        return ReachableStates;
    }

    public static void Minimize(Dfa dfa)
    {
        Dfa.DeleteNotReachableStates(dfa);
        Dfa.DeleteDeadStates(dfa);

        var eqClasses = new ArrayList<List<Integer>>();
        var notFinal = new ArrayList<Integer>(dfa.states);
        notFinal.removeAll(new ArrayList<Integer>(dfa.finalStates));
        eqClasses.add(notFinal);
        eqClasses.add(new ArrayList<>(dfa.finalStates));

        boolean flag = true;
        while(flag)
        {
            var tempEqClasses = new HashSet<List<Integer>>();
            for (var eqCl : eqClasses)
            {
                var tempEqCl = new HashSet<Integer>();
                for (var st : eqCl)
                {
                    boolean isSame = true;
                    for (var letter : dfa.alphabet)
                    {
                        if (!eqCl.contains(dfa.transition.get(st).get(letter)))
                        {
                            tempEqClasses.add(Collections.singletonList(st));
                            isSame = false;
                            break;
                        }
                    }
                    if (isSame)
                        tempEqCl.add(st);
                }
                if (!tempEqCl.isEmpty())
                    tempEqClasses.add(new ArrayList<>(tempEqCl));
            }
            var temp = new ArrayList<List<Integer>>(tempEqClasses);
            if (temp.equals(eqClasses))
                flag = false;
            eqClasses = new ArrayList<>(temp);
        }

        var newFinalStates = new HashSet<Integer>();
        var newStates = new HashSet<Integer>();
        var fromStToEq = new HashMap<Integer, Integer>();
        for (var eq : eqClasses)
        {
            var eqN = eqClasses.indexOf(eq);
            for (var state : eq)
            {
                fromStToEq.put(state, eqN);
                if (state.equals(dfa.startState))
                    dfa.startState = eqN;
                if (dfa.finalStates.contains(state))
                    newFinalStates.add(eqN);
            }
            newStates.add(eqN);
        }

        var newTransition = new HashMap<Integer, Map<Character, Integer>>();
        for (var st : fromStToEq.keySet())
        {
            var eqSt = fromStToEq.get(st);
            if (!newTransition.containsKey(eqSt))
                newTransition.put(eqSt, new HashMap<>());
            for (var letter : dfa.transition.get(st).keySet())
                newTransition.get(eqSt).put(
                        letter, fromStToEq.get(dfa.transition.get(st).get(letter)));
        }

        dfa.states = newStates;
        dfa.finalStates = newFinalStates;
        dfa.transition = newTransition;
    }
    private class InfoStatePair {
        public Integer state;
        public Boolean isFinal;
        public InfoStatePair(Integer state, Boolean isFinal)
        {
            this.state = state;
            this.isFinal = isFinal;
        }
    }
}
