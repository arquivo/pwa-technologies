import { h } from '../ionic.core.js';

import { d as hostContext, c as createColorClasses, b as openURL } from './chunk-7c632336.js';

class Fab {
    constructor() {
        this.edge = false;
        this.activated = false;
    }
    activatedChanged() {
        const activated = this.activated;
        const fab = this.el.querySelector('ion-fab-button');
        if (fab) {
            fab.activated = activated;
        }
        Array.from(this.el.querySelectorAll('ion-fab-list')).forEach(list => {
            list.activated = activated;
        });
    }
    componentDidLoad() {
        if (this.activated) {
            this.activatedChanged();
        }
    }
    onClick() {
        const hasList = !!this.el.querySelector('ion-fab-list');
        if (hasList) {
            this.activated = !this.activated;
        }
    }
    close() {
        this.activated = false;
    }
    hostData() {
        return {
            class: {
                [`fab-horizontal-${this.horizontal}`]: this.horizontal !== undefined,
                [`fab-vertical-${this.vertical}`]: this.vertical !== undefined,
                'fab-edge': this.edge
            }
        };
    }
    render() {
        return h("slot", null);
    }
    static get is() { return "ion-fab"; }
    static get encapsulation() { return "shadow"; }
    static get properties() { return {
        "activated": {
            "type": Boolean,
            "attr": "activated",
            "mutable": true,
            "watchCallbacks": ["activatedChanged"]
        },
        "close": {
            "method": true
        },
        "edge": {
            "type": Boolean,
            "attr": "edge"
        },
        "el": {
            "elementRef": true
        },
        "horizontal": {
            "type": String,
            "attr": "horizontal"
        },
        "vertical": {
            "type": String,
            "attr": "vertical"
        }
    }; }
    static get listeners() { return [{
            "name": "click",
            "method": "onClick"
        }]; }
    static get style() { return ".sc-ion-fab-h{position:absolute;z-index:999}.fab-horizontal-center.sc-ion-fab-h{left:50%;margin-left:-28px}[dir=rtl].fab-horizontal-center.sc-ion-fab-h{right:50%}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.fab-horizontal-center.sc-ion-fab-h{margin-left:unset;-webkit-margin-start:-28px;margin-inline-start:-28px}}.fab-horizontal-start.sc-ion-fab-h{left:calc(10px + var(--ion-safe-area-left, 0px))}[dir=rtl].fab-horizontal-start.sc-ion-fab-h{right:calc(10px + var(--ion-safe-area-left, 0px))}.fab-horizontal-end.sc-ion-fab-h{right:calc(10px + var(--ion-safe-area-right, 0px))}[dir=rtl].fab-horizontal-end.sc-ion-fab-h{left:calc(10px + var(--ion-safe-area-right, 0px))}.fab-vertical-top.sc-ion-fab-h{top:10px}.fab-vertical-top.fab-edge.sc-ion-fab-h{top:-28px}.fab-vertical-bottom.sc-ion-fab-h{bottom:10px}.fab-vertical-bottom.fab-edge.sc-ion-fab-h{bottom:-28px}.fab-vertical-center.sc-ion-fab-h{margin-top:-28px;top:50%}"; }
}

