define(function (require) {

    var Backbone = require('backbone'),
        $        = require('jquery'),

        PeopleRouter = require('js/router/people'),
        WorksRouter  = require('js/router/works'),
        SidebarView  = require('js/view/sidebar');

    require('bootstrap');

    var peopleRouter = new PeopleRouter();
    var worksRouter = new WorksRouter();
    Backbone.history.start();

    var sidebarView = new SidebarView();
    $('#sidebar').html(sidebarView.render().el);

});
