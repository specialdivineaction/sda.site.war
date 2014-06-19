define(function (require) {

    var Backbone = require('backbone');

    var PersonForm = Backbone.View.extend({

        template: require('hb!templates/person_form.html.hbs'),

        render: function () {
            this.$el.html(this.template(this.model.toJSON()));
            return this;
        },

        events: {
            'submit #authorForm': 'submit'
        },

        submit: function (evt) {
            evt.preventDefault();

            console.log('saving person to database');

            return false;
        }

    });

    return PersonForm;

});
