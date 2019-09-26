import { ComponentInterface } from '../../stencil.core';
export declare class App implements ComponentInterface {
    el: HTMLElement;
    componentDidLoad(): void;
    hostData(): {
        class: {
            [x: string]: boolean;
            'ion-page': boolean;
            'force-statusbar-padding': boolean;
        };
    };
}
