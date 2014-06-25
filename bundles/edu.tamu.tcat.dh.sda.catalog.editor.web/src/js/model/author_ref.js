define(function (require) {

    var Backbone = require('backbone');

    var AuthorRef = Backbone.Model.extend({
        defaults: {
            authorId: null,
            name: '',
            role: ''
        }
    });

    return AuthorRef;

});
