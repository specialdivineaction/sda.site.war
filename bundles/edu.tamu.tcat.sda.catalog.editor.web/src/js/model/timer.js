define(function (require) {

    var Backbone = require('backbone');

    require('backbone.epoxy');

    var Timer = Backbone.Epoxy.Model.extend({

        initialize: function (options) {
            this.interval = null;
        },

        defaults: {
            currentValue: 0,
            tick: 50
        },

        computeds: {
            percent: {
                deps: ['currentValue', 'time'],
                get: function (currentValue, time) {
                    return currentValue / time;
                }
            }
        },

        start: function () {
            if (null === this.interval) {
                var _this = this;
                this.interval = setInterval(function () { _this.tick(); }, this.get('tick'));
                this.trigger('start', this);
            }
        },

        stop: function () {
            if (null !== this.interval) {
                clearInterval(this.interval);
                this.interval = null;
                this.trigger('stop', this);
            }
        },

        tick: function () {
            this.set('currentValue', Math.min(this.get('time'), this.get('currentValue') + this.get('tick')));
            this.trigger('tick', this);

            if (this.get('currentValue') == this.get('time')) {
                this.stop();
                this.trigger('finish', this);
            }
        }

    });

    return Timer;

});
