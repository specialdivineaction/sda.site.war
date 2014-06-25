define(function (require) {

    var Backbone = require('backbone');

    var DateDescription = Backbone.Model.extend({
        defaults: {
            display: '',
            value: null
        }
    });

    return DateDescription;

});
