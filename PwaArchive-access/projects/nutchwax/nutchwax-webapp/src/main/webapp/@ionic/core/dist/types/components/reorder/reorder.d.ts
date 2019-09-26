import { ComponentInterface } from '../../stencil.core';
export declare class Reorder implements ComponentInterface {
    onClick(ev: Event): void;
    hostData(): {
        class: {
            [x: string]: boolean;
        };
    };
    render(): any;
}
