define(function (require) {

    var Backbone = require('backbone'),
        Moment   = require('moment'),

        NameSubform            = require('js/view/person/name_subform'),
        HistoricalEventSubform = require('js/view/person/historical_event_subform'),
        Message                = require('js/view/message');

    // attach epoxy to backbone
    require('backbone.epoxy');

    var PersonFormView = Backbone.Epoxy.View.extend({
        tagName: 'form',

        template: require('tpl!templates/person/form.html.ejs'),

        clearAfterSave: false,

        bindings: {
            '.person-id': 'value:id',
            '.summary': 'value:summary,events:["keyup"]',
        },

        events: {
            'submit': 'submit',
            'click .save-new-button': function (evt) { this.clearAfterSave = true; }
        },

        submit: function (evt) {
            evt.preventDefault();

            var _this = this;
            this.model.save({}, {
                success: function (model, response, options) {
                    var alert = new Message({
                        type: 'success',
                        ttl: 5000,
                        message: 'Person saved successfully.'
                    });

                    alert.render();

                    if (_this.clearAfterSave) {
                        console.log('save and new');
                    }
                },
                error: function (model, response, options) {
                    var alert = new Message({
                        type: 'error',
                        message: 'Unable to save person.'
                    });

                    alert.render();
                }
            });

            return false;
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
            this.$el.find('#deathForm').html(deathSubForm.render().el);

            this.applyBindings();

            return this;
        }
    });

    return PersonFormView;

});
