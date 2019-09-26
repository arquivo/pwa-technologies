'use strict';

Object.defineProperty(exports, '__esModule', { value: true });

const __chunk_1 = require('./chunk-8de9033d.js');
require('./chunk-d8847c1c.js');
const __chunk_5 = require('./chunk-9c9fbda4.js');

/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 */
class Card {
    constructor(hostRef) {
        __chunk_1.registerInstance(this, hostRef);
        /**
         * If `true`, a button tag will be rendered and the card will be tappable.
         */
        this.button = false;
        /**
         * The type of the button. Only used when an `onclick` or `button` property is present.
         */
        this.type = 'button';
        /**
         * If `true`, the user cannot interact with the card.
         */
        this.disabled = false;
        /**
         * When using a router, it specifies the transition direction when navigating to
         * another page using `href`.
         */
        this.routerDirection = 'forward';
    }
    isClickable() {
        return (this.href !== undefined || this.button);
    }
    hostData() {
        const mode = __chunk_1.getIonMode(this);
        return {
            class: Object.assign({ [mode]: true }, __chunk_5.createColorClasses(this.color), { 'card-disabled': this.disabled, 'ion-activatable': this.isClickable() })
        };
    }
    __stencil_render() {
        const clickable = this.isClickable();
        if (!clickable) {
            return [
                __chunk_1.h("slot", null)
            ];
        }
        const mode = __chunk_1.getIonMode(this);
        const { href, routerDirection } = this;
        const TagType = clickable ? (href === undefined ? 'button' : 'a') : 'div';
        const attrs = (TagType === 'button')
            ? { type: this.type }
            : {
                download: this.download,
                href: this.href,
                rel: this.rel,
                target: this.target
            };
        return (__chunk_1.h(TagType, Object.assign({}, attrs, { class: "card-native", disabled: this.disabled, onClick: (ev) => __chunk_5.openURL(href, ev, routerDirection) }), __chunk_1.h("slot", null), clickable && mode === 'md' && __chunk_1.h("ion-ripple-effect", null)));
    }
    render() { return __chunk_1.h(__chunk_1.Host, this.hostData(), this.__stencil_render()); }
    static get style() { return ".sc-ion-card-md-h{--ion-safe-area-left:0px;--ion-safe-area-right:0px;-moz-osx-font-smoothing:grayscale;-webkit-font-smoothing:antialiased;display:block;position:relative;background:var(--background);color:var(--color);font-family:var(--ion-font-family,inherit);overflow:hidden}.ion-color.sc-ion-card-md-h{background:var(--ion-color-base)}.ion-color.sc-ion-card-md-h, .sc-ion-card-md-h.ion-color.sc-ion-card-md-s  ion-card-header , .sc-ion-card-md-h.ion-color.sc-ion-card-md-s  ion-card-subtitle , .sc-ion-card-md-h.ion-color.sc-ion-card-md-s  ion-card-title {color:var(--ion-color-contrast)}.sc-ion-card-md-s  img {display:block;width:100%}.sc-ion-card-md-s  ion-list {margin-left:0;margin-right:0;margin-top:0;margin-bottom:0}.card-disabled.sc-ion-card-md-h{cursor:default;opacity:.3;pointer-events:none}.card-native.sc-ion-card-md{font-family:inherit;font-size:inherit;font-style:inherit;font-weight:inherit;letter-spacing:inherit;text-decoration:inherit;text-overflow:inherit;text-transform:inherit;text-align:inherit;white-space:inherit;color:inherit;padding-left:0;padding-right:0;padding-top:0;padding-bottom:0;margin-left:0;margin-right:0;margin-top:0;margin-bottom:0;display:block;width:100%;min-height:var(--min-height);-webkit-transition:var(--transition);transition:var(--transition);border-width:var(--border-width);border-style:var(--border-style);border-color:var(--border-color);outline:none;background:var(--background)}.card-native.sc-ion-card-md::-moz-focus-inner{border:0}a.sc-ion-card-md, button.sc-ion-card-md{cursor:pointer;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;-webkit-user-drag:none}ion-ripple-effect.sc-ion-card-md{color:var(--ripple-color)}.sc-ion-card-md-h{--background:var(--ion-item-background,transparent);--color:var(--ion-color-step-550,#737373);margin-left:10px;margin-right:10px;margin-top:10px;margin-bottom:10px;border-radius:4px;font-size:14px;-webkit-box-shadow:0 3px 1px -2px rgba(0,0,0,.2),0 2px 2px 0 rgba(0,0,0,.14),0 1px 5px 0 rgba(0,0,0,.12);box-shadow:0 3px 1px -2px rgba(0,0,0,.2),0 2px 2px 0 rgba(0,0,0,.14),0 1px 5px 0 rgba(0,0,0,.12)}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.sc-ion-card-md-h{margin-left:unset;margin-right:unset;-webkit-margin-start:10px;margin-inline-start:10px;-webkit-margin-end:10px;margin-inline-end:10px}}"; }
}

