import { NavLink, Outlet } from 'react-router-dom'
import { useTheme } from '../components/theme-provider'
import { useTranslation } from 'react-i18next'

export default function AppLayout() {
  const { theme, toggle } = useTheme()
  const { t } = useTranslation()
  return (
    <div className="min-h-dvh flex flex-col">
      <a href="#main" className="sr-only focus:not-sr-only focus:absolute focus:top-2 focus:left-2 bg-black text-white px-3 py-1 rounded">{t('app.skipToContent')}</a>
      <header className="border-b bg-white/70 backdrop-blur supports-[backdrop-filter]:bg-white/50 dark:bg-black/30">
        <div className="container-max flex h-14 items-center justify-between">
          <div className="font-semibold">{t('app.name')}</div>
          <nav className="flex gap-3 text-sm items-center">
            {[
              { to: '/', label: t('app.nav.home') },
              { to: '/beers', label: t('app.nav.beers') },
              { to: '/customers', label: t('app.nav.customers') },
              { to: '/orders', label: t('app.nav.orders') },
              { to: '/sandbox', label: t('app.nav.sandbox') },
            ].map((it) => (
              <NavLink
                key={it.to}
                to={it.to}
                className={({ isActive }) =>
                  `hover:underline ${isActive ? 'font-semibold underline' : ''}`
                }
                end={it.to === '/'}
              >
                {it.label}
              </NavLink>
            ))}
            <button
              type="button"
              onClick={toggle}
              aria-label={t('app.themeToggle')}
              title={`Theme: ${theme}`}
              className="ml-3 px-2 py-1 rounded border text-xs"
            >
              {theme === 'dark' ? t('app.theme.light') : t('app.theme.dark')}
            </button>
          </nav>
        </div>
      </header>
      <main id="main" className="container-max py-6 grow">
        <Outlet />
      </main>
      <footer className="border-t">
        <div className="container-max py-4 text-sm opacity-70 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2">
          <div>
            © {new Date().getFullYear()} {t('app.name')}. {t('app.footer.copyright')}
          </div>
          <div className="flex gap-3">
            <a className="hover:underline" href="/about">{t('app.footer.about')}</a>
            <a className="hover:underline" href="/docs">{t('app.footer.docs')}</a>
          </div>
        </div>
      </footer>
      {/* Portals */}
      <div id="dialogs-root" />
    </div>
  )
}
