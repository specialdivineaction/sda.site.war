define(function (require) {

    var Backbone = require('backbone'),
        $        = require('jquery'),

        PagesRouter  = require('js/router/pages'),
        PeopleRouter = require('js/router/people'),
        WorksRouter  = require('js/router/works'),
        SearchForm   = require('js/view/search_form'),

        Message      = require('js/view/message');

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
                if (query === '') resolve([]);

                // HACK: replace with something a little more ajaxy
                var results = [];
                for (var i = 0; i < 10; i++) {
                    results.push({
                        content: '<strong>Result ' + i + '</strong><br/>Lorem ipsum dolor sit amet...',
                    });
                }

                resolve(results);
            });
        }
    });
    $('#sidebar #books .search-form').html(bookSearchForm.render().el);

    var peopleSearchForm = new SearchForm({ type: 'people' });
    peopleSearchForm.on('search', function (query) {
        console.log('search for person ' + query);
    });
    $('#sidebar #people .search-form').html(peopleSearchForm.render().el);

});
