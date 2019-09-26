'use strict';

Object.defineProperty(exports, '__esModule', { value: true });

const __chunk_1 = require('./chunk-8de9033d.js');
const __chunk_2 = require('./chunk-d8847c1c.js');
const __chunk_5 = require('./chunk-9c9fbda4.js');
const __chunk_7 = require('./chunk-7fe29d8c.js');
const index = require('./index-496ef24c.js');

class Menu {
    constructor(hostRef) {
        __chunk_1.registerInstance(this, hostRef);
        this.lastOnEnd = 0;
        this.blocker = index.GESTURE_CONTROLLER.createBlocker({ disableScroll: true });
        this.mode = __chunk_1.getIonMode(this);
        this.isAnimating = false;
        this._isOpen = false;
        this.isPaneVisible = false;
        this.isEndSide = false;
        /**
         * If `true`, the menu is disabled.
         */
        this.disabled = false;
        /**
         * Which side of the view the menu should be placed.
         */
        this.side = 'start';
        /**
         * If `true`, swiping the menu is enabled.
         */
        this.swipeGesture = true;
        /**
         * The edge threshold for dragging the menu open.
         * If a drag/swipe happens over this value, the menu is not triggered.
         */
        this.maxEdgeStart = 50;
        this.ionWillOpen = __chunk_1.createEvent(this, "ionWillOpen", 7);
        this.ionWillClose = __chunk_1.createEvent(this, "ionWillClose", 7);
        this.ionDidOpen = __chunk_1.createEvent(this, "ionDidOpen", 7);
        this.ionDidClose = __chunk_1.createEvent(this, "ionDidClose", 7);
        this.ionMenuChange = __chunk_1.createEvent(this, "ionMenuChange", 7);
        this.lazyMenuCtrl = __chunk_1.getConnect(this, "ion-menu-controller");
    }
    typeChanged(type, oldType) {
        const contentEl = this.contentEl;
        if (contentEl) {
            if (oldType !== undefined) {
                contentEl.classList.remove(`menu-content-${oldType}`);
            }
            contentEl.classList.add(`menu-content-${type}`);
            contentEl.removeAttribute('style');
        }
        if (this.menuInnerEl) {
            // Remove effects of previous animations
            this.menuInnerEl.removeAttribute('style');
        }
        this.animation = undefined;
    }
    disabledChanged() {
        this.updateState();
        this.ionMenuChange.emit({
            disabled: this.disabled,
            open: this._isOpen
        });
    }
    sideChanged() {
        this.isEndSide = __chunk_7.isEndSide(this.side);
    }
    swipeGestureChanged() {
        this.updateState();
    }
    async componentWillLoad() {
        if (this.type === undefined) {
            this.type = __chunk_2.config.get('menuType', this.mode === 'ios' ? 'reveal' : 'overlay');
        }
        const menuCtrl = this.menuCtrl = await this.lazyMenuCtrl.componentOnReady().then(p => p._getInstance());
        const el = this.el;
        const parent = el.parentNode;
        const content = this.contentId !== undefined
            ? document.getElementById(this.contentId)
            : parent && parent.querySelector && parent.querySelector('[main]');
        if (!content || !content.tagName) {
            // requires content element
            console.error('Menu: must have a "content" element to listen for drag events on.');
            return;
        }
        this.contentEl = content;
        // add menu's content classes
        content.classList.add('menu-content');
        this.typeChanged(this.type, undefined);
        this.sideChanged();
        // register this menu with the app's menu controller
        menuCtrl._register(this);
        this.gesture = (await new Promise(function (resolve) { resolve(require('./index-496ef24c.js')); })).createGesture({
            el: document,
            gestureName: 'menu-swipe',
            gesturePriority: 30,
            threshold: 10,
            canStart: ev => this.canStart(ev),
            onWillStart: () => this.onWillStart(),
            onStart: () => this.onStart(),
            onMove: ev => this.onMove(ev),
            onEnd: ev => this.onEnd(ev),
        });
        this.updateState();
    }
    componentDidLoad() {
        this.ionMenuChange.emit({ disabled: this.disabled, open: this._isOpen });
    }
    componentDidUnload() {
        this.blocker.destroy();
        this.menuCtrl._unregister(this);
        if (this.animation) {
            this.animation.destroy();
        }
        if (this.gesture) {
            this.gesture.destroy();
            this.gesture = undefined;
        }
        this.animation = undefined;
        this.contentEl = this.backdropEl = this.menuInnerEl = undefined;
    }
    onSplitPaneChanged(ev) {
        this.isPaneVisible = ev.detail.isPane(this.el);
        this.updateState();
    }
    onBackdropClick(ev) {
        if (this._isOpen && this.lastOnEnd < ev.timeStamp - 100) {
            const shouldClose = (ev.composedPath)
                ? !ev.composedPath().includes(this.menuInnerEl)
                : false;
            if (shouldClose) {
                ev.preventDefault();
                ev.stopPropagation();
                this.close();
            }
        }
    }
    /**
     * Returns `true` is the menu is open.
     */
    isOpen() {
        return Promise.resolve(this._isOpen);
    }
    /**
     * Returns `true` is the menu is active.
     *
     * A menu is active when it can be opened or closed, meaning it's enabled
     * and it's not part of a `ion-split-pane`.
     */
    isActive() {
        return Promise.resolve(this._isActive());
    }
    /**
     * Opens the menu. If the menu is already open or it can't be opened,
     * it returns `false`.
     */
    open(animated = true) {
        return this.setOpen(true, animated);
    }
    /**
     * Closes the menu. If the menu is already closed or it can't be closed,
     * it returns `false`.
     */
    close(animated = true) {
        return this.setOpen(false, animated);
    }
    /**
     * Toggles the menu. If the menu is already open, it will try to close, otherwise it will try to open it.
     * If the operation can't be completed successfully, it returns `false`.
     */
    toggle(animated = true) {
        return this.setOpen(!this._isOpen, animated);
    }
    /**
     * Opens or closes the button.
     * If the operation can't be completed successfully, it returns `false`.
     */
    setOpen(shouldOpen, animated = true) {
        return this.menuCtrl._setOpen(this, shouldOpen, animated);
    }
    async _setOpen(shouldOpen, animated = true) {
        // If the menu is disabled or it is currently being animated, let's do nothing
        if (!this._isActive() || this.isAnimating || shouldOpen === this._isOpen) {
            return false;
        }
        this.beforeAnimation(shouldOpen);
        await this.loadAnimation();
        await this.startAnimation(shouldOpen, animated);
        this.afterAnimation(shouldOpen);
        return true;
    }
    async loadAnimation() {
        // Menu swipe animation takes the menu's inner width as parameter,
        // If `offsetWidth` changes, we need to create a new animation.
        const width = this.menuInnerEl.offsetWidth;
        if (width === this.width && this.animation !== undefined) {
            return;
        }
        this.width = width;
        // Destroy existing animation
        if (this.animation) {
            this.animation.destroy();
            this.animation = undefined;
        }
        // Create new animation
        this.animation = await this.menuCtrl._createAnimation(this.type, this);
    }
    async startAnimation(shouldOpen, animated) {
        const ani = this.animation.reverse(!shouldOpen);
        if (animated) {
            await ani.playAsync();
        }
        else {
            ani.playSync();
        }
    }
    _isActive() {
        return !this.disabled && !this.isPaneVisible;
    }
    canSwipe() {
        return this.swipeGesture && !this.isAnimating && this._isActive();
    }
    canStart(detail) {
        if (!this.canSwipe()) {
            return false;
        }
        if (this._isOpen) {
            return true;
            // TODO error
        }
        else if (this.menuCtrl.getOpenSync()) {
            return false;
        }
        return checkEdgeSide(window, detail.currentX, this.isEndSide, this.maxEdgeStart);
    }
    onWillStart() {
        this.beforeAnimation(!this._isOpen);
        return this.loadAnimation();
    }
    onStart() {
        if (!this.isAnimating || !this.animation) {
            __chunk_7.assert(false, 'isAnimating has to be true');
            return;
        }
        // the cloned animation should not use an easing curve during seek
        this.animation.reverse(this._isOpen).progressStart();
    }
    onMove(detail) {
        if (!this.isAnimating || !this.animation) {
            __chunk_7.assert(false, 'isAnimating has to be true');
            return;
        }
        const delta = computeDelta(detail.deltaX, this._isOpen, this.isEndSide);
        const stepValue = delta / this.width;
        this.animation.progressStep(stepValue);
    }
    onEnd(detail) {
        if (!this.isAnimating || !this.animation) {
            __chunk_7.assert(false, 'isAnimating has to be true');
            return;
        }
        const isOpen = this._isOpen;
        const isEndSide = this.isEndSide;
        const delta = computeDelta(detail.deltaX, isOpen, isEndSide);
        const width = this.width;
        const stepValue = delta / width;
        const velocity = detail.velocityX;
        const z = width / 2.0;
        const shouldCompleteRight = velocity >= 0 && (velocity > 0.2 || detail.deltaX > z);
        const shouldCompleteLeft = velocity <= 0 && (velocity < -0.2 || detail.deltaX < -z);
        const shouldComplete = isOpen
            ? isEndSide ? shouldCompleteRight : shouldCompleteLeft
            : isEndSide ? shouldCompleteLeft : shouldCompleteRight;
        let shouldOpen = !isOpen && shouldComplete;
        if (isOpen && !shouldComplete) {
            shouldOpen = true;
        }
        const missing = shouldComplete ? 1 - stepValue : stepValue;
        const missingDistance = missing * width;
        let realDur = 0;
        if (missingDistance > 5) {
            const dur = missingDistance / Math.abs(velocity);
            realDur = Math.min(dur, 300);
        }
        this.lastOnEnd = detail.timeStamp;
        this.animation
            .onFinish(() => this.afterAnimation(shouldOpen), {
            clearExistingCallbacks: true,
            oneTimeCallback: true
        })
            .progressEnd(shouldComplete, stepValue, realDur);
    }
    beforeAnimation(shouldOpen) {
        __chunk_7.assert(!this.isAnimating, '_before() should not be called while animating');
        // this places the menu into the correct location before it animates in
        // this css class doesn't actually kick off any animations
        this.el.classList.add(SHOW_MENU);
        if (this.backdropEl) {
            this.backdropEl.classList.add(SHOW_BACKDROP);
        }
        this.blocker.block();
        this.isAnimating = true;
        if (shouldOpen) {
            this.ionWillOpen.emit();
        }
        else {
            this.ionWillClose.emit();
        }
    }
    afterAnimation(isOpen) {
        __chunk_7.assert(this.isAnimating, '_before() should be called while animating');
        // keep opening/closing the menu disabled for a touch more yet
        // only add listeners/css if it's enabled and isOpen
        // and only remove listeners/css if it's not open
        // emit opened/closed events
        this._isOpen = isOpen;
        this.isAnimating = false;
        if (!this._isOpen) {
            this.blocker.unblock();
        }
        if (isOpen) {
            // add css class
            if (this.contentEl) {
                this.contentEl.classList.add(MENU_CONTENT_OPEN);
            }
            // emit open event
            this.ionDidOpen.emit();
        }
        else {
            // remove css classes
            this.el.classList.remove(SHOW_MENU);
            if (this.contentEl) {
                this.contentEl.classList.remove(MENU_CONTENT_OPEN);
            }
            if (this.backdropEl) {
                this.backdropEl.classList.remove(SHOW_BACKDROP);
            }
            // emit close event
            this.ionDidClose.emit();
        }
    }
    updateState() {
        const isActive = this._isActive();
        if (this.gesture) {
            this.gesture.setDisabled(!isActive || !this.swipeGesture);
        }
        // Close menu immediately
        if (!isActive && this._isOpen) {
            // close if this menu is open, and should not be enabled
            this.forceClosing();
        }
        if (!this.disabled && this.menuCtrl) {
            this.menuCtrl._setActiveMenu(this);
        }
        __chunk_7.assert(!this.isAnimating, 'can not be animating');
    }
    forceClosing() {
        __chunk_7.assert(this._isOpen, 'menu cannot be closed');
        this.isAnimating = true;
        const ani = this.animation.reverse(true);
        ani.playSync();
        this.afterAnimation(false);
    }
    hostData() {
        const { isEndSide, type, disabled, isPaneVisible } = this;
        return {
            role: 'navigation',
            class: {
                [`${this.mode}`]: true,
                [`menu-type-${type}`]: true,
                'menu-enabled': !disabled,
                'menu-side-end': isEndSide,
                'menu-side-start': !isEndSide,
                'menu-pane-visible': isPaneVisible
            }
        };
    }
    __stencil_render() {
        return [
            __chunk_1.h("div", { class: "menu-inner", ref: el => this.menuInnerEl = el }, __chunk_1.h("slot", null)),
            __chunk_1.h("ion-backdrop", { ref: el => this.backdropEl = el, class: "menu-backdrop", tappable: false, stopPropagation: false })
        ];
    }
    get el() { return __chunk_1.getElement(this); }
    static get watchers() { return {
        "type": ["typeChanged"],
        "disabled": ["disabledChanged"],
        "side": ["sideChanged"],
        "swipeGesture": ["swipeGestureChanged"]
    }; }
    render() { return __chunk_1.h(__chunk_1.Host, this.hostData(), this.__stencil_render()); }
    static get style() { return ":host{--width:304px;--min-width:auto;--max-width:auto;--height:100%;--min-height:auto;--max-height:auto;--background:var(--ion-background-color,#fff);left:0;right:0;top:0;bottom:0;display:none;position:absolute;contain:strict}:host(.show-menu){display:block}.menu-inner{left:0;right:auto;top:0;bottom:0;-webkit-transform:translate3d(-9999px,0,0);transform:translate3d(-9999px,0,0);display:-ms-flexbox;display:flex;position:absolute;-ms-flex-direction:column;flex-direction:column;-ms-flex-pack:justify;justify-content:space-between;width:var(--width);min-width:var(--min-width);max-width:var(--max-width);height:var(--height);min-height:var(--min-height);max-height:var(--max-height);background:var(--background);contain:strict}:host-context([dir=rtl]) .menu-inner,[dir=rtl] .menu-inner{left:unset;right:unset;left:auto;right:0;-webkit-transform:translate3d(calc(-1 * -9999px),0,0);transform:translate3d(calc(-1 * -9999px),0,0)}:host(.menu-side-start) .menu-inner{--ion-safe-area-right:0px;right:auto;left:0}:host(.menu-side-end) .menu-inner{--ion-safe-area-left:0px;right:0;left:auto}ion-backdrop{display:none;opacity:.01;z-index:-1}\@media (max-width:340px){.menu-inner{--width:264px}}:host(.menu-type-reveal){z-index:0}:host(.menu-type-reveal.show-menu) .menu-inner{-webkit-transform:translateZ(0);transform:translateZ(0)}:host(.menu-type-overlay){z-index:80}:host(.menu-type-overlay) .show-backdrop{display:block;cursor:pointer}:host(.menu-pane-visible) .menu-inner{left:0;right:0;width:auto;-webkit-transform:none!important;transform:none!important;-webkit-box-shadow:none!important;box-shadow:none!important}:host(.menu-pane-visible) ion-backdrop{display:hidden!important}:host(.menu-type-overlay) .menu-inner{-webkit-box-shadow:0 2px 22px 0 rgba(0,0,0,.09),4px 0 16px 0 rgba(0,0,0,.18);box-shadow:0 2px 22px 0 rgba(0,0,0,.09),4px 0 16px 0 rgba(0,0,0,.18)}"; }
}
function computeDelta(deltaX, isOpen, isEndSide) {
    return Math.max(0, isOpen !== isEndSide ? -deltaX : deltaX);
}
function checkEdgeSide(win, posX, isEndSide, maxEdgeStart) {
    if (isEndSide) {
        return posX >= win.innerWidth - maxEdgeStart;
    }
    else {
        return posX <= maxEdgeStart;
    }
}
const SHOW_MENU = 'show-menu';
const SHOW_BACKDROP = 'show-backdrop';
const MENU_CONTENT_OPEN = 'menu-content-open';

