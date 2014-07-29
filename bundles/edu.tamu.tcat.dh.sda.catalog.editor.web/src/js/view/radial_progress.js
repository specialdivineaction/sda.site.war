define(function (require) {

    var Backbone = require('backbone'),
        $        = require('jquery'),
        _        = require('underscore');

    var RadialProgressIndicator = Backbone.View.extend({

        initialize: function (options) {
            this.listenTo(this.model, 'tick', this.updateValue);

            this.options = _.defaults(options, {
                filled: true
            });
        },

        updateValue: function () {
            var percent = this.model.get('percent'),
                $slice  = this.$el.find('.slice');

            if (percent > 0.5)
                $slice.addClass('gt50');

            var deg = 360 * percent;
            $slice.find('.pie').css({
                '-webkit-transform': 'rotate(' + deg + 'deg)',
                '-moz-transform': 'rotate(' + deg + 'deg)',
                '-ms-transform': 'rotate(' + deg + 'deg)',
                '-o-transform': 'rotate(' + deg + 'deg)',
                'transform': 'rotate(' + deg + 'deg)'
            });
        },

        render: function () {
            this.$el.addClass('radial-progress');

            var $slice = $('<div>', {class: 'slice'}).appendTo(this.$el);
            $('<div>', {class: 'pie'}).appendTo($slice);
            $('<div>', {class: 'pie fill'}).appendTo($slice);

            // override styles
            if (this.options.size) {
                this.$el.css('font-size', this.options.size + 'px');
            }

            if (this.options.filled) {
                this.$el.addClass('filled');
                if (this.options.color)
                    this.$el.find('.pie').css('background-color', this.options.color);
            } else {
                if (this.options.color)
                    this.$el.find('.pie').css('border-color', this.options.color);

                if (this.options.thickness)
                    this.$el.find('.pie').css('border-width', this.options.thickness + 'em');
            }

            this.updateValue();

            return this;
        },

        close: function () {
            this.remove();
            this.unbind();
        }

    });

    return RadialProgressIndicator;

});
