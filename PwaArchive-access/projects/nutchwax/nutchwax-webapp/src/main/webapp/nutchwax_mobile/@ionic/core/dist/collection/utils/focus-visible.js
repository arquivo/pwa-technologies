const ION_FOCUSED = 'ion-focused';
const ION_FOCUSABLE = 'ion-focusable';
const FOCUS_KEYS = ['Tab', 'ArrowDown', 'Space', 'Escape', ' ', 'Shift', 'Enter', 'ArrowLeft', 'ArrowRight', 'ArrowUp'];
export function startFocusVisible(doc) {
    let currentFocus = [];
    let keyboardMode = true;
    function setFocus(elements) {
        currentFocus.forEach(el => el.classList.remove(ION_FOCUSED));
        elements.forEach(el => el.classList.add(ION_FOCUSED));
        currentFocus = elements;
    }
    doc.addEventListener('keydown', ev => {
        keyboardMode = FOCUS_KEYS.includes(ev.key);
        if (!keyboardMode) {
            setFocus([]);
        }
    });
    const pointerDown = () => {
        keyboardMode = false;
        setFocus([]);
    };
    doc.addEventListener('focusin', ev => {
        if (keyboardMode && ev.composedPath) {
            const toFocus = ev.composedPath().filter((el) => {
                if (el.classList) {
                    return el.classList.contains(ION_FOCUSABLE);
                }
                return false;
            });
            setFocus(toFocus);
        }
    });
    doc.addEventListener('focusout', () => {
        if (doc.activeElement === doc.body) {
            setFocus([]);
        }
    });
    doc.addEventListener('touchstart', pointerDown);
    doc.addEventListener('mousedown', pointerDown);
}
