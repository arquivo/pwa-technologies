import '../../stencil.core';
import { ComponentInterface, EventEmitter, QueueApi } from '../../stencil.core';
import { Color, Mode, StyleEventDetail, ToggleChangeEventDetail } from '../../interface';
export declare class Toggle implements ComponentInterface {
    private inputId;
    private gesture?;
    private buttonEl?;
    private lastDrag;
    el: HTMLElement;
    queue: QueueApi;
    activated: boolean;
    /**
     * The mode determines which platform styles to use.
     */
    mode: Mode;
    /**
     * The color to use from your application's color palette.
     * Default options are: `"primary"`, `"secondary"`, `"tertiary"`, `"success"`, `"warning"`, `"danger"`, `"light"`, `"medium"`, and `"dark"`.
     * For more information on colors, see [theming](/docs/theming/basics).
     */
    color?: Color;
    /**
     * The name of the control, which is submitted with the form data.
     */
    name: string;
    /**
     * If `true`, the toggle is selected.
     */
    checked: boolean;
    /**
     * If `true`, the user cannot interact with the toggle.
     */
    disabled: boolean;
    /**
     * The value of the toggle does not mean if it's checked or not, use the `checked`
     * property for that.
     *
     * The value of a toggle is analogous to the value of a `<input type="checkbox">`,
     * it's only used when the toggle participates in a native `<form>`.
     */
    value?: string | null;
    /**
     * Emitted when the value property has changed.
     */
    ionChange: EventEmitter<ToggleChangeEventDetail>;
    /**
     * Emitted when the toggle has focus.
     */
    ionFocus: EventEmitter<void>;
    /**
     * Emitted when the toggle loses focus.
     */
    ionBlur: EventEmitter<void>;
    /**
     * Emitted when the styles change.
     * @internal
     */
    ionStyle: EventEmitter<StyleEventDetail>;
    checkedChanged(isChecked: boolean): void;
    disabledChanged(): void;
    componentWillLoad(): void;
    componentDidLoad(): Promise<void>;
    componentDidUnload(): void;
    onClick(): void;
    private emitStyle;
    private onStart;
    private onMove;
    private onEnd;
    private getValue;
    private setFocus;
    private onFocus;
    private onBlur;
    hostData(): {
        'role': string;
        'aria-disabled': string | null;
        'aria-checked': string;
        'aria-labelledby': string;
        class: {
            'in-item': boolean;
            'toggle-activated': boolean;
            'toggle-checked': boolean;
            'toggle-disabled': boolean;
            'interactive': boolean;
        } | {
            'in-item': boolean;
            'toggle-activated': boolean;
            'toggle-checked': boolean;
            'toggle-disabled': boolean;
            'interactive': boolean;
        };
    };
    render(): JSX.Element[];
}
