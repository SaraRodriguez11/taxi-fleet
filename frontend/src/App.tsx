import { Navigate, Route, Routes } from 'react-router-dom'
import RequireAuth from './auth/RequireAuth'
import Layout from './components/Layout'
import DailyControlPage from './pages/DailyControlPage'
import DashboardPage from './pages/DashboardPage'
import DriversPage from './pages/DriversPage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import ReportsPage from './pages/ReportsPage'
import TaxisPage from './pages/TaxisPage'

export default function App() {
  return (
    <Routes>
      <Route path="login" element={<LoginPage />} />
      <Route path="registro" element={<RegisterPage />} />

      <Route element={<RequireAuth />}>
        <Route element={<Layout />}>
          <Route index element={<DashboardPage />} />
          <Route path="control-diario" element={<DailyControlPage />} />
          <Route path="taxis" element={<TaxisPage />} />
          <Route path="conductores" element={<DriversPage />} />
          <Route path="reportes" element={<ReportsPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
