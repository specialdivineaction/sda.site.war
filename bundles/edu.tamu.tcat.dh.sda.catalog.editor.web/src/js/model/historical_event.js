define(function (require) {

    var Backbone = require('backbone'),
        _        = require('underscore');

    var HistoricalEvent = Backbone.Model.extend({

        defaults: {
            id: null,
            title: null,
            description: null,
            location: null,
            eventDate: null
        },

        set: function () {
            Backbone.Model.prototype.set.apply(this, arguments);
            this._convertEventDate();
        },

        parse: function (resp) {
            resp.eventDate = new Date(resp.eventDate);

            return resp;
        },

        _convertEventDate: function () {
            var date = this.get('eventDate');

            if (_.isString(date)) {
                date = new Date(date);
            }

            if (date instanceof Date) {
                this.set('eventDate', date.getTime());
            }

            console.log('date is now ' + this.get('eventDate'));
        }

    });

    return HistoricalEvent;

});
