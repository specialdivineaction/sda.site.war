define(function (require) {

    var Backbone = require('backbone');

    require('backbone.epoxy');

    var TitleSubform = Backbone.Epoxy.View.extend({
        template: require('tpl!templates/work/title_subform.html.ejs'),

        initialize: function (options) {
            this.allowRemoval = options.allowRemoval;
        },

        bindings: {
            'select.language': 'value:lg,events:["change"]',
            'input.title': 'value:title,events:["keyup"]',
            'input.subtitle': 'value:subtitle,events:["keyup"]'
        },

        events: {
            'click .remove-title': 'dispose'
        },

        render: function () {
            this.$el.html(this.template({
                model: this.model.toJSON(),
                languages: require('js/data/languages'),
                showDelete: this.allowRemoval
            }));

            this.applyBindings();

            return this;
        },

        dispose: function () {
            var _this = this;
            this.$el.slideUp(300, function () {
                _this.remove();
                _this.model.destroy();
            });
        }
    });

    return TitleSubform;

});
