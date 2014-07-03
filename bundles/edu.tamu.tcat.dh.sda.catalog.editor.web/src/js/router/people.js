define(function (require) {

    var Backbone = require('backbone'),
        $ = require('jquery'),

        PersonFormView = require('js/view/person/form'),
        Person = require('js/model/person');

    var PeopleRouter = Backbone.Router.extend({
        routes: {
            'people/new': 'newAction'
        },

        newAction: function () {
            var form = new PersonFormView({
                model: new Person(),
                router: this
            });

            $('#page-title').text('Add Person');
            $('#content').html(form.render().el);
        }
    });

    return PeopleRouter;

});
