'use strict';

const APP = {}

APP.Server = class Server {
	constructor(url){
		this.url = url;
	}
	static option(){
		console.log("static S " + this.name);
		return "toto";
	}
	print(){
		console.log("instance S " + this.constructor.name);
		return this.constructor.option() + this.url;
	}
}

APP.SubServer = class SubServer extends APP.Server {
	constructor(url){
		super(url);
	}
	static option(){
		console.log("static SS " + this.name);
		return "tata";
	}
	print(){
		console.log("instance SS " + this.constructor.name);
		// return "sub" + APP.Server.option() + this.url;
		return "sub" + super.print();
	}
}

CKEDITOR.disableAutoInline = true;

APP.onload = function() {
	console.log("SS name " + APP.SubServer.name);
	const hw = document.getElementById("hw");
	const hw2 = document.getElementById("hw2");
	APP.ed1 = document.getElementById("editor1");
	APP.ed2 = document.getElementById("editor2");
	
	CKEDITOR.disableAutoInline = true;
	CKEDITOR.inline(APP.ed1, {language: 'en'} ).on('change', (evt) => {
		    // getData() returns CKEditor's HTML content.
		    console.log( 'Total bytes: ' + evt.editor.getData().length );
		});

	CKEDITOR.inline(APP.ed2, {language: 'en'} ).on('change', (evt) => {
		    // getData() returns CKEditor's HTML content.
		    console.log( 'Total bytes: ' + evt.editor.getData().length );
		});

	
	hw.addEventListener("click", () => {
			const srv = new APP.Server("titi");
			if (srv instanceof APP.Server)
				console.log("srv hérite de Server");
			hw.innerHTML = srv.print();
			// const srv2 = Object.assign(new APP.Server(""), {url:"tutu"});
			const srv2 = Object.assign(new APP.SubServer(""), {url:"tutu"});
			if (srv2 instanceof APP.Server)
				console.log("srv2 hérite de SubServer");
			if (srv2 instanceof APP.SubServer)
				console.log("srv2 hérite de Server");
			const m = srv2.print();
			hw2.innerHTML = m;
			ed1.innerHTML = APP.data;
		}
	);
	hw2.addEventListener("click", () => {
		APP.data = CKEDITOR.instances.editor1.getData();
		console.log(data);
	});
}