class FabButton {
    constructor() {
        this.activated = false;
        this.disabled = false;
        this.routerDirection = 'forward';
        this.show = false;
        this.translucent = false;
        this.type = 'button';
        this.onFocus = () => {
            this.ionFocus.emit();
        };
        this.onBlur = () => {
            this.ionBlur.emit();
        };
    }
    hostData() {
        const { el, disabled, color, activated, show, translucent, size } = this;
        const inList = hostContext('ion-fab-list', el);
        return {
            'aria-disabled': disabled ? 'true' : null,
            class: Object.assign({}, createColorClasses(color), { 'fab-button-in-list': inList, 'fab-button-translucent-in-list': inList && translucent, 'fab-button-close-active': activated, 'fab-button-show': show, 'fab-button-disabled': disabled, 'fab-button-translucent': translucent, 'ion-activatable': true, 'ion-focusable': true, [`fab-button-${size}`]: size !== undefined })
        };
    }
    render() {
        const TagType = this.href === undefined ? 'button' : 'a';
        const attrs = (TagType === 'button')
            ? { type: this.type }
            : { href: this.href };
        return (h(TagType, Object.assign({}, attrs, { class: "button-native", disabled: this.disabled, onFocus: this.onFocus, onBlur: this.onBlur, onClick: (ev) => openURL(this.win, this.href, ev, this.routerDirection) }),
            h("span", { class: "close-icon" },
                h("ion-icon", { name: "close", lazy: false })),
            h("span", { class: "button-inner" },
                h("slot", null)),
            this.mode === 'md' && h("ion-ripple-effect", null)));
    }
    static get is() { return "ion-fab-button"; }
    static get encapsulation() { return "shadow"; }
    static get properties() { return {
        "activated": {
            "type": Boolean,
            "attr": "activated"
        },
        "color": {
            "type": String,
            "attr": "color"
        },
        "disabled": {
            "type": Boolean,
            "attr": "disabled"
        },
        "el": {
            "elementRef": true
        },
        "href": {
            "type": String,
            "attr": "href"
        },
        "mode": {
            "type": String,
            "attr": "mode"
        },
        "routerDirection": {
            "type": String,
            "attr": "router-direction"
        },
        "show": {
            "type": Boolean,
            "attr": "show"
        },
        "size": {
            "type": String,
            "attr": "size"
        },
        "translucent": {
            "type": Boolean,
            "attr": "translucent"
        },
        "type": {
            "type": String,
            "attr": "type"
        },
        "win": {
            "context": "window"
        }
    }; }
    static get events() { return [{
            "name": "ionFocus",
            "method": "ionFocus",
            "bubbles": true,
            "cancelable": true,
            "composed": true
        }, {
            "name": "ionBlur",
            "method": "ionBlur",
            "bubbles": true,
            "cancelable": true,
            "composed": true
        }]; }
    static get style() { return ".sc-ion-fab-button-ios-h{--transition:background-color,opacity 100ms linear;--ripple-color:currentColor;--border-radius:50%;--border-width:0;--border-style:none;--border-color:initial;--padding-top:0;--padding-end:0;--padding-bottom:0;--padding-start:0;margin-left:0;margin-right:0;margin-top:0;margin-bottom:0;display:block;width:56px;height:56px;font-size:14px;text-align:center;text-overflow:ellipsis;text-transform:none;white-space:nowrap;-webkit-font-kerning:none;font-kerning:none}.ion-color.sc-ion-fab-button-ios-h   .button-native.sc-ion-fab-button-ios{background:var(--ion-color-base);color:var(--ion-color-contrast)}.ion-color.activated.sc-ion-fab-button-ios-h   .button-native.sc-ion-fab-button-ios, .ion-color.ion-focused.sc-ion-fab-button-ios-h   .button-native.sc-ion-fab-button-ios{background:var(--ion-color-shade);color:var(--ion-color-contrast)}.button-native.sc-ion-fab-button-ios{border-radius:var(--border-radius);padding-left:var(--padding-start);padding-right:var(--padding-end);padding-top:var(--padding-top);padding-bottom:var(--padding-bottom);font-family:inherit;font-size:inherit;font-style:inherit;font-weight:inherit;letter-spacing:inherit;text-decoration:inherit;text-overflow:inherit;text-transform:inherit;text-align:inherit;white-space:inherit;color:inherit;display:block;position:relative;width:100%;height:100%;-webkit-transform:var(--transform);transform:var(--transform);-webkit-transition:var(--transition);transition:var(--transition);border-width:var(--border-width);border-style:var(--border-style);border-color:var(--border-color);outline:none;background:var(--background);background-clip:padding-box;color:var(--color);-webkit-box-shadow:var(--box-shadow);box-shadow:var(--box-shadow);contain:strict;cursor:pointer;overflow:hidden;z-index:0;-webkit-appearance:none;-moz-appearance:none;appearance:none;-webkit-box-sizing:border-box;box-sizing:border-box}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.button-native.sc-ion-fab-button-ios{padding-left:unset;padding-right:unset;-webkit-padding-start:var(--padding-start);padding-inline-start:var(--padding-start);-webkit-padding-end:var(--padding-end);padding-inline-end:var(--padding-end)}}.button-inner.sc-ion-fab-button-ios{left:0;right:0;top:0;display:-ms-flexbox;display:flex;position:absolute;-ms-flex-flow:row nowrap;flex-flow:row nowrap;-ms-flex-negative:0;flex-shrink:0;-ms-flex-align:center;align-items:center;-ms-flex-pack:center;justify-content:center;height:100%;-webkit-transition:all .3s ease-in-out;transition:all .3s ease-in-out;-webkit-transition-property:opacity,-webkit-transform;transition-property:opacity,-webkit-transform;transition-property:transform,opacity;transition-property:transform,opacity,-webkit-transform}.activated.sc-ion-fab-button-ios-h   .button-native.sc-ion-fab-button-ios{background:var(--background-activated);color:var(--color-activated)}.ion-focused.sc-ion-fab-button-ios-h   .button-native.sc-ion-fab-button-ios{background:var(--background-focused);color:var(--color-focused)}.fab-button-disabled.sc-ion-fab-button-ios-h{pointer-events:none}.fab-button-disabled.sc-ion-fab-button-ios-h   .button-native.sc-ion-fab-button-ios{cursor:default;opacity:.5;pointer-events:none}.sc-ion-fab-button-ios-s > ion-icon{line-height:1}.fab-button-small.sc-ion-fab-button-ios-h{margin-left:8px;margin-right:8px;margin-top:8px;margin-bottom:8px;width:40px;height:40px}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.fab-button-small.sc-ion-fab-button-ios-h{margin-left:unset;margin-right:unset;-webkit-margin-start:8px;margin-inline-start:8px;-webkit-margin-end:8px;margin-inline-end:8px}}.close-icon.sc-ion-fab-button-ios{left:0;right:0;top:0;display:-ms-flexbox;display:flex;position:absolute;-ms-flex-align:center;align-items:center;-ms-flex-pack:center;justify-content:center;height:100%;-webkit-transform:scale(.4) rotate(-45deg);transform:scale(.4) rotate(-45deg);-webkit-transition:all .3s ease-in-out;transition:all .3s ease-in-out;-webkit-transition-property:opacity,-webkit-transform;transition-property:opacity,-webkit-transform;transition-property:transform,opacity;transition-property:transform,opacity,-webkit-transform;opacity:0}.fab-button-close-active.sc-ion-fab-button-ios-h   .close-icon.sc-ion-fab-button-ios{-webkit-transform:scale(1) rotate(0deg);transform:scale(1) rotate(0deg);opacity:1}.fab-button-close-active.sc-ion-fab-button-ios-h   .button-inner.sc-ion-fab-button-ios{-webkit-transform:scale(.4) rotate(45deg);transform:scale(.4) rotate(45deg);opacity:0}ion-ripple-effect.sc-ion-fab-button-ios{color:var(--ripple-color)}.sc-ion-fab-button-ios-h{--background:var(--ion-color-primary,#3880ff);--background-activated:var(--ion-color-primary-shade,#3171e0);--background-focused:var(--background-activated);--color:var(--ion-color-primary-contrast,#fff);--color-activated:var(--ion-color-primary-contrast,#fff);--color-focused:var(--color-activated);--transition:0.2s transform cubic-bezier(0.25,1.11,0.78,1.59)}.sc-ion-fab-button-ios-h, .activated.sc-ion-fab-button-ios-h{--box-shadow:0 4px 16px rgba(0,0,0,0.12)}.activated.sc-ion-fab-button-ios-h{--transform:scale(1.1);--transition:0.2s transform ease-out}.close-icon.sc-ion-fab-button-ios, .sc-ion-fab-button-ios-s > ion-icon{font-size:28px}.fab-button-in-list.sc-ion-fab-button-ios-h{--background:var(--ion-color-light,#f4f5f8);--background-activated:var(--ion-color-light-shade,#d7d8da);--background-focused:var(--background-activated);--color:var(--ion-color-light-contrast,#000);--color-activated:var(--ion-color-light-contrast,#000);--color-focused:var(--color-activated);--transition:transform 200ms ease 10ms,opacity 200ms ease 10ms}.sc-ion-fab-button-ios-h.fab-button-in-list .sc-ion-fab-button-ios-s > ion-icon{font-size:18px}.fab-button-translucent.sc-ion-fab-button-ios-h{--background:rgba(var(--ion-color-primary-rgb,56,128,255),0.9);--backdrop-filter:saturate(180%) blur(20px)}.fab-button-translucent-in-list.sc-ion-fab-button-ios-h{--background:rgba(var(--ion-background-color-rgb,255,255,255),0.8)}.ion-color.fab-button-translucent.sc-ion-fab-button-ios-h   .button-native.sc-ion-fab-button-ios{background:rgba(var(--ion-color-base-rgb),.9)}.ion-color.activated.fab-button-translucent.sc-ion-fab-button-ios-h   .button-native.sc-ion-fab-button-ios, .ion-color.ion-focused.fab-button-translucent.sc-ion-fab-button-ios-h   .button-native.sc-ion-fab-button-ios{background:var(--ion-color-base)}"; }
    static get styleMode() { return "ios"; }
}

