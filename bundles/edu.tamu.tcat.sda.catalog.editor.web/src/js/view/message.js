define(function (require) {

    var Backbone   = require('backbone'),
        $          = require('jquery'),
        _          = require('underscore'),

        StringUtil = require('js/util/string'),
        Timer      = require('js/model/timer'),

        RadialProgressIndicator = require('js/view/radial_progress');


    /**
     * MessageView class
     *
     * Displays a dismissible (or auto-dismissible) message to the user rendered as a Bootstrap alert.
     *
     * Usage:
     *
     *    new MessageView('Hello, world!').open()               displays "Hello, world!" in an Info message contained in the global message container element (defaults to 'body')
     *
     *    new MessageView({
     *        dismissible: true,
     *        message: 'Hello, world!',
     *        type: 'info',
     *    }).openIn('#container');                          displays same message above in a custom container element
     *
     *    var msg = new MessageView({
     *        dismissible: false,
     *        ttl: 5000,
     *        message: 'Hello, world!',
     *    });
     *    $('#container').append(msg.render().el);
     *    msg.open();                                       same as previous message, only auto-dismissed after 5 seconds.
     *
     *    MessageView.setContainer('#container');           all future messages will be rendered in '#container' by default.
     */
    var MessageView = Backbone.View.extend({

        messageContainer: $('body'),

        className: 'alert',

        attributes: {
            role: 'alert'
        },

        /**
         * Constructor: creates a new MessageView alert.
         *
         * @param {Object {
         *    admonition:  {String}  "Attention" text to display at start of message. Default: capitalized type value
         *    container:   {String|jQuery|HTMLElement} Selector / Element in which to display this message.
         *                                             Default: static MessageView message container
         *    dismissible: {Bool}    Control whether the close button is displayed. Default: true
         *    message:     {String}  REQUIRED. Alert message text.
         *    showTimer:   {Bool}    Control whether a countdown radial progress timer is displayed. Default: false
         *    ttl:         {Integer} Specify time (in milliseconds) before message is automatically dismissed
         *    type:        {String}  Type of alert to display. Can be one of: 'success', 'info', 'warning', 'danger',
         *                           and 'error' (aliased to 'danger'). Default: 'info'
         * }|String} options Configuration options.
         *                   If string is specfied, it becomes the message text and all default values are set.
         */
        initialize: function(options) {
            if (_.isString(options))
                options = {message: options};

            this.options = _.defaults(options, {
                // type technically may be anything, but recognized types are:
                // 'success', 'info', 'warning', 'danger', and 'error' (aliased to 'danger')
                type: 'info',
                dismissible: true,
                showTimer: false,
            });

            if (_.isUndefined(this.options.admonition) || _.isNull(this.options.admonition))
                this.options.admonition = StringUtil.capitalize(this.options.type);

            this.timer = this.options.ttl ? new Timer({ time: this.options.ttl }) : false;

            if (!this.options.ttl && !this.options.dismissible)
                console.error('Warning: alerts should either be dismissible or set to auto-expire.');

            if (!this.options.message)
                console.error('No message given');

            // allow container override
            if (!_.isUndefined(options.container) && !_.isNull(optoins.container))
                this.messageContainer = $(options.container);

            this.rendered = false;
        },

        /**
         * Assembles a Message DOM element and events for display in the document.
         * Provides support for traditional container.append(msg.render().el) Backbone paradigm.
         * NOTE: Message will not display until msg.open() is called.
         *
         * Using msg.open() or msg.openIn(container) is preferred.
         *
         * @return {MessageView} self-reference
         */
        render: function () {
            if (this.rendered)
                return this;

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

                // dispose this instance when alert is manually dismissed
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
                    this.$el.append(this.progressIndicator.render().el);
                }

                // dispose this instance when alert is automatically dismissed
                this.listenTo(this.timer, 'finish', function () {
                    _this.$el.fadeOut(1000, function () {
                        _this.close();
                    });
                });
            }

            this.rendered = true;

            return this;
        },

        /**
         * Renders the DOM element (if it hasn't been rendered already), displays the message, and
         * starts the auto-dismiss timer (if applicable)
         *
         * @param {Bool} autoAppend Whether to automatically append the rendered DOM element to
         *                          the message container. Default: false
         * @return {MessageView} self-reference
         */
        open: function (autoAppend) {
            if (!this.rendered)
                this.render();

            if (_.isUndefined(autoAppend) || _.isNull(autoAppend) || autoAppend)
                this.$el.appendTo(this.messageContainer);

            if (this.timer)
                this.timer.start();

            this.$el.fadeIn(300);

            return this;
        },

        /**
         * Sets the message container and opens the message in it.
         * This method automatically calls 'open(false)'
         *
         * @param {String|jQuery|HTMLElement} container Selector, jQuery object, or DOM element
         * @return {MessageView} self-reference
         */
        openIn: function (container) {
            this.$el.appendTo(container);
            return this.open(false);
        },

        /**
         * Disposes this Message instance.
         * NOTE: automatically called if auto-dismiss is enabled or message close button is clicked.
         */
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


    /**
     * Static method to set global message container for new MessageView instances
     *
     * @param {String|jQuery|HTMLElement} el New global container selector or element.
     */
    MessageView.setContainer = function (el) {
        MessageView.prototype.messageContainer = $(el);
    };


    return MessageView;

});
