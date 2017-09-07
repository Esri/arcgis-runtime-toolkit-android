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
 * Created by alan0001 on 07/09/2017.
 */

public final class ToolkitTestAppMainActivity extends AppCompatActivity {

  private static final String TAG = ToolkitTestAppMainActivity.class.getSimpleName();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.toolkit_main);
    ListView listView = (ListView) findViewById(R.id.toolkit_main_listview);
    listView.setAdapter(new SimpleAdapter(this, getAdapterData(), android.R.layout.simple_list_item_1,
        new String[] { "title" }, new int[] { android.R.id.text1 }));
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Map map = (Map) parent.getItemAtPosition(position);
        Intent intent = (Intent) map.get("intent");
        startActivity(intent);
      }
    });
  }

  private List<Map<String, Object>> getAdapterData() {
    List<Map<String, Object>> adapterData = new ArrayList<>();
    String[] classNames = getResources().getStringArray(R.array.activityClassNames);

    for (String className : classNames) {
      String[] parts = className.split("[.]");
      if (parts.length > 0) {
        String title = parts[parts.length - 1];
        try {
          Intent intent = new Intent(ToolkitTestAppMainActivity.this, Class.forName(className));
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
