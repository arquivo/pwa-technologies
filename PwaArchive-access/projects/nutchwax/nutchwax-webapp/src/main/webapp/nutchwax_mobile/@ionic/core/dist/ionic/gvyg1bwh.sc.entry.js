const t=window.Ionic.h;import{c as e}from"./chunk-7c632336.js";import{g as i}from"./chunk-6d7d2f8c.js";class s{constructor(){this.disabled=!1,this.expandable=!1}onClick(t){t.target.closest("ion-item-option")&&t.preventDefault()}hostData(){return{class:Object.assign({},e(this.color),{"item-option-expandable":this.expandable,"ion-activatable":!0})}}render(){return t(void 0===this.href?"button":"a",{type:"button",class:"button-native",disabled:this.disabled,href:this.href},t("span",{class:"button-inner"},t("slot",{name:"start"}),t("slot",{name:"top"}),t("slot",{name:"icon-only"}),t("slot",null),t("slot",{name:"bottom"}),t("slot",{name:"end"})),"md"===this.mode&&t("ion-ripple-effect",null))}static get is(){return"ion-item-option"}static get encapsulation(){return"shadow"}static get properties(){return{color:{type:String,attr:"color"},disabled:{type:Boolean,attr:"disabled"},el:{elementRef:!0},expandable:{type:Boolean,attr:"expandable"},href:{type:String,attr:"href"},mode:{type:String,attr:"mode"}}}static get listeners(){return[{name:"click",method:"onClick"}]}static get style(){return".sc-ion-item-option-ios-h{--background:var(--ion-color-primary,#3880ff);--color:var(--ion-color-primary-contrast,#fff);background:var(--background);color:var(--color);font-family:var(--ion-font-family,inherit)}.in-list.item-options-end.sc-ion-item-option-ios-h:last-child{padding-right:calc(.7em + var(--ion-safe-area-right))}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.in-list.item-options-end.sc-ion-item-option-ios-h:last-child{padding-right:unset;-webkit-padding-end:calc(.7em + var(--ion-safe-area-right));padding-inline-end:calc(.7em + var(--ion-safe-area-right))}}.in-list.item-options-start.sc-ion-item-option-ios-h:first-child{padding-left:calc(.7em + var(--ion-safe-area-left))}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.in-list.item-options-start.sc-ion-item-option-ios-h:first-child{padding-left:unset;-webkit-padding-start:calc(.7em + var(--ion-safe-area-left));padding-inline-start:calc(.7em + var(--ion-safe-area-left))}}.ion-color.sc-ion-item-option-ios-h{background:var(--ion-color-base);color:var(--ion-color-contrast)}.button-native.sc-ion-item-option-ios{font-family:inherit;font-size:inherit;font-style:inherit;font-weight:inherit;letter-spacing:inherit;text-decoration:inherit;text-overflow:inherit;text-transform:inherit;text-align:inherit;white-space:inherit;color:inherit;padding-left:.7em;padding-right:.7em;padding-top:0;padding-bottom:0;position:relative;width:100%;height:100%;border:0;outline:none;background:transparent;cursor:pointer;-webkit-appearance:none;-moz-appearance:none;appearance:none}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.button-native.sc-ion-item-option-ios{padding-left:unset;padding-right:unset;-webkit-padding-start:.7em;padding-inline-start:.7em;-webkit-padding-end:.7em;padding-inline-end:.7em}}.button-inner.sc-ion-item-option-ios{display:-ms-flexbox;display:flex;-ms-flex-direction:column;flex-direction:column;-ms-flex-flow:row nowrap;flex-flow:row nowrap;-ms-flex-negative:0;flex-shrink:0;-ms-flex-align:center;align-items:center;-ms-flex-pack:center;justify-content:center;width:100%;height:100%}.sc-ion-item-option-ios-s > [slot=icon-only]{padding-left:0;padding-right:0;padding-top:0;padding-bottom:0;margin-left:10px;margin-right:10px;margin-top:0;margin-bottom:0;min-width:.9em;font-size:1.8em}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.sc-ion-item-option-ios-s > [slot=icon-only]{margin-left:unset;margin-right:unset;-webkit-margin-start:10px;margin-inline-start:10px;-webkit-margin-end:10px;margin-inline-end:10px}}.item-option-expandable.sc-ion-item-option-ios-h{-ms-flex-negative:0;flex-shrink:0;-webkit-transition-duration:0;transition-duration:0;-webkit-transition-property:none;transition-property:none;-webkit-transition-timing-function:cubic-bezier(.65,.05,.36,1);transition-timing-function:cubic-bezier(.65,.05,.36,1)}.sc-ion-item-option-ios-h{font-size:16px}.activated.sc-ion-item-option-ios-h{background:var(--ion-color-primary-shade,#3171e0)}.ion-color.activated.sc-ion-item-option-ios-h{background:var(--ion-color-shade)}"}static get styleMode(){return"ios"}}class o{constructor(){this.side="end"}fireSwipeEvent(){this.ionSwipe.emit({side:this.side})}hostData(){const t=i(this.win,this.side);return{class:{"item-options-start":!t,"item-options-end":t}}}static get is(){return"ion-item-options"}static get properties(){return{el:{elementRef:!0},fireSwipeEvent:{method:!0},side:{type:String,attr:"side"},win:{context:"window"}}}static get events(){return[{name:"ionSwipe",method:"ionSwipe",bubbles:!0,cancelable:!0,composed:!0}]}static get style(){return"ion-item-options{top:0;right:0;-ms-flex-pack:end;justify-content:flex-end;display:none;position:absolute;height:100%;font-size:14px;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;z-index:1}:host-context([dir=rtl]) ion-item-options{-ms-flex-pack:start;justify-content:flex-start}:host-context([dir=rtl]) ion-item-options:not(.item-options-end){right:auto;left:0;-ms-flex-pack:end;justify-content:flex-end}.item-options-start{right:auto;left:0;-ms-flex-pack:start;justify-content:flex-start}:host-context([dir=rtl]) .item-options-start{-ms-flex-pack:end;justify-content:flex-end}.item-options-start ion-item-option:first-child{padding-right:var(--ion-safe-area-left)}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.item-options-start ion-item-option:first-child{padding-right:unset;-webkit-padding-end:var(--ion-safe-area-left);padding-inline-end:var(--ion-safe-area-left)}}.item-options-end ion-item-option:last-child{padding-right:var(--ion-safe-area-right)}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.item-options-end ion-item-option:last-child{padding-right:unset;-webkit-padding-end:var(--ion-safe-area-right);padding-inline-end:var(--ion-safe-area-right)}}:host-context([dir=rtl]) .item-sliding-active-slide.item-sliding-active-options-start ion-item-options:not(.item-options-end){width:100%;visibility:visible}.item-sliding-active-slide ion-item-options{display:-ms-flexbox;display:flex}.item-sliding-active-slide.item-sliding-active-options-end ion-item-options:not(.item-options-start),.item-sliding-active-slide.item-sliding-active-options-start .item-options-start{width:100%;visibility:visible}.item-options-ios{border-bottom-width:0;border-bottom-style:solid;border-bottom-color:var(--ion-item-border-color,var(--ion-border-color,var(--ion-color-step-150,#c8c7cc)))}.item-options-ios.item-options-end{border-bottom-width:.55px}.list-ios-lines-none .item-options-ios{border-bottom-width:0}.list-ios-lines-full .item-options-ios,.list-ios-lines-inset .item-options-ios.item-options-end{border-bottom-width:.55px}"}static get styleMode(){return"ios"}}const n=30,a=.55;let h;class r{constructor(){this.item=null,this.openAmount=0,this.initialOpenAmount=0,this.optsWidthRightSide=0,this.optsWidthLeftSide=0,this.sides=0,this.optsDirty=!0,this.state=2,this.disabled=!1}disabledChanged(){this.gesture&&this.gesture.setDisabled(this.disabled)}async componentDidLoad(){this.item=this.el.querySelector("ion-item"),await this.updateOptions(),this.gesture=(await import("./chunk-f56eaea8.js")).createGesture({el:this.el,queue:this.queue,gestureName:"item-swipe",gesturePriority:100,threshold:5,canStart:()=>this.canStart(),onStart:()=>this.onStart(),onMove:t=>this.onMove(t),onEnd:t=>this.onEnd(t)}),this.disabledChanged()}componentDidUnload(){this.gesture&&(this.gesture.destroy(),this.gesture=void 0),this.item=null,this.leftOptions=this.rightOptions=void 0}getOpenAmount(){return Promise.resolve(this.openAmount)}getSlidingRatio(){return Promise.resolve(this.getSlidingRatioSync())}async close(){this.setOpenAmount(0,!0)}async closeOpened(){return void 0!==h&&(h.close(),!0)}async updateOptions(){const t=this.el.querySelectorAll("ion-item-options");let e=0;this.leftOptions=this.rightOptions=void 0;for(let i=0;i<t.length;i++){const s=await t.item(i).componentOnReady();"start"===s.side?(this.leftOptions=s,e|=1):(this.rightOptions=s,e|=2)}this.optsDirty=!0,this.sides=e}canStart(){return h&&h!==this.el?(this.closeOpened(),!1):!(!this.rightOptions&&!this.leftOptions)}onStart(){h=this.el,void 0!==this.tmr&&(clearTimeout(this.tmr),this.tmr=void 0),0===this.openAmount&&(this.optsDirty=!0,this.state=4),this.initialOpenAmount=this.openAmount,this.item&&(this.item.style.transition="none")}onMove(t){this.optsDirty&&this.calculateOptsWidth();let e,i=this.initialOpenAmount-t.deltaX;switch(this.sides){case 2:i=Math.max(0,i);break;case 1:i=Math.min(0,i);break;case 3:break;case 0:return;default:console.warn("invalid ItemSideFlags value",this.sides)}i>this.optsWidthRightSide?i=(e=this.optsWidthRightSide)+(i-e)*a:i<-this.optsWidthLeftSide&&(i=(e=-this.optsWidthLeftSide)+(i-e)*a),this.setOpenAmount(i,!1)}onEnd(t){const e=t.velocityX;let i=this.openAmount>0?this.optsWidthRightSide:-this.optsWidthLeftSide;(function(t,e,i){return!e&&i||t&&e})(this.openAmount>0==!(e<0),Math.abs(e)>.3,Math.abs(this.openAmount)<Math.abs(i/2))&&(i=0);const s=this.state;this.setOpenAmount(i,!0),0!=(32&s)&&this.rightOptions?this.rightOptions.fireSwipeEvent():0!=(64&s)&&this.leftOptions&&this.leftOptions.fireSwipeEvent()}calculateOptsWidth(){this.optsWidthRightSide=0,this.rightOptions&&(this.optsWidthRightSide=this.rightOptions.offsetWidth),this.optsWidthLeftSide=0,this.leftOptions&&(this.optsWidthLeftSide=this.leftOptions.offsetWidth),this.optsDirty=!1}setOpenAmount(t,e){if(void 0!==this.tmr&&(clearTimeout(this.tmr),this.tmr=void 0),!this.item)return;const i=this.item.style;if(this.openAmount=t,e&&(i.transition=""),t>0)this.state=t>=this.optsWidthRightSide+n?40:8;else{if(!(t<0))return this.tmr=window.setTimeout(()=>{this.state=2,this.tmr=void 0},600),h=void 0,void(i.transform="");this.state=t<=-this.optsWidthLeftSide-n?80:16}i.transform=`translate3d(${-t}px,0,0)`,this.ionDrag.emit({amount:t,ratio:this.getSlidingRatioSync()})}getSlidingRatioSync(){return this.openAmount>0?this.openAmount/this.optsWidthRightSide:this.openAmount<0?this.openAmount/this.optsWidthLeftSide:0}hostData(){return{class:{"item-sliding-active-slide":2!==this.state,"item-sliding-active-options-end":0!=(8&this.state),"item-sliding-active-options-start":0!=(16&this.state),"item-sliding-active-swipe-end":0!=(32&this.state),"item-sliding-active-swipe-start":0!=(64&this.state)}}}static get is(){return"ion-item-sliding"}static get properties(){return{close:{method:!0},closeOpened:{method:!0},disabled:{type:Boolean,attr:"disabled",watchCallbacks:["disabledChanged"]},el:{elementRef:!0},getOpenAmount:{method:!0},getSlidingRatio:{method:!0},queue:{context:"queue"},state:{state:!0}}}static get events(){return[{name:"ionDrag",method:"ionDrag",bubbles:!0,cancelable:!0,composed:!0}]}static get style(){return"ion-item-sliding{display:block;position:relative;width:100%;overflow:hidden}ion-item-sliding,ion-item-sliding .item{-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none}.item-sliding-active-slide .item{position:relative;-webkit-transition:-webkit-transform .5s cubic-bezier(.36,.66,.04,1);transition:-webkit-transform .5s cubic-bezier(.36,.66,.04,1);transition:transform .5s cubic-bezier(.36,.66,.04,1);transition:transform .5s cubic-bezier(.36,.66,.04,1),-webkit-transform .5s cubic-bezier(.36,.66,.04,1);opacity:1;z-index:2;pointer-events:none;will-change:transform}.item-sliding-active-swipe-end .item-options-end .item-option-expandable{padding-left:90%;-ms-flex-order:1;order:1;-webkit-transition-duration:.6s;transition-duration:.6s;-webkit-transition-property:padding-left;transition-property:padding-left}:host-context([dir=rtl]) .item-sliding-active-swipe-end .item-options-end .item-option-expandable{-ms-flex-order:-1;order:-1}.item-sliding-active-swipe-start .item-options-start .item-option-expandable{padding-right:90%;-ms-flex-order:-1;order:-1;-webkit-transition-duration:.6s;transition-duration:.6s;-webkit-transition-property:padding-right;transition-property:padding-right}:host-context([dir=rtl]) .item-sliding-active-swipe-start .item-options-start .item-option-expandable{-ms-flex-order:1;order:1}"}}export{s as IonItemOption,o as IonItemOptions,r as IonItemSliding};