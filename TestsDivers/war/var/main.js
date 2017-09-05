'use strict';

const APP = {}
const GEN = {}

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
	APP.bcrypt = dcodeIO.bcrypt;
	
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
		
	const converter = new showdown.Converter();
	const ta = document.getElementById("ta");
	const md = document.getElementById("md");
	const btn = document.getElementById("btn");
	
	const _docrypt = document.getElementById("docrypt");
	const _inp1 = document.getElementById("inp1");
	const _salt = document.getElementById("salt");
	const _bc = document.getElementById("bc");

	const hw = document.getElementById("hw");
	const hw2 = document.getElementById("hw2");
	APP.canvas = document.getElementById("canvas");
	const form = document.getElementById("form1");
	
	APP.getPhoto(form1, canvas, 100, 100, (uint8) =>{
		console.log(uint8.length);
	}, (error) =>{
		console.error(error.message);
	});
	
	_docrypt.addEventListener("click", () => {
		var x = 334;
		console.log(x.toString(16));
		console.log(x.toString(36));
		let text = _inp1.value;
		let salt = APP.bcrypt.genSaltSync(10);
		let hash = APP.bcrypt.hashSync(text, salt);
		let h1 = XXH.h64(text, 0xABCD);	// seed = 0xABCD	
		var s1 = GEN.Crypt.longToBase64(h1);
		let h2 = XXH.h32(text, 0xABCD);	// seed = 0xABCD	
		var s2 = GEN.Crypt.intToBase64(h2);
		_bc.innerHTML = hash + " / " + hash.length + " / " + APP.bcrypt.compareSync(text, hash) + " [" + 
		h2.toString(16) + " " + s2 + " " + s1 + "]";
		_salt.innerHTML = salt + " / " + salt.length;
	});	
	
	btn.addEventListener("click", () => {
		let text = ta.value;
		md.innerHTML = converter.makeHtml(text);
	});
	
	hw2.addEventListener("click", () => {
		let v = sessionStorage.getItem('key');
		v = v ? parseInt(v, 10) + 1 : 1;
		sessionStorage.setItem('key', v);
		window.location.reload();
	});
}