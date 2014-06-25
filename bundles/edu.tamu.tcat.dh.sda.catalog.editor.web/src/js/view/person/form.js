define(function (require) {

    var Backbone = require('backbone'),
        Moment   = require('moment'),

        NameSubform            = require('js/view/person/name_subform'),
        HistoricalEventSubform = require('js/view/person/historical_event_subform');

    // attach epoxy to backbone
    require('backbone.epoxy');

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

            var $nameForms = this.$el.find('.name-forms').empty();
            this.model.get('names').each(function (name) {
                var subForm = new NameSubform({ model: name });
                $nameForms.append(subForm.render().el);
            });

            var birthSubForm = new HistoricalEventSubform({ model: this.model.get('birth') });
            this.$el.find('#birthForm').html(birthSubForm.render().el);

            var deathSubForm = new HistoricalEventSubform({ model: this.model.get('death') });
            this.$el.find('#deathForm').html(deathSubForm.render().el)

            this.applyBindings();

            return this;
        }
    });

    return PersonFormView;

});
