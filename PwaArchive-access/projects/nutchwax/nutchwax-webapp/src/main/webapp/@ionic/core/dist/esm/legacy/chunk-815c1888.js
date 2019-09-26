import * as tslib_1 from "tslib";
import { w as writeTask } from './chunk-f257aad1.js';
import { b as LIFECYCLE_WILL_LEAVE, L as LIFECYCLE_WILL_ENTER, a as LIFECYCLE_DID_ENTER, c as LIFECYCLE_DID_LEAVE } from './chunk-94c4865f.js';
var iosTransitionAnimation = function () { return import('./ios.transition-080db2c2.js'); };
var mdTransitionAnimation = function () { return import('./md.transition-9c1a17b7.js'); };
function transition(opts) {
    return new Promise(function (resolve, reject) {
        writeTask(function () {
            beforeTransition(opts);
            runTransition(opts).then(function (result) {
                if (result.animation) {
                    result.animation.destroy();
                }
                afterTransition(opts);
                resolve(result);
            }, function (error) {
                afterTransition(opts);
                reject(error);
            });
        });
    });
}
function beforeTransition(opts) {
    var enteringEl = opts.enteringEl;
    var leavingEl = opts.leavingEl;
    setZIndex(enteringEl, leavingEl, opts.direction);
    if (opts.showGoBack) {
        enteringEl.classList.add('can-go-back');
    }
    else {
        enteringEl.classList.remove('can-go-back');
    }
    setPageHidden(enteringEl, false);
    if (leavingEl) {
        setPageHidden(leavingEl, false);
    }
}
function runTransition(opts) {
    return tslib_1.__awaiter(this, void 0, void 0, function () {
        var animationBuilder, ani;
        return tslib_1.__generator(this, function (_a) {
            switch (_a.label) {
                case 0: return [4 /*yield*/, getAnimationBuilder(opts)];
                case 1:
                    animationBuilder = _a.sent();
                    ani = (animationBuilder)
                        ? animation(animationBuilder, opts)
                        : noAnimation(opts);
                    return [2 /*return*/, ani];
            }
        });
    });
}
function afterTransition(opts) {
    var enteringEl = opts.enteringEl;
    var leavingEl = opts.leavingEl;
    enteringEl.classList.remove('ion-page-invisible');
    if (leavingEl !== undefined) {
        leavingEl.classList.remove('ion-page-invisible');
    }
}
function getAnimationBuilder(opts) {
    return tslib_1.__awaiter(this, void 0, void 0, function () {
        var builder, _a;
        return tslib_1.__generator(this, function (_b) {
            switch (_b.label) {
                case 0:
                    if (!opts.leavingEl || !opts.animated || opts.duration === 0) {
                        return [2 /*return*/, undefined];
                    }
                    if (opts.animationBuilder) {
                        return [2 /*return*/, opts.animationBuilder];
                    }
                    if (!(opts.mode === 'ios')) return [3 /*break*/, 2];
                    return [4 /*yield*/, iosTransitionAnimation()];
                case 1:
                    _a = (_b.sent()).iosTransitionAnimation;
                    return [3 /*break*/, 4];
                case 2: return [4 /*yield*/, mdTransitionAnimation()];
                case 3:
                    _a = (_b.sent()).mdTransitionAnimation;
                    _b.label = 4;
                case 4:
                    builder = _a;
                    return [2 /*return*/, builder];
            }
        });
    });
}
function animation(animationBuilder, opts) {
    return tslib_1.__awaiter(this, void 0, void 0, function () {
        var trans;
        return tslib_1.__generator(this, function (_a) {
            switch (_a.label) {
                case 0: return [4 /*yield*/, waitForReady(opts, true)];
                case 1:
                    _a.sent();
                    return [4 /*yield*/, import('./index-8ec7f6e0.js').then(function (mod) { return mod.create(animationBuilder, opts.baseEl, opts); })];
                case 2:
                    trans = _a.sent();
                    fireWillEvents(opts.enteringEl, opts.leavingEl);
                    return [4 /*yield*/, playTransition(trans, opts)];
                case 3:
                    _a.sent();
                    if (opts.progressCallback) {
                        opts.progressCallback(undefined);
                    }
                    if (trans.hasCompleted) {
                        fireDidEvents(opts.enteringEl, opts.leavingEl);
                    }
                    return [2 /*return*/, {
                            hasCompleted: trans.hasCompleted,
                            animation: trans
                        }];
            }
        });
    });
}
function noAnimation(opts) {
    return tslib_1.__awaiter(this, void 0, void 0, function () {
        var enteringEl, leavingEl;
        return tslib_1.__generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    enteringEl = opts.enteringEl;
                    leavingEl = opts.leavingEl;
                    return [4 /*yield*/, waitForReady(opts, false)];
                case 1:
                    _a.sent();
                    fireWillEvents(enteringEl, leavingEl);
                    fireDidEvents(enteringEl, leavingEl);
                    return [2 /*return*/, {
                            hasCompleted: true
                        }];
            }
        });
    });
}
function waitForReady(opts, defaultDeep) {
    return tslib_1.__awaiter(this, void 0, void 0, function () {
        var deep, promises;
        return tslib_1.__generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    deep = opts.deepWait !== undefined ? opts.deepWait : defaultDeep;
                    promises = deep ? [
                        deepReady(opts.enteringEl),
                        deepReady(opts.leavingEl),
                    ] : [
                        shallowReady(opts.enteringEl),
                        shallowReady(opts.leavingEl),
                    ];
                    return [4 /*yield*/, Promise.all(promises)];
                case 1:
                    _a.sent();
                    return [4 /*yield*/, notifyViewReady(opts.viewIsReady, opts.enteringEl)];
                case 2:
                    _a.sent();
                    return [2 /*return*/];
            }
        });
    });
}
function notifyViewReady(viewIsReady, enteringEl) {
    return tslib_1.__awaiter(this, void 0, void 0, function () {
        return tslib_1.__generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    if (!viewIsReady) return [3 /*break*/, 2];
                    return [4 /*yield*/, viewIsReady(enteringEl)];
                case 1:
                    _a.sent();
                    _a.label = 2;
                case 2: return [2 /*return*/];
            }
        });
    });
}
function playTransition(trans, opts) {
    var progressCallback = opts.progressCallback;
    var promise = new Promise(function (resolve) { return trans.onFinish(resolve); });
    // cool, let's do this, start the transition
    if (progressCallback) {
        // this is a swipe to go back, just get the transition progress ready
        // kick off the swipe animation start
        trans.progressStart();
        progressCallback(trans);
    }
    else {
        // only the top level transition should actually start "play"
        // kick it off and let it play through
        // ******** DOM WRITE ****************
        trans.play();
    }
    // create a callback for when the animation is done
    return promise;
}
function fireWillEvents(enteringEl, leavingEl) {
    lifecycle(leavingEl, LIFECYCLE_WILL_LEAVE);
    lifecycle(enteringEl, LIFECYCLE_WILL_ENTER);
}
function fireDidEvents(enteringEl, leavingEl) {
    lifecycle(enteringEl, LIFECYCLE_DID_ENTER);
    lifecycle(leavingEl, LIFECYCLE_DID_LEAVE);
}
function lifecycle(el, eventName) {
    if (el) {
        var ev = new CustomEvent(eventName, {
            bubbles: false,
            cancelable: false,
        });
        el.dispatchEvent(ev);
    }
}
function shallowReady(el) {
    if (el && el.componentOnReady) {
        return el.componentOnReady();
    }
    return Promise.resolve();
}
function deepReady(el) {
    return tslib_1.__awaiter(this, void 0, void 0, function () {
        var element, stencilEl;
        return tslib_1.__generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    element = el;
                    if (!element) return [3 /*break*/, 4];
                    if (!(element.componentOnReady != null)) return [3 /*break*/, 2];
                    return [4 /*yield*/, element.componentOnReady()];
                case 1:
                    stencilEl = _a.sent();
                    if (stencilEl != null) {
                        return [2 /*return*/];
                    }
                    _a.label = 2;
                case 2: return [4 /*yield*/, Promise.all(Array.from(element.children).map(deepReady))];
                case 3:
                    _a.sent();
                    _a.label = 4;
                case 4: return [2 /*return*/];
            }
        });
    });
}
function setPageHidden(el, hidden) {
    if (hidden) {
        el.setAttribute('aria-hidden', 'true');
        el.classList.add('ion-page-hidden');
    }
    else {
        el.hidden = false;
        el.removeAttribute('aria-hidden');
        el.classList.remove('ion-page-hidden');
    }
}
function setZIndex(enteringEl, leavingEl, direction) {
    if (enteringEl !== undefined) {
        enteringEl.style.zIndex = (direction === 'back')
            ? '99'
            : '101';
    }
    if (leavingEl !== undefined) {
        leavingEl.style.zIndex = '100';
    }
}
export { deepReady as d, lifecycle as l, setPageHidden as s, transition as t };
