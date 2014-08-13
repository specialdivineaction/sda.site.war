define(function (require) {

    var Backbone = require('backbone'),
        _        = require('underscore'),

        DateDescriptionSubform = require('js/view/work/date_description_subform');

    require('backbone.epoxy');


    var PublicationInfoSubform = Backbone.Epoxy.View.extend({

        template: require('tpl!templates/work/publication_info_subform.html.ejs'),

        initialize: function () {
            this.childViews = [];
        },

        bindings: {
            '.publisher': 'value:publisher,events:["keyup"]',
            '.place': 'value:place,events:["keyup"]'
        },

        render: function () {
            this.$el.html(this.template({
                model: this.model
            }));

            var dateSubform = new DateDescriptionSubform({ model: this.model.get('date') });
            this.childViews.push(dateSubform);
            this.$('.date-form').append(dateSubform.render().el);

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

    return PublicationInfoSubform;

});
