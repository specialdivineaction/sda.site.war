define(function (require) {

    var Backbone = require('backbone'),

        AuthorRef = require('js/model/author_ref'),
        Title     = require('js/model/title');

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


    var TitleSubform = Backbone.Epoxy.View.extend({
        template: require('tpl!templates/work/title_subform.html.ejs'),

        initialize: function (options) {
            this.allowRemoval = options.allowRemoval;
        },

        bindings: {
            'select.language': 'value:lg,events:["change"]',
            'input.title': 'value:title,events:["keyup"]',
            'input.subtitle': 'value:subtitle,events:["keyup"]'
        },

        events: {
            'click .remove-title': 'dispose'
        },

        render: function () {
            this.$el.html(this.template({
                model: this.model.toJSON(),
                languages: require('js/data/languages'),
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
    })


    var TitleDefinitionSubform = Backbone.View.extend({
        template: require('tpl!templates/work/title_definition_subform.html.ejs'),

        events: {
            'click .add-alternate-title': 'addTitleForm'
        },

        addTitleForm: function (evt) {
            var model = new Title();
            this.model.get('alternateTitles').add(model)

            var view = new TitleSubform({ model: model, allowRemoval: true });
            var view$el = view.render().$el;

            view$el.hide();
            this.$el.find('.alternate-title-forms').append(view$el);
            view$el.slideDown(300);
        },

        render: function () {
            this.$el.html(this.template({ model: this.model.toJSON() }));

            var titleSubform = new TitleSubform({ model: this.model.get('canonicalTitle') });
            this.$el.find('.canonical-title-form').html(titleSubform.render().el);

            var shortTitleSubform = new TitleSubform({ model: this.model.get('shortTitle') });
            this.$el.find('.short-title-form').html(shortTitleSubform.render().el);

            var $altTitles = this.$el.find('.alternate-title-forms').empty();
            this.model.get('alternateTitles').each(function (altTitle) {
                var subForm = new TitleSubform({ model: altTitle, allowRemoval: true });
                $altTitles.append(subForm.render().el);
            });

            var localeTitleSubform = new TitleSubform({ model: this.model.get('localeTitle') });
            this.$el.find('.locale-title-form').html(localeTitleSubform.render().el);

            return this;
        }
    });


    var PublicationInfoSubform = Backbone.Epoxy.View.extend({
        render: function () {
            return this;
        }
    });


    var WorkFormView = Backbone.Epoxy.View.extend({
        tagName: 'form',

        template: require('tpl!templates/work/form.html.ejs'),

        bindings: {
            '.series': 'value:series,events:["keyup"]',
            '.summary': 'value:summary,events:["keyup"]'
        },

        events: {
            'click .add-primary-author': 'addPrimaryAuthorForm',
            'click .add-other-author': 'addOtherAuthorForm'
        },

        addPrimaryAuthorForm: function (evt) {
            var model = new AuthorRef();

            this.model.get('authors').add(model);

            var view = new AuthorRefSubform({ model: model, allowRemoval: true });
            var view$el = view.render().$el;

            view$el.hide();
            this.$el.find('.author-forms').append(view$el);
            view$el.slideDown(300);
        },

        addOtherAuthorForm: function (evt) {
            var model = new AuthorRef();

            this.model.get('otherAuthors').add(model);

            var view = new AuthorRefSubform({ model: model, allowRemoval: true });
            var view$el = view.render().$el;

            view$el.hide();
            this.$el.find('.other-author-forms').append(view$el);
            view$el.slideDown(300);
        },

        render: function () {
            this.$el.html(this.template({
                model: this.model.toJSON()
            }));

            var $authorForms = this.$el.find('.author-forms').empty();
            this.model.get('authors').each(function (author) {
                var subForm = new AuthorRefSubform({ model: author });
                $authorForms.append(subForm.render().el);
            });

            var titleSubform = new TitleDefinitionSubform({ model: this.model.get('title') });
            this.$el.find('.title-form').html(titleSubform.render().el);

            var $otherAuthorForms = this.$el.find('.other-author-forms').empty();
            this.model.get('otherAuthors').each(function (otherAuthor) {
                var subForm = new AuthorRefSubform({ model: otherAuthor });
                $otherAuthorForms.append(subForm.render().el);
            });

            var pubInfoSubform = new PublicationInfoSubform({ model: this.model.get('pubInfo') });
            this.$el.find('.pub-info-form').html(pubInfoSubform.render().el);

            this.applyBindings();

            return this;
        }
    });

    return WorkFormView;

});
