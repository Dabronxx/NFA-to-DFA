import java.util.*;
import java.io.*;
import java.lang.*;
public class PA3
{
    protected static String regExString;

    private static String alphabet;

    private static boolean[] hasEpsilon;

    private static ArrayList<ArrayList<ArrayList<Integer>>> dfaFunction = new ArrayList<>();

    private static ArrayList<ArrayList<Integer>> epsilonStates;

    private static ArrayList<String> dfaStates = new ArrayList<>();

    private static ArrayList<String> allPossiblePaths = new ArrayList<>();

    private static ArrayList<ArrayList<Integer>> possiblePathLists = new ArrayList<>();

    public static void main(String[] args) throws FileNotFoundException
    {
        File inputFile;

        inputFile = new File(args[0]);

        if(!inputFile.exists())
        {
            System.out.println( "Cannot find " + args[0]);
            return;
        }

        Scanner fileReader = new Scanner(inputFile);

        alphabet = fileReader.nextLine();

        regExString = fileReader.nextLine();

        regExString = regExString.replaceAll("\\s+", "");

        ArrayList<String> expressionList;

        expressionList = expressionParser(regExString);

        PrintWriter fileWriter = new PrintWriter(args[1]);

        if(expressionList == null)
        {
            fileWriter.println("Invalid Expression");
            fileReader.close();
            fileWriter.close();
            return;
        }

        NFA nfa = convertToNFA(expressionList);

        if(nfa == null)
        {
            fileWriter.println("Invalid Expression");
            fileReader.close();
            fileWriter.close();
            return;
        }

        int originalTotalStates = nfa.totalStates();

        int totalStates = nfa.totalStates();

        hasEpsilon = new boolean[nfa.totalStates()];

        epsilonStates = new ArrayList<>(nfa.totalStates());

        for (int i = 0; i < nfa.totalStates(); i++)
        {
            epsilonStates.add(new ArrayList<>());

        }

        for (int i = 0; i < totalStates; i++ )
        {

            dfaFunction.add(new ArrayList<>());

            for (int k = 0; k < alphabet.length(); k++)
            {

                dfaFunction.get(i).add(new ArrayList<>());

            }

            hasEpsilon[i] = false;
            dfaStates.add(Integer.toString(i + 1));
        }
        int start, next, inputIndex;

        char input;

        for (int i = 0; i < totalStates; i++)
        {
            for (int j = 0; j <= alphabet.length(); j++)
            {
                for (int k = 0; k < nfa.getTransitionStates(i, j).size(); k++)
                {
                    start = i;
                    if(j != alphabet.length())
                    {
                        input = alphabet.charAt(j);
                    }
                    else
                    {
                        input = 'e';
                    }
                    next = nfa.getTransitionStates(i, j).get(k);

                    if(input == 'e')
                    {
                        hasEpsilon[start] = true;

                        epsilonStates.get(start).add(next + 1);
                    }
                    else
                    {
                        inputIndex = alphabet.indexOf(input);

                        if(!dfaFunction.get(start).get(inputIndex).contains( next + 1))
                        {
                            dfaFunction.get(start).get(inputIndex).add(next + 1);
                        }
                    }
                }
            }

        }
        int startStateValue = nfa.getStartIndex() + 1;

        String startState = Integer.toString(startStateValue);

        int startStateIndex = dfaStates.indexOf(startState);

        for (int i = 0; i < originalTotalStates; i++)
        {
            for (int j = 0; j < alphabet.length(); j++)
            {
                for (int k = 0; k < dfaFunction.get(i).get(j).size(); k++)
                {
                    int subState = dfaFunction.get(i).get(j).get(k);

                    for (Integer x : getNewEpsilonStates(subState))
                    {
                        if (!dfaFunction.get(i).get(j).contains(x))
                            dfaFunction.get(i).get(j).add(x);
                    }
                }
            }
        }
        ArrayList<Integer> startStateList = getNewEpsilonStates(startStateValue);

        startState = listToString(startStateList);

        if(!dfaStates.contains(startState))
        {
            dfaStates.add(startState);

            startStateIndex = dfaStates.indexOf(startState);

            dfaFunction.add(new ArrayList<>());



            for (int j = 0; j < alphabet.length(); j++)
            {
                dfaFunction.get(startStateIndex).add(new ArrayList<>());
            }
            for (Integer integer : startStateList)
            {
                int stateIndex = integer - 1;

                for (int m = 0; m < alphabet.length(); m++) {
                    for (Integer x : dfaFunction.get(stateIndex).get(m)) {
                        if (!dfaFunction.get(startStateIndex).get(m).contains(x))
                            dfaFunction.get(startStateIndex).get(m).add(x);
                    }
                }
            }
        }
        allPossiblePaths.add(startState);

        possiblePathLists.add(startStateList);

        createAllPaths(startStateIndex, alphabet.length());

        ArrayList<String> acceptStates = new ArrayList<>();

        for (int i = 0; i < allPossiblePaths.size(); i++)
        {
            for (int j = 0; j < nfa.getAcceptStates().size(); j++)
            {
                if(possiblePathLists.get(i).contains(nfa.getAcceptStates().get(j) + 1))
                {
                    acceptStates.add(allPossiblePaths.get(i));
                    break;
                }
            }
        }

        startStateIndex = allPossiblePaths.indexOf(startState);

        while(fileReader.hasNext())
        {
            String testInput = fileReader.nextLine();

            int currentState = startStateIndex;


            int indexOfState = dfaStates.indexOf(allPossiblePaths.get(currentState));

            for (int i = 0; i < testInput.length(); i++)
            {
                int charIndex = alphabet.indexOf(testInput.charAt(i));

                String transitionState = listToString(dfaFunction.get(indexOfState).get(charIndex));

                currentState = allPossiblePaths.indexOf(transitionState);

                indexOfState = dfaStates.indexOf(allPossiblePaths.get(currentState));
            }
            if (acceptStates.contains(allPossiblePaths.get(currentState)))
            {
                fileWriter.println("true");
            }
            else
            {
                fileWriter.println("false");
            }
        }

        fileReader.close();

        fileWriter.close();

    }
    private static ArrayList<String> expressionParser(String expression)
    {
        ArrayList<String> expressions = new ArrayList<>();
        StringBuilder section = new StringBuilder();
        for (int i = 0; i < expression.length(); i++)
        {
            section.append(expression.charAt(i));
            if(expression.charAt(i) == '(')
            {
                int endIndex = endParentheses(expression,i + 1);
                if(endIndex == -1)
                {
                    return null;
                }
                for (int j = i + 1; j <= endIndex; j++)
                {
                    section.append(expression.charAt(j));

                }
                i = endIndex;
            }
            if(i != expression.length() - 1 && expression.charAt(i + 1) == '*')
            {
                section.append(expression.charAt(++i));
            }
            expressions.add(section.toString());
            section = new StringBuilder();

        }
        return expressions;
    }
    private static int endParentheses(String expression, int startIndex)
    {
        int index = -1;
        for (int i = startIndex; i < expression.length(); i++)
        {
            if(expression.charAt(i) == ')')
            {
                index = i;
                break;
            }
            else if (expression.charAt(i) == '(')
            {
                i = endParentheses(expression,i + 1);
            }

        }
        return index;
    }

