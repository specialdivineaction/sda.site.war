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
        },

        getFullName: function () {
            return this.get('familyName') + ', ' + this.get('givenName') + ' ' + this.get('middleName');
        }

    });

    return PersonNameRef;

});
