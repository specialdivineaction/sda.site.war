define(function (require) {

    var Backbone = require('backbone');

    require('backbone.epoxy');

    var AuthorRefSubform = Backbone.Epoxy.View.extend({
        template: require('tpl!templates/work/author_ref_subform.html.ejs'),

        initialize: function (options) {
            this.allowRemoval = options.allowRemoval;
        },

        bindings: {
            '.name': 'value:name,events:["keyup"]',
            '.role': 'value:role,events:["keyup"]'
        },

        events: {
            'click .remove-author-ref': 'dispose'
        },

        render: function () {
            this.$el.html(this.template({
                model: this.model.toJSON(),
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

    return AuthorRefSubform;

});
