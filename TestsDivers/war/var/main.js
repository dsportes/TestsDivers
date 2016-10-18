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

APP.getPhoto = function(form, canvas, w, h, ok, ko){
		//	drawImage(image,
		//		    sx, sy, sw, sh,
		//		    dx, dy, dw, dh);
		//  ctx.drawImage(image,
		//      70, 20,   // Start at 70/20 pixels from the left and the top of the image (crop),
		//      50, 50,   // "Get" a `50 * 50` (w * h) area from the source image (crop),
		//      0, 0,     // Place the result at 0, 0 in the canvas,
		//      100, 100); // With as width / height: 100 * 100 (scale)
		//}
		canvas.style.width = "" + w + "px";
		canvas.style.height = "" + h + "px";
		form.children[0].addEventListener("change", (e) => {
	    const f = e.currentTarget.files[0];
	    if (!f || !f.type.startsWith("image/")) {
	    	form.reset();
	    	if (ko) ko(new Error("not an image"));
	    	return;
	    }
		const reader = new FileReader();
		reader.onload = function(event) {
			const src= reader.result;
			const image = new Image();
			image.onload = function(){
				const ctx = canvas.getContext('2d');
				const p = w / h;
				const pi = image.width / image.height;
				let sx = 0, sy = 0, sw = 0, sh = 0;
				if (pi < p) { // trop haute
					sh = image.width / p;
					sy = (image.height - sh) / 2;
					sx = 0;
					sw = image.width;
				} else { // trop large
					sw = image.height * p;
					sx = (image.width - sw) / 2;
					sy = 0;
					sh = image.height;
				}
				ctx.clearRect(0, 0, w, h);
				canvas.width = w;
				canvas.height = h;
				ctx.drawImage(image, sx, sy, sw, sh, 0, 0, w, h);
				canvas.toBlob((blob) => {
					const reader2 = new FileReader();
					reader2.onload = function() {
						const uint8 = new Uint8Array(reader2.result);
						form.reset();
						if (ok) ok(uint8);
					};
					reader2.readAsArrayBuffer(blob);
				}, f.type);
			};
			image.src = src;				
		};
		reader.onerror = function(event) {
			form.reset();
			if (ko) ko(event);
		};
		reader.readAsDataURL(f);
	});
};

APP.onload = function() {
	let p = window.location.pathname;
	let q = window.location.search;
	let h = window.location.hash;
	let sw = navigator.serviceWorker ? true : false;
	if (p.endsWith(".sync") && !sw) {
		let nl = p + "2" + (q ? q : "") + (h ? h : "");
		alert(nl);
		window.location = nl;
		return;
	}
	const v = sessionStorage.getItem('key');
	console.log("key=" + v);
	console.log("SS name " + APP.SubServer.name);
	const hw = document.getElementById("hw");
	const hw2 = document.getElementById("hw2");
	APP.inline1 = document.getElementById("inline1");
	APP.top2 = document.getElementById("top2");
	APP.bottom2 = document.getElementById("bottom2");
	
    APP.canvas = document.getElementById("canvas");
    const form = document.getElementById("form1");
	
	
	// Turn off automatic editor creation first.
	CKEDITOR.disableAutoInline = true;

	CKEDITOR.inline(APP.inline1, {
		// To enable source code editing in a dialog window, inline editors require the "sourcedialog" plugin.
		extraPlugins: 'sharedspace,sourcedialog',
		removePlugins: 'floatingspace,maximize,resize',
		sharedSpaces: {	top: APP.top2, bottom: APP.bottom2 }
	} );
	const edt = CKEDITOR.instances.inline1;
	
	APP.getPhoto(form1, canvas, 100, 100, (uint8) =>{
		console.log(uint8.length);
	}, (error) =>{
		console.error(error.message);
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
			// ed1.innerHTML = APP.data;
		}
	);
	hw2.addEventListener("click", () => {
		let v = sessionStorage.getItem('key');
		v = v ? parseInt(v, 10) + 1 : 1;
		sessionStorage.setItem('key', v);
		window.location.reload();
	});
}