export class NavPop {
    pop() {
        const nav = this.el.closest('ion-nav');
        if (nav) {
            nav.pop({ skipIfBusy: true });
        }
    }
    static get is() { return "ion-nav-pop"; }
    static get elementRef() { return "el"; }
    static get listeners() { return [{
            "name": "click",
            "method": "pop",
            "target": undefined,
            "capture": false,
            "passive": false
        }]; }
}
