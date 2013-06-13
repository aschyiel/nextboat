/**
 * Copyright (C) 2010  Ulysses Levy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Ulysses Levy
 * @version 1.0
 */

package org.aschyiel.nextboat;

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


public class DownloadWebPage implements Runnable
{
    //..see WordWidget.java from simpleWiktionary example..
    //..see BooksUpdater.java from Shelves example..
    
    public DownloadWebPage(Context pContext, String pUrl, File pFile) {
        mResolver = pContext.getContentResolver();
        mContext = pContext;
        mWebPageUrl = pUrl;
        mFile = pFile;
    }
    
    private final File mFile;
    private static Context mContext;
    private final ContentResolver mResolver;
    
    private String mWebPageUrl;

    private final int HTTP_STATUS_OK = 200;

    private byte[] sBuffer = new byte[512];

    private String sUserAgent = null;
    
    private String foo = null;
    
    private static final String TAG = "DownloadWebPage";

    //..http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
    
    private Thread mThread;
    
    private volatile boolean mStopped;

    public void start() {
        
        if (mThread == null) {
            mStopped = false;
            mThread = new Thread(this, "DownloadWebPage");
            mThread.start();
        }
    }

    public void stop() {
        if (mThread != null) {
            mStopped = true;
            mThread.interrupt();
            mThread = null;
        }
    }

    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (!mStopped) {
            try {
                doIt();
                Thread.sleep(1000);
                mStopped = true;
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }


    public void doIt()
    {
    
        prepareUserAgent( mContext );
    
        //..TODO..check if online..
        try
        {
            foo = getUrlContent( mWebPageUrl );
        }
        catch (ApiException e)
        {
            Log.e( TAG, "ApiException", e );
            
        }
        
        //..if we actually have something to write..
        //..(might get null)..
        if ( ( foo != null) && (foo.length() > 10 ) )
        {
            //..write to file
            WriteSettings( foo );
        }
    }
    
    public static void setContext( Context pContext )
    {
        mContext = pContext;
    }
    
    
    //..http://www.anddev.org/write_to_and_read_from_a_file-t3173.html.. 
    //..http://techdroid.kbeanie.com/2009/08/filewriter-in-android.html
    //..http://developer.android.com/reference/android/content/Context.html#getExternalFilesDir(java.lang.String)
    private void WriteSettings( String pData )
    {
        //..TODO..check SDCard state ?..
        FileWriter fWriter;
        try
        {
            fWriter = new FileWriter( mFile );
            fWriter.write( pData );
            fWriter.flush();
            fWriter.close();
            
        }catch(Exception e){
            Log.e(TAG, "Exception", e);
        }
    }
    
    //..( http://stackoverflow.com/questions/2902689/read-text-file-data-in-android )..
    public static String ReadFile( File pFile )
    {
        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            //..default is 8k..
            final int bufferSize = 16 * 1024;
            BufferedReader br = new BufferedReader( new FileReader( pFile ), bufferSize );
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
            Log.e( TAG, "IOException", e );
        }
        return text.toString();
    }
    
    public void prepareUserAgent(Context context) {
        try {
            // Read package name and version number from manifest
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            sUserAgent = String.format(context.getString(R.string.template_user_agent),
                    info.packageName, info.versionName);
        } catch(NameNotFoundException e) {
            Log.e(TAG, "Couldn't find package information in PackageManager", e);
        }
    }

    protected synchronized String getUrlContent(String url) throws ApiException {
        if (sUserAgent == null) {
            throw new ApiException("User-Agent string must be prepared");
        }

        // Create client and set our specific user-agent string
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", sUserAgent);

        try {
            HttpResponse response = client.execute(request);

            // Check if server response is valid
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != HTTP_STATUS_OK) {
                throw new ApiException("Invalid response from server: " +
                        status.toString());
            }

            // Pull content stream from response
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();

            ByteArrayOutputStream content = new ByteArrayOutputStream();

            // Read response into a buffered stream
            int readBytes = 0;
            while ((readBytes = inputStream.read(sBuffer)) != -1) {
                content.write(sBuffer, 0, readBytes);
            }

            // Return result from buffered stream
            return new String(content.toByteArray());
        } catch (IOException e) {
            /*
            *
            *..TODO: make toast on failed to connect to host
            *
            */
            throw new ApiException("Problem communicating with API", e);
        }
    }
    
    public class ApiException extends Exception {
        public ApiException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public ApiException(String detailMessage) {
            super(detailMessage);
        }
    }

}
