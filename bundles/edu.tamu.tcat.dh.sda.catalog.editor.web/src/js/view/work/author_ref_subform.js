define(function (require) {

    var Backbone = require('backbone'),

        PeopleCollection       = require('js/collection/people'),
        AuthorAutocompleteView = require('js/view/work/author_autocomplete');

    require('backbone.epoxy');

    var AuthorRefSubform = Backbone.Epoxy.View.extend({
        template: require('tpl!templates/work/author_ref_subform.html.ejs'),

        initialize: function (options) {
            this.allowRemoval = options.allowRemoval;
            this.acEnableBlur = true;
        },

        bindings: {
            '.name': 'value:name,events:["keyup"]',
            '.role': 'value:role,events:["keyup"]'
        },

        events: {
            'click .remove-author-ref': 'dispose',
            'focus .name': 'initAutocomplete',
            'keypress .name': 'autocomplete',
            'keydown .name': 'tryCloseAutocomplete',

            // conditional "blur" to allow users to click on autocomplete elements
            'blur .name': function () {
                if (this.acView && !this.acView.isFocused)
                    this.removeAutocomplete();
            }
        },

        initAutocomplete: function (evt) {
            this.people = new PeopleCollection();

            this.removeAutocomplete();
            this.acView = new AuthorAutocompleteView({ collection: this.people });
            this.listenTo(this.acView, 'select', this.setAuthor);

            this.$el.find('.autocomplete').html(this.acView.render().el);
        },

        removeAutocomplete: function () {
            if (!this.acView) return;
            this.stopListening(this.acView);
            this.acView.remove();
        },

        // Have to listen for tab and esc on keydown instead of keypress
        tryCloseAutocomplete: function (evt) {
            switch(evt.keyCode) {
                case 9:
                case 27: // close on `esc` and `tab` keys
                    this.removeAutocomplete();
                    return;
            }
        },

        autocomplete: function (evt) {
            switch (evt.keyCode) {
                case 13: // commit on enter key
                    evt.preventDefault();
                    this.acView.commitSelection();
                    return false;
                case 38: // up arrow
                    evt.preventDefault();
                    if (this.acView) this.acView.selectPrev();
                    return false;
                case 40: // down arrow
                    evt.preventDefault();
                    if (this.acView) this.acView.selectNext();
                    return false;
            }

            var name = $(evt.target).val();

            if (name.length < 3) return;

            var _this = this;
            $.ajax({
                method: 'get',
                dataType: 'json',
                url: '/api/people',
                data: {
                    last: name
                },
                success: function (data) {
                    if (data.length === 0) {
                        _this.removeAutocomplete();
                    } else {
                        _this.people.reset(data, {parse: true});
                    }
                }
            });
        },

        setAuthor: function (person) {
            this.model.set('authorId', person.id);
            this.$el.find('.linked-author').html(person.getFormattedName());
            this.removeAutocomplete();
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
                _this.removeAutocomplete();
                _this.remove();
                _this.model.destroy();
            });
        }
    });

    return AuthorRefSubform;

});
