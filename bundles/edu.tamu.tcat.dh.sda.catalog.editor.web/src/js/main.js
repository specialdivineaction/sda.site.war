define(function (require) {

    var Backbone = require('backbone'),
        PeopleRouter = require('js/router/people'),
        WorksRouter = require('js/router/works');

    require('bootstrap');

    var peopleRouter = new PeopleRouter();
    var worksRouter = new WorksRouter();
    Backbone.history.start();

});
