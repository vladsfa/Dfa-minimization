import java.util.*;

public class Dfa {
    public final Set<Character> alphabet;
    public final Set<Integer> states;
    public final Integer startState;
    public final Set<Integer> finalStates;
    public final Map<Integer, Map<Character, Integer>> transition;
    public static Dfa DeleteDeadStates(Dfa dfa) throws Exception {
        return DeleteStates(dfa, Dfa.GetDeletionStates(dfa, Dfa.GetNotDeadStates(dfa)));
    }
    public static Dfa DeleteNotReachableStates(Dfa dfa) throws Exception {
        return DeleteStates(dfa, Dfa.GetDeletionStates(dfa, Dfa.GetReachableStates(dfa)));
    }
    public static Dfa ReadDfa(Scanner scanner) throws Exception {
        int nAlphabet = Integer.parseInt(scanner.nextLine());
        int nStates = Integer.parseInt(scanner.nextLine());
        int startState = Integer.parseInt(scanner.nextLine());
        var finalSt = new HashSet<Integer>();
        var transFunc = new HashMap<Integer, Map<Character, Integer>>();

        String[] lineFinalStates = scanner.nextLine().split(" ");
        int nFinalStates = Integer.parseInt(lineFinalStates[0]);
        for (int i = 0; i < nFinalStates; i++) {
            int finalState = Integer.parseInt(lineFinalStates[i + 1]);
            if (finalState >= nStates)
                throw new Exception("Фінальний стан не належить множині станів");
            finalSt.add(finalState);
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

            if (!transFunc.containsKey(from))
                transFunc.put(from, new HashMap<>());

            if (transFunc.get(from).containsKey(letter))
                throw new Exception("Недетермінований автомат неможливо мінімізувати");

            transFunc.get(from).put(letter, to);
        }
        return new Dfa(nAlphabet, nStates, startState, finalSt, transFunc);
    }
    private Dfa(int nAlph, int nSt, int startSt, Set<Integer> finalSt,
                Map<Integer, Map<Character, Integer>> transFunc) throws Exception {
        if (startSt > nSt)
            throw new Exception("Початковий стан не належить множині станів");
        if (nAlph > 26)
            throw new Exception("Кількість букв в алфавіті більше допустимого: >26");

        alphabet = new HashSet<>();
        for (int i = 0; i < nAlph; i++)
            alphabet.add((char) (97 + i));

        states = new HashSet<>();
        for (int i = 0; i < nSt; i++)
            states.add(i);

        transition = transFunc;
        this.startState = startSt;
        finalStates = finalSt;
    }
    private static Dfa DeleteStates(Set<Integer> delSt, Set<Character> delAlp, Dfa dfa)
            throws Exception {
        var newNumberStates = GetNewNumber(delSt, new ArrayList<>(dfa.states));
        var newNameLetters = GetNewNames(delAlp, dfa.alphabet);
        var newTransition =  CleanTransition(
                delSt, newNumberStates, newNameLetters, dfa);

        var newFinalStates = new HashSet<Integer>();
        for (var entry : newNumberStates.entrySet())
            if (dfa.finalStates.contains(entry.getKey()))
                newFinalStates.add(entry.getValue());

        var nAlphabet = dfa.alphabet.size() - delAlp.size();
        var nStates = dfa.states.size() - delSt.size();

        return new Dfa(nAlphabet, nStates,
                delSt.contains(dfa.startState) ? -1 : 0,
                newFinalStates, newTransition);
    }

