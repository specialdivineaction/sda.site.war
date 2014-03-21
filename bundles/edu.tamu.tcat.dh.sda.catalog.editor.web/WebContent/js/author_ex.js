define(function() {
	require.config({
		baseUrl: '/sda/catalog_editor/js',
		enforceDefine: true,
		paths: {
			// NOTE: having http prefixes is nice for dev work, but in production, the schema should be omitted
			//       e.g. //code.jquery.com/jquery-1.11.0.min
			jquery: ['//code.jquery.com/jquery-1.11.0.min', 
			         'vendor/jquery/jquery-1.11.0.min'],
			modernizr: ['//cdnjs.cloudflare.com/ajax/libs/modernizr/2.7.1/modernizr.min',
			            'vendor/modernizr/modernizr-2.6.2.min'],
			backbone: ['//cdnjs.cloudflare.com/ajax/libs/backbone.js/1.1.2/backbone-min', 
			           'vendor/backbone/backbone-1.1.2.min'],
			underscore: ['//cdnjs.cloudflare.com/ajax/libs/underscore.js/1.6.0/underscore-min',
			             'vendor/underscore/underscore-1.6.0-min.js'],
         _string: ['//cdnjs.cloudflare.com/ajax/libs/underscore.string/2.3.3/underscore.string.min',
                      'vendor/underscore-string/underscore.string.2.3.0'],	             
			domReady: 'vendor/require/plugins/domReady-2.0.1',
			i18n: 'vendor/require/plugins/i18n-2.0.4',
			text: 'vendor/require/plugins/text-2.0.10',
		},
		
		shim: {
			'modernizr': {  exports: 'Modernizr' },
			'backbone': {
				deps: ['underscore', 'jquery'],
				exports: 'Backbone'
			},
			'underscore': { exports: '_' }
		}
	});
	
	require(['domReady','plugins', 'modernizr', 'authorApp'], function(domR, p, mod, app) {
		// domReady forces this to wait until the DOM has loaded, plugins auto-runs everything in the plugins.js script, 
		// modernizr runs here to apply CSS classes to the main div element to reflect browser capabilities. 
		
		// you should add a dependency on your main application module.
	   
	   alert(app);
	   
	});
});