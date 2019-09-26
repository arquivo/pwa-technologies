import*as tslib_1 from"../polyfills/tslib.js";import{h}from"../ionic.core.js";import{c as createColorClasses,d as hostContext,e as createThemedClasses}from"./chunk-7c632336.js";import{a as rIC}from"./chunk-6d7d2f8c.js";import{a as isPlatform}from"./chunk-99b2d1db.js";var App=function(){function e(){}return e.prototype.componentDidLoad=function(){var e=this;rIC(function(){var t=e.win,o=e.config,n=e.queue;o.getBoolean("_testing")||importTapClick(t,o),importInputShims(t,o),importStatusTap(t,o,n),importHardwareBackButton(t,o),importFocusVisible(t)})},e.prototype.hostData=function(){return{class:{"ion-page":!0,"force-statusbar-padding":this.config.getBoolean("_forceStatusbarPadding")}}},Object.defineProperty(e,"is",{get:function(){return"ion-app"},enumerable:!0,configurable:!0}),Object.defineProperty(e,"properties",{get:function(){return{config:{context:"config"},el:{elementRef:!0},queue:{context:"queue"},win:{context:"window"}}},enumerable:!0,configurable:!0}),Object.defineProperty(e,"style",{get:function(){return"html.plt-mobile ion-app{-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none}ion-app.force-statusbar-padding{--ion-safe-area-top:20px}"},enumerable:!0,configurable:!0}),e}();function importHardwareBackButton(e,t){t.getBoolean("hardwareBackButton",isPlatform(e,"hybrid"))&&import("./chunk-f5118fa0.js").then(function(t){return t.startHardwareBackButton(e)})}function importStatusTap(e,t,o){t.getBoolean("statusTap",isPlatform(e,"hybrid"))&&import("./chunk-75ece731.js").then(function(t){return t.startStatusTap(e,o)})}function importFocusVisible(e){import("./chunk-2a112823.js").then(function(t){return t.startFocusVisible(e.document)})}function importTapClick(e,t){import("./chunk-4513ec06.js").then(function(o){return o.startTapClick(e.document,t)})}function importInputShims(e,t){t.getBoolean("inputShims",needInputShims(e))&&import("./chunk-b465a1c6.js").then(function(o){return o.startInputShims(e.document,t)})}function needInputShims(e){return isPlatform(e,"ios")&&isPlatform(e,"mobile")}var Buttons=function(){function e(){}return Object.defineProperty(e,"is",{get:function(){return"ion-buttons"},enumerable:!0,configurable:!0}),Object.defineProperty(e,"encapsulation",{get:function(){return"scoped"},enumerable:!0,configurable:!0}),Object.defineProperty(e,"style",{get:function(){return".sc-ion-buttons-md-h{display:-ms-flexbox;display:flex;-ms-flex-align:center;align-items:center;-webkit-transform:translateZ(0);transform:translateZ(0);z-index:99}.sc-ion-buttons-md-s  ion-button {--padding-start:0;--padding-end:0;--overflow:visible;margin-left:0;margin-right:0;margin-top:0;margin-bottom:0;margin-left:2px;margin-right:2px}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.sc-ion-buttons-md-s  ion-button {margin-left:unset;margin-right:unset;-webkit-margin-start:2px;margin-inline-start:2px;-webkit-margin-end:2px;margin-inline-end:2px}}.sc-ion-buttons-md-s  ion-button {--padding-top:0;--padding-bottom:0;--padding-start:8px;--padding-end:8px;--box-shadow:none;height:32px;font-size:14px;font-weight:500}.sc-ion-buttons-md-s  ion-button:not(.button-round) {--border-radius:2px}.sc-ion-buttons-md-h.ion-color.sc-ion-buttons-md-s  .button , .ion-color .sc-ion-buttons-md-h.sc-ion-buttons-md-s  .button {--color:initial;--color-activated:initial;--color-focused:initial;--background-focused:rgba(var(--ion-color-primary-contrast-rgb,255,255,255),0.1)}.sc-ion-buttons-md-s  .button-solid {--color:var(--ion-toolbar-background,var(--ion-background-color,#fff));--color-activated:var(--ion-toolbar-background,var(--ion-background-color,#fff));--background:var(--ion-toolbar-color,var(--ion-text-color,#424242));--background-activated:var(--ion-toolbar-color,var(--ion-text-color,#424242))}.sc-ion-buttons-md-s  .button-outline {--color:initial;--color-activated:currentColor;--background:transparent;--background-activated:transparent;--border-color:currentColor}.sc-ion-buttons-md-s  .button-clear {--color:initial;--color-activated:currentColor;--background:transparent}.sc-ion-buttons-md-s  ion-icon[slot=start] {margin-left:0;margin-right:0;margin-top:0;margin-bottom:0;margin-right:.3em;font-size:1.4em}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.sc-ion-buttons-md-s  ion-icon[slot=start] {margin-right:unset;-webkit-margin-end:.3em;margin-inline-end:.3em}}.sc-ion-buttons-md-s  ion-icon[slot=end] {margin-left:0;margin-right:0;margin-top:0;margin-bottom:0;margin-left:.4em;font-size:1.4em}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.sc-ion-buttons-md-s  ion-icon[slot=end] {margin-left:unset;-webkit-margin-start:.4em;margin-inline-start:.4em}}.sc-ion-buttons-md-s  ion-icon[slot=icon-only] {padding-left:0;padding-right:0;padding-top:0;padding-bottom:0;margin-left:0;margin-right:0;margin-top:0;margin-bottom:0;font-size:1.8em}"},enumerable:!0,configurable:!0}),Object.defineProperty(e,"styleMode",{get:function(){return"md"},enumerable:!0,configurable:!0}),e}(),Content=function(){function e(){this.isScrolling=!1,this.lastScroll=0,this.queued=!1,this.cTop=-1,this.cBottom=-1,this.detail={scrollTop:0,scrollLeft:0,type:"scroll",event:void 0,startX:0,startY:0,startTimeStamp:0,currentX:0,currentY:0,velocityX:0,velocityY:0,deltaX:0,deltaY:0,timeStamp:0,data:void 0,isScrolling:!0},this.fullscreen=!1,this.scrollX=!1,this.scrollY=!0,this.scrollEvents=!1}return e.prototype.componentWillLoad=function(){void 0===this.forceOverscroll&&(this.forceOverscroll="ios"===this.mode&&isPlatform(this.win,"mobile"))},e.prototype.componentDidLoad=function(){this.resize()},e.prototype.componentDidUnload=function(){this.onScrollEnd()},e.prototype.onClick=function(e){this.isScrolling&&(e.preventDefault(),e.stopPropagation())},e.prototype.resize=function(){this.fullscreen?this.queue.read(this.readDimensions.bind(this)):0===this.cTop&&0===this.cBottom||(this.cTop=this.cBottom=0,this.el.forceUpdate())},e.prototype.readDimensions=function(){var e=getPageElement(this.el),t=Math.max(this.el.offsetTop,0),o=Math.max(e.offsetHeight-t-this.el.offsetHeight,0);(t!==this.cTop||o!==this.cBottom)&&(this.cTop=t,this.cBottom=o,this.el.forceUpdate())},e.prototype.onScroll=function(e){var t=this,o=Date.now(),n=!this.isScrolling;this.lastScroll=o,n&&this.onScrollStart(),!this.queued&&this.scrollEvents&&(this.queued=!0,this.queue.read(function(o){t.queued=!1,t.detail.event=e,updateScrollDetail(t.detail,t.scrollEl,o,n),t.ionScroll.emit(t.detail)}))},e.prototype.getScrollElement=function(){return Promise.resolve(this.scrollEl)},e.prototype.scrollToTop=function(e){return void 0===e&&(e=0),this.scrollToPoint(void 0,0,e)},e.prototype.scrollToBottom=function(e){return void 0===e&&(e=0),this.scrollToPoint(void 0,this.scrollEl.scrollHeight-this.scrollEl.clientHeight,e)},e.prototype.scrollByPoint=function(e,t,o){return this.scrollToPoint(e+this.scrollEl.scrollLeft,t+this.scrollEl.scrollTop,o)},e.prototype.scrollToPoint=function(e,t,o){return void 0===o&&(o=0),tslib_1.__awaiter(this,void 0,void 0,function(){var n,r,l,i,c,s,a,u,f;return tslib_1.__generator(this,function(p){return n=this.scrollEl,o<32?(null!=t&&(n.scrollTop=t),null!=e&&(n.scrollLeft=e),[2]):(l=0,i=new Promise(function(e){return r=e}),c=n.scrollTop,s=n.scrollLeft,a=null!=t?t-c:0,u=null!=e?e-s:0,f=function(e){var t=Math.min(1,(e-l)/o)-1,i=Math.pow(t,3)+1;0!==a&&(n.scrollTop=Math.floor(i*a+c)),0!==u&&(n.scrollLeft=Math.floor(i*u+s)),i<1?requestAnimationFrame(f):r()},requestAnimationFrame(function(e){l=e,f(e)}),[2,i])})})},e.prototype.onScrollStart=function(){var e=this;this.isScrolling=!0,this.ionScrollStart.emit({isScrolling:!0}),this.watchDog&&clearInterval(this.watchDog),this.watchDog=setInterval(function(){e.lastScroll<Date.now()-120&&e.onScrollEnd()},100)},e.prototype.onScrollEnd=function(){clearInterval(this.watchDog),this.watchDog=null,this.isScrolling&&(this.isScrolling=!1,this.ionScrollEnd.emit({isScrolling:!1}))},e.prototype.hostData=function(){return{class:Object.assign({},createColorClasses(this.color),{"content-sizing":hostContext("ion-popover",this.el),overscroll:!!this.forceOverscroll}),style:{"--offset-top":this.cTop+"px","--offset-bottom":this.cBottom+"px"}}},e.prototype.render=function(){var e=this,t=this.scrollX,o=this.scrollY,n=this.forceOverscroll;return this.resize(),[h("div",{class:{"inner-scroll":!0,"scroll-x":t,"scroll-y":o,overscroll:(t||o)&&!!n},ref:function(t){return e.scrollEl=t},onScroll:function(t){return e.onScroll(t)}},h("slot",null)),h("slot",{name:"fixed"})]},Object.defineProperty(e,"is",{get:function(){return"ion-content"},enumerable:!0,configurable:!0}),Object.defineProperty(e,"encapsulation",{get:function(){return"shadow"},enumerable:!0,configurable:!0}),Object.defineProperty(e,"properties",{get:function(){return{color:{type:String,attr:"color"},config:{context:"config"},el:{elementRef:!0},forceOverscroll:{type:Boolean,attr:"force-overscroll",mutable:!0},fullscreen:{type:Boolean,attr:"fullscreen"},getScrollElement:{method:!0},queue:{context:"queue"},scrollByPoint:{method:!0},scrollEvents:{type:Boolean,attr:"scroll-events"},scrollToBottom:{method:!0},scrollToPoint:{method:!0},scrollToTop:{method:!0},scrollX:{type:Boolean,attr:"scroll-x"},scrollY:{type:Boolean,attr:"scroll-y"},win:{context:"window"}}},enumerable:!0,configurable:!0}),Object.defineProperty(e,"events",{get:function(){return[{name:"ionScrollStart",method:"ionScrollStart",bubbles:!0,cancelable:!0,composed:!0},{name:"ionScroll",method:"ionScroll",bubbles:!0,cancelable:!0,composed:!0},{name:"ionScrollEnd",method:"ionScrollEnd",bubbles:!0,cancelable:!0,composed:!0}]},enumerable:!0,configurable:!0}),Object.defineProperty(e,"listeners",{get:function(){return[{name:"click",method:"onClick",capture:!0}]},enumerable:!0,configurable:!0}),Object.defineProperty(e,"style",{get:function(){return":host{--background:var(--ion-background-color,#fff);--color:var(--ion-text-color,#000);--padding-top:0px;--padding-bottom:0px;--padding-start:0px;--padding-end:0px;--keyboard-offset:0px;--offset-top:0px;--offset-bottom:0px;--overflow:auto;display:block;position:relative;-ms-flex:1;flex:1;width:100%;height:100%;margin:0!important;padding:0!important;font-family:var(--ion-font-family,inherit);contain:size style}:host(.ion-color) .inner-scroll{background:var(--ion-color-base);color:var(--ion-color-contrast)}:host(.outer-content){--background:var(--ion-color-step-50,#f2f2f2)}.inner-scroll{left:0;right:0;top:calc(var(--offset-top) * -1);bottom:calc(var(--offset-bottom) * -1);padding-left:var(--padding-start);padding-right:var(--padding-end);padding-top:calc(var(--padding-top) + var(--offset-top));padding-bottom:calc(var(--padding-bottom) + var(--keyboard-offset) + var(--offset-bottom));position:absolute;background:var(--background);color:var(--color);-webkit-box-sizing:border-box;box-sizing:border-box;overflow:hidden}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.inner-scroll{padding-left:unset;padding-right:unset;-webkit-padding-start:var(--padding-start);padding-inline-start:var(--padding-start);-webkit-padding-end:var(--padding-end);padding-inline-end:var(--padding-end)}}.scroll-x,.scroll-y{-webkit-overflow-scrolling:touch;will-change:scroll-position;-ms-scroll-chaining:none;overscroll-behavior:contain}.scroll-y{-ms-touch-action:pan-y;touch-action:pan-y;overflow-y:var(--overflow)}.scroll-x{-ms-touch-action:pan-x;touch-action:pan-x;overflow-x:var(--overflow)}.scroll-x.scroll-y{-ms-touch-action:auto;touch-action:auto}.overscroll:after,.overscroll:before{position:absolute;width:1px;height:1px;content:\"\"}.overscroll:before{bottom:-1px}.overscroll:after{top:-1px}:host(.content-sizing){contain:none}:host(.content-sizing) .inner-scroll{position:relative}"},enumerable:!0,configurable:!0}),e}();function getParentElement(e){return e.parentElement?e.parentElement:e.parentNode&&e.parentNode.host?e.parentNode.host:null}function getPageElement(e){var t=e.closest("ion-tabs");return t||(e.closest("ion-app,ion-page,.ion-page,page-inner")||getParentElement(e))}function updateScrollDetail(e,t,o,n){var r=e.currentX,l=e.currentY,i=e.timeStamp,c=t.scrollLeft,s=t.scrollTop;n&&(e.startTimeStamp=o,e.startX=c,e.startY=s,e.velocityX=e.velocityY=0),e.timeStamp=o,e.currentX=e.scrollLeft=c,e.currentY=e.scrollTop=s,e.deltaX=c-e.startX,e.deltaY=s-e.startY;var a=o-i;if(a>0&&a<100){var u=(s-l)/a;e.velocityX=(c-r)/a*.7+.3*e.velocityX,e.velocityY=.7*u+.3*e.velocityY}}var Footer=function(){function e(){this.translucent=!1}return e.prototype.hostData=function(){var e=createThemedClasses(this.mode,"footer"),t=this.translucent?createThemedClasses(this.mode,"footer-translucent"):null;return{class:Object.assign({},e,t)}},Object.defineProperty(e,"is",{get:function(){return"ion-footer"},enumerable:!0,configurable:!0}),Object.defineProperty(e,"properties",{get:function(){return{mode:{type:String,attr:"mode"},translucent:{type:Boolean,attr:"translucent"}}},enumerable:!0,configurable:!0}),Object.defineProperty(e,"style",{get:function(){return"ion-footer{display:block;position:relative;-ms-flex-order:1;order:1;width:100%;z-index:10}ion-footer ion-toolbar:last-child{padding-bottom:var(--ion-safe-area-bottom,0)}.footer-md:before{left:0;top:-2px;bottom:auto;background-position:left 0 top 0;position:absolute;width:100%;height:2px;background-image:url(\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAHBAMAAADzDtBxAAAAD1BMVEUAAAAAAAAAAAAAAAAAAABPDueNAAAABXRSTlMUCS0gBIh/TXEAAAAaSURBVAjXYxCEAgY4UIICBmMogMsgFLtAAQCNSwXZKOdPxgAAAABJRU5ErkJggg==\");background-repeat:repeat-x;content:\"\"}:host-context([dir=rtl]) .footer-md:before{right:0;background-position:right 0 top 0}.footer-md[no-border]:before{display:none}"},enumerable:!0,configurable:!0}),Object.defineProperty(e,"styleMode",{get:function(){return"md"},enumerable:!0,configurable:!0}),e}(),Header=function(){function e(){this.translucent=!1}return e.prototype.hostData=function(){var e=createThemedClasses(this.mode,"header"),t=this.translucent?createThemedClasses(this.mode,"header-translucent"):null;return{class:Object.assign({},e,t)}},Object.defineProperty(e,"is",{get:function(){return"ion-header"},enumerable:!0,configurable:!0}),Object.defineProperty(e,"properties",{get:function(){return{mode:{type:String,attr:"mode"},translucent:{type:Boolean,attr:"translucent"}}},enumerable:!0,configurable:!0}),Object.defineProperty(e,"style",{get:function(){return"ion-header{display:block;position:relative;-ms-flex-order:-1;order:-1;width:100%;z-index:10}ion-header ion-toolbar:first-child{padding-top:var(--ion-safe-area-top,0)}.header-md:after{left:0;bottom:-8px;background-position:left 0 top 0;position:absolute;width:100%;height:8px;background-image:url(\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAIBAMAAAACWGKkAAAAFVBMVEUAAAAAAAAAAAAAAAAAAAAAAAAAAAASAQCkAAAAB3RSTlMFTEIzJBcOYhQUIwAAAB9JREFUCNdjEIQCBiUoYDCGAgYXKGAIhQKGNChgwAAAorMLKSCkL40AAAAASUVORK5CYII=\");background-repeat:repeat-x;content:\"\"}:host-context([dir=rtl]) .header-md:after{right:0;background-position:right 0 top 0}.header-md[no-border]:after{display:none}"},enumerable:!0,configurable:!0}),Object.defineProperty(e,"styleMode",{get:function(){return"md"},enumerable:!0,configurable:!0}),e}(),ToolbarTitle=function(){function e(){}return e.prototype.getMode=function(){var e=this.el.closest("ion-toolbar");return e&&e.mode||this.mode},e.prototype.hostData=function(){var e,t=this.getMode();return{class:Object.assign({},createColorClasses(this.color),(e={},e["title-"+t]=!0,e))}},e.prototype.render=function(){return[h("div",{class:"toolbar-title"},h("slot",null))]},Object.defineProperty(e,"is",{get:function(){return"ion-title"},enumerable:!0,configurable:!0}),Object.defineProperty(e,"encapsulation",{get:function(){return"shadow"},enumerable:!0,configurable:!0}),Object.defineProperty(e,"properties",{get:function(){return{color:{type:String,attr:"color"},el:{elementRef:!0}}},enumerable:!0,configurable:!0}),Object.defineProperty(e,"style",{get:function(){return":host{--color:initial;display:-ms-flexbox;display:flex;-ms-flex:1;flex:1;-ms-flex-align:center;align-items:center;color:var(--color)}:host,:host(.title-ios){-webkit-transform:translateZ(0);transform:translateZ(0)}:host(.title-ios){left:0;top:0;padding-left:90px;padding-right:90px;padding-top:0;padding-bottom:0;position:absolute;width:100%;height:100%;font-size:17px;font-weight:600;letter-spacing:-.03em;text-align:center;-webkit-box-sizing:border-box;box-sizing:border-box;pointer-events:none}:host([dir=rtl].title-ios){right:0}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){:host(.title-ios){padding-left:unset;padding-right:unset;-webkit-padding-start:90px;padding-inline-start:90px;-webkit-padding-end:90px;padding-inline-end:90px}}:host(.title-md){padding-left:12px;padding-right:12px;padding-top:0;padding-bottom:0;font-size:20px;font-weight:500}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){:host(.title-md){padding-left:unset;padding-right:unset;-webkit-padding-start:12px;padding-inline-start:12px;-webkit-padding-end:12px;padding-inline-end:12px}}:host(.ion-color){color:var(--ion-color-base)}.toolbar-title{display:block;width:100%;text-overflow:ellipsis;white-space:nowrap;overflow:hidden;pointer-events:auto}"},enumerable:!0,configurable:!0}),e}(),Toolbar=function(){function e(){this.childrenStyles=new Map}return e.prototype.childrenStyle=function(e){e.stopPropagation();var t=e.target.tagName,o=e.detail,n={},r=this.childrenStyles.get(t)||{},l=!1;Object.keys(o).forEach(function(e){var t="toolbar-"+e,i=o[e];i!==r[t]&&(l=!0),i&&(n[t]=!0)}),l&&(this.childrenStyles.set(t,n),this.el.forceUpdate())},e.prototype.hostData=function(){var e={};return this.childrenStyles.forEach(function(t){Object.assign(e,t)}),{class:Object.assign({},e,createColorClasses(this.color))}},e.prototype.render=function(){return[h("div",{class:"toolbar-background"}),h("div",{class:"toolbar-container"},h("slot",{name:"start"}),h("slot",{name:"secondary"}),h("div",{class:"toolbar-content"},h("slot",null)),h("slot",{name:"primary"}),h("slot",{name:"end"}))]},Object.defineProperty(e,"is",{get:function(){return"ion-toolbar"},enumerable:!0,configurable:!0}),Object.defineProperty(e,"encapsulation",{get:function(){return"shadow"},enumerable:!0,configurable:!0}),Object.defineProperty(e,"properties",{get:function(){return{color:{type:String,attr:"color"},config:{context:"config"},el:{elementRef:!0},mode:{type:String,attr:"mode"}}},enumerable:!0,configurable:!0}),Object.defineProperty(e,"listeners",{get:function(){return[{name:"ionStyle",method:"childrenStyle"}]},enumerable:!0,configurable:!0}),Object.defineProperty(e,"style",{get:function(){return":host{--border-width:0;--border-style:solid;--opacity:1;-moz-osx-font-smoothing:grayscale;-webkit-font-smoothing:antialiased;padding-left:var(--ion-safe-area-left);padding-right:var(--ion-safe-area-right);display:block;position:relative;width:100%;color:var(--color);font-family:var(--ion-font-family,inherit);contain:content;z-index:10;-webkit-box-sizing:border-box;box-sizing:border-box}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){:host{padding-left:unset;padding-right:unset;-webkit-padding-start:var(--ion-safe-area-left);padding-inline-start:var(--ion-safe-area-left);-webkit-padding-end:var(--ion-safe-area-right);padding-inline-end:var(--ion-safe-area-right)}}:host(.ion-color){color:var(--ion-color-contrast)}:host(.ion-color) .toolbar-background{background:var(--ion-color-base)}.toolbar-container{padding-left:var(--padding-start);padding-right:var(--padding-end);padding-top:var(--padding-top);padding-bottom:var(--padding-bottom);display:-ms-flexbox;display:flex;position:relative;-ms-flex-direction:row;flex-direction:row;-ms-flex-align:center;align-items:center;-ms-flex-pack:justify;justify-content:space-between;width:100%;min-height:var(--min-height);contain:content;overflow:hidden;z-index:10;-webkit-box-sizing:border-box;box-sizing:border-box}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.toolbar-container{padding-left:unset;padding-right:unset;-webkit-padding-start:var(--padding-start);padding-inline-start:var(--padding-start);-webkit-padding-end:var(--padding-end);padding-inline-end:var(--padding-end)}}.toolbar-background{left:0;right:0;top:0;bottom:0;position:absolute;-webkit-transform:translateZ(0);transform:translateZ(0);border-width:var(--border-width);border-style:var(--border-style);border-color:var(--border-color);background:var(--background);contain:strict;opacity:var(--opacity);z-index:-1;pointer-events:none}:host(.toolbar-segment){--min-height:auto}::slotted(ion-progress-bar){left:0;right:0;bottom:0;position:absolute}:host{--background:var(--ion-toolbar-background,var(--ion-background-color,#fff));--color:var(--ion-toolbar-color,var(--ion-text-color,#424242));--border-color:var(--ion-toolbar-border-color,var(--ion-border-color,var(--ion-color-step-150,#c1c4cd)));--padding-top:4px;--padding-bottom:4px;--padding-start:4px;--padding-end:4px;--min-height:56px}.toolbar-content{-ms-flex:1;flex:1;-ms-flex-order:3;order:3;min-width:0;max-width:100%}:host(.toolbar-segment){--padding-top:0;--padding-bottom:0;--padding-start:0;--padding-end:0}::slotted([slot=start]){-ms-flex-order:2;order:2}::slotted([slot=secondary]){-ms-flex-order:4;order:4}::slotted([slot=primary]){-ms-flex-order:5;order:5;text-align:end}::slotted([slot=end]){-ms-flex-order:6;order:6;text-align:end}"},enumerable:!0,configurable:!0}),Object.defineProperty(e,"styleMode",{get:function(){return"md"},enumerable:!0,configurable:!0}),e}();export{App as IonApp,Buttons as IonButtons,Content as IonContent,Footer as IonFooter,Header as IonHeader,ToolbarTitle as IonTitle,Toolbar as IonToolbar};