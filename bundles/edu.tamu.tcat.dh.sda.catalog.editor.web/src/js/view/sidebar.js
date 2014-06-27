define(function (require) {

    var Backbone = require('backbone'),

        SearchForm = require('js/view/search_form');

    var SidebarView = Backbone.View.extend({
        render: function () {
            var searchForm = new SearchForm();
            this.listenTo(searchForm, 'search', function (query) {
                console.log('search for ' + query);
            });

            this.$el.html(searchForm.render().el);

            return this;
        }
    });

    return SidebarView;

});
