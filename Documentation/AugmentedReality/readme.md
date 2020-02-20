# ArcGISARView

[![guide doc](https://img.shields.io/badge/Full_Developers_Guide-Doc-purple)](https://developers.arcgis.com/android/latest/guide/display-scenes-in-augmented-reality.htm) [![world-scale sample](https://img.shields.io/badge/World_Scale-Sample-blue)](https://developers.arcgis.com/android/latest/java/sample-code/navigate-in-ar/) [![Tabletop sample](https://img.shields.io/badge/Tabletop-Sample-blue)](https://developers.arcgis.com/android/latest/java/sample-code/display-scene-in-tabletop-ar/) [![Flyover sample](https://img.shields.io/badge/Flyover-Sample-blue)](https://developers.arcgis.com/android/latest/java/sample-code/explore-scene-in-flyover-ar//)

[Augmented reality](https://developers.arcgis.com/android/latest/guide/display-scenes-in-augmented-reality.htm) experiences are designed to "augment" the physical world with virtual content that respects real world scale, position, and orientation of a device. In the case of Runtime, a SceneView displays 3D geographic data as virtual content on top of a camera feed which represents the real, physical world.

The Augmented Reality (AR) toolkit component allows quick and easy integration of AR into your application for a wide variety of scenarios. The toolkit recognizes the following common patterns for AR:

* **Flyover** - Flyover AR allows you to explore a scene using your device as a window into the virtual world. A typical flyover AR scenario will start with the scene’s virtual camera positioned over an area of interest. You can walk around and reorient the device to focus on specific content in the scene. A working [flyover](https://github.com/Esri/arcgis-runtime-samples-android/tree/master/java/explore-scene-in-flyover-ar) app is also available.
* **Tabletop** - Scene content is anchored to a physical surface, as if it were a 3D-printed model. Take a look at our [tabletop](https://github.com/Esri/arcgis-runtime-samples-android/tree/master/java/display-scene-in-tabletop-ar) sample.

    ![Display scene in tabletop AR App](./Images/display-scene-in-tabletop-ar.png)

* **World-scale** - Scene content is rendered exactly where it would be in the physical world. A camera feed is shown and GIS content is rendered on top of that feed. This is used in scenarios ranging from viewing hidden infrastructure to displaying waypoints for navigation. Look at our example [navigation](https://github.com/Esri/arcgis-runtime-samples-android/tree/master/java/navigate-in-ar) app.

The AR toolkit component is comprised of one class: `ArcGISARView`. This is a subclass of `FrameLayout` that contains the functionality needed to display an AR experience in your application. It uses `ARCore`, Google's augmented reality framework to display the live camera feed and handle real world tracking and synchronization with the Runtime SDK's `SceneView`. The `ArcGISARView` is responsible for starting and managing an `ARCore` session. It uses a user-provided `LocationDataSource` for getting an initial GPS location and when continuous GPS tracking is required.

## Features

* Allows display of the live camera feed
* Manages `ARCore` `Session` lifecycle
* Tracks user location and device orientation through a combination of `ARCore` and the device GPS
* Provides access to an `SceneView` to display your GIS 3D data over the live camera feed
* `ARScreenToLocation` method to convert a screen point to a real-world coordinate

## Add ArcGISARView to your app

> **NOTE**: This assumes you've already added the toolkit to your app, either with [Gradle](../../) or by following the [setup instructions](../setup).

1. Add `ArcGISARView` to your layout:

    ```xml
    <android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.esri.arcgisruntime.toolkit.ar.ArcGISArView
        android:id="@+id/arcGisArView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    </android.support.constraint.ConstraintLayout>
    ```

2. Configure lifecycle methods

    ```kotlin
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_arcgissceneview)

        // Register your Activity as the Lifecycle Owner of this View.
        arcGisArView.registerLifecycle(lifecycle)
    }

    override fun onResume() {
        super.onResume()

        // Create a simple scene.
        arcGisArView.sceneView.scene = ArcGISScene(Basemap.createStreets())

        // Set a LocationDataSource, used to get our initial real-world location.
        arcGisArView.locationDataSource = ArLocationDataSource(this)

        // Start tracking our location and device orientation and begin the AR Session.
        arcGisArView.startTracking(ArcGISArView.ARLocationTrackingMode.INITIAL)
    }
    ```

The required permissions will be merged into your `AndroidManifest.xml` and are requested at runtime.

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```
