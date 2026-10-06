# Celebrar Catalog

Catálogo web da Celebrar Confeitaria: uma vitrine online onde o cliente navega,
monta uma cesta e envia o pedido pelo WhatsApp. Sem carrinho, checkout ou
pagamento online.

## Stack

- Backend: Java 25, Spring Boot 4.1.1, Spring Data JPA, PostgreSQL 16, Flyway
- Frontend: React + TypeScript + Vite

## Variáveis de ambiente

Copie `.env.example` para `.env` e preencha — o `.env` está no `.gitignore` e o
`docker compose` o lê automaticamente:

```bash
cp .env.example .env
```

### Obrigatórias

Sem estas, a aplicação não sobe ou sobe insegura:

| Variável | Padrão | Para que serve |
| --- | --- | --- |
| `APP_JWT_SECRET` | *(nenhum — a aplicação **não sobe** sem ela)* | Chave HMAC do JWT. Mínimo 32 bytes. |
| `APP_JWT_COOKIE_SECURE` | `false` | **`true` obrigatório em produção** (HTTPS). Liga `Secure` no cookie e o HSTS. |
| `POSTGRES_PASSWORD` | `celebrarcatalog` *(só dev)* | Senha do banco. **Troque em produção.** |

### Opcionais

| Variável | Padrão | Para que serve |
| --- | --- | --- |
| `APP_JWT_EXPIRACAO` | `8h` | Validade da sessão. |
| `APP_JWT_COOKIE_NOME` | `celebrar_sessao` | Nome do cookie de sessão. |
| `APP_IMAGENS_DIRETORIO` | `dados/imagens` | Onde as imagens do catálogo são gravadas. Precisa ser volume persistente. |
| `POSTGRES_DB` / `POSTGRES_USER` | `celebrarcatalog` | Nome do banco e do usuário. |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/celebrarcatalog` | Conexão JDBC, se o banco não for o do compose. |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | `celebrarcatalog` *(só dev)* | Credenciais JDBC, se diferentes das acima. |

As que merecem mais atenção em produção são `APP_JWT_SECRET` (sem ela a aplicação não
sobe), `APP_JWT_COOKIE_SECURE` (sem ela o cookie de sessão viaja sem `Secure`),
`POSTGRES_PASSWORD` (o padrão é público) e `APP_IMAGENS_DIRETORIO` (sem um volume
persistente as fotos somem).

> As credenciais de banco em `application.properties` são **defaults de desenvolvimento**,
> escritos como `${SPRING_DATASOURCE_PASSWORD:celebrarcatalog}`. Variável de ambiente tem
> precedência sobre `application.properties` no Spring Boot, então definir
> `POSTGRES_PASSWORD` no `.env` já substitui o default — não é preciso editar o
> `application.properties`.

### `APP_JWT_SECRET` — obrigatória

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

### `APP_IMAGENS_DIRETORIO`

As fotos do catálogo ficam **no disco**, não no banco — o registro guarda só o nome do
arquivo. O padrão (`dados/imagens`, relativo ao diretório de trabalho) serve para
desenvolvimento; em qualquer ambiente que reinicie o processo em um filesystem novo, isso
precisa apontar para um volume persistente, ou os produtos passam a referenciar arquivos
que não existem mais.

No `docker-compose.yml` isso já está resolvido: o diretório é
`/var/lib/celebrarcatalog/imagens`, montado no volume nomeado
`celebrarcatalog-imagens`, que sobrevive a `docker compose up --build` e a `restart`.

## Subir o ambiente local

### Opção 1 — Postgres + backend via Docker

Com o `.env` preenchido (ver [Variáveis de ambiente](#variáveis-de-ambiente)), o compose
lê o arquivo sozinho:

```bash
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

Rodando o backend fora do Docker o `.env` não é lido automaticamente — exporte as
variáveis no shell:

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
| `V5__categoria_ativo.sql` | Coluna `categoria.ativo` (desativação em vez de exclusão) |

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

