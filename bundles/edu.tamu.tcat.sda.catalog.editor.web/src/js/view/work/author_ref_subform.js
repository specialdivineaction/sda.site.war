define(function (require) {

    var Backbone = require('backbone'),
        $        = require('jquery'),

        Config                 = require('js/config'),
        PeopleCollection       = require('js/collection/people'),
        Autocomplete           = require('js/view/autocomplete');

    require('backbone.epoxy');


    var AuthorRefSubform = Backbone.Epoxy.View.extend({

        template: require('tpl!templates/work/author_ref_subform.html.ejs'),

        initialize: function (options) {
            this.allowRemoval = options.allowRemoval;
            this.acEnableBlur = true;

            this.people = new PeopleCollection();
        },

        bindings: {
            '.name': 'value:name,events:["keyup"]',
            '.role': 'value:role,events:["keyup"]'
        },

        events: {
            'click .remove-author-ref': 'disposeForm',
        },

        autocomplete: function (name) {
            var _this = this;
            $.ajax({
                method: 'get',
                dataType: 'json',
                url: Config.apiPrefix + '/people',
                data: {
                    last: name
                },
                success: function (data) {
                    _this.people.reset(data, {parse: true});
                }
            });
        },

        setAuthor: function (person) {
            this.model.set('authorId', person.id);

            var name = person.getFormattedName();
            this.model.set('name', name);
            this.$('.linked-author').html(name);
        },

        render: function () {
            this.$el.html(this.template({
                model: this.model.toJSON(),
                showDelete: this.allowRemoval
            }));

            this.acView = new Autocomplete({
                el: this.$('.name'),
                collection: this.people,
                itemRenderer: function (person) { return person.getFormattedName(); }
            });

            this.listenTo(this.acView, 'autocomplete', this.autocomplete);
            this.listenTo(this.acView, 'select', this.setAuthor);

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
            this.acView.close();
            this.remove();
            this.unbind();
        }

    });

    return AuthorRefSubform;

});
