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

                    $el.parent().removeClass('has-error has-success has-feedback');
                    $el.siblings('.form-control-feedback').remove();

                    if (m.isValid()) {
                        $el.val(m.format('YYYY-MM-DD'));
                        $el.siblings('.help-block').text(m.format('ddd, MMM D, YYYY'));
                    }
                },
                get: function ($el, oldValue, evt) {
                    var parent = $el.parent();

                    parent.removeClass('has-error has-success has-feedback').find('.form-control-feedback').remove();

                    var newValue = $el.val();
                    if (newValue === '') return null;

                    var m = Moment(newValue);
                    $el.siblings('.help-block').text(m.format('ddd, MMM D, YYYY'));

                    if (m.isValid()) {
                        parent.addClass('has-success has-feedback');
                        $('<span>', {class: 'glyphicon glyphicon-ok form-control-feedback'}).appendTo(parent);
                        return m.toISOString();
                    } else {
                        parent.addClass('has-error has-feedback');
                        $('<span>', {class: 'glyphicon glyphicon-remove form-control-feedback'}).appendTo(parent);
                        return null;
                    }
                }
            }
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
