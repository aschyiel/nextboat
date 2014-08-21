//..routes/api/bainbridge-seattle.js, uly, aug2014..

/*jshint supernew:true, undef: true, node:true, laxcomma:true */

/**
* Leaving Seattle, Going to Bainbridge.
*/

var router      = require( 'express' ).Router()
  , WsdotHelper = require( './wsdot_helper' )
  ;
module.exports = router;

router.get( '/', function( req, res ) {
  var charon = new WsdotHelper(
    { url:   'http://www.wsdot.wa.gov/Ferries/Schedule/ScheduleDetail.aspx?departingterm=7&arrivingterm=3'
    , title: 'Leaving Seattle'
    , week:  req.query.week
    });
  charon.fetch( function( schedule ) {
        res.json( schedule );
      });
});
