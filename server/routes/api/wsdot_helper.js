//..routes/api/wsdot_helper.js, uly, aug2014..

/*jshint supernew:true, undef: true, node:true, laxcomma:true */

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
*/
function WsdotHelper( p )
{
  this.title = p.title;
  this.url   = p.url;
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
  fetch( helper.url, function( err, ___, body ) {
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
  var data = get_default_response( helper );
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
  return data;
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
  if ( is_tomorrow ) {
    d = new Date( d.getTime() + TWENTY_FOUR_HOURS );
  }
  return d;
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
         , 'url':   helper.url
         , 'date':  new Date
         , 'sailings': []
         };
}
