import { Routes, Route, Navigate } from 'react-router-dom'
import AuthGuard from '@/components/AuthGuard'
import AppLayout from '@/components/layout/AppLayout'

// ── Existing pages ──────────────────────────────────────────────────────────
import LoginPage from '@/pages/Login'
import DashboardPage from '@/pages/Dashboard'
import DormitoriesList from '@/pages/Dormitories/List'
import DormitoriesNew from '@/pages/Dormitories/New'
import DormitoriesDetail from '@/pages/Dormitories/Detail'
import RoomsList from '@/pages/Rooms/List'
import RoomsForm from '@/pages/Rooms/Form'
import CheckinsList from '@/pages/Checkins/List'
import CheckinsDetail from '@/pages/Checkins/Detail'
import CheckinsCheckout from '@/pages/Checkins/Checkout'
import AlertsLongTerm from '@/pages/Alerts/LongTerm'
import FeesList from '@/pages/Fees/List'
import FeesCalculate from '@/pages/Fees/Calculate'
import VacanciesPage from '@/pages/Vacancies'
import EquipmentList from '@/pages/Equipment/List'
import EquipmentProcess from '@/pages/Equipment/Process'
import EquipmentStorage from '@/pages/Equipment/Storage'
import ImportPage from '@/pages/Import'
import LogsPage from '@/pages/Logs'

// ── New pages (current spec) ──────────────────────────────────────────────
import CalendarPage from '@/pages/Calendar'
import ResidenceEditPage from '@/pages/Residences/Edit'
import AlertsPage from '@/pages/Alerts'
import ChangeLogsPage from '@/pages/ChangeLogs'
import DepartmentsPage from '@/pages/Departments'
import SettingsPage from '@/pages/Settings'
import ExportPage from '@/pages/Export'

export default function App() {
  return (
    <Routes>
      {/* Public */}
      <Route path="/login" element={<LoginPage />} />

      {/* Protected — all routes share AuthGuard + AppLayout */}
      <Route
        element={
          <AuthGuard>
            <AppLayout />
          </AuthGuard>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />

        {/* Core */}
        <Route path="/dashboard" element={<DashboardPage />} />

        {/* New-spec routes */}
        <Route path="/calendar" element={<CalendarPage />} />
        <Route path="/residences/:id/edit" element={<ResidenceEditPage />} />
        <Route path="/alerts" element={<AlertsPage />} />
        <Route path="/change-logs" element={<ChangeLogsPage />} />
        <Route path="/departments" element={<DepartmentsPage />} />
        <Route path="/settings" element={<SettingsPage />} />
        <Route path="/export" element={<ExportPage />} />

        {/* Existing routes preserved */}
        <Route path="/dormitories" element={<DormitoriesList />} />
        <Route path="/dormitories/new" element={<DormitoriesNew />} />
        <Route path="/dormitories/:id" element={<DormitoriesDetail />} />
        <Route path="/dormitories/:id/rooms" element={<RoomsList />} />
        <Route path="/rooms/new" element={<RoomsForm />} />
        <Route path="/rooms/:id" element={<RoomsForm />} />
        <Route path="/checkins" element={<CheckinsList />} />
        <Route path="/checkins/new" element={<Navigate to="/checkins?action=new" replace />} />
        <Route path="/checkins/:id" element={<CheckinsDetail />} />
        <Route path="/checkins/:id/checkout" element={<CheckinsCheckout />} />
        <Route path="/alerts/long-term" element={<AlertsLongTerm />} />
        <Route path="/fees" element={<FeesList />} />
        <Route path="/fees/calculate" element={<FeesCalculate />} />
        <Route path="/vacancies" element={<VacanciesPage />} />
        <Route path="/equipment" element={<EquipmentList />} />
        <Route path="/equipment/process/:checkin_id" element={<EquipmentProcess />} />
        <Route path="/equipment/storage" element={<EquipmentStorage />} />
        <Route path="/import" element={<ImportPage />} />
        <Route path="/logs" element={<LogsPage />} />
      </Route>

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}
