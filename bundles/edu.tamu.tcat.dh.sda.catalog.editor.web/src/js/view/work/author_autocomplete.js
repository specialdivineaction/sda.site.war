define(function (require) {

    var Backbone = require('backbone');


    var AutocompleteItemView = Backbone.View.extend({
        tagName: 'li',

        events: {
            'click': function () { this.trigger('click'); }
        },

        render: function () {
            this.$el.html(this.model.getFormattedName());
            return this;
        }
    });


    var AuthorAutocompleteView = Backbone.View.extend({
        tagName: 'ul',

        initialize: function () {
            this.listenTo(this.collection, 'reset', this.render);
        },

        render: function () {
            if (this.collection.length === 0) this.$el.hide();
            this.$el.show();

            this.$el.empty();

            var _this = this;
            this.collection.each(function (person) {
                var subView = new AutocompleteItemView({ model: person });
                _this.listenTo(subView, 'click', function () { _this.trigger('select', person); });
                _this.$el.append(subView.render().el);
            });

            return this;
        }
    });

    return AuthorAutocompleteView;

});
