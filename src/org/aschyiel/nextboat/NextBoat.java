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

    //..ie
    //.. "Leaving Seattle" ..
    //.. "Leaving Bainbridge Island" ..
    private String mFerryDestination;
    private String mFerryPortOne;
    private String mFerryPortTwo;
    
    private Boolean mTheme = false;
    
    private Boolean mDisableAutoLocation = false;
    
    private String mVesselWatchUrl;
    private String mPortOneFerryCamUrl;
    private String mPortTwoFerryCamUrl;
    private String mFerryScheduleUrl;
    private String mRegex;
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
    private LocationManager mLocationManager;
    
    private Button mButtonOne;
    private Button mButtonTwo;
    private Button mButtonThree;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        //..(http://www.stealthcopter.com/blog/2010/01/android-blurring-and-dimming-background-windows-from-dialogs/ )..
        //setBlurFxn();
        //
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mFerryObjectArray = new ArrayList<FerryObject>();
        
        //..as will be user defined later..
        mSharedPreferences = this.getSharedPreferences( 
            PREFERENCE_NAME, 
            Context.MODE_PRIVATE );
        
        readSharedPreferences( mSharedPreferences );
        
        //..init..
        mFerryDestination = mFerryPortOne;
        
        //..assign view vars (text, buttons, etc)..
        setupViews();
        //..set color options..
        setupViewColors();
        
        //..set rel. layout's background color..
        setupBackgroundColor();
        
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        setNextBoatDestinationText();
    }
    
    //..how often do we download our html files?..
    private int pHourlyUpdateInterval = 6;
    
    //
    //..returns true if file doesn't exist or is out of date..
    //
    private boolean isDownloadNeeded()
    {
        //..if file doesn't exit or if it was last modified 6 hours ago..
        File f = whichFile();
        
        //..if we do not have a file..
        if ( !f.exists() )
        {
            return true;
        }
        //..TODO:..
        //..else the file exists, and it is default 6 hours old..
        else if ( ( f.exists() ) && ( ( System.currentTimeMillis() - f.lastModified() ) > ((long)1000*60*60* pHourlyUpdateInterval ) ) )
        {
            return true;
        }
        //..else the NextBoat.html file exists..
        else
        {
            updateDownloadLatestText( whichFile().lastModified() );
            setLastCheckedFxn();
            return false;
        }
    }
    
    private void setNextBoatDestinationText()
    {
        final String zBoat = ( mTargetBoat != null ) ? "Target Boat" : "Next Boat" ;
        textViewLabel.setText( zBoat + " : \n" + mFerryDestination );
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.id_clear_target_boat:
            clearTargetBoat();
            return true;
        case R.id.id_refresh:
            refreshFxn();
            return true;
        case R.id.id_view_all:
            launchScheduleListFxn();
            return true;
        case R.id.id_go_online:
            goOnlineFxn();
            return true;
        case R.id.id_ferry_cam:
            launchFerryCamFxn();
            return true;
        case R.id.id_download:
            downloadLatest();
            return true;
        case R.id.id_vessel_watch:
            vesselWatchFxn();
            return true;
        case R.id.id_go_alert:
            goOnlineAlertFxn();
            return true;
        case R.id.id_view_alert_bulletin:
            viewAlertBulletinDialog();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void clearTargetBoat()
    {
        mTargetBoat = null;
        mTargetBoatIndex = -1;
        //updateTargetBoatText();
        onResume();
    }
    
    //..note: onResume() gets called immediately after onCreate..
    @Override
    protected void onResume()
    {
        super.onResume();
        
        final Boolean b = writeSharedPreferencesIfNeeded();
        readSharedPreferences( mSharedPreferences );
        
        setupViewColors();
        
        setupBackgroundColor();
        
        if (b || isDownloadNeeded() || PLEASE_REFRESH.equals( textViewDeparting.getText() ) )
        {
            //..shortcut if necessary..
            //downloadLatest();
            refreshFxn();
            return;
        }
        
        handleAutoLocation();
        textView.setText( getNextFerry() );
        setNextBoatDestinationText();
        setLastCheckedFxn();
        readFerryBulletin();
    }
    
    private void refreshFxn()
    {   
        
        final Boolean b = writeSharedPreferencesIfNeeded();
        readSharedPreferences( mSharedPreferences );
        
        if (b || isDownloadNeeded() )
        {
            downloadLatest();
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
    
    //public static final String FILE_NAME = "NextBoat.html";
    public static final String FILE_NAME_WEEK_DAY = "NextBoat_week_day.html";
    public static final String FILE_NAME_WEEK_END = "NextBoat_week_end.html";
    
    public static String whichFileName()
    {
        int dayOfWeekIndex = Calendar.getInstance().get( Calendar.DAY_OF_WEEK );
        //..1 == sunday, 7 == saturday, monday == 2, etc..
        
        //..if weekend schedule..
        if ( (dayOfWeekIndex == 1) || (dayOfWeekIndex == 7) )
        {
            return FILE_NAME_WEEK_END;
        }
        //..else weekday schedule..
        else
        {
            return FILE_NAME_WEEK_DAY;
        }
    }
    public boolean isWeekEnd()
    {
        int dayOfWeekIndex = Calendar.getInstance().get( Calendar.DAY_OF_WEEK );
        //..if weekend schedule..
        if ( (dayOfWeekIndex == 1) || (dayOfWeekIndex == 7) )
        {
            return true;
        }
        //..else weekday schedule..
        else
        {
            return false;
        }
    }
    
    //http://developer.android.com/reference/android/content/Context.html#getExternalFilesDir(java.lang.String)
    public static File whichFile()
    {
        return new File( 
            Environment.getExternalStorageDirectory(), 
            whichFileName() );
    }
    
    //
    //..( http://www.androidpeople.com/android-how-to-check-network-statusboth-wifi-and-mobile-3g/ )..
    //
    private Boolean isInternetAvailable()
    {
        final ConnectivityManager connMan = 
                (ConnectivityManager)this.getSystemService( Context.CONNECTIVITY_SERVICE );
        final NetworkInfo wifi =   connMan.getNetworkInfo( ConnectivityManager.TYPE_WIFI );
        final NetworkInfo mobile = connMan.getNetworkInfo( ConnectivityManager.TYPE_MOBILE );
        if ( wifi.isAvailable() )
        {
            return true;
        }
        else if ( mobile.isAvailable() )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    private boolean mShowTimeStamps = false ; 
    
    //
    //..associate our views from main.xml by id..
    //..also bind click-fxns to buttons..
    //
    private void setupViews()
    {
        
        textView = (TextView) findViewById( R.id.id_text );
        textViewLabel = (TextView) findViewById( R.id.id_text_label );
        textViewDeparting = (TextView) findViewById( R.id.id_text_departing );
        //textViewTargetBoat = (TextView) findViewById( R.id.id_text_target_boat );
        
        textViewChecked = (TextView) findViewById( R.id.id_text_checked );
        textViewDownloaded = (TextView) findViewById( R.id.id_text_download );
        
        //mBulletinTextView = (TextView) findViewById( R.id.id_bulletin );
        ScrollView scrollView = (ScrollView) findViewById( R.id.id_scrollview );
        mBulletinTextView = (TextView) scrollView.findViewById( R.id.id_bulletin );
        
        //textViewDepartingLabel = (TextView) findViewById( R.id.id_lbl_departing );
        
        mButtonOne   = (Button) findViewById( R.id.id_button_one );
        mButtonTwo   = (Button) findViewById( R.id.id_button_two );
        mButtonThree = (Button) findViewById( R.id.id_button_three );
        
        mButtonOne.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                launchScheduleListFxn();
            }
        });
        
        mButtonTwo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                launchFerryCamFxn();
            }
        });
        
        mButtonThree.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vesselWatchFxn();
            }
        });
    }
    
    //..default to white text on black background..
    public final static String PREFERENCE_TEXT_COLOR = "PreferenceTextColor";
    public final static String DEFAULT_TEXT_COLOR = "9";
    public static final String PREFERENCE_BACKGROUND_COLOR = "PreferenceBackgroundColor";
    public static final String DEFAULT_BACKGROUND_COLOR = "0";
    
    //
    //..accept a number between 0 and 10,..
    //..and return a standard color..
    //
    public static int getColorFromSwitch( int pIndex )
    {
        int zColor = 0;
        /*
        0,     1,    2,    3,      4,    5,     6,      7,       8,   9,     10.
        BLACK, BLUE, CYAN, DKGRAY, GRAY, GREEN, LTGRAY, MAGENTA, RED, WHITE, YELLOW
        */
        
        switch ( pIndex )
        {
            case 0:
                zColor = Color.BLACK;
                break;
            case 1:
                zColor = Color.BLUE;
                break;
            case 2:
                zColor = Color.CYAN;
                break;
            case 3:
                zColor = Color.DKGRAY;
                break;
            case 4:
                zColor = Color.GRAY;
                break;
            case 5:
                zColor = Color.GREEN;
                break;
            case 6:
                zColor = Color.LTGRAY;
                break;
            case 7:
                zColor = Color.MAGENTA;
                break;
            case 8:
                zColor = Color.RED;
                break;
            case 9:
                zColor = Color.WHITE;
                break;
            case 10:
                zColor = Color.YELLOW;
                break;
            default:
                zColor = Color.BLACK;
        }
        return zColor;
    }
    
    //..set textColor..
    private void setupViewColors()
    {
        final int zColorStateListIndex = Integer.parseInt(
            mSharedPreferences.getString(  
                PREFERENCE_TEXT_COLOR,     
                DEFAULT_TEXT_COLOR )
        );
        
        int zColor = getColorFromSwitch( zColorStateListIndex );
        
        textView.setTextColor(           zColor );
        textViewLabel.setTextColor(      zColor );
        textViewChecked.setTextColor(    zColor );
        textViewDownloaded.setTextColor( zColor );
        textViewDeparting.setTextColor(  zColor );
        //textViewTargetBoat.setTextColor( zColor );
        mBulletinTextView.setTextColor(  zColor );
        //textViewDepartingLabel.setTextColor( zColor );
        
    }
    
    private void setupBackgroundColor()
    {
        RelativeLayout rl = (RelativeLayout)findViewById( R.id.id_main_relative_layout );
        
        final int zColorIndex =  Integer.parseInt(
            mSharedPreferences.getString(  
                PREFERENCE_BACKGROUND_COLOR,     
                DEFAULT_BACKGROUND_COLOR )
        );
            
        rl.setBackgroundColor( getColorFromSwitch( zColorIndex ) );
    }
    
    private void setBlurFxn()
    {
        Window win = getWindow();
        WindowManager.LayoutParams lp = win.getAttributes();  
        lp.dimAmount=1.0f;  
        win.setAttributes(lp); 
        win.addFlags( WindowManager.LayoutParams.FLAG_BLUR_BEHIND );
        
    }
    
    //..launch WSDOT Veseel Watch..
    private void vesselWatchFxn()
    {
        //..TODO..launch WSDOT app..
        //gov.wa.wsdot.android.wsdot/.SplashScreen
        Intent intent = 
            new Intent(
                Intent.ACTION_VIEW,
                Uri.parse( mVesselWatchUrl ) );
        startActivity( intent );
    }
    
    private String manuallyGetFerryDestination()
    {
        //..set to manual..
        final int n = Integer.parseInt( 
            mSharedPreferences.getString(
                "ManualDestination",
                "0") );
        
        return (0 == n) ? mFerryPortOne : mFerryPortTwo;
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
                    mFerryPortOne );
        
        return zPort;
    }
    
    //..
    private void handleAutoLocation()
    {
        final String zPreviousDestination = mFerryDestination;
        
        final String zCurrentDestination = (mDisableAutoLocation) ? 
                manuallyGetFerryDestination() : automagicallyFindDestination();
        
        
        
        saveDestination( zCurrentDestination );
        
        //..set global..
        mFerryDestination = zCurrentDestination;
            
        //..meaning we chose something new..
        if ( !zCurrentDestination.equals( zPreviousDestination ) )
        {
            clearTargetBoat();
            initNextFerry();
        }
    }
    
    private String automagicallyFindDestination()
    {
        //..http://www.damonkohler.com/2009/02/android-recipes.html
        
        Location loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (loc == null) {
          // Fall back to coarse location.
          loc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
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
                return mFerryPortTwo;
            }
            else if ( java.lang.Float.compare( distance2 , distance1 ) > 0 )
            {
                return mFerryPortOne;
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
    
    private int mFerryMode = 0;
    private Double mPortOneLat;
    private Double mPortOneLong;
    private Double mPortTwoLat;
    private Double mPortTwoLong;
    private boolean mCustomMode;
    
    //..keys to shared preferences..
    public static final String PREFERENCE_THEME = "ThemeKey";
    public static final String PREFERENCE_NAME = "NextBoat_prefs";
    public static final String PREFERENCE_FERRY_SCHEDULE_URL = "FerryScheduleURL";
    public static final String PREFERENCE_BAINBRIDGE_FERRY_CAM_URL = "BainbridgeFerryCamURL";
    public static final String PREFERENCE_SEATTLE_FERRY_CAM_URL = "SeattleFerryCamURL";
    public static final String PREFERENCE_DISABLE_AUTO_LOCATION = "DisableAutoLocation";
    public static final String PREFERENCE_FERRY_SCHEDULE_REGEX = "FerryScheduleRegex";
    public static final String PREFERENCE_DESTINATION_A = "SubstringDestinationA";
    public static final String PREFERENCE_DESTINATION_B = "SubstringDestinationB";
    
    public static final String PREFERENCE_VESSEL_WATCH_URL = "PreferenceVesselWatchUrl";
    public static final String PREFERENCE_FERRY_MODE =      "PreferenceFerryMode";
    public static final String PREFERENCE_PORT_ONE_LAT =    "PreferencePortOneLat";
    public static final String PREFERENCE_PORT_ONE_LONG =   "PreferencePortOneLong";
    public static final String PREFERENCE_PORT_TWO_LAT =    "PreferencePortTwoLat";
    public static final String PREFERENCE_PORT_TWO_LONG =   "PreferencePortTwoLong";
    public static final String PREFERENCE_CUSTOM_MODE =     "PreferenceCustomMode";
    
    //..ie. refresh/update textviews visibility..
    public static final String PREFERENCE_SHOW_ADDITIONAL = "PreferenceShowAdditional";
    
    
    //
    //..determine if our ferry mode changed..
    //..used (later) to see if we should update our preferences to reflect our ferry mode..
    //
    //..updates mFerryMode as well..
    //
    private Boolean ferryModeChanged()
    {
        final int i = Integer.parseInt(
                mSharedPreferences.getString(  
                    PREFERENCE_FERRY_MODE,     
                    "0" ) );
        //..stash previous..
        final int j = mFerryMode;
        
        //..update..
        mFerryMode = i;
        
        return (i != j )? true : false;
    }
    
    //
    //..return true if custom ferry mode is enabled..
    //..overides hardcoded values..
    //
    private Boolean isCustomModeEnabled()
    {
        final Boolean b = mSharedPreferences.getBoolean(
                PREFERENCE_CUSTOM_MODE,
                false );
        return b;
    }
    
    private Boolean preferencesNeedChanged()
    {
        //..do not over-write if using custom..
        if ( isCustomModeEnabled() )
        {
            return false;
        }
        //..else for normal ferry modes..
        else
        {
            return ferryModeChanged();
        }
        
    }
    
    //
    //..doesn't directly write ..
    //..decides which values we want to over-write..
    //..under advanced settings..
    //
    private Boolean writeSharedPreferencesIfNeeded()
    {
    }
    
    public static final String PREFERENCE_BULLETIN_REGEX = "PreferenceBulletinRegex";
    public static final String PREFERENCE_HOURLY_UPDATE_INTERVAL = "PreferenceHourlyUpdate";
    public static final String PREFERENCE_BULLETIN_LENGTH = "PreferenceBulletinLength";
    public static final String PREFERENCE_SHOW_ALL_BULLETIN = "PreferenceShowAllBulletins";
    //
    //..reads prefs..
    //..defaults to bainbridge..
    //
    private void readSharedPreferences( SharedPreferences pPrefs )
    {
    }
    
    
    //..
    private void setLastCheckedFxn()
    {
        if (mShowTimeStamps)
        {
            SimpleDateFormat formatter = new SimpleDateFormat( mSimpleDateFormatString );
            textViewChecked.setText( 
                "Last Refreshed : " + formatter.format( new Date() ) );
        }
        else
        {
            //..redundantly clear..
            //..since Download doesn't get called often..
            textViewChecked.setText("");
            textViewDownloaded.setText("");
        }
    }
    
    //..
    private void downloadLatest()
    {
        if ( isInternetAvailable() )
        {
            DownloadWebPage zDownloadWebPage = new DownloadWebPage( this, mFerryScheduleUrl, whichFile() );
            zDownloadWebPage.start();
            
            updateDownloadLatestText( System.currentTimeMillis() );
            downloadFerryBulletin();
        }
        else
        {
            Toast.makeText( 
                this, 
                "Update Failed : no network connection...",
                Toast.LENGTH_LONG
                ).show();  
        }
    }
    
    
    //
    //..displays the pseudo-lastModified date of the NextBoat.html file..
    //
    private void updateDownloadLatestText( long pLong )
    {
        if (mShowTimeStamps)
        {
            SimpleDateFormat formatter = new SimpleDateFormat( mSimpleDateFormatString );
            textViewDownloaded.setText(
                "Last Updated : " + formatter.format( new Date( pLong ) ) );
        }
        else
        {
            textViewDownloaded.setText("");
        }
    }
    //
    //..open a browser to the appropriate ferry cam image..
    //
    private void launchFerryCamFxn()
    {
        Intent intent = 
            new Intent(
                Intent.ACTION_VIEW,
                Uri.parse( getAppropriateFerryCamURL() ) );
        startActivity( intent );
    }
    
    private String getAppropriateFerryCamURL()
    {
    
        if ( mFerryDestination.equals( mFerryPortOne ) )
        {
            return mPortOneFerryCamUrl;
        }
        else
        {
            //..else we are leaving bainbridge..
            return mPortTwoFerryCamUrl;
        }
    }
    
    
    
    //
    //..launch an activity with a list view of our ferry schedule..
    //
    private void launchScheduleListFxn()
    {
        //..assemble a comma delimited string to pass..
        String s = assembleCommaSeparatedSchedule();
        Intent intent = 
            new Intent( 
                this, 
                MyScheduleList.class );
        intent.putExtra( "theList", s );
        
        //.. x can be -1 ..
        final String x = Integer.toString( mFerryObjectArray.indexOf( mNextBoat ) );
        intent.putExtra( "theIndex", x );
        
        final String weekEndOrDay = ( isWeekEnd() ) ? " (Weekend)" : " (Weekday)" ;
        intent.putExtra( "theTitle", (mFerryDestination + weekEndOrDay) );
        
        String y = Integer.toString( -1 );
        if (mTargetBoat != null)
        {
            //..TODO:..simplify
            //..if we force-refresh, we technically cannot find mTargetBoat's index in the "new" array.. 
            //..for a quick-fix, just use the index of what worked last time..
            y = Integer.toString( mFerryObjectArray.indexOf( mTargetBoat ) );
        }
        if( (mTargetBoat != null) && (new Integer(y) == -1) && (mTargetBoatIndex != -1) )
        {
            //..try using the target boat index of last time..
            //..if they are basically the same..
            if ( ( mTargetBoat.prettyPrint() ).equals( mFerryObjectArray.get( mTargetBoatIndex ).prettyPrint() ) )
            {
                y = Integer.toString( mTargetBoatIndex );
            }
        }
        intent.putExtra( "theTargetBoatIndex", y);
        
        //startActivity( intent );
        startActivityForResult( intent, TARGET_BOAT_REQUEST );
    }
    
    public static final int TARGET_BOAT_REQUEST = 123;
    
    //..    http://developer.android.com/reference/android/app/Activity.html
    //..    #startActivityForResult(android.content.Intent, int)
    protected void onActivityResult(int requestCode, int resultCode,
             Intent pData) 
    {
         
         if (requestCode == TARGET_BOAT_REQUEST) {
             if (resultCode == Activity.RESULT_OK ) {
                 final int targetBoatIndex = new Integer(
                        pData.getExtras().getString( MyScheduleList.TARGET_BOAT ) );
                 mTargetBoat = mFerryObjectArray.get( targetBoatIndex );
                 mTargetBoatIndex = targetBoatIndex;
                 //updateTargetBoatText();
             }
         }
         
     }
    
    
    //
    //..joins the ferry object list by ","..
    //
    private String assembleCommaSeparatedSchedule()
    {
        String foo = "";
        for ( int i=0; i < mFerryObjectArray.size(); i++ )
        {
            foo = foo + ( mFerryObjectArray.get( i ).print() + "," );
        }
        return foo;
    }
    
    //
    //..open web page to ferry schedule..
    //
    private void goOnlineFxn()
    {
        //..TODO..beconfigurable via preferences..
        Intent myIntent = new Intent( Intent.ACTION_VIEW, 
            Uri.parse( mFerryScheduleUrl ));
        startActivity( myIntent );
    }
    
    //
    //..open web page to ferry alert bulletins..
    //
    //..TODO: open activity instead..
    private void goOnlineAlertFxn()
    {
        Intent myIntent = new Intent ( Intent.ACTION_VIEW,
            Uri.parse( getString( R.string.alert_bulletin_url ) ) );
        startActivity( myIntent );
    }
    
    
    private void updateWhenBoatDeparts( Boolean b )
    {
        if (b)
        {
            textViewDeparting.setText( mNextBoat.prettyPrintBoat()+ " departs in : " + mNextBoat.printWhenBoatLeaves() );
        }
        else
        {
            textViewDeparting.setText( "n/a" );
        }
    }
    private void updateWhenBoatDeparts( FerryObject pFerryObject )
    {
        textViewDeparting.setText( pFerryObject.prettyPrintBoat()+ " departs in : " + pFerryObject.printWhenBoatLeaves() );
    }
    
    public static final String PLEASE_REFRESH = "[ Instructions ] \n 1.) Don't Panic! \n 2.) menu...preferences...Set Ferry Route (If you already haven't) \n 3.) Check Network \n 4.) menu...more...Force-Update \n 5.) menu...more...Force-Refresh \n 6.)  Enjoy! ;)";
    
    //
    //..parse our html file and get the next ferry that is going to leave..
    //
    //..perhaps this should be it's own thread ?..
    //
    private String getNextFerry()
    {
        //
        //..TODO..set unknown from xml string val..
        //
        
        //..TODO: BUG..
        /* 
        We fail if we don't have our html file on first run through or if empty ?
        */
        File f = whichFile();
        if ( !f.exists() )
        {
            updateWhenBoatDeparts( false );
            textViewDeparting.setText( PLEASE_REFRESH );
            return "";
        }
        //..if empty, re-init..
        if ( mFerryScheduleHtmlString == null )
        {
            initNextFerry();
        }
        //..TODO compare current time and return the right one ..
        if ( mFerryObjectArray.size() == 0 )
        {
            updateWhenBoatDeparts( false );
            textViewDeparting.setText( PLEASE_REFRESH );
            return "";
        }
        if ( mTargetBoat != null )
        {
            updateWhenBoatDeparts( mTargetBoat );
            return mTargetBoat.prettyPrint();
        }
        
        //..get next time..
        ArrayList<FerryObject> localFerryList = new ArrayList<FerryObject>();
        Date now = Calendar.getInstance().getTime();
        for ( int i=0; i < mFerryObjectArray.size(); i++ )
        {
            //..if we could catch the boat..
            //int comparison = now.compareTo( mFerryObjectArray.get(i).toLong() );
            if ( now.before( mFerryObjectArray.get(i).getDate() ) )
            {
                localFerryList.add( mFerryObjectArray.get( i ) );
            }
        }
        
        FerryObject zNextBoat = new FerryObject();
        if ( localFerryList.size() <= 0 )
        {
            //zNextBoat.setNull( true );
            //mNextBoat = zNextBoat;
            //zReturn = zNextboat.print();
            mNextBoat = null;
            updateWhenBoatDeparts( false );
            return "No Next Boat!";
        }
        else if ( localFerryList.get(0) != null )
        {
            zNextBoat = localFerryList.get(0);
            mNextBoat = zNextBoat;
            updateWhenBoatDeparts( mNextBoat );
            return zNextBoat.prettyPrint();
        }
        else
        {
            updateWhenBoatDeparts( false );
            textViewDeparting.setText( PLEASE_REFRESH );
            return "";
        }
    }
    
    //
    //..substrings the html file based on current destination..
    //..in preparation for ferry object list assembly..
    //
    private String getAppropriateHtml()
    {
        String zHtmlSchedule = "";
        
        //..TODO: add StringIndexOutOfBoundsExceptions..
        int i = mFerryScheduleHtmlString.indexOf( mFerryPortOne );
        int j = mFerryScheduleHtmlString.indexOf( mFerryPortTwo );
        
        
        if ( ( -1 == i ) || ( -1 == j ) )
        {
            //downloadLatest();
            return "fail!";
        }
        if ( i < j )
        {
            if ( mFerryDestination.equals( mFerryPortOne ) )
            {
                zHtmlSchedule = mFerryScheduleHtmlString.substring( i, j );
            }
            else
            {
                zHtmlSchedule = mFerryScheduleHtmlString.substring( j );
            }
        }
        else
        {
            //..else j is before i..
            if ( mFerryDestination.equals(mFerryPortTwo) )
            {
                zHtmlSchedule = mFerryScheduleHtmlString.substring( j, i );
            }
            else
            {
                zHtmlSchedule = mFerryScheduleHtmlString.substring( i );
            }
        }
        return zHtmlSchedule;
    }
    
    //
    //..creates an ArrayList AM/PM, HH:MM, and BoatName..
    //
    private ArrayList htmlToListViaRegex( String pHtmlString )
    {
        Pattern pattern = Pattern.compile( mRegex );
        Matcher matcher = pattern.matcher( pHtmlString );
        ArrayList li = new ArrayList();
        while ( matcher.find() )
        {
            if ( matcher.group(1) != null )
            {
                li.add( matcher.group(1) );
            }
            else if ( matcher.group(2) != null )
            {
                li.add( matcher.group(2) );
            }
            else if ( matcher.group(3) != null )
            {
                li.add( matcher.group(3) );
            }
            //..Midnight or Noon..
            else if ( matcher.group(4) != null )
            {
                
                if ( (matcher.group(4)).equals("Midnight") )
                {
                    li.add( "12:00" );
                }
                if ( (matcher.group(4)).equals("Noon") )
                {
                    li.add( "12:00" );
                }
            }
            
        }
        return li;
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
                ferryObject.setLeaving( mFerryDestination );
                //..append to array..
                zList.add( ferryObject );
            }
        }//..end for..
        
        return zList;
    }
    
    //
    //..finds boats between 12am ~ 4am and prefixes our list..
    //..a quick fix for incorrectly predicting next boat post midnight..
    //
    //..TODO: this might inappropriately extrapolate..
    //..today's schedule against say a holiday..
    //
    private ArrayList<FerryObject> correctFerryListForLateNights( 
            ArrayList<FerryObject> pOriginalFerryList           )
    {
        ArrayList<FerryObject> li = new ArrayList<FerryObject>();
        
        int zHour = 0;
        Calendar zCal = Calendar.getInstance();
        for ( int i=0; i < pOriginalFerryList.size(); i++  )
        {
            zHour = pOriginalFerryList.get(i).getCalendar().get(Calendar.HOUR_OF_DAY);
            //..between midnight and 3 am..
            if ( zHour < 4 )
            {
                //..TODO..clone..
                FerryObject zClone = new FerryObject();
                zClone.setBoat( pOriginalFerryList.get(i).getBoat() );
                zCal = (Calendar)pOriginalFerryList.get(i).getCalendar().clone();
                //..set to today..
                zCal.set(
                    Calendar.DAY_OF_YEAR, 
                    zCal.get( Calendar.DAY_OF_YEAR ) - 1 );
                zClone.setCalendar( zCal );
                
                zClone.setExtra( " (extrapolation)" );
                    
                li.add( zClone );
            }
        }
        
        //..2nd iteration to merge lists..
        //..TODO just use + ?
        for ( int i=0; i < pOriginalFerryList.size(); i++  )
        {
            li.add( pOriginalFerryList.get(i) );
        }
        
        return li;
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
        
        //..TODO..
        //..should we do this subString step BEFORE we write to file?..
    
        //..check if we have our html file..
        File f = whichFile();
        if ( !f.exists() )
        {
            //downloadLatest();
            return false;
        }
        //..read it..
        String htmlString = ReadFile( f );
        if ( htmlString.length() < 1 )
        {
            downloadLatest();
            return false;
        }
        
        mFerryScheduleHtmlString = htmlString;
        return true;
    }
    //..( http://stackoverflow.com/questions/2902689/read-text-file-data-in-android )..
    private String ReadFile( File pFile )
    {
        return DownloadWebPage.ReadFile( pFile );
    }
    
    private void downloadFerryBulletin()
    {
        FerryBulletin ferryBulletin = new FerryBulletin( this );
        ferryBulletin.DownloadBulletin();
    }
    
    private TextView mBulletinTextView;
    private int mBulletinLength;
    
    
    //..this actually prints a short summary..
    private void readFerryBulletin()
    {
        //..if set to none, so nothing..
        if (mBulletinLength <= 1)
        {
            mBulletinTextView.setText("");
            return;
        }
        
        String zBulletin = FerryBulletin.readBulletin( mBulletinRegex );
        if ( zBulletin.length() > mBulletinLength )
        {
            zBulletin = zBulletin.substring( 0, mBulletinLength ) + " . . .";
        }
        
        mBulletinTextView.setText( zBulletin.replaceAll("\n", "") );
    }
    
    private boolean mShowAllBulletinIfEmpty = true;
    
    private void viewAlertBulletinDialog()
    {
        String zBulletin = FerryBulletin.readBulletin( mBulletinRegex );
        if ( zBulletin.length() < 5 && mShowAllBulletinIfEmpty)
        {
            zBulletin = FerryBulletin.readBulletin( );    
        }
        if ( zBulletin.length() < 5)
        {
            zBulletin = "No Relevant Alert Bulletins Found!";
        }
        new org.aschyiel.nextboat.BulletinDialog(
                this, zBulletin ).show();
    }
    
    //..TODO..
    //..if first time,..launch setup..
    //
    
    
    
}
