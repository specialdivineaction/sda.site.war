define(function (require) {

    var Backbone = require('backbone'),
        _        = require('underscore'),

        AuthorRef              = require('js/model/author_ref'),
        Title                  = require('js/model/title'),
        TitleSubform           = require('js/view/work/title_subform'),
        AuthorRefSubform       = require('js/view/work/author_ref_subform'),
        PublicationInfoSubform = require('js/view/work/publication_info_subform'),

        Message = require('js/view/message');

    require('backbone.epoxy');


    var WorkFormView = Backbone.Epoxy.View.extend({

        tagName: 'form',

        template: require('tpl!templates/work/form.html.ejs'),

        initialize: function (options) {
            this.router = options.router;
            this.childViews = [];

            this.listenTo(this.model, 'sync', this.render);
        },

        bindings: {
            '.series': 'value:series,events:["keyup"]',
            '.summary': 'value:summary,events:["keyup"]'
        },

        events: {
            'click .add-primary-author': 'addPrimaryAuthorForm',
            'click .add-other-author': 'addOtherAuthorForm',
            'click .add-title': 'addTitleForm',
            'submit': 'submit',
            'click .save-new-button': function (evt) { this.submit(evt, true); }
        },

        addTitleForm: function (evt) {
            var model = new Title();
            this.model.get('titles').add(model);

            var view = new TitleSubform({ model: model, allowRemoval: true });
            this.childViews.push(view);
            var view$el = view.render().$el;

            view$el.hide();
            this.$('.title-forms').append(view$el);
            view$el.slideDown(300);
        },

        addPrimaryAuthorForm: function (evt) {
            var model = new AuthorRef();

            this.model.get('authors').add(model);

            var view = new AuthorRefSubform({ model: model, allowRemoval: true });
            this.childViews.push(view);
            var view$el = view.render().$el;

            view$el.hide();
            this.$('.author-forms').append(view$el);
            view$el.slideDown(300);
        },

        addOtherAuthorForm: function (evt) {
            var model = new AuthorRef();

            this.model.get('otherAuthors').add(model);

            var view = new AuthorRefSubform({ model: model, allowRemoval: true });
            this.childViews.push(view);
            var view$el = view.render().$el;

            view$el.hide();
            this.$('.other-author-forms').append(view$el);
            view$el.slideDown(300);
        },

        submit: function (evt, saveAndNew) {
            evt.preventDefault();

            console.log(saveAndNew);

            var _this = this;
            this.model.save({}, {
                success: function (model, response, options) {
                    var alert = new Message({
                        type: 'success',
                        ttl: 5000,
                        message: 'Work saved successfully.'
                    });

                    alert.render().open();

                    if (saveAndNew) {
                        _this.close();
                        _this.router.newAction();
                    }
                },
                error: function (model, response, options) {
                    var alert = new Message({
                        type: 'error',
                        message: 'Unable to save work.'
                    });

                    alert.render().open();
                }
            });

            return false;
        },

        render: function () {
            this.$el.html(this.template({
                model: this.model.toJSON()
            }));

            var _this = this;

            var $authorForms = this.$('.author-forms').empty();
            this.model.get('authors').each(function (author) {
                var subForm = new AuthorRefSubform({ model: author, allowRemoval: true });
                _this.childViews.push(subForm);
                $authorForms.append(subForm.render().el);
            });

            var $titleForms = this.$('.title-forms').empty();
            this.model.get('titles').each(function (title) {
                var subForm = new TitleSubform({ model: title, allowRemoval: true });
                _this.childViews.push(subForm);
                $titleForms.append(subForm.render().el);
            });

            var $otherAuthorForms = this.$('.other-author-forms').empty();
            this.model.get('otherAuthors').each(function (otherAuthor) {
                var subForm = new AuthorRefSubform({ model: otherAuthor });
                _this.childViews.push(subForm);
                $otherAuthorForms.append(subForm.render().el);
            });

            var pubInfoSubform = new PublicationInfoSubform({ model: this.model.get('pubInfo') });
            this.childViews.push(pubInfoSubform);
            this.$('.pub-info-form').html(pubInfoSubform.render().el);

            this.applyBindings();

            return this;
        },

        close: function () {
            this.remove();
            this.unbind();
            _.each(this.childViews, function (v) {
                if (v.close) v.close();
            });
        }

    });

    return WorkFormView;

});
