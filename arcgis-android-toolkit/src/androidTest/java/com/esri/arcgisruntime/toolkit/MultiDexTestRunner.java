package com.esri.arcgisruntime.toolkit;

import android.os.Bundle;
import android.support.test.runner.AndroidJUnitRunner;
import androidx.multidex.MultiDex;

public class MultiDexTestRunner extends AndroidJUnitRunner {

  @Override public void onCreate(Bundle arguments) {
    MultiDex.install(getTargetContext());
    super.onCreate(arguments);
  }
}
