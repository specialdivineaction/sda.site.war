define(function (require) {

    var Backbone = require('backbone'),
        $        = require('jquery'),

        WorkFormView = require('js/view/work/form'),
        Work         = require('js/model/work');


    var WorksRouter = Backbone.Router.extend({

        routes: {
            'works/new': 'newAction'
        },

        newAction: function () {
            var form = new WorkFormView({
                model: new Work(),
                router: this
            });

            $('#page-title').text('Add Book');
            $('#content').html(form.render().el);
        }

    });

    return WorksRouter;

});
