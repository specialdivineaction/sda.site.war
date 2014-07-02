define(function (require) {

    var Backbone = require('backbone'),
        $        = require('jquery'),

        PagesRouter  = require('js/router/pages'),
        PeopleRouter = require('js/router/people'),
        WorksRouter  = require('js/router/works'),
        SearchForm   = require('js/view/search_form'),

        Message      = require('js/view/message');

    require('bootstrap');

    // routers
    var pagesRouter = new PagesRouter();
    var peopleRouter = new PeopleRouter();
    var worksRouter = new WorksRouter();
    Backbone.history.start();

    // global UI elements
    var bookSearchForm = new SearchForm({ type: 'books' });
    bookSearchForm.on('search', function (query) {
        console.log('search for book ' + query);
    });
    $('#sidebar #books .search-form').html(bookSearchForm.render().el);

    var peopleSearchForm = new SearchForm({ type: 'people' });
    peopleSearchForm.on('search', function (query) {
        console.log('search for person ' + query);
    });
    $('#sidebar #people .search-form').html(peopleSearchForm.render().el);

});
