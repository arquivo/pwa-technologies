'use strict';

Object.defineProperty(exports, '__esModule', { value: true });

const __chunk_1 = require('./chunk-8de9033d.js');
const __chunk_2 = require('./chunk-d8847c1c.js');
require('./chunk-e9c8ab68.js');
const __chunk_5 = require('./chunk-9c9fbda4.js');
const __chunk_7 = require('./chunk-7fe29d8c.js');
const __chunk_9 = require('./chunk-1b4d0f1d.js');
const __chunk_10 = require('./chunk-e77bed2a.js');

/**
 * @deprecated Use `ion-router-link` instead.
 */
class Anchor {
    constructor(hostRef) {
        __chunk_1.registerInstance(this, hostRef);
        /**
         * When using a router, it specifies the transition direction when navigating to
         * another page using `href`.
         */
        this.routerDirection = 'forward';
        this.onClick = (ev) => {
            __chunk_5.openURL(this.href, ev, this.routerDirection);
        };
    }
    componentDidLoad() {
        console.warn('The <ion-anchor> component has been deprecated. Please use an <ion-router-link> if you are using a vanilla JS or Stencil project or an <a> with the Angular router.');
    }
    render() {
        const mode = __chunk_1.getIonMode(this);
        const attrs = {
            href: this.href,
            rel: this.rel
        };
        return (__chunk_1.h(__chunk_1.Host, { onClick: this.onClick, class: Object.assign({}, __chunk_5.createColorClasses(this.color), { [mode]: true, 'ion-activatable': true }) }, __chunk_1.h("a", Object.assign({}, attrs), __chunk_1.h("slot", null))));
    }
    static get style() { return ":host{--background:transparent;--color:var(--ion-color-primary,#3880ff);background:var(--background);color:var(--color)}:host(.ion-color){color:var(--ion-color-base)}a{font-family:inherit;font-size:inherit;font-style:inherit;font-weight:inherit;letter-spacing:inherit;text-decoration:inherit;text-overflow:inherit;text-transform:inherit;text-align:inherit;white-space:inherit;color:inherit}"; }
}

class Route {
    constructor(hostRef) {
        __chunk_1.registerInstance(this, hostRef);
        /**
         * Relative path that needs to match in order for this route to apply.
         *
         * Accepts paths similar to expressjs so that you can define parameters
         * in the url /foo/:bar where bar would be available in incoming props.
         */
        this.url = '';
        this.ionRouteDataChanged = __chunk_1.createEvent(this, "ionRouteDataChanged", 7);
    }
    onUpdate(newValue) {
        this.ionRouteDataChanged.emit(newValue);
    }
    onComponentProps(newValue, oldValue) {
        if (newValue === oldValue) {
            return;
        }
        const keys1 = newValue ? Object.keys(newValue) : [];
        const keys2 = oldValue ? Object.keys(oldValue) : [];
        if (keys1.length !== keys2.length) {
            this.onUpdate(newValue);
            return;
        }
        for (const key of keys1) {
            if (newValue[key] !== oldValue[key]) {
                this.onUpdate(newValue);
                return;
            }
        }
    }
    componentDidLoad() {
        this.ionRouteDataChanged.emit();
    }
    componentDidUnload() {
        this.ionRouteDataChanged.emit();
    }
    static get watchers() { return {
        "url": ["onUpdate"],
        "component": ["onUpdate"],
        "componentProps": ["onComponentProps"]
    }; }
}

class RouteRedirect {
    constructor(hostRef) {
        __chunk_1.registerInstance(this, hostRef);
        this.ionRouteRedirectChanged = __chunk_1.createEvent(this, "ionRouteRedirectChanged", 7);
    }
    propDidChange() {
        this.ionRouteRedirectChanged.emit();
    }
    componentDidLoad() {
        this.ionRouteRedirectChanged.emit();
    }
    componentDidUnload() {
        this.ionRouteRedirectChanged.emit();
    }
    static get watchers() { return {
        "from": ["propDidChange"],
        "to": ["propDidChange"]
    }; }
}

const ROUTER_INTENT_NONE = 'root';
const ROUTER_INTENT_FORWARD = 'forward';
const ROUTER_INTENT_BACK = 'back';

