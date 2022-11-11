package Dfa;

import java.util.*;

public class Minimization
{
    public static Dfa MinimizeDfa(Dfa dfa) throws Exception {
        dfa = Dfa.DeleteDeadStates(dfa);
        dfa = Dfa.DeleteNotReachableStates(dfa);

        var eqClasses = GetEqClasses(GetStartDivision(dfa), dfa);

        var fromStToEq = GetTableTransitionStToEqClasses(eqClasses);

        var states = new HashSet<Integer>();
        for (int i = 0; i < eqClasses.size(); i++)
            states.add(i);
        return GetDfaFromStToEqClasses(fromStToEq, dfa, dfa.alphabet, states);
    }

    private static List<Set<Integer>> GetStartDivision(Dfa dfa)
    {
        var eqClasses = new ArrayList<Set<Integer>>();
        var notFinal = new HashSet<Integer>(dfa.states);
        notFinal.removeAll(new HashSet<Integer>(dfa.finalStates));
        eqClasses.add(notFinal);
        eqClasses.add(dfa.finalStates);
        return eqClasses;
    }

    private static List<Set<Integer>> GetEqClasses(
            List<Set<Integer>> startDivision, Dfa dfa)
            throws Exception {
        while(true)
        {
            var tempEqClasses = new ArrayList<Set<Integer>>();
            for (var eq : startDivision)
            {
                var work = new HashMap<Map<Character, Integer>, Set<Integer>>();
                for (var fromSt : eq)
                {
                    var toEq = new HashMap<Character, Integer>();
                    for (var entry : dfa.transition.get(fromSt).entrySet())
                        toEq.put(entry.getKey(), FindNumberEqClass(entry.getValue(), startDivision));
                    if (!work.containsKey(toEq))
                        work.put(toEq, new HashSet<>());
                    work.get(toEq).add(fromSt);
                }
                tempEqClasses.addAll(work.values());
            }
            if (tempEqClasses.equals(startDivision))
                break;
            startDivision = new ArrayList<>(tempEqClasses);
        }
        return startDivision;
    }

    private static Map<Integer, Integer> GetTableTransitionStToEqClasses(List<Set<Integer>> eqClasses)
    {
        var table = new HashMap<Integer, Integer>();
        for (int i = 0; i < eqClasses.size(); i++)
            for (var st : eqClasses.get(i))
                table.put(st, i);
        return table;
    }

    private static Dfa GetDfaFromStToEqClasses(Map<Integer, Integer> fromStToEq, Dfa dfa,
                                               Set<Character> alphabet, Set<Integer> states) throws Exception {
        var newStartState = 0;
        var newFinalStates = new HashSet<Integer>();
        var newTransition = new HashMap<Integer, Map<Character, Integer>>();
        for (var st : dfa.states)
        {
            var eqSt = fromStToEq.get(st);
            if (!newTransition.containsKey(eqSt))
                newTransition.put(eqSt, new HashMap<>());
            for (var letter : dfa.transition.get(st).keySet())
                newTransition.get(eqSt).put(
                        letter, fromStToEq.get(dfa.transition.get(st).get(letter)));

            if (dfa.startState.equals(st))
                newStartState = st;
            if (dfa.finalStates.contains(st))
                newFinalStates.add(st);
        }

        return new Dfa(alphabet, states, newStartState,
                newFinalStates, newTransition);
    }

    private static Integer FindNumberEqClass(Integer st, List<Set<Integer>> eqClasses)
            throws Exception {
        for (var eqC : eqClasses)
            if (eqC.contains(st))
                return eqClasses.indexOf(eqC);
        throw new Exception();
    }
}
