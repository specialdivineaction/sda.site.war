define(function (require) {

    var Backbone = require('backbone');

    var PagesRouter = Backbone.Router.extend({

        routes: {
            '': 'home'
        },

        home: function () {
            var template = require('tpl!templates/pages/home.html.ejs');
            $('#content').html(template());
        }

    });

    return PagesRouter;
});
