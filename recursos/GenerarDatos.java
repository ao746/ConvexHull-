package ConvexHull.recursos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;


import  ConvexHull.algoritmos.Point;

public class GenerarDatos {

    private static Random random = new Random();

    public static ArrayList<Point> generarCuadrado(int cantidad) {

        HashSet<Point> set = new HashSet<>();
        int id = 0;

        while (set.size() < cantidad) {

            int x = random.nextInt(1000000000);
            int y = random.nextInt(1000000000);

            set.add(new Point(x, y, id++));
        }

        return new ArrayList<>(set);
    }

    public static ArrayList<Point> generarCirculo(int cantidad) {

        HashSet<Point> set = new HashSet<>();
        int id = 0;

        int radio = 500;

        while (set.size() < cantidad) {

            int x = random.nextInt(2 * radio) - radio;
            int y = random.nextInt(2 * radio) - radio;

            if (x * x + y * y <= radio * radio) {
                set.add(new Point(x, y, id++));
            }
        }

        return new ArrayList<>(set);
    }

    public static ArrayList<Point> generarTriangulo(int cantidad) {

        HashSet<Point> set = new HashSet<>();
        int id = 0;

        while (set.size() < cantidad) {

            int x = random.nextInt(1000000000);
            int y = random.nextInt(x + 1);

            set.add(new Point(x, y, id+10));
        }

        return new ArrayList<>(set);
    }

    public static ArrayList<Point> generarRombo(int cantidad) {

        HashSet<Point> set = new HashSet<>();
        int id = 0;

        int tamaño = 500000000;

        while (set.size() < cantidad) {

            int x = random.nextInt(2 * tamaño) - tamaño;
            int y = random.nextInt(2 * tamaño) - tamaño;

            if (Math.abs(x) + Math.abs(y) <= tamaño) {
                set.add(new Point(x, y, id++));
            }
        }

        return new ArrayList<>(set);
    }

    public static ArrayList<Point> generarLineaRectaHorizontal(int cantidad) {

        HashSet<Point> set = new HashSet<>();
        int id = 0;
        while (set.size() < cantidad) {

            int x = random.nextInt(1000000000);
            int y = 0;

            set.add(new Point(x, y, id+10));
        }

        return new ArrayList<>(set);
    }
    public static ArrayList<Point> generarLineaRectaVertical(int cantidad) {

        HashSet<Point> set = new HashSet<>();
        int id = 0;
        while (set.size() < cantidad) {

            int y= random.nextInt(1000000000);
            int x = 0;

            set.add(new Point(x, y, id+10));
        }

        return new ArrayList<>(set);
    }
    public static ArrayList<Point> generarLineaRectaCreciente(int cantidad) {

        HashSet<Point> set = new HashSet<>();
        int id = 0;
        while (set.size() < cantidad) {

            int x= random.nextInt(1000000000);
            int y = x*2+50;

            set.add(new Point(x, y, id+10));
        }

        return new ArrayList<>(set);
    }
    public static ArrayList<Point> generarLineaRectaDecreciente(int cantidad) {

    	HashSet<Point> set = new HashSet<>();
        int id = 0;
        while (set.size() < cantidad) {

            int x= random.nextInt(1000000);
            int y = x*(-200)+5000;

            set.add(new Point(x, y, id++));
        }

        return new ArrayList<>(set);
    }
    

	public static ArrayList<Point> generarGaussiana(int cantidad) {
		HashSet<Point> set = new HashSet<>();
        int id = 0;

        
        double mediaX = 0;
        double mediaY = 0;
        double desviacion = 100000000; 

        while (set.size() < cantidad) {

            int x = (int) (mediaX + random.nextGaussian() * desviacion);
            int y = (int) (mediaY + random.nextGaussian() * desviacion);

            set.add(new Point(x, y, id++));
        }

        return new ArrayList<>(set);
	}

	

}