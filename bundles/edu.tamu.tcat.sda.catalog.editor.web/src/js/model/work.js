define(function (require) {

    var Backbone = require('backbone'),

        Config              = require('config'),
        AuthorRef           = require('js/model/author_ref'),
        Title               = require('js/model/title'),
        TitleCollection     = require('js/collection/titles'),
        PublicationInfo     = require('js/model/publication_info'),
        AuthorRefCollection = require('js/collection/author_refs');


    var Work = Backbone.Model.extend({

        urlRoot: Config.apiPrefix + '/works',

        defaults: function () {
            return {
                id: null,
                authors: new AuthorRefCollection([new AuthorRef({ role: 'author' })]),
                titles: new TitleCollection([
                    new Title({ type: 'short' }),
                    new Title({ type: 'canonical' })
                ]),
                otherAuthors: new AuthorRefCollection(),
                pubInfo: new PublicationInfo(),
                series: '',
                summary: ''
            };
        },

        parse: function (resp) {
            resp.authors = new AuthorRefCollection(resp.authors, {parse: true});
            resp.titles = new TitleCollection(resp.titles, {parse: true});
            resp.otherAuthors = new AuthorRefCollection(resp.otherAuthors, {parse: true});
            resp.pubInfo = new PublicationInfo(resp.pubInfo, {parse: true});

            return resp;
        },

        getFormattedTitle: function () {
            return this.getFullTitle() + ' by ' + this.get('authors').map(function (a) { return a.get('name'); }).join(', ');
        },

        getFullTitle: function () {
            return this.getCanonicalTitle().getFullTitle();
        },

        getCanonicalTitle: function () {
            return this.get('titles').findWhere({ type: 'canonical' });
        }
    });

    return Work;

});
