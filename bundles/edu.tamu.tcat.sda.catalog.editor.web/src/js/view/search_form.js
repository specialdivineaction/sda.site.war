define(function (require) {

    var Backbone = require('backbone'),
        _        = require('underscore'),

        Message  = require('js/view/message');


    var SearchForm = Backbone.View.extend({

        template: require('tpl!templates/search_form.html.ejs'),

        initialize: function (options) {
            this.type = options.type;

            /**
             * Search handler to process queries and give back results.
             * Results are of the form:
             *
             * {
             *     html: 'text to display',        // REQUIRED
             *     href: '#path/to/resource',      // (default: '#')
             *     click: function (event) {
             *         // optional click handler
             *         // the result's context is available in event.data
             *     }
             * }
             *
             * @param {String} query
             * @return {Promise<Array<Result>>}
             */
            this.searchHandler = options.search || function () { return Promise.resolve([]); };
        },

        events: {
            'submit .search-form': 'search',
            'click .btn-reset': 'clearForm'
        },

        search: function(evt) {
            evt.preventDefault();
            var query = this.$('.search').val();
            var resultsPromise = this.searchHandler(query);

            var $resultContainer = this.$('.search-results').empty();

            resultsPromise.then(function (results) {
                if (results.length === 0) {
                    message = new Message({
                        message: 'No results found.',
                        ttl: 5000
                    });
                    message.render().openIn($resultContainer);
                    return;
                }

                _.each(results, function (result) {
                    var resultLink = $('<a>', {
                        'class': 'list-group-item',
                        'href': result.href || '#',
                        'html': result.html
                    }).appendTo($resultContainer);

                    if (_.isFunction(result.click))
                        resultLink.on('click', result, result.click);
                });
            }, function (error) {
                message = new Message({
                    type: 'error',
                    message: 'Unable to fetch results: ' + error.message,
                    ttl: 5000
                });
                message.render().openIn($resultContainer);
            });
        },

        clearForm: function (evt) {
            evt.preventDefault();

            this.$('.search-results').empty();
            this.$('.search').val('').focus();
        },

        render: function () {
            this.$el.html(this.template({ type: this.type }));
            return this;
        },

        close: function () {
            this.remove();
            this.unbind();
        }

    });

    return SearchForm;

});
