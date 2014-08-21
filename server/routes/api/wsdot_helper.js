//..routes/api/wsdot_helper.js, uly, aug2014..

'use strict';

/*jshint supernew:true, undef: true, node:true, laxcomma:true, expr:true */

/**
* The wsdot-helper/utility class.
*
* A mechanism for reusing WSDOT ferry schedule related stuff
* regarding a single direction.
*/

/**
* @constructor
* @param {*} p The named key/value parameters.
*              - url, the wsdot page we're scraping.
*              - title, the appropriate label.
*              - week {String} optional, values: day|end;
*                  overrides the default behavior of looking up
*                  by today's schedule.
*/
function WsdotHelper( p )
{
  this.title = p.title;
  this.url   = p.url;

  /** Optional @type {String} day|end */
  this.week  = p.week || null;
}
var fn = WsdotHelper.prototype;
module.exports = WsdotHelper;

//---------------------------------
//
// mods
//
//---------------------------------

var cheerio = require( 'cheerio' )
  , _       = require( 'lodash' )

    // Request is kind of a confusing name within the node context.
  , fetch   = require( 'request' )
  ;

//---------------------------------
//
// constz
//
//---------------------------------

/** Frequently used regex @see get_time */
var TIMEISH     = /\d?\d:\d\d/
  , MIDNIGHTISH = /midnight/i
  , NOONISH     = /noon/i

    /** @see interprete_am */
  , AMISH       = /am/i
  , PMISH       = /pm/i

  , WEEKENDISH  = /(sat)|(sun)/i

    /** one day in msec. */
  , TWENTY_FOUR_HOURS = 24 * 60 * 60 * 1000
  ;

//---------------------------------
//
// public
//
//---------------------------------

/**
* Fetch the ferry-schedule data, asynchronously.
*
* @public
* @param {Closure} cb The thing that consumes the schedule-data
*                     we're scraping together.
* @return void
*/
fn.fetch = function( cb ) {
  var helper = this;
  fetch( get_url( helper ), function( err, ___, body ) {
        if ( err ) {
          console.error( err );
          cb( as_error( helper, err.toString() ) );
        }
        cb( as_data( helper, cheerio.load( body ) ) );
      });
};

//---------------------------------
//
// private
//
//---------------------------------

/**
* Returns true if a given date is currently representing a weekend.
*
* @private
* @static
* @param {Date} d Optional
* @return {boolean}
*/
function is_weekend( d ) {
  d = d || new Date;
  return WEEKENDISH.test( d.toString() );
}

/**
* Returns the appropriate WSDOT url.
*/
function get_url( helper ) {
  var url = helper.url
    , d   = new Date
    , is_weekday_today = !is_weekend( d )
    ;

  //
  // Allow looking up schedules for NOT today.
  //

  if ( helper.week ) {
    if ( 'end' === helper.week &&  is_weekday_today ) {
      while ( !is_weekend( d ) ) {
        as_tomorrow( d );
      }
    } else if ( 'day' === helper.week && !is_weekday_today ) {
      // TODO: DRY?
      while ( is_weekend( d ) ) {
        as_tomorrow( d );
      }
    }
    url += '&tripdate=' + as_tripdate( d );
  }

  return url;
}

/**
* Formats a date to be a valid WSDOT tripdate url-param.
* ie. yyyymmdd or some such.
*
* For example: tripdate=20140820
*
* @private
* @static
* @param {Date} d
* @return {String}
*/
function as_tripdate( d ) {
  var z = zero_padded;
  return d.getFullYear()    +
      z( 1 + d.getMonth() ) +    // GOTCHA: Off by one, ie. august as "7".
      z( d.getDate() );
}

/**
* Puts a zero to the left of an integer if it needs it.
*
* @private
* @static
* @param {int} n
* @return {String|Number}
*/
function zero_padded( n ) {
  return ( n < 10 )?
      '0'+ n : n;
}

/**
* Creates an "error" response.
*
* @private
* @param {WsdotHelper} helper
* @param {String} msg
*/
function as_error( helper, msg ) {
  return _.extend( get_default_response( helper ), { error: msg } );
}

