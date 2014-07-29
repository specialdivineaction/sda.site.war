define(function (require) {

    var Backbone = require('backbone'),

        languages = require('js/data/languages');

    require('backbone.epoxy');


    var TitleSubform = Backbone.Epoxy.View.extend({

        template: require('tpl!templates/work/title_subform.html.ejs'),

        initialize: function (options) {
            this.allowRemoval = options.allowRemoval;
        },

        bindings: {
            // 'select.language': 'value:lg,events:["change"]',
            'input.title': 'value:title,events:["keyup"]',
            'input.subtitle': 'value:subtitle,events:["keyup"]',
            'select.role': 'value:type,events:["change"]'
        },

        events: {
            'click .remove-title': 'disposeForm'
        },

        render: function () {
            this.$el.html(this.template({
                model: this.model.toJSON(),
                languages: languages,
                showDelete: this.allowRemoval
            }));

            this.applyBindings();

            return this;
        },

        disposeForm: function () {
            var _this = this;
            this.$el.slideUp(300, function () {
                _this.close();
                _this.model.destroy();
            });
        },

        close: function () {
            this.remove();
            this.unbind();
        }

    });

    return TitleSubform;

});
