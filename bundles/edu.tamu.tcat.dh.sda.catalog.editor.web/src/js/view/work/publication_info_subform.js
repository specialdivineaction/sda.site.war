define(function (require) {

    var Backbone = require('backbone');

    require('backbone.epoxy');

    var PublicationInfoSubform = Backbone.Epoxy.View.extend({
        render: function () {
            return this;
        }
    });

    return PublicationInfoSubform;

});
