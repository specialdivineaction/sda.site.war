define(function (require) {

    var Backbone = require('backbone');

    var SearchForm = Backbone.View.extend({
        tagName: 'form',

        template: require('tpl!templates/search_form.html.ejs'),

        initialize: function (options) {
            this.type = options.type;
        },

        events: {
            'submit': 'search',
        },

        search: function(evt) {
            evt.preventDefault();
            var query = this.$el.find('.search').val();
            this.trigger('search', query);
        },

        render: function () {
            this.$el.attr('role', 'search');
            this.$el.html(this.template({ type: this.type }));
            return this;
        },

        close: function () {
            this.remove();
            this.unbind();
        }
    });

    return SearchForm;

});
