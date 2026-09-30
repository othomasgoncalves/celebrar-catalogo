import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import { CestaProvider } from './context/CestaContext'
import './styles/global.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <CestaProvider>
      <App />
    </CestaProvider>
  </React.StrictMode>,
)
