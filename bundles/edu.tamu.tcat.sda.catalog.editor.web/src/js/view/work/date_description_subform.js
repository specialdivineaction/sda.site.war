define(function (require) {

    var Backbone = require('backbone'),
        Moment   = require('moment'),

        inferMoment = require('js/util/moment/parse');

    // attach epoxy to backbone
    require('backbone.epoxy');

    // attach dateValue handler to epoxy
    require('js/util/epoxy/handlers/date_value');


    var DateDescriptionSubform = Backbone.Epoxy.View.extend({

        template: require('tpl!templates/work/date_description_subform.html.ejs'),

        bindings: {
            '.date-value': 'dateValue:value,events:["blur"]',
            '.display': 'value:display,events:["keyup"]'
        },

        events: {
            'blur .date-value': function (evt) { this.valueSetAutomatically = !this.model.get('value'); },
            'keyup .display': 'inferDate'
        },

        inferDate: function (evt) {
            var m = inferMoment($(evt.target).val());
            if (!m.isValid()) {
                return;
            }

            if (!this.model.get('value') || this.valueSetAutomatically) {
                this.model.set('value', m.toISOString());
                this.valueSetAutomatically = true;
            }
        },

        render: function () {
            this.$el.html(this.template({
                model: this.model.toJSON()
            }));

            this.applyBindings();

            return this;
        },

        close: function () {
            this.remove();
            this.unbind();
        }

    });

    return DateDescriptionSubform;

});
