package mathematicalAnalysis.simplex;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;

/**
 * Алгоритм Нелдера-Мида (Nelder-Mead algorithm) - это метод оптимизации без производных, который ищет минимум (или максимум)
 * многомерной функции. Он использует метод симплекса для аппроксимации многомерной функции. Суть алгоритма заключается
 * в следующих шагах:
 * <p>
 * Создать начальный симплекс (n+1 точка в n-мерном пространстве), где n - это число переменных в функции, которую нужно оптимизировать.
 * <p>
 * Оценить значение функции в каждой точке симплекса.
 * <p>
 * Отсортировать точки симплекса в порядке убывания значений функции.
 * <p>
 * Выполнить следующие действия в цикле до тех пор, пока не будет достигнут критерий останова:
 * a. Определить центр тяжести всех точек, кроме наихудшей точки (точки с наибольшим значением функции).
 * b. Отразить наихудшую точку относительно центра тяжести.
 * c. Оценить значение функции в отраженной точке.
 * d. Если значение функции в отраженной точке меньше значения функции в наилучшей точке, то продолжить в направлении экспансии;
 * в противном случае, проверить, является ли значение функции в отраженной точке лучшим, чем значение функции во второй наихудшей точке.
 * Если это так, то продолжить в направлении отражения; в противном случае, выполнить сжатие.
 * e. Если значение функции в новой точке меньше значения функции в наилучшей точке, то заменить наихудшую точку новой точкой.
 * f. Отсортировать точки симплекса в порядке убывания значений функции.
 * <p>
 * Вернуть точку с наилучшим значением функции.
 */


/**
 * Многомерная функция - это функция, которая зависит от нескольких переменных. В отличие от обычной функции, которая
 * имеет только один аргумент и возвращает одно значение, многомерная функция принимает несколько аргументов и может возвращать множество значений.
 * <p>
 * Примером многомерной функции может быть функция, которая определяет расстояние между двумя точками в трехмерном пространстве.
 * Такая функция имеет три аргумента - координаты двух точек - и возвращает одно значение - расстояние между ними. Формально она может быть записана как:
 * <p>
 * f(x1, y1, z1, x2, y2, z2) = sqrt((x2-x1)^2 + (y2-y1)^2 + (z2-z1)^2)
 * <p>
 * где x1, y1, z1 - координаты первой точки, а x2, y2, z2 - координаты второй точки.
 */
public class NelderMeadAlgorithm {
    public static double[] optimize(double[] initialGuess, double tolerance,
                                    double scalingFactor, double reflectionFactor,
                                    double contractionFactor, double expansionFactor,
                                    int maxIterations, Function<double[], Double> objectiveFunction) {
        int n = initialGuess.length;
        double[][] simplex = new double[n + 1][n];
        simplex[0] = initialGuess;
        double[] fx = new double[n + 1];
        fx[0] = objectiveFunction.apply(initialGuess);

        for (int i = 0; i < n; i++) {
            double[] point = initialGuess.clone();
            point[i] += scalingFactor;
            simplex[i + 1] = point;
            fx[i + 1] = objectiveFunction.apply(point);
        }

        int iterations = 0;
        while (iterations < maxIterations) {
            int[] order = sortSimplex(fx);
            double[] centroid = new double[n];
            for (int i = 0; i < n; i++) {
                double sum = 0;
                for (int j = 0; j < n; j++) {
                    if (j != order[n]) {
                        sum += simplex[j][i];
                    }
                }
                centroid[i] = sum / n;
            }

            double[] reflected = reflect(simplex[order[n]], centroid, reflectionFactor);
            double reflectedFx = objectiveFunction.apply(reflected);

            if (reflectedFx < fx[order[0]]) {
                double[] expanded = expand(reflected, centroid, expansionFactor);
                double expandedFx = objectiveFunction.apply(expanded);
                if (expandedFx < reflectedFx) {
                    simplex[order[n]] = expanded;
                    fx[order[n]] = expandedFx;
                } else {
                    simplex[order[n]] = reflected;
                    fx[order[n]] = reflectedFx;
                }
            } else {
                boolean reflectedBetter = true;
                for (int i = 0; i < n + 1; i++) {
                    if (i != order[n] && reflectedFx >= fx[i]) {
                        reflectedBetter = false;
                        break;
                    }
                }

                if (reflectedBetter) {
                    simplex[order[n]] = reflected;
                    fx[order[n]] = reflectedFx;
                } else {
                    double[] contracted = contract(simplex[order[n]], centroid, contractionFactor);
                    double contractedFx = objectiveFunction.apply(contracted);
                    if (contractedFx < fx[order[n]]) {
                        simplex[order[n]] = contracted;
                        fx[order[n]] = contractedFx;
                    } else {
                        shrink(simplex, fx, order[0], scalingFactor);
                    }
                }
            }

            double maxError = 0;
            for (int i = 0; i < n + 1; i++) {
                double error = Math.abs(fx[i] - fx[order[0]]);
                if (error > maxError) {
                    maxError = error;
                }
            }

            if (maxError < tolerance) {
                break;
            }

            iterations++;
        }

        return simplex[0];
    }


    private static int[] sortSimplex(double[] fx) {
        int n = fx.length - 1;
        Integer[] order = new Integer[n + 1];
        for (int i = 0; i < n + 1; i++) {
            order[i] = i;
        }
        Arrays.sort(order, new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                return Double.compare(fx[o1], fx[o2]);
            }
        });
        int[] result = new int[n + 1];
        for (int i = 0; i < n + 1; i++) {
            result[i] = order[i];
        }
        return result;
    }

    private static double[] reflect(double[] point, double[] centroid, double reflectionFactor) {
        int n = point.length;
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = centroid[i] + reflectionFactor * (centroid[i] - point[i]);
        }
        return result;
    }

    private static double[] expand(double[] point, double[] centroid, double expansionFactor) {
        int n = point.length;
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = centroid[i] + expansionFactor * (point[i] - centroid[i]);
        }
        return result;
    }

    private static double[] contract(double[] point, double[] centroid, double contractionFactor) {
        int n = point.length;
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = centroid[i] + contractionFactor * (point[i] - centroid[i]);
        }
        return result;
    }

    private static void shrink(double[][] simplex, double[] fx, int start, double scalingFactor) {
        int n = simplex[0].length;
        for (int i = start + 1; i < n + 1; i++) {
            for (int j = 0; j < n; j++) {
                simplex[i][j] = simplex[start][j] + scalingFactor * (simplex[i][j] - simplex[start][j]);
            }
            fx[i] = 0;
        }
    }
}
