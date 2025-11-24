import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'

// Simple i18n bootstrap with in-memory resources; can be extended to load via HTTP
const resources = {
  en: {
    translation: {
      app: {
        name: 'JunieMVC',
        skipToContent: 'Skip to content',
        nav: {
          home: 'Home',
          beers: 'Beers',
          customers: 'Customers',
          orders: 'Orders',
          sandbox: 'Sandbox'
        },
        themeToggle: 'Toggle dark mode',
        theme: {
          dark: 'Dark',
          light: 'Light'
        },
        footer: {
          copyright: 'All rights reserved.',
          about: 'About',
          docs: 'Docs'
        },
        loading: 'Loading…'
      }
    }
  }
}

void i18n
  .use(initReactI18next)
  .init({
    resources,
    lng: 'en',
    fallbackLng: 'en',
    interpolation: { escapeValue: false }
  })

export default i18n
