define(function (require) {

    var Backbone = require('backbone'),

        Title            = require('js/model/title'),
        TitleSubform     = require('js/view/work/title_subform');

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

            return this;
        }
    });

    return TitleDefinitionSubform;

});
