import { getIonMode } from '../../global/ionic-global';
export class Buttons {
    hostData() {
        const mode = getIonMode(this);
        return {
            class: {
                [mode]: true
            }
        };
    }
    static get is() { return "ion-buttons"; }
    static get encapsulation() { return "scoped"; }
    static get originalStyleUrls() { return {
        "ios": ["buttons.ios.scss"],
        "md": ["buttons.md.scss"]
    }; }
    static get styleUrls() { return {
        "ios": ["buttons.ios.css"],
        "md": ["buttons.md.css"]
    }; }
}
