/*
COPYRIGHT 1995-2012 ESRI

TRADE SECRETS: ESRI PROPRIETARY AND CONFIDENTIAL
Unpublished material - all rights reserved under the
Copyright Laws of the United States.

For additional information, contact:
Environmental Systems Research Institute, Inc.
Attn: Contracts Dept
380 New York Street
Redlands, California, USA 92373

email: contracts@esri.com*/

package com.esri.android.appframework;

import java.io.Serializable;

import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;

public class NAUtil implements Serializable {
  private static final long serialVersionUID = 1L;
  
  public static Polyline decompressGeometry(String compressedGeometry)
  {
    /**
     * returns array of doubles with 2 to 4 values per point:
     * x1, y1[, z1[, m1]], x2, y2[, z2[, m2]],...
     */
    
    if (compressedGeometry == null)
      throw new IllegalArgumentException("Compressed Geometry cannot be null");

    int flags = 0;

    boolean hasMs = false;
    boolean hasZs = false;

    MutableInt nIndex_XY = new MutableInt();
    MutableInt nIndex_Z = new MutableInt();
    MutableInt nIndex_M = new MutableInt();
    double dMultBy_XY = 0;
    double dMultBy_Z = 0;
    double dMultBy_M = 0;

    int firstElement = extractInt(compressedGeometry, nIndex_XY);
    if (firstElement == 0) //post 9.3 format
    {
      int version = extractInt(compressedGeometry, nIndex_XY);
      if(version != 1) 
        throw new IllegalArgumentException("Compressed geometry: Unexpected version.");

      flags = extractInt(compressedGeometry, nIndex_XY);
      if(0 != (0xfffffffc & flags)) 
        throw new IllegalArgumentException("Compressed geometry: Invalid flags.");

      dMultBy_XY = (double)extractInt(compressedGeometry, nIndex_XY);
    }
    else
      dMultBy_XY = (double)firstElement;

    int nLength;
    if (flags == 0)
      nLength = compressedGeometry.length();
    else
    {
      nLength = compressedGeometry.indexOf('|');

      hasZs = (flags & 1) == 1;
      hasMs = (flags & 2) == 2;

      if (hasZs)
      {
        nIndex_Z.setValue(nLength + 1);
        dMultBy_Z = (double)extractInt(compressedGeometry, nIndex_Z);
      }
      if (hasMs) 
      {
        nIndex_M.setValue(compressedGeometry.indexOf('|', nIndex_Z.getValue()) + 1);
        dMultBy_M = (double)extractInt(compressedGeometry, nIndex_M);
      }
    }
    int nLastDiffX = 0;
    int nLastDiffY = 0;
    int nLastDiffZ = 0;
    int nLastDiffM = 0;
    boolean firstPoint = true;
    Polyline res = new Polyline();
    int nDiffX, nX, nDiffY, nY;
    double dX, dY;
    Point p;
    while (nIndex_XY.getValue() != nLength)
    {
      p = null;
      
      //X
      nDiffX = extractInt(compressedGeometry, nIndex_XY);
      nX = nDiffX + nLastDiffX;
      nLastDiffX = nX;
      dX = (double)nX / dMultBy_XY;
      //Y
      nDiffY = extractInt(compressedGeometry, nIndex_XY);
      nY = nDiffY + nLastDiffY;
      nLastDiffY = nY;
      dY = (double)nY / dMultBy_XY;

      if (hasZs) //has Zs
      { //Z
        int nDiffZ = extractInt(compressedGeometry, nIndex_Z);
        int nZ = nDiffZ + nLastDiffZ;
        nLastDiffZ = nZ;
        double dZ = (double)nZ / dMultBy_Z;

        p = new Point(dX, dY, dZ);
      }
      if (hasMs) //has Ms
      { //M
        int nDiffM = extractInt(compressedGeometry, nIndex_M);
        int nM = nDiffM + nLastDiffM;
        nLastDiffM = nM;
        double dM = (double)nM / dMultBy_M;

        if (p == null)
          p = new Point(dX, dY);
        p.setM(dM);
      }
      
      if (p == null)
        p = new Point(dX, dY);
      if(firstPoint) {
        res.startPath(p);
        firstPoint = false;
      }
      else
        res.lineTo(p);
    }
    return res;
  }

