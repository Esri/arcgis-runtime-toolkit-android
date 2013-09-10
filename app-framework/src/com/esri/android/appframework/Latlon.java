/*
 COPYRIGHT 1995-2011 ESRI

 TRADE SECRETS: ESRI PROPRIETARY AND CONFIDENTIAL
 Unpublished material - all rights reserved under the
 Copyright Laws of the United States.

 For additional information, contact:
 Environmental Systems Research Institute, Inc.
 Attn: Contracts Dept
 380 New York Street
 Redlands, California, USA 92373

 email: contracts@esri.com
 */

package com.esri.android.appframework;

import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;

/**
 * Utility class providing convenient methods to create geometries from latitude and longitude.
 * 
 * @since 10.2
 */
// TODO: validation of given lat/lon. waiting for geometry team for a proper solution.
public class Latlon {
	
	/**
  * Creates points from latitude and longitude. 
  * 
  * @param latlon an array of one or more latitude and longitude pairs,
  * e.g. {{37.761, -122.50}, {37.750, -122.474}, ...}. latitude and longitude are 
  * measured in degrees. 
  * @return an array of points or null if the given latlon is null or empty.
  * 
  */
  public static Point[] createPoints(double[][] latlon) {
    if (latlon == null || latlon.length < 1)
      return null;
    Point[] pts = new Point[latlon.length];
    for (int i = 0; i < pts.length; i++) {
      pts[i] = new Point(latlon[i][1], latlon[i][0]);
    }
    return pts;
  }
  
  /**
   * Creates a polyline from latitude and longitude. 
   * 
   * @param latlon an array of one or more latitude and longitude pairs,
   * e.g. {{37.761, -122.50}, {37.750, -122.474}, ...}. latitude and longitude are 
   * measured in degrees. 
   * @return a polyline or null if the given latlon is null or its length is less than 2.
   * 
   */
  public static Polyline createPolyline(double[][] latlon) {
    if (latlon == null || latlon.length < 2)
      return null;
    Polyline polyline = new Polyline();
    polyline.startPath(latlon[0][1], latlon[0][0]);
    for (int i = 1; i < latlon.length; i++) {
      polyline.lineTo(latlon[i][1], latlon[i][0]);
    }
    return polyline;
  }
  
  /**
   * Creates a polygon from latitude and longitude. 
   * 
   * @param latlon an array of one or more latitude and longitude pairs,
   * e.g. {{37.761, -122.50}, {37.750, -122.474}, ...}. latitude and longitude are 
   * measured in degrees. 
   * @return a polygon or null if the given latlon is null or its length is less than 3.
   * 
   */
  public static Polygon createPolygon(double[][] latlon) {
    if (latlon == null || latlon.length < 3)
      return null;
    Polygon polygon = new Polygon();
    polygon.startPath(latlon[0][1], latlon[0][0]);
    for (int i = 1; i < latlon.length; i++) {
      polygon.lineTo(latlon[i][1], latlon[i][0]);
    }
    polygon.closeAllPaths();
    return polygon;
  }
}