class MenuButton {
    constructor(hostRef) {
        __chunk_1.registerInstance(this, hostRef);
        /**
         * If `true`, the user cannot interact with the menu button.
         */
        this.disabled = false;
        /**
         * Automatically hides the menu button when the corresponding menu is not active
         */
        this.autoHide = true;
        /**
         * The type of the button.
         */
        this.type = 'button';
    }
    hostData() {
        const mode = __chunk_1.getIonMode(this);
        const { color, disabled } = this;
        return {
            'aria-disabled': disabled ? 'true' : null,
            class: Object.assign({}, __chunk_5.createColorClasses(color), { [mode]: true, 'button': true, 'menu-button-disabled': disabled, 'ion-activatable': true, 'ion-focusable': true })
        };
    }
    __stencil_render() {
        const mode = __chunk_1.getIonMode(this);
        const menuIcon = __chunk_2.config.get('menuIcon', 'menu');
        const attrs = {
            type: this.type
        };
        return (__chunk_1.h("ion-menu-toggle", { menu: this.menu, autoHide: this.autoHide }, __chunk_1.h("button", Object.assign({}, attrs, { disabled: this.disabled, class: "button-native" }), __chunk_1.h("slot", null, __chunk_1.h("ion-icon", { icon: menuIcon, mode: mode, lazy: false })), mode === 'md' && __chunk_1.h("ion-ripple-effect", { type: "unbounded" }))));
    }
    render() { return __chunk_1.h(__chunk_1.Host, this.hostData(), this.__stencil_render()); }
    static get style() { return ":host{--background:transparent;--color-focused:var(--color);--border-radius:initial;--padding-top:0;--padding-bottom:0;color:var(--color);text-align:center;text-decoration:none;text-overflow:ellipsis;text-transform:none;white-space:nowrap;-webkit-font-kerning:none;font-kerning:none}.button-native{border-radius:var(--border-radius);font-family:inherit;font-size:inherit;font-style:inherit;font-weight:inherit;letter-spacing:inherit;text-decoration:inherit;text-overflow:inherit;text-transform:inherit;text-align:inherit;white-space:inherit;color:inherit;margin-left:0;margin-right:0;margin-top:0;margin-bottom:0;padding-left:var(--padding-start);padding-right:var(--padding-end);padding-top:var(--padding-top);padding-bottom:var(--padding-bottom);-moz-osx-font-smoothing:grayscale;-webkit-font-smoothing:antialiased;display:-ms-flexbox;display:flex;position:relative;-ms-flex-flow:row nowrap;flex-flow:row nowrap;-ms-flex-negative:0;flex-shrink:0;-ms-flex-align:center;align-items:center;-ms-flex-pack:center;justify-content:center;width:100%;height:100%;border:0;outline:none;background:var(--background);line-height:1;cursor:pointer;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;z-index:0;-webkit-appearance:none;-moz-appearance:none;appearance:none}\@supports ((-webkit-margin-start:0) or (margin-inline-start:0)) or (-webkit-margin-start:0){.button-native{padding-left:unset;padding-right:unset;-webkit-padding-start:var(--padding-start);padding-inline-start:var(--padding-start);-webkit-padding-end:var(--padding-end);padding-inline-end:var(--padding-end)}}ion-icon{margin-left:0;margin-right:0;margin-top:0;margin-bottom:0;padding-left:0;padding-right:0;padding-top:0;padding-bottom:0;pointer-events:none}:host(.menu-button-disabled){cursor:default;opacity:.5;pointer-events:none}\@media (any-hover:hover){:host(:hover) .button-native{background:var(--background-hover);color:var(--color-hover)}}:host(.ion-focused) .button-native{background:var(--background-focused);color:var(--color-focused)}:host(.ion-color) .button-native{color:var(--ion-color-base)}:host-context(ion-toolbar:not(.ion-color)){color:var(--ion-toolbar-color,var(--color))}:host{--background-focused:rgba(66,66,66,0.24);--background-hover:rgba(66,66,66,0.08);--border-radius:50%;--color:initial;--padding-start:8px;--padding-end:8px;width:48px;height:48px}ion-icon{font-size:24px}\@media (any-hover:hover){:host(.ion-color:hover) .button-native{background:rgba(var(--ion-color-base-rgb),.08)}}:host(.ion-color.ion-focused) .button-native{background:rgba(var(--ion-color-base-rgb),.24);color:var(--ion-color-base)}"; }
}

