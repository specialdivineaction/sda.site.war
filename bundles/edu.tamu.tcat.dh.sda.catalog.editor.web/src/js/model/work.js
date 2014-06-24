define(function (require) {

    var Backbone = require('backbone');

    var Work = Backbone.Model.extend({
        urlBase: '/api/works',

        defaults: {
            id: null,
            // authors
            // title
            // otherAuthors
            // pubInfo
            series: '',
            summary: ''
        }
    });

    return Work;

});
