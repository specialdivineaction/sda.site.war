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

        parse: function (resp) {
            resp.eventDate = new Date(resp.eventDate);

            return resp;
        }

    });

    return HistoricalEvent;

});
