//..Downloader.java, uly, june2013..
package org.aschyiel.nextboat;
/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.aschyiel.nextboat.NextBoat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Environment;
import android.content.pm.PackageInfo;
import android.util.Log;
import android.net.Uri;
import android.content.Context;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Process;
import android.os.Looper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import android.widget.Toast;

/**
* The downloader runs in it's own thread, and then tells the main activity to
* do something when it's done.
* 
* @see http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
* @see WordWidget.java from simpleWiktionary example.
* @see BooksUpdater.java from Shelves example.
*/
public class Downloader implements Runnable
{
  /**
  * A reference back to the main activity.
  */
  private static NextBoat _context;
 
  /** The web-page url we want to download stuff from. */
  private String _url;

  /**
  * Our logger tag.
  */
  private static final String TAG = "Downloader";

  /**
  * The callback to execute after a successful download.
  */
  private String _callbackMethodName;

  /**
  * Our downloader thread.
  */
  private Thread _thread;

  /**
  * When true - represents that the thread is currently "stopped".
  */
  private volatile boolean _isStopped;

  //---------------------------------
  //
  // Public Methods.
  //
  //---------------------------------

  /** @constructor */
  public Downloader( NextBoat context, String url, String callbackMethodName )
  {
    _context            = context;
    _url                = url;
    _callbackMethodName = callbackMethodName;
  }

  /**
  * @override Thread#start
  */
  public void start()
  {
    if ( null == _thread )
    {
      _isStopped = false;
      _thread = new Thread( this, "Downloader" );
      _thread.start();
    }
  }

  /**
  * @override Thread#stop
  */
  public void stop()
  {
    if ( null != _thread ) {
      _isStopped = true;
      _thread.interrupt();
      _thread = null;
    }
  }

  /**
  * @override Thread#run
  */
  public void run() {
    Process.setThreadPriority( Process.THREAD_PRIORITY_BACKGROUND );
    while ( !_isStopped )
    {
      try
      {
        _download();
        Thread.sleep( 1000 );
        _isStopped = true;
      } catch ( InterruptedException e ) {
        // Ignore
      }
    }
  }

  /**
  * Download something, and then execute callbacks
  * against the downloaded text-content.
  */
  private void _download()
  {
    String userAgent = _prepareUserAgent( _context );
    try
    {
      content = getUrlContent( _url, userAgent );
    }
    catch ( DownloaderFail fail )
    {
        Log.e( TAG, "DownloaderFail", fail );
    }

    if ( null != content )
    {
      Method callback = _context.getClass().getMethod( _callbackMethodName,
          new Class[]{ String.class } )
      callback.invoke( _context, content );
    }
  }
 
  //---------------------------------
  //
  // Private Methods.
  //
  //---------------------------------

  /**
  * Make sure the user-agent represents us as an android device.
  */
  private void _prepareUserAgent( Context context )
  {
    try
    {
      // Read the package name and version number from the manifest.
      PackageManager manager = context.getPackageManager();
      PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
      sUserAgent = String.format( context.getString( R.string.template_user_agent ),
          info.packageName, info.versionName);
    }
    catch( NameNotFoundException e )
    {
      Log.e(TAG, "Couldn't find package information in PackageManager", e);
    }
  }

  /**
  * Returns the web-page text-content for a given web-page url.
  *
  * @private
  * @synchronized
  * @param (String) url is the web-page to download stuff from.
  * @return (String) is the plain-text contained there-in.
  * @throws DownloaderFail
  */
  private synchronized String getUrlContent( String url ) throws DownloaderFail
  {
    //
    // Create client and set our specific user-agent string
    //

    if ( null == sUserAgent )
    {
        throw new DownloaderFail( "User-Agent string must be prepared" );
    }
    HttpClient client = new DefaultHttpClient();
    HttpGet request   = new HttpGet( url );
    request.setHeader( "User-Agent", sUserAgent );

    String textContent;
    try
    {
      HttpResponse response = client.execute( request );

      // Check if server response is valid
      StatusLine status = response.getStatusLine();
      if ( 200 != status.getStatusCode() )
      {
        throw new DownloaderFail("Invalid response from server: " +
            status.toString());
      }

      // Pull content stream from response
      HttpEntity entity             = response.getEntity();
      InputStream inputStream       = entity.getContent();
      ByteArrayOutputStream content = new ByteArrayOutputStream();

      // Read response into a buffered stream
      int bytesRead = 0;
      byte[] buffer = new byte[ 512 ];
      while ( -1 != ( bytesRead = inputStream.read( buffer ) ) )
      {
        content.write( buffer, 0, bytesRead );
      }

      // Return result from buffered stream
      textContent = new String( content.toByteArray() );
    }
    catch ( IOException e )
    {
      throw new DownloaderFail("Problem communicating with API", e);
    }
    return textContent;
  }

  //---------------------------------
  //
  // Private Inner Classes.
  //
  //---------------------------------

  /**
  * Our very own way of failing.
  * Not exactly sure why it's needed in the first place!
  */
  private class DownloaderFail extends Exception
  {
    public DownloaderFail( String message )
    {
      super( message );
    }
    public DownloaderFail( String message, Throwable throwable)
    {
      super( message, throwable );
    }
  }
}
