CKEDITOR.editorConfig = function( config ) {
	config.toolbar = [
		{ name: 'styles', items: [ 'Format' ] },
		{ name: 'clipboard', items: [ 'Undo', 'Redo', '-', 'Link', 'Unlink'] },
		{ name: 'basicstyles', items: [ 'Bold', 'Italic', 'Underline', 'Strike']},
		{ name: 'basic2', items: ['Subscript', 'Superscript', '-', 'RemoveFormat' ]},
		{ name: 'paragraph', items: [ 'NumberedList', 'BulletedList', 'Outdent', 'Indent' ] },
		{ name: 'insert', items: [ 'Image', 'Table', 'HorizontalRule', 'SpecialChar' ] },
		{ name: 'colors', items: [ 'TextColor', 'BGColor' ] }
	];
};