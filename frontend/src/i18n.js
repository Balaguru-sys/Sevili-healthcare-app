import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

import en from './locales/en/translation.json';
import ta from './locales/ta/translation.json';
import hi from './locales/hi/translation.json';
import ml from './locales/ml/translation.json';
import te from './locales/te/translation.json';

const savedLang = localStorage.getItem('medapp_lang') || 'en';

i18n
  .use(initReactI18next)
  .init({
    resources: {
      en: { translation: en },
      ta: { translation: ta },
      hi: { translation: hi },
      ml: { translation: ml },
      te: { translation: te },
    },
    lng: savedLang,
    fallbackLng: 'en',
    interpolation: { escapeValue: false },
  });

// Persist language choice
i18n.on('languageChanged', (lng) => {
  localStorage.setItem('medapp_lang', lng);
});

export default i18n;

export const LANGUAGES = [
  { code: 'en', label: 'EN', name: 'English' },
  { code: 'ta', label: 'தமிழ்', name: 'Tamil' },
  { code: 'hi', label: 'हिंदी', name: 'Hindi' },
  { code: 'ml', label: 'മലയാളം', name: 'Malayalam' },
  { code: 'te', label: 'తెలుగు', name: 'Telugu' },
];

// Maps i18n language codes to backend chat language codes
export const LANG_TO_CHAT = {
  en: 'EN', ta: 'TA', hi: 'HI', ml: 'ML', te: 'TE',
};
