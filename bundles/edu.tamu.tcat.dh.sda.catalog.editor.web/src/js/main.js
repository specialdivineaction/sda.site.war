define(['require', 'js/model/person', 'js/view/person_form'], function (require) {
    var PersonFormView = require('js/view/person_form'),
        Person = require('js/model/person'),
        $ = require('jquery');

    var form = new PersonFormView({
        model: new Person()
    });

    $('.container').html(form.render().el);
});
