import { h } from '../ionic.core.js';

import { c as createColorClasses, d as hostContext } from './chunk-7c632336.js';
import { d as findItemLabel, e as renderHiddenInput } from './chunk-6d7d2f8c.js';
import { d as hapticSelection } from './chunk-81780b86.js';

class Toggle {
    constructor() {
        this.inputId = `ion-tg-${toggleIds++}`;
        this.lastDrag = 0;
        this.activated = false;
        this.name = this.inputId;
        this.checked = false;
        this.disabled = false;
        this.value = 'on';
        this.onFocus = () => {
            this.ionFocus.emit();
        };
        this.onBlur = () => {
            this.ionBlur.emit();
        };
    }
    checkedChanged(isChecked) {
        this.ionChange.emit({
            checked: isChecked,
            value: this.value
        });
    }
    disabledChanged() {
        this.emitStyle();
        if (this.gesture) {
            this.gesture.setDisabled(this.disabled);
        }
    }
    componentWillLoad() {
        this.emitStyle();
    }
    async componentDidLoad() {
        this.gesture = (await import('./chunk-f56eaea8.js')).createGesture({
            el: this.el,
            queue: this.queue,
            gestureName: 'toggle',
            gesturePriority: 100,
            threshold: 5,
            passive: false,
            onStart: () => this.onStart(),
            onMove: ev => this.onMove(ev),
            onEnd: ev => this.onEnd(ev),
        });
        this.disabledChanged();
    }
    componentDidUnload() {
        if (this.gesture) {
            this.gesture.destroy();
            this.gesture = undefined;
        }
    }
    onClick() {
        if (this.lastDrag + 300 < Date.now()) {
            this.checked = !this.checked;
        }
    }
    emitStyle() {
        this.ionStyle.emit({
            'interactive-disabled': this.disabled,
        });
    }
    onStart() {
        this.activated = true;
        this.setFocus();
    }
    onMove(detail) {
        if (shouldToggle(this.checked, detail.deltaX, -10)) {
            this.checked = !this.checked;
            hapticSelection();
        }
    }
    onEnd(ev) {
        this.activated = false;
        this.lastDrag = Date.now();
        ev.event.preventDefault();
        ev.event.stopImmediatePropagation();
    }
    getValue() {
        return this.value || '';
    }
    setFocus() {
        if (this.buttonEl) {
            this.buttonEl.focus();
        }
    }
    hostData() {
        const { inputId, disabled, checked, activated, color, el } = this;
        const labelId = inputId + '-lbl';
        const label = findItemLabel(el);
        if (label) {
            label.id = labelId;
        }
        return {
            'role': 'checkbox',
            'aria-disabled': disabled ? 'true' : null,
            'aria-checked': `${checked}`,
            'aria-labelledby': labelId,
            class: Object.assign({}, createColorClasses(color), { 'in-item': hostContext('ion-item', el), 'toggle-activated': activated, 'toggle-checked': checked, 'toggle-disabled': disabled, 'interactive': true })
        };
    }
    render() {
        const value = this.getValue();
        renderHiddenInput(true, this.el, this.name, (this.checked ? value : ''), this.disabled);
        return [
            h("div", { class: "toggle-icon" },
                h("div", { class: "toggle-inner" })),
            h("button", { type: "button", onFocus: this.onFocus, onBlur: this.onBlur, disabled: this.disabled, ref: el => this.buttonEl = el })
        ];
    }
    static get is() { return "ion-toggle"; }
    static get encapsulation() { return "shadow"; }
    static get properties() { return {
        "activated": {
            "state": true
        },
        "checked": {
            "type": Boolean,
            "attr": "checked",
            "mutable": true,
            "watchCallbacks": ["checkedChanged"]
        },
        "color": {
            "type": String,
            "attr": "color"
        },
        "disabled": {
            "type": Boolean,
            "attr": "disabled",
            "watchCallbacks": ["disabledChanged"]
        },
        "el": {
            "elementRef": true
        },
        "mode": {
            "type": String,
            "attr": "mode"
        },
        "name": {
            "type": String,
            "attr": "name"
        },
        "queue": {
            "context": "queue"
        },
        "value": {
            "type": String,
            "attr": "value"
        }
    }; }
    static get events() { return [{
            "name": "ionChange",
            "method": "ionChange",
            "bubbles": true,
            "cancelable": true,
            "composed": true
        }, {
            "name": "ionFocus",
            "method": "ionFocus",
            "bubbles": true,
            "cancelable": true,
            "composed": true
        }, {
            "name": "ionBlur",
            "method": "ionBlur",
            "bubbles": true,
            "cancelable": true,
            "composed": true
        }, {
            "name": "ionStyle",
            "method": "ionStyle",
            "bubbles": true,
            "cancelable": true,
            "composed": true
        }]; }
    static get listeners() { return [{
            "name": "click",
            "method": "onClick"
        }]; }
    static get style() { return ".sc-ion-toggle-md-h{-webkit-box-sizing:content-box!important;box-sizing:content-box!important;display:inline-block;outline:none;contain:content;cursor:pointer;-ms-touch-action:none;touch-action:none;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;z-index:2}.ion-focused.sc-ion-toggle-md-h   input.sc-ion-toggle-md{border:2px solid #5e9ed6}.toggle-disabled.sc-ion-toggle-md-h{pointer-events:none}button.sc-ion-toggle-md{left:0;top:0;margin-left:0;margin-right:0;margin-top:0;margin-bottom:0;position:absolute;width:100%;height:100%;border:0;background:transparent;cursor:pointer;-webkit-appearance:none;-moz-appearance:none;appearance:none;outline:none}[dir=rtl].sc-ion-toggle-md-h   button.sc-ion-toggle-md, [dir=rtl]   .sc-ion-toggle-md-h   button.sc-ion-toggle-md{right:0}button.sc-ion-toggle-md::-moz-focus-inner{border:0}.sc-ion-toggle-md-h{--background:var(--ion-color-medium-tint,#a2a4ab);--background-checked:rgba(var(--ion-color-primary-rgb,56,128,255),0.5);--handle-background:var(--ion-background-color,#fff);--handle-background-checked:var(--ion-color-primary,#3880ff);padding-left:12px;padding-right:12px;padding-top:12px;padding-bottom:12px;-webkit-box-sizing:content-box;box-sizing:content-box;position:relative;width:36px;height:14px;contain:strict}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.sc-ion-toggle-md-h{padding-left:unset;padding-right:unset;-webkit-padding-start:12px;padding-inline-start:12px;-webkit-padding-end:12px;padding-inline-end:12px}}.ion-color.toggle-checked.sc-ion-toggle-md-h   .toggle-icon.sc-ion-toggle-md{background:rgba(var(--ion-color-base-rgb),.5)}.ion-color.toggle-checked.sc-ion-toggle-md-h   .toggle-inner.sc-ion-toggle-md{background:var(--ion-color-base)}.toggle-icon.sc-ion-toggle-md{border-radius:14px;display:block;position:relative;width:100%;height:100%;-webkit-transition:background-color .16s;transition:background-color .16s;background:var(--background);pointer-events:none}.toggle-inner.sc-ion-toggle-md{left:0;top:-3px;border-radius:50%;position:absolute;width:20px;height:20px;-webkit-transition-duration:.16s;transition-duration:.16s;-webkit-transition-property:background-color,-webkit-transform;transition-property:background-color,-webkit-transform;transition-property:transform,background-color;transition-property:transform,background-color,-webkit-transform;-webkit-transition-timing-function:cubic-bezier(.4,0,.2,1);transition-timing-function:cubic-bezier(.4,0,.2,1);background:var(--handle-background);-webkit-box-shadow:0 2px 2px 0 rgba(0,0,0,.14),0 3px 1px -2px rgba(0,0,0,.2),0 1px 5px 0 rgba(0,0,0,.12);box-shadow:0 2px 2px 0 rgba(0,0,0,.14),0 3px 1px -2px rgba(0,0,0,.2),0 1px 5px 0 rgba(0,0,0,.12);will-change:transform,background-color;contain:strict}[dir=rtl].sc-ion-toggle-md-h   .toggle-inner.sc-ion-toggle-md, [dir=rtl]   .sc-ion-toggle-md-h   .toggle-inner.sc-ion-toggle-md{right:0}.toggle-checked.sc-ion-toggle-md-h   .toggle-icon.sc-ion-toggle-md{background:var(--background-checked)}.toggle-checked.sc-ion-toggle-md-h   .toggle-inner.sc-ion-toggle-md{-webkit-transform:translate3d(16px,0,0);transform:translate3d(16px,0,0);background:var(--handle-background-checked)}[dir=rtl].toggle-checked.sc-ion-toggle-md-h   .toggle-inner.sc-ion-toggle-md{-webkit-transform:translate3d(calc(-1 * 16px),0,0);transform:translate3d(calc(-1 * 16px),0,0)}.toggle-disabled.sc-ion-toggle-md-h{opacity:.3}.in-item[slot].sc-ion-toggle-md-h{margin-left:0;margin-right:0;margin-top:0;margin-bottom:0;padding-left:16px;padding-right:0;padding-top:12px;padding-bottom:12px;cursor:pointer}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.in-item[slot].sc-ion-toggle-md-h{padding-left:unset;padding-right:unset;-webkit-padding-start:16px;padding-inline-start:16px;-webkit-padding-end:0;padding-inline-end:0}}.in-item[slot=start].sc-ion-toggle-md-h{padding-left:2px;padding-right:18px;padding-top:12px;padding-bottom:12px}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.in-item[slot=start].sc-ion-toggle-md-h{padding-left:unset;padding-right:unset;-webkit-padding-start:2px;padding-inline-start:2px;-webkit-padding-end:18px;padding-inline-end:18px}}"; }
    static get styleMode() { return "md"; }
}
function shouldToggle(checked, deltaX, margin) {
    const isRTL = document.dir === 'rtl';
    if (checked) {
        return (!isRTL && (margin > deltaX)) ||
            (isRTL && (-margin < deltaX));
    }
    else {
        return (!isRTL && (-margin < deltaX)) ||
            (isRTL && (margin > deltaX));
    }
}
let toggleIds = 0;

export { Toggle as IonToggle };
