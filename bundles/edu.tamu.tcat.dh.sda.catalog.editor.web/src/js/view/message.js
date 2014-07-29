define(function (require) {

    var Backbone   = require('backbone'),
        $          = require('jquery'),

        StringUtil = require('js/util/string'),
        Timer      = require('js/model/timer'),

        RadialProgressIndicator = require('js/view/radial_progress');


    var MessageView = Backbone.View.extend({

        messageContainer: $('body'),

        initialize: function(options) {
            // type may be anything, but recognized types are:
            // 'success', 'info', 'warning', 'danger', and 'error' (aliased to 'danger')
            this.type = options.type || 'info';
            this.message = options.message;
            this.admonition = (typeof options.admonition === 'undefined') ? StringUtil.capitalize(this.type) : options.admonition;
            this.dismissible = (typeof options.dismissible === 'undefined') ? true : options.dismissible;
            this.timer = options.ttl ? new Timer({ time: options.ttl }) : false;

            if (!this.timer && !this.dismissible) {
                console.error('Warning: alerts should either be dismissible or set to auto-expire.');
            }

            // allow container override
            if (typeof options.container !== 'undefined') this.messageContainer = $(options.container);
        },

        render: function () {
            var type = (this.type === 'error') ? 'danger' : this.type;
            var _this = this;

            this.$el
                .attr('class', 'alert alert-' + type)
                .attr('role', 'alert')
                .append($('<strong>').html(this.admonition + ':'))
                .append(' ' + this.message)
                .hide()
            ;

            if (this.dismissible) {
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
                if (this.showTimer) {
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

                this.timer.start();
            }

            $(this.messageContainer).append(this.$el);

            this.$el.fadeIn(300);

            return this;
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
