'use strict';

Object.defineProperty(exports, '__esModule', { value: true });

const __chunk_2 = require('./chunk-d8847c1c.js');
const __chunk_3 = require('./chunk-e9c8ab68.js');
const __chunk_4 = require('./chunk-6d671032.js');

function setupConfig(config) {
    const win = window;
    const Ionic = win.Ionic;
    if (Ionic && Ionic.config && Ionic.config.constructor.name !== 'Object') {
        console.error('ionic config was already initialized');
        return;
    }
    win.Ionic = win.Ionic || {};
    win.Ionic.config = Object.assign({}, win.Ionic.config, config);
    return win.Ionic.config;
}

exports.getPlatforms = __chunk_2.getPlatforms;
exports.isPlatform = __chunk_2.isPlatform;
exports.LIFECYCLE_DID_ENTER = __chunk_3.LIFECYCLE_DID_ENTER;
exports.LIFECYCLE_DID_LEAVE = __chunk_3.LIFECYCLE_DID_LEAVE;
exports.LIFECYCLE_WILL_ENTER = __chunk_3.LIFECYCLE_WILL_ENTER;
exports.LIFECYCLE_WILL_LEAVE = __chunk_3.LIFECYCLE_WILL_LEAVE;
exports.LIFECYCLE_WILL_UNLOAD = __chunk_3.LIFECYCLE_WILL_UNLOAD;
exports.actionSheetController = __chunk_4.actionSheetController;
exports.alertController = __chunk_4.alertController;
exports.loadingController = __chunk_4.loadingController;
exports.modalController = __chunk_4.modalController;
exports.pickerController = __chunk_4.pickerController;
exports.popoverController = __chunk_4.popoverController;
exports.toastController = __chunk_4.toastController;
exports.setupConfig = setupConfig;
