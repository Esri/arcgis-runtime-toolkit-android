# FloorFilterView

The FloorFilter component simplifies visualization of GIS data for a specific floor of a building in your application. It allows you to filter down the floor aware data displayed in your GeoView to a site, a building in the site, or a floor in the building.

The ArcGIS Runtime SDK currently supports filtering a 2D floor aware map based on the sites, buildings, or levels in the map.

## Workflow 1

The simplest workflow is to just instantiate a `FloorFilterView` and call `FloorFilterView.addToGeoView(GeoView)`
to display it within the GeoView after the map is `Loaded`. This causes default settings to be used. Optionally,
setter methods may be called to override some of the default settings. The app has limited control over the position
of the `FloorFilterView` (bottom-left, bottom-right, top-left, or top-right).

For example in Kotlin:

```kotlin
val floorFilterView = FloorFilterView(mapView.context)
floorFilterView.setMaxDisplayLevels(3) // optionally override default settings
mapView.map.addDoneLoadingListener {
  if (mapView.map.loadStatus == LoadStatus.LOADED) {
    floorFilterView.addToGeoView(mapView, FloorFilterView.ListPosition.TOP_END)
  }
}
```

## Workflow 2

Alternatively, the app could define a `FloorFilterView` anywhere it likes in its view hierarchy, because `FloorFilterView` extends the
Android `LinearLayout` class. The app then calls `FloorFilterView.bindTo(GeoView)` after the map is `Loaded` to make it come to life as
a `FloorFilterView` for the given GeoView. This workflow gives the app complete control over where the `FloorFilterView` is
displayed - it could be positioned on top of any part of the GeoView, or placed somewhere outside the bounds of the GeoView.

Here's example XML code that puts a 'FloorFilterView' in the bottom-left corner of a MapView :

```xml
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@android:color/background_dark">

        <com.esri.arcgisruntime.mapping.view.MapView
            android:id="@+id/mapView"
            android:layout_width="0dp"
            android:layout_height="0dp"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent" />

        <com.esri.arcgisruntime.toolkit.floorfilter.FloorFilterView
            android:id="@+id/floorFilterView"
            android:layout_width="wrap_content"
            android:layout_height="0dp"
            android:layout_marginBottom="40dp"
            android:layout_marginStart="20dp"
            android:layout_marginEnd="20dp"
            android:layout_marginTop="20dp"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintHeight_max="wrap"
            app:layout_constraintVertical_bias="1"
            app:closeButtonPosition="top"
            android:background="@drawable/floor_filter_rounded_background"
            android:elevation="4dp"/>

    </androidx.constraintlayout.widget.ConstraintLayout>

</layout>
```

Here's example Kotlin code to bind the `FloorFilterView` to the GeoView:

```kotlin
val floorFilterView = findViewById(R.id.floorFilterView)
mapView.map.addDoneLoadingListener {
  if (mapView.map.loadStatus == LoadStatus.LOADED) {
    floorFilterView.bindTo(mapView)
  }
}
```

To see it in action, try out the floorfilter test in the [toolkit-test-app](https://github.com/Esri/arcgis-runtime-toolkit-android/tree/master/toolkit-test-app/src/main/java/com/esri/arcgisruntime/toolkit/test)
