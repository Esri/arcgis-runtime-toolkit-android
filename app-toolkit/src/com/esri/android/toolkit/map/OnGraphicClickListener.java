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

package com.esri.android.toolkit.map;

import com.esri.core.map.Graphic;

/**
 * Defines signatures for methods that are called when tapping on a graphic added through 
 * the MapView.addXXXGraphic() methods.
 * 
 * @since 10.2
 */

public interface OnGraphicClickListener {
	
	/**
	 * Called when tapping on the graphic.
	 * 
	 * @param g the graphic which is associated with the clicked callout window.
	 */
    void onGraphicClick(Graphic g);
}
