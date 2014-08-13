define(function (require) {

    var Backbone = require('backbone');


    var Title = Backbone.Model.extend({

        defaults: {
            type: 'undefined',
            lg: 'en',
            title: '',
            subtitle: ''
        },

        getFullTitle: function () {
            var title = this.get('title');

            if (this.get('subtitle') !== '')
                title += ': ' + this.get('subtitle');

            return title;
        }

    });

    return Title;

});
