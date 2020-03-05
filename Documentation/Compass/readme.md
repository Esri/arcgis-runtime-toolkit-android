# Compass

A compass shows the current orientation of a map or scene by displaying a compass icon that points towards North. The icon can be tapped to reset the map/scene to 0 degrees orientation. By default the icon is hidden any time the map/scene is orientated to 0 degrees. The auto hide behavior can be disabled using `Compass.isAutoHide = false`.

## Workflow 1

The simplest workflow is to instantiate a `Compass` and call `Compass.addToGeoView(GeoView)` to display it within the GeoView. Optionally, setter methods may be called to override some of the default settings. This workflow gives the app no control over the position of the compass - it's always placed at the top-right corner of the GeoView.

For example in Kotlin:

```kotlin
val compass = Compass(geoView.context)
with (compass) {
    isAutoHide = false
    addToGeoView(geoView)
}
```

and the same thing in Java:

```java
mCompass = new Compass(mGeoView.getContext());
mCompass.setAutoHide(false); // optionally override default settings
mCompass.addToGeoView(mGeoView);
```

## Workflow 2

Alternatively, the app could define a `Compass` anywhere it likes in its view hierarchy, because `Compass` extends the Android `View` class. The app then calls `Compass.bindTo(GeoView)` to make it come to life as a compass for the given GeoView. This workflow gives the app complete control over where the compass is displayed - it could be positioned on top of any part of the GeoView, or placed somewhere outside the bounds of the GeoView.

Here's example XML code that puts a compass on top of the top-left corner of a MapView and overrides the default
value of `autoHide`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.esri.arcgisruntime.mapping.view.MapView
        android:id="@+id/mapView"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"/>

    <com.esri.arcgisruntime.toolkit.compass.Compass
        android:id="@+id/compass"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:layout_margin="5dp"
        app:autoHide="false"
        app:layout_constraintStart_toStartOf="@+id/mapView"
        app:layout_constraintTop_toTopOf="@+id/mapView" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

Here's example Kotlin code to bind the `Compass` to a GeoView:

```kotlin
compass.bindTo(geoView)
```

and the same thing in Java:

```java
mCompass = (Compass) findViewById(R.id.compass);
mCompass.bindTo(mGeoView);
```
