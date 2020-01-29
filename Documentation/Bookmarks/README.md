# Bookmarks

The Bookmarks component will show the list of bookmarks in a RecyclerView and allows the user to select
a bookmark and perform some action.

## Workflow

The app can define the `BookmarkView` in its view hierarchy, as `BookmarkView` extends FrameLayout. The app will
have to set the `bookmarks` property on the `BookmarkView` with the map's bookmarks. To handle the event when the 
user taps on an item in the list, the app will have to implement `BookmarkView.onItemClickListener` interface.

Here is an example XML code that shows the BookMarkView below a MapView and the BookmarkView's bookmarks attribute 
being bound to `map.bookmarks` via mapViewModel's bookmarks property:

```
  <androidx.constraintlayout.widget.ConstraintLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent">

        <com.esri.arcgisruntime.mapping.view.MapView
                android:id="@+id/mapView"
                android:layout_width="0dp"
                android:layout_height="0dp"
                app:layout_constraintBottom_toTopOf="@id/guideline"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toTopOf="parent"
                app:map="@{mapViewModel.map}">
        </com.esri.arcgisruntime.mapping.view.MapView>

        <com.esri.arcgisruntime.toolkit.bookmark.BookmarkView
                android:id="@+id/bookmarkView"
                android:layout_width="0dp"
                android:layout_height="0dp"
                app:bookmarks="@{mapViewModel.bookmarks}"
                app:layout_constraintBottom_toBottomOf="parent"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toBottomOf="@id/guideline" />

        <androidx.constraintlayout.widget.Guideline
                android:id="@+id/guideline"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                app:layout_constraintGuide_percent="0.5" />

    </androidx.constraintlayout.widget.ConstraintLayout>
```

Here is example Kotlin code to set the activity that implements the BookmarkView's onItemClickListener interface as the onItemClickListener
and implementing the `onItemClick()`

```
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