  public static boolean checkCompressedGeometriesEquality(String expectedCG, String examinedCG)
  {
    boolean hasZs = isZAwareCompressedGeometry(expectedCG);
    if (hasZs != isZAwareCompressedGeometry(examinedCG))
      return false;

    boolean hasMs = isMAwareCompressedGeometry(expectedCG);
    if (hasMs != isMAwareCompressedGeometry(examinedCG))
      return false;

    Polyline path1 = decompressGeometry(expectedCG);
    Polyline path2 = decompressGeometry(examinedCG);
    
    return path1.equals(path2);
  }
  
  public static boolean isPost93CompressedGeometry(String compressedGeometry)
  {
    MutableInt nIndex_XY = new MutableInt();
    return extractInt(compressedGeometry, nIndex_XY) == 0;
  }
  
  public static boolean isMAwareCompressedGeometry(String compressedGeometry)
  {
    MutableInt nIndex_XY = new MutableInt();
    if (extractInt(compressedGeometry, nIndex_XY) != 0)
      return false;

    extractInt(compressedGeometry, nIndex_XY); //version
    int flags = extractInt(compressedGeometry, nIndex_XY);

    return (flags & 2) == 2;
  }
  
  public static boolean isZAwareCompressedGeometry(String compressedGeometry)
  {
    MutableInt nIndex_XY = new MutableInt();
    if (extractInt(compressedGeometry, nIndex_XY) != 0)
      return false;

    extractInt(compressedGeometry, nIndex_XY); //version
    int flags = extractInt(compressedGeometry, nIndex_XY);

    return (flags & 1) == 1;
  }
  
  private static int extractInt(String src, MutableInt nStartPos)
  {
    // Read one integer from compressed geometry string by using passed position
    // Returns extracted integer, and re-writes nStartPos for the next integer
    boolean bStop = false;
    int result = 0;
    int nCurrentPos = nStartPos.getValue()+1;
    while (!bStop)
    {
      char cCurrent = src.charAt(nCurrentPos);
      if (cCurrent == '+' || cCurrent == '-' || cCurrent == '|')
      {
        if (nCurrentPos != nStartPos.getValue())
        {
          bStop = true;
          continue;
        }
      }
      if (cCurrent >= '0' && cCurrent <= '9')
        result = (result << 5) + Character.getNumericValue(cCurrent) - Character.getNumericValue('0');
      else if (cCurrent >= 'a' && cCurrent <= 'v')
        result = (result << 5) + Character.getNumericValue(cCurrent) - Character.getNumericValue('a') + 10;
      else throw new IllegalArgumentException("Cannot parse CompressedGeometry. Format is incorrect.");
      nCurrentPos++;
      if (nCurrentPos == src.length()) // check overflow
        bStop = true;
    }
    if ((nCurrentPos - nStartPos.getValue()) > 1)
    {
      if (src.charAt(nStartPos.getValue()) == '-')
        result = -result;
      else if (src.charAt(nStartPos.getValue()) != '+')
        throw new IllegalArgumentException("Cannot parse CompressedGeometry. Format is incorrect.");
      nStartPos.setValue(nCurrentPos);
      return result;
    }
    return Integer.MIN_VALUE;
  }
  
  private static class MutableInt {
    private int value;
    
    public MutableInt() {
      
    }
    
    public MutableInt(int value) {
      this.value = value;
    }
    
    public void increment() {
      this.value += 1;
    }
    
    public void decrement() {
      this.value -= 1;
    }
    
    public int getValue() {
      return value;
    }
    
    public void setValue(int value) {
      this.value = value;
    }
  }

}
