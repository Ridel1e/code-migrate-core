import Jama.Matrix;
import com.sun.org.apache.xpath.internal.operations.Mod;
import sets.Set;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Model {

    // Необходимо для теста Дарбина-Уотсона
    // Взято из таблицы http://crow.academy.ru/econometrics/materials_/Tables_/DW-distr.htm
    private static final double DL = 1.38;
    private static final double DU = 1.67;

    // Необходимо для теста Гольдфельда-Квандта
    // Критерий Фишера
    private static final double FISHER_VALUE = 2.98d;

    // Необходимо для теста Метод Феррара-Глобера
    private static final double HI_KVADRAT = 11.3;

    // Генерируем все возможные множества
    static List<Model> generateModelSystem(List<List<Double>> inputs,
                                           Function<Double, Double> inputProcessFunction,
                                           List<Double> answers) {
        List<Model> modelSystem = new ArrayList<>();

        List<List<Set<Double>>> allWays = new ArrayList<>();
        int elementsCount = inputs.get(0).size();

        for (int i = 0; i < elementsCount; i++) {
            List<Double> doubles = new ArrayList<>();

            for (List<Double> list : inputs) {
                doubles.add(list.get(i));
            }

            Set<Double> set = new Set<>(doubles);
            allWays.add(set.generateSubsets());
        }

        List<Input> allInputs = new ArrayList<>();
        int allInputsCount = allWays.get(0).size();

        for (int i = 0; i < allInputsCount; i++) {
            List<List<Double>> list = new ArrayList<>();

            for (List<Set<Double>> listOfSet : allWays) {
                list.add(listOfSet.get(i).getOriginal());
            }

            allInputs.add(new Input(list));
        }

        for (Input input : allInputs) {
            for (List<Double> list : input.value) {
                list.add(0, 1d);
            }
        }

        for (Input input : allInputs) {
            Model model = new Model(
                    input.value,
                    inputProcessFunction,
                    answers
            );

            modelSystem.add(model);
        }

        return modelSystem;
    }

    private Matrix inputs;

    private double[] parameters;

    double R;       // Коэфииент детерминации

    private double D;       // Критерий Дарбина Уотсона

    private final Function<Double, Double> inputProcessFunction;

    private Matrix answers;

    private Model(List<List<Double>> inputs,
                  Function<Double, Double> inputProcessFunction,
                  List<Double> answers) {
        this.inputs = new Matrix(Utils.listOfListToArray(inputs));
        this.inputProcessFunction = inputProcessFunction;
        double[] answersArray = answers.stream().mapToDouble(d -> d).toArray();

        this.answers = new Matrix(answersArray, answers.size());
    }

    public Model(double[][] inputs,
                 Function<Double, Double> inputProcessFunction,
                 List<Double> answers) {
        this.inputs = new Matrix(inputs);
        this.inputProcessFunction = inputProcessFunction;
        double[] answersArray = answers.stream().mapToDouble(d -> d).toArray();

        this.answers = new Matrix(answersArray, answers.size());
    }

    public Model(List<List<Double>> inputs,
                 Function<Double, Double> inputProcessFunction,
                 List<Double> answers,
                 double[] parameters) {
        this.inputs = new Matrix(Utils.listOfListToArray(inputs));
        this.inputProcessFunction = inputProcessFunction;
        double[] answersArray = answers.stream().mapToDouble(d -> d).toArray();

        this.answers = new Matrix(answersArray, answers.size());

        this.parameters = parameters;
    }

    public void setAnswers(List<Double> answers) {
        double[] answersArray = answers.stream().mapToDouble(d -> d).toArray();

        this.answers = new Matrix(answersArray, answers.size());
    }

    public void setAnswers(Matrix answers) {
        this.answers = answers;
    }

    public void setInputs(List<List<Double>> inputs) {
        this.inputs = new Matrix(Utils.listOfListToArray(inputs));
    }

    public void setInputs(double[][] a) {
        this.inputs = new Matrix(a);
    }

    // Нахождение параметров модели
    void calculateParameters() {
        applyFunction();

        Matrix result = inputs.transpose().times(inputs).inverse();
        result = result.times(inputs.transpose());
        result = result.times(answers);

        parameters = result.getColumnPackedCopy();
    }

    // вычисление коэффициент детерминации
    double calculateR() {
        double[][] xArray = inputs.getArray();
        double[] yArray = answers.getColumnPackedCopy();

        double S1 = 0;

        for (int i = 0; i < xArray.length; i++) {
            double realY = yArray[i];
            double predictedY = predict(xArray[i]);

            double temp = realY - predictedY;
            temp = temp * temp;

            S1 += temp;
        }

        double tempY = 0;
        for (int i = 0; i < yArray.length; i++) {
            tempY += yArray[i];
        }
        tempY = tempY / yArray.length;

        double S2 = 0;
        for (int i = 0; i < xArray.length; i++) {
            double realY = yArray[i];
            realY = realY - tempY;

            S2 += realY * realY;
        }

        R = 1 - S1/S2;

        return R;
    }

    // Расчет критерия Дарбина-Уотсона
    double calculateAutoCorrelation() {
        double[][] xArray = inputs.getArray();

        double sequenceSum =0;
        for (int i = 1; i < xArray.length; i++) {
            double temp = getRowE(i) - getRowE(i - 1);
            temp = temp * temp;

            sequenceSum += temp;
        }

        D = sequenceSum / getSumE();

        return D;
    }

    // Проверка на наличие автокорреляции остатков
    boolean isHasAutoСorrelation() {
        if (D > DL && D > DU && D < 4 - DU) {
            return false;
        } else {
            return true;
        }
    }

    // тест Гольдфельда-Квандта
    double calculateGetero(int variableIndex) {
        double[][] xArray = inputs.getArray();
        double[] yArray = answers.getColumnPackedCopy();

        List<Line> allLines = new ArrayList<>();
        for (int i = 0; i < xArray.length; i++) {
            List<Double> list = new ArrayList<>();
            list.addAll(Arrays.stream(xArray[i]).boxed().collect(Collectors.toList()));
            list.add(yArray[i]);

            allLines.add(new Line(list));
        }

        // Сортируем по переменной
        Collections.sort(allLines, (o1, o2) -> o1.values.get(variableIndex).compareTo(o2.values.get(variableIndex)));

        int sublistsSize = allLines.size() / 3;

        List<Line> beginList = allLines.subList(0, sublistsSize);
        List<Line> endList = allLines.subList(allLines.size() - sublistsSize, allLines.size());

        double beginListResult = 0;
        for (Line line : beginList) {
            beginListResult += getSumE(line);
        }

        double endListResult = 0;
        for (Line line : endList) {
            endListResult += getSumE(line);
        }

        return beginListResult / endListResult;
    }

    // Проверяем на гетероскедастичность по всем переменным
    boolean isGetero() {
        boolean result = true;
        for (int i = 1; i < parameters.length; i++) {
            boolean val = calculateGetero(i) > FISHER_VALUE;

            result = result && val;
        }

        return result;
    }

    // Проверяем на гетероскедастичность по заданной переменной (индекс переменной)
    boolean isGetero(int index) {
        double value = calculateGetero(index);

        return  value > FISHER_VALUE;
    }

    double[] getPredictedY() {
        double[][] xArray = inputs.getArray();

        double[] result = new double[inputs.getArray().length];
        for (int i = 0; i < inputs.getArray().length; i++) {
            result[i] = predict(xArray[i]);
        }

        return result;
    }

    boolean isHasMulticorrelation() {
        double[][] source = inputs.getArray();

        double[][] temp = inputs.transpose().getArray();

        List<Column> columns = new ArrayList<>();
        for (int i = 0; i < temp.length; i++) {
            Column column = new Column(temp[i]);
            column.normalize();

            columns.add(column);
        }
        columns.remove(0);

        double[][] newMatrix = new double[source.length][source[0].length];


        for (int i = 0; i < source.length; i++) {

            List<Double> doubles = new ArrayList<>();
            for (Column column : columns) {
                doubles.add(column.values.get(i));
            }

            newMatrix[i] = Utils.listToArray(doubles);
        }


        Matrix matrix = new Matrix(newMatrix);

        Matrix r = matrix.inverse().times(matrix);

        double h2 = - (source.length - 1 * 1/6 * (2 * 3 + 5)) * Math.log(r.det());

        return h2 > HI_KVADRAT;
    }

    private double getRowE(int i) {
        double[][] xArray = inputs.getArray();
        double[] yArray = answers.getColumnPackedCopy();

        double realY = yArray[i];
        double predictedY = predict(xArray[i]);

        double temp = realY - predictedY;

        return temp;
    }

    private double getSumE() {
        double[][] xArray = inputs.getArray();
        double[] yArray = answers.getColumnPackedCopy();

        double sumE = 0;
        for (int i = 0; i < xArray.length; i++) {
            double realY = yArray[i];
            double predictedY = predict(xArray[i]);

            double temp = realY - predictedY;
            temp = temp * temp;

            sumE += temp;
        }

        return sumE;
    }

    private double getSumE(Line line) {
        List<Double> doublesX = line.values.subList(0, line.values.size() - 1);
        double realY = line.values.get(line.values.size() - 1);

        double[] xArray = doublesX.stream().mapToDouble(i -> i).toArray();

        double predictedY = predict(xArray);

        double result = realY - predictedY;
        result = result * result;

        return result;
    }

    // Предсказание значения
    double predict(double[] factors) {
        if (parameters == null) {
            return -1d;
        }

        double result = 0;
        for (int i = 0; i < parameters.length; i++) {
            result += parameters[i] * inputProcessFunction.apply(factors[i]);
        }

        return result;
    }

    double[] getParameters() {
        return parameters;
    }

    private void applyFunction() {
        if (inputProcessFunction == null) {
            return;
        }

        double[][] array = inputs.getArray();
        for (int i = 1; i < array.length; i++) {
            for (int k = 1; k < array[0].length; k++) {
                array[i][k] = inputProcessFunction.apply(array[i][k]);
            }
        }
    }

    // Вложенные классы используемые внутри модели для упрощения расчетов

    private static class Input {

        List<List<Double>> value;

        Input(List<List<Double>> value) {
            this.value = value;
        }
    }

    private static class Line {

        List<Double> values;

        public Line(List<Double> values) {
            this.values = values;
        }
    }

    private static class Column {

        List<Double> values;

        public Column(double[] values) {
            this.values = Arrays.stream(values).boxed().collect(Collectors.toList());
        }

        double getAverage() {
            double result = 0;
            for (Double d : values) {
                result += d;
            }
            return result;
        }

        double getDispersion() {
            double result = 0;

            double average = getAverage();

            for (Double d : values) {
                double temp = d - average;

                temp = temp * temp;

                result += temp;
            }

            result = result / values.size();
            return result;
        }

        double findX(int index) {
            return values.get(index) - getAverage() / (Math.sqrt(values.size() * getDispersion()));
        }

        void normalize() {
            for (int i = 0; i < values.size(); i++) {
                values.set(i, findX(i));
            }
        }
    }

}
