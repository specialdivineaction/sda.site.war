define(function (require) {

    var Backbone = require('backbone'),
        $ = require('jquery'),

        PersonFormView = require('js/view/person/form'),
        Person = require('js/model/person');

    var PeopleRouter = Backbone.Router.extend({
        routes: {
            'people/add': 'addPerson'
        },


        addPerson: function () {
            var form = new PersonFormView({
                model: new Person()
            });

            $('.container').html(form.render().el);
        }
    });

    return PeopleRouter;

});
