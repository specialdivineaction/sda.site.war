define(function (require) {

    var Backbone = require('backbone'),
        _        = require('underscore');


    var SearchForm = Backbone.View.extend({

        template: require('tpl!templates/search_form.html.ejs'),

        initialize: function (options) {
            this.type = options.type;

            /**
             * Search handler to process queries and give back results.
             * Results are of the form:
             *
             * {
             *     content: 'text to display',     // REQUIRED
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
            'submit .search-form': 'search'
        },

        search: function(evt) {
            evt.preventDefault();
            var query = this.$el.find('.search').val();
            var resultsPromise = this.searchHandler(query);

            var $resultContainer = this.$el.find('.search-results').empty();

            resultsPromise.then(function (results) {
                _.each(results, function (result) {
                    var resultLink = $('<a>', {
                        'class': 'list-group-item',
                        'href': result.href || '#',
                        'html': result.content
                    }).appendTo($resultContainer);

                    if (_.isFunction(result.click))
                        resultLink.on('click', result, result.click);
                });
            });
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