/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 */
class CardContent {
    constructor(hostRef) {
        __chunk_1.registerInstance(this, hostRef);
    }
    hostData() {
        const mode = __chunk_1.getIonMode(this);
        return {
            class: {
                [mode]: true,
                // Used internally for styling
                [`card-content-${mode}`]: true
            }
        };
    }
    render() { return __chunk_1.h(__chunk_1.Host, this.hostData()); }
    static get style() { return "ion-card-content{display:block;position:relative}.card-content-md{padding-left:16px;padding-right:16px;padding-top:13px;padding-bottom:13px;font-size:14px;line-height:1.5}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.card-content-md{padding-left:unset;padding-right:unset;-webkit-padding-start:16px;padding-inline-start:16px;-webkit-padding-end:16px;padding-inline-end:16px}}.card-content-md h1{margin-left:0;margin-right:0;margin-top:0;margin-bottom:2px;font-size:24px;font-weight:400}.card-content-md h2{margin-left:0;margin-right:0;margin-top:2px;margin-bottom:2px;font-size:16px;font-weight:400}.card-content-md h3,.card-content-md h4,.card-content-md h5,.card-content-md h6{margin-left:0;margin-right:0;margin-top:2px;margin-bottom:2px;font-size:14px;font-weight:400}.card-content-md p{margin-left:0;margin-right:0;margin-top:0;margin-bottom:2px;font-size:14px;font-weight:400;line-height:1.5}ion-card-header+.card-content-md{padding-top:0}"; }
}

/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 */
class CardHeader {
    constructor(hostRef) {
        __chunk_1.registerInstance(this, hostRef);
        /**
         * If `true`, the card header will be translucent.
         */
        this.translucent = false;
    }
    hostData() {
        const mode = __chunk_1.getIonMode(this);
        return {
            class: Object.assign({}, __chunk_5.createColorClasses(this.color), { 'card-header-translucent': this.translucent, [mode]: true })
        };
    }
    __stencil_render() {
        return __chunk_1.h("slot", null);
    }
    render() { return __chunk_1.h(__chunk_1.Host, this.hostData(), this.__stencil_render()); }
    static get style() { return ":host{display:block;position:relative;background:var(--background);color:var(--color)}:host(.ion-color){background:var(--ion-color-base);color:var(--ion-color-contrast)}:host(.ion-color) ::slotted(ion-card-subtitle),:host(.ion-color) ::slotted(ion-card-title){color:currentColor}:host{padding-left:16px;padding-right:16px;padding-top:16px;padding-bottom:16px}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){:host{padding-left:unset;padding-right:unset;-webkit-padding-start:16px;padding-inline-start:16px;-webkit-padding-end:16px;padding-inline-end:16px}}::slotted(ion-card-subtitle:not(:first-child)),::slotted(ion-card-title:not(:first-child)){margin-top:8px}"; }
}

/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 */
class CardSubtitle {
    constructor(hostRef) {
        __chunk_1.registerInstance(this, hostRef);
    }
    hostData() {
        const mode = __chunk_1.getIonMode(this);
        return {
            class: Object.assign({}, __chunk_5.createColorClasses(this.color), { [mode]: true }),
            'role': 'heading',
            'aria-level': '3'
        };
    }
    __stencil_render() {
        return __chunk_1.h("slot", null);
    }
    render() { return __chunk_1.h(__chunk_1.Host, this.hostData(), this.__stencil_render()); }
    static get style() { return ":host{display:block;position:relative;color:var(--color)}:host(.ion-color){color:var(--ion-color-base)}:host{--color:var(--ion-color-step-550,#737373);margin-left:0;margin-right:0;margin-top:0;margin-bottom:0;padding-left:0;padding-right:0;padding-top:0;padding-bottom:0;font-size:14px;font-weight:500}"; }
}

/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 */
class CardTitle {
    constructor(hostRef) {
        __chunk_1.registerInstance(this, hostRef);
    }
    hostData() {
        const mode = __chunk_1.getIonMode(this);
        return {
            class: Object.assign({}, __chunk_5.createColorClasses(this.color), { [mode]: true }),
            'role': 'heading',
            'aria-level': '2'
        };
    }
    __stencil_render() {
        return __chunk_1.h("slot", null);
    }
    render() { return __chunk_1.h(__chunk_1.Host, this.hostData(), this.__stencil_render()); }
    static get style() { return ":host{display:block;position:relative;color:var(--color)}:host(.ion-color){color:var(--ion-color-base)}:host{--color:var(--ion-color-step-850,#262626);margin-left:0;margin-right:0;margin-top:0;margin-bottom:0;padding-left:0;padding-right:0;padding-top:0;padding-bottom:0;font-size:20px;font-weight:500;line-height:1.2}"; }
}

exports.ion_card = Card;
exports.ion_card_content = CardContent;
exports.ion_card_header = CardHeader;
exports.ion_card_subtitle = CardSubtitle;
exports.ion_card_title = CardTitle;