    private static NFA convertToNFA(ArrayList<String> states)
    {
        NFA nfa;
        if(states.get(0).charAt(0) == '(')
        {
            ArrayList<String> newStates = expressionParser(removeParentheses(states.get(0)));
            nfa = convertToNFA(newStates);
            if (nfa == null)
            {
                return null;

            }
            if (states.get(0).charAt(states.get(0).length() - 1) == '*')
            {
                nfa.starNFA();
            }
        }
        else if(states.get(0).charAt(0) == '|' || states.get(0).charAt(0) == ')')
        {
            return null;
        }
        else
        {
            nfa = new NFA(states.get(0), alphabet);
        }
        for (int i = 1; i < states.size(); i++)
        {
            if(states.get(i).charAt(0) == ')')
            {
                return null;
            }
            if (states.get(i).equals("|") )
            {
                if(i == states.size() -1 || states.get(i + 1).equals("|"))
                {
                    return null;
                }
                //System.out.println("Test");
                int nextOrIndex = findNextOr(states, i + 1);

                ArrayList<String> statesSubset = subList(i + 1, nextOrIndex, states);

                NFA nextNFA = convertToNFA(statesSubset);
                if(nextNFA == null)
                {
                    return null;
                }
                nfa.orNFA(nextNFA);
                i = nextOrIndex - 1;

            }
            else if(states.get(i).charAt(0) == '(')
            {
                ArrayList<String> newStates = expressionParser(removeParentheses(states.get(i)));
                NFA nextNFA = convertToNFA(newStates);
                if(nextNFA == null)
                {
                    return null;
                }
                if (states.get(i).charAt(states.get(i).length() - 1) == '*')
                {
                    //System.out.println("Test");
                    nextNFA.starNFA();
                }
                nfa.concatenateNFA(nextNFA);
            }
            else
            {
                NFA nextNfa = new NFA(states.get(i), alphabet);

                nfa.concatenateNFA(nextNfa);
            }

        }
        return nfa;
    }

