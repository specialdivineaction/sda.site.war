define(function (require) {

    var Backbone = require('backbone'),
        WorkFormView = require('js/view/work/form'),
        Work = require('js/model/work');

    var WorksRouter = Backbone.Router.extend({
        routes: {
            'works/add': 'addWork'
        },

        addWork: function () {
            var form = new WorkFormView({
                model: new Work()
            });

            $('#main').html(form.render().el);
        }
    });

    return WorksRouter;

});
