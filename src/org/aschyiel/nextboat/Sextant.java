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

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;

/**
* This class is responsible for owning the "where are we?" question.
*/
public class Sextant
{

  /**
  * Calculate the closes location given our current position.
  *
  * @public
  * @param (Context) context is the parent activity.
  * @param (double) lat1 is location-1's latitude.
  * @param (double) lon1 is location-1's longitude.
  * @param (double) lat2 is location-2's latitude.
  * @param (double) lon2 is location-2's longitude.
  * @return (int) Is 1 if location-1 is closer, or 2 if location-2 is closer,
  *         and 0 if they are tied;
  *         Returns -1 if our current-location is unavailable.
  * @see http://www.damonkohler.com/2009/02/android-recipes.html
  */
  public int getClosestLocation( Context context, double lat1, double lon1,
                                 double lat2, double lon2 )
  {
    LocationManager manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );

    //
    // Get our current position, aka "you are here".
    // Try GPS first, falling back to coarse location if it's unavailable.
    //

    Location currentLocation = manager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
    if ( null == currentLocation )
    {
      currentLocation = manager.getLastKnownLocation( LocationManager.NETWORK_PROVIDER );
    }

    if ( null == currentLocation ) {
      return -1;
    }

    Location location1 = new Location( "providerName" );
    location1.setLatitude(  lat1 );
    location1.setLongitude( lon1 );
    
    Location location2 = new Location( "providerName" );
    location2.setLatitude(  lat2 );
    location2.setLongitude( lon2 );
    
    final float distance1 = currentLocation.distanceTo( location1 );
    final float distance2 = currentLocation.distanceTo( location2 );
    
    // Note: If the float comparison is negative, that means location-2 is closer.
    int comparison = java.lang.Float.compare( distance2, distance1 );
    return ( comparison < 0 )?
        2 : ( comparison > 0 )?
        1 : 0;
  }
}