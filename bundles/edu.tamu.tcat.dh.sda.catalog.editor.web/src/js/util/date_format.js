/*
 * Copyright (C) 2010 Matthew J. Barry
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

define(['js/util/ordinal', 'js/util/pad'], function (ord, pad) {
    var MONTH, REGEXP, WEEKDAY, ord, pad, isLeapYear;

    var REGEXP = /(\\\w|[ABDF-IL-PS-UWYZac-eg-jl-or-uwyz])/g,
        MONTH = ['January','February','March','April','May','June','July','August','September','October','November','December'],
        WEEKDAY = ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];

    function isLeapYear(y) { return y%400 == 0 || (y%100 != 0 && y%4 == 0); }

    // see http://php.net/manual/en/function.date.php for more documentation
    function format(d, f) {
        if (typeof d === 'undefined' || d === null || !(d instanceof Date)) return '';

        return f.replace(REGEXP, function(symb) {
            var d2, diff, jan, jul, stdOffset, swatch, yrStart;

            switch (symb) {
                case 'd':  // day of month with leading zero (01..31)
                    return pad(d.getDate(), 2);
                case 'D':  // short, 3-letter weekday name
                    return WEEKDAY[d.getDay()].substr(0, 3);
                case 'j':  // day of month (1..31)
                    return d.getDate();
                case 'l':  // full weekday name
                    return WEEKDAY[d.getDay()];
                case 'N':  // ISO-8601 day of week (1=Monday, 7=Sunday)
                    return (d.getDay() + 6) % 7 + 1;
                case 'S':  // ordinal for day of month ('st', 'nd', 'rd', 'th', etc.)
                    return ord(d.getDate());
                case 'w':  // weekday number (0=Sunday, 6=Saturday)
                    return d.getDay();
                case 'z':  // day of year (0..365)
                    return Math.ceil((d - new Date(d.getFullYear(), 0, 1)) / 86400000);
                case 'W':  // ISO-8601 week number of year (1..52)
                    yrStart = new Date(d.getFullYear(), 0, 1);
                    return Math.ceil(((d - yrStart) / 86400000 + yrStart.getDay() + 1) / 7);
                case 'F':  // full month name
                    return MONTH[d.getMonth()];
                case 'm':  // month number with leading zero (01..12)
                    return pad(d.getMonth() + 1, 2);
                case 'M':  // short, 3-letter month name
                    return MONTH[d.getMonth()].substr(0, 3);
                case 'n':  // month number (1..12)
                    return d.getMonth() + 1;
                case 't':  // number of days in current month
                    switch (d.getMonth()) {
                        case 1:
                            return isLeapYear(d.getFullYear()) ? 29 : 28;
                        case 3: case 5: case 8: case 10:
                            return 30;
                        default:
                            return 31;
                    }
                case 'L':  // leap year (1=leap year, 0=regular year)
                    return isLeapYear(d.getFullYear()) ? 1 : 0;
                case 'o':  // ISO-8601 year number (based on ISO-8601 week number)
                    d2 = new Date(d.valueOf());
                    d2.setDate(d2.getDate() - (d.getDay() + 6) % 7 + 3);
                    return d2.getFullYear();
                case 'Y':  // full, 4-digit year
                    return d.getFullYear();
                case 'y':  // short, 2-digit year
                    return d.getFullYear() % 100;
                case 'a':  // ante-/post-meridian, lowercase
                    return d.getHours() < 12 ? 'am' : 'pm';
                case 'A':  // ante-/post-meridian, uppercase
                    return format(d, 'a').toUpperCase();
                case 'B':  // internet swatch time (000..999)
                    swatch = (d.getUTCHours() + 1) % 24 + d.getUTCMinutes() / 60 + d.getUTCSeconds() / 3600;
                    return pad(Math.floor(swatch * 1000 / 24), 3);
                case 'g':  // 12-hour format (1..12)
                    return (d.getHours() + 11) % 12 + 1;
                case 'G':  // 24-hour format (0..23)
                    return d.getHours();
                case 'h':  // 12-hour with leading zero (01..12)
                    return pad(format(d, 'g'), 2);
                case 'H':  // 24-hour with leading zero (00..23)
                    return pad(d.getHours(), 2);
                case 'i':  // minutes with leading zero (00..59)
                    return pad(d.getMinutes(), 2);
                case 's':  // seconds with leading zero (00..59)
                    return pad(d.getSeconds(), 2);
                case 'u':  // milliseconds with leading zeros (000..999)  NOTE: PHP returns usec, but JS supports only msec
                    return pad(d.getMilliseconds(), 3);
                case 'e':  // timezone identifier
                    return '';
                case 'I':  // daylight saving time (1=DST, 0=standard)
                    jan = new Date(d.getFullYear(), 0, 1);
                    jul = new Date(d.getFullYear(), 6, 1);
                    return d.getTimezoneOffset() < Math.max(jan.getTimezoneOffset(), jul.getTimezoneOffset()) ? 1 : 0;
                case 'O':  // difference to GMT in hours (e.g. +0200)
                    diff = Math.floor(d.getTimezoneOffset() / 60);
                    return diff < 0 ? "-" + (pad(-diff, 2)) + "00" : "+" + (pad(diff, 2)) + "00";
                case 'P':  // difference to GMT in hrs:mins (e.g. +02:00)
                    diff = Math.floor(d.getTimezoneOffset() / 60);
                    return diff < 0 ? "-" + (pad(-diff, 2)) + ":00" : "+" + (pad(diff, 2)) + ":00";
                case 'T':  // timezone abbreviation
                    return '';
                case 'Z':  // timezone offset in seconds (-43200..50400)
                    return d.getTimezoneOffset() * 60;
                case 'c':  // ISO-8601 date
                    return format(d, 'Y-m-d\\TH:i:sP');
                case 'r':  // RFC-2822 date
                    return format(d, 'D, j M Y H:i:s O');
                case 'U':  // seconds since Unix Epoch (1 Jan 1970 00:00:00 GMT)
                    return Math.floor(d.getTime() / 1000).toString();
                default:  // pass-through for escaped chars
                    return symb.substr(1);
            }
        });
    }

    return format;
});
