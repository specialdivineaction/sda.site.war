define(function (require) {

    var Backbone = require('backbone');

    var Person = Backbone.Model.extend({

        urlRoot: '/api/people'

    });

    return Person;

});
