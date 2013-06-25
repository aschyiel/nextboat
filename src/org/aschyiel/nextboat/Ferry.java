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

import android.util.Log;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.DateFormat;


/**
* A "ferry" represents a single ferry departure;
* scheduled to go from point A to point B at specific time.
*/
public class Ferry
{
  /** @constructor */
  public Ferry()
  {
    _calendar = Calendar.getInstance();
  }

  //---------------------------------
  //
  // Public Properties.
  //
  //---------------------------------

  /**
  * The "time"-stamp property.
  * (String)
  *
  * For Example:
  *   ferry.setTime( "12:00" )
  *   ==> void
  *
  *   ferry.getTime()
  *   ==> "12:00"
  */
  public String getTime()
  {
    return this._time;
  }
  public void setTime( String time )
  {
    this._time = time;

    //
    // Update our internal calendar to reflect the HH:MM time.
    //

    int splitIndex = time.indexOf( ":" );
    int hour = Integer.parseInt( time.substring( 0, splitIndex ) );
    if (hour > 11)
    {
      hour = 0;
    }
    _calendar.set( Calendar.HOUR, hour );
    
    _calendar.set( Calendar.MINUTE,
        Integer.parseInt( time.substring( splitIndex + 1, time.length() ) ) );
  }

  /**
  * The ferry's destination property.
  * (String)
  *
  * For Example:
  *   ferry.setDestination( "Seattle" )
  *   ==> void
  *
  *   ferry.getDestination()
  *   ==> "Seattle"
  */
  public String getDestination()
  {
    return this._destination;
  }
  public void setDestination( String destination )
  {
    this._destination = destination;
  }

  /**
  * The "leaving" property represents the port-name the ferry is departing from.
  * (String)
  *
  * For Example:
  *   ferry.getLeaving()
  *   ==> "Bainbridge Island"
  */
  public String getLeaving()
  {
    return this._leaving;
  }
  public void setLeaving( String leaving )
  {
    this._leaving = leaving;
  }

  /**
  * The "vessel" property represents the boat's name.
  * (String)
  *
  * For Example:
  *   ferry.getVessel()
  *   ==> "Tacoma"
  */
  public String getVessel()
  {
    return this._vessel;
  }
  public void setVessel( String vessel )
  {
    this._vessel = vessel;
  }

  /**
  * The "AM/PM" property reflects the day vs. night-ness of the scheduled
  * departure.
  *
  * For Example:
  *   ferry.getAmPm()
  *   ==> "PM"
  */
  public String getAmPm()
  {
    return this._amPm;
  }
  public void setAmPm( String amPm )
  {
    this._amPm = amPm;
    _calendar.set( Calendar.AM_PM,
        ( amPm.equals( "AM" ) )? 0 : 1 );
  }

  /**
  * The normalized ferry-route name between two ports.
  *
  * For Example:
  *   ferry.getRoute()
  *   ==> "Seattle-Bainbridge Island"
  */
  public String getRoute()
  {
    return this._route;
  }
  public void setRoute( String route )
  {
    this._route = route;
  }

  /**
  * The isWeekday property is "true" for Monday through Friday,
  * and is false for Saturday, and Sunday.
  * (Boolean)
  *
  * For Example:
  *   ferry.setIsWeekday( "false" )
  *   ==> void
  *   ferry.getIsWeekday()
  *   ==> false
  */
  public Boolean getIsWeekday()
  {
    return this._isWeekday;
  }
  public void setIsWeekday( String isWeekday )
  {
    this.setIsWeekday( ( new Boolean( true ) ).toString().equals( isWeekday ) );
  }
  public void setIsWeekday( Boolean isWeekday )
  {
    this._isWeekday = isWeekday;
  }

  //---------------------------------
  //
  // Private Variables.
  //
  //---------------------------------

  /**
  * @private
  * @see Ferry#getAmPm
  * @see Ferry#setAmPm
  */
  private String _amPm = null;

  /**
  * @private
  * @see Ferry#getTime
  * @see Ferry#setTime
  */
  private String _time = null;

  /**
  * @private
  * @see Ferry#getDestination
  * @see Ferry#setDestination
  */
  private String _destination = null;

  /**
  * @private
  * @see Ferry#getLeaving
  * @see Ferry#setLeaving
  */
  private String _leaving = null;

  /**
  * @private
  * @see Ferry#getVessel
  * @see Ferry#setVessel
  */
  private String _vessel = null;

  /**
  * @private
  * @see Ferry#getRoute
  * @see Ferry#setRoute
  */
  private String _route = null;

  /**
  * @private
  * @see Ferry#getIsWeekday
  * @see Ferry#setIsWeekday
  */
  private Boolean _isWeekday = null;

  public static final String TAG = "Ferry";

  /**
  * This ferry's internal calendar, gets updated based on the other
  * time-related setters.
  *
  * @private
  */
  private Calendar _calendar;

  private String _boat = "?";
  private String mExtra = "";
  private Boolean mNull = false;

  //---------------------------------
  //
  // Public Methods.
  //
  //---------------------------------
  
  public String getBoat()
  {
      return _boat;
  }
  
  public void setBoat( String pBoatName )
  {
      _boat = pBoatName;
  }
  
  public String getTimeAsString()
  {
      SimpleDateFormat formatter = new SimpleDateFormat( "hh:mm a" );
      return formatter.format( _calendar.getTime() );
  }
  
  public Date getDate()
  {
      return _calendar.getTime();
  }
  
  public long toLong()
  {
      return _calendar.getTime().getTime();
  }
  
  public String print()
  {
      return 	mNull ? "No Next Boat!" : ("@"+getTimeAsString() + " ["+_boat +"] "+ mExtra);
  }
  
  public String toString()
  {
      return print();
  }
  
  public void setNull( Boolean b )
  {
      mNull = b;
  }
  
  public Boolean isNull()
  {
      return mNull;
  }
  
  public String prettyPrint()
  {
      //..    @09:00PM 
      return 	mNull ? "No Next Boat!" : ( "@" + getTimeAsString() );
  }
  
  public String prettyPrintBoat()
  {
      return ("[" + _boat + "]");
  }
  
  public String printWhenBoatLeaves()
  {
      //..future val - present val = delta val..
      double foo = (double)( _calendar.getTime().getTime() - System.currentTimeMillis() ) * ( 1.0 / 1000.0 ) * ( 1.0 / 60.0 );
      //return Double.toString( foo ) + " min";
      //..do a crappy rounding to one significant figure..
      return (int)foo > -1 ? Integer.toString( (int)foo ) + " min" : "[ Already Departed ]";
  } 

  //---------------------------------
  //
  // Private Methods.
  //
  //---------------------------------


}
