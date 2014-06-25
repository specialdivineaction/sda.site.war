define(function (require) {

    var Backbone = require('backbone'),

        DateDescriptionSubform = require('js/view/work/date_description_subform');

    require('backbone.epoxy');

    var PublicationInfoSubform = Backbone.Epoxy.View.extend({
        template: require('tpl!templates/work/publication_info_subform.html.ejs'),

        bindings: {
            '.publisher': 'value:publisher,events:["keyup"]',
            '.place': 'value:place,events:["keyup"]'
        },

        render: function () {
            this.$el.html(this.template({
                model: this.model
            }));

            var dateSubform = new DateDescriptionSubform({ model: this.model.get('date') });
            this.$el.find('.date-form').append(dateSubform.render().el);

            this.applyBindings();

            return this;
        }
    });

    return PublicationInfoSubform;

});
