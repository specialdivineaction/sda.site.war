define(function (require) {

    var Backbone = require('backbone'),
        Moment   = require('moment');

    // attach epoxy to backbone
    require('backbone.epoxy');

    var HistoricalEventSubform = Backbone.Epoxy.View.extend({
        template: require('tpl!templates/person/historical_event_subform.html.ejs'),

        bindings: {
            'input.event-date': 'dateValue:eventDate,events:["blur"]',
            'input.location': 'value:location,events:["keyup"]'
        },

        bindingHandlers: {
            dateValue: {
                set: function ($el, modelValue) {
                    var m = Moment(modelValue);
                    if (m.isValid()) {
                        $el.val(m.format('YYYY-MM-DD'));
                    }
                },
                get: function ($el, oldValue, evt) {
                    $el.parent().removeClass('has-error');

                    var newValue = $el.val();
                    if (newValue === '') return null;

                    var m = Moment(newValue);
                    $el.siblings('.help-block').text(m.format('ddd, MMM D, YYYY'));

                    if (m.isValid()) {
                        return m.toISOString();
                    } else {
                        $el.parent().addClass('has-error');
                        return null;
                    }

                }
            }
        },

        render: function () {
            this.$el.html(this.template({
                model: this.model.toJSON(),
                moment: Moment
            }));

            this.applyBindings();

            return this;
        }
    });

    return HistoricalEventSubform;

});
