var __awaiter=this&&this.__awaiter||function(t,e,i,n){return new(i||(i=Promise))(function(s,r){function o(t){try{u(n.next(t))}catch(t){r(t)}}function a(t){try{u(n["throw"](t))}catch(t){r(t)}}function u(t){t.done?s(t.value):new i(function(e){e(t.value)}).then(o,a)}u((n=n.apply(t,e||[])).next())})};var __generator=this&&this.__generator||function(t,e){var i={label:0,sent:function(){if(r[0]&1)throw r[1];return r[1]},trys:[],ops:[]},n,s,r,o;return o={next:a(0),throw:a(1),return:a(2)},typeof Symbol==="function"&&(o[Symbol.iterator]=function(){return this}),o;function a(t){return function(e){return u([t,e])}}function u(o){if(n)throw new TypeError("Generator is already executing.");while(i)try{if(n=1,s&&(r=o[0]&2?s["return"]:o[0]?s["throw"]||((r=s["return"])&&r.call(s),0):s.next)&&!(r=r.call(s,o[1])).done)return r;if(s=0,r)o=[o[0]&2,r.value];switch(o[0]){case 0:case 1:r=o;break;case 4:i.label++;return{value:o[1],done:false};case 5:i.label++;s=o[1];o=[0];continue;case 7:o=i.ops.pop();i.trys.pop();continue;default:if(!(r=i.trys,r=r.length>0&&r[r.length-1])&&(o[0]===6||o[0]===2)){i=0;continue}if(o[0]===3&&(!r||o[1]>r[0]&&o[1]<r[3])){i.label=o[1];break}if(o[0]===6&&i.label<r[1]){i.label=r[1];r=o;break}if(r&&i.label<r[2]){i.label=r[2];i.ops.push(o);break}if(r[2])i.ops.pop();i.trys.pop();continue}o=e.call(t,i)}catch(t){o=[6,t];s=0}finally{n=r=0}if(o[0]&5)throw o[1];return{value:o[0]?o[1]:void 0,done:true}}};System.register(["./p-a6904dd4.system.js","./p-45890bdd.system.js","./p-94417ec5.system.js","./p-78563a64.system.js"],function(t,e){"use strict";var i,n,s,r,o,a,u,h;return{setters:[function(t){i=t.r;n=t.d;s=t.h;r=t.H;o=t.e;a=t.c},function(){},function(t){u=t.c},function(t){h=t.i}],execute:function(){var p=function(){function t(t){i(this,t);this.disabled=false;this.expandable=false;this.type="button";this.onClick=function(t){var e=t.target.closest("ion-item-option");if(e){t.preventDefault()}}}t.prototype.render=function(){var t;var e=this,i=e.disabled,o=e.expandable,a=e.href;var h=a===undefined?"button":"a";var p=n(this);var l=h==="button"?{type:this.type}:{download:this.download,href:this.href,target:this.target};return s(r,{onClick:this.onClick,class:Object.assign({},u(this.color),(t={},t[p]=true,t["item-option-disabled"]=i,t["item-option-expandable"]=o,t["ion-activatable"]=true,t))},s(h,Object.assign({},l,{class:"button-native",disabled:i}),s("span",{class:"button-inner"},s("slot",{name:"top"}),s("div",{class:"horizontal-wrapper"},s("slot",{name:"start"}),s("slot",{name:"icon-only"}),s("slot",null),s("slot",{name:"end"})),s("slot",{name:"bottom"})),p==="md"&&s("ion-ripple-effect",null)))};Object.defineProperty(t.prototype,"el",{get:function(){return o(this)},enumerable:true,configurable:true});Object.defineProperty(t,"style",{get:function(){return":host{--background:var(--ion-color-primary,#3880ff);--color:var(--ion-color-primary-contrast,#fff);background:var(--background);color:var(--color);font-family:var(--ion-font-family,inherit)}:host(.in-list.item-options-end:last-child){padding-right:calc(.7em + var(--ion-safe-area-right))}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){:host(.in-list.item-options-end:last-child){padding-right:unset;-webkit-padding-end:calc(.7em + var(--ion-safe-area-right));padding-inline-end:calc(.7em + var(--ion-safe-area-right))}}:host(.in-list.item-options-start:first-child){padding-left:calc(.7em + var(--ion-safe-area-left))}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){:host(.in-list.item-options-start:first-child){padding-left:unset;-webkit-padding-start:calc(.7em + var(--ion-safe-area-left));padding-inline-start:calc(.7em + var(--ion-safe-area-left))}}:host(.ion-color){background:var(--ion-color-base);color:var(--ion-color-contrast)}.button-native{font-family:inherit;font-size:inherit;font-style:inherit;font-weight:inherit;letter-spacing:inherit;text-decoration:inherit;text-overflow:inherit;text-transform:inherit;text-align:inherit;white-space:inherit;color:inherit;padding-left:.7em;padding-right:.7em;padding-top:0;padding-bottom:0;display:inline-block;position:relative;width:100%;height:100%;border:0;outline:none;background:transparent;cursor:pointer;-webkit-appearance:none;-moz-appearance:none;appearance:none;-webkit-box-sizing:border-box;box-sizing:border-box}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.button-native{padding-left:unset;padding-right:unset;-webkit-padding-start:.7em;padding-inline-start:.7em;-webkit-padding-end:.7em;padding-inline-end:.7em}}.button-inner{-ms-flex-flow:column nowrap;flex-flow:column nowrap;height:100%}.button-inner,.horizontal-wrapper{display:-ms-flexbox;display:flex;-ms-flex-negative:0;flex-shrink:0;-ms-flex-align:center;align-items:center;-ms-flex-pack:center;justify-content:center;width:100%}.horizontal-wrapper{-ms-flex-flow:row nowrap;flex-flow:row nowrap}::slotted(*){-ms-flex-negative:0;flex-shrink:0}::slotted([slot=start]){margin-left:0;margin-right:5px;margin-top:0;margin-bottom:0}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){::slotted([slot=start]){margin-left:unset;margin-right:unset;-webkit-margin-start:0;margin-inline-start:0;-webkit-margin-end:5px;margin-inline-end:5px}}::slotted([slot=end]){margin-left:5px;margin-right:0;margin-top:0;margin-bottom:0}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){::slotted([slot=end]){margin-left:unset;margin-right:unset;-webkit-margin-start:5px;margin-inline-start:5px;-webkit-margin-end:0;margin-inline-end:0}}::slotted([slot=icon-only]){padding-left:0;padding-right:0;padding-top:0;padding-bottom:0;margin-left:10px;margin-right:10px;margin-top:0;margin-bottom:0;min-width:.9em;font-size:1.8em}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){::slotted([slot=icon-only]){margin-left:unset;margin-right:unset;-webkit-margin-start:10px;margin-inline-start:10px;-webkit-margin-end:10px;margin-inline-end:10px}}:host(.item-option-expandable){-ms-flex-negative:0;flex-shrink:0;-webkit-transition-duration:0;transition-duration:0;-webkit-transition-property:none;transition-property:none;-webkit-transition-timing-function:cubic-bezier(.65,.05,.36,1);transition-timing-function:cubic-bezier(.65,.05,.36,1)}:host(.item-option-disabled){pointer-events:none}:host(.item-option-disabled) .button-native{cursor:default;opacity:.5;pointer-events:none}:host{font-size:16px}:host(.activated){background:var(--ion-color-primary-shade,#3171e0)}:host(.ion-color.activated){background:var(--ion-color-shade)}"},enumerable:true,configurable:true});return t}();t("ion_item_option",p);var l=function(){function t(t){i(this,t);this.side="end";this.ionSwipe=a(this,"ionSwipe",7)}t.prototype.fireSwipeEvent=function(){return __awaiter(this,void 0,void 0,function(){return __generator(this,function(t){this.ionSwipe.emit({side:this.side});return[2]})})};t.prototype.hostData=function(){var t;var e=n(this);var i=h(this.side);return{class:(t={},t[e]=true,t["item-options-"+e]=true,t["item-options-start"]=!i,t["item-options-end"]=i,t)}};Object.defineProperty(t.prototype,"el",{get:function(){return o(this)},enumerable:true,configurable:true});t.prototype.render=function(){return s(r,this.hostData())};Object.defineProperty(t,"style",{get:function(){return"ion-item-options{top:0;right:0;-ms-flex-pack:end;justify-content:flex-end;display:none;position:absolute;height:100%;font-size:14px;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;z-index:1}:host-context([dir=rtl]) ion-item-options,[dir=rtl] ion-item-options{-ms-flex-pack:start;justify-content:flex-start}:host-context([dir=rtl]) ion-item-options:not(.item-options-end),[dir=rtl] ion-item-options:not(.item-options-end){right:auto;left:0;-ms-flex-pack:end;justify-content:flex-end}.item-options-start{right:auto;left:0;-ms-flex-pack:start;justify-content:flex-start}:host-context([dir=rtl]) .item-options-start,[dir=rtl] .item-options-start{-ms-flex-pack:end;justify-content:flex-end}.item-options-start ion-item-option:first-child{padding-right:var(--ion-safe-area-left)}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.item-options-start ion-item-option:first-child{padding-right:unset;-webkit-padding-end:var(--ion-safe-area-left);padding-inline-end:var(--ion-safe-area-left)}}.item-options-end ion-item-option:last-child{padding-right:var(--ion-safe-area-right)}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.item-options-end ion-item-option:last-child{padding-right:unset;-webkit-padding-end:var(--ion-safe-area-right);padding-inline-end:var(--ion-safe-area-right)}}:host-context([dir=rtl]) .item-sliding-active-slide.item-sliding-active-options-start ion-item-options:not(.item-options-end),[dir=rtl] .item-sliding-active-slide.item-sliding-active-options-start ion-item-options:not(.item-options-end){width:100%;visibility:visible}.item-sliding-active-slide ion-item-options{display:-ms-flexbox;display:flex;visibility:hidden}.item-sliding-active-slide.item-sliding-active-options-end ion-item-options:not(.item-options-start),.item-sliding-active-slide.item-sliding-active-options-start .item-options-start{width:100%;visibility:visible}.item-options-ios{border-bottom-width:0;border-bottom-style:solid;border-bottom-color:var(--ion-item-border-color,var(--ion-border-color,var(--ion-color-step-150,#c8c7cc)))}.item-options-ios.item-options-end{border-bottom-width:.55px}.list-ios-lines-none .item-options-ios{border-bottom-width:0}.list-ios-lines-full .item-options-ios,.list-ios-lines-inset .item-options-ios.item-options-end{border-bottom-width:.55px}"},enumerable:true,configurable:true});return t}();t("ion_item_options",l);var f=30;var d=.55;var c;var m=function(){function t(t){i(this,t);this.item=null;this.openAmount=0;this.initialOpenAmount=0;this.optsWidthRightSide=0;this.optsWidthLeftSide=0;this.sides=0;this.optsDirty=true;this.state=2;this.disabled=false;this.ionDrag=a(this,"ionDrag",7)}t.prototype.disabledChanged=function(){if(this.gesture){this.gesture.setDisabled(this.disabled)}};t.prototype.componentDidLoad=function(){return __awaiter(this,void 0,void 0,function(){var t;var i=this;return __generator(this,function(n){switch(n.label){case 0:this.item=this.el.querySelector("ion-item");return[4,this.updateOptions()];case 1:n.sent();t=this;return[4,e.import("./p-4d6d38a9.system.js")];case 2:t.gesture=n.sent().createGesture({el:this.el,gestureName:"item-swipe",gesturePriority:100,threshold:5,canStart:function(){return i.canStart()},onStart:function(){return i.onStart()},onMove:function(t){return i.onMove(t)},onEnd:function(t){return i.onEnd(t)}});this.disabledChanged();return[2]}})})};t.prototype.componentDidUnload=function(){if(this.gesture){this.gesture.destroy();this.gesture=undefined}this.item=null;this.leftOptions=this.rightOptions=undefined;if(c===this.el){c=undefined}};t.prototype.getOpenAmount=function(){return Promise.resolve(this.openAmount)};t.prototype.getSlidingRatio=function(){return Promise.resolve(this.getSlidingRatioSync())};t.prototype.open=function(t){return __awaiter(this,void 0,void 0,function(){var e,i,n;var s=this;return __generator(this,function(r){if(this.item===null){return[2]}e=this.getOptions(t);if(!e){return[2]}if(t===undefined){t=e===this.leftOptions?"start":"end"}t=h(t)?"end":"start";i=this.openAmount<0;n=this.openAmount>0;if(i&&e===this.leftOptions){return[2]}if(n&&e===this.rightOptions){return[2]}this.closeOpened();this.state=4;requestAnimationFrame(function(){s.calculateOptsWidth();var e=t==="end"?s.optsWidthRightSide:-s.optsWidthLeftSide;c=s.el;s.setOpenAmount(e,false);s.state=t==="end"?8:16});return[2]})})};t.prototype.close=function(){return __awaiter(this,void 0,void 0,function(){return __generator(this,function(t){this.setOpenAmount(0,true);return[2]})})};t.prototype.closeOpened=function(){return __awaiter(this,void 0,void 0,function(){return __generator(this,function(t){if(c!==undefined){c.close();c=undefined;return[2,true]}return[2,false]})})};t.prototype.getOptions=function(t){if(t===undefined){return this.leftOptions||this.rightOptions}else if(t==="start"){return this.leftOptions}else{return this.rightOptions}};t.prototype.updateOptions=function(){return __awaiter(this,void 0,void 0,function(){var t,e,i,n,s;return __generator(this,function(r){switch(r.label){case 0:t=this.el.querySelectorAll("ion-item-options");e=0;this.leftOptions=this.rightOptions=undefined;i=0;r.label=1;case 1:if(!(i<t.length))return[3,4];return[4,t.item(i).componentOnReady()];case 2:n=r.sent();s=h(n.side)?"end":"start";if(s==="start"){this.leftOptions=n;e|=1}else{this.rightOptions=n;e|=2}r.label=3;case 3:i++;return[3,1];case 4:this.optsDirty=true;this.sides=e;return[2]}})})};t.prototype.canStart=function(){var t=c;if(t&&t!==this.el){this.closeOpened();return false}return!!(this.rightOptions||this.leftOptions)};t.prototype.onStart=function(){c=this.el;if(this.tmr!==undefined){clearTimeout(this.tmr);this.tmr=undefined}if(this.openAmount===0){this.optsDirty=true;this.state=4}this.initialOpenAmount=this.openAmount;if(this.item){this.item.style.transition="none"}};t.prototype.onMove=function(t){if(this.optsDirty){this.calculateOptsWidth()}var e=this.initialOpenAmount-t.deltaX;switch(this.sides){case 2:e=Math.max(0,e);break;case 1:e=Math.min(0,e);break;case 3:break;case 0:return;default:console.warn("invalid ItemSideFlags value",this.sides);break}var i;if(e>this.optsWidthRightSide){i=this.optsWidthRightSide;e=i+(e-i)*d}else if(e<-this.optsWidthLeftSide){i=-this.optsWidthLeftSide;e=i+(e-i)*d}this.setOpenAmount(e,false)};t.prototype.onEnd=function(t){var e=t.velocityX;var i=this.openAmount>0?this.optsWidthRightSide:-this.optsWidthLeftSide;var n=this.openAmount>0===!(e<0);var s=Math.abs(e)>.3;var r=Math.abs(this.openAmount)<Math.abs(i/2);if(g(n,s,r)){i=0}var o=this.state;this.setOpenAmount(i,true);if((o&32)!==0&&this.rightOptions){this.rightOptions.fireSwipeEvent()}else if((o&64)!==0&&this.leftOptions){this.leftOptions.fireSwipeEvent()}};t.prototype.calculateOptsWidth=function(){this.optsWidthRightSide=0;if(this.rightOptions){this.rightOptions.style.display="flex";this.optsWidthRightSide=this.rightOptions.offsetWidth;this.rightOptions.style.display=""}this.optsWidthLeftSide=0;if(this.leftOptions){this.leftOptions.style.display="flex";this.optsWidthLeftSide=this.leftOptions.offsetWidth;this.leftOptions.style.display=""}this.optsDirty=false};t.prototype.setOpenAmount=function(t,e){var i=this;if(this.tmr!==undefined){clearTimeout(this.tmr);this.tmr=undefined}if(!this.item){return}var n=this.item.style;this.openAmount=t;if(e){n.transition=""}if(t>0){this.state=t>=this.optsWidthRightSide+f?8|32:8}else if(t<0){this.state=t<=-this.optsWidthLeftSide-f?16|64:16}else{this.tmr=setTimeout(function(){i.state=2;i.tmr=undefined},600);c=undefined;n.transform="";return}n.transform="translate3d("+-t+"px,0,0)";this.ionDrag.emit({amount:t,ratio:this.getSlidingRatioSync()})};t.prototype.getSlidingRatioSync=function(){if(this.openAmount>0){return this.openAmount/this.optsWidthRightSide}else if(this.openAmount<0){return this.openAmount/this.optsWidthLeftSide}else{return 0}};t.prototype.hostData=function(){var t;var e=n(this);return{class:(t={},t[e]=true,t["item-sliding-active-slide"]=this.state!==2,t["item-sliding-active-options-end"]=(this.state&8)!==0,t["item-sliding-active-options-start"]=(this.state&16)!==0,t["item-sliding-active-swipe-end"]=(this.state&32)!==0,t["item-sliding-active-swipe-start"]=(this.state&64)!==0,t)}};Object.defineProperty(t.prototype,"el",{get:function(){return o(this)},enumerable:true,configurable:true});Object.defineProperty(t,"watchers",{get:function(){return{disabled:["disabledChanged"]}},enumerable:true,configurable:true});t.prototype.render=function(){return s(r,this.hostData())};Object.defineProperty(t,"style",{get:function(){return"ion-item-sliding{display:block;position:relative;width:100%;overflow:hidden}ion-item-sliding,ion-item-sliding .item{-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none}.item-sliding-active-slide .item{position:relative;-webkit-transition:-webkit-transform .5s cubic-bezier(.36,.66,.04,1);transition:-webkit-transform .5s cubic-bezier(.36,.66,.04,1);transition:transform .5s cubic-bezier(.36,.66,.04,1);transition:transform .5s cubic-bezier(.36,.66,.04,1),-webkit-transform .5s cubic-bezier(.36,.66,.04,1);opacity:1;z-index:2;pointer-events:none;will-change:transform}.item-sliding-active-swipe-end .item-options-end .item-option-expandable{padding-left:100%;-ms-flex-order:1;order:1;-webkit-transition-duration:.6s;transition-duration:.6s;-webkit-transition-property:padding-left;transition-property:padding-left}:host-context([dir=rtl]) .item-sliding-active-swipe-end .item-options-end .item-option-expandable,[dir=rtl] .item-sliding-active-swipe-end .item-options-end .item-option-expandable{-ms-flex-order:-1;order:-1}.item-sliding-active-swipe-start .item-options-start .item-option-expandable{padding-right:100%;-ms-flex-order:-1;order:-1;-webkit-transition-duration:.6s;transition-duration:.6s;-webkit-transition-property:padding-right;transition-property:padding-right}:host-context([dir=rtl]) .item-sliding-active-swipe-start .item-options-start .item-option-expandable,[dir=rtl] .item-sliding-active-swipe-start .item-options-start .item-option-expandable{-ms-flex-order:1;order:1}"},enumerable:true,configurable:true});return t}();t("ion_item_sliding",m);function g(t,e,i){return!e&&i||t&&e}}}});