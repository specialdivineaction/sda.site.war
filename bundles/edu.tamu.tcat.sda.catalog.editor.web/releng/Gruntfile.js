// TODO add license
// TODO add versioning
// TODO copy HTML and other static content as needed
// TODO need to produce debug versions of output

var path = require("path");

module.exports = function (grunt) {

   // staging area for joining files, downloaded third-party tools, etc.'
   var rootPath = '..';
   var srcPath = rootPath + '/src';
   var stagingPath = rootPath + '/build';
   var vendorPath = stagingPath + '/vendor';

   // where the final built/deployable artifacts go
   var buildPath = rootPath + '/web';

    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),

        /* dependency management system for front-end scripts (as opposed to npm modules) */
        bower: {
            install: {
                options: {
                    targetDir: vendorPath,      // temporary dir used during install
                    layout: 'byComponent',
                    cleanup: true
                }
            },
            dev: {
                options: {
                    targetDir: buildPath + '/vendor',
                    layout: 'byComponent'
                }
            }
        },

        clean: {
            // options global to all subtasks
            options: {
                force: true         // allow cleaning outside CWD
            },

            build: [stagingPath],
            clean: [buildPath, stagingPath, 'bower_components'],
            clobber: [buildPath, stagingPath, 'bower_components', 'node_modules']
        },

        copy: {
            build: {
                files: [
                    {
                        expand: true,
                        flatten: true,
                        dest: buildPath + '/fonts',
                        src: vendorPath + '/bootstrap/glyphicons-halflings-regular.*'
                    },
                    {
                        dest: buildPath + '/index.html',
                        src: srcPath + '/index.html'
                    },
                    {
                        dest: buildPath + '/login.html',
                        src: srcPath + '/login.html'
                    },
                    {
                        dest: buildPath + '/js/config.js',
                        src: srcPath + '/js/config.js'
                    }
                ]
            },
            dev: {
                files: [
                    {
                        expand: true,
                        flatten: true,
                        dest: buildPath + '/vendor/fonts',
                        src: buildPath + '/vendor/bootstrap/glyphicons-halflings-regular.*'
                    },
                    {
                        expand: true,
                        cwd: srcPath,
                        dest: buildPath,
                        src: ['js/**/*', 'templates/**/*']
                    },
                    {
                        dest: buildPath + '/index.html',
                        src: srcPath + '/index_dev.html'
                    },
                    {
                        dest: buildPath + '/login.html',
                        src: srcPath + '/login.html'
                    }
                ]
            }
        },

        /* minify the combined CSS files */
        cssmin: {
            build: {
                files: [
                    {
                        dest: buildPath + '/css/style.min.css' ,
                        src: [
                            vendorPath + '/bootstrap/bootstrap.css',
                            stagingPath + '/css/style.css'
                        ]
                    }
                ]
            }
        },

        requirejs: {
            build: {
                options: {
                    baseUrl: srcPath,
                    name: path.relative(srcPath, vendorPath) + '/almond/almond',     // magically included by Bower (we hope).
                    paths: {
                        backbone: path.relative(srcPath, vendorPath) + '/backbone/backbone',
                        'backbone.epoxy': path.relative(srcPath, vendorPath) + '/backbone.epoxy/backbone.epoxy',
                        bootstrap: path.relative(srcPath, vendorPath) + '/bootstrap/bootstrap',
                        jquery: path.relative(srcPath, vendorPath) + '/jquery/jquery',
                        'jquery.autosize': path.relative(srcPath, vendorPath) + '/jquery-autosize/jquery.autosize',
                        moment: path.relative(srcPath, vendorPath) + '/moment/moment',
                        text: path.relative(srcPath, vendorPath) + '/requirejs-text/text',
                        tpl: path.relative(srcPath, vendorPath) + '/requirejs-tpl/tpl',
                        underscore: path.relative(srcPath, vendorPath) + '/underscore/underscore',

                        // will be loaded externally at runtime:
                        config: 'empty:'
                    },
                    shim: {
                        backbone: { deps: ['jquery', 'underscore'], exports: 'Backbone' },
                        'backbone.epoxy': ['backbone'],
                        bootstrap: ['jquery'],
                        'jquery.autosize': ['jquery'],
                        moment: { exports: 'moment' },
                        underscore: { exports: '_' }
                    },
                    include: ['js/main'],

                    // since config is externally defined (i.e. in a separate file), js/main must be
                    // called manually AFTER config definition is loaded
                    // insertRequire: ['js/main'],

                    stubModules: [],
                    exclude: [],
                    out: buildPath + '/js/main.min.js'
                }
            }
        },

        stylus: {
            build: {
                options: {
                    import: ['nib'],
                    'include css': true
                },
                files: [
                    {
                        dest: stagingPath + '/css/style.css',
                        src: srcPath + '/stylus/style.styl'
                    }
                ]
            },
            dev: {
                options: {
                    compress: false,
                    linenos: true,
                    import: ['nib'],
                    'include css': true
                },
                files: [
                    {
                        dest: buildPath + '/css/style.css',
                        src: srcPath + '/stylus/style.styl'
                    }
                ]
            }
        },

        uglify: {
            build: {
                files: [
                    {
                        dest: buildPath + '/js/promise.polyfill.min.js',
                        src: vendorPath + '/promise-polyfill/Promise.js'
                    }
                ]
            }
        },

        watch: {
            src: {
                files: [srcPath + '/**/*'],
                tasks: ['dev']
            }
        }
    });

    grunt.loadNpmTasks('grunt-bower-task');
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-cssmin');
    grunt.loadNpmTasks('grunt-contrib-requirejs');
    grunt.loadNpmTasks('grunt-contrib-stylus');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-watch');

    grunt.registerTask('prod', ['bower:install', 'requirejs:build', 'stylus:build', 'uglify:build', 'cssmin:build', 'copy:build', 'clean:build']);
    grunt.registerTask('dev', ['bower:dev', 'stylus:dev', 'copy:dev']);

    grunt.registerTask('default', ['dev']);
};
