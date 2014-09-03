define(function (require) {

    var Backbone = require('backbone'),
        _        = require('underscore'),

        Message  = require('js/view/message');


    var ResultItemView = Backbone.View.extend({

        tagName: 'a',

        className: 'list-group-item',

        events: {
            click: function (e) { this.options.click.call(this.options, e); }
        },

        defaultOptions: {
            href: '#',
            click: function () {},
        },

        initialize: function (options) {
            this.options = _.defaults(options, this.defaultOptions);
        },

        render: function () {
            this.$el
                .attr('href', this.options.href)
                .html(this.options.html)
            ;

            return this;
        },

        close: function () {
            this.remove();
            this.unbind();
        }
    });


    var SearchForm = Backbone.View.extend({

        template: require('tpl!templates/search_form.html.ejs'),

        initialize: function (options) {
            this.type = options.type;

            /**
             * Search handler to process queries and give back resulting model objects.
             *
             * @param {String} query
             * @return {Promise<Array<Object>>}
             */
            this.searchHandler = options.search || function () { return Promise.resolve([]); };

            /**
             * Data transformer to convert raw collection models into search results.
             * A search result is of the form:
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
             * @param {Object} model data
             * @return {Result}
             */
            this.transform = options.transform || function (a) { return a; };

            if (typeof options.collection === 'undefined') {
                this.collection = new Backbone.Collection();
            }

            this.listenTo(this.collection, 'change reset', this.refresh);

            this.resultViews = [];
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

            var _this = this;

            resultsPromise.then(function (results) {
                if (results.length === 0) {
                    message = new Message({
                        message: 'No results found.',
                        ttl: 5000
                    });
                    message.render().openIn($resultContainer);
                    return;
                }

                _this.collection.reset(results, {parse: true});

            }, function (error) {
                message = new Message({
                    type: 'error',
                    message: 'Unable to fetch results: ' + error.message,
                    ttl: 5000
                });
                message.render().openIn($resultContainer);
            });
        },

        refresh: function () {
            var $resultContainer = this.$('.search-results');
            this.emptyResults();

            var _this = this;
            this.resultViews = this.collection.map(function (result) {
                var subView = new ResultItemView(_this.transform(result));
                $resultContainer.append(subView.render().el);
                return subView;
            });
        },

        clearForm: function (evt) {
            evt.preventDefault();
            this.emptyResults();
            this.$('.search').val('').focus();
        },

        emptyResults: function () {
            _.each(this.resultViews, function (v) {
                if (v.close) v.close();
            });
        },

        render: function () {
            this.$el.html(this.template({ type: this.type }));
            this.refresh();
            return this;
        },

        close: function () {
            this.remove();
            this.unbind();
            this.emptyResults();
        }

    });

    return SearchForm;

});
