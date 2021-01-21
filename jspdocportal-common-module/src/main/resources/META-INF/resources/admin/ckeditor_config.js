CKEDITOR.editorConfig = function( config )
{
    config.language = 'de';
    config.uiColor = '#80C5DD';
    config.height = '450px';
    config.toolbar = 'Full'; 
/*
    config.toolbar_Full =
    [
        ['Source','-','Preview','-','Templates'],
        ['Cut','Copy','Paste','PasteText','PasteFromWord','-','Print', 'SpellChecker', 'Scayt'],
        ['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
        ['Form', 'Checkbox', 'Radio', 'TextField', 'Textarea', 'Select', 'Button', 'ImageButton', 'HiddenField'],
        '/',
        ['Bold','Italic','Underline','Strike','-','Subscript','Superscript'],
        ['NumberedList','BulletedList','-','Outdent','Indent','Blockquote'],
        ['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
        ['Link','Unlink','Anchor'],
        ['Image','Flash','Table','HorizontalRule','Smiley','SpecialChar','PageBreak'],
        '/',
        ['Styles','Format','Font','FontSize'],
        ['TextColor','BGColor'],
        ['Maximize', 'ShowBlocks','-','About']
    ];
  */  
	config.contentsCss = 'assets/output_xhtml.css';

	/*
	 * Core styles.
	 */
	config.coreStyles_bold	= { element : 'span', attributes : {'class': 'Bold'} };
	config.coreStyles_italic	= { element : 'span', attributes : {'class': 'Italic'}};
	config.coreStyles_underline	= { element : 'span', attributes : {'class': 'Underline'}};
	config.coreStyles_strike	= { element : 'span', attributes : {'class': 'StrikeThrough'}, overrides : 'strike' };

	config.coreStyles_subscript = { element : 'span', attributes : {'class': 'Subscript'}, overrides : 'sub' };
	config.coreStyles_superscript = { element : 'span', attributes : {'class': 'Superscript'}, overrides : 'sup' };

	/*
	 * Font face
	 */
	// List of fonts available in the toolbar combo. Each font definition is
	// separated by a semi-colon (;). We are using class names here, so each font
	// is defined by {Combo Label}/{Class Name}.
	config.font_names = 'Comic Sans MS/FontComic;Courier New/FontCourier;Times New Roman/FontTimes';

	// Define the way font elements will be applied to the document. The "span"
	// element will be used. When a font is selected, the font name defined in the
	// above list is passed to this definition with the name "Font", being it
	// injected in the "class" attribute.
	// We must also instruct the editor to replace span elements that are used to
	// set the font (Overrides).
	config.font_style =
	{
			element		: 'span',
			attributes		: { 'class' : '#(family)' },
			overrides	: [ { element : 'span', attributes : { 'class' : /^Font(?:Comic|Courier|Times)$/ } } ]
	};

	/*
	 * Font sizes.
	 */
	config.fontSize_sizes = 'Smaller/FontSmaller;Larger/FontLarger;8pt/FontSmall;14pt/FontBig;Double Size/FontDouble';
	config.fontSize_style =
		{
			element		: 'span',
			attributes	: { 'class' : '#(size)' },
			overrides	: [ { element : 'span', attributes : { 'class' : /^Font(?:Smaller|Larger|Small|Big|Double)$/ } } ]
		};

	/*
	 * Font colors.
	 */
	config.colorButton_enableMore = false;

	config.colorButton_colors = 'FontColor1/FF9900,FontColor2/0066CC,FontColor3/F00';
	config.colorButton_foreStyle =
		{
			element : 'span',
			attributes : { 'class' : '#(color)' },
			overrides	: [ { element : 'span', attributes : { 'class' : /^FontColor(?:1|2|3)$/ } } ]
		};

	config.colorButton_backStyle =
		{
			element : 'span',
			attributes : { 'class' : '#(color)BG' },
			overrides	: [ { element : 'span', attributes : { 'class' : /^FontColor(?:1|2|3)BG$/ } } ]
		};

	/*
	 * Indentation.
	 */
	config.indentClasses = ['Indent1', 'Indent2', 'Indent3'];

	/*
	 * Paragraph justification.
	 */
	config.justifyClasses = [ 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyFull' ];

	/*
	 * Styles combo.
	 */
	config.stylesSet =
			[
				{ name : 'Strong Emphasis', element : 'strong' },
				{ name : 'Emphasis', element : 'em' },

				{ name : 'Computer Code', element : 'code' },
				{ name : 'Keyboard Phrase', element : 'kbd' },
				{ name : 'Sample Text', element : 'samp' },
				{ name : 'Variable', element : 'var' },

				{ name : 'Deleted Text', element : 'del' },
				{ name : 'Inserted Text', element : 'ins' },

				{ name : 'Cited Work', element : 'cite' },
				{ name : 'Inline Quotation', element : 'q' }
			];
};