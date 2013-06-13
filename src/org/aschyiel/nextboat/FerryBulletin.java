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
 
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;
import java.io.File;
import android.content.Context;
import android.os.Environment;
import java.util.ArrayList;
 
 
public class FerryBulletin
{
    public FerryBulletin( Context pContext )
    {
        mContext = pContext;
    }

    
    private static final String TAG = "FerryBulletin";
    private final Context mContext;
    public static final String BULLETIN_URL = "http://www.wsdot.wa.gov/ferries/schedule/bulletin.aspx";
    public static final String BULLETIN_FILE_NAME = "NextBoat_bulletin.html";

    public void DownloadBulletin()
    {
        DownloadWebPage zDownloadWebPage = 
                new DownloadWebPage( mContext, BULLETIN_URL, getBulletinFile() );
        zDownloadWebPage.start();
    }
    
    private static File getBulletinFile()
    {
        File file = new File( 
                Environment.getExternalStorageDirectory(), 
                BULLETIN_FILE_NAME );
        return file;
    }
    
    private static String getHtmlPara( String pString )
    {
        int i = pString.indexOf("<p>");
        int j = pString.indexOf("</p>");
        return pString.substring( i+3 , j );
    }
    
    //..show all..
    public static String readBulletin()
    {
        //..match any character, thus shows all..
        return readBulletin( "." );
    }
    
    /** mark where to put \n characters */
    public static String END_PARA = "qweasdzxc123!@#";
    
    /** to replace <br> */
    public static String BREAK = "zxcasdqwe321#@!";
    
    public static String readBulletin( String pRegex )
    {
        String htmlString = DownloadWebPage.ReadFile( getBulletinFile() );
        
        if (htmlString.length() < 15)
        {
            return "";
        }
        
        int firstIndex = htmlString.indexOf("<p>");
        int lastIndex = htmlString.lastIndexOf("</p>");
        htmlString = htmlString.substring( firstIndex, (lastIndex + 4) );
    
    
        //
        //..create a list of html <p></p>..
        //
        ArrayList<String> li = new ArrayList<String>();
        while (firstIndex > -1)
        {
            firstIndex = htmlString.indexOf("<p>");
            if ( firstIndex > -1 )
            {
                lastIndex = htmlString.indexOf("</p>", firstIndex);
                String c = htmlString.substring( firstIndex, lastIndex ) + END_PARA;
                c = c.replaceAll( "<br>", BREAK );
                c = c.replaceAll( "<br />", BREAK ); //..doh!
                li.add( c );
                htmlString = htmlString.substring( lastIndex, htmlString.length() );
            }
        }
        
        
        String zResult = new String();
        Pattern pattern = Pattern.compile( pRegex );   
              
        for (int i=0; i < li.size(); i++)
        {
            boolean b = false;
            Matcher matcher = pattern.matcher( li.get(i) );
            while ( matcher.find() )
            {
                if ( matcher.group() != null )
                {
                    b = true;
                }
            }
            
            if (b)
            {
                zResult += li.get( i );
            }
        }
        zResult = correctHref( zResult );
        zResult = removeHtmlTags( zResult );
        zResult = correctWhiteSpace( zResult );
        zResult = zResult.replaceAll( END_PARA, "\n\n" );
        zResult = zResult.replaceAll( BREAK, "\n" );
        return zResult;
    }
    
    private static String correctWhiteSpace( String pString )
    {
        //..greedy white space..
        String zRegex = "\\s+";
        Matcher matcher = Pattern.compile( zRegex ).matcher( pString );
        String zString = new String();
        while (matcher.find())
        {
            //..replace with a single white space..
            zString = matcher.replaceAll( " " );
        }
        return zString;
    }
    
    private static String correctHref( String pString )
    {
        //..<a href="www.foo.org" class="content_text"> --> www.foo.org ..
        String zRegex = "<a href=\"(.*?)\".*?class=\"content_text\".*?>";
        String zString = pString;
        Matcher matcher = Pattern.compile( zRegex ).matcher( zString );
        String zUrl = new String();
        
        while ( matcher.find() )
        {
            zUrl = "( "+ matcher.group(1) +" ) ";
            zString = matcher.replaceAll( zUrl );
        }
        
        return zString;
    }
    
    //..http://www.osherove.com/blog/2003/5/13/strip-html-tags-from-a-string-using-regular-expressions.html
    private static String removeHtmlTags( String pString )
    {
        //..group1 non-greedy html tag..
        //..group2 greedy rest of match..
        String zRegex = "(<(?:.|\n)*?>)";
        Pattern pattern = Pattern.compile( zRegex );
        Matcher matcher = pattern.matcher( pString );
        String zString = new String();
        while ( matcher.find() )
        {
            zString = matcher.replaceAll( "" );
        }
        return zString;
        
        
    }
    
    

}
