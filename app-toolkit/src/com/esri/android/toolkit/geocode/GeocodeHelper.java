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

package com.esri.android.toolkit.geocode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import android.graphics.drawable.Drawable;

import com.esri.android.map.MapView;
import com.esri.android.toolkit.map.MapViewHelper;
import com.esri.android.toolkit.util.TaskExecutor;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.CallbackListener;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;
import com.esri.core.tasks.geocode.LocatorReverseGeocodeResult;

/**
 * Helper class providing static methods to generate candidates for an address
 * or find an address for a given location. These methods simplify the workflow
 * of setting parameters and getting result from a Locator. The operation can be
 * cancelled through Future. When an operation is cancelled, the the callback
 * won't be invoked. However, the request to the operation may be sent before
 * the operation is cancelled.
 * 
 * @since 10.2
 */

public class GeocodeHelper {

	private static final int TOLERANCE = 100;

	/**
	 * Finds an address for a given location.
	 * 
	 * @param screenX
	 *            the x coordinate of the touch pointer in screen pixels.
	 * @param screenY
	 *            the y coordinate of the touch pointer in screen pixels.
	 * @param locator
	 *            the geocode service of ArcGIS Server to use. If null, a
	 *            default locator using ESRI ArcGIS online worldwide geocoding
	 *            service will be used. The endpoint of this service is
	 *            "http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer"
	 *            .
	 * @param mapView
	 *            the MapView. When mapView is null, null will be returned.
	 * @param callback
	 *            the callback to invoke when the operation is complete or on
	 *            error.
	 * @return a Future which resolves to a LocatorReverseGeocodeResult if the
	 *         operation is complete.
	 */
	public static Future<LocatorReverseGeocodeResult> findAddress(
			float screenX, float screenY, Locator locator, MapView mapView,
			CallbackListener<LocatorReverseGeocodeResult> callback) {
		if (mapView == null || !mapView.isLoaded())
			return null;
		Point point = mapView.toMapPoint(screenX, screenY);
		Point point2 = (Point) GeometryEngine.project(point,
				mapView.getSpatialReference(),
				SpatialReference.create(SpatialReference.WKID_WGS84));
		return Locator.findAddress(point2.getY(), point2.getX(), locator,
				callback);
	}

	/**
	 * Generate location candidates for a given address. For each candidate the
	 * location will be added to the map view as a marker graphic. When tapping
	 * on the marker graphic the given address will be displayed in a callout
	 * window. The marker graphic and the associated callout window can be
	 * manipulated through MapView.
	 * 
	 * @param name
	 *            the address to lookup.
	 * @param locator
	 *            the geocode service of ArcGIS Server to use. If null, a
	 *            default locator using ESRI ArcGIS online worldwide geocoding
	 *            service will be used. The endpoint of this service is
	 *            "http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer"
	 *            .
	 * @param mapViewHelper
	 *            the MapView used to show the results. When mapview is null,
	 *            results won't be shown.
	 * @param icon
	 *            the symbol used to display the resulted location. If icon is
	 *            null an Android default icon which is device dependent is
	 *            used.
	 * @param numToShow
	 *            the number of candidates to show. Null will be return if
	 *            numToShow is less than 1.
	 * @param callback
	 *            the callback to invoke when the operation is complete or on
	 *            error.
	 * @return a Future which resolves to a LocatorGeocodeResult if the
	 *         operation is complete.
	 */
	public static Future<List<LocatorGeocodeResult>> showLocation(
			final String name, final Locator locator,
			final MapViewHelper mapViewHelper, final Drawable icon,
			final int numToShow,
			final CallbackListener<List<LocatorGeocodeResult>> callback) {
		if (mapViewHelper == null || !mapViewHelper.isLoaded())
			return null;
		return TaskExecutor.pool
				.submit(new Callable<List<LocatorGeocodeResult>>() {
					@Override
					public List<LocatorGeocodeResult> call() {
						if (numToShow < 1)
							return null;
						try {
							final Locator loc = locator != null ? locator
									: Locator.createOnlineLocator();
							LocatorFindParameters findParams = new LocatorFindParameters(
									name);
							findParams.setMaxLocations(numToShow);
							final List<LocatorGeocodeResult> results = loc
									.find(findParams);
							if (Thread.currentThread().isInterrupted())
								return results;
							if (callback != null) {
								callback.onCallback(results);
							}
							if (results != null && results.size() > 0) {
								mapViewHelper.getMapView().post(new Runnable() {

									@Override
									public void run() {
										for (int i = 0; i < results.size(); i++) {
											LocatorGeocodeResult result = results
													.get(i);
											mapViewHelper
													.addMarkerGraphic(result
															.getLocation()
															.getY(), result
															.getLocation()
															.getX(), result
															.getAddress(),
															null, null, icon,
															false, 1);
										}
									}
								});
							}
							return results;
						} catch (Exception e) {
							if (callback != null)
								callback.onError(e);
						}
						return null;
					}
				});
	}

