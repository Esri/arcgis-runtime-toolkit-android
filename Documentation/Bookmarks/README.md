# Bookmarks

The Bookmarks component shows the list of a map's bookmarks in a RecyclerView and allows the user to select
a bookmark and perform some action.

## Workflow

The app can define the `BookmarkView` in its view hierarchy, as `BookmarkView` extends FrameLayout. The app 
has to set the `bookmarks` property on the `BookmarkView` with the map's bookmarks. To handle the event when the 
user taps on an item in the list, the app has to implement `BookmarkView.onItemClickListener` interface.

Here is an example XML code that shows the BookMarkView and the BookmarkView's bookmarks attribute
being bound to `map.bookmarks` via mapViewModel's bookmarks property:

```xml
  <data>
        <variable
                name="mapViewModel"
                type="com.esri.arcgisruntime.toolkit.test.bookmark.map.MapViewModel" />
  </data>
  
  
       .....

        <com.esri.arcgisruntime.toolkit.bookmark.BookmarkView
                android:id="@+id/bookmarkView"
                android:layout_width="0dp"
                android:layout_height="0dp"
                app:bookmarks="@{mapViewModel.bookmarks}"
                app:layout_constraintBottom_toBottomOf="parent"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toBottomOf="@id/guideline" />

        .....
```

Here is example Kotlin code to set the activity that implements the BookmarkView's onItemClickListener interface as the 
onItemClickListener and implementing the `onItemClick()`

```kotlin
class BookmarkActivity : AppCompatActivity(), BookmarkView.OnItemClickListener<Bookmark> {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ...
        ...

        bookmarkView.onItemClickListener = this
    }

    override fun onItemClick(item: Bookmark) {
        mapView.setViewpointAsync(item.viewpoint)
    }

```

To see it in action, try out the bookmark test in the [toolkit-test-app](https://github.com/Esri/arcgis-runtime-toolkit-android/tree/master/toolkit-test-app/src/main/java/com/esri/arcgisruntime/toolkit/test)