/**
* Snatch up the ferry-data from the WSDOT site.
*
* @private
* @param {WsdotHelper} helper is our instance.
* @param {Cheerio} $ The thing that wraps the html-body content; faux-jquery.
* @return {Map} The schedule-listing.
*/
function as_data( helper, $ ) {
  var rows = $( '.schedgrid tr' );
  if ( !rows.length ) {
    return as_error( helper,
                     'Invalid WSDOT results, '+
                     'perhaps our url or selectors are no longer good?' );
  }

  var ampm_flips  = 0
    , is_am       = true    // Changes as it reads in teh AM/PM sub-headings.
    , tmp_is_am   = is_am
    , is_tomorrow = false
    , sailings = []
    ;

  _.each( rows, function( el ) {
    var time = get_time( $( '.schedgriddataleft', el ).text(), !is_am, is_tomorrow );
    if ( time ) {
      var vessel = $( '.schedgriddatamiddle a', el ).text() || 'TBD';
      sailings.push( { at: time, vessel: vessel } );
    } else {
      tmp_is_am = interprete_am( $( '.schedgridsubheading', el ).text(), is_am );
      if ( tmp_is_am !== is_am ) {
        is_am = tmp_is_am;
        ampm_flips++;
      }
      if ( !is_tomorrow && ampm_flips > 1 ) {
        is_tomorrow = true;
      }
    }
  });
  return _.extend( get_default_response( helper ), { sailings: sailings } );
}

/**
* @private
* @static
* @param {String} ampm aka "AM" or "PM".
* @param {Boolean} b The current value as the default.
* @returns the default value passed in, otherwise reads from the HTML.
*/
function interprete_am( ampm, b ) {
  if ( !ampm ) {
    return b;
  }
  if ( AMISH.test( ampm ) ) {
    b = true;
  } else if ( PMISH.test( ampm ) ) {
    b = false;
  } else {
    console.warn( 'Don\'t know how to read AM/PM value of :', ampm );
  }
  return b;
}

/**
* Prefixes for quickly putting together a javaScript date.
* ie. date-part + whitespace + time-part
*/
var date_prefix = (new Date).toDateString() +" ";

/**
* Returns the interpreted time-string.
*
* @private
* @static
* @param {String} s is the time-string, usually in HH:MM,
*                   but can also be Midnight/noon.
* @param {Boolean} is_pm, optional.  Defaults to assuming everything
*                         is in the morning-context.
* @param {Boolean} is_tomorrow, optional. When true, will add 24 hours.
* @return {Date}
*/
function get_time( s, is_pm, is_tomorrow ) {
  var ts = null;
  if ( TIMEISH.test( s ) ) {
    ts = s;
  } else if ( NOONISH.test( s ) ) {
    ts = '12:00';
  } else if ( MIDNIGHTISH.test( s ) ) {
    ts = '00:00';
  }
  if ( !ts ) {
    return null;
  }
  // TODO: UTC ?
  var d = new Date( date_prefix + ts );

  // Corner-case for noon vs. midnight.
  if ( is_pm && d.getHours() < 11 ) {
    d.setHours( d.getHours() + 12 );
  } else if ( !is_pm && d.getHours() > 11 ) {
    d.setHours( d.getHours() - 12 );    // TODO: DRY?
  }
  is_tomorrow && as_tomorrow( d );
  return d;
}

/**
* Add 24 hours to the given date-time in-place.
*
* @private
* @static
* @param {Date} d
* @return void
*/
function as_tomorrow( d ) {
  d.setTime( d.getTime() + TWENTY_FOUR_HOURS );
}

/**
* Returns the default response.
* ie. something bad happened.
*
* @private
* @param {WsdotHelper} helper instance.
* @return {*}
*/
function get_default_response( helper ) {
  return { 'title': helper.title
         , 'url':   get_url( helper )
         , 'date':  new Date
         , 'sailings': []
         , 'as_weekend': 'end' === helper.week || ( null === helper.week && is_weekend() )
         };
}
