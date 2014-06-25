define(function (require) {

    var Backbone = require('backbone'),

        AuthorRef = require('js/model/author_ref');

    var AuthorRefCollection = Backbone.Collection.extend({
        model: AuthorRef
    });

    return AuthorRefCollection;

});
