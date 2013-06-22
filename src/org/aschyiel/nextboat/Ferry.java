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
    //..set to now..
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
  private Calendar _calendar;
  private String _boat = "?";
  private String mExtra = "";
  private Boolean mNull = false;
  
  public void setDate( Date pDate )
  {
    _calendar.setCalendar( pDate );
  }

  public void setCalendar( Calendar pCal )
  {
    _calendar = pCal;
  }


  public Calendar getCalendar()
  {
    return _calendar;
  }

  public void setExtra( String pExtra )
  {
    mExtra = pExtra;
  }

  private void setCalendarDay( int pDayOfYear )
  {
    _calendar.set( Calendar.DAY_OF_YEAR, pDayOfYear );
  }
 



  //---------------------------------
  //
  // Public Methods.
  //
  //---------------------------------

  //
  //..a convenience function..
  //
  //..see http://developer.android.com/reference/java/util/Calendar.html
  //..http://developer.android.com/reference/java/util/Date.html
  public void setCalendar( String pHHMM, String paa, Boolean pNextDay )
  {
      int AM_or_PM = 0;   //..0 is AM..
      if (!paa.equals("AM"))
      {
          AM_or_PM = 1;   //..1 is PM..
      }
      //pHHMM might be 1:23 or 12:34
      Calendar cal = Calendar.getInstance(); 
      
      if ( pNextDay )
      {
          cal.set( 
              Calendar.DAY_OF_YEAR,
              ( cal.get( Calendar.DAY_OF_YEAR ) + 1 ) );
      }
      
      int indexColon = pHHMM.indexOf(":");
      int hour = Integer.parseInt( pHHMM.substring(0, indexColon) );
      if (hour > 11)
      {
          hour = 0;
      }
      
      cal.set(
          Calendar.HOUR ,
          hour );  
      
      
      cal.set(
          Calendar.MINUTE ,
          Integer.parseInt( pHHMM.substring( indexColon + 1, pHHMM.length() ) ) );
      
      cal.set(
          Calendar.AM_PM ,
          AM_or_PM );        
      
      setCalendar( cal );
      
      setCalendarDay( cal.get( Calendar.DAY_OF_YEAR ) );
  }
  
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
