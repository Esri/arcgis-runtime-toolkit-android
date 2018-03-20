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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

/**
 * TODO
 */
public final class CompassTestActivity extends AppCompatActivity implements MapOrSceneDialogFragment.Listener {

  private static final String TAG = CompassTestActivity.class.getSimpleName();

  private boolean mUseMap = true;

  private GeoView mGeoView;

  private ArcGISMap mMap;

  private ArcGISScene mScene;

  private Compass mCompass;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Prompt user to select Map or Scene; note setContentView() isn't called till they do so
    new MapOrSceneDialogFragment().show(getSupportFragmentManager(), "MapOrSceneDialog");
  }

  @Override
  public void onMapOrSceneSpecified(boolean useMap) {
    // Set content view etc based on whether map or scene was selected
    mUseMap = useMap;
    if (mUseMap) {
      setContentView(R.layout.compass_regular_mapview);
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
      setContentView(R.layout.compass_regular_sceneview);
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
        }
      });
    }

    // Create a Compass and add it to the GeoView (Workflow 1)
    mCompass = new Compass(mGeoView.getContext());
    mCompass.addToGeoView(mGeoView);
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

}