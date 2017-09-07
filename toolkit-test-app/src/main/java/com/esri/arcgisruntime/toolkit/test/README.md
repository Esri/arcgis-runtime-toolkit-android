# toolkit-test-app

## Usage instructions

This app displays a list of activities that are used to test the toolkit components. To test a component, just select
the corresponding test activity from the list that's displayed. Each test activity has its own README.md file in the
same directory as the source code.

## Adding a new test activity

1. Create a subdirectory of `com.esri.arcgisruntime.toolkit.test` to hold the new activity, for example
`com.esri.arcgisruntime.toolkit.test.foo`.
2. Put all the source code for the new activity in the new subdirectory.
3. Add the new activity to the `Android.Manifest.xml` file, for example:
```
<activity
        android:name="com.esri.arcgisruntime.toolkit.test.foo.FooTestActivity"
        android:label="Foo Test"
        android:theme="@style/AppTheme">
    <intent-filter>
        <action android:name="android.intent.action.MAIN"/>

        <category android:name="android.intent.category.TEST"/>
    </intent-filter>
</activity>
```
4. Add an item containing the fully-qualified class name of the new activity to `activityClassNames` in
`res/values/arrays.xml`, for example:
```
<item>com.esri.arcgisruntime.toolkit.test.foo.FooTestActivity</item>
```
5. When you create resource files for the new activity, give them all a prefix that identifies the activity that uses
them, for example `foo_layout1.xml`.
6. Add a README.md file in the new subdirectory.