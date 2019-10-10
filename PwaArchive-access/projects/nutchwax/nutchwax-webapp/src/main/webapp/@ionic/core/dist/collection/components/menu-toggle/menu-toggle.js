import { Host, h } from '@stencil/core';
import { getIonMode } from '../../global/ionic-global';
export class MenuToggle {
    constructor() {
        this.visible = false;
        /**
         * Automatically hides the content when the corresponding menu is not active.
         *
         * By default, it's `true`. Change it to `false` in order to
         * keep `ion-menu-toggle` always visible regardless the state of the menu.
         */
        this.autoHide = true;
        this.onClick = async () => {
            const menuCtrl = await getMenuController(document);
            if (menuCtrl) {
                const menu = await menuCtrl.get(this.menu);
                if (menu) {
                    menuCtrl.toggle(this.menu);
                }
            }
        };
    }
    componentDidLoad() {
        return this.updateVisibility();
    }
    async updateVisibility() {
        const menuCtrl = await getMenuController(document);
        if (menuCtrl) {
            const menu = await menuCtrl.get(this.menu);
            if (menu && await menu.isActive()) {
                this.visible = true;
                return;
            }
        }
        this.visible = false;
    }
    render() {
        const mode = getIonMode(this);
        const hidden = this.autoHide && !this.visible;
        return (h(Host, { onClick: this.onClick, "aria-hidden": hidden ? 'true' : null, class: {
                [mode]: true,
                'menu-toggle-hidden': hidden,
            } },
            h("slot", null)));
    }
    static get is() { return "ion-menu-toggle"; }
    static get encapsulation() { return "shadow"; }
    static get originalStyleUrls() { return {
        "$": ["menu-toggle.scss"]
    }; }
    static get styleUrls() { return {
        "$": ["menu-toggle.css"]
    }; }
    static get properties() { return {
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
                "text": "Optional property that maps to a Menu's `menuId` prop.\nCan also be `start` or `end` for the menu side.\nThis is used to find the correct menu to toggle.\n\nIf this property is not used, `ion-menu-toggle` will toggle the\nfirst menu that is active."
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
                "text": "Automatically hides the content when the corresponding menu is not active.\n\nBy default, it's `true`. Change it to `false` in order to\nkeep `ion-menu-toggle` always visible regardless the state of the menu."
            },
            "attribute": "auto-hide",
            "reflect": false,
            "defaultValue": "true"
        }
    }; }
    static get states() { return {
        "visible": {}
    }; }
    static get listeners() { return [{
            "name": "ionMenuChange",
            "method": "updateVisibility",
            "target": "body",
            "capture": false,
            "passive": false
        }, {
            "name": "ionSplitPaneVisible",
            "method": "updateVisibility",
            "target": "body",
            "capture": false,
            "passive": false
        }]; }
}
function getMenuController(doc) {
    const menuControllerElement = doc.querySelector('ion-menu-controller');
    if (!menuControllerElement) {
        return Promise.resolve(undefined);
    }
    return menuControllerElement.componentOnReady();
}
