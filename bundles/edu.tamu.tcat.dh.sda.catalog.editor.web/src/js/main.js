define(['jquery', 'bootstrap', 'jquery.autosize'], function ($) {
    $('textarea.autosize').autosize();

    $('input#author')
        .popover({
            container: 'body',
            html: true,
            placement: 'left',
            trigger: 'focus',
            title: 'Author Details',
            content: '<p><strong>Author:</strong> John Doe (1234-1320)<br/><strong>Display as:</strong> J. Doe<br/><strong>Role:</strong> Author</p><a href="#" class="btn btn-sm btn-block btn-info" data-toggle="modal" data-target="#quickAddForm">Edit</a>'
        });

});