function generatePath(segments) {
    const path = segments
        .filter(s => s.length > 0)
        .join('/');
    return '/' + path;
}
function chainToPath(chain) {
    const path = [];
    for (const route of chain) {
        for (const segment of route.path) {
            if (segment[0] === ':') {
                const param = route.params && route.params[segment.slice(1)];
                if (!param) {
                    return null;
                }
                path.push(param);
            }
            else if (segment !== '') {
                path.push(segment);
            }
        }
    }
    return path;
}
function writePath(history, root, useHash, path, direction, state) {
    let url = generatePath([
        ...parsePath(root),
        ...path
    ]);
    if (useHash) {
        url = '#' + url;
    }
    if (direction === ROUTER_INTENT_FORWARD) {
        history.pushState(state, '', url);
    }
    else {
        history.replaceState(state, '', url);
    }
}
function removePrefix(prefix, path) {
    if (prefix.length > path.length) {
        return null;
    }
    if (prefix.length <= 1 && prefix[0] === '') {
        return path;
    }
    for (let i = 0; i < prefix.length; i++) {
        if (prefix[i].length > 0 && prefix[i] !== path[i]) {
            return null;
        }
    }
    if (path.length === prefix.length) {
        return [''];
    }
    return path.slice(prefix.length);
}
function readPath(loc, root, useHash) {
    let pathname = loc.pathname;
    if (useHash) {
        const hash = loc.hash;
        pathname = (hash[0] === '#')
            ? hash.slice(1)
            : '';
    }
    const prefix = parsePath(root);
    const path = parsePath(pathname);
    return removePrefix(prefix, path);
}
function parsePath(path) {
    if (path == null) {
        return [''];
    }
    const segments = path.split('/')
        .map(s => s.trim())
        .filter(s => s.length > 0);
    if (segments.length === 0) {
        return [''];
    }
    else {
        return segments;
    }
}

function printRoutes(routes) {
    console.group(`[ion-core] ROUTES[${routes.length}]`);
    for (const chain of routes) {
        const path = [];
        chain.forEach(r => path.push(...r.path));
        const ids = chain.map(r => r.id);
        console.debug(`%c ${generatePath(path)}`, 'font-weight: bold; padding-left: 20px', '=>\t', `(${ids.join(', ')})`);
    }
    console.groupEnd();
}
function printRedirects(redirects) {
    console.group(`[ion-core] REDIRECTS[${redirects.length}]`);
    for (const redirect of redirects) {
        if (redirect.to) {
            console.debug('FROM: ', `$c ${generatePath(redirect.from)}`, 'font-weight: bold', ' TO: ', `$c ${generatePath(redirect.to)}`, 'font-weight: bold');
        }
    }
    console.groupEnd();
}

async function writeNavState(root, chain, direction, index, changed = false) {
    try {
        // find next navigation outlet in the DOM
        const outlet = searchNavNode(root);
        // make sure we can continue interacting the DOM, otherwise abort
        if (index >= chain.length || !outlet) {
            return changed;
        }
        await outlet.componentOnReady();
        const route = chain[index];
        const result = await outlet.setRouteId(route.id, route.params, direction);
        // if the outlet changed the page, reset navigation to neutral (no direction)
        // this means nested outlets will not animate
        if (result.changed) {
            direction = ROUTER_INTENT_NONE;
            changed = true;
        }
        // recursively set nested outlets
        changed = await writeNavState(result.element, chain, direction, index + 1, changed);
        // once all nested outlets are visible let's make the parent visible too,
        // using markVisible prevents flickering
        if (result.markVisible) {
            await result.markVisible();
        }
        return changed;
    }
    catch (e) {
        console.error(e);
        return false;
    }
}
async function readNavState(root) {
    const ids = [];
    let outlet;
    let node = root;
    // tslint:disable-next-line:no-constant-condition
    while (true) {
        outlet = searchNavNode(node);
        if (outlet) {
            const id = await outlet.getRouteId();
            if (id) {
                node = id.element;
                id.element = undefined;
                ids.push(id);
            }
            else {
                break;
            }
        }
        else {
            break;
        }
    }
    return { ids, outlet };
}
function waitUntilNavNode() {
    if (searchNavNode(document.body)) {
        return Promise.resolve();
    }
    return new Promise(resolve => {
        window.addEventListener('ionNavWillLoad', resolve, { once: true });
    });
}
const QUERY = ':not([no-router]) ion-nav, :not([no-router]) ion-tabs, :not([no-router]) ion-router-outlet';
function searchNavNode(root) {
    if (!root) {
        return undefined;
    }
    if (root.matches(QUERY)) {
        return root;
    }
    const outlet = root.querySelector(QUERY);
    return outlet ? outlet : undefined;
}

