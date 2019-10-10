import { ComponentInterface } from '../../stencil.core';
export declare class Grid implements ComponentInterface {
    /**
     * If `true`, the grid will have a fixed width based on the screen size.
     */
    fixed: boolean;
    hostData(): {
        class: {
            [x: string]: boolean;
            'grid-fixed': boolean;
        };
    };
    render(): any;
}
