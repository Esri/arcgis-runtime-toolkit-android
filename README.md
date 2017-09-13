# ArcGIS Runtime Toolkit Android

## Components
Please read the toolkit components usage guide on our [wiki](https://github.com/ArcGIS/arcgis-runtime-toolkit-android/wiki)

- [Scalebar](https://github.com/ArcGIS/arcgis-runtime-toolkit-android/wiki/Scalebar)

## Usage

```groovy
repositories {
    jcenter()
    // Our internal artifactory repository
    maven { url 'http://android:8080/artifactory/arcgis' }
}

dependencies {
    // use compile for pre Android 3.0
    implementation "com.esri.arcgisruntime:arcgis-android-toolkit:100.1.0-SNAPSHOT"
}
```

## Build the Toolkit AAR

```groovy
$ ./gradlew clean assembleDebug --info
```

## Publish the Toollkit AAR

```groovy
$ ./gradlew clean artifactoryPublish --info
```

## Issues
Find a bug or want to request a new feature enhancement?  Please let us know by submitting an issue.

## Contributing
Anyone and everyone is welcome to [contribute](.github/CONTRIBUTING.md). We do accept pull requests.

1. Get Involved
2. Report Issues
3. Contribute Code
4. Improve Documentation

Please see our [guidelines for contributing doc](https://github.com/Esri/contributing/blob/master/README.md)

## Licensing
Copyright 2017 Esri

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

A copy of the license is available in the repository's [LICENSE](LICENSE) file.
