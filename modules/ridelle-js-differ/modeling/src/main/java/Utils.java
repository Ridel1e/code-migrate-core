import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    public static double[][] listOfListToArray(List<List<Double>> list) {
        double[][] array = new double[list.size()][list.get(0).size()];

        for (int i = 0; i < list.size(); i++) {
            List<Double> list1 = list.get(i);

            for (int k = 0; k < list1.size(); k++) {
                array[i][k] = list1.get(k);
            }
        }

        return array;
    }

    public static double[] listToArray(List<Double> doubles) {
        return doubles.stream().mapToDouble(d -> d).toArray();
    }

    public static List<Double> arrayToList(double[] a) {
        return Arrays.stream(a).boxed().collect(Collectors.toList());
    }

}
