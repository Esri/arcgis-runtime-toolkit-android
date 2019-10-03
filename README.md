# ArcGIS Runtime Toolkit Android

## Components
Please read the toolkit components usage guide on our [wiki](../../wiki)

- [ArcGISArView](../../wiki/ArcGISArView) - Allows quick and easy integration of AR into your application for a wide variety of scenarios
- [Compass](../../wiki/Compass) -  Shows the current orientation of a map or scene by displaying a compass icon that points towards North
- [Scalebar](../../wiki/Scalebar) - Displays the representation of an accurate linear measurement on a map

## Instructions

Update the project Gradle repositories in the **Project** view under **Gradle Scripts > build.gradle (Project:...)** to Esri's Bintray maven repository URL.

```groovy
allprojects {
  ...
  repositories {
    ...
    // *** ADD ***
    maven {
      url 'https://esri.bintray.com/arcgis'
    }
```

Update the app module Gradle dependencies in the **Project** view under **Gradle Scripts > build.gradle (Module: app)** to include the **ArcGIS Runtime Toolkit Android** dependency.

```groovy
dependencies {
  ...
  // *** ADD ***
  implementation "com.esri.arcgisruntime:arcgis-android-toolkit:100.6.0"
}

```

## Build the Toolkit AAR

```groovy
$ ./gradlew clean assembleDebug --info
```

## Requirements

The toolkit requires the ArcGIS Runtime SDK for Android. Refer to the Instructions section above if you are using Gradle.
See [this guide](https://developers.arcgis.com/android/latest/guide/install-and-set-up.htm) for complete instructions and
other options for installing the SDK.

The following table shows which versions of the SDK are compatible with the toolkit:

|  SDK Version  |  Toolkit Version  |
| --- | --- |
| 100.6.0 | 100.6.0 |

## Resources

* [ArcGIS Runtime SDK for Android](https://developers.arcgis.com/android/)
* [ArcGIS Blog](http://blogs.esri.com/esri/arcgis/)
* [twitter@esri](http://twitter.com/esri)

## Issues
Find a bug or want to request a new feature enhancement?  Please let us know by submitting an issue.

## Contributing

Esri welcomes contributions from anyone and everyone. Please see our [guidelines for contributing](https://github.com/esri/contributing).

## Licensing
Copyright 2019 Esri

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

A copy of the license is available in the repository's [LICENSE](LICENSE) file.
