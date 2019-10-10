import { Build } from '@stencil/core';
import { config } from '../../global/config';
import { menuOverlayAnimation } from './animations/overlay';
import { menuPushAnimation } from './animations/push';
import { menuRevealAnimation } from './animations/reveal';
export class MenuController {
    constructor() {
        this.menus = [];
        this.menuAnimations = new Map();
        this.registerAnimation('reveal', menuRevealAnimation);
        this.registerAnimation('push', menuPushAnimation);
        this.registerAnimation('overlay', menuOverlayAnimation);
    }
    /**
     * Open the menu. If a menu is not provided then it will open the first
     * menu found. If the specified menu is `start` or `end`, then it will open
     * the enabled menu on that side. Otherwise, it will try to find the menu
     * using the menu's `id` property. If a menu is not found then it will
     * return `false`.
     *
     * @param menu The menuId or side of the menu to open.
     */
    async open(menu) {
        const menuEl = await this.get(menu);
        if (menuEl) {
            return menuEl.open();
        }
        return false;
    }
    /**
     * Close the menu. If a menu is specified, it will close that menu.
     * If no menu is specified, then it will close any menu that is open.
     * If it does not find any open menus, it will return `false`.
     *
     * @param menu The menuId or side of the menu to close.
     */
    async close(menu) {
        const menuEl = await (menu !== undefined ? this.get(menu) : this.getOpen());
        if (menuEl !== undefined) {
            return menuEl.close();
        }
        return false;
    }
    /**
     * Toggle the menu open or closed. If the menu is already open, it will try to
     * close the menu, otherwise it will try to open it. Returns `false` if
     * a menu is not found.
     *
     * @param menu The menuId or side of the menu to toggle.
     */
    async toggle(menu) {
        const menuEl = await this.get(menu);
        if (menuEl) {
            return menuEl.toggle();
        }
        return false;
    }
    /**
     * Enable or disable a menu. Disabling a menu will not allow gestures
     * for that menu or any calls to open it. This is useful when there are
     * multiple menus on the same side and only one of them should be allowed
     * to open. Enabling a menu will automatically disable all other menus
     * on that side.
     *
     * @param enable If `true`, the menu should be enabled.
     * @param menu The menuId or side of the menu to enable or disable.
     */
    async enable(enable, menu) {
        const menuEl = await this.get(menu);
        if (menuEl) {
            menuEl.disabled = !enable;
        }
        return menuEl;
    }
    /**
     * Enable or disable the ability to swipe open the menu.
     *
     * @param enable If `true`, the menu swipe gesture should be enabled.
     * @param menu The menuId or side of the menu to enable or disable the swipe gesture on.
     */
    async swipeGesture(enable, menu) {
        const menuEl = await this.get(menu);
        if (menuEl) {
            menuEl.swipeGesture = enable;
        }
        return menuEl;
    }
    /**
     * Get whether or not the menu is open. Returns `true` if the specified
     * menu is open. If a menu is not specified, it will return `true` if
     * any menu is currently open.
     *
     * @param menu The menuId or side of the menu that is being checked.
     */
    async isOpen(menu) {
        if (menu != null) {
            const menuEl = await this.get(menu);
            return (menuEl !== undefined && menuEl.isOpen());
        }
        else {
            const menuEl = await this.getOpen();
            return menuEl !== undefined;
        }
    }
    /**
     * Get whether or not the menu is enabled. Returns `true` if the
     * specified menu is enabled. Returns `false` if a menu is disabled
     * or not found.
     *
     * @param menu The menuId or side of the menu that is being checked.
     */
    async isEnabled(menu) {
        const menuEl = await this.get(menu);
        if (menuEl) {
            return !menuEl.disabled;
        }
        return false;
    }
    /**
     * Get a menu instance. If a menu is not provided then it will return the first
     * menu found. If the specified menu is `start` or `end`, then it will return the
     * enabled menu on that side. Otherwise, it will try to find the menu using the menu's
     * `id` property. If a menu is not found then it will return `null`.
     *
     * @param menu The menuId or side of the menu.
     */
    async get(menu) {
        if (Build.isDev) {
            if (menu === 'left') {
                console.error('menu.side=left is deprecated, use "start" instead');
                return undefined;
            }
            if (menu === 'right') {
                console.error('menu.side=right is deprecated, use "end" instead');
                return undefined;
            }
        }
        await this.waitUntilReady();
        if (menu === 'start' || menu === 'end') {
            // there could be more than one menu on the same side
            // so first try to get the enabled one
            const menuRef = this.find(m => m.side === menu && !m.disabled);
            if (menuRef) {
                return menuRef;
            }
            // didn't find a menu side that is enabled
            // so try to get the first menu side found
            return this.find(m => m.side === menu);
        }
        else if (menu != null) {
            // the menuId was not left or right
            // so try to get the menu by its "id"
            return this.find(m => m.menuId === menu);
        }
        // return the first enabled menu
        const menuEl = this.find(m => !m.disabled);
        if (menuEl) {
            return menuEl;
        }
        // get the first menu in the array, if one exists
        return this.menus.length > 0 ? this.menus[0].el : undefined;
    }
    /**
     * Get the instance of the opened menu. Returns `null` if a menu is not found.
     */
    async getOpen() {
        await this.waitUntilReady();
        return this.getOpenSync();
    }
    /**
     * Get all menu instances.
     */
    async getMenus() {
        await this.waitUntilReady();
        return this.getMenusSync();
    }
    /**
     * Get whether or not a menu is animating. Returns `true` if any
     * menu is currently animating.
     */
    async isAnimating() {
        await this.waitUntilReady();
        return this.isAnimatingSync();
    }
    /**
     * Registers a new animation that can be used with any `ion-menu` by
     * passing the name of the animation in its `type` property.
     *
     * @param name The name of the animation to register.
     * @param animation The animation function to register.
     */
    async registerAnimation(name, animation) {
        this.menuAnimations.set(name, animation);
    }
    /**
     * @internal
     */
    _getInstance() {
        return Promise.resolve(this);
    }
    _register(menu) {
        const menus = this.menus;
        if (menus.indexOf(menu) < 0) {
            if (!menu.disabled) {
                this._setActiveMenu(menu);
            }
            menus.push(menu);
        }
    }
    _unregister(menu) {
        const index = this.menus.indexOf(menu);
        if (index > -1) {
            this.menus.splice(index, 1);
        }
    }
    _setActiveMenu(menu) {
        // if this menu should be enabled
        // then find all the other menus on this same side
        // and automatically disable other same side menus
        const side = menu.side;
        this.menus
            .filter(m => m.side === side && m !== menu)
            .forEach(m => m.disabled = true);
    }
    async _setOpen(menu, shouldOpen, animated) {
        if (this.isAnimatingSync()) {
            return false;
        }
        if (shouldOpen) {
            const openedMenu = await this.getOpen();
            if (openedMenu && menu.el !== openedMenu) {
                await openedMenu.setOpen(false, false);
            }
        }
        return menu._setOpen(shouldOpen, animated);
    }
    async _createAnimation(type, menuCmp) {
        const animationBuilder = this.menuAnimations.get(type);
        if (!animationBuilder) {
            throw new Error('animation not registered');
        }
        const animation = await import('../../utils/animation')
            .then(mod => mod.create(animationBuilder, null, menuCmp));
        if (!config.getBoolean('animated', true)) {
            animation.duration(0);
        }
        return animation;
    }
    getOpenSync() {
        return this.find(m => m._isOpen);
    }
    getMenusSync() {
        return this.menus.map(menu => menu.el);
    }
    isAnimatingSync() {
        return this.menus.some(menu => menu.isAnimating);
    }
    find(predicate) {
        const instance = this.menus.find(predicate);
        if (instance !== undefined) {
            return instance.el;
        }
        return undefined;
    }
    waitUntilReady() {
        return Promise.all(Array.from(document.querySelectorAll('ion-menu'))
            .map(menu => menu.componentOnReady()));
    }
    static get is() { return "ion-menu-controller"; }
    static get originalStyleUrls() { return {
        "$": ["menu-controller.scss"]
    }; }
    static get styleUrls() { return {
        "$": ["menu-controller.css"]
    }; }
    static get methods() { return {
        "open": {
            "complexType": {
                "signature": "(menu?: string | null | undefined) => Promise<boolean>",
                "parameters": [{
                        "tags": [{
                                "text": "menu The menuId or side of the menu to open.",
                                "name": "param"
                            }],
                        "text": "The menuId or side of the menu to open."
                    }],
                "references": {
                    "Promise": {
                        "location": "global"
                    }
                },
                "return": "Promise<boolean>"
            },
            "docs": {
                "text": "Open the menu. If a menu is not provided then it will open the first\nmenu found. If the specified menu is `start` or `end`, then it will open\nthe enabled menu on that side. Otherwise, it will try to find the menu\nusing the menu's `id` property. If a menu is not found then it will\nreturn `false`.",
                "tags": [{
                        "name": "param",
                        "text": "menu The menuId or side of the menu to open."
                    }]
            }
        },
        "close": {
            "complexType": {
                "signature": "(menu?: string | null | undefined) => Promise<boolean>",
                "parameters": [{
                        "tags": [{
                                "text": "menu The menuId or side of the menu to close.",
                                "name": "param"
                            }],
                        "text": "The menuId or side of the menu to close."
                    }],
                "references": {
                    "Promise": {
                        "location": "global"
                    }
                },
                "return": "Promise<boolean>"
            },
            "docs": {
                "text": "Close the menu. If a menu is specified, it will close that menu.\nIf no menu is specified, then it will close any menu that is open.\nIf it does not find any open menus, it will return `false`.",
                "tags": [{
                        "name": "param",
                        "text": "menu The menuId or side of the menu to close."
                    }]
            }
        },
        "toggle": {
            "complexType": {
                "signature": "(menu?: string | null | undefined) => Promise<boolean>",
                "parameters": [{
                        "tags": [{
                                "text": "menu The menuId or side of the menu to toggle.",
                                "name": "param"
                            }],
                        "text": "The menuId or side of the menu to toggle."
                    }],
                "references": {
                    "Promise": {
                        "location": "global"
                    }
                },
                "return": "Promise<boolean>"
            },
            "docs": {
                "text": "Toggle the menu open or closed. If the menu is already open, it will try to\nclose the menu, otherwise it will try to open it. Returns `false` if\na menu is not found.",
                "tags": [{
                        "name": "param",
                        "text": "menu The menuId or side of the menu to toggle."
                    }]
            }
        },
        "enable": {
            "complexType": {
                "signature": "(enable: boolean, menu?: string | null | undefined) => Promise<HTMLIonMenuElement | undefined>",
                "parameters": [{
                        "tags": [{
                                "text": "enable If `true`, the menu should be enabled.",
                                "name": "param"
                            }],
                        "text": "If `true`, the menu should be enabled."
                    }, {
                        "tags": [{
                                "text": "menu The menuId or side of the menu to enable or disable.",
                                "name": "param"
                            }],
                        "text": "The menuId or side of the menu to enable or disable."
                    }],
                "references": {
                    "Promise": {
                        "location": "global"
                    },
                    "HTMLIonMenuElement": {
                        "location": "global"
                    }
                },
                "return": "Promise<HTMLIonMenuElement | undefined>"
            },
            "docs": {
                "text": "Enable or disable a menu. Disabling a menu will not allow gestures\nfor that menu or any calls to open it. This is useful when there are\nmultiple menus on the same side and only one of them should be allowed\nto open. Enabling a menu will automatically disable all other menus\non that side.",
                "tags": [{
                        "name": "param",
                        "text": "enable If `true`, the menu should be enabled."
                    }, {
                        "name": "param",
                        "text": "menu The menuId or side of the menu to enable or disable."
                    }]
            }
        },
        "swipeGesture": {
            "complexType": {
                "signature": "(enable: boolean, menu?: string | null | undefined) => Promise<HTMLIonMenuElement | undefined>",
                "parameters": [{
                        "tags": [{
                                "text": "enable If `true`, the menu swipe gesture should be enabled.",
                                "name": "param"
                            }],
                        "text": "If `true`, the menu swipe gesture should be enabled."
                    }, {
                        "tags": [{
                                "text": "menu The menuId or side of the menu to enable or disable the swipe gesture on.",
                                "name": "param"
                            }],
                        "text": "The menuId or side of the menu to enable or disable the swipe gesture on."
                    }],
                "references": {
                    "Promise": {
                        "location": "global"
                    },
                    "HTMLIonMenuElement": {
                        "location": "global"
                    }
                },
                "return": "Promise<HTMLIonMenuElement | undefined>"
            },
            "docs": {
                "text": "Enable or disable the ability to swipe open the menu.",
                "tags": [{
                        "name": "param",
                        "text": "enable If `true`, the menu swipe gesture should be enabled."
                    }, {
                        "name": "param",
                        "text": "menu The menuId or side of the menu to enable or disable the swipe gesture on."
                    }]
            }
        },
        "isOpen": {
            "complexType": {
                "signature": "(menu?: string | null | undefined) => Promise<boolean>",
                "parameters": [{
                        "tags": [{
                                "text": "menu The menuId or side of the menu that is being checked.",
                                "name": "param"
                            }],
                        "text": "The menuId or side of the menu that is being checked."
                    }],
                "references": {
                    "Promise": {
                        "location": "global"
                    }
                },
                "return": "Promise<boolean>"
            },
            "docs": {
                "text": "Get whether or not the menu is open. Returns `true` if the specified\nmenu is open. If a menu is not specified, it will return `true` if\nany menu is currently open.",
                "tags": [{
                        "name": "param",
                        "text": "menu The menuId or side of the menu that is being checked."
                    }]
            }
        },
        "isEnabled": {
            "complexType": {
                "signature": "(menu?: string | null | undefined) => Promise<boolean>",
                "parameters": [{
                        "tags": [{
                                "text": "menu The menuId or side of the menu that is being checked.",
                                "name": "param"
                            }],
                        "text": "The menuId or side of the menu that is being checked."
                    }],
                "references": {
                    "Promise": {
                        "location": "global"
                    }
                },
                "return": "Promise<boolean>"
            },
            "docs": {
                "text": "Get whether or not the menu is enabled. Returns `true` if the\nspecified menu is enabled. Returns `false` if a menu is disabled\nor not found.",
                "tags": [{
                        "name": "param",
                        "text": "menu The menuId or side of the menu that is being checked."
                    }]
            }
        },
        "get": {
            "complexType": {
                "signature": "(menu?: string | null | undefined) => Promise<HTMLIonMenuElement | undefined>",
                "parameters": [{
                        "tags": [{
                                "text": "menu The menuId or side of the menu.",
                                "name": "param"
                            }],
                        "text": "The menuId or side of the menu."
                    }],
                "references": {
                    "Promise": {
                        "location": "global"
                    },
                    "HTMLIonMenuElement": {
                        "location": "global"
                    }
                },
                "return": "Promise<HTMLIonMenuElement | undefined>"
            },
            "docs": {
                "text": "Get a menu instance. If a menu is not provided then it will return the first\nmenu found. If the specified menu is `start` or `end`, then it will return the\nenabled menu on that side. Otherwise, it will try to find the menu using the menu's\n`id` property. If a menu is not found then it will return `null`.",
                "tags": [{
                        "name": "param",
                        "text": "menu The menuId or side of the menu."
                    }]
            }
        },
        "getOpen": {
            "complexType": {
                "signature": "() => Promise<HTMLIonMenuElement | undefined>",
                "parameters": [],
                "references": {
                    "Promise": {
                        "location": "global"
                    },
                    "HTMLIonMenuElement": {
                        "location": "global"
                    }
                },
                "return": "Promise<HTMLIonMenuElement | undefined>"
            },
            "docs": {
                "text": "Get the instance of the opened menu. Returns `null` if a menu is not found.",
                "tags": []
            }
        },
        "getMenus": {
            "complexType": {
                "signature": "() => Promise<HTMLIonMenuElement[]>",
                "parameters": [],
                "references": {
                    "Promise": {
                        "location": "global"
                    },
                    "HTMLIonMenuElement": {
                        "location": "global"
                    }
                },
                "return": "Promise<HTMLIonMenuElement[]>"
            },
            "docs": {
                "text": "Get all menu instances.",
                "tags": []
            }
        },
        "isAnimating": {
            "complexType": {
                "signature": "() => Promise<boolean>",
                "parameters": [],
                "references": {
                    "Promise": {
                        "location": "global"
                    }
                },
                "return": "Promise<boolean>"
            },
            "docs": {
                "text": "Get whether or not a menu is animating. Returns `true` if any\nmenu is currently animating.",
                "tags": []
            }
        },
        "registerAnimation": {
            "complexType": {
                "signature": "(name: string, animation: AnimationBuilder) => Promise<void>",
                "parameters": [{
                        "tags": [{
                                "text": "name The name of the animation to register.",
                                "name": "param"
                            }],
                        "text": "The name of the animation to register."
                    }, {
                        "tags": [{
                                "text": "animation The animation function to register.",
                                "name": "param"
                            }],
                        "text": "The animation function to register."
                    }],
                "references": {
                    "Promise": {
                        "location": "global"
                    },
                    "AnimationBuilder": {
                        "location": "import",
                        "path": "../../interface"
                    }
                },
                "return": "Promise<void>"
            },
            "docs": {
                "text": "Registers a new animation that can be used with any `ion-menu` by\npassing the name of the animation in its `type` property.",
                "tags": [{
                        "name": "param",
                        "text": "name The name of the animation to register."
                    }, {
                        "name": "param",
                        "text": "animation The animation function to register."
                    }]
            }
        },
        "_getInstance": {
            "complexType": {
                "signature": "() => Promise<MenuControllerI>",
                "parameters": [],
                "references": {
                    "Promise": {
                        "location": "global"
                    },
                    "MenuControllerI": {
                        "location": "import",
                        "path": "../../interface"
                    }
                },
                "return": "Promise<MenuControllerI>"
            },
            "docs": {
                "text": "",
                "tags": [{
                        "name": "internal",
                        "text": undefined
                    }]
            }
        }
    }; }
}