/**
 * baseAnimation
 * Base class which is extended by the various types. Each
 * type will provide their own animations for open and close
 * and registers itself with Menu.
 */
function baseAnimation(AnimationC) {
    // https://material.io/guidelines/motion/movement.html#movement-movement-in-out-of-screen-bounds
    // https://material.io/guidelines/motion/duration-easing.html#duration-easing-natural-easing-curves
    // "Apply the sharp curve to items temporarily leaving the screen that may return
    // from the same exit point. When they return, use the deceleration curve. On mobile,
    // this transition typically occurs over 300ms" -- MD Motion Guide
    return Promise.resolve(new AnimationC()
        .easing('cubic-bezier(0.0, 0.0, 0.2, 1)') // Deceleration curve (Entering the screen)
        .easingReverse('cubic-bezier(0.4, 0.0, 0.6, 1)') // Sharp curve (Temporarily leaving the screen)
        .duration(300));
}

const BOX_SHADOW_WIDTH = 8;
/**
 * Menu Overlay Type
 * The menu slides over the content. The content
 * itself, which is under the menu, does not move.
 */
function menuOverlayAnimation(AnimationC, _, menu) {
    let closedX;
    let openedX;
    const width = menu.width + BOX_SHADOW_WIDTH;
    if (menu.isEndSide) {
        // right side
        closedX = width + 'px';
        openedX = '0px';
    }
    else {
        // left side
        closedX = -width + 'px';
        openedX = '0px';
    }
    const menuAnimation = new AnimationC()
        .addElement(menu.menuInnerEl)
        .fromTo('translateX', closedX, openedX);
    const backdropAnimation = new AnimationC()
        .addElement(menu.backdropEl)
        .fromTo('opacity', 0.01, 0.32);
    return baseAnimation(AnimationC).then(animation => {
        return animation.add(menuAnimation)
            .add(backdropAnimation);
    });
}

