define(function (require) {

    var Backbone = require('backbone'),
        Moment   = require('moment');

    require('backbone.epoxy');


    var INFER_FORMATS = [
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
        'YYYY-M-D',

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

        // year
        'YYYY'
    ];


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

        bindingHandlers: {
            dateValue: {
                set: function ($el, modelValue) {
                    var m = Moment(modelValue);

                    $el.parent()
                        .removeClass('has-error has-success has-feedback')
                        .find('.form-control-feedback').remove();

                    if (m.isValid()) {
                        $el.val(m.format('YYYY-MM-DD'));
                        $el.siblings('.help-block').text(m.format('ddd, MMM D, YYYY'));
                    }
                },
                get: function ($el, oldValue, evt) {
                    var parent = $el.parent();

                    parent.removeClass('has-error has-success has-feedback');
                    $el.siblings('.form-control-feedback').remove();

                    var newValue = $el.val();
                    if (newValue === '') return null;

                    var m = Moment(newValue);
                    $el.siblings('.help-block').text(m.format('ddd, MMM D, YYYY'));

                    if (m.isValid()) {
                        parent.addClass('has-feedback has-success');
                        $('<span>', {class: 'glyphicon glyphicon-ok form-control-feedback'}).appendTo(parent);
                        return m.toISOString();
                    } else {
                        parent.addClass('has-feedback has-error');
                        $('<span>', {class: 'glyphicon glyphicon-remove form-control-feedback'}).appendTo(parent);
                        return null;
                    }
                }
            }
        },

        inferDate: function (evt) {
            var m = Moment($(evt.target).val(), INFER_FORMATS);
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
