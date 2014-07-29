define(function (require) {

    var Backbone = require('backbone');


    var Title = Backbone.Model.extend({

        defaults: {
            type: 'undefined',
            lg: 'en',
            title: '',
            subtitle: ''
        }

    });

    return Title;

});
