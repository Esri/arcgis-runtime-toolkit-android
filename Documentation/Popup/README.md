# PopupView

The PopupView shows the Popup's attribute list in a Recyclerview, allows user to edit Popup's attribute list and delete the Popup. 


## Workflow 

The app can define the `PopupView` in its view hierarchy, as `PopupView` extends FrameLayout. The app 
has to set the `popup` and `popupManager` property on the `PopupView` with the Popup it wasnts to view/edit. The `PopupView` toolkit component
adhers to the `MVVM` pattern and comes with the `PopupViewModel` that exposes the functionality to delete the `GeoElement`, set the edit mode on
the `PopupView` and other LiveData events that the app can listen to, to provide user with UI notifications.

Here is an example XML code that shows the PopupView and how the PopupView Properties are tied to the PopupViewModel:

```xml
  <data>
        <import type="android.view.View"/>
        <variable
                name="popupViewModel"
                type="com.esri.arcgisruntime.toolkit.popup.PopupViewModel"/>
    </data>

       .....

        <ImageView
                    android:id="@+id/edit"
                    android:layout_width="24dp"
                    android:layout_height="24dp"
                    android:contentDescription="@string/edit"
                    android:onClick="@{() -> popupViewModel.setEditMode(true)}"
                    android:visibility="@{popupViewModel.isPopupInEditMode ? View.GONE : View.VISIBLE}"
                    app:layout_constraintBottom_toBottomOf="parent"
                    app:layout_constraintEnd_toEndOf="parent"
                    app:layout_constraintHorizontal_bias="0.718"
                    app:layout_constraintTop_toTopOf="parent"
                    app:layout_constraintStart_toStartOf="parent"
                    app:layout_constraintVertical_bias="0.475"
                    app:srcCompat="@drawable/ic_pencil_24" />

            <ImageView
                    android:id="@+id/delete"
                    android:layout_width="24dp"
                    android:layout_height="24dp"
                    android:contentDescription="@string/cancel"
                    android:onClick="@{() -> popupViewModel.confirmDeletePopup()}"
                    android:visibility="@{popupViewModel.isPopupInEditMode ? View.GONE : View.VISIBLE}"
                    app:layout_constraintBottom_toBottomOf="parent"
                    app:layout_constraintStart_toEndOf="@id/edit"
                    app:layout_constraintTop_toTopOf="parent"
                    app:layout_constraintEnd_toEndOf="parent"
                    app:layout_constraintVertical_bias="0.475"
                    app:srcCompat="@drawable/ic_trash_24" />

            <ImageView
                    android:id="@+id/ok"
                    android:layout_width="24dp"
                    android:layout_height="24dp"
                    android:contentDescription="@string/ok"
                    android:onClick="@{() -> popupViewModel.savePopupEdits()}"
                    android:visibility="@{popupViewModel.isPopupInEditMode ? View.VISIBLE : View.GONE}"
                    app:layout_constraintBottom_toBottomOf="parent"
                    app:layout_constraintEnd_toEndOf="parent"
                    app:layout_constraintHorizontal_bias="0.718"
                    app:layout_constraintTop_toTopOf="parent"
                    app:layout_constraintStart_toStartOf="parent"
                    app:layout_constraintVertical_bias="0.475"
                    app:srcCompat="@drawable/ic_save_24" />

            <ImageView
                    android:id="@+id/cancel"
                    android:layout_width="24dp"
                    android:layout_height="24dp"
                    android:contentDescription="@string/cancel"
                    android:onClick="@{() -> popupViewModel.confirmCancelEditing()}"
                    android:visibility="@{popupViewModel.isPopupInEditMode ? View.VISIBLE : View.GONE}"
                    app:layout_constraintBottom_toBottomOf="parent"
                    app:layout_constraintStart_toEndOf="@id/ok"
                    app:layout_constraintTop_toTopOf="parent"
                    app:layout_constraintEnd_toEndOf="parent"
                    app:layout_constraintVertical_bias="0.475"
                    app:srcCompat="@drawable/ic_x_24" />

  
  
       .....
            <com.esri.arcgisruntime.toolkit.popup.PopupView
                    android:id="@+id/popupView"
                    android:layout_width="match_parent"
                    android:layout_height="0dp"
                    app:layout_constraintBottom_toBottomOf="parent"
                    app:layout_constraintEnd_toEndOf="parent"
                    app:layout_constraintStart_toStartOf="parent"
                    app:layout_constraintTop_toBottomOf="@id/editLayout"
                    app:popup="@{popupViewModel.popup}"
                    app:popupManager="@{popupViewModel.popupManager}"
                    app:editMode="@{popupViewModel.isPopupInEditMode()}">
            </com.esri.arcgisruntime.toolkit.popup.PopupView>
       .....
```

Here is example Kotlin code to set the `Popup` on the `PopupViewModel` and handle the `Events` raised by the `PopupViewModel`

```kotlin

    .....
     identifyLayerResultsFuture.addDoneListener {
                try {
                    val identifyLayerResult = identifyLayerResultsFuture.get()

                    if (identifyLayerResult.popups.size > 0) {
                        popupViewModel.setPopup(identifyLayerResult.popups[0])

     .....
     
     popupViewModel.showSavingProgressEvent.observeEvent(viewLifecycleOwner) { isShowProgressBar ->
            if (isShowProgressBar) {
                requireActivity().window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                progressBarLayout.visibility = View.VISIBLE
            } else {
                progressBarLayout.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        }                    
     
     .....

     popupViewModel.showSavePopupErrorEvent.observeEvent(viewLifecycleOwner) { errorMessage ->
            showAlertDialog(errorMessage)
        }

        popupViewModel.showDeletePopupErrorEvent.observeEvent(viewLifecycleOwner) { errorMessage ->
            showAlertDialog(errorMessage)
        }

        popupViewModel.confirmCancelPopupEditingEvent.observeEvent(viewLifecycleOwner) {
            showConfirmCancelEditingDialog()
        }

        popupViewModel.confirmDeletePopupEvent.observeEvent(viewLifecycleOwner) {
            showConfirmDeletePopupDialog()
        }


```

To see it in action, try out the Popup test in the [toolkit-test-app](https://github.com/Esri/arcgis-runtime-toolkit-android/tree/master/toolkit-test-app/src/main/java/com/esri/arcgisruntime/toolkit/test)