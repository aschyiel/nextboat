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
 
import android.app.AlertDialog;
import android.content.Context;

//..see droidwall HelpDialog as example..
public class BulletinDialog extends AlertDialog {
        protected BulletinDialog(Context context, String pMessage) {
                super(context);
                setMessage( pMessage );
                setButton("OK", (OnClickListener)null);
                setIcon(R.drawable.icon);
                setTitle("Ferry Alert Bulletin");
        }
}

