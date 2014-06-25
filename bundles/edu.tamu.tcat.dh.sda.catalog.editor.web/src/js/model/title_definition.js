define(function (require) {

    var Backbone = require('backbone'),

        Title           = require('js/model/title'),
        TitleCollection = require('js/collection/titles');

    var TitleDefinition = Backbone.Model.extend({
        defaults: {
            canonicalTitle: new Title({ type: 'default' }),
            shortTitle: new Title({ type: 'short' }),
            alternateTitles: new TitleCollection()
        },

        parse: function (resp) {
            resp.canonicalTitle = new Title(resp.canonicalTitle, {parse: true});
            resp.shortTitle = new Title(resp.shortTitle, {parse: true});
            resp.alternateTitles = new TitleCollection(resp.alternateTitles, {parse: true});

            return resp;
        }
    });

    return TitleDefinition;

});
