export class NavSetRoot {
    push() {
        const nav = this.el.closest('ion-nav');
        const toPush = this.component;
        if (nav && toPush !== undefined) {
            nav.setRoot(toPush, this.componentProps, { skipIfBusy: true });
        }
    }
    static get is() { return "ion-nav-set-root"; }
    static get properties() { return {
        "component": {
            "type": "string",
            "mutable": false,
            "complexType": {
                "original": "NavComponent",
                "resolved": "Function | HTMLElement | ViewController | null | string | undefined",
                "references": {
                    "NavComponent": {
                        "location": "import",
                        "path": "../../interface"
                    }
                }
            },
            "required": false,
            "optional": true,
            "docs": {
                "tags": [],
                "text": "Component you want to make root for the navigation stack"
            },
            "attribute": "component",
            "reflect": false
        },
        "componentProps": {
            "type": "unknown",
            "mutable": false,
            "complexType": {
                "original": "ComponentProps",
                "resolved": "undefined | { [key: string]: any; }",
                "references": {
                    "ComponentProps": {
                        "location": "import",
                        "path": "../../interface"
                    }
                }
            },
            "required": false,
            "optional": true,
            "docs": {
                "tags": [],
                "text": "Data you want to pass to the component as props"
            }
        }
    }; }
    static get elementRef() { return "el"; }
    static get listeners() { return [{
            "name": "click",
            "method": "push",
            "target": undefined,
            "capture": false,
            "passive": false
        }]; }
}
