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

console.log("SS name " + APP.SubServer.name);
const hw = document.getElementById("hw");
const hw2 = document.getElementById("hw2");
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
		hw2.innerHTML = srv2.print();
	}
);