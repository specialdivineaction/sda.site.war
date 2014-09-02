define(function (require) {

    var Backbone = require('backbone'),
        $        = require('jquery'),

        PagesRouter  = require('js/router/pages'),
        PeopleRouter = require('js/router/people'),
        WorksRouter  = require('js/router/works'),
        SearchForm   = require('js/view/search_form'),

        PersonCollection = require('js/collection/people'),
        WorkCollection   = require('js/collection/works'),

        Message = require('js/view/message'),
        Config  = require('config');

    require('bootstrap');


    // set message container
    Message.setContainer('#messages');

    // routers
    var pagesRouter = new PagesRouter();
    var peopleRouter = new PeopleRouter();
    var worksRouter = new WorksRouter();
    Backbone.history.start();

    // global UI elements

    var bookSearchForm = new SearchForm({
        type: 'books',
        collection: new WorkCollection(),

        transform: function (work) {
            return {
                html: work.getFormattedTitle(),
                href: '#works/' + work.id
            };
        },

        search: function (query) {
            return new Promise(function (resolve, reject) {
                if (query === '') {
                    reject(new Error('No query given.'));
                    return;
                }

                $.getJSON(Config.apiPrefix + '/works', { title: query }, function (data) {
                    resolve(data);
                }).fail(function (jqxhr, status, errorMessage) {
                    reject(new Error(errorMessage));
                });
            });
        }
    });

    bookSearchForm.collection.fetch({reset: true});

    $('#sidebar #books .search-form').html(bookSearchForm.render().el);



    var peopleSearchForm = new SearchForm({
        type: 'people',
        collection: new PersonCollection(),

        transform: function (person) {
            return {
                html: person.getFormattedName(),
                href: '#people/' + person.id
            };
        },

        search: function (query) {
            return new Promise(function (resolve, reject) {
                if (query === '') {
                    reject(new Error('No query given.'));
                    return;
                }

                $.getJSON(Config.apiPrefix + '/people', { lastName: query }, function (data) {
                    resolve(data);
                }).fail(function (jqxhr, status, errorMessage) {
                    reject(new Error(errorMessage));
                });
            });
        }
    });

    peopleSearchForm.collection.fetch({reset: true});

    $('#sidebar #people .search-form').html(peopleSearchForm.render().el);

});
