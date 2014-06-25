define(function (require) {

    var Backbone = require('backbone'),

        AuthorRef           = require('js/model/author_ref'),
        TitleDefinition     = require('js/model/title_definition'),
        PublicationInfo     = require('js/model/publication_info'),
        AuthorRefCollection = require('js/collection/author_refs');

    var Work = Backbone.Model.extend({
        urlBase: '/api/works',

        defaults: {
            id: null,
            authors: new AuthorRefCollection([new AuthorRef()]),
            title: new TitleDefinition(),
            otherAuthors: new AuthorRefCollection(),
            pubInfo: new PublicationInfo(),
            series: '',
            summary: ''
        },

        parse: function (resp) {
            resp.authors = new AuthorRefCollection(resp.authors, {parse: true});
            resp.title = new TitleDefinition(resp.title, {parse: true});
            resp.otherAuthors = new AuthorRefCollection(resp.otherAuthors, {parse: true});
            resp.pubInfo = new PublicationInfo(resp.pubInfo, {parse: true});

            return resp;
        }
    });

    return Work;

});