class FabList {
    constructor() {
        this.activated = false;
        this.side = 'bottom';
    }
    activatedChanged(activated) {
        const fabs = Array.from(this.el.querySelectorAll('ion-fab-button'));
        const timeout = activated ? 30 : 0;
        fabs.forEach((fab, i) => {
            setTimeout(() => fab.show = activated, i * timeout);
        });
    }
    hostData() {
        return {
            class: {
                'fab-list-active': this.activated,
                [`fab-list-side-${this.side}`]: true
            }
        };
    }
    render() {
        return h("slot", null);
    }
    static get is() { return "ion-fab-list"; }
    static get encapsulation() { return "shadow"; }
    static get properties() { return {
        "activated": {
            "type": Boolean,
            "attr": "activated",
            "watchCallbacks": ["activatedChanged"]
        },
        "el": {
            "elementRef": true
        },
        "side": {
            "type": String,
            "attr": "side"
        }
    }; }
    static get style() { return ".sc-ion-fab-list-h{margin-left:0;margin-right:0;margin-top:66px;margin-bottom:66px;display:none;position:absolute;top:0;-ms-flex-direction:column;flex-direction:column;-ms-flex-align:center;align-items:center;min-width:56px;min-height:56px}.fab-list-active.sc-ion-fab-list-h{display:-ms-flexbox;display:flex}.sc-ion-fab-list-s > .fab-button-in-list{margin-left:0;margin-right:0;margin-top:8px;margin-bottom:8px;width:40px;height:40px;-webkit-transform:scale(0);transform:scale(0);opacity:0;visibility:hidden}.sc-ion-fab-list-h.fab-list-side-bottom .sc-ion-fab-list-s > .fab-button-in-list, .sc-ion-fab-list-h.fab-list-side-top .sc-ion-fab-list-s > .fab-button-in-list{margin-left:0;margin-right:0;margin-top:5px;margin-bottom:5px}.sc-ion-fab-list-h.fab-list-side-end .sc-ion-fab-list-s > .fab-button-in-list, .sc-ion-fab-list-h.fab-list-side-start .sc-ion-fab-list-s > .fab-button-in-list{margin-left:5px;margin-right:5px;margin-top:0;margin-bottom:0}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.sc-ion-fab-list-h.fab-list-side-end .sc-ion-fab-list-s > .fab-button-in-list, .sc-ion-fab-list-h.fab-list-side-start .sc-ion-fab-list-s > .fab-button-in-list{margin-left:unset;margin-right:unset;-webkit-margin-start:5px;margin-inline-start:5px;-webkit-margin-end:5px;margin-inline-end:5px}}.sc-ion-fab-list-s > .fab-button-in-list.fab-button-show{-webkit-transform:scale(1);transform:scale(1);opacity:1;visibility:visible}.fab-list-side-top.sc-ion-fab-list-h{top:auto;bottom:0;-ms-flex-direction:column-reverse;flex-direction:column-reverse}.fab-list-side-start.sc-ion-fab-list-h{margin-left:66px;margin-right:66px;margin-top:0;margin-bottom:0;right:0;-ms-flex-direction:row-reverse;flex-direction:row-reverse}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.fab-list-side-start.sc-ion-fab-list-h{margin-left:unset;margin-right:unset;-webkit-margin-start:66px;margin-inline-start:66px;-webkit-margin-end:66px;margin-inline-end:66px}}[dir=rtl].fab-list-side-start.sc-ion-fab-list-h{left:0}.fab-list-side-end.sc-ion-fab-list-h{margin-left:66px;margin-right:66px;margin-top:0;margin-bottom:0;left:0;-ms-flex-direction:row;flex-direction:row}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.fab-list-side-end.sc-ion-fab-list-h{margin-left:unset;margin-right:unset;-webkit-margin-start:66px;margin-inline-start:66px;-webkit-margin-end:66px;margin-inline-end:66px}}[dir=rtl].fab-list-side-end.sc-ion-fab-list-h{right:0}"; }
}

export { Fab as IonFab, FabButton as IonFabButton, FabList as IonFabList };
