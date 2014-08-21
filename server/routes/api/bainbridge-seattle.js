//..routes/api/bainbridge-seattle.js, uly, aug2014..

/*jshint supernew:true, undef: true, node:true, laxcomma:true */

/**
* Returns JSON representing the ferry schedule
* going from-bainbridge-to-seattle.
*/

var router      = require( 'express' ).Router()
  , WsdotHelper = require( './wsdot_helper' )
  ;
module.exports = router;

router.get( '/', function( req, res ) {
  var charon = new WsdotHelper(
    { url:   'http://www.wsdot.wa.gov/Ferries/Schedule/ScheduleDetail.aspx?departingterm=3&arrivingterm=7'
    , title: 'Leaving Bainbridge Island'
    , week:  req.query.week
    });
  charon.fetch( function( schedule ) {
        res.json( schedule );
      });
});