### Como trocar a senha do admin

A troca é feita direto no banco — não existe endpoint de troca de senha. São dois passos:
gerar o hash e aplicar o `UPDATE`.

**1. Gere um hash BCrypt com custo 12.** Qualquer uma das opções abaixo serve:

```bash
# Opção A — htpasswd (pacote apache2-utils / httpd-tools)
htpasswd -bnBC 12 "" 'minha-senha-nova' | tr -d ':\n'

# Opção B — Python (bcrypt instalado via pip)
python -c "import bcrypt; print(bcrypt.hashpw(b'minha-senha-nova', bcrypt.gensalt(12)).decode())"
```

O resultado começa com `$2a$12$` ou `$2b$12$` e tem 60 caracteres. O Spring Security
aceita os dois prefixos.

> Cuidado com o histórico do shell: um comando com a senha em claro fica gravado no
> `~/.bash_history`. Prefixe com um espaço, ou limpe a linha depois.

**2. Aplique o hash.** Abra um `psql` no banco da aplicação:

```bash
# Com o banco do docker-compose de pé:
docker compose exec postgres psql -U celebrarcatalog -d celebrarcatalog
```

E rode o `UPDATE` (o `falhas_login`/`bloqueado_ate` zerados destravam a conta, caso ela
tenha sido bloqueada por tentativas erradas):

```sql
UPDATE usuario
   SET senha_hash = '<novo-hash-bcrypt-custo-12>',
       falhas_login = 0,
       bloqueado_ate = NULL
 WHERE email = 'admin@celebrar.local';
```

Confirme que atingiu exatamente uma linha (`UPDATE 1`) e faça login com a senha nova. A
sessão antiga continua válida até expirar: o JWT é stateless e não é revogado pela troca
de senha. Para invalidar todas as sessões de imediato, troque também o `APP_JWT_SECRET` e
reinicie a aplicação.

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

