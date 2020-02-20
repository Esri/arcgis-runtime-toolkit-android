# Scalebar

A scalebar displays the representation of an accurate linear measurement on the map. It provides a visual indication
through which users can determine the size of features or the distance between features on a map. A scalebar is a line
or bar, sometimes divided into segments. It is labeled with its ground length, usually in multiples of map units, such
as tens of kilometers or hundreds of miles.

The scalebar uses geodetic calculations to provide accurate measurements for maps of any spatial reference. The
measurement is accurate for the center of the map extent being displayed. This means at smaller scales (zoomed way out)
you might find it somewhat inaccurate at the extremes of the visible extent. As the map is panned and zoomed, the
scalebar automatically grows and shrinks and updates its measurement based on the new map extent.

## Style

Five Style options are available:

* `BAR` - A simple, non-segmented bar. A single label is displayed showing the distance represented by the length of the
whole bar.
* `ALTERNATING_BAR` - This is the default style. A bar split up into equal-length segments, with the colors of the segments
alternating between the fill color and the alternate fill color. A label is displayed at the end of each segment,
showing the distance represented by the length of the bar up to that point.
* `LINE` - A simple, non-segmented line. A single label is displayed showing the distance represented by the length of the
whole line.
* `GRADUATED_LINE` - A line split up into equal-length segments. A tick and a label are displayed at the end of each
segment, showing the distance represented by the length of the line up to that point.
* `DUAL_UNIT_LINE` - A line showing distance in dual unit systems - metric and imperial. The primary unit system, as set by
`Scalebar.setUnitSystem(UnitSystem)`, is used to determine the length of the line. A label above the line shows the
distance represented by the length of the whole line, in the primary unit system. A tick and another label are displayed
below the line, showing distance in the other unit system.

## Unit System

Two options are available - `METRIC` and `IMPERIAL`. The default is `METRIC` which displays distances in meters and
kilometers depending on the map scale. `IMPERIAL` displays distances in feet and miles.

## Alignment

As you pan & zoom the map, the scalebar automatically grows and shrinks to update its measurements for the new map
extent. The `alignment` property indicates how the scalebar has been placed on the UI and which section of the scalebar
should remain fixed. For example, if you place it in the lower-left corner of the UI, set the alignment to `LEFT` which
means that it will grow/shrink on the right side. The other options are `RIGHT` and `CENTER`.

## Workflow 1

The simplest workflow is to just instantiate a `Scalebar` and call `ScaleBar.addToMapView(MapView)` to display it within
the MapView. This causes default settings to be used. Optionally, setter methods may be called before calling
`addToMapView`, to override some of the default settings. The app has limited control over the position of the scalebar
(bottom-left, bottom-right or bottom-centered) and no control over the size (it is sized automatically to fit
comfortably within the MapView).

For example in Kotlin:

```kotlin
val scaleBar = Scalebar(mapView.context)
with (scaleBar) {
    alignment = Scalebar.Alignment.CENTER
    addToMapView(mapView)
}
```

and the same thing in Java:

```java
mScalebar = new Scalebar(mMapView.getContext());
mScalebar.setAlignment(Scalebar.Alignment.CENTER); // optionally override default settings
mScalebar.addToMapView(mMapView);
```

## Workflow 2

Alternatively, the app could define a `Scalebar` anywhere it likes in its view hierarchy, because `Scalebar` extends the
Android `View` class. The app then calls `Scalebar.bindTo(MapView)` to make it come to life as a scalebar for the given
MapView. This workflow gives the app complete control over where the scalebar is displayed - it could be positioned on
top of any part of the MapView, or placed somewhere outside the bounds of the MapView. It also gives the app complete
control over the size of the scalebar.

Here's example XML code that puts a scalebar on top of the top-left corner of the MapView and overrides the default
values of `fillColor`, `lineColor`, `style`, `textColor`, `textShadowColor` and `unitSystem` :

```xml
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- MapView -->
    <com.esri.arcgisruntime.mapping.view.MapView
        android:id="@+id/mapView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent">
    </com.esri.arcgisruntime.mapping.view.MapView>

    <com.esri.arcgisruntime.toolkit.scalebar.Scalebar
        android:id="@+id/scalebar"
        android:layout_width="0dp"
        android:layout_height="50dp"
        android:layout_margin="5dp"
        app:fillColor="@android:color/holo_orange_light"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:lineColor="@android:color/holo_blue_bright"
        app:style="dualUnitLine"
        app:textColor="@android:color/black"
        app:textShadowColor="@android:color/white"
        app:unitSystem="imperial" />

</android.support.constraint.ConstraintLayout>
```

Here's example Kotlin code to bind the `Scalebar` to the MapView:

```kotlin
scaleBar.bindTo(mapView)
```

and the same thing in Java:

```java
mScalebar = (Scalebar) findViewById(R.id.scalebar);
mScalebar.bindTo(mMapView);
```
