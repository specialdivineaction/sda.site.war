define(function (require) {

    var Backbone = require('backbone'),
        $        = require('jquery'),

        WorkFormView = require('js/view/work/form'),
        Work         = require('js/model/work'),

        Message = require('js/view/message');


    var WorksRouter = Backbone.Router.extend({

        routes: {
            'works/new': 'newAction',
            'works/:id': 'editAction'
        },

        newAction: function () {
            var form = new WorkFormView({
                model: new Work(),
                router: this
            });

            $('#page-title').text('Add Book');
            $('#content').html(form.render().el);
        },

        editAction: function (id) {
            var work = new Work({ id: id });

            work.fetch({
                error: function (model, response, error) {
                    var msg = new Message({
                        type: 'error',
                        message: 'Unable to load work with ID: <code>' + id + '</code>'
                    });

                    msg.render();
                }
            });

            // fetch will update the form when it loads
            var form = new WorkFormView({
                model: work,
                router: this
            });

            $('#page-title').text('Edit Work');
            $('#content').html(form.render().el);
        }

    });

    return WorksRouter;

});
