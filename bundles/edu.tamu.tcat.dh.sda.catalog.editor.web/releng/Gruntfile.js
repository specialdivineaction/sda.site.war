module.exports = function (grunt) {

    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),


        bower: {
            install: {
                options: {
                    targetDir: './vendor',
                    layout: 'byComponent',
                    cleanup: true
                }
            }
        },

        clean: {
            build: {
                css: { expand: true, src: 'dist/css/*.build.css' }
            },
            clobber: ['build', 'vendor']
        },

        cssmin: {
            build: {
                files: {
                    'dist/css/style.min.css': ['vendor/bootstrap/bootstrap.css', 'dist/css/*.build.css']
                }
            }
        },

        requirejs: {
            build: {
                options: {
                    baseUrl: 'src',
                    name: '../vendor/almond/almond',
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
                    out: 'dist/js/main.min.js'
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
