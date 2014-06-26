define(function (require) {

    var Backbone = require('backbone'),

        PeopleCollection       = require('js/collection/people'),
        AuthorAutocompleteView = require('js/view/work/author_autocomplete');

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
            'click .remove-author-ref': 'dispose',
            'focus .name': 'initAutocomplete',
            'keyup .name': 'autocomplete'
        },

        initAutocomplete: function (evt) {
            this.people = new PeopleCollection();

            this.destroyAutocomplete();
            this.acView = new AuthorAutocompleteView({ collection: this.people });
            this.listenTo(this.acView, 'select', this.setAuthor);

            this.$el.find('.autocomplete').html(this.acView.render().el);
        },

        destroyAutocomplete: function () {
            if (!this.acView) return;
            this.stopListening(this.acView);
            this.acView.remove();
        },

        autocomplete: function (evt) {
            // close on `esc` key
            if (evt.keyCode === 27) {
                this.destroyAutocomplete();
                return;
            }

            var $target = $(evt.target);
            var name = $target.val();

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
                    if (data.length == 0) {
                        _this.destroyAutocomplete();
                    } else {
                        _this.people.reset(data, {parse: true});
                    }
                }
            });
        },

        setAuthor: function (person) {
            this.model.set('authorId', person.id);
            this.$el.find('.linked-author').html(person.getFormattedName());
            this.destroyAutocomplete();
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
                _this.destroyAutocomplete();
                _this.remove();
                _this.model.destroy();
            });
        }
    });

    return AuthorRefSubform;

});
