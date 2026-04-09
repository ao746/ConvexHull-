package ConvexHull.algoritmos;

import java.util.ArrayList;
import java.util.HashSet;

public class QuickHull {

    public static ArrayList<Point> calcularHull(ArrayList<Point> puntos) {

        if (puntos == null) return new ArrayList<>();

        HashSet<Point> aux = new HashSet<>(puntos);
        ArrayList<Point> result = new ArrayList<>(aux);

        if (result.size() <= 2) return result;

        Point max = result.get(0);
        Point min = result.get(0);

        for (Point punto : result) {
            if (punto.compareTo(min) < 0) min = punto;
            if (punto.compareTo(max) > 0) max = punto;
        }

        ArrayList<Point> cotaSuperior = new ArrayList<>();
        ArrayList<Point> cotaInferior = new ArrayList<>();

        for (Point punto : result) {
            if (punto.equals(max) || punto.equals(min)) continue;

            double area = calcularArea( min,max, punto);

            if (area < 0) cotaInferior.add(punto);
            if (area > 0) cotaSuperior.add(punto);
        }

        ArrayList<Point> hull = new ArrayList<>();
        hull.add(min);
        hull.addAll(quickHullRecursivo(min, max, cotaSuperior));
        hull.add(max);
        hull.addAll(quickHullRecursivo(max, min, cotaInferior));

        return hull;
    }

    public static ArrayList<Point> quickHullRecursivo(Point a, Point b, ArrayList<Point> puntos) {
        ArrayList<Point> result = new ArrayList<>();

        if (puntos.isEmpty()) return result;

        Point aux = null;
        double areaMax = -1;

        for (Point punto : puntos) {
            double area = Math.abs(calcularArea(a, b, punto));
            if (area > areaMax) {
                areaMax = area;
                aux = punto;
            }
        }

        if (aux == null) return result;

        ArrayList<Point> leftOfAC = new ArrayList<>();
        ArrayList<Point> leftOfCB = new ArrayList<>();

        for (Point punto : puntos) {
            if (punto.equals(aux)) continue;

            if (calcularArea(a, aux, punto) > 0) {
                leftOfAC.add(punto);
                continue;
            } else if (calcularArea(aux, b, punto) > 0) {
                leftOfCB.add(punto);
                continue;
            }
        }

        result.addAll(quickHullRecursivo(a, aux, leftOfAC));
        result.add(aux);
        result.addAll(quickHullRecursivo(aux, b, leftOfCB));

        return result;
    }

    public static double calcularArea(Point a, Point b, Point c) {
        return (b.getX() - a.getX()) * (c.getY() - a.getY()) - (b.getY() - a.getY()) * (c.getX() - a.getX());
    }

}