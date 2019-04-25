/*
 * Copyright 2017 Esri
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
package com.esri.arcgisruntime.toolkit.test.scalebar;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.esri.arcgisruntime.ArcGISRuntimeException;
import com.esri.arcgisruntime.UnitSystem;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.toolkit.scalebar.Scalebar;
import com.esri.arcgisruntime.toolkit.scalebar.style.Style;
import com.esri.arcgisruntime.toolkit.test.NumberDialogFragment;
import com.esri.arcgisruntime.toolkit.test.R;

import java.util.concurrent.CancellationException;

/**
 * The Scalebar test activity.
 */
public final class ScalebarTestActivity extends AppCompatActivity implements ScalebarStyleDialogFragment.Listener,
    ScalebarAlignmentDialogFragment.Listener, ScalebarUnitSystemDialogFragment.Listener,
    ScalebarColorDialogFragment.Listener, ScalebarTypefaceDialogFragment.Listener, NumberDialogFragment.Listener,
    ScalebarBasemapDialogFragment.Listener {

  private static final String TAG = ScalebarTestActivity.class.getSimpleName();

  private MapView mMapView;

  private ArcGISMap mMap;

  private Scalebar mScalebar;

  private int mMenuItemId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Create a map containing just a Basemap
    mMap = new ArcGISMap(Basemap.createStreetsVector());

    // Set the content view, find the MapView within it and set the map created above on the MapView
    changeContentView(R.layout.scalebar_regular, R.string.scalebar_message_regular);

    // Setting the map on the MapView causes the map to be loaded; set a DoneLoading listener
    mMap.addDoneLoadingListener(new Runnable() {
      @Override
      public void run() {
        // Log error information if the map's not loaded successfully
        if (mMap.getLoadStatus() != LoadStatus.LOADED) {
          logLoadError(mMap.getLoadError());
        }

        // Create a Scalebar and add it to the MapView (Workflow 1)
        mScalebar = new Scalebar(mMapView.getContext());
        mScalebar.addToMapView(mMapView);
      }
    });
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (mMapView != null) {
      mMapView.pause();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (mMapView != null) {
      mMapView.resume();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mMapView != null) {
      mMapView.dispose();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the scalebar_options menu; this adds items to the action bar
    getMenuInflater().inflate(R.menu.scalebar_options, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    mMenuItemId = item.getItemId();
    try {
      switch (mMenuItemId) {
        case R.id.action_style:
          new ScalebarStyleDialogFragment().show(getSupportFragmentManager(), "StyleDialog");
          return true;
        case R.id.action_alignment:
          new ScalebarAlignmentDialogFragment().show(getSupportFragmentManager(), "AlignmentDialog");
          return true;
        case R.id.action_unit_system:
          new ScalebarUnitSystemDialogFragment().show(getSupportFragmentManager(), "UnitSystemDialog");
          return true;
        case R.id.action_fill_color:
        case R.id.action_alternate_fill_color:
        case R.id.action_line_color:
        case R.id.action_shadow_color:
        case R.id.action_text_color:
        case R.id.action_text_shadow_color:
          new ScalebarColorDialogFragment().show(getSupportFragmentManager(), "ColorDialog");
          return true;
        case R.id.action_typeface:
          new ScalebarTypefaceDialogFragment().show(getSupportFragmentManager(), "TypefaceDialog");
          return true;
        case R.id.action_text_size:
          NumberDialogFragment.newInstance(
              "Text Size in DP", mScalebar.getTextSizeDp()).show(getSupportFragmentManager(), "NumberDialog");
          return true;
        case R.id.action_bar_height:
          NumberDialogFragment.newInstance(
              "Bar Height in DP", mScalebar.getBarHeightDp()).show(getSupportFragmentManager(), "NumberDialog");
          return true;
        case R.id.action_add_insets:
          addInsetsToMapView();
          return true;
        case R.id.action_remove_insets:
          removeInsetsFromMapView();
          return true;
        case R.id.action_change_basemap:
          new ScalebarBasemapDialogFragment().show(getSupportFragmentManager(), "BasemapDialog");
          return true;
      }
    } catch (CancellationException e) {
      // CancellationException is thrown if user cancels out of one of the selection dialogs
    }
    return super.onOptionsItemSelected(item);
  }

  // The following methods are callbacks from the dialog fragments that are invoked above

  @Override
  public void onScalebarStyleSpecified(Style style) {
    mScalebar.setStyle(style);
  }

  @Override
  public void onScalebarAlignmentSpecified(Scalebar.Alignment alignment) {
    mScalebar.setAlignment(alignment);
  }

  @Override
  public void onScalebarUnitSystemSpecified(UnitSystem unitSystem) {
    mScalebar.setUnitSystem(unitSystem);
  }

  @Override
  public void onScalebarColorSpecified(int color) {
    switch (mMenuItemId) {
      case R.id.action_fill_color:
        mScalebar.setFillColor(color);
        break;
      case R.id.action_alternate_fill_color:
        mScalebar.setAlternateFillColor(color);
        break;
      case R.id.action_line_color:
        mScalebar.setLineColor(color);
        break;
      case R.id.action_shadow_color:
        mScalebar.setShadowColor(color);
        break;
      case R.id.action_text_color:
        mScalebar.setTextColor(color);
        break;
      case R.id.action_text_shadow_color:
        mScalebar.setTextShadowColor(color);
        break;
    }
  }

  @Override
  public void onScalebarTypefaceSpecified(Typeface typeface) {
    mScalebar.setTypeface(typeface);
  }

  @Override
  public void onNumberSpecified(int number) {
    switch (mMenuItemId) {
      case R.id.action_text_size:
        mScalebar.setTextSizeDp(number);
        break;
      case R.id.action_bar_height:
        mScalebar.setBarHeightDp(number);
        break;
    }
  }

  @Override
  public void onBasemapSpecified(Basemap basemap) {
    mMap = new ArcGISMap(basemap);
    mMapView.setMap(mMap);
  }

  /**
   * Changes the activity content view, finds the MapView in the new content view and binds adds/binds a scalebar to it.
   *
   * @param layoutId        resource ID of the layout to set as the content view
   * @param messageStringId resource ID of a string to display in the 'message' field
   */
  private void changeContentView(int layoutId, int messageStringId) {
    // Set the content view
    setContentView(layoutId);

    // Display the given string in the 'message' field
    TextView message = findViewById(R.id.message);
    message.setText(messageStringId);

    // If we already have a MapView, tell it to release resources
    if (mMapView != null) {
      mMapView.dispose();
    }

    // Find the MapView in the new content view and set the map on it; this loads the map
    mMapView = findViewById(R.id.mapview);
    mMapView.setMap(mMap);

    // Find the buttons used to select different layouts and set listeners on them
    Button button = findViewById(R.id.regular_layout_button);
    button.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        // In the 'regular' layout a new Scalebar is added to the MapView (Workflow 1)
        changeContentView(R.layout.scalebar_regular, R.string.scalebar_message_regular);
        mScalebar = new Scalebar(mMapView.getContext());
        mScalebar.addToMapView(mMapView);
      }
    });
    button = findViewById(R.id.custom1_layout_button);
    button.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        // In Custom Layout 1 the Scalebar is overlayed on top of the MapView and bound to it using Workflow 2
        changeContentView(R.layout.scalebar_custom1, R.string.scalebar_message_custom1);
        mScalebar = findViewById(R.id.scalebar);
        mScalebar.bindTo(mMapView);
      }
    });
    button = findViewById(R.id.custom2_layout_button);
    button.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        // In Custom Layout 2 the Scalebar is displayed separate from the MapView and bound to it using Workflow 2
        changeContentView(R.layout.scalebar_custom2, R.string.scalebar_message_custom2);
        mScalebar = findViewById(R.id.scalebar);
        mScalebar.bindTo(mMapView);
      }
    });
  }

  /**
   * Makes the "inset" views that overlay the 4 edges of the MapView visible and tells the MapView about them. Insets
   * controls the active visible area, instructing the MapView to ignore parts that are obstructed by overlaid UI
   * elements.
   */
  private void addInsetsToMapView() {
    RelativeLayout leftInset = findViewById(R.id.left_inset);
    RelativeLayout topInset = findViewById(R.id.top_inset);
    RelativeLayout rightInset = findViewById(R.id.right_inset);
    RelativeLayout bottomInset = findViewById(R.id.bottom_inset);
    if (leftInset != null && topInset != null && rightInset != null && bottomInset != null) {
      leftInset.setVisibility(View.VISIBLE);
      topInset.setVisibility(View.VISIBLE);
      rightInset.setVisibility(View.VISIBLE);
      bottomInset.setVisibility(View.VISIBLE);
      float density = mMapView.getContext().getResources().getDisplayMetrics().density;
      mMapView.setViewInsets(leftInset.getWidth() / density, topInset.getHeight() / density,
          rightInset.getWidth() / density, bottomInset.getHeight() / density);
    }
  }

  /**
   * Makes the "inset" views that overlay the 4 edges of the MapView invisible and tells the MapView there are no
   * insets.
   */
  private void removeInsetsFromMapView() {
    RelativeLayout leftInset = findViewById(R.id.left_inset);
    RelativeLayout topInset = findViewById(R.id.top_inset);
    RelativeLayout rightInset = findViewById(R.id.right_inset);
    RelativeLayout bottomInset = findViewById(R.id.bottom_inset);
    if (leftInset != null && topInset != null && rightInset != null && bottomInset != null) {
      leftInset.setVisibility(View.INVISIBLE);
      topInset.setVisibility(View.INVISIBLE);
      rightInset.setVisibility(View.INVISIBLE);
      bottomInset.setVisibility(View.INVISIBLE);
      mMapView.setViewInsets(0, 0, 0, 0);
    }
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