    private static String removeParentheses(String state)
    {
        int startIndex = 1;
        int endIndex = state.length() - 1;
        if(state.charAt(state.length() -1) == '*')
        {
            endIndex--;

        }
        return state.substring(startIndex, endIndex);
    }

    private static int findNextOr(ArrayList<String> list, int startIndex)
    {
        for (int i = startIndex; i < list.size(); i++)
        {
            if(list.get(i).equals("|"))
            {
                return i;
            }

        }
        return list.size();
    }

    private static ArrayList<String> subList(int start, int end, ArrayList<String> list)
    {
        ArrayList<String> sublist = new ArrayList<>();

        for (int i = start; i < end; i++)
        {
            sublist.add(list.get(i));
        }
        return sublist;
    }

    public static void createAllPaths(int stateIndex, int alphabetLength)
    {
        if(!inPossiblePaths(dfaFunction.get(stateIndex)))
        {
            for (int i = 0; i < dfaFunction.get(stateIndex).size(); i++)
            {
                String stateString = listToString(dfaFunction.get(stateIndex).get(i));

                if(!dfaStates.contains(stateString))
                {
                    dfaStates.add(stateString);

                    dfaFunction.add(new ArrayList<>());

                    int newStateIndex = dfaStates.size() - 1;

                    for (int j = 0; j < alphabetLength; j++)
                    {
                        dfaFunction.get(newStateIndex).add(new ArrayList<>());
                    }

                    for (int j = 0; j < dfaFunction.get(stateIndex).get(i).size(); j++)
                    {
                        int nextSubState = dfaFunction.get(stateIndex).get(i).get(j);

                        int nextIndex = dfaStates.indexOf(Integer.toString(nextSubState));

                        for (int k = 0; k < alphabetLength; k++)
                        {
                            for (Integer x : dfaFunction.get(nextIndex).get(k))
                            {
                                if (!dfaFunction.get(newStateIndex).get(k).contains(x))
                                    dfaFunction.get(newStateIndex).get(k).add(x);
                            }
                        }
                    }
                }
                if(!allPossiblePaths.contains(stateString))
                {
                    allPossiblePaths.add(stateString);

                    possiblePathLists.add(dfaFunction.get(stateIndex).get(i));

                    int newStateIndex = dfaStates.indexOf(stateString);

                    createAllPaths(newStateIndex, alphabetLength);

                }
            }
        }

    }
    public static boolean inPossiblePaths(ArrayList<ArrayList<Integer>> states)
    {
        for (ArrayList<Integer> state1 : states)
        {
            String state = listToString(state1);

            if (!allPossiblePaths.contains(state)) {
                return false;
            }
        }
        return true;
    }

    public static ArrayList<Integer> getNewEpsilonStates(int state)
    {
        ArrayList<Integer> alreadyVisited = new ArrayList<>();

        alreadyVisited.add(state);

        ArrayList<Integer> extraStates = new ArrayList<>();

        extraStates.add(state);

        if(hasEpsilon[state - 1])
        {
            for (int i = 0; i < epsilonStates.get(state - 1).size(); i++)
            {
                for (Integer x : getNewEpsilonStates(epsilonStates.get(state - 1).get(i), alreadyVisited))
                {
                    if (!extraStates.contains(x))
                        extraStates.add(x);
                }
            }
        }
        return extraStates;
    }
    public static ArrayList<Integer> getNewEpsilonStates(int state, ArrayList<Integer> visited)
    {
        ArrayList<Integer> extraStates = new ArrayList<>();

        extraStates.add(state);

        if(hasEpsilon[state - 1] && !visited.contains(state))
        {
            for (int i = 0; i < epsilonStates.get(state - 1).size(); i++)
            {
                visited.add(state);

                for (Integer x : getNewEpsilonStates(epsilonStates.get(state - 1).get(i), visited))
                {
                    if (!extraStates.contains(x))
                        extraStates.add(x);
                }
            }
        }
        return extraStates;
    }
    public static String listToString(ArrayList<Integer> list)
    {
        StringBuilder listString = new StringBuilder();

        Collections.sort(list);

        for(Integer x : list)
        {
            listString.append(Integer.toString(x));
        }
        return listString.toString();
    }
    static class NFA
    {
        private int startState;

        private String alphabet;

        private ArrayList<Integer> acceptStates;

        private ArrayList<ArrayList<ArrayList<Integer>>> transitionFunction;

        private boolean starred;

        private int epsilonIndex;

        private int initialAcceptState;