function matchesRedirect(input, route) {
    const { from, to } = route;
    if (to === undefined) {
        return false;
    }
    if (from.length > input.length) {
        return false;
    }
    for (let i = 0; i < from.length; i++) {
        const expected = from[i];
        if (expected === '*') {
            return true;
        }
        if (expected !== input[i]) {
            return false;
        }
    }
    return from.length === input.length;
}
function routeRedirect(path, routes) {
    return routes.find(route => matchesRedirect(path, route));
}
function matchesIDs(ids, chain) {
    const len = Math.min(ids.length, chain.length);
    let i = 0;
    for (; i < len; i++) {
        if (ids[i].toLowerCase() !== chain[i].id) {
            break;
        }
    }
    return i;
}
function matchesPath(inputPath, chain) {
    const segments = new RouterSegments(inputPath);
    let matchesDefault = false;
    let allparams;
    for (let i = 0; i < chain.length; i++) {
        const path = chain[i].path;
        if (path[0] === '') {
            matchesDefault = true;
        }
        else {
            for (const segment of path) {
                const data = segments.next();
                // data param
                if (segment[0] === ':') {
                    if (data === '') {
                        return null;
                    }
                    allparams = allparams || [];
                    const params = allparams[i] || (allparams[i] = {});
                    params[segment.slice(1)] = data;
                }
                else if (data !== segment) {
                    return null;
                }
            }
            matchesDefault = false;
        }
    }
    const matches = (matchesDefault)
        ? matchesDefault === (segments.next() === '')
        : true;
    if (!matches) {
        return null;
    }
    if (allparams) {
        return chain.map((route, i) => ({
            id: route.id,
            path: route.path,
            params: mergeParams(route.params, allparams[i])
        }));
    }
    return chain;
}
function mergeParams(a, b) {
    if (!a && b) {
        return b;
    }
    else if (a && !b) {
        return a;
    }
    else if (a && b) {
        return Object.assign({}, a, b);
    }
    return undefined;
}
function routerIDsToChain(ids, chains) {
    let match = null;
    let maxMatches = 0;
    const plainIDs = ids.map(i => i.id);
    for (const chain of chains) {
        const score = matchesIDs(plainIDs, chain);
        if (score > maxMatches) {
            match = chain;
            maxMatches = score;
        }
    }
    if (match) {
        return match.map((route, i) => ({
            id: route.id,
            path: route.path,
            params: mergeParams(route.params, ids[i] && ids[i].params)
        }));
    }
    return null;
}
function routerPathToChain(path, chains) {
    let match = null;
    let matches = 0;
    for (const chain of chains) {
        const matchedChain = matchesPath(path, chain);
        if (matchedChain !== null) {
            const score = computePriority(matchedChain);
            if (score > matches) {
                matches = score;
                match = matchedChain;
            }
        }
    }
    return match;
}
function computePriority(chain) {
    let score = 1;
    let level = 1;
    for (const route of chain) {
        for (const path of route.path) {
            if (path[0] === ':') {
                score += Math.pow(1, level);
            }
            else if (path !== '') {
                score += Math.pow(2, level);
            }
            level++;
        }
    }
    return score;
}
class RouterSegments {
    constructor(path) {
        this.path = path.slice();
    }
    next() {
        if (this.path.length > 0) {
            return this.path.shift();
        }
        return '';
    }
}

function readRedirects(root) {
    return Array.from(root.children)
        .filter(el => el.tagName === 'ION-ROUTE-REDIRECT')
        .map(el => {
        const to = readProp(el, 'to');
        return {
            from: parsePath(readProp(el, 'from')),
            to: to == null ? undefined : parsePath(to),
        };
    });
}
function readRoutes(root) {
    return flattenRouterTree(readRouteNodes(root));
}
function readRouteNodes(root, node = root) {
    return Array.from(node.children)
        .filter(el => el.tagName === 'ION-ROUTE' && el.component)
        .map(el => {
        const component = readProp(el, 'component');
        if (component == null) {
            throw new Error('component missing in ion-route');
        }
        return {
            path: parsePath(readProp(el, 'url')),
            id: component.toLowerCase(),
            params: el.componentProps,
            children: readRouteNodes(root, el)
        };
    });
}
function readProp(el, prop) {
    if (prop in el) {
        return el[prop];
    }
    if (el.hasAttribute(prop)) {
        return el.getAttribute(prop);
    }
    return null;
}
function flattenRouterTree(nodes) {
    const routes = [];
    for (const node of nodes) {
        flattenNode([], routes, node);
    }
    return routes;
}
function flattenNode(chain, routes, node) {
    const s = chain.slice();
    s.push({
        id: node.id,
        path: node.path,
        params: node.params
    });
    if (node.children.length === 0) {
        routes.push(s);
        return;
    }
    for (const sub of node.children) {
        flattenNode(s, routes, sub);
    }
}

