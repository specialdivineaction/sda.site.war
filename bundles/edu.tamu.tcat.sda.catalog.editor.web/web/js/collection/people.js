define(function (require) {

    var Backbone = require('backbone'),

        Config = require('js/config'),
        Person = require('js/model/person');


    var PersonCollection = Backbone.Collection.extend({

        url: Config.apiPrefix + '/people',

        model: Person

    });

    return PersonCollection;

});
