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



public class FerryObject
{
    public FerryObject()
    {
        //..set to now..
        _calendar = Calendar.getInstance();
    }
    
    public static final String TAG = "FerryObject";
    private Calendar _calendar;
    private String _boat = "?";
    private String _leaving;
    private String mExtra = "";
    private Boolean mNull = false;
    
    public void setDate( Date pDate )
    {
        _calendar.setTime( pDate );
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
        _calendar.set(
            Calendar.DAY_OF_YEAR,
            pDayOfYear );    
    }
    
    //
    //..a convenience function..
    //
    //..see http://developer.android.com/reference/java/util/Calendar.html
    //..http://developer.android.com/reference/java/util/Date.html
    public void setTime( String pHHMM, String paa, Boolean pNextDay )
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
    
    public void setLeaving( String pLeaving )
    {
        //..ie. Leaving Seattle..
        _leaving = pLeaving;
    }
    
    public String getLeaving()
    {
        return _leaving;
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

    

}
