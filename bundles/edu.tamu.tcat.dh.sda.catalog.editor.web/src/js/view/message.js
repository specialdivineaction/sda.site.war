define(function (require) {

    var Backbone   = require('backbone'),
        $          = require('jquery'),
        StringUtil = require('js/util/string');

    var MessageView = Backbone.View.extend({
        initialize: function(options) {
            // type may be anything, but recognized types are:
            // 'success', 'info', 'warning', 'danger', and 'error' (aliased to 'danger')
            this.type = options.type || 'info';
            this.message = options.message;
            this.admonition = (typeof options.admonition === 'undefined') ? StringUtil.capitalize(this.type) : options.admonition;
            this.dismissible = (typeof options.dismissible === 'undefined') ? true : options.dismissible;
            this.ttl = options.ttl || false;
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
                    _this.remove();
                });
            }

            if (this.ttl) {
                this.timeout = setTimeout(function () {
                    _this.$el.fadeOut(1000, function () {
                        _this.remove();
                    });
                }, this.ttl);
            }

            this.$el.fadeIn(300);

            return this;
        },
    });

    return MessageView;

});
