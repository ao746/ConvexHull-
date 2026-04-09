package ConvexHull.algoritmos;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class ConvexHull {

	// To find orientation of ordered triplet (a, b, c). <=> ccw operation in Point
    // The function returns following values
    // -1 --> Clockwise
    // 0  --> a, b and c are co-linear
    // 1  --> Counterclockwise
    public static int orientation(Point a, Point b, Point c) {
        double value = (b.getX() - a.getX()) * (c.getY() - a.getY()) - (b.getY() - a.getY()) * (c.getX() - a.getX());
        if      (value < 0) return -1;
        else if (value > 0) return +1;
        else               return  0;
    }
    
    public static ArrayList<Point> GrahamScanConvexHull(ArrayList<Point> points) {
        if (points == null) throw new IllegalArgumentException("argument is null");
        if (points.size() == 0) throw new IllegalArgumentException("array is of length 0");

        // defensive copy
        int n = points.size();
        Point[] a = new Point[n];
        for (int i = 0; i < n; i++) {
            a[i] = points.get(i);
        }

        // preprocess so that a[0] has lowest y-coordinate; break ties by x-coordinate
        // a[0] is an extreme point of the convex hull
        // (alternatively, could do easily in linear time)
        Arrays.sort(a);

        // sort by polar angle with respect to base point a[0],
        // breaking ties by distance to a[0]
        Arrays.sort(a, 1, n, a[0].polarOrder());
        
        // It is a stack
        Deque<Point> GrahamCH = new ArrayDeque<Point>();

        GrahamCH.addFirst(a[0]);       // a[0] is first extreme point

        // find index j of first point not equal to a[0]
        int j;
        for (j = 1; j < n; j++)
            if (!a[0].equals(a[j]))
            	break;
        if (j == n)
        	return null;        // all points equal

        // find index k of first point not collinear with a[0] and a[j]
        int k;
        for (k = j + 1; k < n; k++)
        	if (orientation(a[0], a[j], a[k]) != 0) break;
        
        GrahamCH.addFirst(a[k - 1]);    // a[k-1] is second extreme point

        // Graham scan; note that a[n-1] is extreme point different from a[0]
        for (int i = k; i < n; i++) {
            Point top = GrahamCH.removeFirst();
            while (orientation(GrahamCH.peekFirst(), top, a[i]) <= 0) {
                top = GrahamCH.removeFirst();
            }
            GrahamCH.addFirst(top);
            GrahamCH.addFirst(a[i]);
        }

        if (isConvex(GrahamCH))
        	System.out.println("The hull is convex");
        else
        	System.out.println("The hull is not convex");
        
        ArrayList<Point> gsCH = new ArrayList<Point>();
        for (Point p : GrahamCH)
        	gsCH.add(p);

        return gsCH;
    }
    
    // check that boundary of hull is strictly convex
    public static boolean isConvex(Deque<Point> hull) {
        int n = hull.size();
        if (n <= 2) return true;

        Deque<Point> auxHull = new ArrayDeque<Point>();
        for (Point p : hull)
        	auxHull.addFirst(p);
        
        Point[] points = new Point[n];
        int k = 0;
        for (Point p : auxHull) {
            points[k++] = p;
        }

        for (int i = 0; i < n; i++) {
        	if (orientation(points[i], points[(i + 1) % n], points[(i + 2) % n]) <= 0) {
                return false;
            }
        }
        return true;
    }
    

    private static int RIGHT_INDEX = 1;
	private static int LEFT_INDEX = 0;
	public static ArrayList<Point> computeMergeHull(ArrayList<Point> points) {
		ArrayList<Point> hull = new ArrayList<Point>();
		
		if (points.size() > 0) { //the list contains points
			//sort the list of points by x-value from left to right (and by y-value as well)
			//this algorithm is O(nlogn), so it doesn't increase the big O time
			PointComparator comparator = new PointComparator();
			Collections.sort(points, comparator);
			
			//removes any duplicate points in the list
			//this algorithm is O(n), so it doesn't increase the big O time
			//NOTE: this noticeably slows the run time with tens of thousands of points, but also dramatically reduces the frequency of errors (I observed none after many runs)
			for (int i = 0; i < points.size() - 1; i++) {
				if (points.get(i + 1).equals(points.get(i))) {
					points.remove(i + 1);
					i--;
				}
			}
			
			//call recursive algorithm
			hull = recursiveMergeHull(points);
		}
		
		return hull;
	}

	// Recursively finds the convex hull around a set of points points - the list of points
	public static ArrayList<Point> recursiveMergeHull(List<Point> points) {
		//base case
		if (points.size() == 1) {
			return new ArrayList<Point>(points);
		}
		else {			
			//divide the points in half
			int mid = points.size() / 2;
			
			//recursively calculate both the left and right hulls
			ArrayList<Point> leftHull = recursiveMergeHull(points.subList(0, mid));
			ArrayList<Point> rightHull = recursiveMergeHull(points.subList(mid, points.size()));
			
			//merge the 2 hulls together
			return mergeHulls(leftHull, rightHull);
		}	
	}
	
	// Merges 2 convex hulls together
	// leftHull - the left convex hull to be merged
	// rightHull - the right convex hull to be merged
	public static ArrayList<Point> mergeHulls(ArrayList<Point> leftHull, ArrayList<Point> rightHull) {
		ArrayList<Point> hull = new ArrayList<Point>();
		
		//generate the indices for the upper and lower tangents
		//NOTE: the index variables RIGHT_LIST_INDEX and LEFT_LIST_INDEX can be found at the top of the file
		int[] upperTangentIndices = findUpperTangent(leftHull, rightHull);
		int[] lowerTangentIndices = findLowerTangent(leftHull, rightHull);
	
		//add the point on the upper tangent line that's in the right hull
		hull.add(rightHull.get(upperTangentIndices[RIGHT_INDEX]));

		//trace around the edge of the left hull from the index of the upper tangent until you reach the lower tangent, adding all points along the way to the hull
		int index = upperTangentIndices[LEFT_INDEX];
		while(index != lowerTangentIndices[LEFT_INDEX]) {
			hull.add(leftHull.get(index));
			index = (index + 1) % leftHull.size();
		}
		
		//add the points from the lower tangent line
		hull.add(leftHull.get(lowerTangentIndices[LEFT_INDEX]));					
		
		//trace around the edge of the right hull from the index of the lower tangent until you reach the upper tangent, adding all points along the way to the hull
		index = lowerTangentIndices[RIGHT_INDEX];
		while (index != upperTangentIndices[RIGHT_INDEX]) {
			hull.add(rightHull.get(index));
			index = (index + 1) % rightHull.size();
		}
				
		return hull;
	}	

	// Finds the lower tangent line connecting 2 convex hulls
	// leftHull - the left convex hull
	// rightHull - the right convex hull
	public static int[] findLowerTangent(ArrayList<Point> leftHull, ArrayList<Point> rightHull) {
		//find the leftmost and rightmost points of the right and left hulls respectively and connect them with a line
		int leftHullIndex = getRightmostPointIndex(leftHull);
		int rightHullIndex = getLeftmostPointIndex(rightHull);
		
		Point2D lhi = new Point2D.Double(leftHull.get(leftHullIndex).getX(), leftHull.get(leftHullIndex).getY());
		Point2D rhi = new Point2D.Double(rightHull.get(rightHullIndex).getX(), rightHull.get(rightHullIndex).getY());
		Line2D tangentLine = new Line2D.Double(lhi, rhi);
				
		//while you haven't found the lower tangent, keep walking down on both sides
		while(!(isLowerTangent(tangentLine, leftHull, leftHullIndex) && isLowerTangent(tangentLine, rightHull, rightHullIndex))) {
			while(!isLowerTangent(tangentLine, leftHull, leftHullIndex)) {
				leftHullIndex--;
				Point2D lh = new Point2D.Double(leftHull.get((leftHullIndex % leftHull.size() + leftHull.size()) % leftHull.size()).getX(), leftHull.get((leftHullIndex % leftHull.size() + leftHull.size()) % leftHull.size()).getY());
				tangentLine.setLine(lh, tangentLine.getP2());
			}
			while(!isLowerTangent(tangentLine, rightHull, rightHullIndex)) {
				rightHullIndex++;
				Point2D rh = new Point2D.Double(rightHull.get(rightHullIndex % rightHull.size()).getX(), rightHull.get(rightHullIndex % rightHull.size()).getY());
				tangentLine.setLine(tangentLine.getP1(), rh);
			}
		}
		
		int[] tangentIndices = new int[2];
		tangentIndices[LEFT_INDEX] = (leftHullIndex % leftHull.size() + leftHull.size()) % leftHull.size();
		tangentIndices[RIGHT_INDEX] = rightHullIndex % rightHull.size();
		
		return tangentIndices;
	}
	
	// Finds the upper tangent line connecting 2 convex hulls
	// leftHull - the left convex hull
	// rightHull - the right convex hull
	public static int[] findUpperTangent(ArrayList<Point> leftHull, ArrayList<Point> rightHull) {
		//find the leftmost and rightmost points of the right and left hulls respectively and connect them with a line		
		int leftHullIndex = getRightmostPointIndex(leftHull);
		int rightHullIndex = getLeftmostPointIndex(rightHull);
		
		Point2D lhi = new Point2D.Double(leftHull.get(leftHullIndex).getX(), leftHull.get(leftHullIndex).getY());
		Point2D rhi = new Point2D.Double(rightHull.get(rightHullIndex).getX(), rightHull.get(rightHullIndex).getY());
		Line2D tangentLine = new Line2D.Double(lhi, rhi);
			
		//while you haven't found the upper tangent, keep walking up on both sides
		while(!(isUpperTangent(tangentLine, leftHull, leftHullIndex) && isUpperTangent(tangentLine, rightHull, rightHullIndex))) {
			while(!isUpperTangent(tangentLine, leftHull, leftHullIndex)) {
				leftHullIndex++;
				Point2D lh = new Point2D.Double(leftHull.get(leftHullIndex % leftHull.size()).getX(), leftHull.get(leftHullIndex % leftHull.size()).getY());
				tangentLine.setLine(lh, tangentLine.getP2());
			}
			while(!isUpperTangent(tangentLine, rightHull, rightHullIndex)) {
				rightHullIndex--;
				Point2D rh = new Point2D.Double(rightHull.get((rightHullIndex % rightHull.size() + rightHull.size()) % rightHull.size()).getX(), rightHull.get((rightHullIndex % rightHull.size() + rightHull.size()) % rightHull.size()).getY());
				tangentLine.setLine(tangentLine.getP1(), rh);
			}
		}
		
		int[] tangentIndices = new int[2];
		tangentIndices[LEFT_INDEX] = leftHullIndex % leftHull.size();
		tangentIndices[RIGHT_INDEX] = (rightHullIndex % rightHull.size() + rightHull.size()) % rightHull.size();
		
		return tangentIndices;
	}
	
	// Returns true if the line is a lower tangent line to the given points
	// tangentLine - the potential tangent line
	// points - the points being compared to the line
	// index - the index of the point in the list which is a part of the potential tangent line
	public static boolean isLowerTangent(Line2D tangentLine, ArrayList<Point> points, int index) {
		boolean isLowerTangent = true;

		//get the points on either side of the point at the given index (the point that is contained in the line)
		Point2D testPoint1 = new Point2D.Double(points.get(((index + 1) % points.size() + points.size()) % points.size()).getX(), points.get(((index + 1) % points.size() + points.size()) % points.size()).getY());
		Point2D testPoint2 = new Point2D.Double(points.get(((index - 1) % points.size() + points.size()) % points.size()).getX(), points.get(((index - 1) % points.size() + points.size()) % points.size()).getY());

		if (tangentLine.relativeCCW(testPoint1) == -1) { //there is a point that is clockwise to the line, so it isn't a lower tangent
			isLowerTangent = false;
		}
		
		if (tangentLine.relativeCCW(testPoint2) == -1) { //there is a point that is clockwise to the line, so it isn't a lower tangent
			isLowerTangent = false;
		}
		
		return isLowerTangent;
	}
	
	// Returns true if the line is an upper tangent line to the given points
	// tangentLine - the potential tangent line
	// points - the points being compared to the line
	//index - the index of the point in the list which is a part of the potential tangent line
	public static boolean isUpperTangent(Line2D tangentLine, ArrayList<Point> points, int index) {
		boolean isUpperTangent = true;
		
		//get the points on either side of the point at the given index (the point that is contained in the line)
		Point2D testPoint1 = new Point2D.Double(points.get(((index + 1) % points.size() + points.size()) % points.size()).getX(), points.get(((index + 1) % points.size() + points.size()) % points.size()).getY());
		Point2D testPoint2 = new Point2D.Double(points.get(((index - 1) % points.size() + points.size()) % points.size()).getX(), points.get(((index - 1) % points.size() + points.size()) % points.size()).getY());

		if (tangentLine.relativeCCW(testPoint1) == 1) { //there is a point that is counterclockwise to the line, so it isn't a lower tangent
			isUpperTangent = false;
		}
		
		if (tangentLine.relativeCCW(testPoint2) == 1) { //there is a point that is counterclockwise to the line, so it isn't a lower tangent
			isUpperTangent = false;
		}
		
		return isUpperTangent;
	}
	
	// Returns the index of the rightmost point in a list of points
	// points - the list of points
	public static int getRightmostPointIndex(ArrayList<Point> points){
		int index = -1;
		//find the rightmost point
		double maxXValue = Double.MIN_VALUE;
		double maxYValue = Double.MIN_VALUE;
		for (int i = 0; i < points.size(); i++) {
			if (points.get(i).getX() > maxXValue) {
				maxXValue = points.get(i).getX();
				maxYValue = points.get(i).getY();
				index = i;
			} else if (points.get(i).getX() == maxXValue) { //the points have the same x-value, so sort by y-value
				if (points.get(i).getY() > maxYValue){
					maxYValue = points.get(i).getY();
					index = i;
				}
			}
		}
		
		if (index == -1) {
			//System.out.println("size of points: " + points.size());
			index = points.size() - 1;
		}
		
		return index;
	}
	
	// Returns the index of the leftmost point in a list of points
	// points - the list of points
	public static int getLeftmostPointIndex(ArrayList<Point> points){
		int index = -1;
		//find the rightmost point
		double minXValue = Double.MAX_VALUE;
		double minYValue = Double.MAX_VALUE;
		for (int i = 0; i < points.size(); i++) {
			if (points.get(i).getX() < minXValue) {
				minXValue = points.get(i).getX();
				minYValue = points.get(i).getY();
				index = i;
			} else if (points.get(i).getX() == minYValue) { //the points have the same x-value, so sort by y-value
				if (points.get(i).getY() < minYValue) {
					minYValue = points.get(i).getY();
					index = i;
				}
			}
		}

		if (index == -1) {
			//System.out.println("size of points: " + points.size());
			index = 0;
		}

		return index;
	}
	
    
}
