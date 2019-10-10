import { getIonMode } from '../../global/ionic-global';
/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 */
export class Header {
    constructor() {
        /**
         * If `true`, the header will be translucent. Only applies to `ios` mode.
         * Note: In order to scroll content behind the header, the `fullscreen`
         * attribute needs to be set on the content.
         */
        this.translucent = false;
    }
    hostData() {
        const mode = getIonMode(this);
        return {
            class: {
                [mode]: true,
                // Used internally for styling
                [`header-${mode}`]: true,
                [`header-translucent`]: this.translucent,
                [`header-translucent-${mode}`]: this.translucent,
            }
        };
    }
    static get is() { return "ion-header"; }
    static get originalStyleUrls() { return {
        "ios": ["header.ios.scss"],
        "md": ["header.md.scss"]
    }; }
    static get styleUrls() { return {
        "ios": ["header.ios.css"],
        "md": ["header.md.css"]
    }; }
    static get properties() { return {
        "translucent": {
            "type": "boolean",
            "mutable": false,
            "complexType": {
                "original": "boolean",
                "resolved": "boolean",
                "references": {}
            },
            "required": false,
            "optional": false,
            "docs": {
                "tags": [],
                "text": "If `true`, the header will be translucent. Only applies to `ios` mode.\nNote: In order to scroll content behind the header, the `fullscreen`\nattribute needs to be set on the content."
            },
            "attribute": "translucent",
            "reflect": false,
            "defaultValue": "false"
        }
    }; }
}
