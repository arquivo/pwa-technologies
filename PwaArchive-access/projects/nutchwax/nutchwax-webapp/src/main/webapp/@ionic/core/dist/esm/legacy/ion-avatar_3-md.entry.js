import { r as registerInstance, d as getIonMode, h, H as Host } from './chunk-f257aad1.js';
import './chunk-1074393c.js';
import { c as createColorClasses } from './chunk-9d21e8e5.js';
var Avatar = /** @class */ (function () {
    function Avatar(hostRef) {
        registerInstance(this, hostRef);
    }
    Avatar.prototype.hostData = function () {
        var _a;
        var mode = getIonMode(this);
        return {
            class: (_a = {},
                _a[mode] = true,
                _a)
        };
    };
    Avatar.prototype.__stencil_render = function () {
        return h("slot", null);
    };
    Avatar.prototype.render = function () { return h(Host, this.hostData(), this.__stencil_render()); };
    Object.defineProperty(Avatar, "style", {
        get: function () { return ":host{border-radius:var(--border-radius);display:block}::slotted(img),::slotted(ion-img){border-radius:var(--border-radius);width:100%;height:100%;-o-object-fit:cover;object-fit:cover;overflow:hidden}:host{--border-radius:50%;width:64px;height:64px}"; },
        enumerable: true,
        configurable: true
    });
    return Avatar;
}());
/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 */
var Badge = /** @class */ (function () {
    function Badge(hostRef) {
        registerInstance(this, hostRef);
    }
    Badge.prototype.hostData = function () {
        var _a;
        var mode = getIonMode(this);
        return {
            class: Object.assign({}, createColorClasses(this.color), (_a = {}, _a[mode] = true, _a))
        };
    };
    Badge.prototype.__stencil_render = function () {
        return h("slot", null);
    };
    Badge.prototype.render = function () { return h(Host, this.hostData(), this.__stencil_render()); };
    Object.defineProperty(Badge, "style", {
        get: function () { return ":host{--background:var(--ion-color-primary,#3880ff);--color:var(--ion-color-primary-contrast,#fff);--padding-end:8px;--padding-bottom:3px;--padding-start:8px;-moz-osx-font-smoothing:grayscale;-webkit-font-smoothing:antialiased;padding-left:var(--padding-start);padding-right:var(--padding-end);padding-top:var(--padding-top);padding-bottom:var(--padding-bottom);display:inline-block;min-width:10px;background:var(--background);color:var(--color);font-family:var(--ion-font-family,inherit);font-size:13px;font-weight:700;line-height:1;text-align:center;white-space:nowrap;contain:content;vertical-align:baseline}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){:host{padding-left:unset;padding-right:unset;-webkit-padding-start:var(--padding-start);padding-inline-start:var(--padding-start);-webkit-padding-end:var(--padding-end);padding-inline-end:var(--padding-end)}}:host(.ion-color){background:var(--ion-color-base);color:var(--ion-color-contrast)}:host(:empty){display:none}:host{--padding-top:3px;--padding-end:4px;--padding-bottom:4px;--padding-start:4px;border-radius:4px}"; },
        enumerable: true,
        configurable: true
    });
    return Badge;
}());
var Thumbnail = /** @class */ (function () {
    function Thumbnail(hostRef) {
        registerInstance(this, hostRef);
    }
    Thumbnail.prototype.hostData = function () {
        var _a;
        var mode = getIonMode(this);
        return {
            class: (_a = {},
                _a[mode] = true,
                _a)
        };
    };
    Thumbnail.prototype.__stencil_render = function () {
        return h("slot", null);
    };
    Thumbnail.prototype.render = function () { return h(Host, this.hostData(), this.__stencil_render()); };
    Object.defineProperty(Thumbnail, "style", {
        get: function () { return ":host{--size:48px;--border-radius:0;border-radius:var(--border-radius);display:block;width:var(--size);height:var(--size)}::slotted(img),::slotted(ion-img){border-radius:var(--border-radius);width:100%;height:100%;-o-object-fit:cover;object-fit:cover;overflow:hidden}"; },
        enumerable: true,
        configurable: true
    });
    return Thumbnail;
}());
export { Avatar as ion_avatar, Badge as ion_badge, Thumbnail as ion_thumbnail };
