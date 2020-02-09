/******************************************************************************
 * Copyright © 2013-2016 The Nxt Core Developers.                             *
 * Copyright © 2016-2018 Jelurida IP B.V.                                     *
 *                                                                            *
 * See the LICENSE.txt file at the top-level directory of this distribution   *
 * for licensing information.                                                 *
 *                                                                            *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,*
 * no part of the Nxt software, including this file, may be copied, modified, *
 * propagated, or distributed except according to the terms contained in the  *
 * LICENSE.txt file.                                                          *
 *                                                                            *
 * Removal or modification of this copyright notice is prohibited.            *
 *                                                                            *
 ******************************************************************************/

var NRS = (function (NRS) {
    var currentLocale = {};

    NRS.getLocaleList = function() {
        return SORTED_LOCALE_DATA;
    };
    
    NRS.getLocaleName = function(locale) {
        return LOCALE_DATA[locale].displayName;
    };

    NRS.getLocale = function () {
        var lang;
        if (NRS.settings && NRS.settings['regional_format'] != "default") {
            lang = NRS.settings['regional_format'];
        } else {
            lang = window.javaFxLanguage || window.navigator.userLanguage || window.navigator.language;
            if (!LOCALE_DATA[lang]) {
                if (lang && lang.length == 2) {
                    // Attempt to expand the Chrome two letter language to country specific locale
                    if (window.navigator.languages) {
                        var tokens = String(window.navigator.languages).split(",");
                        for (var i=0; i<tokens.length; i++) {
                            var separator = tokens[i].indexOf("-");
                            if (separator == -1) {
                                continue;
                            }
                            if (tokens[i].substring(0, separator) == lang) {
                                NRS.logConsole("Language " + lang + " resolved to locale " + tokens[i]);
                                lang = tokens[i];
                                break;
                            }
                        }
                    }
                }
                if (!LOCALE_DATA[lang]) {
                    if (!currentLocale.lang) {
                        NRS.logConsole("Cannot find locale definitions for language " + lang + " default to en-US");
                    }
                    lang = "en-US";
                }
            }
        }
        if (!currentLocale.lang || currentLocale.lang != lang) {
            currentLocale = {};
            currentLocale.lang = lang;
            currentLocale.dateFormat = LOCALE_DATA[lang].dateFormat;
            currentLocale.decimal = LOCALE_DATA[lang].decimal;
            currentLocale.section = LOCALE_DATA[lang].section;
            currentLocale.displayName = LOCALE_DATA[lang].displayName;
            NRS.logConsole("Locale language: '" + currentLocale.lang +
                "' date format: '" + currentLocale.dateFormat +
                "' decimal separator: '" + currentLocale.decimal +
                "' section separator: '" + currentLocale.section +
                "' display name: '" + currentLocale.displayName + "'");
        }
        return currentLocale;
    };

    var LOCALE_DATA = {
        "af-ZA": {dateFormat: "yyyy/MM/dd", decimal: ".", section: ",", displayName: "Afrikaans (South Africa)"},
        "am-ET": {dateFormat: "d/M/yyyy", decimal: ".", section: ",", displayName: "Amharic (Ethiopia)"},
        "ar-AE": {dateFormat: "dd/MM/yyyy", decimal: "٫", section: "٬", displayName: "Arabic (United Arab Emirates)"},
        "ar-BH": {dateFormat: "dd/MM/yyyy", decimal: "٫", section: "٬", displayName: "Arabic (Bahrain)"},
        "ar-DZ": {dateFormat: "dd-MM-yyyy", decimal: "٫", section: "٬", displayName: "Arabic (Algeria)"},
        "ar-EG": {dateFormat: "dd/MM/yyyy", decimal: "٫", section: "٬", displayName: "Arabic (Egypt)"},
        "ar-IQ": {dateFormat: "dd/MM/yyyy", decimal: "٫", section: "٬", displayName: "Arabic (Iraq)"},
        "ar-JO": {dateFormat: "dd/MM/yyyy", decimal: "٫", section: "٬", displayName: "Arabic (Jordan)"},
        "ar-KW": {dateFormat: "dd/MM/yyyy", decimal: "٫", section: "٬", displayName: "Arabic (Kuwait)"},
        "ar-LB": {dateFormat: "dd/MM/yyyy", decimal: "٫", section: "٬", displayName: "Arabic (Lebanon)"},
        "ar-LY": {dateFormat: "dd/MM/yyyy", decimal: "٫", section: "٬", displayName: "Arabic (Libya)"},
        "ar-MA": {dateFormat: "dd-MM-yyyy", decimal: "٫", section: "٬", displayName: "Arabic (Morocco)"},
        "arn-CL": {dateFormat: "dd-MM-yyyy", decimal: ",", section: " ", displayName: "Mapudungun (Chile)"},
        "ar-OM": {dateFormat: "dd/MM/yyyy", decimal: "٫", section: "٬", displayName: "Arabic (Oman)"},
        "ar-QA": {dateFormat: "dd/MM/yyyy", decimal: "٫", section: "٬", displayName: "Arabic (Qatar)"},
        "ar-SA": {dateFormat: "dd/MM/yy", decimal: "٫", section: "٬", displayName: "Arabic (Saudi Arabia)"},
        "ar-SY": {dateFormat: "dd/MM/yyyy", decimal: "٫", section: "٬", displayName: "Arabic (Syria)"},
        "ar-TN": {dateFormat: "dd-MM-yyyy", decimal: "٫", section: "٬", displayName: "Arabic (Tunisia)"},
        "ar-YE": {dateFormat: "dd/MM/yyyy", decimal: "٫", section: "٬", displayName: "Arabic (Yemen)"},
        "as-IN": {dateFormat: "dd-MM-yyyy", decimal: ",", section: " ", displayName: "Assamese (India)"},
        "az-Cyrl-AZ": {dateFormat: "dd.MM.yyyy", decimal: ".", section: ",", displayName: "Azerbaijani (CYRL,AZ)"},
        "az-Latn-AZ": {dateFormat: "dd.MM.yyyy", decimal: ".", section: ",", displayName: "Azerbaijani (LATN,AZ)"},
        "ba-RU": {dateFormat: "dd.MM.yy", decimal: ",", section: " ", displayName: "Bashkir (Russia)"},
        "be-BY": {dateFormat: "dd.MM.yyyy", decimal: ".", section: ",", displayName: "Belarusian (Belarus)"},
        "bg-BG": {dateFormat: "dd.M.yyyy", decimal: ",", section: " ", displayName: "Bulgarian (Bulgaria)"},
        "bn-BD": {dateFormat: "dd-MM-yy", decimal: ".", section: ",", displayName: "Bengali (Bangladesh)"},
        "bn-IN": {dateFormat: "dd-MM-yy", decimal: ".", section: ",", displayName: "Bengali (India)"},
        "bo-CN": {dateFormat: "yyyy/M/d", decimal: ",", section: " ", displayName: "Tibetan (China)"},
        "br-FR": {dateFormat: "dd/MM/yyyy", decimal: ".", section: ",", displayName: "Breton (France)"},
        "bs-Cyrl-BA": {dateFormat: "d.M.yyyy", decimal: ".", section: ",", displayName: "Bosnian (CYRL,BA)"},
        "bs-Latn-BA": {dateFormat: "d.M.yyyy", decimal: ".", section: ",", displayName: "Bosnian (LATN,BA)"},
        "ca-ES": {dateFormat: "dd/MM/yyyy", decimal: ",", section: ".", displayName: "Catalan (Spain)"},
        "co-FR": {dateFormat: "dd/MM/yyyy", decimal: ",", section: " ", displayName: "Corsican (France)"},
        "cs-CZ": {dateFormat: "d.M.yyyy", decimal: ",", section: " ", displayName: "Czech (Czech Republic)"},
        "cy-GB": {dateFormat: "dd/MM/yyyy", decimal: ".", section: ",", displayName: "Welsh (United Kingdom)"},
        "da-DK": {dateFormat: "dd-MM-yyyy", decimal: ",", section: ".", displayName: "Danish (Denmark)"},
        "de-AT": {dateFormat: "dd.MM.yyyy", decimal: ",", section: ".", displayName: "German (Austria)"},
        "de-CH": {dateFormat: "dd.MM.yyyy", decimal: ".", section: "'", displayName: "German (Switzerland)"},
        "de-DE": {dateFormat: "dd.MM.yyyy", decimal: ",", section: ".", displayName: "German (Germany)"},
        "de-LI": {dateFormat: "dd.MM.yyyy", decimal: ",", section: ".", displayName: "German (Liechtenstein)"},
        "de-LU": {dateFormat: "dd.MM.yyyy", decimal: ",", section: ".", displayName: "German (Luxembourg)"},
        "dsb-DE": {dateFormat: "d. M. yyyy", decimal: ",", section: " ", displayName: "Lower Sorbian (Germany)"},
        "dv-MV": {dateFormat: "dd/MM/yy", decimal: ",", section: " ", displayName: "Divehi (Maldives)"},
        "el-GR": {dateFormat: "d/M/yyyy", decimal: ",", section: ".", displayName: "Greek (Greece)"},
        "en-029": {dateFormat: "MM/dd/yyyy", decimal: ".", section: ",", displayName: "English (Caribbean)"},
        "en-AU": {dateFormat: "d/MM/yyyy", decimal: ".", section: ",", displayName: "English (Australia)"},
        "en-BZ": {dateFormat: "dd/MM/yyyy", decimal: ".", section: ",", displayName: "English (Belize)"},
        "en-CA": {dateFormat: "dd/MM/yyyy", decimal: ".", section: ",", displayName: "English (Canada)"},
        "en-GB": {dateFormat: "dd/MM/yyyy", decimal: ".", section: ",", displayName: "English (United Kingdom)"},
        "en-IE": {dateFormat: "dd/MM/yyyy", decimal: ".", section: ",", displayName: "English (Ireland)"},
        "en-IN": {dateFormat: "dd-MM-yyyy", decimal: ".", section: ",", displayName: "English (India)"},
        "en-JM": {dateFormat: "dd/MM/yyyy", decimal: ".", section: ",", displayName: "English (Jamaica)"},
        "en-MY": {dateFormat: "d/M/yyyy", decimal: ".", section: ",", displayName: "English (Malaysia)"},
        "en-NZ": {dateFormat: "d/MM/yyyy", decimal: ".", section: ",", displayName: "English (New Zealand)"},
        "en-PH": {dateFormat: "M/d/yyyy", decimal: ".", section: ",", displayName: "English (Philippines)"},
        "en-SG": {dateFormat: "d/M/yyyy", decimal: ".", section: ",", displayName: "English (Singapore)"},
        "en-TT": {dateFormat: "dd/MM/yyyy", decimal: ".", section: ",", displayName: "English (Trinidad and Tobago)"},
        "en-US": {dateFormat: "M/d/yyyy", decimal: ".", section: ",", displayName: "English (United States)"},
        "en-ZA": {dateFormat: "yyyy/MM/dd", decimal: ",", section: " ", displayName: "English (South Africa)"},
        "en-ZW": {dateFormat: "M/d/yyyy", decimal: ".", section: ",", displayName: "English (Zimbabwe)"},
        "es-AR": {dateFormat: "dd/MM/yyyy", decimal: ",", section: ".", displayName: "Spanish (Argentina)"},
        "es-BO": {dateFormat: "dd/MM/yyyy", decimal: ",", section: ".", displayName: "Spanish (Bolivia)"},
        "es-CL": {dateFormat: "dd-MM-yyyy", decimal: ",", section: ".", displayName: "Spanish (Chile)"},
        "es-CO": {dateFormat: "dd/MM/yyyy", decimal: ",", section: ".", displayName: "Spanish (Colombia)"},
        "es-CR": {dateFormat: "dd/MM/yyyy", decimal: ",", section: ".", displayName: "Spanish (Costa Rica)"},
        "es-DO": {dateFormat: "dd/MM/yyyy", decimal: ",", section: ".", displayName: "Spanish (Dominican Republic)"},
        "es-EC": {dateFormat: "dd/MM/yyyy", decimal: ",", section: ".", displayName: "Spanish (Ecuador)"},
        "es-ES": {dateFormat: "dd/MM/yyyy", decimal: ",", section: ".", displayName: "Spanish (Spain)"},
        "es-GT": {dateFormat: "dd/MM/yyyy", decimal: ",", section: ".", displayName: "Spanish (Guatemala)"},
        "es-HN": {dateFormat: "dd/MM/yyyy", decimal: ",", section: ".", displayName: "Spanish (Honduras)"},
        "es-MX": {dateFormat: "dd/MM/yyyy", decimal: ".", section: ",", displayName: "Spanish (Mexico)"},
        "es-NI": {dateFormat: "dd/MM/yyyy", decimal: ",", section: ".", displayName: "Spanish (Nicaragua)"},
        "es-PA": {dateFormat: "MM/dd/yyyy", decimal: ",", section: ".", displayName: "Spanish (Panama)"},
        "es-PE": {dateFormat: "dd/MM/yyyy", decimal: ",", section: ".", displayName: "Spanish (Peru)"},
        "es-PR": {dateFormat: "dd/MM/yyyy", decimal: ",", section: ".", displayName: "Spanish (Puerto Rico)"},
        "es-PY": {dateFormat: "dd/MM/yyyy", decimal: ",", section: ".", displayName: "Spanish (Paraguay)"},
        "es-SV": {dateFormat: "dd/MM/yyyy", decimal: ",", section: ".", displayName: "Spanish (El Salvador)"},
        "es-US": {dateFormat: "M/d/yyyy", decimal: ".", section: ",", displayName: "Spanish (United States)"},
        "es-UY": {dateFormat: "dd/MM/yyyy", decimal: ",", section: ".", displayName: "Spanish (Uruguay)"},
        "es-VE": {dateFormat: "dd/MM/yyyy", decimal: ",", section: ".", displayName: "Spanish (Venezuela)"},
        "et-EE": {dateFormat: "d.MM.yyyy", decimal: ",", section: " ", displayName: "Estonian (Estonia)"},
        "eu-ES": {dateFormat: "yyyy/MM/dd", decimal: ".", section: ",", displayName: "Basque (Spain)"},
        "fa-IR": {dateFormat: "MM/dd/yyyy", decimal: "٫", section: "٬", displayName: "Persian (Iran)"},
        "fi-FI": {dateFormat: "d.M.yyyy", decimal: ",", section: " ", displayName: "Finnish (Finland)"},
        "fil-PH": {dateFormat: "M/d/yyyy", decimal: ".", section: ",", displayName: "Filipino (Philippines)"},
        "fo-FO": {dateFormat: "dd-MM-yyyy", decimal: ".", section: ",", displayName: "Faroese (Faroe Islands)"},
        "fr-BE": {dateFormat: "d/MM/yyyy", decimal: ",", section: " ", displayName: "French (Belgium)"},
        "fr-CA": {dateFormat: "yyyy-MM-dd", decimal: ",", section: " ", displayName: "French (Canada)"},
        "fr-CH": {dateFormat: "dd.MM.yyyy", decimal: ".", section: " ", displayName: "French (Switzerland)"},
        "fr-FR": {dateFormat: "dd/MM/yyyy", decimal: ",", section: " ", displayName: "French (France)"},
        "fr-LU": {dateFormat: "dd/MM/yyyy", decimal: ",", section: " ", displayName: "French (Luxembourg)"},
        "fr-MC": {dateFormat: "dd/MM/yyyy", decimal: ",", section: " ", displayName: "French (Monaco)"},
        "fy-NL": {dateFormat: "d-M-yyyy", decimal: ",", section: " ", displayName: "Frisian (Netherlands)"},
        "ga-IE": {dateFormat: "dd/MM/yyyy", decimal: ".", section: ",", displayName: "Irish (Ireland)"},
        "gd-GB": {dateFormat: "dd/MM/yyyy", decimal: ",", section: " ", displayName: "Scottish Gaelic (United Kingdom)"},
        "gl-ES": {dateFormat: "dd/MM/yy", decimal: ".", section: ",", displayName: "Gallegan (Spain)"},
        "gsw-FR": {dateFormat: "dd/MM/yyyy", decimal: ",", section: " ", displayName: "Swiss German (France)"},
        "gu-IN": {dateFormat: "dd-MM-yy", decimal: ".", section: ",", displayName: "Gujarati (India)"},
        "ha-Latn-NG": {dateFormat: "d/M/yyyy", decimal: ".", section: ",", displayName: "Hausa (LATN,NG)"},
        "he-IL": {dateFormat: "dd/MM/yyyy", decimal: ".", section: ",", displayName: "Hebrew (Israel)"},
        "hi-IN": {dateFormat: "dd-MM-yyyy", decimal: ".", section: ",", displayName: "Hindi (India)"},
        "hr-BA": {dateFormat: "d.M.yyyy.", decimal: ",", section: ".", displayName: "Croatian (Bosnia and Herzegovina)"},
        "hr-HR": {dateFormat: "d.M.yyyy", decimal: ",", section: ".", displayName: "Croatian (Croatia)"},
        "hsb-DE": {dateFormat: "d. M. yyyy", decimal: ",", section: " ", displayName: "Upper Sorbian (Germany)"},
        "hu-HU": {dateFormat: "yyyy. MM. dd.", decimal: ",", section: " ", displayName: "Hungarian (Hungary)"},
        "hy-AM": {dateFormat: "dd.MM.yyyy", decimal: ".", section: ",", displayName: "Armenian (Armenia)"},
        "id-ID": {dateFormat: "dd/MM/yyyy", decimal: ",", section: ".", displayName: "Indonesian (Indonesia)"},
        "ig-NG": {dateFormat: "d/M/yyyy", decimal: ".", section: ",", displayName: "Igbo (Nigeria)"},
        "ii-CN": {dateFormat: "yyyy/M/d", decimal: ",", section: " ", displayName: "Sichuan Yi (China)"},
        "is-IS": {dateFormat: "d.M.yyyy", decimal: ".", section: ",", displayName: "Icelandic (Iceland)"},
        "it-CH": {dateFormat: "dd.MM.yyyy", decimal: ",", section: ".", displayName: "Italian (Switzerland)"},
        "it-IT": {dateFormat: "dd/MM/yyyy", decimal: ",", section: ".", displayName: "Italian (Italy)"},
        "iu-Cans-CA": {dateFormat: "d/M/yyyy", decimal: ",", section: " ", displayName: "Inuktitut (CANS,CA)"},
        "iu-Latn-CA": {dateFormat: "d/MM/yyyy", decimal: ",", section: " ", displayName: "Inuktitut (LATN,CA)"},
        "ja-JP": {dateFormat: "yyyy/MM/dd", decimal: ".", section: ",", displayName: "Japanese (Japan)"},
        "ka-GE": {dateFormat: "dd.MM.yyyy", decimal: ".", section: ",", displayName: "Georgian (Georgia)"},
        "kk-KZ": {dateFormat: "dd.MM.yyyy", decimal: ".", section: ",", displayName: "Kazakh (Kazakhstan)"},
        "kl-GL": {dateFormat: "dd-MM-yyyy", decimal: ",", section: " ", displayName: "Greenlandic (Greenland)"},
        "km-KH": {dateFormat: "yyyy-MM-dd", decimal: ".", section: ",", displayName: "Khmer (Cambodia)"},
        "kn-IN": {dateFormat: "dd-MM-yy", decimal: ".", section: ",", displayName: "Kannada (India)"},
        "kok-IN": {dateFormat: "dd-MM-yyyy", decimal: ",", section: " ", displayName: "Konkani (India)"},
        "ko-KR": {dateFormat: "yyyy-MM-dd", decimal: ".", section: ",", displayName: "Korean (South Korea)"},
        "ky-KG": {dateFormat: "dd.MM.yy", decimal: ".", section: ",", displayName: "Kirghiz (Kyrgyzstan)"},
        "lb-LU": {dateFormat: "dd/MM/yyyy", decimal: ",", section: " ", displayName: "Luxembourgish (Luxembourg)"},
        "lo-LA": {dateFormat: "dd/MM/yyyy", decimal: ".", section: ",", displayName: "Lao (Laos)"},
        "lt-LT": {dateFormat: "yyyy.MM.dd", decimal: ",", section: " ", displayName: "Lithuanian (Lithuania)"},
        "lv-LV": {dateFormat: "yyyy.MM.dd.", decimal: ",", section: " ", displayName: "Latvian (Latvia)"},
        "mi-NZ": {dateFormat: "dd/MM/yyyy", decimal: ",", section: " ", displayName: "Maori (New Zealand)"},
        "mk-MK": {dateFormat: "dd.MM.yyyy", decimal: ".", section: ",", displayName: "Macedonian (Macedonia)"},
        "ml-IN": {dateFormat: "dd-MM-yy", decimal: ".", section: ",", displayName: "Malayalam (India)"},
        "mn-MN": {dateFormat: "yy.MM.dd", decimal: ".", section: ",", displayName: "Mongolian (Mongolia)"},
        "mn-Mong-CN": {dateFormat: "yyyy/M/d", decimal: ".", section: ",", displayName: "Mongolian (MONG,CN)"},
        "moh-CA": {dateFormat: "M/d/yyyy", decimal: ",", section: " ", displayName: "Mohawk (Canada)"},
        "mr-IN": {dateFormat: "dd-MM-yyyy", decimal: ".", section: ",", displayName: "Marathi (India)"},
        "ms-BN": {dateFormat: "dd/MM/yyyy", decimal: ".", section: ",", displayName: "Malay (Brunei)"},
        "ms-MY": {dateFormat: "dd/MM/yyyy", decimal: ".", section: ",", displayName: "Malay (Malaysia)"},
        "mt-MT": {dateFormat: "dd/MM/yyyy", decimal: ".", section: ",", displayName: "Maltese (Malta)"},
        "nb-NO": {dateFormat: "dd.MM.yyyy", decimal: ",", section: " ", displayName: "Norwegian Bokmål (Norway)"},
        "ne-NP": {dateFormat: "M/d/yyyy", decimal: ".", section: ",", displayName: "Nepali (Nepal)"},
        "nl-BE": {dateFormat: "d/MM/yyyy", decimal: ",", section: ".", displayName: "Dutch (Belgium)"},
        "nl-NL": {dateFormat: "d-M-yyyy", decimal: ",", section: ".", displayName: "Dutch (Netherlands)"},
        "nn-NO": {dateFormat: "dd.MM.yyyy", decimal: ".", section: ",", displayName: "Norwegian Nynorsk (Norway)"},
        "nso-ZA": {dateFormat: "yyyy/MM/dd", decimal: ",", section: " ", displayName: "Pedi (South Africa)"},
        "oc-FR": {dateFormat: "dd/MM/yyyy", decimal: ",", section: " ", displayName: "Occitan (France)"},
        "or-IN": {dateFormat: "dd-MM-yy", decimal: ".", section: ",", displayName: "Oriya (India)"},
        "pa-IN": {dateFormat: "dd-MM-yy", decimal: ".", section: ",", displayName: "Panjabi (India)"},
        "pl-PL": {dateFormat: "yyyy-MM-dd", decimal: ",", section: " ", displayName: "Polish (Poland)"},
        "prs-AF": {dateFormat: "dd/MM/yy", decimal: ",", section: " ", displayName: "prs (Afghanistan)"},
        "ps-AF": {dateFormat: "dd/MM/yy", decimal: ".", section: ",", displayName: "Pushto (Afghanistan)"},
        "pt-BR": {dateFormat: "d/M/yyyy", decimal: ",", section: ".", displayName: "Portuguese (Brazil)"},
        "pt-PT": {dateFormat: "dd-MM-yyyy", decimal: ",", section: " ", displayName: "Portuguese (Portugal)"},
        "qut-GT": {dateFormat: "dd/MM/yyyy", decimal: ",", section: " ", displayName: "qut (Guatemala)"},
        "quz-BO": {dateFormat: "dd/MM/yyyy", decimal: ",", section: " ", displayName: "quz (Bolivia)"},
        "quz-EC": {dateFormat: "dd/MM/yyyy", decimal: ",", section: " ", displayName: "quz (Ecuador)"},
        "quz-PE": {dateFormat: "dd/MM/yyyy", decimal: ",", section: " ", displayName: "quz (Peru)"},
        "rm-CH": {dateFormat: "dd/MM/yyyy", decimal: ".", section: ",", displayName: "Raeto-Romance (Switzerland)"},
        "ro-RO": {dateFormat: "dd.MM.yyyy", decimal: ",", section: ".", displayName: "Romanian (Romania)"},
        "ru-RU": {dateFormat: "dd.MM.yyyy", decimal: ",", section: " ", displayName: "Russian (Russia)"},
        "rw-RW": {dateFormat: "M/d/yyyy", decimal: ".", section: ",", displayName: "Kinyarwanda (Rwanda)"},
        "sah-RU": {dateFormat: "MM.dd.yyyy", decimal: ",", section: " ", displayName: "Yakut (Russia)"},
        "sa-IN": {dateFormat: "dd-MM-yyyy", decimal: ",", section: " ", displayName: "Sanskrit (India)"},
        "se-FI": {dateFormat: "d.M.yyyy", decimal: ",", section: " ", displayName: "Northern Sami (Finland)"},
        "se-NO": {dateFormat: "dd.MM.yyyy", decimal: ",", section: " ", displayName: "Northern Sami (Norway)"},
        "se-SE": {dateFormat: "yyyy-MM-dd", decimal: ",", section: " ", displayName: "Northern Sami (Sweden)"},
        "si-LK": {dateFormat: "yyyy-MM-dd", decimal: ".", section: ",", displayName: "Sinhalese (Sri Lanka)"},
        "sk-SK": {dateFormat: "d. M. yyyy", decimal: ",", section: " ", displayName: "Slovak (Slovakia)"},
        "sl-SI": {dateFormat: "d.M.yyyy", decimal: ",", section: ".", displayName: "Slovenian (Slovenia)"},
        "sma-NO": {dateFormat: "dd.MM.yyyy", decimal: ",", section: " ", displayName: "Southern Sami (Norway)"},
        "sma-SE": {dateFormat: "yyyy-MM-dd", decimal: ",", section: " ", displayName: "Southern Sami (Sweden)"},
        "smj-NO": {dateFormat: "dd.MM.yyyy", decimal: ",", section: " ", displayName: "Lule Sami (Norway)"},
        "smj-SE": {dateFormat: "yyyy-MM-dd", decimal: ",", section: " ", displayName: "Lule Sami (Sweden)"},
        "smn-FI": {dateFormat: "d.M.yyyy", decimal: ",", section: " ", displayName: "Inari Sami (Finland)"},
        "sms-FI": {dateFormat: "d.M.yyyy", decimal: ",", section: " ", displayName: "Skolt Sami (Finland)"},
        "sq-AL": {dateFormat: "yyyy-MM-dd", decimal: ".", section: ",", displayName: "Albanian (Albania)"},
        "sr-Cyrl-BA": {dateFormat: "d.M.yyyy", decimal: ",", section: ".", displayName: "Serbian (CYRL,BA)"},
        "sr-Cyrl-CS": {dateFormat: "d.M.yyyy", decimal: ",", section: ".", displayName: "Serbian (CYRL,CS)"},
        "sr-Cyrl-ME": {dateFormat: "d.M.yyyy", decimal: ",", section: ".", displayName: "Serbian (CYRL,ME)"},
        "sr-Cyrl-RS": {dateFormat: "d.M.yyyy", decimal: ",", section: ".", displayName: "Serbian (CYRL,RS)"},
        "sr-Latn-BA": {dateFormat: "d.M.yyyy", decimal: ",", section: ".", displayName: "Serbian (LATN,BA)"},
        "sr-Latn-CS": {dateFormat: "d.M.yyyy", decimal: ",", section: ".", displayName: "Serbian (LATN,CS)"},
        "sr-Latn-ME": {dateFormat: "d.M.yyyy", decimal: ",", section: ".", displayName: "Serbian (LATN,ME)"},
        "sr-Latn-RS": {dateFormat: "d.M.yyyy", decimal: ",", section: ".", displayName: "Serbian (LATN,RS)"},
        "sv-FI": {dateFormat: "d.M.yyyy", decimal: ",", section: " ", displayName: "Swedish (Finland)"},
        "sv-SE": {dateFormat: "yyyy-MM-dd", decimal: ",", section: " ", displayName: "Swedish (Sweden)"},
        "sw-KE": {dateFormat: "M/d/yyyy", decimal: ".", section: ",", displayName: "Swahili (Kenya)"},
        "syr-SY": {dateFormat: "dd/MM/yyyy", decimal: ",", section: " ", displayName: "Syriac (Syria)"},
        "ta-IN": {dateFormat: "dd-MM-yyyy", decimal: ".", section: ",", displayName: "Tamil (India)"},
        "te-IN": {dateFormat: "dd-MM-yy", decimal: ".", section: ",", displayName: "Telugu (India)"},
        "tg-Cyrl-TJ": {dateFormat: "dd.MM.yy", decimal: ".", section: ",", displayName: "Tajik (CYRL,TJ)"},
        "th-TH": {dateFormat: "d/M/yyyy", decimal: ".", section: ",", displayName: "Thai (Thailand)"},
        "tk-TM": {dateFormat: "dd.MM.yy", decimal: ",", section: " ", displayName: "Turkmen (Turkmenistan)"},
        "tn-ZA": {dateFormat: "yyyy/MM/dd", decimal: ",", section: " ", displayName: "Tswana (South Africa)"},
        "tr-TR": {dateFormat: "dd.MM.yyyy", decimal: ",", section: ".", displayName: "Turkish (Turkey)"},
        "tt-RU": {dateFormat: "dd.MM.yyyy", decimal: ",", section: " ", displayName: "Tatar (Russia)"},
        "tzm-Latn-DZ": {dateFormat: "dd-MM-yyyy", decimal: ",", section: " ", displayName: "tzm (LATN,DZ)"},
        "ug-CN": {dateFormat: "yyyy-M-d", decimal: ",", section: " ", displayName: "Uighur (China)"},
        "uk-UA": {dateFormat: "dd.MM.yyyy", decimal: ",", section: " ", displayName: "Ukrainian (Ukraine)"},
        "ur-PK": {dateFormat: "dd/MM/yyyy", decimal: ".", section: ",", displayName: "Urdu (Pakistan)"},
        "uz-Cyrl-UZ": {dateFormat: "dd.MM.yyyy", decimal: ".", section: ",", displayName: "Uzbek (CYRL,UZ)"},
        "uz-Latn-UZ": {dateFormat: "dd/MM yyyy", decimal: ".", section: ",", displayName: "Uzbek (LATN,UZ)"},
        "vi-VN": {dateFormat: "dd/MM/yyyy", decimal: ",", section: ".", displayName: "Vietnamese (Vietnam)"},
        "wo-SN": {dateFormat: "dd/MM/yyyy", decimal: ",", section: " ", displayName: "Wolof (Senegal)"},
        "xh-ZA": {dateFormat: "yyyy/MM/dd", decimal: ",", section: " ", displayName: "Xhosa (South Africa)"},
        "yo-NG": {dateFormat: "d/M/yyyy", decimal: ".", section: ",", displayName: "Yoruba (Nigeria)"},
        "zh-CN": {dateFormat: "yyyy/M/d", decimal: ".", section: ",", displayName: "Chinese (China)"},
        "zh-HK": {dateFormat: "d/M/yyyy", decimal: ".", section: ",", displayName: "Chinese (Hong Kong)"},
        "zh-MO": {dateFormat: "d/M/yyyy", decimal: ".", section: ",", displayName: "Chinese (Macao)"},
        "zh-SG": {dateFormat: "d/M/yyyy", decimal: ".", section: ",", displayName: "Chinese (Singapore)"},
        "zh-TW": {dateFormat: "yyyy/M/d", decimal: ".", section: ",", displayName: "Chinese (Taiwan)"},
        "zu-ZA": {dateFormat: "yyyy/MM/dd", decimal: ".", section: ",", displayName: "Zulu (South Africa)"}
    };

    var SORTED_LOCALE_DATA = Object.keys(LOCALE_DATA).sort(function(a,b) {
        return LOCALE_DATA[a].displayName.localeCompare(LOCALE_DATA[b].displayName);
    });

    return NRS;
}(NRS || {}));