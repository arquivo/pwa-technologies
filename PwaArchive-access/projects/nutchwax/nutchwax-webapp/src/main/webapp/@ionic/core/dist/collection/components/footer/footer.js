import { getIonMode } from '../../global/ionic-global';
/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 */
export class Footer {
    constructor() {
        /**
         * If `true`, the footer will be translucent. Only applies to `ios` mode.
         * Note: In order to scroll content behind the footer, the `fullscreen`
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
                [`footer-${mode}`]: true,
                [`footer-translucent`]: this.translucent,
                [`footer-translucent-${mode}`]: this.translucent,
            }
        };
    }
    static get is() { return "ion-footer"; }
    static get originalStyleUrls() { return {
        "ios": ["footer.ios.scss"],
        "md": ["footer.md.scss"]
    }; }
    static get styleUrls() { return {
        "ios": ["footer.ios.css"],
        "md": ["footer.md.css"]
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
                "text": "If `true`, the footer will be translucent. Only applies to `ios` mode.\nNote: In order to scroll content behind the footer, the `fullscreen`\nattribute needs to be set on the content."
            },
            "attribute": "translucent",
            "reflect": false,
            "defaultValue": "false"
        }
    }; }
}
