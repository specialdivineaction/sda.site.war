define(function (require) {

    var Backbone = require('backbone');

    // attach epoxy to backbone
    require('backbone.epoxy');

    var NameSubform = Backbone.Epoxy.View.extend({
        template: require('tpl!templates/person/name_subform.html.ejs'),

        bindings: {
            'input.display-name': 'value:displayName,events:["keyup"]',
            'input.title': 'value:title,events:["keyup"]',
            'input.given-name': 'value:givenName,events:["keyup"]',
            'input.middle-name': 'value:middleName,events:["keyup"]',
            'input.family-name': 'value:familyName,events:["keyup"]',
            'input.suffix': 'value:suffix,events:["keyup"]'
        },

        render: function () {
            this.$el.html(this.template({
                model: this.model.toJSON()
            }));

            this.applyBindings();

            return this;
        }
    });

    return NameSubform;

});
