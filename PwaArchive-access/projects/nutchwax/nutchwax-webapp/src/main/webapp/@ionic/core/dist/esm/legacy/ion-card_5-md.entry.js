import { r as registerInstance, d as getIonMode, h, H as Host } from './chunk-f257aad1.js';
import './chunk-1074393c.js';
import { c as createColorClasses, o as openURL } from './chunk-9d21e8e5.js';
/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 */
var Card = /** @class */ (function () {
    function Card(hostRef) {
        registerInstance(this, hostRef);
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
    Card.prototype.isClickable = function () {
        return (this.href !== undefined || this.button);
    };
    Card.prototype.hostData = function () {
        var _a;
        var mode = getIonMode(this);
        return {
            class: Object.assign((_a = {}, _a[mode] = true, _a), createColorClasses(this.color), { 'card-disabled': this.disabled, 'ion-activatable': this.isClickable() })
        };
    };
    Card.prototype.__stencil_render = function () {
        var clickable = this.isClickable();
        if (!clickable) {
            return [
                h("slot", null)
            ];
        }
        var mode = getIonMode(this);
        var _a = this, href = _a.href, routerDirection = _a.routerDirection;
        var TagType = clickable ? (href === undefined ? 'button' : 'a') : 'div';
        var attrs = (TagType === 'button')
            ? { type: this.type }
            : {
                download: this.download,
                href: this.href,
                rel: this.rel,
                target: this.target
            };
        return (h(TagType, Object.assign({}, attrs, { class: "card-native", disabled: this.disabled, onClick: function (ev) { return openURL(href, ev, routerDirection); } }), h("slot", null), clickable && mode === 'md' && h("ion-ripple-effect", null)));
    };
    Card.prototype.render = function () { return h(Host, this.hostData(), this.__stencil_render()); };
    Object.defineProperty(Card, "style", {
        get: function () { return ".sc-ion-card-md-h{--ion-safe-area-left:0px;--ion-safe-area-right:0px;-moz-osx-font-smoothing:grayscale;-webkit-font-smoothing:antialiased;display:block;position:relative;background:var(--background);color:var(--color);font-family:var(--ion-font-family,inherit);overflow:hidden}.ion-color.sc-ion-card-md-h{background:var(--ion-color-base)}.ion-color.sc-ion-card-md-h, .sc-ion-card-md-h.ion-color.sc-ion-card-md-s  ion-card-header , .sc-ion-card-md-h.ion-color.sc-ion-card-md-s  ion-card-subtitle , .sc-ion-card-md-h.ion-color.sc-ion-card-md-s  ion-card-title {color:var(--ion-color-contrast)}.sc-ion-card-md-s  img {display:block;width:100%}.sc-ion-card-md-s  ion-list {margin-left:0;margin-right:0;margin-top:0;margin-bottom:0}.card-disabled.sc-ion-card-md-h{cursor:default;opacity:.3;pointer-events:none}.card-native.sc-ion-card-md{font-family:inherit;font-size:inherit;font-style:inherit;font-weight:inherit;letter-spacing:inherit;text-decoration:inherit;text-overflow:inherit;text-transform:inherit;text-align:inherit;white-space:inherit;color:inherit;padding-left:0;padding-right:0;padding-top:0;padding-bottom:0;margin-left:0;margin-right:0;margin-top:0;margin-bottom:0;display:block;width:100%;min-height:var(--min-height);-webkit-transition:var(--transition);transition:var(--transition);border-width:var(--border-width);border-style:var(--border-style);border-color:var(--border-color);outline:none;background:var(--background)}.card-native.sc-ion-card-md::-moz-focus-inner{border:0}a.sc-ion-card-md, button.sc-ion-card-md{cursor:pointer;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;-webkit-user-drag:none}ion-ripple-effect.sc-ion-card-md{color:var(--ripple-color)}.sc-ion-card-md-h{--background:var(--ion-item-background,transparent);--color:var(--ion-color-step-550,#737373);margin-left:10px;margin-right:10px;margin-top:10px;margin-bottom:10px;border-radius:4px;font-size:14px;-webkit-box-shadow:0 3px 1px -2px rgba(0,0,0,.2),0 2px 2px 0 rgba(0,0,0,.14),0 1px 5px 0 rgba(0,0,0,.12);box-shadow:0 3px 1px -2px rgba(0,0,0,.2),0 2px 2px 0 rgba(0,0,0,.14),0 1px 5px 0 rgba(0,0,0,.12)}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.sc-ion-card-md-h{margin-left:unset;margin-right:unset;-webkit-margin-start:10px;margin-inline-start:10px;-webkit-margin-end:10px;margin-inline-end:10px}}"; },
        enumerable: true,
        configurable: true
    });
    return Card;
}());
/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 */
var CardContent = /** @class */ (function () {
    function CardContent(hostRef) {
        registerInstance(this, hostRef);
    }
    CardContent.prototype.hostData = function () {
        var _a;
        var mode = getIonMode(this);
        return {
            class: (_a = {},
                _a[mode] = true,
                // Used internally for styling
                _a["card-content-" + mode] = true,
                _a)
        };
    };
    CardContent.prototype.render = function () { return h(Host, this.hostData()); };
    Object.defineProperty(CardContent, "style", {
        get: function () { return "ion-card-content{display:block;position:relative}.card-content-md{padding-left:16px;padding-right:16px;padding-top:13px;padding-bottom:13px;font-size:14px;line-height:1.5}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.card-content-md{padding-left:unset;padding-right:unset;-webkit-padding-start:16px;padding-inline-start:16px;-webkit-padding-end:16px;padding-inline-end:16px}}.card-content-md h1{margin-left:0;margin-right:0;margin-top:0;margin-bottom:2px;font-size:24px;font-weight:400}.card-content-md h2{margin-left:0;margin-right:0;margin-top:2px;margin-bottom:2px;font-size:16px;font-weight:400}.card-content-md h3,.card-content-md h4,.card-content-md h5,.card-content-md h6{margin-left:0;margin-right:0;margin-top:2px;margin-bottom:2px;font-size:14px;font-weight:400}.card-content-md p{margin-left:0;margin-right:0;margin-top:0;margin-bottom:2px;font-size:14px;font-weight:400;line-height:1.5}ion-card-header+.card-content-md{padding-top:0}"; },
        enumerable: true,
        configurable: true
    });
    return CardContent;
}());
/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 */
var CardHeader = /** @class */ (function () {
    function CardHeader(hostRef) {
        registerInstance(this, hostRef);
        /**
         * If `true`, the card header will be translucent.
         */
        this.translucent = false;
    }
    CardHeader.prototype.hostData = function () {
        var _a;
        var mode = getIonMode(this);
        return {
            class: Object.assign({}, createColorClasses(this.color), (_a = { 'card-header-translucent': this.translucent }, _a[mode] = true, _a))
        };
    };
    CardHeader.prototype.__stencil_render = function () {
        return h("slot", null);
    };
    CardHeader.prototype.render = function () { return h(Host, this.hostData(), this.__stencil_render()); };
    Object.defineProperty(CardHeader, "style", {
        get: function () { return ":host{display:block;position:relative;background:var(--background);color:var(--color)}:host(.ion-color){background:var(--ion-color-base);color:var(--ion-color-contrast)}:host(.ion-color) ::slotted(ion-card-subtitle),:host(.ion-color) ::slotted(ion-card-title){color:currentColor}:host{padding-left:16px;padding-right:16px;padding-top:16px;padding-bottom:16px}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){:host{padding-left:unset;padding-right:unset;-webkit-padding-start:16px;padding-inline-start:16px;-webkit-padding-end:16px;padding-inline-end:16px}}::slotted(ion-card-subtitle:not(:first-child)),::slotted(ion-card-title:not(:first-child)){margin-top:8px}"; },
        enumerable: true,
        configurable: true
    });
    return CardHeader;
}());
/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 */
var CardSubtitle = /** @class */ (function () {
    function CardSubtitle(hostRef) {
        registerInstance(this, hostRef);
    }
    CardSubtitle.prototype.hostData = function () {
        var _a;
        var mode = getIonMode(this);
        return {
            class: Object.assign({}, createColorClasses(this.color), (_a = {}, _a[mode] = true, _a)),
            'role': 'heading',
            'aria-level': '3'
        };
    };
    CardSubtitle.prototype.__stencil_render = function () {
        return h("slot", null);
    };
    CardSubtitle.prototype.render = function () { return h(Host, this.hostData(), this.__stencil_render()); };
    Object.defineProperty(CardSubtitle, "style", {
        get: function () { return ":host{display:block;position:relative;color:var(--color)}:host(.ion-color){color:var(--ion-color-base)}:host{--color:var(--ion-color-step-550,#737373);margin-left:0;margin-right:0;margin-top:0;margin-bottom:0;padding-left:0;padding-right:0;padding-top:0;padding-bottom:0;font-size:14px;font-weight:500}"; },
        enumerable: true,
        configurable: true
    });
    return CardSubtitle;
}());
/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 */
var CardTitle = /** @class */ (function () {
    function CardTitle(hostRef) {
        registerInstance(this, hostRef);
    }
    CardTitle.prototype.hostData = function () {
        var _a;
        var mode = getIonMode(this);
        return {
            class: Object.assign({}, createColorClasses(this.color), (_a = {}, _a[mode] = true, _a)),
            'role': 'heading',
            'aria-level': '2'
        };
    };
    CardTitle.prototype.__stencil_render = function () {
        return h("slot", null);
    };
    CardTitle.prototype.render = function () { return h(Host, this.hostData(), this.__stencil_render()); };
    Object.defineProperty(CardTitle, "style", {
        get: function () { return ":host{display:block;position:relative;color:var(--color)}:host(.ion-color){color:var(--ion-color-base)}:host{--color:var(--ion-color-step-850,#262626);margin-left:0;margin-right:0;margin-top:0;margin-bottom:0;padding-left:0;padding-right:0;padding-top:0;padding-bottom:0;font-size:20px;font-weight:500;line-height:1.2}"; },
        enumerable: true,
        configurable: true
    });
    return CardTitle;
}());
export { Card as ion_card, CardContent as ion_card_content, CardHeader as ion_card_header, CardSubtitle as ion_card_subtitle, CardTitle as ion_card_title };
