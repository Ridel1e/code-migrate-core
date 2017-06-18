import Jama.Matrix;
import com.sun.org.apache.xpath.internal.operations.Mod;
import io.DataReader;
import sets.Set;

import java.util.*;
import java.util.function.Function;


public class Main {

    public static void main(String[] args) {
        // Считываем данные

        List<List<Double>> allX = DataReader.readArgumentsData();
        List<Double> allY = DataReader.readResultsData();


        // Делим данные на обучающую и тестовую выборку (60 на 40 прцоентов)

        List<List<Double>> learningX = new ArrayList<>();
        List<List<Double>> testX = new ArrayList<>();

        List<Double> learningY = new ArrayList<>();
        List<Double> testY = new ArrayList<>();

        int delimiterIndex = (int) (allY.size()  * 0.6);

        for (List<Double> list : allX) {
            learningX.add(list.subList(0, delimiterIndex));
            testX.add(list.subList(delimiterIndex + 1, allX.get(0).size()));
        }

        learningY.addAll(allY.subList(0, delimiterIndex));
        testY.addAll(allY.subList(delimiterIndex + 1, allY.size()));


        // Строим семейства моделей и находим параметры модели

        List<Model> allModel = new ArrayList<>();

        // Первое семейство моделей
        Function<Double, Double> firstFunc = aDouble -> aDouble;
        buildModelSystem(firstFunc, learningX, learningY, allModel);

        // Второе семейство моделей
        Function<Double, Double> secondFunc = aDouble -> aDouble * aDouble;
        buildModelSystem(secondFunc, learningX, learningY, allModel);

        // Третие семейство моделй
        Function<Double, Double> thirdFunc = aDouble -> aDouble;

        // Четвертое семейство моделей
        Function<Double, Double> fourthFunc = aDouble -> aDouble * aDouble * aDouble;
        buildModelSystem(fourthFunc, learningX, learningY, allModel);

        // Пятое семейство моделей
        Function<Double, Double> fifthFunc = Math::log;
        buildModelSystem(fifthFunc, learningX, learningY, allModel);

        // Берем 10 наилучших
        Collections.sort(allModel, new ModelComparator());
        allModel = allModel.subList(0, 10);


        //  Строим все возможные попарные комбинации, вычисляем параметры и показатели

        Map<Model, Set<Model>> modelSetMap = new HashMap<>();
        List<Set<Model>> pairModelsSubsets = new Set<>(allModel).generateSubsetWithElementCount(2);
        List<Model> newModels = new ArrayList<>();
        for (Set<Model> modelSet : pairModelsSubsets) {
            double[][] newInput = getInputForModelSet(modelSet);

            Model model = new Model(newInput, aDouble -> aDouble, learningY);
            newModels.add(model);
            model.calculateParameters();

            modelSetMap.put(model, modelSet);

            System.out.println(Arrays.toString(model.getParameters()));

            System.out.println("Коэфициент детерминации " + model.calculateR());
            System.out.println("Критерий Дарбина Уотсона " + model.calculateAutoCorrelation());
            System.out.println("Наличие автокорелияции " + model.isHasAutoСorrelation());
            System.out.println("Значение теста Тест Голдфелда — Куандта по первой переменной " + model.calculateGetero(2));
            System.out.println("Наличие гетероскедастичности по первой переменной " + model.isGetero(2));
            System.out.println("Наличие мультиколлениарности " + model.isHasMulticorrelation());

            System.out.println("");
            System.out.println("");
        }

        // Выбираем лучшую модель из новых
        Collections.sort(newModels, new ModelComparator());

        Model bestModel = newModels.get(0);
        Set<Model> modelSet = modelSetMap.get(bestModel);

        List<Double> firstInputToNewModel = new ArrayList<>();
        List<Double> secondINputToNewModel = new ArrayList<>();
        for (int i = 0; i < testX.get(0).size(); i++) {
            List<Double> doubles = new ArrayList<>();
            doubles.add(0, 1d);

            for (List<Double> list : testX) {
                doubles.add(list.get(i));
            }

            firstInputToNewModel.add(modelSet.getOriginal().get(0).predict(Utils.listToArray(doubles)));
            secondINputToNewModel.add(modelSet.getOriginal().get(1).predict(Utils.listToArray(doubles)));
        }

        for (int i = 0; i < firstInputToNewModel.size(); i ++) {
            List<Double> line = new ArrayList<>();
            line.add(1d);
            line.add(firstInputToNewModel.get(i));
            line.add(secondINputToNewModel.get(i));

            double predictedY = bestModel.predict(Utils.listToArray(line));

            double realY = testY.get(i);

            System.out.println("Предсказанное значение: " + predictedY + " Реальное значение " + realY);
        }

        // Проверяем новую модель на тестовой выборке
        System.out.println("Лучшая модель");
        System.out.println(Arrays.toString(bestModel.getParameters()));

    }

    private static double[][] getInputForModelSet(Set<Model> a) {
        List<List<Double>> resultList = new ArrayList<>();
        for (Model model : a.getOriginal()) {
            resultList.add(Utils.arrayToList(model.getPredictedY()));
        }

        resultList.add(0, Collections.nCopies(resultList.get(0).size(), 1d));

        return new Matrix(Utils.listOfListToArray(resultList)).transpose().getArray();
    }

    private static void buildModelSystem(Function<Double, Double> function,
                                  List<List<Double>> xVals,
                                  List<Double> uVals,
                                  List<Model> allModels) {
        List<Model> modelSystem = Model.generateModelSystem(xVals, function, uVals);
        calculateParams(modelSystem);
        allModels.addAll(modelSystem);
    }

    private static void calculateParams(List<Model> models) {
        for (Model model : models) {
            model.calculateParameters();

            model.calculateR();
        }
    }

    static class ModelComparator implements Comparator<Model> {

        @Override
        public int compare(Model o1, Model o2) {
            double distanseO1 = 1 - o1.calculateR();
            double distanseO2 = 1 - o2.calculateR();

            double delta = Math.abs(distanseO1 - distanseO2);

            if (delta > 0.1) {
                return Double.compare(distanseO1, distanseO2);
            } else {
                boolean o1AutoCor = o1.isHasAutoСorrelation();
                boolean o2AutoCor = o2.isHasAutoСorrelation();

                if (o1AutoCor == o2AutoCor) {

                    boolean isGetero1 = o1.isGetero();
                    boolean isGetero2 = o2.isGetero();

                    if (isGetero1 == isGetero2) {

                        boolean isMulti1 = o1.isHasMulticorrelation();
                        boolean isMulti2 = o2.isHasMulticorrelation();

                        return Boolean.compare(isMulti1, isMulti2);

                    } else {
                        return Boolean.compare(isGetero1, isGetero2);
                    }

                }
                return Boolean.compare(o1AutoCor, o2AutoCor);
            }
        }
    }

}
