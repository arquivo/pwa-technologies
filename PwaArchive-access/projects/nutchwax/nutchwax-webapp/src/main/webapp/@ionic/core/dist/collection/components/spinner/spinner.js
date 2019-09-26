import { h } from '@stencil/core';
import { config } from '../../global/config';
import { getIonMode } from '../../global/ionic-global';
import { createColorClasses } from '../../utils/theme';
import { SPINNERS } from './spinner-configs';
export class Spinner {
    constructor() {
        /**
         * If `true`, the spinner's animation will be paused.
         */
        this.paused = false;
    }
    getName() {
        const name = this.name || config.get('spinner');
        const mode = getIonMode(this);
        if (name) {
            return name;
        }
        return (mode === 'ios') ? 'lines' : 'crescent';
    }
    hostData() {
        const mode = getIonMode(this);
        return {
            class: Object.assign({}, createColorClasses(this.color), { [mode]: true, [`spinner-${this.getName()}`]: true, 'spinner-paused': !!this.paused || config.getBoolean('_testing') })
        };
    }
    render() {
        const name = this.getName();
        const spinner = SPINNERS[name] || SPINNERS['lines'];
        const duration = (typeof this.duration === 'number' && this.duration > 10 ? this.duration : spinner.dur);
        const svgs = [];
        if (spinner.circles !== undefined) {
            for (let i = 0; i < spinner.circles; i++) {
                svgs.push(buildCircle(spinner, duration, i, spinner.circles));
            }
        }
        else if (spinner.lines !== undefined) {
            for (let i = 0; i < spinner.lines; i++) {
                svgs.push(buildLine(spinner, duration, i, spinner.lines));
            }
        }
        return svgs;
    }
    static get is() { return "ion-spinner"; }
    static get encapsulation() { return "shadow"; }
    static get originalStyleUrls() { return {
        "$": ["spinner.scss"]
    }; }
    static get styleUrls() { return {
        "$": ["spinner.css"]
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
        "duration": {
            "type": "number",
            "mutable": false,
            "complexType": {
                "original": "number",
                "resolved": "number | undefined",
                "references": {}
            },
            "required": false,
            "optional": true,
            "docs": {
                "tags": [],
                "text": "Duration of the spinner animation in milliseconds. The default varies based on the spinner."
            },
            "attribute": "duration",
            "reflect": false
        },
        "name": {
            "type": "string",
            "mutable": false,
            "complexType": {
                "original": "SpinnerTypes",
                "resolved": "\"bubbles\" | \"circles\" | \"crescent\" | \"dots\" | \"lines\" | \"lines-small\" | undefined",
                "references": {
                    "SpinnerTypes": {
                        "location": "import",
                        "path": "../../interface"
                    }
                }
            },
            "required": false,
            "optional": true,
            "docs": {
                "tags": [],
                "text": "The name of the SVG spinner to use. If a name is not provided, the platform's default\nspinner will be used."
            },
            "attribute": "name",
            "reflect": false
        },
        "paused": {
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
                "text": "If `true`, the spinner's animation will be paused."
            },
            "attribute": "paused",
            "reflect": false,
            "defaultValue": "false"
        }
    }; }
}
function buildCircle(spinner, duration, index, total) {
    const data = spinner.fn(duration, index, total);
    data.style['animation-duration'] = `${duration}ms`;
    return (h("svg", { viewBox: "0 0 64 64", style: data.style },
        h("circle", { transform: "translate(32,32)", r: data.r })));
}
function buildLine(spinner, duration, index, total) {
    const data = spinner.fn(duration, index, total);
    data.style['animation-duration'] = `${duration}ms`;
    return (h("svg", { viewBox: "0 0 64 64", style: data.style },
        h("line", { transform: "translate(32,32)", y1: data.y1, y2: data.y2 })));
}
