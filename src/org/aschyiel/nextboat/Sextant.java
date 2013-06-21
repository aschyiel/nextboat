//..Sextant, uly, june2013..
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

/**
* This class is responsible for owning the "where are we?" question.
*/
public class Sextant
{
 
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
  
}