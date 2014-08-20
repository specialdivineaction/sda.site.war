define(function (require) {

    var Moment = require('moment');


    var INFER_FORMATS = [
        // year
        'YYYY',

        // month year
        'MMMM-YYYY',
        'MMM-YYYY',
        'MM-YYYY',
        'M-YYYY',

        // year month
        'YYYY-MMMM',
        'YYYY-MMM',
        'YYYY-MM',
        'YYYY-M',

        // month day year
        'MMMM-DD-YYYY',
        'MMM-DD-YYYY',
        'MM-DD-YYYY',
        'M-DD-YYYY',
        'MMMM-D-YYYY',
        'MMM-D-YYYY',
        'MM-D-YYYY',
        'M-D-YYYY',

        // day month year
        'DD-MMMM-YYYY',
        'DD-MMM-YYYY',
        'DD-MM-YYYY',
        'DD-M-YYYY',
        'D-MMMM-YYYY',
        'D-MMM-YYYY',
        'D-MM-YYYY',
        'D-M-YYYY',

        // year month day
        'YYYY-MM-DD',
        'YYYY-M-D'
    ];


    function inferDate(str) {
        return Moment(str, INFER_FORMATS);
    }

    return inferDate;
});
