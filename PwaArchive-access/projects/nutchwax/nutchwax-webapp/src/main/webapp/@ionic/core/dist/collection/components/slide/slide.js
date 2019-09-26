import { getIonMode } from '../../global/ionic-global';
export class Slide {
    componentDidLoad() {
        this.ionSlideChanged.emit();
    }
    componentDidUnload() {
        this.ionSlideChanged.emit();
    }
    hostData() {
        const mode = getIonMode(this);
        return {
            class: {
                [mode]: true,
                'swiper-slide': true,
                'swiper-zoom-container': true
            }
        };
    }
    static get is() { return "ion-slide"; }
    static get originalStyleUrls() { return {
        "$": ["slide.scss"]
    }; }
    static get styleUrls() { return {
        "$": ["slide.css"]
    }; }
    static get events() { return [{
            "method": "ionSlideChanged",
            "name": "ionSlideChanged",
            "bubbles": true,
            "cancelable": true,
            "composed": true,
            "docs": {
                "tags": [{
                        "text": undefined,
                        "name": "internal"
                    }],
                "text": ""
            },
            "complexType": {
                "original": "void",
                "resolved": "void",
                "references": {}
            }
        }]; }
}
