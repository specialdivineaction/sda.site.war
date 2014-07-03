define(function (require) {

    var Backbone = require('backbone');


    var PersonNameRef = Backbone.Model.extend({

        defaults: {
            title: '',
            givenName: '',
            middleName: '',
            familyName: '',
            suffix: '',
            displayName: ''
        }

    });

    return PersonNameRef;

});
