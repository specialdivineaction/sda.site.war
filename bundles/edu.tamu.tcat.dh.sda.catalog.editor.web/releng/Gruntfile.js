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
   var buildPath = '../dist'
   
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
            }
        },

        clean: {
            build: [stagingPath],
            clobber: [buildPath, stagingPath]
        },

        /* minify the combined CSS files */              
        cssmin: {
            build: {
                files: [{ dest: buildPath + '/css/style.min.css' ,
                          src: [vendorPath + '/bootstrap/bootstrap.css', 
                                stagingPath + '/css/*.css'] }
                ]
            }
        },

        requirejs: {
            build: {
                options: {
                    baseUrl: srcPath,
                    name: path.relative(srcPath, vendorPath) + '../vendor/almond/almond',     // magically included by Bower (we hope).
                    plugins: {
                        hbs: '../vendor/require-handlebars-plugin/hbs'
                    },
                    paths: {
                        bootstrap: '../vendor/bootstrap/bootstrap',
                        jquery: '../vendor/jquery/jquery',
                        'jquery.autosize': '../vendor/jquery-autosize/jquery.autosize'
                    },
                    shim: {
                        backbone: { deps: ['underscore'], exports: 'Backbone' },
                        bootstrap: ['jquery'],
                        'jquery.autosize': ['jquery'],
                        underscore: { exports: '_' }
                    },
                    include: ['js/main'],
                    insertRequire: ['js/main'],
                    stubModules: [],
                    exclude: [],
                    out: buildPath + '/js/main.min.js'
                }
            }
        },

        stylus: {
            build: {
                options: {
                    compress: false,
                    linenos: true,
                    import: ['nib'],
                    'include css': true
                },
                files: {
                    'dist/css/layout.build.css': 'src/stylus/layout.styl'
                }
            }
        }
    });

    grunt.loadNpmTasks('grunt-bower-task');
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-cssmin');
    grunt.loadNpmTasks('grunt-contrib-requirejs');
    grunt.loadNpmTasks('grunt-contrib-stylus');

    grunt.registerTask('default', ['bower:install', 'requirejs:build', 'stylus:build', 'cssmin:build', 'clean:build']);
};
