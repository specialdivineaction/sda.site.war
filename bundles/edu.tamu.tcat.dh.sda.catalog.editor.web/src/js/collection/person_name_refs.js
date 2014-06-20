define(function (require) {

    var Backbone = require('backbone');

    var PersonNameRef = require('js/model/person_name_ref');

    var PersonNameRefCollection = Backbone.Collection.extend({

        model: PersonNameRef

    });

    return PersonNameRefCollection;
});
