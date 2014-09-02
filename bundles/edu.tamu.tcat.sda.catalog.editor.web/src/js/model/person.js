define(function (require) {

    var Backbone = require('backbone'),
        Moment   = require('moment'),

        Config          = require('config'),
        Name            = require('js/model/name'),
        HistoricalEvent = require('js/model/historical_event'),
        NameCollection  = require('js/collection/names');


    var Person = Backbone.Model.extend({

        urlRoot: Config.apiPrefix + '/people',

        defaults: function () {
            return {
                id: null,
                names: new NameCollection([ new Name() ]),
                birth: new HistoricalEvent({ title: 'Date of birth' }),
                death: new HistoricalEvent({ title: 'Date of death' }),
                summary: ''
            };
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
        },

        getFormattedName: function () {
            var birth = Moment(this.get('birth').get('eventDate')),
                death = Moment(this.get('death').get('eventDate'));

            var formattedName = this.getFullName();

            if (birth.isValid() || death.isValid()) {
                formattedName += ' (';
                formattedName += birth.isValid() ? birth.format('YYYY') : '?';
                formattedName += '&ndash;';
                formattedName += death.isValid() ? death.format('YYYY') : '?';
                formattedName += ')';
            }

            return formattedName;
        },

        getFullName: function () {
            return this.getCanonicalName().getFullName();
        },

        getCanonicalName: function () {
            return this.get('names').at(0);
        }

    });

    return Person;

});
