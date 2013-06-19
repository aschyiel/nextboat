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

import org.aschyiel.nextboat.DeparturesList;
import org.aschyiel.nextboat.DownloadWebPage;
import org.aschyiel.nextboat.FerryObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NextBoat extends Activity
{
  public static final String TAG = "NextBoat";

  //..contains both destinations..
  private String mFerryScheduleHtmlString;

  /**
  * The ferry's current destination.
  *
  * For Example:
  * "Leaving Seattle"
  * "Leaving Bainbridge Island"
  */
  private String _destination;

  /**
  * The two terminals connecting the ferry route.
  */
  private String _destinationA;
  private String _destinationB;
  
  private Boolean mTheme = false;
  
  private Boolean mDisableAutoLocation = false;
  
  private String mVesselWatchUrl;
  private String mFerryScheduleUrl;
  private String mBulletinRegex;
  
  private String mSimpleDateFormatString = "hh:mm a, MMM-dd";
  
  private ArrayList<FerryObject> mFerryObjectArray;
  private FerryObject mTargetBoat;
  private FerryObject mNextBoat;
  
  private int mTargetBoatIndex;
  
  private TextView textView;
  private TextView textViewLabel;
  private TextView textViewChecked;
  private TextView textViewDownloaded;
  private TextView textViewDeparting;
  
  private SharedPreferences mSharedPreferences;
  
  private Button mButtonOne;
  private Button mButtonTwo;
  private Button mButtonThree;

  private TextView mBulletinTextView;
  private int mBulletinLength;

  //---------------------------------
  //
  // Public/Overriden Methods
  //
  //---------------------------------

  /** Called when the activity is first created. */
  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );
    setContentView( R.layout.main );
    
    mFerryObjectArray = new ArrayList<FerryObject>();
    
    //..init..
    _destination = _destinationA;
    
    _initView();
  }

  @Override
  public boolean onCreateOptionsMenu( Menu menu )
  {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate( R.menu.my_menu, menu );
    return true;
  }

  /**
  * The user selected something from the nextboat options menu.
  */
  @Override
  public boolean onOptionsItemSelected( MenuItem item ) {
    // Handle item selection
    switch ( item.getItemId() ) {
    case R.id.id_refresh:
      refreshFxn();
      return true;
    case R.id.id_view_all:
      _showDepartures();
      return true;
    case R.id.id_go_online:
      _showScheduleWebPage();
      return true;
    case R.id.id_ferry_cam:
      _showFerryCam();
      return true;
    case R.id.id_vessel_watch:
      _showVesselWatch();
      return true;
    case R.id.id_go_alert:
      _showBulletinPage();
      return true;
    case R.id.id_view_alert_bulletin:
      viewAlertBulletinDialog();
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  //..note: onResume() gets called immediately after onCreate..
  @Override
  protected void onResume()
  {
    super.onResume();
    
    _setupView();
    
    if ( isDownloadNeeded() )
    {
      refreshFxn();
      return;
    }
    
    handleAutoLocation();
    textView.setText( getNextFerry() );
    setLastCheckedFxn();
    readFerryBulletin();
  }

  //---------------------------------
  //
  // Private Methods
  //
  //---------------------------------

  /**
  * Returns true if the nextboat app needs to hit WSDOT
  * for an updated copy of the schedule.
  */
  private boolean isDownloadNeeded()
  {
    // TODO
    // Ask sqlite how old we are, and ask WSDOT how old thier copy is.
    return false;
  }


  private void setNextBoatDestinationText()
  {
  }

  private void refreshFxn()
  {
    if ( isDownloadNeeded() )
    {
      _download();
    }
    
    handleAutoLocation();
    
    //..and re-read our file..
    initNextFerry();

    //..actual refresh part..
    textView.setText( getNextFerry() );
    setNextBoatDestinationText();
    setLastCheckedFxn();
    readFerryBulletin();
  }

  /**
  * Returns true if today is Saturday or Sunday.
  */
  public boolean isWeekEnd()
  {
    return 1 == ( Calendar.getInstance().get( Calendar.DAY_OF_WEEK ) % 6 );
  }

    
  /**
  * Returns true if our deice has access to the internet.
  *
  * @see http://www.androidpeople.com/android-how-to-check-network-statusboth-wifi-and-mobile-3g/
  */
  private Boolean hasInternets()
  {
    ConnectivityManager connMan = 
            (ConnectivityManager)this.getSystemService( Context.CONNECTIVITY_SERVICE );
    NetworkInfo wifi =   connMan.getNetworkInfo( ConnectivityManager.TYPE_WIFI );
    NetworkInfo mobile = connMan.getNetworkInfo( ConnectivityManager.TYPE_MOBILE );
    return ( wifi.isAvailable() || mobile.isAvailable() );
  }

  /**
  * Initialize the view and prepare the component references.
  */
  private void _initView()
  {
    textView          = (TextView) findViewById( R.id.id_text );
    textViewLabel     = (TextView) findViewById( R.id.id_text_label );
    textViewDeparting = (TextView) findViewById( R.id.id_text_departing );
    
    textViewChecked    = (TextView) findViewById( R.id.id_text_checked );
    textViewDownloaded = (TextView) findViewById( R.id.id_text_download );
    
    ScrollView scrollView = (ScrollView) findViewById( R.id.id_scrollview );
    mBulletinTextView     = (TextView) scrollView.findViewById( R.id.id_bulletin );
    
    mButtonOne   = (Button) findViewById( R.id.id_button_one );
    mButtonTwo   = (Button) findViewById( R.id.id_button_two );
    mButtonThree = (Button) findViewById( R.id.id_button_three );
    
    mButtonOne.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        _showDepartures();
      }
    });
    
    mButtonTwo.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        _showFerryCam();
      }
    });
    
    mButtonThree.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        _showVesselWatch();
      }
    });

    _setupView();
  }
   
  /**
  * Re-configure the view;
  * To be called after the view has been initialized.
  *
  * ie. The app has resumed, etc.
  */
  private void _setupView()
  {
    _setupTextColour();
    _setupBackgroundColour();

    textViewLabel.setText(  "Next Boat : \n" + _destination );
  }

  /**
  * Set the text-colour for all of the view components.
  */
  private void _setupTextColour()
  {
    int textColour = Color.GREEN;
    textView.setTextColor(           textColour );
    textViewLabel.setTextColor(      textColour );
    textViewChecked.setTextColor(    textColour );
    textViewDownloaded.setTextColor( textColour );
    textViewDeparting.setTextColor(  textColour );
    mBulletinTextView.setTextColor(  textColour );
  }
  
  /**
  * Set the view's background colour.
  */
  private void _setupBackgroundColour()
  {
    ( (RelativeLayout) findViewById( R.id.id_main_relative_layout ) )
        .setBackgroundColor( Color.BLACK );
  }

  /**
  * Bring up the WSDOT Vessel Watch webpage.
  *
  * @see gov.wa.wsdot.android.wsdot/.SplashScreen
  */
  private void _showVesselWatch()
  {
    // TODO: Consider directly launching the WSDOT app.
    startActivity( new Intent(
        Intent.ACTION_VIEW,
        Uri.parse( mVesselWatchUrl ) ) );
  }

  private String manuallyGetFerryDestination()
  {
      //..set to manual..
      final int n = Integer.parseInt( 
          mSharedPreferences.getString(
              "ManualDestination",
              "0") );
      
      return (0 == n) ? _destinationA : _destinationB;
  }
    
    final static String PREFERENCE_LAST_PORT = "PreferenceLastPort";
    
    //
    private void saveDestination( String pDestination )
    {
        if ( !pDestination.equals( getSavedDestination() )  )
        {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString( PREFERENCE_LAST_PORT, pDestination );
            editor.commit();    //..save..
        }
    }
    
    private String getSavedDestination()
    {
        //..default to portOne..
        final String zPort = mSharedPreferences.getString(  
                    PREFERENCE_LAST_PORT,     
                    _destinationA );
        
        return zPort;
    }
    
    //..
    private void handleAutoLocation()
    {
        final String zPreviousDestination = _destination;
        
        final String zCurrentDestination = (mDisableAutoLocation) ? 
                manuallyGetFerryDestination() : automagicallyFindDestination();
        
        saveDestination( zCurrentDestination );
        
        //..set global..
        _destination = zCurrentDestination;
            
        //..meaning we chose something new..
        if ( !zCurrentDestination.equals( zPreviousDestination ) )
        {
            initNextFerry();
        }
    }
    
    private String automagicallyFindDestination()
    {
        //..http://www.damonkohler.com/2009/02/android-recipes.html
        
        LocationManager locationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        Location loc = locationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
        if (loc == null)
        {
          // Fall back to coarse location.
          loc = locationManager.getLastKnownLocation( LocationManager.NETWORK_PROVIDER );
        }
        if ( loc != null )
        {
            
            Location location1 = new Location("providerName");
            location1.setLatitude( mPortOneLat );
            location1.setLongitude( mPortOneLong );
            
            Location location2 = new Location("providerName");
            location2.setLatitude( mPortTwoLat );
            location2.setLongitude( mPortTwoLong );
            
            final float distance1 = loc.distanceTo( location1 );
            final float distance2 = loc.distanceTo( location2 );
            
            //..if the float comparison is negative, that means portTwo is closer..            
            if ( java.lang.Float.compare( distance2 , distance1 ) < 0 )
            {
                return _destinationB;
            }
            else if ( java.lang.Float.compare( distance2 , distance1 ) > 0 )
            {
                return _destinationA;
            }
            else
            {
                return getSavedDestination();
            }
        }
        else
        {
            return getSavedDestination();
        }
    }
    

  /**
  * Download the latest ferry schedule(s).
  */
  private void _download()
  {
    if ( hasInternets() )
    {
      Log.w( TAG, "Failed to download the latest schedule, "+
          "we're not connected to the internet!" );
      return;
    }

    DownloadWebPage downloader = new DownloadWebPage( this, mFerryScheduleUrl, whichFile() );
    downloader.start();
  }

  /**
  * Show the live ferry-cam image to the user.
  */
  private void _showFerryCam()
  {
    String link = ( _destination.equals( _destinationA ) )?
        getDestinationAFerryCamUrl() : getDestinationBFerryCamUrl();

    startActivity( new Intent(
        Intent.ACTION_VIEW,
        Uri.parse( link ) ) );
  }

  /**
  * Bring up a schedule listing all of the ferry departures for our current destination.
  */
  private void _showDepartures()
  {
    Intent departures = new Intent( this, DeparturesList.class );

    departures.putExtra( "theList", _getScheduleAsCsv() );
    departures.putExtra( "theIndex",
        Integer.toString( mFerryObjectArray.indexOf( mNextBoat ) ) );    //..can be -1..
    departures.putExtra( "theTitle",
        _destination +
            ( isWeekEnd() ) ? " (Weekend)" : " (Weekday)" );

    startActivity( departures );
  }

  /**
  * Serialize the ferry schedule as a CSV string.
  *
  * @return (String)
  */
  private String _getScheduleAsCsv()
  {
    return android.text.TextUtils.join( ',', mFerryObjectArray );
  }

  /**
  * Open the ferry schedule web page.
  */
  private void _showScheduleWebPage()
  {
    startActivity( new Intent(
        Intent.ACTION_VIEW,
        Uri.parse( mFerryScheduleUrl ) ) );
  }
  
  /**
  * Open the ferry alerts/bulletins web page.
  */
  private void _showBulletinPage()
  {
    startActivity( new Intent (
        Intent.ACTION_VIEW,
        Uri.parse( getString( R.string.alert_bulletin_url ) ) ) );
  }

  /**
  * Update the view's "next-sailing" label.
  *
  * @private
  * @param next (FerryObject) is the next sailing.
  * @return void
  */
  private void _displayNextSailing( FerryObject next )
  {
    textViewDeparting.setText( ( null == next )?
        '' : next.prettyPrintBoat()+ " departs in : " + next.printWhenBoatLeaves() );
  }

  //
  //..parse our html file and get the next ferry that is going to leave..
  //
  //..perhaps this should be it's own thread ?..
  //
  private String getNextFerry()
  {
    //..if empty, re-init..
    if ( mFerryScheduleHtmlString == null )
    {
      initNextFerry();
    }
    //..TODO compare current time and return the right one ..
    if ( mFerryObjectArray.size() == 0 )
    {
      _displayNextSailing( false );
      return "";
    }
    
    //..get next time..
    ArrayList<FerryObject> localFerryList = new ArrayList<FerryObject>();
    Date now = Calendar.getInstance().getTime();
    for ( int i=0; i < mFerryObjectArray.size(); i++ )
    {
      //..if we could catch the boat..
      if ( now.before( mFerryObjectArray.get(i).getDate() ) )
      {
        localFerryList.add( mFerryObjectArray.get( i ) );
      }
    }
    
    FerryObject zNextBoat = new FerryObject();
    if ( localFerryList.size() <= 0 )
    {
      mNextBoat = null;
      _displayNextSailing( null );
      return "No Next Boat!";
    }
    else if ( localFerryList.get(0) != null )
    {
      zNextBoat = localFerryList.get(0);
      mNextBoat = zNextBoat;
      _displayNextSailing( mNextBoat );
      return zNextBoat.prettyPrint();
    }
    else
    {
      _displayNextSailing( false );
      return "";
    }
  }

  /**
  * Parse the WSDOT web page and extract the schedule related content.
  *
  * For Example:
  * AM/PM, HH:MM, and the vessel's name.
  *
  * @param html (String) is the WSDOT web page HTML contents to search within.
  * @return (ArrayList<String>) of parsed contents.
  */
  private ArrayList htmlToListViaRegex( String html )
  {
    Pattern pattern = Pattern.compile( mRegex );
    Matcher matcher = pattern.matcher( html );
    ArrayList parsed = new ArrayList();
    while ( matcher.find() )
    {
      for ( var i = 1; i < 5; i++ ) {
        String content = matcher.group( i );
        if ( null != content ) {
          parsed.add( ( content.equals( 'Midnight' ) ||
            content.equals( 'Noon' ) )?
              '12:00' : content );
        }
      }
    }
    return parsed;
  }

  //
  //..selectively loads html,..
  //..then assembles a list of ferryObjects..
  //
  private void initNextFerry()
  {
    //..clear variables..
    mFerryObjectArray.clear();
    mFerryScheduleHtmlString = "";
    
    //..set our local var mFerryScheduleHtmlString..
    if ( loadFerrySchedule() )
    {
      //..reduce htmlString to applicable destination..
      String htmlSchedule = getAppropriateHtml();
      
      ArrayList li = new ArrayList();
      li = htmlToListViaRegex( htmlSchedule );
      
      //..TODO..remove debug log..
      
      ArrayList<FerryObject> ki = populateFerryObjectList( li );
      
      mFerryObjectArray = correctFerryListForLateNights( ki );
    }
  }

    //
    //..used to populate mFerryObjectArray with FerryObjects..
    //
    private ArrayList<FerryObject> populateFerryObjectList( ArrayList pLi )
    {
        ArrayList<FerryObject> zList = new ArrayList<FerryObject>();
        int k = pLi.size();
        String aa = "";
        String HHMM = "";
        String boat = "";
        Boolean bNextDay = false;
        for ( int n=0; n < k; n++  )
        {
            //..set var(s)..
            String elem = ( pLi.get( n ) ).toString();
            if ( elem.equals("AM") || elem.equals("PM") )
            {
                //..if we are talking about the next day..
                if ( aa.equals("PM") && elem.equals("AM") )
                {
                    bNextDay = true;
                }
                aa = elem;
            }
            else if ( elem.indexOf(":") > 0 )
            {
                HHMM = elem;
            }
            //..else it is a boat name (our LAST var)..
            else if ( !elem.equals("AM") || !elem.equals("PM") || elem.indexOf(":") == -1 )
            { 
                //
                // Unfortunately I'm really at the mercy of WSDOT and however 
                // they decide to NOT format their schedules.
                //
                // ie. bremerton schedule randomly uses quotes/extra whitespace for the 4:50 Kitsap 
                // sailing which my regex promptly fails on.
                // 
                // -uly, sept2012
                //
                if ( 0 == HHMM.length() ) 
                {
                    Log.w( TAG, "Detected zero length \"HHMM\", skipping..." );
                    continue; 
                }

                boat = elem;
                
                //..create FerryObject..
                FerryObject ferryObject = new FerryObject();
                ferryObject.setTime( HHMM, aa, bNextDay );
                //..TODO..might be null boat..
                ferryObject.setBoat( boat );
                ferryObject.setLeaving( _destination );
                //..append to array..
                zList.add( ferryObject );
            }
        }//..end for..
        
        return zList;
    }

    
  //
  //..reads the NextBoat.html file..
  //..substrings the important parts..
  //..and stores it into the variable mFerryScheduleHtmlString..
  //
  //..returns true on success (we have file)..
  //
  private Boolean loadFerrySchedule()
  {
    return false;
  }

}