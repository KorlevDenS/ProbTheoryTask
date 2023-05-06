package org.korolev.dens;

import lombok.Getter;
import lombok.Setter;

import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class NumberSelection {

    private ArrayList<Double> selection = new ArrayList<>();
    private ArrayList<Double> frequenciesInSelection;
    private ArrayList<Double> probabilitiesInSelection;
    private ArrayList<Double> distinctSelection;
    private boolean dataInstalled;

    public NumberSelection() {
    }

    public boolean installData(String filePath) {
        if (dataInstalled) return true;
        try {
            FileReader fileReader = new FileReader(filePath);
            Scanner scanner = new Scanner(fileReader);
            while (scanner.hasNext()) {
                this.selection.add(scanner.nextDouble());
            }
            this.dataInstalled = true;
            return true;
        } catch (Exception e) {
            this.dataInstalled = false;
            return false;
        }
    }

    public boolean installData() {
        if (dataInstalled) return true;
        try {
            Scanner strScanner = new Scanner(System.in);
            Scanner scanner = new Scanner(strScanner.nextLine());
            while (scanner.hasNext()) {
                this.selection.add(scanner.nextDouble());
            }
            this.dataInstalled = true;
            return true;
        } catch (Exception e) {
            this.dataInstalled = false;
            return false;
        }
    }

    public ArrayList<Double> formVariationSeries() {
        Collections.sort(this.selection);
        return this.selection;
    }

    @Getter
    @Setter
    static final class MinMax {

        private final double min;
        private final double max;

        public MinMax(double min, double max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public String toString() {
            return "Экстремальные значения:\nMIN = "
                    + min + "  MAX = " + max;
        }

        public double getScope() {
            return max - min;
        }
    }

    public MinMax getMinMax() {
        return new MinMax(this.selection.get(0), this.selection.get(selection.size() - 1));
    }

    public double getSelectionScope() {
        return getMinMax().getScope();
    }

    public double calcMathExpectation() {
        frequenciesInSelection = new ArrayList<>();
        probabilitiesInSelection = new ArrayList<>();
        distinctSelection = new ArrayList<>();

        this.selection.stream().distinct().forEach( x -> {
            AtomicInteger freq = new AtomicInteger();
            this.distinctSelection.add(x);
            this.selection.forEach(num -> {
                if (Objects.equals(num, x)) {
                    freq.getAndIncrement();
                }
            });
            this.frequenciesInSelection.add((double) freq.intValue());
            this.probabilitiesInSelection.add(
                    BigDecimal.valueOf(freq.intValue())
                            .divide(BigDecimal.valueOf(this.selection.size()), 10, RoundingMode.HALF_UP)
                            .doubleValue()
            );
        });

        double mathExpectation = 0;
        for (int i = 0; i < this.distinctSelection.size(); i++)
            mathExpectation += this.distinctSelection.get(i) * this.probabilitiesInSelection.get(i);
        return mathExpectation;
    }

    public double calcDispersion() {
        double dispersion = 0;
        double mathExpectation = calcMathExpectation();
        for (int i = 0; i < distinctSelection.size(); i++)
            dispersion += Math.pow((distinctSelection.get(i) - mathExpectation), 2) * frequenciesInSelection.get(i);

        dispersion *= 1.0 / selection.size();
        return dispersion;
    }

    public double calcStandardDeviation() {
        return Math.sqrt(calcDispersion());
    }

    public ArrayList<String> calcEmpiricFunction() {
        double h = probabilitiesInSelection.get(0);
        ArrayList<String> empiricStrings = new ArrayList<>();
        empiricStrings.add("\t\t\t>>> Функция <<<\n");
        empiricStrings.add(String.format("\t\t\tx\t<=\t%.2f\t->\t%.2f\n", distinctSelection.get(0), 0.0));
        for (int i = 0; i < distinctSelection.size() - 1; i++) {
            empiricStrings.add(String.format("%.2f\t<\tx\t<=\t%.2f\t->\t%.2f\n",
                    distinctSelection.get(i), distinctSelection.get(i + 1), h));
            h += probabilitiesInSelection.get(i + 1);
        }
        empiricStrings.add(String.format("%.2f\t<\tx\t\t\t\t->\t%.2f\n",
                distinctSelection.get(distinctSelection.size() - 1), h));
        return empiricStrings;
    }

    public double getH() {
        return (selection.get(selection.size() - 1) - selection.get(0)) / (1 + ((Math.log(selection.size()) / Math.log(2))));
    }

    public int getM() {
        return (int) Math.ceil(1 + (Math.log(selection.size()) / Math.log(2)));
    }

    public void drawEmpiricFunction() {
        ChartDrawer drawChart = new ChartDrawer("x", "f(X)", "Эмпирическая функция");

        double h = probabilitiesInSelection.get(0);
        drawChart.addChart("x <= " + distinctSelection.get(0), distinctSelection.get(0) - 0.5,
                distinctSelection.get(0), 0);
        for (int i = 0; i < distinctSelection.size() - 1; i++) {
            drawChart.addChart(distinctSelection.get(i) + " < x <= " + distinctSelection.get(i + 1),
                    distinctSelection.get(i), distinctSelection.get(i + 1), h);
            h += probabilitiesInSelection.get(i + 1);
        }
        drawChart.addChart(distinctSelection.get(distinctSelection.size() - 1) + " < x",
                distinctSelection.get(distinctSelection.size() - 1),
                distinctSelection.get(distinctSelection.size() - 1) + 1, h);
        drawChart.plot("EmpiricFunc");
    }

    public void drawFrequencyPolygon() {
        ChartDrawer frequencyPolygon = new ChartDrawer("x", "p_i", "Полигон частот");

        double x_start = selection.get(0) - getH() / 2;
        for (int i = 0; i < getM(); i++) {
            int count = 0;
            for (double value : selection)
                if (value >= x_start && value < (x_start + getH()))
                    count++;

            frequencyPolygon.PolygonalChart(x_start + getH() / 2, (double) count / (double) selection.size());
            System.out.println("[ " + x_start + " : " + (x_start+getH()) + " ) -> " + (double) count / (double) selection.size());

            x_start += getH();
        }
        frequencyPolygon.plotPolygon("FrequencyPolygon");
    }

    public void drawHistogram(int size) {
        ChartDrawer Histogram = new ChartDrawer("x", "p_i / h", "Гистограмма частот");
        double x_start = selection.get(0) - getH() / 2;
        for (int i = 0; i < getM(); i++) {
            int s = 0;
            for (double value : selection)
                if (value >= x_start && value < (x_start + getH())) {
                    s++;
                }

            Histogram.addHistogram(x_start + " : " + x_start + getH(), x_start, x_start + getH(),
                    ((double) s / (double) size) / getH());
            x_start += getH();
        }
        Histogram.plot("Histogram");
    }

    public void printVectors() {
        for (Double aDouble : distinctSelection) {
            System.out.println(aDouble + " " + aDouble + " " + aDouble);
        }
    }
}

