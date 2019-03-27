import { createColorClasses, openURL } from '../../utils/theme';
export class Anchor {
    constructor() {
        this.routerDirection = 'forward';
    }
    onClick(ev) {
        openURL(this.win, this.href, ev, this.routerDirection);
    }
    hostData() {
        return {
            class: Object.assign({}, createColorClasses(this.color), { 'ion-activatable': true })
        };
    }
    render() {
        return (h("a", { href: this.href },
            h("slot", null)));
    }
    static get is() { return "ion-anchor"; }
    static get encapsulation() { return "shadow"; }
    static get properties() { return {
        "color": {
            "type": String,
            "attr": "color"
        },
        "href": {
            "type": String,
            "attr": "href"
        },
        "routerDirection": {
            "type": String,
            "attr": "router-direction"
        },
        "win": {
            "context": "window"
        }
    }; }
    static get listeners() { return [{
            "name": "click",
            "method": "onClick"
        }]; }
    static get style() { return "/**style-placeholder:ion-anchor:**/"; }
}