- `GET /api/categorias` — só as categorias ativas
- `GET /api/produtos` (filtros opcionais `?categoriaId=` e `?disponivelNaCesta=`) — só os ativos
- `GET /api/cestas` — só as ativas
- `GET /api/imagens/{nome}` — serve o arquivo. Ver [Imagens](#imagens).

### Autenticação

- `POST /api/auth/login` — corpo `{ "email": "...", "senha": "..." }`. Grava o cookie de
  sessão e devolve `{ id, email, role }`. Isento de CSRF (é o que cria a sessão).
- `POST /api/auth/logout` — limpa o cookie (`Max-Age=0`). **Exige o header CSRF.**
- `GET /api/auth/eu` — dados da sessão atual; `401` se não houver sessão.

### Administrativos

Todos sob `/api/admin/**`, exigindo sessão com `ROLE_ADMIN` **e** o header CSRF nos
métodos de escrita.

| Método | Rota | O que faz |
| --- | --- | --- |
| `GET` | `/api/admin/categorias` | Lista **incluindo as inativas** |
| `GET` | `/api/admin/categorias/{id}` | Uma categoria |
| `POST` | `/api/admin/categorias` | Cria (`201` + `Location`) |
| `PUT` | `/api/admin/categorias/{id}` | Atualiza; é por aqui que se desativa |
| `DELETE` | `/api/admin/categorias/{id}` | Exclui de verdade — `409` se houver produto vinculado |
| `GET` | `/api/admin/produtos` | Lista **incluindo os inativos** |
| `GET` | `/api/admin/produtos/{id}` | Um produto |
| `POST` | `/api/admin/produtos` | Cria (`201` + `Location`) |
| `PUT` | `/api/admin/produtos/{id}` | Atualiza |
| `DELETE` | `/api/admin/produtos/{id}` | **Exclusão lógica** (`ativo = false`) |
| `GET` | `/api/admin/cestas` | Lista **incluindo as inativas** |
| `GET` | `/api/admin/cestas/{id}` | Uma cesta |
| `POST` | `/api/admin/cestas` | Cria (`201` + `Location`) |
| `PUT` | `/api/admin/cestas/{id}` | Atualiza |
| `DELETE` | `/api/admin/cestas/{id}` | **Exclusão lógica** (`ativo = false`) |
| `POST` | `/api/admin/imagens` | Upload `multipart` → `{ "nome": "<uuid>.webp" }` |

Qualquer rota fora desse mapa é negada (`.anyRequest().denyAll()`): liberar um endpoint
novo é uma decisão explícita na `SecurityConfig`, não um efeito colateral de criar um
controller.

### Exclusão: lógica para produto e cesta, física para categoria

`DELETE` em **produto** e **cesta** não apaga a linha, só marca `ativo = false`. O registro
sai do catálogo público e continua visível na área administrativa, porque ele já pode
aparecer em cestas montadas e em pedidos enviados pelo WhatsApp — apagar destruiria esse
histórico. Para trazer de volta, basta um `PUT` com `"ativo": true`.

`DELETE` em **categoria** apaga de verdade, mas só quando nenhum produto a referencia.
Com produto vinculado a resposta é `409` com a saída sugerida na mensagem: desativar a
categoria pelo `PUT`. Apagar os produtos junto destruiria o histórico deles, e apagar só a
categoria quebraria a foreign key.

A contagem considera **também os produtos inativos**: eles continuam apontando para a
categoria, então a FK impede a exclusão do mesmo jeito.

### Corpos de entrada

Os DTOs de entrada são classes separadas dos de saída e **não têm o campo `id`** — não há
onde um `id` do corpo encaixar, então não existe como escolher a chave primária de um
registro novo nem repontar um `PUT` para outra linha. O `id` vem sempre da URL. Mandar
`id` no corpo não é ignorado em silêncio: a aplicação roda com
`spring.jackson.deserialization.fail-on-unknown-properties=true`, então qualquer campo
desconhecido devolve `400`.

Validação (Bean Validation) comum a produto e cesta:

| Campo | Regra |
| --- | --- |
| `nome` | obrigatório, até 120 caracteres (categoria: **60**, o tamanho da coluna) |
| `descricao` | opcional, até 2000 caracteres |
| `preco` | obrigatório, `>= 0.00`, no máximo 8 dígitos inteiros e 2 decimais |
| `quantidade` | obrigatório, `>= 0` (só produto) |
| `categoriaId` | obrigatório (só produto) |
| `itens` | obrigatório, até 2000 caracteres (só cesta) |
| `imagem` | opcional; precisa ser um nome gerado pelo upload |

Campos booleanos são opcionais e têm padrão: `ativo` ausente vale `true`,
`disponivelNaCesta` ausente vale `false`. No `POST`, `ativo` é ignorado — todo registro
nasce ativo.

Um erro de validação responde `400` com os campos nomeados:

```json
{ "erro": "Dados invalidos", "campos": { "nome": "não deve estar em branco" } }
```

## Imagens

As fotos ficam no disco (`APP_IMAGENS_DIRETORIO`); o banco guarda só o nome do arquivo.

### Upload — `POST /api/admin/imagens`

`multipart/form-data` com o arquivo no campo `arquivo`. Responde `201` com o nome gerado:

```bash
curl -X POST http://localhost:8080/api/admin/imagens \
  -b cookies.txt -H "X-XSRF-TOKEN: $CSRF" \
  -F 'arquivo=@foto.png;type=image/png'
# {"nome":"9d98181a-47f7-4595-911d-f0aa888967ab.png"}
```

Esse `nome` é o valor que vai no campo `imagem` de um produto ou cesta.

O arquivo passa por três validações independentes:

1. **Tamanho** — máximo 3 MB (`spring.servlet.multipart.max-file-size`), aplicado pelo
   container antes de a requisição chegar à aplicação. Acima disso, `413`.
2. **Content-type declarado** — precisa ser `image/jpeg`, `image/png` ou `image/webp`.
3. **Magic bytes do conteúdo** — e é esta que decide. O content-type e a extensão vêm da
   requisição e são triviais de falsificar; os bytes do arquivo, não. Um script PHP
   enviado como `image/png` é recusado com `400`, e a extensão gravada é a do formato
   **detectado**, não a do que o cliente declarou.

### Por que o nome original é descartado

O nome enviado pelo cliente é o vetor clássico de path traversal (`../../etc/passwd.png`),
então ele **não é usado para nada** — nem para extrair a extensão. O nome gravado é um
UUID sorteado mais a extensão da whitelist, o que limita o alfabeto a hexadecimal, `-` e
um ponto: não cabe `..`, `/`, `\` nem byte nulo.

Em cima disso, todo caminho é resolvido com `normalize()` e conferido contra o diretório
base antes de tocar o disco. Com o nome já restrito pelo regex a checagem é redundante — e
é por isso que ela existe: se algum código novo chamar o armazenamento com um nome que não
passou pelo regex, ele para ali, e não no sistema de arquivos.

### Entrega — `GET /api/imagens/{nome}`

Público. O nome é validado contra o padrão UUID+extensão **antes** de qualquer acesso ao
disco; nome fora do padrão e arquivo inexistente respondem igual (`404`), porque não há
motivo para o cliente distinguir os dois casos.

A resposta traz `Content-Type` deduzido da extensão já validada,
`Content-Disposition: inline` (é foto para exibir, não anexo para baixar) e
`Cache-Control: max-age=31536000, public, immutable` — o cache pode ser longo porque o
nome contém um UUID e nunca é reaproveitado: trocar a imagem de um produto gera outro
nome, nunca sobrescreve a URL antiga.

### Troca de imagem apaga o arquivo antigo

Ao trocar a imagem de um registro, o arquivo anterior é removido do disco, para o volume
não acumular arquivos que nenhum registro referencia. A remoção é melhor esforço: uma
falha ao apagar é registrada no log e não desfaz uma escrita que o banco já aceitou.

`DELETE` (exclusão lógica) **não** apaga a imagem — o registro pode voltar com um `PUT`.

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

## Segurança

Resumo do modelo de segurança da aplicação. Cada item tem a seção detalhada linkada.

### Superfície pública

A API é **fechada por padrão**: `SecurityConfig` termina em `.anyRequest().denyAll()`, então
uma rota só fica acessível se estiver listada explicitamente. O que é público é só leitura:

- `GET /api/categorias`, `GET /api/produtos`, `GET /api/cestas` — só registros ativos
- `GET /api/imagens/{nome}` — nome validado contra `UUID+extensão` antes de tocar o disco
- `POST /api/auth/login` e `POST /api/auth/logout`

**Nenhum endpoint de escrita é público.** Toda escrita de catálogo vive sob
`/api/admin/**`, que exige `hasRole("ADMIN")` — sessão válida — **e** o header CSRF.
O upload de imagem (`POST /api/admin/imagens`) está dentro desse prefixo.

### Autenticação e sessão

JWT HS256 assinado com `APP_JWT_SECRET`, em cookie `HttpOnly` + `SameSite=Strict` +
`Path=/`, com `Secure` quando `APP_JWT_COOKIE_SECURE=true`. Ver
[Como a sessão funciona](#como-a-sessão-funciona).

- A aplicação **se recusa a subir** sem um segredo de 32+ bytes. Não há segredo padrão.
- A validação do token rejeita `alg: none`, só aceita `HS256`, compara a assinatura em
  tempo constante (`MessageDigest.isEqual`) e exige `exp`, `iat`, `jti`, `sub` e `role`.
- Senhas são BCrypt custo 12. Login com e-mail inexistente ainda roda um `matches` contra
  um hash descartável, para o tempo de resposta não revelar quais e-mails existem.
- Cinco falhas bloqueiam a conta por 15 minutos. Ver
  [Proteção contra tentativas repetidas](#proteção-contra-tentativas-repetidas).

### Entrada

- Todo DTO de entrada (`LoginRequest`, `CategoriaEntradaDto`, `ProdutoEntradaDto`,
  `CestaProntaEntradaDto`) é validado com Bean Validation e aplicado com `@Valid`.
- `spring.jackson.deserialization.fail-on-unknown-properties=true`: campo desconhecido no
  corpo é `400`, não é ignorado em silêncio. DTOs de entrada não têm `id`.
- **Não há SQL concatenado.** O acesso a dados é só Spring Data JPA com query methods
  derivadas do nome — nenhuma `@Query`, nenhuma native query, nenhum `EntityManager` ou
  `JdbcTemplate` no projeto. Os parâmetros são sempre bind parameters.
- Upload valida tamanho, content-type declarado **e** magic bytes do conteúdo; o nome do
  cliente é descartado e substituído por UUID + extensão do formato detectado. Ver
  [Imagens](#imagens).

### Respostas de erro

Nem `404` nem `500` vazam stack trace, classe de exceção ou SQL:

- `server.error.include-stacktrace=never` e `server.error.include-message=never`.
- `ApiExceptionHandler` é o único caminho de saída: `500` responde sempre
  `{"erro":"Erro inesperado"}` com o detalhe apenas no log do servidor; `404` responde a
  mensagem de domínio (`"Produto <id> nao encontrado"`), sem internals.
- `401` e `403` são JSON fixo (`RespostasSeguranca`), nunca a página HTML do container.

### Cabeçalhos

`SecurityConfig` envia CSP (`default-src 'self'`, `object-src 'none'`,
`frame-ancestors 'none'`), `X-Frame-Options: DENY`, `Referrer-Policy: no-referrer`,
`Permissions-Policy` negando câmera/microfone/geolocalização, e HSTS de um ano
(`includeSubDomains`) quando `APP_JWT_COOKIE_SECURE=true`.

### Riscos residuais conhecidos

Pontos que **não** estão resolvidos no código e dependem de ação no deploy:

1. **A senha inicial do admin é pública** — o hash está em `V4__seed_admin.sql` e a senha
   em claro está neste README. É obrigatório trocá-la antes de expor a aplicação. Ver
   [Como trocar a senha do admin](#como-trocar-a-senha-do-admin).
2. **O default de `POSTGRES_PASSWORD` é público** e precisa ser trocado em produção.
3. **O container do backend roda como `root`** (`backend/Dockerfile` não tem `USER`).
   Trocar exige acertar a posse do volume de imagens; até lá, não exponha a porta do
   container diretamente.
4. **Logout não revoga o JWT** — o token é stateless e continua válido até `exp` (8h
   por padrão). Trocar o `APP_JWT_SECRET` invalida todas as sessões.
5. **Não há rate limit por IP**, só o bloqueio por usuário cadastrado. Tentativas contra
   e-mails inexistentes não são limitadas pela aplicação; use o proxy reverso.

## Deploy

Passo a passo para colocar em produção atrás de HTTPS.

### 1. Pré-requisitos

- Docker e Docker Compose no servidor
- Um domínio apontando para o servidor
- Um proxy reverso terminando TLS (Nginx, Caddy, Traefik) — **a aplicação não termina TLS**

### 2. Clonar e configurar o ambiente

```bash
git clone https://github.com/othomasgoncalves/celebrar-catalogo.git
cd celebrar-catalogo
cp .env.example .env
```

Edite o `.env` com valores de produção:

```bash
APP_JWT_SECRET=<saída de: openssl rand -base64 48>
APP_JWT_COOKIE_SECURE=true
POSTGRES_PASSWORD=<senha forte, gerada>
```

`APP_JWT_COOKIE_SECURE=true` é o que liga o atributo `Secure` no cookie de sessão e o
HSTS. Com ele em `false` atrás de HTTPS, o cookie da sessão do admin pode vazar em uma
requisição HTTP.

### 3. Subir backend e banco

```bash
docker compose up -d --build
```

O Flyway aplica as migrations na subida. Confira que subiu:

```bash
docker compose ps
curl http://localhost:8080/api/produtos
```

### 4. Trocar a senha do admin — antes de expor

Este passo não é opcional: a senha do seed é pública. Siga
[Como trocar a senha do admin](#como-trocar-a-senha-do-admin) **antes** de liberar o
acesso externo.

Confirme que a senha antiga não funciona mais:

```bash
curl -i -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@celebrar.local","senha":"CelebrarAdmin!2026"}'
# esperado: 401
```

### 5. Build do frontend

```bash
cd frontend
npm ci
npm run build
```

Gera `frontend/dist/`, que é conteúdo estático. Sirva pelo proxy reverso.

### 6. Proxy reverso

O proxy serve `frontend/dist/` na raiz e encaminha `/api` para o backend. Esboço com
Nginx:

```nginx
server {
    listen 443 ssl;
    server_name catalogo.exemplo.com.br;

    ssl_certificate     /etc/letsencrypt/live/catalogo.exemplo.com.br/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/catalogo.exemplo.com.br/privkey.pem;

    root /srv/celebrar-catalogo/frontend/dist;

    # SPA: rotas do React resolvem no index.html
    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host              $host;
        proxy_set_header X-Real-IP         $remote_addr;
        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # O upload aceita até 3 MB; o padrão do Nginx é 1 MB.
        client_max_body_size 4m;
    }
}

server {
    listen 80;
    server_name catalogo.exemplo.com.br;
    return 301 https://$host$request_uri;
}
```

Backend e frontend ficam na **mesma origem**, então não há CORS a configurar e o
`SameSite=Strict` do cookie funciona.

Com o proxy no lugar, feche a porta `8080` para o mundo — no `docker-compose.yml`, troque
`"8080:8080"` por `"127.0.0.1:8080:8080"` para publicar só no loopback.

### 7. Verificação pós-deploy

```bash
# Cookie de sessão com HttpOnly, Secure, SameSite=Strict, Path=/
curl -si -X POST https://catalogo.exemplo.com.br/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@celebrar.local","senha":"<senha-nova>"}' | grep -i set-cookie

# Escrita sem sessão é recusada (401)
curl -s -o /dev/null -w '%{http_code}\n' \
  -X POST https://catalogo.exemplo.com.br/api/admin/produtos \
  -H 'Content-Type: application/json' -d '{}'

# Rota não mapeada é negada
curl -s -o /dev/null -w '%{http_code}\n' https://catalogo.exemplo.com.br/api/qualquer
```

### 8. Backup

O que precisa de backup são o banco e o volume de imagens — as fotos **não** estão no
banco:

```bash
docker compose exec -T postgres pg_dump -U celebrarcatalog celebrarcatalog | gzip > backup-db.sql.gz
docker run --rm -v celebrar-catalogo_celebrarcatalog-imagens:/dados -v "$PWD":/backup \
  alpine tar czf /backup/backup-imagens.tar.gz -C /dados .
```

### Atualizar uma instalação existente

```bash
git pull
docker compose up -d --build        # Flyway aplica as migrations novas
cd frontend && npm ci && npm run build
```

## Testes

```bash
cd backend
./mvnw test
```

Os testes de autenticação, autorização, CSRF e rate limit são fatias `@WebMvcTest` e
rodam sem banco. `ArmazenamentoImagensTest` roda em um diretório temporário e concentra os
casos de entrada hostil do upload: content-type mentiroso, conteúdo que não é imagem e
tentativas de path traversal. `CelebrarcatalogApplicationTests` é um `@SpringBootTest` e
precisa do PostgreSQL de pé (`docker compose up postgres`) — é ele que valida as migrations
e o mapeamento das entidades contra o schema real.
