import { useState } from 'react'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { Header } from './components/Header/Header'
import { MontadorHero } from './components/MontadorHero/MontadorHero'
import { FiltroCategorias, CATEGORIA_TODOS } from './components/FiltroCategorias/FiltroCategorias'
import { GridProdutos } from './components/GridProdutos/GridProdutos'
import { CestasProntas } from './components/CestasProntas/CestasProntas'
import { Sobre } from './components/Sobre/Sobre'
import { Footer } from './components/Footer/Footer'
import { MontarCesta } from './pages/MontarCesta'
import { RotaProtegida } from './components/RotaProtegida'
import { AuthProvider } from './context/AuthContext'
import { AdminLayout } from './pages/admin/AdminLayout'
import { Login } from './pages/admin/Login'
import { AdminProdutos } from './pages/admin/Produtos'
import { AdminCestas } from './pages/admin/Cestas'
import { AdminCategorias } from './pages/admin/Categorias'

function Home() {
  const [categoriaAtiva, setCategoriaAtiva] = useState<string>(CATEGORIA_TODOS)

  return (
    <>
      <Header />
      <div className="wrap">
        <MontadorHero />

        <div className="sec">
          <div className="sec-hd">
            <h2>Nossos produtos</h2>
            <span>Feito com carinho</span>
          </div>
        </div>
        <FiltroCategorias ativo={categoriaAtiva} onChange={setCategoriaAtiva} />
        <GridProdutos categoriaAtiva={categoriaAtiva} />

        <div className="sec">
          <div className="sec-hd">
            <h2>Cestas prontas</h2>
            <span>Já montadas por nós</span>
          </div>
        </div>
        <CestasProntas />

        <Sobre />
      </div>
      <Footer />
    </>
  )
}

function App() {
  return (
    <BrowserRouter>
      {/* Dentro do router porque o provider navega para o login quando a sessão cai. */}
      <AuthProvider>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route
            path="/montar"
            element={
              <div className="wrap">
                <MontarCesta />
              </div>
            }
          />

          <Route path="/admin/login" element={<Login />} />
          <Route
            path="/admin"
            element={
              <RotaProtegida>
                <AdminLayout />
              </RotaProtegida>
            }
          >
            <Route index element={<Navigate to="/admin/produtos" replace />} />
            <Route path="produtos" element={<AdminProdutos />} />
            <Route path="cestas" element={<AdminCestas />} />
            <Route path="categorias" element={<AdminCategorias />} />
            {/* Sem isto, uma URL errada sob /admin renderizaria tela branca. */}
            <Route path="*" element={<Navigate to="/admin/produtos" replace />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
