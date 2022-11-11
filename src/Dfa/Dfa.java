package Dfa;

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

    public static Dfa Minimize(Dfa dfa) throws Exception {
        return Minimization.MinimizeDfa(dfa);
    }

    public void Print()
    {
        System.out.println(this.alphabet.size());
        System.out.println(this.states.size());
        System.out.println(this.startState);
        System.out.print(this.finalStates.size() + " ");
        for (var st : this.finalStates)
            System.out.print(st + " ");
        System.out.println();
        for (var st : this.transition.keySet())
            for (var entry: this.transition.get(st).entrySet())
                System.out.println(st + " " + entry.getKey() + " " + entry.getValue());
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
                throw new Exception("The final state does not belong to the set of states");
            finalSt.add(finalState);
        }

        var alphabet = new HashSet<Character>();
        for (int i = 0; i < nAlphabet; i++)
            alphabet.add((char)(97 + i));
        var states = new HashSet<Integer>();
        for (int i = 0; i < nStates; i++)
            states.add(i);

        while (scanner.hasNextLine()) {
            String[] transition = scanner.nextLine().split(" ");
            Integer from = Integer.parseInt(transition[0]);
            Character letter = transition[1].charAt(0);
            int to = Integer.parseInt(transition[2]);
            if (from >= nStates || to >= nStates)
                throw new Exception("States from transition functions do not belong to the set of states");
            if ((int) letter > 96 + nAlphabet)
                throw new Exception("The letters from the transition functions do not belong to the set of letters");

            if (!transFunc.containsKey(from))
                transFunc.put(from, new HashMap<>());

            if (transFunc.get(from).containsKey(letter))
                throw new Exception("A non-deterministic automaton cannot be minimized");

            transFunc.get(from).put(letter, to);
        }
        return new Dfa(alphabet, states, startState, finalSt, transFunc);
    }

    Dfa(Set<Character> alph, Set<Integer> st, int startSt, Set<Integer> finalSt,
        Map<Integer, Map<Character, Integer>> transFunc) throws Exception {
        if (!st.contains(startSt))
            throw new Exception("The initial state does not belong to the set of state");

        alphabet = alph;
        states = st;
        transition = transFunc;
        this.startState = startSt;
        finalStates = finalSt;
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

    private static Dfa DeleteStates(Set<Integer> delSt, Set<Character> delAlp, Dfa dfa)
            throws Exception {
        var newTransition =  CleanTransition(delSt, dfa);

        var newFinalStates = new HashSet<Integer>();
        for (var st : newTransition.keySet())
            if (dfa.finalStates.contains(st))
                newFinalStates.add(st);

        var alphabet = new HashSet<>(dfa.alphabet);
        alphabet.removeAll(delAlp);
        var states = new HashSet<>(dfa.states);
        states.removeAll(delSt);

        return new Dfa(alphabet, states,
                delSt.contains(dfa.startState) ? -1 : 0,
                newFinalStates, newTransition);
    }

    private static HashMap<Integer, Map<Character, Integer>> CleanTransition(
            Set<Integer> states, Dfa dfa)
    {
        var newTransition = new HashMap<Integer, Map<Character, Integer>>();

        for (var state : states)
            dfa.transition.remove(state);
        for (var fromEntry : dfa.transition.entrySet())
        {
            for (var toEntry : dfa.transition.get(fromEntry.getKey()).entrySet())
                if (!states.contains(toEntry.getValue()))
                {
                    if (!newTransition.containsKey(fromEntry.getKey()))
                        newTransition.put(fromEntry.getKey(), new HashMap<Character, Integer>());
                    newTransition.get(fromEntry.getKey())
                            .put(toEntry.getKey(), toEntry.getValue());
                }
        }
        return newTransition;
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
