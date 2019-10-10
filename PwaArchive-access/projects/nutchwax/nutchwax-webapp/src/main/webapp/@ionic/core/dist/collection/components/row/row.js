import { h } from '@stencil/core';
import { getIonMode } from '../../global/ionic-global';
export class Row {
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
    static get is() { return "ion-row"; }
    static get encapsulation() { return "shadow"; }
    static get originalStyleUrls() { return {
        "$": ["row.scss"]
    }; }
    static get styleUrls() { return {
        "$": ["row.css"]
    }; }
}