    private static HashMap<Integer, Map<Character, Integer>> CleanTransition(
            Set<Integer> states, Map<Integer, Integer> newNumber,
            Map<Character, Character> newName, Dfa dfa)
    {
        var newTransition = new HashMap<Integer, Map<Character, Integer>>();

        for (var state : states)
            dfa.transition.remove(state);
        for (var fromEntry : dfa.transition.entrySet())
        {
            for (var toEntry : dfa.transition.get(fromEntry.getKey()).entrySet())
                if (!states.contains(toEntry.getValue()))
                {
                    if (!newTransition.containsKey(newNumber.get(fromEntry.getKey())))
                        newTransition.put(newNumber.get(fromEntry.getKey()), new HashMap<Character, Integer>());
                    newTransition.get(newNumber.get(fromEntry.getKey()))
                            .put(newName.get(toEntry.getKey()), newNumber.get(toEntry.getValue()));
                }
        }
        return newTransition;
    }
    private static Map<Integer, Integer> GetNewNumber(
            Set<Integer> removeElem, List<Integer> removeFrom)
    {
        Collections.sort(removeFrom);
        var newNames = new HashMap<Integer, Integer>();
        int counter = 0;
        for (var elem : removeFrom)
        {
            if (removeElem.contains(elem))
            {
                counter += 1;
                continue;
            }
            newNames.put(elem, elem - counter);
        }
        return newNames;
    }
    private static Map<Character, Character> GetNewNames(Set<Character> removeLetter, Set<Character> alphabet)
    {
        var lettersNumber = new HashSet<Integer>();
        for (var elem : removeLetter)
            lettersNumber.add((int)elem);
        var alphabetNumber = new ArrayList<Integer>();
        for (var elem : alphabet)
            alphabetNumber.add((int)elem);

        var newNumber = GetNewNumber(lettersNumber, alphabetNumber);

        var convertToChar = new HashMap<Character, Character>();
        for (var entry : newNumber.entrySet())
            convertToChar.put((char)(int)entry.getKey(), (char)(int)entry.getValue());
        return convertToChar;
    }
    private static Dfa DeleteStates(Dfa dfa, StAndAlp delStAndAlp) throws Exception {
        return DeleteStates(delStAndAlp.St, delStAndAlp.Alp, dfa);
    }
    private static StAndAlp GetDeletionStates(Dfa dfa, StAndAlp stAndAlp)
    {
        Set<Integer> deadStates = new HashSet<>(dfa.states);
        deadStates.removeAll(stAndAlp.St);
        Set<Character> notReachAlp = new HashSet<>(dfa.alphabet);
        notReachAlp.removeAll(stAndAlp.Alp);
        return new StAndAlp(deadStates, notReachAlp);
    }
    private static StAndAlp GetNotDeadStates(Dfa dfa)
    {
        Set<Integer> notDeadStates = new HashSet<>(dfa.finalStates);
        Set<Character> reachableAlp = new HashSet<>();
        Set<Integer> workStates = dfa.finalStates;
        while (!workStates.isEmpty())
        {
            Set<Integer> temp = new HashSet<>();
            for (var workState : workStates)
                for (var entry : dfa.transition.entrySet())
                    for (var entryTo : entry.getValue().entrySet())
                        if (entryTo.getValue().equals(workState))
                        {
                            if (!notDeadStates.contains(entry.getKey()))
                                temp.add(entry.getKey());
                            notDeadStates.add(entry.getKey());
                            reachableAlp.add(entryTo.getKey());
                        }
            workStates = temp;
        }
        return new StAndAlp(notDeadStates, reachableAlp);
    }
    private static StAndAlp GetReachableStates(Dfa dfa)
    {
        Set<Integer> ReachableStates = new HashSet<>(Collections.singletonList(dfa.startState));
        Set<Character> reachableAlp = new HashSet<>();
        Set<Integer> WorkStates = new HashSet<>(Collections.singletonList(dfa.startState));
        while (!WorkStates.isEmpty())
        {
            Set<Integer> temp = new HashSet<>();
            for (Integer fromState : WorkStates) {
                if(!dfa.transition.containsKey(fromState))
                    continue;
                Set<Integer> toStates = new HashSet<>(dfa.transition.get(fromState).values());
                reachableAlp.addAll(dfa.transition.get(fromState).keySet());
                toStates.removeAll(ReachableStates);
                ReachableStates.addAll(toStates);
                temp.addAll(toStates);
            }
            WorkStates = new HashSet<>(temp);
        }
        return new StAndAlp(ReachableStates, reachableAlp);
    }
    public static Dfa Minimize(Dfa dfa) throws Exception {
        dfa = Dfa.DeleteDeadStates(dfa);
        dfa = Dfa.DeleteNotReachableStates(dfa);
        
        var eqClasses = new ArrayList<Set<Integer>>();
        var notFinal = new HashSet<Integer>(dfa.states);
        notFinal.removeAll(new HashSet<Integer>(dfa.finalStates));
        eqClasses.add(notFinal);
        eqClasses.add(dfa.finalStates);

        while(true)
        {
            var tempEqClasses = new ArrayList<Set<Integer>>();
            for (var eq : eqClasses)
            {
                var work = new HashMap<Map<Character, Integer>, Set<Integer>>();
                for (var fromSt : eq)
                {
                    var toEq = new HashMap<Character, Integer>();
                    for (var entry : dfa.transition.get(fromSt).entrySet())
                        toEq.put(entry.getKey(), FindNumberEqClass(entry.getValue(), eqClasses));
                    if (!work.containsKey(toEq))
                        work.put(toEq, new HashSet<>());
                    work.get(toEq).add(fromSt);
                }
                tempEqClasses.addAll(work.values());
            }
            if (tempEqClasses.equals(eqClasses))
                break;
            eqClasses = new ArrayList<>(tempEqClasses);
        }

        var newFinalStates = new HashSet<Integer>();
        var fromStToEq = new HashMap<Integer, Integer>();
        var newStartState = 0;
        for (int i = 0; i < eqClasses.size(); i++)
            for (var st : eqClasses.get(i))
            {
                fromStToEq.put(st, i);
                if (st.equals(dfa.startState))
                    newStartState = i;
                if (dfa.finalStates.contains(st))
                    newFinalStates.add(i);
            }

        var newTransition = new HashMap<Integer, Map<Character, Integer>>();
        for (var st : dfa.states)
        {
            var eqSt = fromStToEq.get(st);
            if (!newTransition.containsKey(eqSt))
                newTransition.put(eqSt, new HashMap<>());
            for (var letter : dfa.transition.get(st).keySet())
                newTransition.get(eqSt).put(
                        letter, fromStToEq.get(dfa.transition.get(st).get(letter)));
        }

        return new Dfa(dfa.alphabet.size(), eqClasses.size(),
                newStartState, newFinalStates, newTransition);
    }
    private static Integer FindNumberEqClass(Integer st, ArrayList<Set<Integer>> eqClasses) throws Exception {
        for (var eqC : eqClasses)
            if (eqC.contains(st))
                return eqClasses.indexOf(eqC);
        throw new Exception();
    }
    private static class StAndAlp {
        public final Set<Integer> St;
        public final Set<Character> Alp;
        public StAndAlp(Set<Integer> delSt, Set<Character> delAlp)
        {
            this.St = new HashSet<Integer>(delSt);
            this.Alp = new HashSet<Character>(delAlp);
        }
    }
}