/**
 * Menu Push Type
 * The content slides over to reveal the menu underneath.
 * The menu itself also slides over to reveal its bad self.
 */
function menuPushAnimation(AnimationC, _, menu) {
    let contentOpenedX;
    let menuClosedX;
    const width = menu.width;
    if (menu.isEndSide) {
        contentOpenedX = -width + 'px';
        menuClosedX = width + 'px';
    }
    else {
        contentOpenedX = width + 'px';
        menuClosedX = -width + 'px';
    }
    const menuAnimation = new AnimationC()
        .addElement(menu.menuInnerEl)
        .fromTo('translateX', menuClosedX, '0px');
    const contentAnimation = new AnimationC()
        .addElement(menu.contentEl)
        .fromTo('translateX', '0px', contentOpenedX);
    const backdropAnimation = new AnimationC()
        .addElement(menu.backdropEl)
        .fromTo('opacity', 0.01, 0.32);
    return baseAnimation(AnimationC).then(animation => {
        return animation.add(menuAnimation)
            .add(backdropAnimation)
            .add(contentAnimation);
    });
}

/**
 * Menu Reveal Type
 * The content slides over to reveal the menu underneath.
 * The menu itself, which is under the content, does not move.
 */
function menuRevealAnimation(AnimationC, _, menu) {
    const openedX = (menu.width * (menu.isEndSide ? -1 : 1)) + 'px';
    const contentOpen = new AnimationC()
        .addElement(menu.contentEl)
        .fromTo('translateX', '0px', openedX);
    return baseAnimation(AnimationC).then(animation => {
        return animation.add(contentOpen);
    });
}