class Router {
    constructor(hostRef) {
        __chunk_1.registerInstance(this, hostRef);
        this.previousPath = null;
        this.busy = false;
        this.state = 0;
        this.lastState = 0;
        /**
         * By default `ion-router` will match the routes at the root path ("/").
         * That can be changed when
         *
         */
        this.root = '/';
        /**
         * The router can work in two "modes":
         * - With hash: `/index.html#/path/to/page`
         * - Without hash: `/path/to/page`
         *
         * Using one or another might depend in the requirements of your app and/or where it's deployed.
         *
         * Usually "hash-less" navigation works better for SEO and it's more user friendly too, but it might
         * requires additional server-side configuration in order to properly work.
         *
         * On the otherside hash-navigation is much easier to deploy, it even works over the file protocol.
         *
         * By default, this property is `true`, change to `false` to allow hash-less URLs.
         */
        this.useHash = true;
        this.ionRouteWillChange = __chunk_1.createEvent(this, "ionRouteWillChange", 7);
        this.ionRouteDidChange = __chunk_1.createEvent(this, "ionRouteDidChange", 7);
    }
    async componentWillLoad() {
        console.debug('[ion-router] router will load');
        await waitUntilNavNode();
        console.debug('[ion-router] found nav');
        await this.onRoutesChanged();
    }
    componentDidLoad() {
        window.addEventListener('ionRouteRedirectChanged', __chunk_7.debounce(this.onRedirectChanged.bind(this), 10));
        window.addEventListener('ionRouteDataChanged', __chunk_7.debounce(this.onRoutesChanged.bind(this), 100));
    }
    onPopState() {
        const direction = this.historyDirection();
        const path = this.getPath();
        console.debug('[ion-router] URL changed -> update nav', path, direction);
        return this.writeNavStateRoot(path, direction);
    }
    onBackButton(ev) {
        ev.detail.register(0, () => this.back());
    }
    /**
     * Navigate to the specified URL.
     *
     * @param url The url to navigate to.
     * @param direction The direction of the animation. Defaults to `"forward"`.
     */
    push(url, direction = 'forward') {
        if (url.startsWith('.')) {
            url = (new URL(url, window.location.href)).pathname;
        }
        console.debug('[ion-router] URL pushed -> updating nav', url, direction);
        const path = parsePath(url);
        this.setPath(path, direction);
        return this.writeNavStateRoot(path, direction);
    }
    /**
     * Go back to previous page in the window.history.
     */
    back() {
        window.history.back();
        return Promise.resolve(this.waitPromise);
    }
    /** @internal */
    async printDebug() {
        console.debug('CURRENT PATH', this.getPath());
        console.debug('PREVIOUS PATH', this.previousPath);
        printRoutes(readRoutes(this.el));
        printRedirects(readRedirects(this.el));
    }
    /** @internal */
    async navChanged(direction) {
        if (this.busy) {
            console.warn('[ion-router] router is busy, navChanged was cancelled');
            return false;
        }
        const { ids, outlet } = await readNavState(window.document.body);
        const routes = readRoutes(this.el);
        const chain = routerIDsToChain(ids, routes);
        if (!chain) {
            console.warn('[ion-router] no matching URL for ', ids.map(i => i.id));
            return false;
        }
        const path = chainToPath(chain);
        if (!path) {
            console.warn('[ion-router] router could not match path because some required param is missing');
            return false;
        }
        console.debug('[ion-router] nav changed -> update URL', ids, path);
        this.setPath(path, direction);
        await this.safeWriteNavState(outlet, chain, ROUTER_INTENT_NONE, path, null, ids.length);
        return true;
    }
    onRedirectChanged() {
        const path = this.getPath();
        if (path && routeRedirect(path, readRedirects(this.el))) {
            this.writeNavStateRoot(path, ROUTER_INTENT_NONE);
        }
    }
    onRoutesChanged() {
        return this.writeNavStateRoot(this.getPath(), ROUTER_INTENT_NONE);
    }
    historyDirection() {
        const win = window;
        if (win.history.state === null) {
            this.state++;
            win.history.replaceState(this.state, win.document.title, win.document.location && win.document.location.href);
        }
        const state = win.history.state;
        const lastState = this.lastState;
        this.lastState = state;
        if (state > lastState) {
            return ROUTER_INTENT_FORWARD;
        }
        else if (state < lastState) {
            return ROUTER_INTENT_BACK;
        }
        else {
            return ROUTER_INTENT_NONE;
        }
    }
    async writeNavStateRoot(path, direction) {
        if (!path) {
            console.error('[ion-router] URL is not part of the routing set');
            return false;
        }
        // lookup redirect rule
        const redirects = readRedirects(this.el);
        const redirect = routeRedirect(path, redirects);
        let redirectFrom = null;
        if (redirect) {
            this.setPath(redirect.to, direction);
            redirectFrom = redirect.from;
            path = redirect.to;
        }
        // lookup route chain
        const routes = readRoutes(this.el);
        const chain = routerPathToChain(path, routes);
        if (!chain) {
            console.error('[ion-router] the path does not match any route');
            return false;
        }
        // write DOM give
        return this.safeWriteNavState(document.body, chain, direction, path, redirectFrom);
    }
    async safeWriteNavState(node, chain, direction, path, redirectFrom, index = 0) {
        const unlock = await this.lock();
        let changed = false;
        try {
            changed = await this.writeNavState(node, chain, direction, path, redirectFrom, index);
        }
        catch (e) {
            console.error(e);
        }
        unlock();
        return changed;
    }
    async lock() {
        const p = this.waitPromise;
        let resolve;
        this.waitPromise = new Promise(r => resolve = r);
        if (p !== undefined) {
            await p;
        }
        return resolve;
    }
    async writeNavState(node, chain, direction, path, redirectFrom, index = 0) {
        if (this.busy) {
            console.warn('[ion-router] router is busy, transition was cancelled');
            return false;
        }
        this.busy = true;
        // generate route event and emit will change
        const routeEvent = this.routeChangeEvent(path, redirectFrom);
        if (routeEvent) {
            this.ionRouteWillChange.emit(routeEvent);
        }
        const changed = await writeNavState(node, chain, direction, index);
        this.busy = false;
        if (changed) {
            console.debug('[ion-router] route changed', path);
        }
        // emit did change
        if (routeEvent) {
            this.ionRouteDidChange.emit(routeEvent);
        }
        return changed;
    }
    setPath(path, direction) {
        this.state++;
        writePath(window.history, this.root, this.useHash, path, direction, this.state);
    }
    getPath() {
        return readPath(window.location, this.root, this.useHash);
    }
    routeChangeEvent(path, redirectFromPath) {
        const from = this.previousPath;
        const to = generatePath(path);
        this.previousPath = to;
        if (to === from) {
            return null;
        }
        const redirectedFrom = redirectFromPath ? generatePath(redirectFromPath) : null;
        return {
            from,
            redirectedFrom,
            to,
        };
    }
    get el() { return __chunk_1.getElement(this); }
}

