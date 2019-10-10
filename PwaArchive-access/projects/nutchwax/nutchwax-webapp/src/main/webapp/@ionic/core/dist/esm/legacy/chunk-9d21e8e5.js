import * as tslib_1 from "tslib";
function hostContext(selector, el) {
    return el.closest(selector) !== null;
}
/**
 * Create the mode and color classes for the component based on the classes passed in
 */
function createColorClasses(color) {
    var _a;
    return (typeof color === 'string' && color.length > 0) ? (_a = {
            'ion-color': true
        },
        _a["ion-color-" + color] = true,
        _a) : undefined;
}
function getClassList(classes) {
    if (classes !== undefined) {
        var array = Array.isArray(classes) ? classes : classes.split(' ');
        return array
            .filter(function (c) { return c != null; })
            .map(function (c) { return c.trim(); })
            .filter(function (c) { return c !== ''; });
    }
    return [];
}
function getClassMap(classes) {
    var map = {};
    getClassList(classes).forEach(function (c) { return map[c] = true; });
    return map;
}
var SCHEME = /^[a-z][a-z0-9+\-.]*:/;
function openURL(url, ev, direction) {
    return tslib_1.__awaiter(this, void 0, void 0, function () {
        var router;
        return tslib_1.__generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    if (!(url != null && url[0] !== '#' && !SCHEME.test(url))) return [3 /*break*/, 2];
                    router = document.querySelector('ion-router');
                    if (!router) return [3 /*break*/, 2];
                    if (ev != null) {
                        ev.preventDefault();
                    }
                    return [4 /*yield*/, router.componentOnReady()];
                case 1:
                    _a.sent();
                    return [2 /*return*/, router.push(url, direction)];
                case 2: return [2 /*return*/, false];
            }
        });
    });
}
export { createColorClasses as c, getClassMap as g, hostContext as h, openURL as o };
