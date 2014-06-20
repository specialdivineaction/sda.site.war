define(function (require) {

    var Backbone = require('backbone'),
        Handlebars = require('handlebars');

    Handlebars.registerHelper('date', require('js/util/date_format'));


    var modelFormBind = function (cb) {
        return function (evt) {
            evt.stopPropagation();

            var target = this.$(evt.target),
                attribute = target.attr('name'),
                value = target.val(),
                attrs = {};

            attrs[attribute] = value;
            if (cb) attrs = cb(attrs);

            this.model.set(attrs);
        };
    };


    var PersonNameRefFormView = Backbone.View.extend({
        template: require('hb!templates/name_ref_subform.html.hbs'),

        render: function () {
            this.$el.html(this.template(this.model.toJSON()));
            return this;
        },

        events: {
            'blur input': modelFormBind(function (attrs) {
                for (attr in attrs) console.log('setting PersonNameRef::' + attr + ' to ' + attrs[attr]);
                return attrs;
            })
        }
    });


    var HistoricalEventFormView = Backbone.View.extend({
        template: require('hb!templates/historical_event_subform.html.hbs'),

        render: function () {
            this.$el.html(this.template(this.model.toJSON()));
            return this;
        },

        events: {
            'blur input, textarea': modelFormBind(function (attrs) {
                for (attr in attrs) console.log('setting HistoricalEvent::' + attr + ' to ' + attrs[attr]);
                return attrs;
            })
        }
    })


    var PersonFormView = Backbone.View.extend({
        tagName: 'form',

        template: require('hb!templates/person_form.html.hbs'),

        render: function () {
            this.$el.html(this.template({ model: this.model }));

            var $personNameRefForms = this.$el.find('#personNameRefForms').empty();
            this.model.get('names').each(function (personNameRef) {
                var subForm = new PersonNameRefFormView({ model: personNameRef });
                $personNameRefForms.append(subForm.render().el);
            });

            var birthSubForm = new HistoricalEventFormView({ model: this.model.get('birth') });
            this.$el.find('#birthForm').html(birthSubForm.render().el);

            var deathSubForm = new HistoricalEventFormView({ model: this.model.get('death') });
            this.$el.find('#deathForm').html(deathSubForm.render().el)

            return this;
        },

        events: {
            'submit': 'submit',
            'blur input, textarea': modelFormBind(function (attrs) {
                for (attr in attrs) console.log('setting Person::' + attr + ' to ' + attrs[attr]);
                return attrs;
            })
        },

        submit: function (evt) {
            evt.preventDefault();

            this.model.save();

            return false;
        }

    });

    return PersonFormView;

});
