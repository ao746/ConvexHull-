package ConvexHull


.recursos;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import ConvexHull.algoritmos.Point;

public class LectorPuntos {

    public static ArrayList<Point> leerArchivo(File f) throws FileNotFoundException {
        ArrayList<Point> puntos = new ArrayList<>();
        Scanner sc = new Scanner(f);

        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] token = line.split("\\s+");
            int id = Integer.parseInt(token[0]);
            double x = Float.parseFloat(token[1].replace(",", "."));
            double y = Float.parseFloat(token[2].replace(",", "."));

            puntos.add(new Point(x, y, id));
        }

        sc.close();
        return puntos;
    }
}