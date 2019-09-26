import { h } from '@stencil/core';
import { getIonMode } from '../../global/ionic-global';
export class Reorder {
    onClick(ev) {
        ev.preventDefault();
        ev.stopImmediatePropagation();
    }
    hostData() {
        const mode = getIonMode(this);
        return {
            class: {
                [mode]: true,
            }
        };
    }
    render() {
        return (h("slot", null,
            h("ion-icon", { name: "reorder", lazy: false, class: "reorder-icon" })));
    }
    static get is() { return "ion-reorder"; }
    static get encapsulation() { return "shadow"; }
    static get originalStyleUrls() { return {
        "ios": ["reorder.ios.scss"],
        "md": ["reorder.md.scss"]
    }; }
    static get styleUrls() { return {
        "ios": ["reorder.ios.css"],
        "md": ["reorder.md.css"]
    }; }
    static get listeners() { return [{
            "name": "click",
            "method": "onClick",
            "target": undefined,
            "capture": true,
            "passive": false
        }]; }
}
