define(function (require) {

    var Backbone = require('backbone'),

        DateDescription = require('js/model/date_description');

    var PublicationInfo = Backbone.Model.extend({
        defaults: function () {
            return {
                publisher: '',
                place: '',
                date: new DateDescription()
            };
        },

        parse: function (resp) {
            resp.date = new DateDescription(resp.date, {parse: true});

            return resp;
        }
    });

    return PublicationInfo;

});