	/**
	 * Finds an address for a given location using screen coordinates and add to
	 * the MapView as a marker graphic. When tapping on the marker graphic the
	 * address will be displayed in a callout window. The marker graphic and the
	 * associated callout window can be manipulated through MapView.
	 * 
	 * @param screenX
	 *            the x coordinate of the touch pointer in screen pixels.
	 * @param screenY
	 *            the y coordinate of the touch pointer in screen pixels.
	 * @param locator
	 *            the geocode service of ArcGIS Server to use. If null, a
	 *            default locator using ESRI ArcGIS online worldwide geocoding
	 *            service will be used. The endpoint of this service is
	 *            "http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer"
	 *            .
	 * @param mapViewHelper
	 *            the MapViewHelper used to show the results. When mapViewHelper
	 *            is null, results won't be shown.
	 * @param icon
	 *            the symbol used to display the given location. If icon is null
	 *            an Android default icon which is device dependent will be
	 *            used.
	 * @param fieldsToShow
	 *            the list of address fields used to display the address in a
	 *            callout window. An empty callout window will be display if
	 *            fieldsToShow is null.
	 * @param callback
	 *            the callback to invoke when the operation is complete or on
	 *            error.
	 * @return a Future which resolves to a LocatorReverseGeocodeResult if the
	 *         operation is complete.
	 */
	public static Future<LocatorReverseGeocodeResult> showAddress(
			final float screenX, final float screenY, final Locator locator,
			final MapViewHelper mapViewHelper, final Drawable icon,
			final String[] fieldsToShow,
			final CallbackListener<LocatorReverseGeocodeResult> callback) {
		if (mapViewHelper == null || !mapViewHelper.isLoaded())
			return null;
		Point point = mapViewHelper.getMapView().toMapPoint(screenX, screenY);
		Point point2 = (Point) GeometryEngine.project(point, mapViewHelper
				.getMapView().getSpatialReference(), SpatialReference
				.create(SpatialReference.WKID_WGS84));
		return showAddress(point2.getY(), point2.getX(), locator,
				mapViewHelper, icon, fieldsToShow, callback);
	}

	/**
	 * Finds an address for a given location using lat/lon and add to the
	 * MapView as a marker graphic. When tapping on the marker graphic the
	 * address will be displayed in a callout window. The marker graphic and the
	 * associated callout window can be manipulated through MapView.
	 * 
	 * @param lat
	 *            latitude of the given location.
	 * @param lon
	 *            logitude of the given location.
	 * @param locator
	 *            the geocode service of ArcGIS Server to use. If null, a
	 *            default locator using ESRI ArcGIS online worldwide geocoding
	 *            service will be used. The endpoint of this service is
	 *            "http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer"
	 *            .
	 * @param mapViewHelper
	 *            the MapViewHelper used to show the results. When mapViewHelper
	 *            is null, results won't be shown.
	 * @param icon
	 *            the symbol used to display the given location. If icon is null
	 *            an Android default icon which is device dependent is used.
	 * @param fieldsToShow
	 *            the list of address fields used to display the address in a
	 *            callout window. An empty callout window will be display if
	 *            fieldsToShow is null.
	 * @param callback
	 *            the callback to invoke when the operation is complete or on
	 *            error.
	 * @return a Future which resolves to a LocatorReverseGeocodeResult if the
	 *         operation is complete.
	 */
	public static Future<LocatorReverseGeocodeResult> showAddress(
			final double lat, final double lon, final Locator locator,
			final MapViewHelper mapViewHelper, final Drawable icon,
			final String[] fieldsToShow,
			final CallbackListener<LocatorReverseGeocodeResult> callback) {
		if (mapViewHelper == null || !mapViewHelper.isLoaded())
			return null;
		return TaskExecutor.pool
				.submit(new Callable<LocatorReverseGeocodeResult>() {
					@Override
					public LocatorReverseGeocodeResult call() {
						try {
							final Locator loc = locator != null ? locator
									: Locator.createOnlineLocator();
							SpatialReference sr = SpatialReference
									.create(SpatialReference.WKID_WGS84);
							final LocatorReverseGeocodeResult result = loc
									.reverseGeocode(new Point(lon, lat),
											TOLERANCE, sr, sr);
							if (Thread.currentThread().isInterrupted())
								return result;
							if (callback != null)
								callback.onCallback(result);
							if (result != null) {
								mapViewHelper.getMapView().post(new Runnable() {

									@Override
									public void run() {
										StringBuilder sb = new StringBuilder();
										Map<String, String> addressFields = result
												.getAddressFields();
										if (addressFields != null
												&& !addressFields.isEmpty()
												&& fieldsToShow != null
												&& fieldsToShow.length > 0) {
											for (String field : fieldsToShow) {
												if (addressFields
														.containsKey(field))
													sb.append(addressFields
															.get(field) + " ");
											}
										}
										mapViewHelper.addMarkerGraphic(lat,
												lon, sb.toString(), null, null,
												icon, false, 1);
									}
								});
							}
							return result;
						} catch (Exception e) {
							if (callback != null)
								callback.onError(e);
						}
						return null;
					}
				});
	}
}
