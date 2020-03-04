# ArcGIS Runtime Toolkit Android

[![doc badge](https://img.shields.io/badge/Doc-purple)](Documentation) [![package badge](https://img.shields.io/bintray/v/esri/arcgis/arcgis-android-toolkit?color=limegreen)](https://bintray.com/esri/arcgis/arcgis-android-toolkit)

The ArcGIS Runtime Toolkit for Android contains views that you can use with [ArcGIS Runtime SDK for Android](https://developers.arcgis.com/android/).

You can use the Toolkit in your projects by:

1. **Reference from Bintray** - the fastest way to get toolkit into your app
    * Ensure the Esri public Bintray Maven repository is in your project's gradle file - `https://esri.bintray.com/arcgis`

    ```
    allprojects {
    	repositories {
    		...
    		maven { url 'https://esri.bintray.com/arcgis' }
    		...
    	}
    }
    ```

    * Add the toolkit dependency to the module's gradle file 

    ```
    dependencies {
    	implementation 'com.esri.arcgisruntime:arcgis-android-toolkit:100.7.0'
    }
    ```

2. **[Build from source](Documentation/setup.md)** - do this if you want to customize the toolkit

    ```groovy
    $ ./gradlew clean assembleDebug --info
    ```

See the [setup instructions](Documentation/setup.md) for a more detailed guide.

## Features

* **[ArcGISArView](Documentation/AugmentedReality)** - Integrates SceneView with ARCore to enable augmented reality (AR)
* **[Bookmarks](Documentation/Bookmarks)** - Displays the bookmarks present in a map
* **[Compass](Documentation/Compass)** -  Shows the current orientation of a map or scene by displaying a compass icon that points towards North
* **[Scalebar](Documentation/Scalebar)** - Displays the representation of an accurate linear measurement on a map

## Requirements

The toolkit requires the ArcGIS Runtime SDK for Android. Refer to the Instructions section above if you are using Gradle.
See [this guide](https://developers.arcgis.com/android/latest/guide/install-and-set-up.htm) for complete instructions and
other options for installing the SDK.

The following table shows which versions of the SDK are compatible with the toolkit:

|  SDK Version  |  Toolkit Version  |
| --- | --- |
| 100.6.0 | 100.6.0 |
| 100.6.0 | 100.6.1 |
| 100.7.0 | 100.7.0 |

## Resources

* [ArcGIS Runtime SDK for Android](https://developers.arcgis.com/android/)
* [System requirements](https://developers.arcgis.com/android/latest/guide/system-requirements.htm)
* [ArcGIS Blog](http://blogs.esri.com/esri/arcgis/)
* [twitter@esri](http://twitter.com/esri)

## Issues

Find a bug or want to request a new feature enhancement? Please let us know by [submitting an issue](https://github.com/Esri/arcgis-runtime-toolkit-android/issues/new).

## Contributing

Esri welcomes contributions from anyone and everyone. Please see our [guidelines for contributing](https://github.com/esri/contributing).

## Licensing

Copyright 2019-2020 Esri

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

A copy of the license is available in the repository's [LICENSE](LICENSE) file.
