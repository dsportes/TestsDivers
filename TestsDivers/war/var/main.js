'use strict';

const APP = {}

APP.Stamp = class Stamp {
	static init(){
		this._nbj = [[0,31,28,31,30,31,30,31,31,30,31,30,31],
			[0,31,29,31,30,31,30,31,31,30,31,30,31]];
		this._nbjc = new Array(2);
		for (let i = 0; i < 2; i++) {
			this._nbjc[i] = new Array(13);
			for(let m = 1; m < 13; m++) {
				this._nbjc[i][m] = 0;
				for(let k = 1; k < m; k++)
					this._nbjc[i][m] += this._nbj[i][k];
			}
		}
		this._qa = (365 * 4) + 1;
		this._nbjq = [0, 366, 366 + 365, 366 + 365 + 365, 366 + 365 + 365 + 365];

		// nb jours 2000-01-01 - 1970-01-01 - 30 années dont 7 bissextiles - C'était un Samedi
		this._nbj00 = (365 * 30) + 7;
		this._wd00 = 5;
		
		this.minStamp = new APP.Stamp().normalize();
		const s = new APP.Stamp();
		s.yy = 99;
		s.MM = 12;
		s.dd = 31;
		s.HH = 23;
		s.mm = 59;
		s.ss = 59;
		s.SSS = 999;
		this.maxStamp = s.normalize();
	}
	
	static edl(stamp){
		const s = "000" + stamp;
		return  s.substring(s.length() - 15);
	}

	static nbj(yy, mm){
		return this._nbj[yy % 4 == 0 ? 1 : 0][mm];
	}

	static truncJJ(yy, mm, jj){
		const x = this._nbj[yy % 4 == 0 ? 1 : 0][mm];
		return jj > x ? x : jj;
	}
	
	static fromDetail(yy, MM, dd, HH, mm, ss, SSS){
		const s = new APP.Stamp();
		s.yy = yy;
		s.MM = MM;
		s.dd = dd;
		s.HH = HH;
		s.mm = mm;
		s.ss = ss;
		s.SSS = SSS;
		return s.normalize();
	}
	
	static fromNow(deltaInMs){
		return this.fromEpoch(new Date().getTime() + deltaInMs);
	}
	
	static trunc(src, timeUnit){ // timeUnit: "yy" "MM" "dd" "HH" "mm" "ss"
		const s = this.fromDetail(src.yy, src.MM, src.dd, src.HH, src.mm, src.ss, src.SSS);
		switch (timeUnit){
			case "yy" : s.MM = 1;
			case "MM" : s.dd = 1;
			case "dd" : s.HH = 0;
			case "HH" : s.mm = 0;
			case "mm" : s.ss = 0;
			case "ss" : s.SSS = 0;
		}
		return s.normalize();
	}

	static fromEpoch(l){
		if (l > this.maxStamp.epoch) return this.maxStamp;
		if (l < this.minStamp.epoch) return this.minStamp;
		const s = new APP.Stamp();
		s.epoch = l;
		s.nbd00 = Math.floor(l / 86400000) - this._nbj00;
		s.wd = ((s.nbd00 + this._wd00) % 7) + 1;
		s.nbms = (l % 86400000);
		s.epoch00 = (s.nbd00 * 86400000) + s.nbms;
		s.yy = Math.floor(s.nbd00 / this._qa) * 4;
		let x1 = s.nbd00 % this._qa;
		for(let na = 0;;s.yy++, na++){
			let nbjcx = this._nbjc[s.yy % 4 == 0 ? 1 : 0];
			if (x1 < this._nbjq[na + 1]) {
				let nj = x1 - this._nbjq[na];
				for(s.MM = 1;; s.MM++) {
					if (nj <= nbjcx[s.MM+1]){
						s.dd = nj - nbjcx[s.MM] + 1;
						break;
					}
				}
				break;
			}
		}
		s.date = s.dd + (s.MM * 100) + (s.yy * 10000);
		s.HH = Math.floor(s.nbms / 3600000);
		let x = s.nbms % 3600000;
		s.mm = Math.floor(x / 60000);
		x = s.nbms % 60000;
		s.ss = Math.floor(x / 1000);
		s.SSS = x % 1000;
		s.time = s.SSS + (s.ss * 1000) + (s.mm * 100000) + (s.HH * 10000000);
		s.stamp = (s.date * 1000000000) + s.time;
		return s.normalize2();
	}

	static test() {
		const n = APP.Stamp.nbj(16, 10);
		let n1 = APP.Stamp.fromNow(0);
		console.log(n1);
		let n2 = APP.Stamp.fromEpoch(n1.epoch + 86400000);
		console.log(n2);
		n2 = APP.Stamp.fromDetail(0, 0, 0, 0, 0, 0, 0);
		// Stamp n4 = minStamp;
		console.log(n2);
		n2 = APP.Stamp.fromDetail(100, 13, 32, 24, 60, 60, 1000);
		console.log(n2);
		n2 = APP.Stamp.fromDetail(17, 1, 1, 23, 59, 59, 999);
		console.log(n2);
		n2 = APP.Stamp.fromEpoch(n2.epoch);
		console.log(n2);
	}
	
	compareTo(stamp) {
		return this.stamp < stamp.stamp ? -1 : (this.stamp == stamp.stamp ? 0 : 1);
	}

	equals(stamp){
		return this.stamp == stamp;
	}
	
	toString() { return this.constructor.edl(this.stamp); }

	lapseInMs(){
		return new Date().getTime() - this.epoch;
	}
	
	normalize2(){
		if (this.constructor.minStamp && this.constructor.maxStamp) {
			if (this.stamp == this.constructor.minStamp.stamp) return this.constructor.minStamp;
			if (this.stamp == this.constructor.maxStamp.stamp) return this.constructor.maxStamp;
		}
		return this;
	}
	
	normalize(){
		if (this.yy < 0) this.yy = 0;
		if (this.yy > 99) this.yy = 99;
		if (this.MM < 1) this.MM = 1;
		if (this.MM > 12) this.MM = 12;
		this.dd = this.dd < 1 ? 1 : this.constructor.truncJJ(this.yy, this.MM, this.dd);
		if (this.HH < 0) this.HH = 0;
		if (this.mm < 0) this.mm = 0;
		if (this.ss < 0) this.ss = 0;
		if (this.SSS < 0) this.SSS = 0;
		if (this.HH > 23) this.HH = 23;
		if (this.mm > 59) this.mm = 59;
		if (this.ss > 59) this.ss = 59;
		if (this.SSS > 999) this.SSS = 999;
		this.time = this.SSS + (this.ss * 1000) + (this.mm * 100000) + (this.HH * 10000000);
		this.date = this.dd + (this.MM * 100) + (this.yy * 10000);
		this.stamp = (this.date * 1000000000) + this.time;
		this.nbd00 = (Math.floor(this.yy / 4) * this.constructor._qa) + this.constructor._nbjq[(this.yy % 4)];
		this.nbms = this.SSS + (this.ss * 1000) + (this.mm * 60000) + (this.HH * 3600000);
		this.epoch00 = (this.nbd00 * 86400000) + this.nbms;
		this.epoch = ((this.nbd00 + this.constructor._nbj00) * 86400000) + this.nbms;
		this.wd = ((this.nbd00 + this.constructor._wd00) % 7) + 1;
		return this.normalize2();
	}

	constructor(){
		this.yy = 0;
		this.MM = 1;
		this.dd = 1;
		this.HH = 0;
		this.mm = 0;
		this.ss = 0;
		this.SSS = 0;
		this.wd = 0;
		this.epoch = 0;
		this.epoch00 = 0;
		this.nbd00 = 0;
		this.nbms = 0;
		this.date = 0;
		this.time = 0;
		this.stamp = 0;
	}
	
}

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
	APP.Stamp.init();
	APP.Stamp.test();

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