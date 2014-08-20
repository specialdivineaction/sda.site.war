define(function (require) {

    var Backbone = require('backbone'),
        Moment   = require('moment'),

        inferMoment = require('js/util/moment/parse');

    require('backbone.epoxy');

    var dateValueHandler = {
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

            var m = inferMoment(newValue);
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
    };

    Backbone.Epoxy.binding.addHandler('dateValue', dateValueHandler);

    return dateValueHandler;
});
