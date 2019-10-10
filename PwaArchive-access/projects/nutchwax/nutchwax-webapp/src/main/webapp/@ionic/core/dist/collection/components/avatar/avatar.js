import { h } from '@stencil/core';
import { getIonMode } from '../../global/ionic-global';
export class Avatar {
    hostData() {
        const mode = getIonMode(this);
        return {
            class: {
                [mode]: true,
            }
        };
    }
    render() {
        return h("slot", null);
    }
    static get is() { return "ion-avatar"; }
    static get encapsulation() { return "shadow"; }
    static get originalStyleUrls() { return {
        "ios": ["avatar.ios.scss"],
        "md": ["avatar.md.scss"]
    }; }
    static get styleUrls() { return {
        "ios": ["avatar.ios.css"],
        "md": ["avatar.md.css"]
    }; }
}
