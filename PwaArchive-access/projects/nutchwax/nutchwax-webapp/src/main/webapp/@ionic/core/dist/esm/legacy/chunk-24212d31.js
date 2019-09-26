var _this = this;
import * as tslib_1 from "tslib";
import { b as config } from './chunk-1074393c.js';
var lastId = 0;
var createController = function (tagName) {
    return {
        create: function (options) {
            return createOverlay(tagName, options);
        },
        dismiss: function (data, role, id) {
            return dismissOverlay(document, data, role, tagName, id);
        },
        getTop: function () {
            return tslib_1.__awaiter(this, void 0, void 0, function () {
                return tslib_1.__generator(this, function (_a) {
                    return [2 /*return*/, getOverlay(document, tagName)];
                });
            });
        }
    };
};
var alertController = /*@__PURE__*/ createController('ion-alert');
var actionSheetController = /*@__PURE__*/ createController('ion-action-sheet');
var loadingController = /*@__PURE__*/ createController('ion-loading');
var modalController = /*@__PURE__*/ createController('ion-modal');
var pickerController = /*@__PURE__*/ createController('ion-picker');
var popoverController = /*@__PURE__*/ createController('ion-popover');
var toastController = /*@__PURE__*/ createController('ion-toast');
var createOverlay = function (tagName, opts) {
    return customElements.whenDefined(tagName).then(function () {
        var doc = document;
        var element = doc.createElement(tagName);
        connectListeners(doc);
        // convert the passed in overlay options into props
        // that get passed down into the new overlay
        Object.assign(element, opts);
        element.classList.add('overlay-hidden');
        var overlayIndex = lastId++;
        element.overlayIndex = overlayIndex;
        if (!element.hasAttribute('id')) {
            element.id = "ion-overlay-" + overlayIndex;
        }
        // append the overlay element to the document body
        getAppRoot(doc).appendChild(element);
        return element.componentOnReady();
    });
};
var connectListeners = function (doc) {
    if (lastId === 0) {
        lastId = 1;
        // trap focus inside overlays
        doc.addEventListener('focusin', function (ev) {
            var lastOverlay = getOverlay(doc);
            if (lastOverlay && lastOverlay.backdropDismiss && !isDescendant(lastOverlay, ev.target)) {
                var firstInput = lastOverlay.querySelector('input,button');
                if (firstInput) {
                    firstInput.focus();
                }
            }
        });
        // handle back-button click
        doc.addEventListener('ionBackButton', function (ev) {
            var lastOverlay = getOverlay(doc);
            if (lastOverlay && lastOverlay.backdropDismiss) {
                ev.detail.register(100, function () {
                    return lastOverlay.dismiss(undefined, BACKDROP);
                });
            }
        });
        // handle ESC to close overlay
        doc.addEventListener('keyup', function (ev) {
            if (ev.key === 'Escape') {
                var lastOverlay = getOverlay(doc);
                if (lastOverlay && lastOverlay.backdropDismiss) {
                    lastOverlay.dismiss(undefined, BACKDROP);
                }
            }
        });
    }
};
var dismissOverlay = function (doc, data, role, overlayTag, id) {
    var overlay = getOverlay(doc, overlayTag, id);
    if (!overlay) {
        return Promise.reject('overlay does not exist');
    }
    return overlay.dismiss(data, role);
};
var getOverlays = function (doc, overlayTag) {
    var overlays = Array.from(getAppRoot(doc).children).filter(function (c) { return c.overlayIndex > 0; });
    if (overlayTag === undefined) {
        return overlays;
    }
    overlayTag = overlayTag.toUpperCase();
    return overlays.filter(function (c) { return c.tagName === overlayTag; });
};
var getOverlay = function (doc, overlayTag, id) {
    var overlays = getOverlays(doc, overlayTag);
    return (id === undefined)
        ? overlays[overlays.length - 1]
        : overlays.find(function (o) { return o.id === id; });
};
var present = function (overlay, name, iosEnterAnimation, mdEnterAnimation, opts) { return tslib_1.__awaiter(_this, void 0, void 0, function () {
    var animationBuilder, completed;
    return tslib_1.__generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                if (overlay.presented) {
                    return [2 /*return*/];
                }
                overlay.presented = true;
                overlay.willPresent.emit();
                animationBuilder = (overlay.enterAnimation)
                    ? overlay.enterAnimation
                    : config.get(name, overlay.mode === 'ios' ? iosEnterAnimation : mdEnterAnimation);
                return [4 /*yield*/, overlayAnimation(overlay, animationBuilder, overlay.el, opts)];
            case 1:
                completed = _a.sent();
                if (completed) {
                    overlay.didPresent.emit();
                }
                return [2 /*return*/];
        }
    });
}); };
var dismiss = function (overlay, data, role, name, iosLeaveAnimation, mdLeaveAnimation, opts) { return tslib_1.__awaiter(_this, void 0, void 0, function () {
    var animationBuilder, err_1;
    return tslib_1.__generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                if (!overlay.presented) {
                    return [2 /*return*/, false];
                }
                overlay.presented = false;
                _a.label = 1;
            case 1:
                _a.trys.push([1, 3, , 4]);
                overlay.willDismiss.emit({ data: data, role: role });
                animationBuilder = (overlay.leaveAnimation)
                    ? overlay.leaveAnimation
                    : config.get(name, overlay.mode === 'ios' ? iosLeaveAnimation : mdLeaveAnimation);
                return [4 /*yield*/, overlayAnimation(overlay, animationBuilder, overlay.el, opts)];
            case 2:
                _a.sent();
                overlay.didDismiss.emit({ data: data, role: role });
                return [3 /*break*/, 4];
            case 3:
                err_1 = _a.sent();
                console.error(err_1);
                return [3 /*break*/, 4];
            case 4:
                overlay.el.remove();
                return [2 /*return*/, true];
        }
    });
}); };
var getAppRoot = function (doc) {
    return doc.querySelector('ion-app') || doc.body;
};
var overlayAnimation = function (overlay, animationBuilder, baseEl, opts) { return tslib_1.__awaiter(_this, void 0, void 0, function () {
    var aniRoot, animation, hasCompleted;
    return tslib_1.__generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                if (overlay.animation) {
                    overlay.animation.destroy();
                    overlay.animation = undefined;
                    return [2 /*return*/, false];
                }
                // Make overlay visible in case it's hidden
                baseEl.classList.remove('overlay-hidden');
                aniRoot = baseEl.shadowRoot || overlay.el;
                return [4 /*yield*/, import('./index-8ec7f6e0.js').then(function (mod) { return mod.create(animationBuilder, aniRoot, opts); })];
            case 1:
                animation = _a.sent();
                overlay.animation = animation;
                if (!overlay.animated || !config.getBoolean('animated', true)) {
                    animation.duration(0);
                }
                if (overlay.keyboardClose) {
                    animation.beforeAddWrite(function () {
                        var activeElement = baseEl.ownerDocument.activeElement;
                        if (activeElement && activeElement.matches('input, ion-input, ion-textarea')) {
                            activeElement.blur();
                        }
                    });
                }
                return [4 /*yield*/, animation.playAsync()];
            case 2:
                _a.sent();
                hasCompleted = animation.hasCompleted;
                animation.destroy();
                overlay.animation = undefined;
                return [2 /*return*/, hasCompleted];
        }
    });
}); };
var eventMethod = function (element, eventName) {
    var resolve;
    var promise = new Promise(function (r) { return resolve = r; });
    onceEvent(element, eventName, function (event) {
        resolve(event.detail);
    });
    return promise;
};
var onceEvent = function (element, eventName, callback) {
    var handler = function (ev) {
        element.removeEventListener(eventName, handler);
        callback(ev);
    };
    element.addEventListener(eventName, handler);
};
var isCancel = function (role) {
    return role === 'cancel' || role === BACKDROP;
};
var isDescendant = function (parent, child) {
    while (child) {
        if (child === parent) {
            return true;
        }
        child = child.parentElement;
    }
    return false;
};
var BACKDROP = 'backdrop';
export { BACKDROP as B, alertController as a, actionSheetController as b, popoverController as c, present as d, dismiss as e, eventMethod as f, createOverlay as g, dismissOverlay as h, isCancel as i, getOverlay as j, loadingController as l, modalController as m, pickerController as p, toastController as t };
