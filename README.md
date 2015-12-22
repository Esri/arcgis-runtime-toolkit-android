# Application Toolkit for the ArcGIS Android API.
The application toolkit for ArcGIS Android provides simplification classes to assist in supporting some Mapping and Locator workflows. You can add the Application Toolkit to an Android Studio project by adding a dependency to the ArcGIS Runtime SDK for Android AAR (library module) bundle to your project. This AAR bundle includes both the main API and the application toolkit API.

## API Usage
==========================

### MapViewHelper
MapView helper class to assist in simplifiying programmatic workflows to easily show callouts, add geometries, and create popups. You create an MapViewHelper by passing a MapView to it's constructor: 

```java
MapViewHelper mvHelper = new MapViewHelper(mMapView);
```

Now you can start calling methods on the ```MapViewHelper```.

### Show Callout
When you tap on a graphic in an app added by using methods below, a callout will open and show the title, snippet, and image of the selected graphic by default. This feature can be disabled by calling:

```java
mvHelper.setShowGraphicCallout(false);
```

You can also interact with geometries by registering a listener with the `MapViewHelper` class and implementing the `onGraphicClick` method:

```java
mvHelper.setOnGraphicClickListener(new OnGraphicClickListener(){
		public void onGraphicClick(graphic){
				// do something
 	}
});
```

### Popup
When you tap on a location on a map, the `MapViewHelper` will query all layers and create and return popups for the selected features to the listener using the following method. A popup does not need to be defined for any layers through webmap authoring or programmatically, the popup configuration will be used. 

```java
mvHelper.createPopup(screenX, screenY, new PopupCreateListener(){
		public void onResult(popupContainer){
			// do something
		}
});
```

### Find Place and Address
In addition to a MapViewHelper the application toolkit provides a `GeocodeHelper` to support geocode (find locations for given address/place name) and reverse geocode (find address for given location). 

```java
GeocodeHelper geocodeHelper = new GeocodeHelper();
// find an address for a given location
Future<LocatorReverseGeocodeResult> = geocodeHelper.findAddress(screenX, screenY, locator, mMapView, callback)
```

## Resources
* [ArcGIS Runtime SDK for Android Developers Site](https://developers.arcgis.com/en/android/)
* [ArcGIS Mobile Blog](http://blogs.esri.com/esri/arcgis/category/mobile/)
* [ArcGIS Developer Blog](http://blogs.esri.com/esri/arcgis/category/developer/)
* [twitter@esri](http://twitter.com/esri)
