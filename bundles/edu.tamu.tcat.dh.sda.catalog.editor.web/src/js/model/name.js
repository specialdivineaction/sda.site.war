define(function (require) {

    var Backbone = require('backbone');

    var PersonNameRef = Backbone.Model.extend({
        defaults: {
            title: '',
            name: '',
            givenName: '',
            middleName: '',
            familyName: '',
            suffix: '',
            displayName: ''
        }
    });

    return PersonNameRef;

});
