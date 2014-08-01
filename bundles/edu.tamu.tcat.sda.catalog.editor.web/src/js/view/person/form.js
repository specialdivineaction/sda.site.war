define(function (require) {

    var Backbone = require('backbone'),
        Moment   = require('moment'),
        _        = require('underscore'),

        NameSubform            = require('js/view/person/name_subform'),
        HistoricalEventSubform = require('js/view/person/historical_event_subform'),
        Message                = require('js/view/message');

    // attach epoxy to backbone
    require('backbone.epoxy');


    var PersonFormView = Backbone.Epoxy.View.extend({

        tagName: 'form',

        template: require('tpl!templates/person/form.html.ejs'),

        initialize: function (options) {
            this.router = options.router;
            this.childViews = [];

            this.listenTo(this.model, 'sync', this.render);
        },

        bindings: {
            '.person-id': 'value:id',
            '.summary': 'value:summary,events:["keyup"]',
        },

        events: {
            'submit': 'submit',
            'click .save-new-button': function (evt) { this.submit(evt, true); }
        },

        submit: function (evt, saveAndNew) {
            evt.preventDefault();

            var _this = this;
            this.model.save({}, {
                success: function (model, response, options) {
                    var alert = new Message({
                        type: 'success',
                        ttl: 5000,
                        message: 'Person saved successfully.'
                    });

                    alert.render().open();

                    if (saveAndNew) {
                        _this.close();
                        _this.router.newAction();
                    }
                },
                error: function (model, response, options) {
                    var alert = new Message({
                        type: 'error',
                        message: 'Unable to save person.'
                    });

                    alert.render().open();
                }
            });

            return false;
        },

        render: function () {
            this.$el.html(this.template({ model: this.model }));

            var _this = this;

            var $nameForms = this.$('.name-forms').empty();
            this.model.get('names').each(function (name) {
                var subForm = new NameSubform({ model: name });
                _this.childViews.push(subForm);
                $nameForms.append(subForm.render().el);
            });

            var birthSubForm = new HistoricalEventSubform({ model: this.model.get('birth') });
            this.childViews.push(birthSubForm);
            this.$('.birth-form').html(birthSubForm.render().el);

            var deathSubForm = new HistoricalEventSubform({ model: this.model.get('death') });
            this.childViews.push(deathSubForm);
            this.$('.death-form').html(deathSubForm.render().el);

            this.applyBindings();

            return this;
        },

        close: function () {
            this.remove();
            this.unbind();
            _.each(this.childViews, function (v) {
                if (v.close) v.close();
            });
        }

    });

    return PersonFormView;

});
