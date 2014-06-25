define(function (require) {

    var Backbone = require('backbone'),

        Work = require('js/model/work');

    var WorkCollection = Backbone.Collection.extend({
        url: '/api/works',
        model: Work
    });

    return WorkCollection;

});
