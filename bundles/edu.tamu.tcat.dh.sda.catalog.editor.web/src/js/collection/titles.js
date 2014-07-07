define(function (require) {

    var Backbone = require('backbone'),

        Title = require('js/model/title');


    var TitleCollection = Backbone.Collection.extend({

        model: Title

    });

    return TitleCollection;

});
