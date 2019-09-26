import { ComponentInterface, EventEmitter } from '../../stencil.core';
import { PickerColumn } from '../../interface';
/**
 * @internal
 */
export declare class PickerColumnCmp implements ComponentInterface {
    private bounceFrom;
    private lastIndex?;
    private minY;
    private maxY;
    private optHeight;
    private rotateFactor;
    private scaleFactor;
    private velocity;
    private y;
    private optsEl?;
    private gesture?;
    private rafId;
    private tmrId;
    private noAnimate;
    el: HTMLElement;
    /**
     * Emitted when the selected value has changed
     * @internal
     */
    ionPickerColChange: EventEmitter<PickerColumn>;
    /** Picker column data */
    col: PickerColumn;
    protected colChanged(): void;
    componentWillLoad(): void;
    componentDidLoad(): Promise<void>;
    componentDidUnload(): void;
    private emitColChange;
    private setSelected;
    private update;
    private decelerate;
    private indexForY;
    private onStart;
    private onMove;
    private onEnd;
    private refresh;
    hostData(): {
        class: {
            [x: string]: boolean;
            'picker-col': boolean;
            'picker-opts-left': boolean;
            'picker-opts-right': boolean;
        };
        style: {
            'max-width': string | undefined;
        };
    };
    render(): any[];
}
