define(['jquery', 'hb!templates/author_popover.hbs', 'bootstrap', 'jquery.autosize'], function ($, template) {
    $('textarea.autosize').autosize();

    $('input#author')
        .popover({
            container: 'body',
            html: true,
            placement: 'left',
            trigger: 'focus',
            title: 'Author Details',
            content: template({
                author: {
                    first: 'John',
                    last: 'Doe',
                    birth: '1234',
                    death: '1320'
                },
                display: 'J. Doe',
                role: 'Author',
                editModalTarget: '#quickAddForm'
            })
        });
});
