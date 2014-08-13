define(function (require) {

    var Backbone = require('backbone'),
        $        = require('jquery'),

        PersonFormView = require('js/view/person/form'),
        Person         = require('js/model/person'),

        Message = require('js/view/message');


    var PeopleRouter = Backbone.Router.extend({

        routes: {
            'people/new': 'newAction',
            'people/:id': 'editAction'
        },

        newAction: function () {
            var form = new PersonFormView({
                model: new Person(),
                router: this
            });

            $('#page-title').text('Add Person');
            $('#content').html(form.render().el);
        },

        editAction: function (id) {
            var person = new Person({ id: id });

            person.fetch({
                error: function (model, response, options) {
                    var msg = new Message({
                        type: 'error',
                        message: 'Unable to load person with ID: <code>' + id + '</code>'
                    });

                    msg.render().open();
                }
            });

            var form = new PersonFormView({
                model: person,
                router: this
            });

            $('#page-title').text('Edit Person');
            $('#content').html(form.render().el);
        }

    });

    return PeopleRouter;

});
