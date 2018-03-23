/*
 * Copyright 2018 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.arcgisruntime.toolkit.test.compass;

import java.util.concurrent.CancellationException;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.esri.arcgisruntime.ArcGISRuntimeException;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.GeoView;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.toolkit.compass.Compass;
import com.esri.arcgisruntime.toolkit.test.MapOrSceneDialogFragment;
import com.esri.arcgisruntime.toolkit.test.R;
import com.esri.arcgisruntime.toolkit.test.NumberDecimalDialogFragment;

/**
 * TODO
 */
public final class CompassTestActivity extends AppCompatActivity implements MapOrSceneDialogFragment.Listener,
    CompassAutoHideDialogFragment.Listener, NumberDecimalDialogFragment.Listener {

  private static final String TAG = CompassTestActivity.class.getSimpleName();

  private boolean mUseMap = true;

  private GeoView mGeoView;

  private ArcGISMap mMap;

  private ArcGISScene mScene;

  private Compass mCompass;

  private int mMenuItemId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Prompt user to select Map or Scene; note setContentView() isn't called till they do so
    new MapOrSceneDialogFragment().show(getSupportFragmentManager(), "MapOrSceneDialog");
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (mGeoView != null) {
      mGeoView.pause();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (mGeoView != null) {
      mGeoView.resume();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mGeoView != null) {
      mGeoView.dispose();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the compass_options menu; this adds items to the action bar
    getMenuInflater().inflate(R.menu.compass_options, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    mMenuItemId = item.getItemId();
    try {
      switch (mMenuItemId) {
        case R.id.action_auto_hide:
          new CompassAutoHideDialogFragment().show(getSupportFragmentManager(), "AutoHideDialog");
          return true;
        case R.id.action_compass_height:
          NumberDecimalDialogFragment.newInstance(
              "Compass Height in DP", mCompass.getCompassHeight()).show(getSupportFragmentManager(), "NumberDialog");
          return true;
        case R.id.action_compass_width:
          NumberDecimalDialogFragment.newInstance(
              "Compass Width in DP", mCompass.getCompassWidth()).show(getSupportFragmentManager(), "NumberDialog");
          return true;
        case R.id.action_add_insets:
//          addInsetsToMapView();
          return true;
        case R.id.action_remove_insets:
//          removeInsetsFromMapView();
          return true;
      }
    } catch (CancellationException e) {
      // CancellationException is thrown if user cancels out of one of the selection dialogs
    }
    return super.onOptionsItemSelected(item);
  }

  // The following methods are callbacks from the dialog fragments that are invoked above

  @Override
  public void onMapOrSceneSpecified(boolean useMap) {
    // Set content view etc
    mUseMap = useMap;
    changeContentView(LayoutOption.REGULAR);

    // Create a Compass and add it to the GeoView (Workflow 1)
    mCompass = new Compass(mGeoView.getContext());
    mCompass.addToGeoView(mGeoView);
  }

  @Override
  public void onCompassAutoHideSpecified(boolean autoHide) {
    mCompass.setAutoHide(autoHide);
  }

  @Override
  public void onNumberDecimalSpecified(float number) {
    switch (mMenuItemId) {
      case R.id.action_compass_height:
        mCompass.setCompassHeight(number);
        break;
      case R.id.action_compass_width:
        mCompass.setCompassWidth(number);
        break;
    }
  }

  /**
   * Changes the activity content view, finds the GeoView in the new content view and binds adds/binds a Compass to it.
   *
   * @param layoutOption indicates the type of layout to be displayed
   */
  private void changeContentView(LayoutOption layoutOption) {
    // Set the content view
    switch (layoutOption) {
      case REGULAR:
        setContentView(mUseMap ? R.layout.compass_regular_mapview : R.layout.compass_regular_sceneview);
        break;
      case CUSTOM1:
        setContentView(mUseMap ? R.layout.compass_custom1_mapview : R.layout.compass_custom1_sceneview);
        break;
      case CUSTOM2:
        setContentView(mUseMap ? R.layout.compass_custom2_mapview : R.layout.compass_custom2_sceneview);
        break;
    }

    // If we already have a GeoView, tell it to release resources
    if (mGeoView != null) {
//      mGeoView.dispose();//TODO: calling dispose() on SceneView seems to cuase fuzzy text
    }
    final GeoView oldGeoView = mGeoView;//TODO: calling dispose() on SceneView seems to cuase fuzzy text

    // Find the GeoView in the new content view and set the map/scene on it; this loads the map/scene
    if (mUseMap) {
      MapView mapView = findViewById(R.id.mapview);
      mGeoView = mapView;
      mMap = new ArcGISMap(Basemap.createLightGrayCanvas());
      mapView.setMap(mMap);

      // Setting the map on the MapView causes the map to be loaded; set a DoneLoading listener
      mMap.addDoneLoadingListener(new Runnable() {
        @Override
        public void run() {
          // Log error information if the map's not loaded successfully
          if (mMap.getLoadStatus() != LoadStatus.LOADED) {
            logLoadError(mMap.getLoadError());
          }
        }
      });
    } else {
      SceneView sceneView = findViewById(R.id.sceneview);
      mGeoView = sceneView;
      mScene = new ArcGISScene(Basemap.createLightGrayCanvas());
      sceneView.setScene(mScene);

      // Setting the scene on the SceneView causes the scene to be loaded; set a DoneLoading listener
      mScene.addDoneLoadingListener(new Runnable() {
        @Override
        public void run() {
          // Log error information if the map's not loaded successfully
          if (mScene.getLoadStatus() != LoadStatus.LOADED) {
            logLoadError(mScene.getLoadError());
          }
          if (oldGeoView != null) {
            oldGeoView.dispose();
          }
        }
      });
    }

    // Find the buttons used to select different layouts and set listeners on them
    Button button = findViewById(R.id.regular_layout_button);
    button.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        // In the 'regular' layout a new Compass is added to the MapView (Workflow 1)
        changeContentView(LayoutOption.REGULAR);
        mCompass = new Compass(mGeoView.getContext());
        mCompass.addToGeoView(mGeoView);
      }
    });
    button = findViewById(R.id.custom1_layout_button);
    button.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        // In Custom Layout 1 the Compass is overlayed on top of the GeoView and bound to it using Workflow 2
        changeContentView(LayoutOption.CUSTOM1);
        mCompass = findViewById(R.id.compass);
        mCompass.bindTo(mGeoView);
      }
    });
    button = findViewById(R.id.custom2_layout_button);
    button.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        // In Custom Layout 2 the Compass is displayed separate from the GeoView and bound to it using Workflow 2
        changeContentView(LayoutOption.CUSTOM2);
        mCompass = findViewById(R.id.compass);
        mCompass.bindTo(mGeoView);
      }
    });
  }

  /**
   * Writes information about a load error to the log.
   *
   * @param loadError the load error
   */
  private void logLoadError(ArcGISRuntimeException loadError) {
    if (loadError != null) {
      Log.e(TAG, String.format("loadError.getMessage: %s", loadError.getMessage()));
      Log.e(TAG, String.format("loadError.getAdditionalMessage: %s", loadError.getAdditionalMessage()));
      Log.e(TAG, String.format("loadError.getErrorDomain: %s", loadError.getErrorDomain()));
      Log.e(TAG, "loadError.getErrorCode: " + loadError.getErrorCode());
      if (loadError.getCause() != null) {
        Log.e(TAG, String.format("loadError.getCause().getMessage: %s", loadError.getCause().getMessage()));
        loadError.getCause().printStackTrace();
      }
    }
  }

  /**
   * Represents the type of layout to be displayed.
   */
  private enum LayoutOption {
    /**
     * In the 'regular' layout a new Compass is added to the GeoView (Workflow 1).
     */
    REGULAR,

    /**
     * In Custom Layout 1 the Compass is overlayed on top of the GeoView and bound to it using Workflow 2.
     */
    CUSTOM1,

    /**
     * TIn Custom Layout 2 the Compass is displayed separate from the GeoView and bound to it using Workflow 2.
     */
    CUSTOM2
  }

}