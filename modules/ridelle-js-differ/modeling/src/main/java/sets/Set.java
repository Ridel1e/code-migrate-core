package sets;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Set<T> {

    private List<T> original;

    public Set(List<T> original) {
        this.original = original;
    }

    private int size() {
        return original.size();
    }

    public List<T> getOriginal() {
        return original;
    }

    public List<Set<T>> generateSubsets() {
        List<Set<T>> allSubsets = new ArrayList<>();

        List<Boolean> originalBooleanVector = new ArrayList<>();
        for (T d : original) {
            originalBooleanVector.add(false);
        }

        boolean isAllFound = false;

        while (!isAllFound) {
            long trueItemsCount = originalBooleanVector.stream().filter(item -> item).count();
            if (trueItemsCount == originalBooleanVector.size()) {
                isAllFound = true;
            }

            for (int i = originalBooleanVector.size() - 1; i >= 0; i--) {
                 if (!originalBooleanVector.get(i)) {
                     originalBooleanVector.set(i, true);

                     for (int k = i + 1; k < originalBooleanVector.size(); k++) {
                         if (originalBooleanVector.get(k)) {
                             originalBooleanVector.set(k, false);
                         }

                     }

                     allSubsets.add(new Set<>(generateSetFromBooleanVector(originalBooleanVector)));
                     break;
                }
            }
        }

        return allSubsets;
    }

    private List<T> generateSetFromBooleanVector(List<Boolean> booleanVector) {
        List<T> doubles = new ArrayList<>();

        for (int i = 0; i < booleanVector.size(); i++) {
            if (booleanVector.get(i)) {
                doubles.add(original.get(i));
            }
        }

        return doubles;
    }

    public List<Set<T>> generateSubsetWithElementCount(int n) {
        return generateSubsets().stream().filter(set -> {
            return set.size() == n;
        }).collect(Collectors.toList());
    }

    private List<T> generateListFromIndexes(List<Integer> indexes) {
        List<T> doubles = new ArrayList<>();
        for (Integer i : indexes) {
            doubles.add(original.get(i));
        }

        return doubles;
    }

    private static int factorial(int n) {
        int fact = 1; // this  will be the result
        for (int i = 1; i <= n; i++) {
            fact *= i;
        }
        return fact;
    }
}
