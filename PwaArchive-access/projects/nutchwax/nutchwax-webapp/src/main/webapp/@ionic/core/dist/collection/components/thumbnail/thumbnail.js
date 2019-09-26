import { h } from '@stencil/core';
import { getIonMode } from '../../global/ionic-global';
export class Thumbnail {
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
    static get is() { return "ion-thumbnail"; }
    static get encapsulation() { return "shadow"; }
    static get originalStyleUrls() { return {
        "$": ["thumbnail.scss"]
    }; }
    static get styleUrls() { return {
        "$": ["thumbnail.css"]
    }; }
}
