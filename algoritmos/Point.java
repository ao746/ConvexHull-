package ConvexHull.algoritmos;

import java.util.Comparator;

public class Point implements Comparable<Point> {
    // instance variables for coordinates
    private double x;
    private double y;
    private int id;

    public Point(double x, double y, int id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getId() {
        return id;
    }

    public String toString() {
        return "(" + x + ", " + y + ") - " + id;
    }

    public boolean equals(Object o) {
        Point other = (Point)o;
        return (x == other.x) && (y == other.y); // && (id == other.id);
    }

    public int compareTo(Point other) {
        if (this.y < other.y) return -1;
        if (this.y > other.y) return +1;
        if (this.x < other.x) return -1;
        if (this.x > other.x) return +1;
        return 0;
    }
    
    public double squaredDistance(Point other) {
        double dx, dy;
        dx = x - other.x;
        dy = y - other.y;
        return dx * dx + dy * dy;
    }
    
    /**
     * Compares two points by polar angle (between 0 and 2&pi;) with respect to this point.
     *
     * @return the comparator
     */
    public Comparator<Point> polarOrder() {
        return new PolarOrder();
    }

    /**
    Check if this point is directly in between the two given
    points.  Note: the assumption is that they are colinear.
    @param o1 one of the points
    @param o2 the other point
    @return whether this point is between the two given points
     */
    public boolean isBetween(Point o1, Point o2) {
        double sqDisto1o2 = o1.squaredDistance(o2);
        return (squaredDistance(o1) < sqDisto1o2) && (squaredDistance(o2) < sqDisto1o2);
    }
    
    /**
     * Returns true if a→b→c is a counterclockwise turn.
     * @param a first point
     * @param b second point
     * @param c third point
     * @return { -1, 0, +1 } if a→b→c is a { clockwise, collinear; counterclockwise } turn.
     */
    public static int ccw(Point a, Point b, Point c) {
        double value = (b.x-a.x)*(c.y-a.y) - (b.y-a.y)*(c.x-a.x);
        if      (value < 0) return -1;
        else if (value > 0) return +1;
        else                return  0;
    }

    // compare other points relative to polar angle (between 0 and 2*pi) they make with this Point
    private class PolarOrder implements Comparator<Point> {
        public int compare(Point q1, Point q2) {
            double dx1 = q1.x - x;
            double dy1 = q1.y - y;
            double dx2 = q2.x - x;
            double dy2 = q2.y - y;

            if      (dy1 >= 0 && dy2 < 0) return -1;    // q1 above; q2 below
            else if (dy2 >= 0 && dy1 < 0) return +1;    // q1 below; q2 above
            else if (dy1 == 0 && dy2 == 0) {            // 3-collinear and horizontal
                if      (dx1 >= 0 && dx2 < 0) return -1;
                else if (dx2 >= 0 && dx1 < 0) return +1;
                else                          return  0;
            }
            else return -ccw(Point.this, q1, q2);     // both above or below
            // Note: ccw() recomputes dx1, dy1, dx2, and dy2
        }
    }
}
