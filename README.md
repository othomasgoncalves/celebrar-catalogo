# Celebrar Catalog

Catálogo web da Celebrar Confeitaria: uma vitrine online onde o cliente navega,
monta uma cesta e envia o pedido pelo WhatsApp. Sem carrinho, checkout ou
pagamento online.

## Stack

- Backend: Java 25, Spring Boot 4.1.1, Spring Data JPA, PostgreSQL 16, Flyway
- Frontend: React + TypeScript + Vite

## Configuração obrigatória: `APP_JWT_SECRET`

O backend assina a sessão do admin com HS256 e **não sobe sem um segredo válido** —
se `APP_JWT_SECRET` estiver ausente ou tiver menos de 32 bytes (256 bits, exigência do
HS256), a inicialização falha com mensagem explicando o que fazer. Isso é intencional:
não existe segredo padrão no repositório, para que nenhum ambiente rode com uma chave
conhecida.

Gere um segredo por ambiente:

```bash
openssl rand -base64 48
```

E exporte antes de subir a aplicação (ou copie `.env.example` para `.env`, que está no
`.gitignore`):

```bash
export APP_JWT_SECRET='<o-valor-gerado>'
```

| Variável | Padrão | Para que serve |
| --- | --- | --- |
| `APP_JWT_SECRET` | *(nenhum — obrigatório)* | Chave HMAC do JWT. Mínimo 32 bytes. |
| `APP_JWT_EXPIRACAO` | `8h` | Validade da sessão. |
| `APP_JWT_COOKIE_SECURE` | `false` | `true` em produção (HTTPS). Liga `Secure` no cookie e o HSTS. |
| `APP_JWT_COOKIE_NOME` | `celebrar_sessao` | Nome do cookie de sessão. |

## Subir o ambiente local

### Opção 1 — Postgres + backend via Docker

```bash
export APP_JWT_SECRET='<o-valor-gerado>'
docker compose up --build
```

O Flyway aplica as migrations automaticamente quando o backend sobe. A API fica
disponível em `http://localhost:8080`.

```bash
curl http://localhost:8080/api/produtos
```

### Opção 2 — só o Postgres via Docker, backend pelo VS Code

```bash
docker compose up postgres
```

```bash
cd backend
export APP_JWT_SECRET='<o-valor-gerado>'
./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Abre em `http://localhost:5173` e consome `/api/produtos` através do proxy do
Vite (`vite.config.ts`) para `http://localhost:8080` — não precisa configurar
CORS no backend.

## Migrations

As migrations rodam automaticamente na subida do backend (Flyway integrado ao
Spring Boot, `spring-boot-starter-flyway`).

| Migration | Conteúdo |
| --- | --- |
| `V1__create_tables.sql` | `categoria`, `produto`, `cesta_pronta` |
| `V2__seed_data.sql` | Dados iniciais do catálogo |
| `V3__usuario.sql` | Tabela `usuario` (credenciais e estado de bloqueio) |
| `V4__seed_admin.sql` | Usuário administrador inicial |

Para inspecionar o estado das migrations manualmente:

```bash
cd backend
./mvnw flyway:info
```

## Acesso administrativo

### ⚠️ Senha inicial — trocar no primeiro acesso

A migration `V4__seed_admin.sql` cria um único usuário com `ROLE_ADMIN`:

| E-mail | Senha inicial |
| --- | --- |
| `admin@celebrar.local` | `CelebrarAdmin!2026` |

**Essa senha é pública.** O hash BCrypt está versionado no repositório, então qualquer
pessoa com acesso ao código conhece a credencial. Ela existe só para o primeiro acesso
em ambiente local. **Troque a senha antes de expor a aplicação a qualquer rede.**

Para trocar, gere um hash BCrypt com custo 12 e atualize a linha do usuário:

```sql
UPDATE usuario
   SET senha_hash = '<novo-hash-bcrypt-custo-12>',
       falhas_login = 0,
       bloqueado_ate = NULL
 WHERE email = 'admin@celebrar.local';
```

Nunca grave a senha em claro no banco nem em uma migration versionada.

### Proteção contra tentativas repetidas

Cinco senhas erradas consecutivas para o mesmo usuário bloqueiam a conta por 15
minutos (`falhas_login` e `bloqueado_ate` na tabela `usuario`); a sexta tentativa
responde `423 Locked`. Um login bem-sucedido zera o contador. A resposta para e-mail
inexistente, conta inativa e senha errada é idêntica (`401` com
`E-mail ou senha inválidos`), para não revelar quais e-mails existem.

O contador é por usuário cadastrado. Tentativas contra um e-mail que não existe não têm
onde ser registradas e portanto não disparam bloqueio — em compensação também não
revelam nada, já que a resposta é sempre a mesma.

## Endpoints

### Públicos (somente leitura)

- `GET /api/categorias`
- `GET /api/produtos` (filtros opcionais `?categoriaId=` e `?disponivelNaCesta=`)
- `GET /api/cestas`
- `GET /api/imagens/**`

### Autenticação

- `POST /api/auth/login` — corpo `{ "email": "...", "senha": "..." }`. Grava o cookie de
  sessão e devolve `{ id, email, role }`. Isento de CSRF (é o que cria a sessão).
- `POST /api/auth/logout` — limpa o cookie (`Max-Age=0`). **Exige o header CSRF.**
- `GET /api/auth/eu` — dados da sessão atual; `401` se não houver sessão.

### Administrativos

- `/api/admin/**` — exige sessão com `ROLE_ADMIN`.

Qualquer rota fora desse mapa é negada (`.anyRequest().denyAll()`): liberar um endpoint
novo é uma decisão explícita na `SecurityConfig`, não um efeito colateral de criar um
controller.

## Como a sessão funciona

O token JWT (HS256, validade de 8 horas) viaja em um cookie **`HttpOnly`**, inacessível
a JavaScript — o que evita que um XSS consiga ler a sessão. O cookie é
`SameSite=Strict`, `Path=/`, e ganha `Secure` quando `APP_JWT_COOKIE_SECURE=true`.

Como o cookie é enviado pelo navegador automaticamente, a API usa **CSRF** para garantir
que a requisição partiu da aplicação:

1. O backend envia o cookie `XSRF-TOKEN` (legível por JavaScript).
2. O frontend copia esse valor para o header **`X-XSRF-TOKEN`** em toda requisição que
   altera estado (`POST`, `PUT`, `PATCH`, `DELETE`).

`GET`, `HEAD` e `OPTIONS` não precisam do header. `POST /api/auth/login` é a única
exceção entre os métodos de escrita, porque acontece antes de existir sessão.

Exemplo com `fetch` (o `credentials: 'include'` é necessário para o cookie ir junto):

```ts
const csrf = document.cookie
  .split('; ')
  .find((c) => c.startsWith('XSRF-TOKEN='))
  ?.split('=')[1];

await fetch('/api/auth/logout', {
  method: 'POST',
  credentials: 'include',
  headers: { 'X-XSRF-TOKEN': csrf ?? '' },
});
```

Uma requisição que altera estado sem o header recebe `403` com
`{"erro":"Acesso negado"}`; sem sessão válida, `401` com `{"erro":"Não autenticado"}`.
Nenhuma das duas devolve HTML ou stack trace.

## Testes

```bash
cd backend
./mvnw test
```

Os testes de autenticação, autorização, CSRF e rate limit são fatias `@WebMvcTest` e
rodam sem banco. `CelebrarcatalogApplicationTests` é um `@SpringBootTest` e precisa do
PostgreSQL de pé (`docker compose up postgres`) — é ele que valida as migrations e o
mapeamento das entidades contra o schema real.
