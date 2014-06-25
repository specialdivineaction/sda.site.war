define(function (require) {

    var Backbone = require('backbone');

    var PersonNameRef = require('js/model/person_name_ref'),
        HistoricalEvent = require('js/model/historical_event'),
        NameCollection = require('js/collection/person_name_refs');

    var Person = Backbone.Model.extend({

        urlRoot: '/api/people',

        defaults: {
            id: null,
            names: new NameCollection([ new PersonNameRef() ]),
            birth: new HistoricalEvent({ title: 'Date of birth' }),
            death: new HistoricalEvent({ title: 'Date of death' }),
            summary: ''
        },

        /**
         * Modifies the server's response to conform to the model's expected
         * attribute fields and types. The resulting object will be passed to
         * the "set" method.
         *
         * @param {Object} resp The raw XHR response data
         * @return {Object} modified JSON attributes that will be set on the model
         */
        parse: function (resp) {
            // recursively call "parse" on the contained JSON models
            resp.names = new NameCollection(resp.names, {parse: true});
            resp.birth = new HistoricalEvent(resp.birth, {parse: true});
            resp.death = new HistoricalEvent(resp.death, {parse: true});

            return resp;
        }

    });

    return Person;

});
