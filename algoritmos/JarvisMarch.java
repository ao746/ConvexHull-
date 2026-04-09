package     ConvexHull.algoritmos;

import java.util.ArrayList;
import java.util.HashSet;

public class JarvisMarch {

    public static ArrayList<Point> calcularHull(ArrayList<Point> puntos) {

        if (puntos == null) {
            throw new IllegalArgumentException("No hay puntos");
        }

        HashSet<Point> aux = new HashSet<>(puntos);
        ArrayList<Point> candidatos = new ArrayList<>(aux);

        if (candidatos.size() <= 2) {
            return candidatos;
        }

        int puntoIni = 0;
        PointComparator comp = new PointComparator();

        for (int i = 1; i < candidatos.size(); i++) {
            if (comp.compare(candidatos.get(i), candidatos.get(puntoIni)) < 0) {
                puntoIni = i;
            }
        }

        int p = puntoIni;
        ArrayList<Point> hull = new ArrayList<>();

        do {
            hull.add(candidatos.get(p));
            int q = (p + 1) % candidatos.size();

            for (int i = 0; i < candidatos.size(); i++) {
                if (i == p) continue;

                int direccion = Point.ccw(
                    candidatos.get(p),
                    candidatos.get(i),
                    candidatos.get(q)
                );

                if (direccion > 0) {
                    q = i;
                } else if (direccion == 0) {
                    if (candidatos.get(p).squaredDistance(candidatos.get(i)) >
                        candidatos.get(p).squaredDistance(candidatos.get(q))) {
                        q = i;
                    }
                }
            }

            p = q;
            
        } while (p != puntoIni);

        return hull;
    }
}