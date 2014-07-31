define(function (require) {

    var Backbone   = require('backbone'),
        $          = require('jquery'),
        _          = require('underscore'),

        StringUtil = require('js/util/string'),
        Timer      = require('js/model/timer'),

        RadialProgressIndicator = require('js/view/radial_progress');


    var MessageView = Backbone.View.extend({

        messageContainer: $('body'),

        className: 'alert',

        attributes: {
            role: 'alert'
        },

        initialize: function(options) {
            this.options = _.defaults(options, {
                // type technically may be anything, but recognized types are:
                // 'success', 'info', 'warning', 'danger', and 'error' (aliased to 'danger')
                type: 'info',
                dismissible: true,
                showTimer: true,
            });

            if (typeof this.options.admonition === 'undefined' || this.options.admonition === null)
                this.options.admonition = StringUtil.capitalize(this.options.type);

            this.timer = this.options.ttl ? new Timer({ time: this.options.ttl }) : false;

            if (!this.options.ttl && !this.options.dismissible)
                console.error('Warning: alerts should either be dismissible or set to auto-expire.');

            if (!this.options.message)
                console.error('No message given');

            // allow container override
            if (typeof options.container !== 'undefined') this.messageContainer = $(options.container);
        },

        render: function () {
            var type = (this.options.type === 'error') ? 'danger' : this.options.type;
            var _this = this;

            var admonition = (this.options.admonition) ? '<strong>' + this.options.admonition + ':</strong> ' : '';

            this.$el
                .addClass('alert-' + type)
                .html(admonition + this.options.message)
                .hide()
            ;

            if (this.options.dismissible) {
                $('<button>', {'type': 'button', 'class': 'close', 'data-dismiss': 'alert'})
                    .append($('<span>', {'aria-hidden': 'true', 'html': '&times;'}))
                    .append($('<span>', {'class': 'sr-only', 'text': 'Close'}))
                    .appendTo(this.$el)
                ;

                this.$el.on('closed.bs.alert', function (evt) {
                    _this.close();
                });
            }

            if (this.timer) {
                if (this.options.showTimer) {
                    this.progressIndicator = new RadialProgressIndicator({
                        model: this.timer,
                        filled: false,
                        size: 24,
                        thickness: 0.2
                    });
                    this.$el.append(this.progressIndicator.render().$el);
                }

                this.listenTo(this.timer, 'finish', function () {
                    _this.$el.fadeOut(1000, function () {
                        _this.close();
                    });
                });
            }

            return this;
        },

        open: function (autoAppend) {
            if (typeof autoAppend === 'undefined' || autoAppend === null || autoAppend)
                this.$el.appendTo(this.messageContainer);

            if (this.timer)
                this.timer.start();

            this.$el.fadeIn(300);

            return this;
        },

        openIn: function (container) {
            this.$el.appendTo(container);
            return this.open(false);
        },

        close: function () {
            this.remove();
            this.unbind();

            if (this.timer) {
                this.timer.destroy();

                if (this.progressIndicator)
                    this.progressIndicator.close();
            }
        }

    });

    MessageView.setContainer = function (el) {
        MessageView.prototype.messageContainer = $(el);
    };

    return MessageView;

});
