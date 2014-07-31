define(function (require) {

    var Backbone = require('backbone'),
        $        = require('jquery'),

        Config                 = require('js/config'),
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
            'click .remove-author-ref': 'disposeForm',
            'focus .name': 'initAutocomplete',
            'keypress .name': 'autocomplete',
            'keydown .name': 'tryCloseAutocomplete',

            // conditional "blur" to allow users to click on autocomplete elements
            'blur .name': function () {
                if (this.acView && !this.acView.isFocused)
                    this.closeAutocomplete();
            }
        },

        initAutocomplete: function (evt) {
            this.people = new PeopleCollection();

            this.closeAutocomplete();
            this.acView = new AuthorAutocompleteView({ collection: this.people });
            this.listenTo(this.acView, 'select', this.setAuthor);

            this.$('.autocomplete').html(this.acView.render().el);
        },

        closeAutocomplete: function () {
            if (!this.acView) return;
            this.stopListening(this.acView);
            this.acView.close();
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
                url: Config.apiPrefix + '/people',
                data: {
                    last: name
                },
                success: function (data) {
                    if (data.length === 0) {
                        _this.closeAutocomplete();
                    } else {
                        _this.people.reset(data, {parse: true});
                    }
                }
            });
        },

        setAuthor: function (person) {
            this.model.set('authorId', person.id);
            this.$('.linked-author').html(person.getFormattedName());
            this.closeAutocomplete();
        },

        render: function () {
            this.$el.html(this.template({
                model: this.model.toJSON(),
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
            this.closeAutocomplete();
            this.remove();
            this.unbind();
        }

    });

    return AuthorRefSubform;

});