class RouterLink {
    constructor(hostRef) {
        __chunk_1.registerInstance(this, hostRef);
        /**
         * When using a router, it specifies the transition direction when navigating to
         * another page using `href`.
         */
        this.routerDirection = 'forward';
        this.onClick = (ev) => {
            __chunk_5.openURL(this.href, ev, this.routerDirection);
        };
    }
    render() {
        const mode = __chunk_1.getIonMode(this);
        const attrs = {
            href: this.href,
            rel: this.rel
        };
        return (__chunk_1.h(__chunk_1.Host, { onClick: this.onClick, class: Object.assign({}, __chunk_5.createColorClasses(this.color), { [mode]: true, 'ion-activatable': true }) }, __chunk_1.h("a", Object.assign({}, attrs), __chunk_1.h("slot", null))));
    }
    static get style() { return ":host{--background:transparent;--color:var(--ion-color-primary,#3880ff);background:var(--background);color:var(--color)}:host(.ion-color){color:var(--ion-color-base)}a{font-family:inherit;font-size:inherit;font-style:inherit;font-weight:inherit;letter-spacing:inherit;text-decoration:inherit;text-overflow:inherit;text-transform:inherit;text-align:inherit;white-space:inherit;color:inherit}"; }
}

class RouterOutlet {
    constructor(hostRef) {
        __chunk_1.registerInstance(this, hostRef);
        /**
         * If `true`, the router-outlet should animate the transition of components.
         */
        this.animated = true;
        this.ionNavWillLoad = __chunk_1.createEvent(this, "ionNavWillLoad", 7);
        this.ionNavWillChange = __chunk_1.createEvent(this, "ionNavWillChange", 3);
        this.ionNavDidChange = __chunk_1.createEvent(this, "ionNavDidChange", 3);
    }
    swipeHandlerChanged() {
        if (this.gesture) {
            this.gesture.setDisabled(this.swipeHandler === undefined);
        }
    }
    componentWillLoad() {
        this.ionNavWillLoad.emit();
    }
    async componentDidLoad() {
        this.gesture = (await new Promise(function (resolve) { resolve(require('./swipe-back-fb8aac3a.js')); })).createSwipeBackGesture(this.el, () => !!this.swipeHandler && this.swipeHandler.canStart(), () => this.swipeHandler && this.swipeHandler.onStart(), step => this.ani && this.ani.progressStep(step), (shouldComplete, step, dur) => {
            if (this.ani) {
                this.ani.progressEnd(shouldComplete, step, dur);
            }
            if (this.swipeHandler) {
                this.swipeHandler.onEnd(shouldComplete);
            }
        });
        this.swipeHandlerChanged();
    }
    componentDidUnload() {
        this.activeEl = this.activeComponent = undefined;
        if (this.gesture) {
            this.gesture.destroy();
            this.gesture = undefined;
        }
    }
    /** @internal */
    async commit(enteringEl, leavingEl, opts) {
        const unlock = await this.lock();
        let changed = false;
        try {
            changed = await this.transition(enteringEl, leavingEl, opts);
        }
        catch (e) {
            console.error(e);
        }
        unlock();
        return changed;
    }
    /** @internal */
    async setRouteId(id, params, direction) {
        const changed = await this.setRoot(id, params, {
            duration: direction === 'root' ? 0 : undefined,
            direction: direction === 'back' ? 'back' : 'forward',
        });
        return {
            changed,
            element: this.activeEl
        };
    }
    /** @internal */
    async getRouteId() {
        const active = this.activeEl;
        return active ? {
            id: active.tagName,
            element: active,
        } : undefined;
    }
    async setRoot(component, params, opts) {
        if (this.activeComponent === component) {
            return false;
        }
        // attach entering view to DOM
        const leavingEl = this.activeEl;
        const enteringEl = await __chunk_9.attachComponent(this.delegate, this.el, component, ['ion-page', 'ion-page-invisible'], params);
        this.activeComponent = component;
        this.activeEl = enteringEl;
        // commit animation
        await this.commit(enteringEl, leavingEl, opts);
        await __chunk_9.detachComponent(this.delegate, leavingEl);
        return true;
    }
    async transition(enteringEl, leavingEl, opts = {}) {
        if (leavingEl === enteringEl) {
            return false;
        }
        // emit nav will change event
        this.ionNavWillChange.emit();
        const mode = __chunk_1.getIonMode(this);
        const { el } = this;
        const animated = this.animated && __chunk_2.config.getBoolean('animated', true);
        const animationBuilder = this.animation || opts.animationBuilder || __chunk_2.config.get('navAnimation');
        await __chunk_10.transition(Object.assign({ mode,
            animated,
            animationBuilder,
            enteringEl,
            leavingEl, baseEl: el, progressCallback: (opts.progressAnimation
                ? ani => this.ani = ani
                : undefined) }, opts));
        // emit nav changed event
        this.ionNavDidChange.emit();
        return true;
    }
    async lock() {
        const p = this.waitPromise;
        let resolve;
        this.waitPromise = new Promise(r => resolve = r);
        if (p !== undefined) {
            await p;
        }
        return resolve;
    }
    render() {
        return (__chunk_1.h("slot", null));
    }
    get el() { return __chunk_1.getElement(this); }
    static get watchers() { return {
        "swipeHandler": ["swipeHandlerChanged"]
    }; }
    static get style() { return ":host{left:0;right:0;top:0;bottom:0;position:absolute;contain:layout size style;overflow:hidden;z-index:0}"; }
}

exports.ion_anchor = Anchor;
exports.ion_route = Route;
exports.ion_route_redirect = RouteRedirect;
exports.ion_router = Router;
exports.ion_router_link = RouterLink;
exports.ion_router_outlet = RouterOutlet;
