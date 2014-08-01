define(function (require) {

    var Backbone = require('backbone'),
        _        = require('underscore');


    var AutocompleteItemView = Backbone.View.extend({

        tagName: 'li',

        initialize: function (options) {
            this.renderer = options.renderer;
        },

        events: {
            click: function () { this.trigger('click'); }
        },

        render: function () {
            this.$el.html(this.renderer(this.model));
            return this;
        },

        close: function () {
            this.remove();
            this.unbind();
        }

    });


    var AutocompleteMenuView = Backbone.View.extend({

        tagName: 'ul',

        initialize: function (options) {
            this.listenTo(this.collection, 'reset', this.render);
            this.isFocused = false;
            this.childViews = [];
            this.itemRenderer = options.itemRenderer;
        },

        events: {
            'hover': 'cancelSelection',

            // delegate mouseover and mouseout events
            'mouseover': function (evt) { this.isFocused = true; },
            'mouseout': function (evt) { this.isFocused = false; }
        },

        render: function () {
            if (this.collection.length === 0) {
                this.$el.hide();
                return this;
            }

            this.cancelSelection();
            this.$el.show().empty();

            var _this = this;
            this.collection.each(function (model) {
                var subView = new AutocompleteItemView({ model: model, renderer: _this.itemRenderer });
                _this.childViews.push(subView);
                _this.listenTo(subView, 'click', function () { _this.trigger('select', model); });
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


    var AutocompleteInputView = Backbone.View.extend({

        tagName: 'input',

        attributes: {
            type: 'text',
            autocomplete: 'off'
        },

        initialize: function (options) {
            this.itemRenderer = options.itemRenderer || String;
            this.minLength = options.minLength || 3;
        },

        events: {
            'focus': 'initAutocomplete',
            'keypress': 'autocomplete',
            'keydown': 'tryCloseAutocomplete',

            // conditional "blur" to allow users to click on autocomplete elements
            'blur': function () {
                if (this.menu && !this.menu.isFocused)
                    this.closeAutocomplete();
            }
        },

        initAutocomplete: function (evt) {
            this.closeAutocomplete();
            this.menu = new AutocompleteMenuView({ collection: this.collection, itemRenderer: this.itemRenderer });
            this.listenTo(this.menu, 'select', this.setSelection);

            $('<div>', {
                class: 'autocomplete',
                html: this.menu.render().el
            }).insertAfter(this.$el);
        },

        closeAutocomplete: function () {
            if (!this.menu) return;
            this.stopListening(this.menu);
            this.menu.close();
        },

        // Have to listen for tab and esc on keydown instead of keypress
        tryCloseAutocomplete: function (evt) {
            switch(evt.keyCode) {
                case 9:
                case 27: // close on `esc` and `tab` keys
                    this.closeAutocomplete();
                    return;
            }
        },

        autocomplete: function (evt) {
            switch (evt.keyCode) {
                case 13: // commit on enter key
                    evt.preventDefault();
                    this.menu.commitSelection();
                    return false;
                case 38: // up arrow
                    evt.preventDefault();
                    if (this.menu) this.menu.selectPrev();
                    return false;
                case 40: // down arrow
                    evt.preventDefault();
                    if (this.menu) this.menu.selectNext();
                    return false;
            }

            var value = $(evt.target).val();
            if (value.length < this.minLength) return;

            // collection should be reset by the client
            this.trigger('autocomplete', value);
        },

        setSelection: function (model) {
            this.trigger('select', model);
            this.closeAutocomplete();
        },

        render: function () {
            return this;
        },

        close: function () {
            this.closeAutocomplete();
            this.remove();
            this.unbind();
        }

    });

    return AutocompleteInputView;

});