        public NFA(String expression, String alphabet)
        {
            acceptStates = new ArrayList<>();
            startState = 0;
            transitionFunction = new ArrayList<>();
            this.alphabet = alphabet;
            epsilonIndex = alphabet.length();
            int charIndex;
            if(expression.charAt(0) != 'e')
            {
                charIndex = alphabet.indexOf(expression.charAt(0));
            }
            else
            {
                charIndex = epsilonIndex;
            }
            if(expression.length() > 1 && expression.charAt(1) == '*')
            {
                starred = true;
                initialAcceptState = 0;
                acceptStates.add(0);
            }
            else
            {
                starred = false;
                initialAcceptState = 1;
                acceptStates.add(1);
            }
            for (int i = 0; i < initialAcceptState + 1; i++)
            {
                transitionFunction.add(new ArrayList<>());
                for (int j = 0; j < alphabet.length() + 1; j++)
                {
                    transitionFunction.get(i).add(new ArrayList<>());
                }
            }
            transitionFunction.get(0).get(charIndex).add(initialAcceptState);
        }

        public void concatenateNFA(NFA nfa)
        {
            int originalSize = transitionFunction.size();
            for (int i = 0; i < nfa.totalStates(); i++)
            {
                transitionFunction.add(new ArrayList<>());
                for (int j = 0; j <= alphabet.length(); j++)
                {
                    transitionFunction.get(transitionFunction.size() - 1).add(new ArrayList<>());

                }
            }
            for (Integer acceptState : acceptStates)
            {
                transitionFunction.get(acceptState).get(epsilonIndex).add(nfa.getStartIndex() + originalSize);
            }
            for (int i = originalSize, j = 0; i < transitionFunction.size(); i++, j++)
            {
                for (int k = 0; k <= alphabet.length(); k++)
                {
                    for (int l = 0; l < nfa.getTransitionStates(j, k).size(); l++)
                    {
                        transitionFunction.get(i).get(k).add(nfa.getTransitionStates(j, k).get(l) + originalSize);
                    }
                }
            }
            acceptStates.clear();
            for (int i = 0; i < nfa.getAcceptStates().size(); i++)
            {
                acceptStates.add(nfa.getAcceptStates().get(i) + originalSize);
            }
            starred = false;
        }

        public void starNFA()
        {
            if(!starred)
            {
                for (Integer acceptState : acceptStates)
                {
                    transitionFunction.get(acceptState).get(epsilonIndex).add(startState);
                }
                acceptStates.clear();
                acceptStates.add(startState);
                starred = true;
            }
        }

        public void orNFA(NFA nfa)
        {
            int originalSize = transitionFunction.size();
            for (int i = 0; i < nfa.totalStates(); i++)
            {
                transitionFunction.add(new ArrayList<>());
                for (int j = 0; j <= alphabet.length(); j++)
                {
                    transitionFunction.get(transitionFunction.size() - 1).add(new ArrayList<>());

                }
            }
            for (int i = originalSize, j = 0; i < transitionFunction.size(); i++, j++)
            {
                for (int k = 0; k <= alphabet.length(); k++)
                {
                    for (int l = 0; l < nfa.getTransitionStates(j, k).size(); l++)
                    {
                        transitionFunction.get(i).get(k).add(nfa.getTransitionStates(j, k).get(l) + originalSize);
                    }
                }
            }
            for (int i = 0; i < nfa.getAcceptStates().size(); i++)
            {
                acceptStates.add(nfa.getAcceptStates().get(i) + originalSize);
            }

            transitionFunction.add(new ArrayList<>());

            int newStartIndex = transitionFunction.size() - 1;

            for (int i = 0; i <= alphabet.length(); i++)
            {
                transitionFunction.get(newStartIndex).add(new ArrayList<>());
            }

            transitionFunction.get(newStartIndex).get(epsilonIndex).add(startState);

            transitionFunction.get(newStartIndex).get(epsilonIndex).add(nfa.getStartIndex() + originalSize);

            startState = newStartIndex;

            starred = false;
        }

        public String getAlphabet()
        {
            return alphabet;
        }


        public int totalStates()
        {
            return transitionFunction.size();
        }

        public boolean isStarred()
        {
            return starred;
        }

        public int getStartIndex()
        {
            return startState;
        }

        public ArrayList<Integer> getAcceptStates()
        {
            return acceptStates;
        }

        public ArrayList<ArrayList<Integer>> getState(int index)
        {
            return transitionFunction.get(index);
        }

        public ArrayList<Integer> getTransitionStates(int index, int charIndex)
        {
            return transitionFunction.get(index).get(charIndex);
        }

    }
}