class MenuController {
    constructor(hostRef) {
        __chunk_1.registerInstance(this, hostRef);
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
        const animation = await new Promise(function (resolve) { resolve(require('./index-7a1c7008.js')); })
            .then(mod => mod.create(animationBuilder, null, menuCmp));
        if (!__chunk_2.config.getBoolean('animated', true)) {
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
    static get style() { return ".menu-content{-webkit-transform:translateZ(0);transform:translateZ(0)}.menu-content-open{cursor:pointer;-ms-touch-action:manipulation;touch-action:manipulation;pointer-events:none}.ios .menu-content-reveal{-webkit-box-shadow:-8px 0 42px rgba(0,0,0,.08);box-shadow:-8px 0 42px rgba(0,0,0,.08)}[dir=rtl].ios .menu-content-reveal{-webkit-box-shadow:8px 0 42px rgba(0,0,0,.08);box-shadow:8px 0 42px rgba(0,0,0,.08)}.md .menu-content-push,.md .menu-content-reveal{-webkit-box-shadow:0 2px 22px 0 rgba(0,0,0,.09),4px 0 16px 0 rgba(0,0,0,.18);box-shadow:0 2px 22px 0 rgba(0,0,0,.09),4px 0 16px 0 rgba(0,0,0,.18)}"; }
}

class MenuToggle {
    constructor(hostRef) {
        __chunk_1.registerInstance(this, hostRef);
        this.visible = false;
        /**
         * Automatically hides the content when the corresponding menu is not active.
         *
         * By default, it's `true`. Change it to `false` in order to
         * keep `ion-menu-toggle` always visible regardless the state of the menu.
         */
        this.autoHide = true;
        this.onClick = async () => {
            const menuCtrl = await getMenuController(document);
            if (menuCtrl) {
                const menu = await menuCtrl.get(this.menu);
                if (menu) {
                    menuCtrl.toggle(this.menu);
                }
            }
        };
    }
    componentDidLoad() {
        return this.updateVisibility();
    }
    async updateVisibility() {
        const menuCtrl = await getMenuController(document);
        if (menuCtrl) {
            const menu = await menuCtrl.get(this.menu);
            if (menu && await menu.isActive()) {
                this.visible = true;
                return;
            }
        }
        this.visible = false;
    }
    render() {
        const mode = __chunk_1.getIonMode(this);
        const hidden = this.autoHide && !this.visible;
        return (__chunk_1.h(__chunk_1.Host, { onClick: this.onClick, "aria-hidden": hidden ? 'true' : null, class: {
                [mode]: true,
                'menu-toggle-hidden': hidden,
            } }, __chunk_1.h("slot", null)));
    }
    static get style() { return ":host(.menu-toggle-hidden){display:none}"; }
}
function getMenuController(doc) {
    const menuControllerElement = doc.querySelector('ion-menu-controller');
    if (!menuControllerElement) {
        return Promise.resolve(undefined);
    }
    return menuControllerElement.componentOnReady();
}

exports.ion_menu = Menu;
exports.ion_menu_button = MenuButton;
exports.ion_menu_controller = MenuController;
exports.ion_menu_toggle = MenuToggle;
