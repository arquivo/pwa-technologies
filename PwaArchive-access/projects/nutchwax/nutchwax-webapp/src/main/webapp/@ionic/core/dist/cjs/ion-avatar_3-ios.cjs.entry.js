'use strict';

Object.defineProperty(exports, '__esModule', { value: true });

const __chunk_1 = require('./chunk-8de9033d.js');
require('./chunk-d8847c1c.js');
const __chunk_5 = require('./chunk-9c9fbda4.js');

class Avatar {
    constructor(hostRef) {
        __chunk_1.registerInstance(this, hostRef);
    }
    hostData() {
        const mode = __chunk_1.getIonMode(this);
        return {
            class: {
                [mode]: true,
            }
        };
    }
    __stencil_render() {
        return __chunk_1.h("slot", null);
    }
    render() { return __chunk_1.h(__chunk_1.Host, this.hostData(), this.__stencil_render()); }
    static get style() { return ":host{border-radius:var(--border-radius);display:block}::slotted(img),::slotted(ion-img){border-radius:var(--border-radius);width:100%;height:100%;-o-object-fit:cover;object-fit:cover;overflow:hidden}:host{--border-radius:50%;width:48px;height:48px}"; }
}

/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 */
class Badge {
    constructor(hostRef) {
        __chunk_1.registerInstance(this, hostRef);
    }
    hostData() {
        const mode = __chunk_1.getIonMode(this);
        return {
            class: Object.assign({}, __chunk_5.createColorClasses(this.color), { [mode]: true })
        };
    }
    __stencil_render() {
        return __chunk_1.h("slot", null);
    }
    render() { return __chunk_1.h(__chunk_1.Host, this.hostData(), this.__stencil_render()); }
    static get style() { return ":host{--background:var(--ion-color-primary,#3880ff);--color:var(--ion-color-primary-contrast,#fff);--padding-top:3px;--padding-end:8px;--padding-bottom:3px;--padding-start:8px;-moz-osx-font-smoothing:grayscale;-webkit-font-smoothing:antialiased;padding-left:var(--padding-start);padding-right:var(--padding-end);padding-top:var(--padding-top);padding-bottom:var(--padding-bottom);display:inline-block;min-width:10px;background:var(--background);color:var(--color);font-family:var(--ion-font-family,inherit);font-size:13px;font-weight:700;line-height:1;text-align:center;white-space:nowrap;contain:content;vertical-align:baseline}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){:host{padding-left:unset;padding-right:unset;-webkit-padding-start:var(--padding-start);padding-inline-start:var(--padding-start);-webkit-padding-end:var(--padding-end);padding-inline-end:var(--padding-end)}}:host(.ion-color){background:var(--ion-color-base);color:var(--ion-color-contrast)}:host(:empty){display:none}:host{border-radius:10px}"; }
}

class Thumbnail {
    constructor(hostRef) {
        __chunk_1.registerInstance(this, hostRef);
    }
    hostData() {
        const mode = __chunk_1.getIonMode(this);
        return {
            class: {
                [mode]: true,
            }
        };
    }
    __stencil_render() {
        return __chunk_1.h("slot", null);
    }
    render() { return __chunk_1.h(__chunk_1.Host, this.hostData(), this.__stencil_render()); }
    static get style() { return ":host{--size:48px;--border-radius:0;border-radius:var(--border-radius);display:block;width:var(--size);height:var(--size)}::slotted(img),::slotted(ion-img){border-radius:var(--border-radius);width:100%;height:100%;-o-object-fit:cover;object-fit:cover;overflow:hidden}"; }
}

exports.ion_avatar = Avatar;
exports.ion_badge = Badge;
exports.ion_thumbnail = Thumbnail;
