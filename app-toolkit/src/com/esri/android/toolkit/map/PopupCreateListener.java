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

import com.esri.android.map.popup.PopupContainer;

/**
 * Defines signatures for methods that are called when some popups have been added to the PopupContainer.
 * 
 * @since 10.2
 */

public interface PopupCreateListener {
	/**
	 * Called when some popups have been added to the PopupContainer.
	 * 
	 * @param container the PopupContainer to hold the created popups.
	 */
  public void onResult(PopupContainer container);
}
