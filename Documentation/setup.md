# Toolkit setup instructions

## Prerequisites

To use the ArcGIS Runtime Toolkit for Android, first you need an app that uses the ArcGIS Runtime SDK for Android:

1. Install the ArcGIS Runtime SDK for Android by following the instructions to [Install the SDK](https://developers.arcgis.com/android/install-and-set-up/).
2. Create an app that uses the ArcGIS Runtime SDK. For example you could follow the instructions to [Display a map](https://developers.arcgis.com/android/maps-2d/tutorials/display-a-map/).

## Use the toolkit library

You can add toolkit components which complement the functionality of the ArcGIS Runtime SDK for Android into your own applications.  For example the toolkit allows you to add components such as scale bars, north arrows, or augmented reality controls.

The toolkit components are available as a library (.aar) in Jfrog, or you can use the source code for the toolkit directly from this repository.  Read the [read me](https://github.com/Esri/arcgis-runtime-toolkit-android/blob/master/README.md) instructions for using the compiled library.

## Use the toolkit from source code

To download the Toolkit, you should fork the repo and then create a local copy, or clone, of it:

1. To fork this Github repo, [click here and follow the instructions](https://github.com/ArcGIS/arcgis-runtime-toolkit-android/fork).
2. There are various ways to clone the fork you've created, but here's how it can be done using command line git:

    ```sh
    cd /<YOUR-DEVELOPMENT-FOLDER>
    git clone https://github.com/YOUR-USERNAME/arcgis-runtime-toolkit-android.git
    ```

For more information about forking and cloning GitHub repos, see [GitHub Help](https://help.github.com/articles/fork-a-repo/).

## Build the toolkit

You'll need to build the toolkit before you can add it to your apps:

1. Run Android Studio
2. Select **Open an existing Android Studio project** from the Welcome to Android Studio dialog.
3. Navigate to the `arcgis-runtime-toolkit-android` folder what was created when you cloned the repo above, and click **Open**.
4. Pull down the **Build** menu and select **Rebuild Project**. This builds the aar file that you'll add to your project below.
5. If you wish you can run the `toolkit-test-app` to see toolkit components in action. This app displays a list of activities that are used to test the toolkit components. To test a component, just select the corresponding test activity from the list that's displayed. See [CompassTestActivity](./Compass/testing.md) and [ScalebarTestActivity](./Scalebar/testing.md) for more information.

## Add toolkit components to an app

To add the toolkit to an app:

1. Open the app in Android Studio.
2. Pull down the **File** menu and select **Project Structure...**.
3. Click on your app module and then the **Dependencies** tab.
4. Click the **+** button and select **Jar Dependency**.
5. Navigate to the aar file that was created when you built the toolkit above, and click **Open**. The path to the aar file is:

    ```txt
    arcgis-runtime-toolkit-android/arcgis-android-toolkit/build/outputs/aar/arcgis-android-toolkit.aar
    ```

6. You can now add toolkit components to the app. See [Compass](Compass/) and [Scalebar](Scalebar/) for more information.
