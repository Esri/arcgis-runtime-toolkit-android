/*
 COPYRIGHT 1995-2010 ESRI

 TRADE SECRETS: ESRI PROPRIETARY AND CONFIDENTIAL
 Unpublished material - all rights reserved under the
 Copyright Laws of the United States.

 For additional information, contact:
 Environmental Systems Research Institute, Inc.
 Attn: Contracts Dept
 380 New York Street
 Redlands, California, USA 92373

 email: contracts@esri.com
 */
package com.esri.android.appframework.util;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskExecutor implements Serializable {
  private static final long serialVersionUID = 1L;
  
  static short POOL_SIZE = 5;

  public static ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);
}
