define(function (require) {

    var Backbone = require('backbone');


    var AutocompleteItemView = Backbone.View.extend({
        tagName: 'li',

        events: {
            click: function () { this.trigger('click'); }
        },

        render: function () {
            this.$el.html(this.model.getFormattedName());
            return this;
        },

        close: function () {
            this.remove();
            this.unbind();
        }
    });


    var AuthorAutocompleteView = Backbone.View.extend({
        tagName: 'ul',

        initialize: function () {
            this.listenTo(this.collection, 'reset', this.render);
            this.isFocused = false;
            this.childViews = [];
        },

        events: {
            hover: 'cancelSelection',

            // delegate mouseover and mouseout events
            mouseover: function (evt) { this.isFocused = true; },
            mouseout: function (evt) { this.isFocused = false; }
        },

        render: function () {
            if (this.collection.length === 0) {
                this.$el.hide();
                return this;
            }

            this.cancelSelection();
            this.$el.show().empty();

            var _this = this;
            this.collection.each(function (person) {
                var subView = new AutocompleteItemView({ model: person });
                _this.childViews.push(subView);
                _this.listenTo(subView, 'click', function () { _this.trigger('select', person); });
                _this.$el.append(subView.render().el);
            });

            return this;
        },

        cancelSelection: function () {
            if (this.$selected)
                this.$selected.removeClass('hover');

            this.$selected = null;
        },

        commitSelection: function () {
            if (this.$selected)
                this.$selected.trigger('click');
        },

        selectNext: function () {
            if (this.$selected) {
                var next = this.$selected.next();
                if (next.length === 0) return;

                this.$selected.removeClass('hover');
                this.$selected = next;
            } else {
                this.$selected = this.$el.children().first();
            }
            this.$selected.addClass('hover');
        },

        selectPrev: function () {
            if (this.$selected) {
                var prev = this.$selected.prev();
                if (prev.length === 0) return;

                this.$selected.removeClass('hover');
                this.$selected = prev;
            } else {
                this.$selected = this.$el.children().last();
            }
            this.$selected.addClass('hover');
        },

        close: function () {
            this.remove();
            this.unbind();
            _.each(this.childViews, function (v) {
                if (v.close) v.close();
            });
        }
    });

    return AuthorAutocompleteView;

});
