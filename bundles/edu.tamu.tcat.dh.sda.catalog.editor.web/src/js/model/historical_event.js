define(function (require) {

    var Backbone = require('backbone');


    var HistoricalEvent = Backbone.Model.extend({

        defaults: {
            id: null,
            title: null,
            description: null,
            location: null,
            eventDate: null
        }

    });

    return HistoricalEvent;

});
