define(function (require) {

    var Backbone = require('backbone'),
        Moment   = require('moment');

    require('backbone.epoxy');


    var DateDescriptionSubform = Backbone.Epoxy.View.extend({

        template: require('tpl!templates/work/date_description_subform.html.ejs'),

        bindings: {
            '.date-value': 'dateValue:value,events:["blur"]',
            '.display': 'value:display,events:["keyup"]'
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
