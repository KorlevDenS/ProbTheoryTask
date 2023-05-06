package org.korolev.dens;

public class Main {
    public static void main(String[] args) {

        NumberSelection selection = new NumberSelection();
        if (selection.installData("src/main/resources/input1.txt")) {
            System.out.println("Полученная выборка:");
            System.out.println(selection.getSelection());
        } else {
            System.out.println("Couldn't read data");
            System.exit(1);
        }

        System.out.println("Вариационный ряд:");
        System.out.println(selection.formVariationSeries());
        System.out.println(selection.getMinMax());

        System.out.println("Размах ряда:");
        System.out.println(selection.getSelectionScope());

        System.out.println("Оценка математического ожидания:");
        System.out.println(selection.calcMathExpectation());

        System.out.println("Дисперсия:");
        System.out.println(selection.calcDispersion());

        System.out.println("Среднеквадратичное отклонение:");
        System.out.println(selection.calcStandardDeviation());

        selection.calcEmpiricFunction().forEach(System.out::print);


        selection.drawEmpiricFunction();

        selection.drawFrequencyPolygon();

        selection.drawHistogram(selection.getSelection().size());
    }
}