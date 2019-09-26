import { h } from '@stencil/core';
import { config } from '../../global/config';
import { getIonMode } from '../../global/ionic-global';
import { createColorClasses } from '../../utils/theme';
export class MenuButton {
    constructor() {
        /**
         * If `true`, the user cannot interact with the menu button.
         */
        this.disabled = false;
        /**
         * Automatically hides the menu button when the corresponding menu is not active
         */
        this.autoHide = true;
        /**
         * The type of the button.
         */
        this.type = 'button';
    }
    hostData() {
        const mode = getIonMode(this);
        const { color, disabled } = this;
        return {
            'aria-disabled': disabled ? 'true' : null,
            class: Object.assign({}, createColorClasses(color), { [mode]: true, 'button': true, 'menu-button-disabled': disabled, 'ion-activatable': true, 'ion-focusable': true })
        };
    }
    render() {
        const mode = getIonMode(this);
        const menuIcon = config.get('menuIcon', 'menu');
        const attrs = {
            type: this.type
        };
        return (h("ion-menu-toggle", { menu: this.menu, autoHide: this.autoHide },
            h("button", Object.assign({}, attrs, { disabled: this.disabled, class: "button-native" }),
                h("slot", null,
                    h("ion-icon", { icon: menuIcon, mode: mode, lazy: false })),
                mode === 'md' && h("ion-ripple-effect", { type: "unbounded" }))));
    }
    static get is() { return "ion-menu-button"; }
    static get encapsulation() { return "shadow"; }
    static get originalStyleUrls() { return {
        "ios": ["menu-button.ios.scss"],
        "md": ["menu-button.md.scss"]
    }; }
    static get styleUrls() { return {
        "ios": ["menu-button.ios.css"],
        "md": ["menu-button.md.css"]
    }; }
    static get properties() { return {
        "color": {
            "type": "string",
            "mutable": false,
            "complexType": {
                "original": "Color",
                "resolved": "string | undefined",
                "references": {
                    "Color": {
                        "location": "import",
                        "path": "../../interface"
                    }
                }
            },
            "required": false,
            "optional": true,
            "docs": {
                "tags": [],
                "text": "The color to use from your application's color palette.\nDefault options are: `\"primary\"`, `\"secondary\"`, `\"tertiary\"`, `\"success\"`, `\"warning\"`, `\"danger\"`, `\"light\"`, `\"medium\"`, and `\"dark\"`.\nFor more information on colors, see [theming](/docs/theming/basics)."
            },
            "attribute": "color",
            "reflect": false
        },
        "disabled": {
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
                "text": "If `true`, the user cannot interact with the menu button."
            },
            "attribute": "disabled",
            "reflect": false,
            "defaultValue": "false"
        },
        "menu": {
            "type": "string",
            "mutable": false,
            "complexType": {
                "original": "string",
                "resolved": "string | undefined",
                "references": {}
            },
            "required": false,
            "optional": true,
            "docs": {
                "tags": [],
                "text": "Optional property that maps to a Menu's `menuId` prop. Can also be `start` or `end` for the menu side. This is used to find the correct menu to toggle"
            },
            "attribute": "menu",
            "reflect": false
        },
        "autoHide": {
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
                "text": "Automatically hides the menu button when the corresponding menu is not active"
            },
            "attribute": "auto-hide",
            "reflect": false,
            "defaultValue": "true"
        },
        "type": {
            "type": "string",
            "mutable": false,
            "complexType": {
                "original": "'submit' | 'reset' | 'button'",
                "resolved": "\"button\" | \"reset\" | \"submit\"",
                "references": {}
            },
            "required": false,
            "optional": false,
            "docs": {
                "tags": [],
                "text": "The type of the button."
            },
            "attribute": "type",
            "reflect": false,
            "defaultValue": "'button'"
        }
    }; }
}
