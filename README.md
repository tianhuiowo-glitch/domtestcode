# domtestcode

企業宿舎管理システム

## 構成

- `dom-dev/` — フロントエンド（React + Vite + TypeScript + Ant Design）
- `dom-server/` — バックエンド（Spring Boot 2.7 + Java 11 + MyBatis）

## デプロイ

- **フロントエンド**: Cloudflare Pages（ビルドコマンド: `npm run build`、出力: `dist`）
- **バックエンド**: Railway / Render（Java 11 対応ホスティング）

### Cloudflare Pages 環境変数

| 変数名 | 説明 |
|--------|------|
| `VITE_API_BASE_URL` | バックエンド API の URL（例: `https://your-backend.railway.app/api/v1`） |
