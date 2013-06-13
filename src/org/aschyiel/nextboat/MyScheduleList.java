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

//..see ( http://developer.android.com/resources/tutorials/views/hello-listview.html )..

package org.aschyiel.nextboat;

import android.app.Activity;
import android.app.ListActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.preference.PreferenceManager;

import android.util.Log;


import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;

import android.widget.BaseAdapter;
import android.widget.ListView;

import android.widget.ArrayAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;

import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;



import java.util.ArrayList;
import java.lang.String;


public class MyScheduleList extends ListActivity
{
    private static final String TAG = "MyListView";
    
    public static final String TARGET_BOAT = "targetBoatIndex";
    
    public static final int CONTEXT_MENU_TARGET_BOAT = 0;
    
    private int mTextColor;
    private int mBackgroundColor;
    
    
    Bundle extras;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
    
        String[] li = new String[64];
    
        extras = getIntent().getExtras();
        if (extras != null)
        {
            li = extras.getString("theList").split(",");
            
            int k = Integer.parseInt( 
                    extras.getString("theTargetBoatIndex") );
            
            //..targetBoat could be null..
            if ( k > -1 )
            {
                li[ k ] = li[ k ] + " (TARGET)";
            }
            
            int i = Integer.parseInt( extras.getString("theIndex") );
            if ( i > -1 )
            {
                li[ i ] = li[ i ] + " <-- NEXT!";
            }
            
            setTitle( extras.getString("theTitle") );
        }
        else
        {
            li[0] = "err0r";
        }
        
        //..TODO.. make this more abstract..
        
        //..prefs..
        SharedPreferences mSharedPreferences = this.getSharedPreferences( 
            NextBoat.PREFERENCE_NAME, 
            Context.MODE_PRIVATE );
        
        //getColorFromSwitch
        
        final int zTextColorIndex = Integer.parseInt(
            mSharedPreferences.getString(  
                NextBoat.PREFERENCE_TEXT_COLOR,     
                NextBoat.DEFAULT_TEXT_COLOR )
        );
        
        final int zBackgroundColorIndex = Integer.parseInt(
            mSharedPreferences.getString(  
                NextBoat.PREFERENCE_BACKGROUND_COLOR,     
                NextBoat.DEFAULT_BACKGROUND_COLOR )
        );
        
        mTextColor =        NextBoat.getColorFromSwitch( zTextColorIndex  );
        mBackgroundColor =  NextBoat.getColorFromSwitch( zBackgroundColorIndex );
        
        setListAdapter( new MyAdapter( this, li ) );
            
        if (extras != null)
        {
            int i = Integer.parseInt( extras.getString("theIndex") );
            setSelection( i );
        }
    
        //.. http://www.anddev.org/creating_a_contextmenu_on_a_listview-t2438.html ..
        getListView().setOnCreateContextMenuListener( new OnCreateContextMenuListener() 
        {
            @Override
            public void onCreateContextMenu(
                    ContextMenu menu, 
                    View v,
                    ContextMenuInfo menuInfo ) 
            {

                // TODO Auto-generated method stub
                menu.setHeaderTitle("ContextMenu");
                menu.add(
                    Menu.NONE,
                    Menu.NONE, 
                    CONTEXT_MENU_TARGET_BOAT, 
                    R.string.context_menu_target_boat );
            } 
        });
    
    }
    
    /*
    see 
    http://groups.google.com/group/android-developers/browse_thread/thread/721ba12dbd20a023/1f726518277739e3?show_docid=1f726518277739e3
    */
    private class MyAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;
    
    
        private String[] mList = new String[64];
        
        MyAdapter( Context pContext, String[] pList)
        {
            mInflater = LayoutInflater.from( pContext );
            mList = pList.clone();
            
        }
        
        public int getCount()
        {
            return mList.length;
        }
        
        public Object getItem(int position) {
            return position;
        }
        
        public long getItemId(int position) {
            return position;
        }
        
        public View getView( 
                int position, 
                View convertView, 
                ViewGroup parent )
        {
            ViewHolder holder;
            
            if ( convertView  == null )
            {
                convertView = mInflater.inflate( R.layout.list_item, null );
                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById( R.id.id_list_item );
                
                holder.textView.setTextColor( mTextColor );
                holder.textView.setBackgroundColor( mBackgroundColor );
                
                convertView.setTag( holder );
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }
        
            holder.textView.setText( mList[ position ] );
            
            return convertView;
        
        }
        
        /*
        http://developer.android.com/resources/samples/ApiDemos/src/com/example/android/apis/view/List14.html
        */
        class ViewHolder 
        {
            TextView textView;
        }
    }
    
    
    
    @Override
    public boolean onContextItemSelected(MenuItem item) 
    {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) 
        {
            case CONTEXT_MENU_TARGET_BOAT:
                Intent iData = new Intent();
                iData.putExtra( 
                        TARGET_BOAT, 
                        Integer.toString( (int) info.id ) );
                
                setResult( 
                    android.app.Activity.RESULT_OK,
                    iData );
                finish();
                
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    
    //..for menu button (back)..
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_schedule_menu, menu);
        return true;
    }

    //..for menu button (back)..
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.id_go_back:
            //..return to parent activity..
            setFinish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void setFinish()
    {
        finish();
    }

}
