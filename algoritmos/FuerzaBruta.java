package ConvexHull.algoritmos;

import java.util.ArrayList;
import java.util.HashSet;

import javax.management.RuntimeErrorException;

public class FuerzaBruta {

    public static ArrayList<Point> calcularHull(ArrayList<Point> puntos) {
        ArrayList<Point> result = new ArrayList<>();
        // asegura que no se le mande una lista nula 
        if (puntos == null) {
            throw new RuntimeErrorException(null, "No hay puntos");
        }
        //elimina duplicados
        HashSet<Point> aux = new HashSet<>(puntos);
        ArrayList<Point> candidatos = new ArrayList<>(aux);
        //en caso de que nos den 2 o menos puntos los devolvemos 
        if (candidatos.size() <= 2) {
            return candidatos;
        }
        //cogemos dos puntos 
        for (int i = 0; i < candidatos.size(); i++) {
            for (int j = 0; j < candidatos.size(); j++) {

                Point p1 = candidatos.get(i);
                Point p2 = candidatos.get(j);
                //si son el mismo punto se salta
                if (p1.compareTo(p2) == 0) continue;
                //calculamos valores para calcular la recta 
                double a = p1.getY() - p2.getY();
                double b = p2.getX() - p1.getX();
                double c = (p1.getX() * p2.getY()) - (p2.getX() * p1.getY());
                //banderas para decidir si el punto es valido o no
                boolean posit = false;
                boolean neg = false;
                boolean inutil = false;
                //vemos si el p1 y el p2 forman parte de la envolvente comprobando el resto de puntos
                for (int k = 0; k < candidatos.size(); k++) {

                    Point p3 = candidatos.get(k);
                    if (p1.compareTo(p3) == 0 || p2.compareTo(p3) == 0) continue;
                    //calcula si el punto p3 esta por debajo o por encima de la recta formada por p1 y p2
                    double value = (a * p3.getX()) + (b * p3.getY()) + c;
                    //muchas veces hay pequeños errores al trabajar con numeros decimales y cuando debe dar 0 da una aproximacion por eso usamos 1e-10 que es un numero muy cercano a 0 y que minimiza el margen de error
                    if (Math.abs(value) < 0.0000000001) {
                        if (!p3.isBetween(p1, p2)) {
                            inutil = true;
                            break;
                        }
                    }
                   //los flags cambian en funcion al value
                    if (value > 0) posit = true;
                    if (value < 0) neg = true;
                    //si es positivo y negativo quiere decir que hay puntos por debajo y por arriba por tanto descartamos el punto
                    if (neg && posit) {
                        inutil = true;
                        break;
                    }
                }
                
                if (!inutil) {
                    if (!result.contains(p1)) result.add(p1);
                    if (!result.contains(p2)) result.add(p2);
                }
            }
        }

        return ordenarAntihorario(result);
    }

    public static ArrayList<Point> ordenarAntihorario(ArrayList<Point> points) {

        ArrayList<Point> ordered = new ArrayList<>(points);
        if (ordered.size() <= 2) return ordered;

        double cx = 0.0;
        double cy = 0.0;

        for (Point p : ordered) {
            cx += p.getX();
            cy += p.getY();
        }

        cx /= ordered.size();
        cy /= ordered.size();

        final double centerX = cx;
        final double centerY = cy;

        ordered.sort((p1, p2) -> {
            double angle1 = Math.atan2(p1.getY() - centerY, p1.getX() - centerX);
            double angle2 = Math.atan2(p2.getY() - centerY, p2.getX() - centerX);

            if (angle1 < angle2) return -1;
            if (angle1 > angle2) return 1;

            double d1 = Math.pow(p1.getX() - centerX, 2) + Math.pow(p1.getY() - centerY, 2);
            double d2 = Math.pow(p2.getX() - centerX, 2) + Math.pow(p2.getY() - centerY, 2);

            return Double.compare(d1, d2);
        });

        return ordered;
    }
}