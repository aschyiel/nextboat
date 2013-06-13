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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceWriter
{
    private Context mContext;
    private SharedPreferences mSharedPreferences;

    //..constructor..
    public PreferenceWriter( Context pContext, SharedPreferences pSharedPreferences )
    {
        mContext = pContext;
        mSharedPreferences = pSharedPreferences;
    }    

    //
    //..this doubles up as a "reset" button..
    //
    public void writeSharedPreferencesBainbridge( )
    {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
    
        editor.putString(  
            NextBoat.PREFERENCE_FERRY_SCHEDULE_URL,     
            mContext.getString(R.string.hardcoded_bainbridge_to_seattle_schedule_url) );
            
    	editor.putString( 
    	    NextBoat.PREFERENCE_BAINBRIDGE_FERRY_CAM_URL, 
    	    mContext.getString(R.string.hardcoded_bainbridge_to_seattle_ferry_cam_2_url) );
    	    
        editor.putString(
            NextBoat.PREFERENCE_SEATTLE_FERRY_CAM_URL,   
            mContext.getString(R.string.hardcoded_bainbridge_to_seattle_ferry_cam_1_url) );
            
        editor.putString(
            NextBoat.PREFERENCE_DESTINATION_A,
            mContext.getString(R.string.hardcoded_bainbridge_to_seattle_port_1) );
        
        editor.putString(
            NextBoat.PREFERENCE_DESTINATION_B,
            mContext.getString(R.string.hardcoded_bainbridge_to_seattle_port_2) );
        
        editor.putString(
            NextBoat.PREFERENCE_FERRY_SCHEDULE_REGEX,     
            mContext.getString(R.string.hardcoded_bainbridge_to_seattle_regex) );
        
        editor.putString(
            NextBoat.PREFERENCE_BULLETIN_REGEX,     
            mContext.getString(R.string.hardcoded_bainbridge_to_seattle_regex_bulletin) );
       
        editor.putString(  
                NextBoat.PREFERENCE_PORT_ONE_LAT,     
                mContext.getString(R.string.hardcoded_bainbridge_to_seattle_port_1_lat) );
        
        editor.putString(  
                NextBoat.PREFERENCE_PORT_ONE_LONG,     
                mContext.getString(R.string.hardcoded_bainbridge_to_seattle_port_1_long) );
        
        editor.putString(
                NextBoat.PREFERENCE_PORT_TWO_LAT,     
                mContext.getString(R.string.hardcoded_bainbridge_to_seattle_port_2_lat) );
                
        editor.putString(  
                NextBoat.PREFERENCE_PORT_TWO_LONG,     
                mContext.getString(R.string.hardcoded_bainbridge_to_seattle_port_2_long) );
        
        editor.putString(
                NextBoat.PREFERENCE_VESSEL_WATCH_URL,
                mContext.getString(R.string.hardcoded_bainbridge_to_seattle_vessel_watch_url) );
        
        //..commit to shared preferences..
        editor.commit();
    }
    
    //
    //..basically the same as writeSharedPreferencesBainbridge()..
    //..but with different R.string. values..
    //
    public void writeSharedPreferencesBremerton( )
    {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
    
        editor.putString(  
            NextBoat.PREFERENCE_FERRY_SCHEDULE_URL,     
            mContext.getString(R.string.hardcoded_bremerton_to_seattle_schedule_url) );
            
    	editor.putString( 
    	    NextBoat.PREFERENCE_BAINBRIDGE_FERRY_CAM_URL, 
    	    mContext.getString(R.string.hardcoded_bremerton_to_seattle_ferry_cam_2_url) );
    	    
        editor.putString(
            NextBoat.PREFERENCE_SEATTLE_FERRY_CAM_URL,   
            mContext.getString(R.string.hardcoded_bremerton_to_seattle_ferry_cam_1_url) );
            
        editor.putString(
            NextBoat.PREFERENCE_DESTINATION_A,
            mContext.getString(R.string.hardcoded_bremerton_to_seattle_port_1) );
        
        editor.putString(
            NextBoat.PREFERENCE_DESTINATION_B,
            mContext.getString(R.string.hardcoded_bremerton_to_seattle_port_2) );
        
        editor.putString(
            NextBoat.PREFERENCE_FERRY_SCHEDULE_REGEX,     
            mContext.getString(R.string.hardcoded_bremerton_to_seattle_regex) );
        
        
        editor.putString(
            NextBoat.PREFERENCE_BULLETIN_REGEX,     
            mContext.getString(R.string.hardcoded_bremerton_to_seattle_regex_bulletin) );
       
        editor.putString(  
                NextBoat.PREFERENCE_PORT_ONE_LAT,     
                mContext.getString(R.string.hardcoded_bremerton_to_seattle_port_1_lat) );
        
        editor.putString(  
                NextBoat.PREFERENCE_PORT_ONE_LONG,     
                mContext.getString(R.string.hardcoded_bremerton_to_seattle_port_1_long) );
        
        editor.putString(
                NextBoat.PREFERENCE_PORT_TWO_LAT,     
                mContext.getString(R.string.hardcoded_bremerton_to_seattle_port_2_lat) );
                
        editor.putString(  
                NextBoat.PREFERENCE_PORT_TWO_LONG,     
                mContext.getString(R.string.hardcoded_bremerton_to_seattle_port_2_long) );
        
        editor.putString(
                NextBoat.PREFERENCE_VESSEL_WATCH_URL,
                mContext.getString(R.string.hardcoded_bremerton_to_seattle_vessel_watch_url) );
        
        //..commit to shared preferences..
        editor.commit();
    }
    
    public void writeSharedPreferencesEdmondsKingston( )
    {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
    
        editor.putString(  
            NextBoat.PREFERENCE_FERRY_SCHEDULE_URL,     
            mContext.getString(R.string.hardcoded_edmonds_to_kingston_schedule_url) );
            
    	editor.putString( 
    	    NextBoat.PREFERENCE_BAINBRIDGE_FERRY_CAM_URL, 
    	    mContext.getString(R.string.hardcoded_edmonds_to_kingston_ferry_cam_2_url) );
    	    
        editor.putString(
            NextBoat.PREFERENCE_SEATTLE_FERRY_CAM_URL,   
            mContext.getString(R.string.hardcoded_edmonds_to_kingston_ferry_cam_1_url) );
            
        editor.putString(
            NextBoat.PREFERENCE_DESTINATION_A,
            mContext.getString(R.string.hardcoded_edmonds_to_kingston_port_1) );
        
        editor.putString(
            NextBoat.PREFERENCE_DESTINATION_B,
            mContext.getString(R.string.hardcoded_edmonds_to_kingston_port_2) );
        
        editor.putString(
            NextBoat.PREFERENCE_FERRY_SCHEDULE_REGEX,     
            mContext.getString(R.string.hardcoded_edmonds_to_kingston_regex) );
        
        editor.putString(
            NextBoat.PREFERENCE_BULLETIN_REGEX,     
            mContext.getString(R.string.hardcoded_edmonds_to_kingston_regex_bulletin) );
       
        editor.putString(  
                NextBoat.PREFERENCE_PORT_ONE_LAT,     
                mContext.getString(R.string.hardcoded_edmonds_to_kingston_port_1_lat) );
        
        editor.putString(  
                NextBoat.PREFERENCE_PORT_ONE_LONG,     
                mContext.getString(R.string.hardcoded_edmonds_to_kingston_port_1_long) );
        
        editor.putString(
                NextBoat.PREFERENCE_PORT_TWO_LAT,     
                mContext.getString(R.string.hardcoded_edmonds_to_kingston_port_2_lat) );
                
        editor.putString(  
                NextBoat.PREFERENCE_PORT_TWO_LONG,     
                mContext.getString(R.string.hardcoded_edmonds_to_kingston_port_2_long) );
        
        editor.putString(
                NextBoat.PREFERENCE_VESSEL_WATCH_URL,
                mContext.getString(R.string.hardcoded_edmonds_to_kingston_vessel_watch_url) );
        
        //..commit to shared preferences..
        editor.commit();
    }
}
