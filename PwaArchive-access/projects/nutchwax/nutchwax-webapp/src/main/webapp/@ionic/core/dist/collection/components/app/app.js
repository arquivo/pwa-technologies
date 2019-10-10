import { config } from '../../global/config';
import { getIonMode } from '../../global/ionic-global';
import { rIC } from '../../utils/helpers';
import { isPlatform } from '../../utils/platform';
export class App {
    componentDidLoad() {
        rIC(() => {
            if (!config.getBoolean('_testing')) {
                importTapClick();
            }
            importInputShims();
            importStatusTap();
            importHardwareBackButton();
            importFocusVisible();
        });
    }
    hostData() {
        const mode = getIonMode(this);
        return {
            class: {
                [mode]: true,
                'ion-page': true,
                'force-statusbar-padding': config.getBoolean('_forceStatusbarPadding')
            }
        };
    }
    static get is() { return "ion-app"; }
    static get originalStyleUrls() { return {
        "$": ["app.scss"]
    }; }
    static get styleUrls() { return {
        "$": ["app.css"]
    }; }
    static get elementRef() { return "el"; }
}
function importHardwareBackButton() {
    const hardwareBackConfig = config.getBoolean('hardwareBackButton', isPlatform(window, 'hybrid'));
    if (hardwareBackConfig) {
        import('../../utils/hardware-back-button').then(module => module.startHardwareBackButton());
    }
}
function importStatusTap() {
    const statusTap = config.getBoolean('statusTap', isPlatform(window, 'hybrid'));
    if (statusTap) {
        import('../../utils/status-tap').then(module => module.startStatusTap());
    }
}
function importFocusVisible() {
    import('../../utils/focus-visible').then(module => module.startFocusVisible());
}
function importTapClick() {
    import('../../utils/tap-click').then(module => module.startTapClick(config));
}
function importInputShims() {
    const inputShims = config.getBoolean('inputShims', needInputShims());
    if (inputShims) {
        import('../../utils/input-shims/input-shims').then(module => module.startInputShims(config));
    }
}
function needInputShims() {
    return isPlatform(window, 'ios') && isPlatform(window, 'mobile');
}
