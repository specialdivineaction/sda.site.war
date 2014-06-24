define(function (require) {

    var Backbone = require('backbone'),
        Moment   = require('moment');

    // attach epoxy to backbone
    require('backbone.epoxy');


    var PersonNameRefFormView = Backbone.Epoxy.View.extend({
        template: require('tpl!templates/person/name_ref_subform.html.ejs'),

        bindings: {
            'input.name': 'value:name,events:["keyup"]',
            'input.title': 'value:title,events:["keyup"]',
            'input.given-name': 'value:givenName,events:["keyup"]',
            'input.middle-name': 'value:middleName,events:["keyup"]',
            'input.family-name': 'value:familyName,events:["keyup"]',
            'input.suffix': 'value:suffix,events:["keyup"]'
        },

        render: function () {
            this.$el.html(this.template(this.model.toJSON()));
            this.applyBindings();
            return this;
        }
    });


    var HistoricalEventFormView = Backbone.Epoxy.View.extend({
        template: require('tpl!templates/person/historical_event_subform.html.ejs'),

        bindings: {
            'input.event-date': 'dateValue:eventDate,events:["blur"]',
            'input.location': 'value:location,events:["keyup"]'
        },

        bindingHandlers: {
            dateValue: {
                set: function ($el, modelValue) {
                    var m = Moment(modelValue);
                    if (m.isValid()) {
                        $el.val(m.format('MM/DD/YYYY'));
                    }
                },
                get: function ($el, oldValue, evt) {
                    $el.parent().removeClass('has-error');

                    var newValue = $el.val();
                    if (newValue === '') return null;

                    var m = Moment(newValue, 'MM/DD/YYYY');
                    if (m.isValid()) {
                        return m.toISOString();
                    } else {
                        $el.parent().addClass('has-error');
                        return null;
                    }
                }
            }
        },

        render: function () {
            this.$el.html(this.template({
                model: this.model.toJSON(),
                moment: Moment
            }));
            this.applyBindings();
            return this;
        }
    });


    var PersonFormView = Backbone.Epoxy.View.extend({
        tagName: 'form',

        template: require('tpl!templates/person/form.html.ejs'),

        bindings: {
            '.person-id': 'value:id',
            '.summary': 'value:summary,events:["keyup"]',
        },

        events: {
            'submit': function (evt) {
                evt.preventDefault();
                this.model.save();
                return false;
            }
        },

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

            this.applyBindings();

            return this;
        }
    });

    return PersonFormView;

});
