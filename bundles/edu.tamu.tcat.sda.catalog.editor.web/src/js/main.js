define(function (require) {

    var Backbone = require('backbone'),
        $        = require('jquery'),

        PagesRouter  = require('js/router/pages'),
        PeopleRouter = require('js/router/people'),
        WorksRouter  = require('js/router/works'),
        SearchForm   = require('js/view/search_form'),

        Message      = require('js/view/message'),
        Config       = require('js/config');

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
        search: function (query) {
            return new Promise(function (resolve, reject) {
                if (query === '') {
                    reject(new Error('No query given.'));
                    return;
                }

                $.getJSON(Config.apiPrefix + '/works', { title: query }, function (data) {

                    console.log(data);

                    // TODO: convert data to result array
                    resolve(data.map(function (work) {
                        return {
                            html: 'TITLE',
                            href: '#works/WORK_ID'
                        };
                    }));
                }).fail(function (jqxhr, status, errorMessage) {
                    reject(new Error(errorMessage));
                });
            });
        }
    });
    $('#sidebar #books .search-form').html(bookSearchForm.render().el);

    var peopleSearchForm = new SearchForm({
        type: 'people',
        search: function (query) {
            return new Promise(function (resolve, reject) {
                if (query === '') {
                    reject(new Error('No query given.'));
                    return;
                }

                $.getJSON(Config.apiPrefix + '/people', { lastName: query }, function (data) {

                    console.log(data);

                    // TODO: convert data to result array
                    resolve(data.map(function (person) {
                        return {
                            html: 'NAME',
                            href: '#people/PERSON_ID'
                        };
                    }));
                }).fail(function (jqxhr, status, errorMessage) {
                    reject(new Error(errorMessage));
                });
            });
        }
    });
    $('#sidebar #people .search-form').html(peopleSearchForm.render().el);

});
