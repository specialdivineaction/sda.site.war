define(function (require) {

    var Backbone = require('backbone'),

        Name = require('js/model/name');


    var NameCollection = Backbone.Collection.extend({

        model: Name

    });

    return NameCollection;

});
