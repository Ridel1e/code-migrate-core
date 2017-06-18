package io;


import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataReader {

    private static final String ARGUMENTS_PATH = "arguments";
    private static final String RESULTS_PATH = "results";

    public static List<List<Double>> readArgumentsData() {
        List<File> argumentsFile = getFiles(ARGUMENTS_PATH);

        // Считываем данные агрументов (переменных)
        List<List<Double>> arguments = new ArrayList<>();
        for (File file : argumentsFile) {
            List<Double> list = new ArrayList<>();

            Iterator<Row> iterator = getIteratorForFile(file);

            for (int i = 0; i <= 5; i++) {
                Row row = iterator.next();

                Iterator<Cell> cellIterator = row.cellIterator();
                cellIterator.next();

                double lastValue = -1;

                while (cellIterator.hasNext()) {
                    Cell currentCell = cellIterator.next();
                    double currentValue = currentCell.getNumericCellValue();

                    if (lastValue != -1) {
                        list.add(currentValue - lastValue);
                    } else {
                        list.add(currentValue);
                    }

                    lastValue = currentValue;
                }
            }

            arguments.add(list);
        }

        return arguments;
    }

    public static List<Double> readResultsData() {
        List<Double> resultsData = new ArrayList<>();

        File resultsFile = getFiles(RESULTS_PATH).get(0);

        Iterator<Row> iterator = getIteratorForFile(resultsFile);

        for (int i = 0; i <= 5; i++) {
            Row row = iterator.next();

            Iterator<Cell> cellIterator = row.cellIterator();
            cellIterator.next();

            while (cellIterator.hasNext()) {
                Cell currentCell = cellIterator.next();
                resultsData.add(currentCell.getNumericCellValue());
            }
        }

        return resultsData;
    }

    private static Iterator<Row> getIteratorForFile(File file) {
        FileInputStream excelFile = null;
        try {
            excelFile = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Workbook workbook = null;
        try {
            workbook = new HSSFWorkbook(excelFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Sheet firstSheet = workbook.getSheetAt(0);
        Iterator<Row> iterator = firstSheet.iterator();
        iterator.next();
        iterator.next();
        iterator.next();
        iterator.next();

        return iterator;
    }

    private static List<File> getFiles(String projectPath) {
        List<File> codeFiles = new ArrayList<>();

        try(Stream<Path> paths = Files.walk(Paths.get(projectPath))) {
            paths.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    codeFiles.add(filePath.toFile());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return codeFiles
                .stream()
                .filter(file -> file.getName().contains("xls"))
                .collect(Collectors.toList());
    }
}
