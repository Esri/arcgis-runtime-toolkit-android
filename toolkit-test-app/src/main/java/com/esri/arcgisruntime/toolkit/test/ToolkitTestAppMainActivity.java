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
package com.esri.arcgisruntime.toolkit.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main Activity class for the toolkit test app. Displays a list of test activities that can be run.
 *
 * @since 100.1.0
 */
public final class ToolkitTestAppMainActivity extends AppCompatActivity {

  private static final String TAG = ToolkitTestAppMainActivity.class.getSimpleName();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Set the content view and find the ListView within it
    setContentView(R.layout.toolkit_main);
    ListView listView = (ListView) findViewById(R.id.toolkit_main_listview);

    // Set adapter on the ListView
    listView.setAdapter(new SimpleAdapter(this, getAdapterData(), android.R.layout.simple_list_item_1,
        new String[] { "title" }, new int[] { android.R.id.text1 }));

    // Set an onItemClick listener that starts the activity that corresponds with the selected item
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Map map = (Map) parent.getItemAtPosition(position);
        Intent intent = (Intent) map.get("intent");
        startActivity(intent);
      }
    });
  }

  /**
   * Gets adapter data to be set on the ListView. This consists of a Map containing the title (to be displayed in the
   * ListView) and an Intent that can be used to start the activity.
   *
   * @return the adapter data
   * @since 100.1.0
   */
  private List<Map<String, Object>> getAdapterData() {
    List<Map<String, Object>> adapterData = new ArrayList<>();

    // Get the fully-qualified class names of the test activities
    String[] classNames = getResources().getStringArray(R.array.activityClassNames);

    // Loop through the test activities
    for (String className : classNames) {
      String[] parts = className.split("[.]");
      if (parts.length > 0) {
        // Create title
        String title = parts[parts.length - 1];
        try {
          // Create Intent
          Intent intent = new Intent(ToolkitTestAppMainActivity.this, Class.forName(className));

          // Add a Map containing the title and Intent
          Map<String, Object> map = new HashMap<String, Object>();
          map.put("title", title);
          map.put("intent", intent);
          adapterData.add(map);
        } catch (ClassNotFoundException e) {
          Log.e(TAG, "Class not found: " + className);
        }
      }
    }
    return adapterData;
  }
}
