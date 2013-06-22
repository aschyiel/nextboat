//..OptionsMenu.java, uly, june2013..
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

import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;

/**
* All of the available options under the "menu" section live here.
*/
public class OptionsMenu
{

  /**
  * Handle a menu-option getting selected by the user.
  *
  * @param (NextBoat) activity is the original context.
  * @param (MenuItem) item is the menu-item that was selected by the user.
  */
  public Boolean onOptionsItemSelected( NextBoat activity, MenuItem item ) {
    // Handle item selection
    switch ( item.getItemId() ) {
      case R.id.id_view_all:
        showDepartures( activity );
        return true;
      case R.id.id_go_online:
        showScheduleWebPage( activity );
        return true;
      case R.id.id_ferry_cam:
        showFerryCam( activity );
        return true;
      case R.id.id_vessel_watch:
        showVesselWatch( activity );
        return true;
      case R.id.id_go_alert:
        showBulletinPage( activity );
        return true;
      case R.id.id_view_alert_bulletin:
        activity.viewAlertBulletinDialog();
        return true;
      default:
        return false;
    }
  }

  /**
  * Bring up the WSDOT Vessel Watch webpage.
  *
  * @see gov.wa.wsdot.android.wsdot/.SplashScreen
  */
  public void showVesselWatch( NextBoat nextboat )
  {
    // TODO: Consider directly launching the WSDOT app.
    String link = nextboat.getVesselWatchUrl();
    nextboat.startActivity( new Intent(
        Intent.ACTION_VIEW,
        Uri.parse( link ) ) );
  }

  /**
  * Show the live ferry-cam image to the user.
  */
  public void showFerryCam( NextBoat nextboat )
  {
    String link = nextboat.getFerryCamUrl();
    nextboat.startActivity( new Intent(
        Intent.ACTION_VIEW,
        Uri.parse( link ) ) );
  }

  /**
  * Bring up a schedule listing all of the ferry departures for our current destination.
  */
  public void showDepartures( NextBoat nextboat )
  {
    Intent departures = new Intent( nextboat, DeparturesList.class );
    departures.putExtra( "theList",  nextboat.getDeparturesScheduleAsCsv() );
    departures.putExtra( "theIndex", nextboat.getNextDepartureIndex() );
    departures.putExtra( "theTitle", nextboat.getDeparturesTitle() );
    nextboat.startActivity( departures );
  }

  /**
  * Open the ferry schedule web page.
  */
  public void showScheduleWebPage( NextBoat nextboat )
  {
    String link = nextboat.getFerryScheduleUrl();
    nextboat.startActivity( new Intent(
        Intent.ACTION_VIEW,
        Uri.parse( link ) ) );
  }
  
  /**
  * Open the ferry alerts/bulletins web page.
  */
  public void showBulletinPage( NextBoat nextboat )
  {
    String link = nextboat.getBulletinUrl();
    nextboat.startActivity( new Intent(
        Intent.ACTION_VIEW,
        Uri.parse( link ) ) );
  }

}
