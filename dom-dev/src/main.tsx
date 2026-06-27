import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'
import { ConfigProvider } from 'antd'
import { StyleProvider } from '@ant-design/cssinjs'
import jaJP from 'antd/locale/ja_JP'
import dayjs from 'dayjs'
import 'dayjs/locale/ja'
import App from './App'

dayjs.locale('ja')

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 1000 * 60,
      refetchOnWindowFocus: false,
    },
  },
})

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <StyleProvider hashPriority="high">
      <ConfigProvider
        locale={jaJP}
        theme={{
          token: {
            colorPrimary: '#1677ff',
            colorBgLayout: '#f0f5ff',
            colorBgContainer: '#ffffff',
            borderRadius: 8,
            boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
            fontFamily: "'Noto Sans JP', 'Segoe UI', sans-serif",
          },
          components: {
            Layout: {
              siderBg: '#001d6e',
              headerBg: '#ffffff',
            },
            Menu: {
              darkItemBg: '#001d6e',
              darkSubMenuItemBg: '#002080',
              darkItemSelectedBg: '#1677ff',
              darkItemHoverBg: '#0030a0',
              itemHeight: 44,
            },
            Card: {
              boxShadow: '0 2px 8px rgba(22,119,255,0.08)',
            },
          },
        }}
      >
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </ConfigProvider>
      </StyleProvider>
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  </React.StrictMode>
)
