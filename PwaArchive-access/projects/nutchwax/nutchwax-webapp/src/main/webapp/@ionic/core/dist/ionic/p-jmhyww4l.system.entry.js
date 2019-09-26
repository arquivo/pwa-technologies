var __awaiter=this&&this.__awaiter||function(e,t,r,n){return new(r||(r=Promise))(function(i,o){function s(e){try{l(n.next(e))}catch(e){o(e)}}function a(e){try{l(n["throw"](e))}catch(e){o(e)}}function l(e){e.done?i(e.value):new r(function(t){t(e.value)}).then(s,a)}l((n=n.apply(e,t||[])).next())})};var __generator=this&&this.__generator||function(e,t){var r={label:0,sent:function(){if(o[0]&1)throw o[1];return o[1]},trys:[],ops:[]},n,i,o,s;return s={next:a(0),throw:a(1),return:a(2)},typeof Symbol==="function"&&(s[Symbol.iterator]=function(){return this}),s;function a(e){return function(t){return l([e,t])}}function l(s){if(n)throw new TypeError("Generator is already executing.");while(r)try{if(n=1,i&&(o=s[0]&2?i["return"]:s[0]?i["throw"]||((o=i["return"])&&o.call(i),0):i.next)&&!(o=o.call(i,s[1])).done)return o;if(i=0,o)s=[s[0]&2,o.value];switch(s[0]){case 0:case 1:o=s;break;case 4:r.label++;return{value:s[1],done:false};case 5:r.label++;i=s[1];s=[0];continue;case 7:s=r.ops.pop();r.trys.pop();continue;default:if(!(o=r.trys,o=o.length>0&&o[o.length-1])&&(s[0]===6||s[0]===2)){r=0;continue}if(s[0]===3&&(!o||s[1]>o[0]&&s[1]<o[3])){r.label=s[1];break}if(s[0]===6&&r.label<o[1]){r.label=o[1];o=s;break}if(o&&r.label<o[2]){r.label=o[2];r.ops.push(s);break}if(o[2])r.ops.pop();r.trys.pop();continue}s=t.call(e,r)}catch(e){s=[6,e];i=0}finally{n=o=0}if(s[0]&5)throw s[1];return{value:s[0]?s[1]:void 0,done:true}}};System.register(["./p-a6904dd4.system.js","./p-45890bdd.system.js","./p-2978c157.system.js","./p-8dca3b40.system.js","./p-94417ec5.system.js","./p-80b537b4.system.js","./p-02ea9913.system.js"],function(e){"use strict";var t,r,n,i,o,s,a,l,u,d,c,p,f,m;return{setters:[function(e){t=e.r;r=e.d;n=e.c;i=e.h;o=e.e;s=e.H},function(){},function(){},function(e){a=e.B;l=e.d;u=e.e;d=e.f},function(e){c=e.g},function(e){p=e.a;f=e.d},function(e){m=e.d}],execute:function(){function h(e,t){var r=new e;var n=new e;n.addElement(t.querySelector("ion-backdrop"));var i=new e;i.addElement(t.querySelector(".modal-wrapper"));i.beforeStyles({opacity:1}).fromTo("translateY","100%","0%");n.fromTo("opacity",.01,.4);return Promise.resolve(r.addElement(t).easing("cubic-bezier(0.36,0.66,0.04,1)").duration(400).beforeAddClass("show-modal").add(n).add(i))}function y(e,t){var r=new e;var n=new e;n.addElement(t.querySelector("ion-backdrop"));var i=new e;var o=t.querySelector(".modal-wrapper");i.addElement(o);var s=o.getBoundingClientRect();i.beforeStyles({opacity:1}).fromTo("translateY","0%",t.ownerDocument.defaultView.innerHeight-s.top+"px");n.fromTo("opacity",.4,0);return Promise.resolve(r.addElement(t).easing("ease-out").duration(250).add(n).add(i))}function v(e,t){var r=new e;var n=new e;n.addElement(t.querySelector("ion-backdrop"));var i=new e;i.addElement(t.querySelector(".modal-wrapper"));i.fromTo("opacity",.01,1).fromTo("translateY","40px","0px");n.fromTo("opacity",.01,.32);return Promise.resolve(r.addElement(t).easing("cubic-bezier(0.36,0.66,0.04,1)").duration(280).beforeAddClass("show-modal").add(n).add(i))}function b(e,t){var r=new e;var n=new e;n.addElement(t.querySelector("ion-backdrop"));var i=new e;var o=t.querySelector(".modal-wrapper");i.addElement(o);i.fromTo("opacity",.99,0).fromTo("translateY","0px","40px");n.fromTo("opacity",.32,0);return Promise.resolve(r.addElement(t).easing("cubic-bezier(0.47,0,0.745,0.715)").duration(200).add(n).add(i))}var w=function(){function e(e){t(this,e);this.presented=false;this.mode=r(this);this.keyboardClose=true;this.backdropDismiss=true;this.showBackdrop=true;this.animated=true;this.didPresent=n(this,"ionModalDidPresent",7);this.willPresent=n(this,"ionModalWillPresent",7);this.willDismiss=n(this,"ionModalWillDismiss",7);this.didDismiss=n(this,"ionModalDidDismiss",7)}e.prototype.onDismiss=function(e){e.stopPropagation();e.preventDefault();this.dismiss()};e.prototype.onBackdropTap=function(){this.dismiss(undefined,a)};e.prototype.lifecycle=function(e){var t=this.usersElement;var r=g[e.type];if(t&&r){var n=new CustomEvent(r,{bubbles:false,cancelable:false,detail:e.detail});t.dispatchEvent(n)}};e.prototype.present=function(){return __awaiter(this,void 0,void 0,function(){var e,t,r;return __generator(this,function(n){switch(n.label){case 0:if(this.presented){return[2]}e=this.el.querySelector(".modal-wrapper");if(!e){throw new Error("container is undefined")}t=Object.assign({},this.componentProps,{modal:this.el});r=this;return[4,p(this.delegate,e,this.component,["ion-page"],t)];case 1:r.usersElement=n.sent();return[4,m(this.usersElement)];case 2:n.sent();return[2,l(this,"modalEnter",h,v)]}})})};e.prototype.dismiss=function(e,t){return __awaiter(this,void 0,void 0,function(){var r;return __generator(this,function(n){switch(n.label){case 0:return[4,u(this,e,t,"modalLeave",y,b)];case 1:r=n.sent();if(!r)return[3,3];return[4,f(this.delegate,this.usersElement)];case 2:n.sent();n.label=3;case 3:return[2,r]}})})};e.prototype.onDidDismiss=function(){return d(this.el,"ionModalDidDismiss")};e.prototype.onWillDismiss=function(){return d(this.el,"ionModalWillDismiss")};e.prototype.hostData=function(){var e;var t=r(this);return{"no-router":true,"aria-modal":"true",class:Object.assign((e={},e[t]=true,e),c(this.cssClass)),style:{zIndex:2e4+this.overlayIndex}}};e.prototype.__stencil_render=function(){var e;var t=r(this);var n=(e={},e["modal-wrapper"]=true,e[t]=true,e);return[i("ion-backdrop",{visible:this.showBackdrop,tappable:this.backdropDismiss}),i("div",{role:"dialog",class:n})]};Object.defineProperty(e.prototype,"el",{get:function(){return o(this)},enumerable:true,configurable:true});e.prototype.render=function(){return i(s,this.hostData(),this.__stencil_render())};Object.defineProperty(e,"style",{get:function(){return".sc-ion-modal-md-h{--width:100%;--min-width:auto;--max-width:auto;--height:100%;--min-height:auto;--max-height:auto;--overflow:hidden;--border-radius:0;--border-width:0;--border-style:none;--border-color:transparent;--background:var(--ion-background-color,#fff);--box-shadow:none;left:0;right:0;top:0;bottom:0;display:-ms-flexbox;display:flex;position:absolute;-ms-flex-align:center;align-items:center;-ms-flex-pack:center;justify-content:center;contain:strict}.overlay-hidden.sc-ion-modal-md-h{display:none}.modal-wrapper.sc-ion-modal-md{border-radius:var(--border-radius);width:var(--width);min-width:var(--min-width);max-width:var(--max-width);height:var(--height);min-height:var(--min-height);max-height:var(--max-height);border-width:var(--border-width);border-style:var(--border-style);border-color:var(--border-color);background:var(--background);-webkit-box-shadow:var(--box-shadow);box-shadow:var(--box-shadow);overflow:var(--overflow);z-index:10}\@media only screen and (min-width:768px) and (min-height:600px){.sc-ion-modal-md-h{--width:600px;--height:500px;--ion-safe-area-top:0px;--ion-safe-area-bottom:0px;--ion-safe-area-right:0px;--ion-safe-area-left:0px}}\@media only screen and (min-width:768px) and (min-height:768px){.sc-ion-modal-md-h{--width:600px;--height:600px}}\@media only screen and (min-width:768px) and (min-height:600px){.sc-ion-modal-md-h{--border-radius:2px;--box-shadow:0 28px 48px rgba(0,0,0,0.4)}}.modal-wrapper.sc-ion-modal-md{-webkit-transform:translate3d(0,40px,0);transform:translate3d(0,40px,0);opacity:.01}"},enumerable:true,configurable:true});return e}();e("ion_modal",w);var g={ionModalDidPresent:"ionViewDidEnter",ionModalWillPresent:"ionViewWillEnter",ionModalWillDismiss:"ionViewWillLeave",ionModalDidDismiss:"ionViewDidLeave"}}}});