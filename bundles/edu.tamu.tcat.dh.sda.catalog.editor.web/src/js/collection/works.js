define(function (require) {

    var Backbone = require('backbone'),

        Config = require('js/config'),
        Work   = require('js/model/work');


    var WorkCollection = Backbone.Collection.extend({

        url: Config.apiPrefix + '/works',

        model: Work

    });

    return WorkCollection;

});
