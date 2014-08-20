//..routes/api/bainbridge-seattle.js, uly, aug2014..

/*jshint supernew:true, undef: true, node:true, laxcomma:true */

/**
* Returns JSON representing the ferry schedule
* going from-bainbridge-to-seattle.
*/

//
// mods
//

var express = require( 'express' )
  , router  = express.Router()
  , cheerio = require( 'cheerio' )
  , _       = require( 'lodash' )

    // Request is kind of a confusing name within the node context.
  , fetch   = require( 'request' )
  ;
module.exports = router;

//
// consts
//

var SCHEDULE_URL = 'http://www.wsdot.wa.gov/Ferries/Schedule/ScheduleDetail.aspx?departingterm=3&arrivingterm=7';

/** Frequently used regex @see get_time */
var TIMEISH     = /\d?\d:\d\d/;
var MIDNIGHTISH = /midnight/i;
var NOONISH     = /noon/i;

/** @see interprete_am */
var AMISH       = /am/i;
var PMISH       = /pm/i;

var TITLE = 'Leaving Bainbridge Island';

var DEFAULT_RESPONSE = { something_went_wrong: true, title: TITLE };

/** one day in msec. */
var TWENTY_FOUR_HOURS = 24 * 60 * 60 * 1000;

//
// public
//

router.get( '/', function( req, res ) {
  fetch( SCHEDULE_URL, function( err, res2, body ) {
    if ( err ) {
      res.json( _.extend( {}, DEFAULT_RESPONSE, { date: new Date } ) );
      throw err;
    }
    res.json( as_data( cheerio.load( body ) ) );
  });
});

//
// private
//

/**
* Snatch up the ferry-data from the WSDOT site.
*
* @param {Cheerio} $ The thing that wraps the html-body content; faux-jquery.
* @return {Map} The schedule-listing.
*/
function as_data( $ ) {
  var data = { sailings: [] };
  var is_am = true;    // Changes as it reads in teh AM/PM sub-headings.
  var ampm_flips = 0
    , tmp_is_am = is_am
    , is_tomorrow = false
    ;

  _.each( $( '.schedgrid tr' ), function( el ) {
    var time = get_time( $( '.schedgriddataleft', el ).text(), !is_am, is_tomorrow );
    if ( time ) {
      var vessel = $( '.schedgriddatamiddle a', el ).text();
      data.sailings.push( { at: time, vessel: vessel } );
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

  data.date  = new Date;
  data.title = TITLE;
  return data;
}

/**
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
    // TODO: log err.
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
  if ( is_tomorrow ) {
    d = new Date( d.getTime() + TWENTY_FOUR_HOURS );
  }
  return d;
}
