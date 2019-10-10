/**
 * Gets a date value given a format
 * Defaults to the current date if
 * no date given
 */
export declare function getDateValue(date: DatetimeData, format: string): number;
export declare function renderDatetime(template: string, value: DatetimeData | undefined, locale: LocaleData): string | undefined;
export declare function renderTextFormat(format: string, value: any, date: DatetimeData | undefined, locale: LocaleData): string | undefined;
export declare function dateValueRange(format: string, min: DatetimeData, max: DatetimeData): any[];
export declare function dateSortValue(year: number | undefined, month: number | undefined, day: number | undefined, hour?: number, minute?: number): number;
export declare function dateDataSortValue(data: DatetimeData): number;
export declare function daysInMonth(month: number, year: number): number;
export declare function isLeapYear(year: number): boolean;
export declare function parseDate(val: string | undefined | null): DatetimeData | undefined;
/**
 * Converts a valid UTC datetime string
 * To the user's local timezone
 * Note: This is not meant for time strings
 * such as "01:47"
 */
export declare const getLocalDateTime: (dateString?: any) => Date;
export declare function updateDate(existingData: DatetimeData, newData: any): boolean;
export declare function parseTemplate(template: string): string[];
export declare function getValueFromFormat(date: DatetimeData, format: string): any;
export declare function convertFormatToKey(format: string): string | undefined;
export declare function convertDataToISO(data: DatetimeData): string;
/**
 * Use to convert a string of comma separated strings or
 * an array of strings, and clean up any user input
 */
export declare function convertToArrayOfStrings(input: string | string[] | undefined | null, type: string): string[] | undefined;
/**
 * Use to convert a string of comma separated numbers or
 * an array of numbers, and clean up any user input
 */
export declare function convertToArrayOfNumbers(input: any[] | string | number, type: string): number[];
export interface DatetimeData {
    year?: number;
    month?: number;
    day?: number;
    hour?: number;
    minute?: number;
    second?: number;
    millisecond?: number;
    tzOffset?: number;
}
export interface LocaleData {
    monthNames?: string[];
    monthShortNames?: string[];
    dayNames?: string[];
    dayShortNames?: string[];
}
