package com.bearlycattable.bait.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.bearlycattable.bait.commons.BaitConstants;

public class CombinationUtils {

    public static synchronized List<List<Integer>> generateAllCombinationsToList(int from, int to, int totalNumItems) {
        List<List<List<Integer>>> container = new ArrayList<>();

        for (int i = from; i <= to; i++) {
            container.add(generate(totalNumItems, i));
        }

        return container.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    private static synchronized List<List<Integer>> generate(int n, int r) {
        List<List<Integer>> combinations = new ArrayList<>();
        comboHelper(combinations, new int[r], 1, n, 0);
        return combinations;
    }

    private static synchronized void comboHelper(List<List<Integer>> combinations, int[] data, int start, int end, int index) {
        if (index == data.length) {
            List<Integer> combination = Arrays.stream(data.clone()).boxed().collect(Collectors.toList());
            combinations.add(combination);
        } else if (start <= end) {
            data[index] = start;
            comboHelper(combinations, data, start + 1, end, index + 1);
            comboHelper(combinations, data, start + 1, end, index);
        }
    }

    public static synchronized List<List<Integer>> generateAllPermutationsToList() {
        List<List<Integer>> result = new ArrayList<>();
        addWordPermutations(BaitConstants.ALL_WORD_NUMBERS.stream().mapToInt(i -> i).toArray(), result);

        return result;
    }

    private static synchronized void addWordPermutations(int[] arr, List<List<Integer>> template) {
        permuteRecursive(arr, 0, template);
    }

    private static synchronized void permuteRecursive(int[] arr, int currentIndex, List<List<Integer>> allWordPermutations) {
        if (currentIndex >= arr.length - 1) { //If we are at the last element - nothing left to permute
            allWordPermutations.add(Arrays.stream(arr).boxed().collect(Collectors.toList()));
            return;//do not execute for loop
        }

        for (int i = currentIndex; i < arr.length; i++) { //For each index in the sub array arr[index...end]
            //1. Swap the elements at indices "currentIndex" and "i"
            int savedNum = arr[currentIndex]; //save value at current index
            arr[currentIndex] = arr[i]; //swap in 2 steps...
            arr[i] = savedNum;

            //2. Recurse on the sub array arr[index+1...end]
            permuteRecursive(arr, currentIndex + 1, allWordPermutations);

            //3. Swap the elements back so for loop can continue correctly
            savedNum = arr[currentIndex]; //save value at current index (which is different because we swapped them before)
            arr[currentIndex] = arr[i]; //swap back in 2 steps...
            arr[i] = savedNum;
        }
    }
}
