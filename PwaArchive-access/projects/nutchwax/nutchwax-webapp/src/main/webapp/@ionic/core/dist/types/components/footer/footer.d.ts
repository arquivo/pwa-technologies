import { ComponentInterface } from '../../stencil.core';
/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 */
export declare class Footer implements ComponentInterface {
    /**
     * If `true`, the footer will be translucent. Only applies to `ios` mode.
     * Note: In order to scroll content behind the footer, the `fullscreen`
     * attribute needs to be set on the content.
     */
    translucent: boolean;
    hostData(): {
        class: {
            [x: string]: boolean;
            [`footer-translucent`]: boolean;
        };
    };
}
