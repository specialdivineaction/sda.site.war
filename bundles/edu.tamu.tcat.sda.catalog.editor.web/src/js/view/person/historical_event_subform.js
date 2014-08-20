define(function (require) {

    var Backbone = require('backbone'),
        Moment   = require('moment');

    // attach epoxy to backbone
    require('backbone.epoxy');

    // attach dateValue handler to epoxy
    require('js/util/epoxy/handlers/date_value');


    var HistoricalEventSubform = Backbone.Epoxy.View.extend({

        template: require('tpl!templates/person/historical_event_subform.html.ejs'),

        bindings: {
            'input.event-date': 'dateValue:eventDate,events:["blur"]',
            'input.location': 'value:location,events:["keyup"]'
        },

        render: function () {
            this.$el.html(this.template({
                model: this.model.toJSON(),
            }));

            this.applyBindings();

            return this;
        },

        close: function () {
            this.remove();
            this.unbind();
        }

    });

    return HistoricalEventSubform;

});
