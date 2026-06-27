# func_front.md — 社員寮管理システム 前端功能图谱

> 由 fe-precheck 扫描 `dom-dev/src` 自动生成。每次新增/修改页面、组件、接口时须同步更新本文件。
> 扫描時間：2026-05-30 / 最終更新：2026-06-27（備品管理画面に転寮・廃棄機能追加：`Equipment`型`dormitoryDeletedAt`追加、`TransferEquipmentRequest`追加、`api/equipment.ts`に`transferEquipment`/`discardEquipment`追加、`List.tsx`に転寮Modal・廃棄Popconfirm・削除済み寮フィルター・所属寮列削除済タグ表示を追加）/ 2026-06-08（費用管理ページ改修：checkbox行選択+全選択、行レベル確定ボタン削除→ページ上部確定ボタン、編集ボタン+編集モーダル、寮フィルタ・月フィルタ追加、UpdateFeeRequest型追加、api/fees.ts updateFee追加）/ 2026-06-08（入住记录2バグ修正：予定入居在寮日数負数→'—'表示、ステータス「入居予定」青タグ追加、退寮ボタン非表示、Checkout.txsに入居予定ガード追加）/ 2026-06-08（Detail.tsxに入居予定ガード適用：isPlannedフラグ追加、ステータスTag「入居予定」青、退寮ボタン非表示、在寮日数'—'）/ 2026-06-13（エラーメッセージ正規化：client.ts response interceptor に msg→message 変換を追加。バックエンドが `{code,msg,data}` を返す際に全 onError ハンドラーの `err.response?.data?.message` が正しく参照できるよう一括対応。types/auth.ts の ApiResponse に msg?: string フィールドを追加）/ 2026-06-20（`/dormitories`画面に部屋・入居状況表示機能を追加：バックエンド改修不要、既存`DormitoryVO`/`RoomVO`/`CheckinVO`のフィールドをフロント型に追従。`types/dormitory.ts`に`totalRooms`/`totalCapacity`/`currentOccupancy`追加、`types/room.ts`に`currentOccupancy`追加、`Rooms/List.tsx`に「現在人数/定員」「状態」列追加、`Dormitories/List.tsx`に「部屋数」「入居率」列＋`expandable`展開行（`RoomOccupancyCard`グリッド、展開時遅延ロードでN+1回避）追加、新規`utils/roomStatus.ts`・`components/RoomOccupancyCard.tsx`）/ 2026-06-20（「入居予定／在寮中／退寮済」三態分離：`types/checkin.ts`の`CheckinStatus`に`'pending'`追加、`Checkins/List.tsx`のステータスフィルターを三態化＋ラベル「退寮処理済」→「退寮済」＋URL`?status=`パラメータからの初期化対応、`api/dashboard.ts`の`DashboardStats`に`pendingResidents`追加、`Dashboard/index.tsx`に「入居予定者数」統計カード追加（`/checkins?status=pending`へ誘導）。バックエンド側は`ResidenceHistoryMapper.xml`の`status`絞り込み条件三態化・`DashboardMapper.xml`の`countPendingResidents`新規・`RoomMapper.xml`の`currentOccupancy`口径統一が対応済み、本フロント改修と整合）

---

## Pages（页面层）

| 路由 | 文件 | 功能描述 | 状态 |
|------|------|----------|------|
| `/login` | `pages/Login/index.tsx` | 登录页（左侧装饰区 + 右侧表单，mockLogin） | ✅ 已实现 |
| `/dashboard` | `pages/Dashboard/index.tsx` | 仪表盘（統計摘要、3パネル等高カードレイアウト：寮別入居率横棒グラフ/空室ドーナツグラフ/直近入退寮、快捷操作、功能模块卡片）。vacantRooms カードリンク→/vacancies。X軸ラベルは絶対配置（0%左端固定/中間translateX(-50%)/100%右端translateX(-100%)）。3パネルRowはデフォルトflexストレッチ（align="top"なし）。左Col（md=14）は`display:flex`でOccupancyRatePanel CardがCard root:`flex:1,display:flex,flexDirection:column`+body:`flex:1,display:flex,flexDirection:column`で全高ストレッチ。棒グラフ行コンテナ：`flex:1,display:flex,flexDirection:column,justifyContent:space-between`で棒を均等分配。右Col（md=10）は`display:flex,flexDirection:column,gap:12`（入れ子Row/Colなし）。RecentChangeLogsPanel Cardは`flex:1,display:flex,flexDirection:column`、bodyも`flex:1,display:flex,flexDirection:column,overflow:hidden`、スクロールコンテナは**`height:88,flexShrink:0`（固定2件表示高さ）**、最新5件取得してスクロールで表示。**2026-06-20：統計カード5枚目「入居予定者数」追加**（`pendingResidents`、`ClockCircleOutlined`アイコン、紫`#722ed1`、クリックで`/checkins?status=pending`へ遷移、`currentResidents`カードの直後に配置）。`statCardDefs`のColレスポンシブを`xs={24} sm={12} md={6}`の4等分固定から`xs={24} sm={12} md={8} lg={8} xl={4} flex="1 1 180px"`に変更（5枚を画面幅に応じて柔軟に折り返し表示） | ✅ 已实现 |
| `/dormitories` | `pages/Dormitories/List.tsx` | 寮管理列表（分页、关键字/地域筛选、checkbox行选择、页面级编辑/删除按钮+确认对话框、部屋一覧跳转）。**「部屋数」「入居率」列追加（`totalRooms`/`totalCapacity`/`currentOccupancy`使用、満室=warning橙/空室あり=success緑タグ）。展開行（`expandable`）で寮配下の部屋を`RoomOccupancyCard`グリッド表示**（`expandedRowKeys`制御、展開時のみ`getRooms({dormitoryId,pageSize:100})`+`getCheckins({dormitoryId,status:'active',pageSize:100})`を遅延発行＝N+1回避、`checkin.roomNumber`と`room.name`を文字列マッチで入居者氏名グルーピング） | ✅ 已实现 |
| `/dormitories/new` | `pages/Dormitories/New.tsx` | 新建寮（名称/类型/地址表单） | ✅ 已实现 |
| `/dormitories/:id` | `pages/Dormitories/Detail.tsx` | 寮详情。**onErrorで`err.response?.data?.message`（msgではなくmessageフィールド）を参照** | ✅ 已实现 |
| `/dormitories/:id/rooms` | `pages/Rooms/List.tsx` | 寮下房间列表（分页、checkbox行选択、页面級新規登録/編集/削除ボタン+確認ダイアログ、**操作列あり：編集（/rooms/:id）・削除（Popconfirm）**、戻るは`navigate(-1)`）。**「現在人数/定員」「状態」列追加**（`room.currentOccupancy`使用、`utils/roomStatus.ts`の`getRoomOccupancyStatus`で空室=success緑/入居中=processing青/満室=warning橙タグ判定） | ✅ 已实现 |
| `/rooms/new` | `pages/Rooms/Form.tsx` | 新建房间（含 dormitoryId query param） | ✅ 已实现 |
| `/rooms/:id` | `pages/Rooms/Form.tsx` | 编辑房间（复用 Form）。**updateMutation.onSuccess時に`navigate('/dormitories/:id/rooms')`で寮部屋一覧へ遷移** | ✅ 已实现 |
| `/checkins` | `pages/Checkins/List.tsx` | 入住记录列表（分页、关键字/状态筛选、**isError時に再読み込みボタン表示**）。**入居日が未来のレコードは在寮日数列に'—'表示・ステータスTagをblue「入居予定」・退寮手続きボタン非表示**。`isPlanned`判定：`dayjs(record.checkinDate).isAfter(dayjs(), 'day')`。**「ステータスで絞り込み」フィルターは2026-06-20に三態化**：`入居予定(pending)`/`在寮中(active)`/`退寮済(checked_out)`（旧ラベル「退寮処理済」→「退寮済」に統一）。**URL `?status=` パラメータからの初期化対応**：`useEffect`で`searchParams.get('status')`を読み取り`pending`/`active`/`checked_out`のいずれかなら`setStatusFilter`（Dashboard「入居予定者数」カードからの`/checkins?status=pending`遷移に対応）。`action=new`判定と同一`useEffect`に統合 | ✅ 已实现 |
| `/checkins/new` | `pages/Checkins/New.tsx` | 新建入住（员工查询 → 宿舍联动 → 空房间选择）。**dormitoryType（male/female/mixed）で性別フィルタリング実装**、社員IDルックアップ中は`<Form disabled={lookingUp}>`で全フィールド無効化、寮選択placeholder「寮を選択してください」 | ✅ 已实现 |
| `/checkins/:id` | `pages/Checkins/Detail.tsx` | 入住详情。**入居日が未来のレコードは`isPlanned`フラグで判定（`dayjs(checkin.checkinDate).isAfter(dayjs(), 'day')`）し、ステータスTagをblue「入居予定」・退寮ボタン非表示（`status==='active' && !isPlanned`）・在寮日数を'—'表示**。戻るは`navigate(-1)`（/checkinsと/alertsの両方から遷移するため） | ✅ 已实现 |
| `/checkins/:id/checkout` | `pages/Checkins/Checkout.tsx` | 退寮手续（日期选择 + 备注）。**入居日が未来の場合（`dayjs(checkin.checkinDate).isAfter(dayjs(), 'day')`）に「入居予定の記録には退寮手続きはできません」Alertを表示して早期リターン**（`checkin.status !== 'active'`チェックより前に評価） | ✅ 已实现 |
| `/alerts/long-term` | `pages/Alerts/LongTerm.tsx` | 長期入居**警告**リスト（级别/最少天数筛选、**操作列なし**、戻るボタン→/alerts）。予警→警告に用語統一済み | ✅ 已实现 |
| `/fees` | `pages/Fees/List.tsx` | 寮費管理列表。**checkbox行選択（rowSelection type:checkbox、selectedRowKeys: number[]管理）**。行レベル確定ボタン廃止→**ページ右上「確定」ボタン**（pending選択時のみ有効、Popconfirm → confirmFees({feeIds:selectedRowKeys}))、**「編集」ボタン**（pending1件選択時のみ有効、Modal開→PUT /fees/:id）。フィルタ：社員ID/ステータス/**寮（getDormitoriesで動的取得）**/**月（DatePicker picker="month"→periodStart/periodEnd変換）**。月次一括生成ボタン+モーダル（POST /fees/generate）。処理完了後にselectedRowKeysクリア＋invalidateQueries(['fees']) | ✅ 已实现 |
| `/fees/calculate` | `pages/Fees/Calculate.tsx` | 寮费计算。**日額レートInputNumberに`max={99999}`**。日額レートフィールドの`help`テキストに「部屋の日額レートは月次一括生成で自動設定されます」を追記 | ✅ 已实现 |
| `/vacancies` | `pages/Vacancies/index.tsx` | 空房汇总（按寮统计，含空房率进度条）。**寮種別4分岐：male→男性寮/female→女性寮/mixed→混合寮（緑）/未定義→グレータグ**。Table.Summary合計行に全体空室率をProgressで表示 | ✅ 已实现 |
| `/equipment` | `pages/Equipment/List.tsx` | 设备列表（分页、关键字/状态筛选）。**ヘッダーに「備品新規登録」ボタン→Modalで備品名/カテゴリ/シリアル番号/所属寮を入力してPOST /equipment**。**2026-06-27：転寮・廃棄機能追加**：`Equipment`型に`dormitoryDeletedAt: string\|null`追加、「所属寮」列に削除済み寮は赤`削除済`Tagを付加、操作列に「転寮」ボタン（`SwapOutlined`、クリックで転寮Modalを開く）と「廃棄」ボタン（`DeleteOutlined`、`Popconfirm`経由でDELETE /equipment/:id）を追加。転寮Modal：現在の所属寮表示（削除済タグ付）＋転寮先Select（現役寮のみ、既存`dormitoriesQuery`流用）＋`転寮実行`ボタン→PUT /equipment/:id/transfer。フィルターエリアに「寮の状態で絞り込み」Select追加（現役寮のみ/削除済み寮のみ）、クライアント側フィルタリングで実現（`deletedDormFilter` state、`filteredItems`算出）。`api/equipment.ts`に`transferEquipment`/`discardEquipment`追加。`types/equipment.ts`に`dormitoryDeletedAt`フィールドと`TransferEquipmentRequest`インターフェース追加 | ✅ 已实现 |
| `/equipment/process/:checkin_id` | `pages/Equipment/Process.tsx` | 设备损坏/丢失处理记录。戻るボタン→location.state.from ?? /checkins/:id。**備品選択肢は`e.checkinId === Number(checkin_id)`でフィルタリング**。`completingId` stateで行ごとのloading管理 | ✅ 已实现 |
| `/equipment/storage` | `pages/Equipment/Storage.tsx` | 设备入库管理。**`retrievedAt === null`の行に「取出」Popconfirm→PUT /equipment/storage/:id/retrieve** | ✅ 已实现 |
| `/import` | `pages/Import/index.tsx` | Excel 批量导入向导（4步：上传→预览→确認→结果）。**完了後遷移先 /dashboard**（/calendarではなく）。エラー行があっても`validRows > 0`なら実行可（「エラー行を除いて {validRows} 件を取り込む」ラベル動的切替）。useEffect cleanupで`clearInterval(pollingRef.current)` | ✅ 已实现 |
| `/logs` | `pages/Logs/index.tsx` | 操作日志（操作者/操作类型/资源/日期范围筛选）。**isError時に再読み込みボタン表示** | ✅ 已实现 |
| `/calendar` | `pages/Calendar/index.tsx` | 寮割カレンダー（年月/地域/部屋フィルター/氏名検索、sticky固定列、退寮警告、居住期間、備考、checkbox選択、入居登録/入居編集ボタン）。**入居登録ボタンはページ遷移ではなく本画面内Modal表示**（`pages/Checkins/CheckinsNewDrawer.tsx`を再利用、`newCheckinOpen` stateで開閉、成功時は`queryClient.invalidateQueries({queryKey:['calendar']})`のみで`/calendar`に留まる。旧実装は`navigate('/checkins?action=new')`で画面遷移していたが廃止）。**room_filter型に'warning'追加**。氏名検索は大文字小文字区別なし。**列順：寮名→部屋名→入居者氏名→責任者→所属→居住期間→備考→日付セル**（部屋名を寮名直後へ移動）。**基準日（今日）の日付ヘッダーを赤字太字表示**（表示中年月が実際の今日の年月と一致する場合のみ、他月では非表示）。**入居者情報マスタートグル**「入居者情報を表示」（Checkbox、**デフォルトOFF**）が旧`showResponsible`/`showDept`/`showRemarks`の3個別チェックボックスを統合・置換、入居者氏名列も含め一括制御。**部屋単位の集約行（折りたたみ）**：トグルON時、各部屋はデフォルト1行（寮名/部屋名＋入居者数「n名」表示、日次セルは当日在籍者0名→空室色/1名以上重複なし→在籍色/2名以上重複→重複エラー色に集約、既存`hasViolation`をそのまま流用）。**未展開かつ入居者ありの部屋は、責任者/所属/居住期間/備考の4列を`colSpan={4}`で1セルに統合し、薄灰色で「部屋名をクリックして詳細表示」を一度だけ表示**（空室行・展開済み行は従来通り4列とも空欄）。**部屋名セル全体（部屋名テキスト・展開アイコン・重複警告アイコンを含むflexコンテナdiv）をクリック可能領域とし**（旧実装は展開アイコン`<span>`のみがクリック対象だったが、cellの`<div>`にonClickを移動して範囲を拡大）、`expandedRooms: Set<"dormId-roomId">` stateにより当該部屋のみ個別展開、入居者明細行（氏名/責任者/所属/居住期間/備考/日次セル、checkbox選択可）を挿入。**展開後明細行の日次セルは、在籍時の背景色に集約行と異なる薄い金色`CELL_RESIDENT_DETAIL`（`#FFF3B0`）、重複エラー時は集約行と異なる薄い赤`CELL_VIOLATION_DETAIL`（`#FFB3B3`）を使用**（集約行は従来の`CELL_RESIDENT` `#FFD700`／`CELL_VIOLATION` `#FF4444`のまま）、画面右側カレンダー領域だけでも集約行/明細行を視覚的に区別可能。凡例エリア下部に「※展開時はより淡い色で表示されます」の注記テキストを追加（新規スウォッチは追加せず1行のみ）。**「列表示」エリアに「すべて展開」「すべて折りたたむ」の独立した2ボタンを追加**（`入居者情報を表示`Checkboxの隣、`allExpandableKeys` useMemoで`filteredDormitories`全体＝ページング非依存の展開可能部屋key一覧を算出し、一括で`expandedRooms`をセット/クリア。`!showResidentInfo || allExpandableKeys.length===0`時はdisabled）。**部屋にhasViolation=trueの入居者が含まれる場合、折りたたみ時も展開アイコンを赤色化＋`WarningOutlined`赤アイコンで警告表示**。トグルOFF時は入居者関連5列を非表示にし展開アイコン自体を出さず、全部屋が集約行のみの表示になる（印刷用途）。**氏名検索ヒット時かつトグルON時は該当部屋を自動展開**（useEffect）。空室部屋（`residents.length===0`）は集約行ロジックに統合済み（旧来の専用空室行レンダリングを廃止） | ✅ 已实现 |
| `/residences/new` | `pages/Residences/New.tsx` | 入居登録（新规格）。**onSuccess時に`form.setFieldValue('dormitoryId', undefined)`＋`setSelectedDormitoryId(null)`でdormitoryIdも含めフルリセット**。キャンセル→`/dashboard` | 🚧 Placeholder |
| `/residences/:id/edit` | `pages/Residences/Edit.tsx` | 入居编辑（新规格）。**部屋セレクトに`loading={roomsQuery.isLoading}`**、**isNaN(residenceId)チェック＋「無効なURLです」Alert** | 🚧 Placeholder |
| `/alerts` | `pages/Alerts/index.tsx` | 退寮予定**警告**一覧（退寮日14日以内、残日数ソート、7日以内緊急タグ、操作列に**詳細ボタン→/checkins/:id**、右上に「長期入居**警告**を確認」リンクボタン）。予警→警告に用語統一済み | ✅ 已实现 |
| `/change-logs` | `pages/ChangeLogs/index.tsx` | 入居履歴変更ログ（操作種別/日付範囲フィルター、ページネーション）。**`api/changeLogs.ts`の`getChangeLogs`関数を使用**（直接client呼び出し廃止）、型は`ResidenceChangeLogVO` | ✅ 已实现 |
| `/departments` | `pages/Departments/index.tsx` | 所属マスタ。**sortOrderフィールドは`<InputNumber min={1}/>`**（InputではなくInputNumber）、Popconfirmに`cancelText="キャンセル"` | 🚧 Placeholder |
| `/settings` | `pages/Settings/index.tsx` | 系统设置。**TanStack Query v5対応：`useQuery`の廃止済み`onSuccess`を削除し、`useEffect(() => { if (data?.data) form.setFieldsValue(...) }, [data, form])`パターンに置き換え** | 🚧 Placeholder |

---

## Components（组件层）

| 文件 | 用途 | Props / 事件 |
|------|------|--------------|
| `components/AuthGuard.tsx` | 路由鉴权守卫，未登录重定向 `/login?redirect=...` | `children: ReactNode` |
| `components/layout/AppLayout.tsx` | 主布局（Sider + Header + Outlet）；侧边栏折叠；Header 用户下拉登出 | 无 Props，读 `useAuthStore` |
| `components/layout/Navbar.tsx` | 侧边栏菜单，精确+前缀匹配高亮当前路由。Ant Design inline Menu の collapsible SubMenu 構造：grp-overview（概要：/dashboard, /calendar）、grp-residence（入居管理：/residences/new, /checkins, **退寮警告**/alerts, **長期入居警告**/alerts/long-term）、grp-fee（費用・空室：/fees, /vacancies）、grp-facility（設備・マスタ：/equipment, /dormitories, /departments）、grp-data（データ入出力：/import, /export）、grp-log（ログ・設定：/change-logs, /logs, /settings）。`openKeys` 制御（controlled）でアクティブルート所属グループを自動展開。`useEffect` でルート変化時に `openKeys` を追加（削除しない）。`onOpenChange`はaddedキーを蓄積し既存グループを折りたたまない。`allLeafItems` は `menuGroups.flatMap` でモジュールレベルに定数化。**ラベルは「警告」統一（予警→警告）** | 无 Props，内部 `useNavigate` / `useLocation` / `useState` / `useEffect` |
| `pages/Placeholder.tsx` | 开发中页面占位符 | `title: string` |
| `components/RoomOccupancyCard.tsx` | 部屋単位の入居状況カード（`Dormitories/List.tsx`展開行で使用）。状態タグは`utils/roomStatus.ts`の`getRoomOccupancyStatus`で判定（空室/入居中/満室）、入居者氏名は最大3名表示・超過分「他n名」 | `roomName: string`, `capacity: number`, `currentOccupancy: number`, `residentNames: string[]` |

> 注意：目前无独立的业务表单组件（如 DormitoryForm、CheckinForm），逻辑内联在页面中。后续若多页面复用同一表单，应提取为 `components/{Domain}Form.tsx`。

---

## Api（接口层）

### `api/client.ts` — Axios 基础实例
- **baseURL**: `/api/v1`，timeout: 30s
- **请求拦截**：自动注入 `Authorization: Bearer <token>`
- **响应拦截**：401 → 清除本地 token，跳转 `/login`；**エラー時に `msg` → `message` へ正規化**（バックエンドの `{code,msg,data}` 形式に対応し、全 onError ハンドラーが `err.response?.data?.message` で参照できるよう変換）
- **DEV Mock（条件已收窄）**：仅当 `import.meta.env.DEV === true` **且** `error.response` 不存在（即请求未到达后端：网络错误/连接失败/超时）时，才返回空数据骨架占位（列表返回 `{items:[],total:0}`，单条返回 `null`，summary 返回 `{}`/`[]`）。**只要后端返回了任何 HTTP 响应（包括 4xx/5xx 业务错误），一律 `Promise.reject(error)`，不再伪装成 200 成功** —— 2026-06-21 修复：原逻辑仅判断 `DEV` 模式即吞掉所有非 401 错误响应（含后端已正确返回的 400/500 业务错误），导致 Excel 导入空文件校验等场景下真实错误提示被静默吞掉、误判为成功并跳转下一步。详见 `VIew/client_dev_mode_error_masking_bugfix_report.md`。

### `api/auth.ts`
| 函数 | 方法 | 端点 | 说明 |
|------|------|------|------|
| `login(data)` | POST | `/auth/login` | 正式登录 |
| `logout()` | POST | `/auth/logout` | 登出 |
| `mockLogin(email)` | — | 本地模拟 | 开发用，600ms 延迟，任意邮箱可用 |

### `api/dashboard.ts`
| 函数 | 方法 | 端点 | 说明 |
|------|------|------|------|
| `getDashboardStats()` | GET | `/dashboard/stats` | ダッシュボード統計取得。`DashboardStats`型：`{ currentResidents, pendingResidents, vacantRooms, withdrawalAlerts, duplicateErrors, longTermAlerts }`。**`pendingResidents`は2026-06-20追加**（入居予定者数）、**`longTermAlerts`は2026-06-27追加**（入居日から90日以上経過の在住者数、クリック→`/alerts/long-term`） |

### `api/dormitories.ts`
| 函数 | 方法 | 端点 |
|------|------|------|
| `getDormitories(params)` | GET | `/dormitories` |
| `getDormitory(id)` | GET | `/dormitories/:id` |
| `createDormitory(data)` | POST | `/dormitories` |
| `updateDormitory(id, data)` | PUT | `/dormitories/:id` |
| `deleteDormitory(id)` | DELETE | `/dormitories/:id` |

### `api/rooms.ts`
| 函数 | 方法 | 端点 |
|------|------|------|
| `getRooms(params)` | GET | `/rooms` |
| `getRoom(id)` | GET | `/rooms/:id` |
| `createRoom(data)` | POST | `/rooms` |
| `updateRoom(id, data)` | PUT | `/rooms/:id` |
| `deleteRoom(id)` | DELETE | `/rooms/:id` |
| `getVacantRooms(dormitoryId)` | GET | `/rooms/vacant?dormitoryId=...` |

### `api/checkins.ts`
| 函数 | 方法 | 端点 |
|------|------|------|
| `getCheckins(params)` | GET | `/checkins` |
| `getCheckin(id)` | GET | `/checkins/:id` |
| `createCheckin(data)` | POST | `/checkins` |
| `checkout(id, data)` | POST | `/checkins/:id/checkout` |
| `lookupEmployee(employeeId)` | GET | `/employees/lookup?employeeId=...` |

### `api/fees.ts`
| 函数 | 方法 | 端点 |
|------|------|------|
| `getFees(params)` | GET | `/fees` |
| `getFee(id)` | GET | `/fees/:id` |
| `calculateFee(data)` | POST | `/fees/calculate` |
| `confirmFees(data)` | POST | `/fees/confirm` |
| `generateFees(data)` | POST | `/fees/generate` |
| `updateFee(id, data)` | PUT | `/fees/:id` |

### `api/alerts.ts`
| 函数 | 方法 | 端点 |
|------|------|------|
| `getLongTermAlerts(params)` | GET | `/alerts/long-term` |
| `getAlertSummary()` | GET | `/alerts/summary` |

### `api/vacancies.ts`
| 函数 | 方法 | 端点 |
|------|------|------|
| `getVacancySummary()` | GET | `/vacancies/summary` |

### `api/equipment.ts`
| 函数 | 方法 | 端点 |
|------|------|------|
| `getEquipments(params)` | GET | `/equipment` |
| `getEquipment(id)` | GET | `/equipment/:id` |
| `createEquipment(data)` | POST | `/equipment` |
| `getEquipmentProcesses(checkinId)` | GET | `/equipment/processes?checkinId=...` |
| `createEquipmentProcess(data)` | POST | `/equipment/processes` |
| `completeProcess(id)` | POST | `/equipment/processes/:id/complete` |
| `getStorageItems()` | GET | `/equipment/storage` |
| `addToStorage(equipmentId, location, remark)` | POST | `/equipment/storage` |
| `retrieveStorageItem(id)` | PUT | `/equipment/storage/:id/retrieve` |

### `api/imports.ts`
| 函数 | 方法 | 端点 |
|------|------|------|
| `uploadImportFile(file)` | POST | `/imports/upload`（multipart/form-data） |
| `executeImport(taskId, selectedRows)` | POST | `/imports/execute` |
| `getImportTask(taskId)` | GET | `/imports/tasks/:taskId` |

### `api/calendar.ts`
- `room_filter` パラメータ型：`'all' | 'occupied' | 'vacant' | 'warning' | 'error'`（**'warning'追加済み**）

### `Export`（`pages/Export/index.tsx` 内 直接 client 呼び出し）
- エクスポートエンドポイント：`GET /exports`（旧：`/csv/export`）

### `api/logs.ts`
| 函数 | 方法 | 端点 |
|------|------|------|
| `getLogs(params)` | GET | `/logs` |

### `api/changeLogs.ts`
| 函数 | 方法 | 端点 | 说明 |
|------|------|------|------|
| `getRecentChangeLogs(pageSize?)` | GET | `/residence-change-logs` | ダッシュボード直近入退寮取得。params: `{ page:1, pageSize }` |

**型定義**:
- `ResidenceChangeLogVO` — `{ id, operationType: string, residentName, dormitoryName, roomName, operatedAt }`

---

## Utils（工具层）

### `utils/roomStatus.ts`
| 函数 | 说明 |
|------|------|
| `getRoomOccupancyStatus(currentOccupancy, capacity)` | 部屋の入居状況を判定し`{status:'vacant'\|'occupied'\|'full', label, color}`を返す。`Rooms/List.tsx`の状態タグ列と`components/RoomOccupancyCard.tsx`で共用（ロジック重複回避） |

---

## Store（状态层）

### `store/authStore.ts` — `useAuthStore`（Zustand）

| 状态/方法 | 类型 | 说明 |
|-----------|------|------|
| `token` | `string \| null` | JWT token，持久化到 localStorage |
| `user` | `UserInfo \| null` | 当前登录用户信息，持久化到 localStorage |
| `setAuth(token, user)` | Action | 登录成功后写入状态 + localStorage |
| `clearAuth()` | Action | 登出时清除状态 + localStorage |
| `isAuthenticated()` | Getter | 判断是否已登录（`!!token`） |

---

## Types（类型层）

### `types/auth.ts`
- `LoginRequest` — `{ username, password }`
- `LoginResponse` — `{ token, user: UserInfo }`
- `UserInfo` — `{ id, username, displayName, role: 'admin'|'manager'|'viewer' }`
- `ApiResponse<T>` — `{ data: T, message, code }`
- `PaginatedData<T>` — `{ items: T[], total, page, pageSize }`

### `types/dormitory.ts`
- `Dormitory` — `{ id, regionId, regionName?, name, address?, dailyRate, sortOrder, dormitoryType: 'male'|'female'|'mixed', version, createdAt, updatedAt, totalRooms, occupiedRooms }`（**`totalRooms`/`occupiedRooms`は2026-06-20追加・同日にバグ修正で`totalCapacity`/`currentOccupancy`から訂正。バックエンド`DormitoryVO`が実際に返却するのは部屋数ベースの`occupiedRooms`であり、人数ベースの容量フィールドは存在しない**）
- `CreateDormitoryRequest` — `{ regionId, name, address?, dailyRate, sortOrder }`
- `UpdateDormitoryRequest` — `{ regionId, name, address?, dailyRate, sortOrder, version }`
- `DormitoryListParams` — `{ page?, pageSize?, keyword?, regionId? }`

### `types/room.ts`
- `Room` — `{ id, dormitoryId, dormitoryName?, name, capacity, version, createdAt, updatedAt, currentOccupancy }`（**`currentOccupancy`は2026-06-20追加。バックエンド`RoomVO`は元々返却済み**）
- `CreateRoomRequest` — `{ dormitoryId, name, capacity }`
- `UpdateRoomRequest` — `{ name, capacity, version }`
- `RoomListParams` — `{ dormitoryId?, page?, pageSize? }`

### `types/checkin.ts`
- `Gender` — `'male' | 'female'`
- `CheckinStatus` — `'pending' | 'active' | 'checked_out'`（**2026-06-20三態化**：`pending`=入居予定（入居日未到来）、`active`=在寮中（入居日到来済みかつ退寮日未到来/未設定）、`checked_out`=退寮済（退寮日が既に経過）。`/checkins?status=` フィルターパラメータの合法値。**注意**：`Checkin.status` フィールド自体は引き続き `active`/`checked_out` の二値のみ返却される（バックエンド `CheckinVO.status` は退寮ボタン制御用で本改修対象外）。三態は専らフィルター用パラメータの概念であり、一覧の「ステータス」列 Tag 表示は `dayjs` による日付比較で別途判定（`Checkins/List.tsx`内ロジック、変更なし）
- `Checkin` — `{ id, employeeId, employeeName, gender, dormitoryId, dormitoryName, dormitoryType, roomId, roomNumber, checkinDate, checkoutDate, plannedCheckoutDate, stayDays, status, remark, createdAt, updatedAt }`
- `CreateCheckinRequest` — `{ employeeId, dormitoryId, roomId, checkinDate, plannedCheckoutDate?, remark? }`
- `CheckoutRequest` — `{ checkoutDate, remark? }`
- `EmployeeLookup` — `{ employeeId, employeeName, gender }`
- `CheckinListParams` — `{ page?, pageSize?, keyword?, status?, dormitoryId? }`

### `types/fee.ts`
- `FeeStatus` — `'pending' | 'confirmed' | 'paid'`
- `Fee` — `{ id, checkinId, employeeId, employeeName, dormitoryName, roomNumber, periodStart, periodEnd, amount, status, confirmedAt, confirmedBy, paidAt, createdAt }`
- `CalculateFeeRequest` — `{ checkinId, periodStart, periodEnd, dailyRate }`
- `ConfirmFeeRequest` — `{ feeIds: number[] }`
- `FeeListParams` — `{ page?, pageSize?, status?, employeeId?, dormitoryId?, periodStart?, periodEnd? }`
- `GenerateFeeRequest` — `{ year: number, month: number }`
- `GenerateFeeResult` — `{ generated: number, skipped: number, total: number }`
- `UpdateFeeRequest` — `{ periodStart: string, periodEnd: string, dailyRate: number, dailySuppliesCost: number }`

### `types/alert.ts`
- `LongTermAlert` — `{ id, checkinId, employeeId, employeeName, dormitoryName, roomNumber, checkinDate, stayDays, thresholdDays, alertLevel: 'warning'|'critical' }`
- `AlertSummary` — `{ totalAlerts, warningCount, criticalCount }`
- `AlertListParams` — `{ page?, pageSize?, alertLevel?, minDays? }`

### `types/equipment.ts`
- `EquipmentStatus` — `'normal' | 'damaged' | 'lost' | 'in_storage'`
- `ProcessStatus` — `'pending' | 'processing' | 'completed'`
- `Equipment` — `{ id, name, category, serialNumber, dormitoryId, dormitoryName, roomId, roomNumber, status, checkinId, createdAt, updatedAt }`
- `EquipmentProcess` — `{ id, checkinId, employeeId, employeeName, equipmentId, equipmentName, issueType, description, compensation, processStatus, processedAt, processedBy, createdAt }`
- `StorageItem` — `{ id, equipmentId, equipmentName, serialNumber, storageLocation, storedAt, retrievedAt, remark }`
- `CreateEquipmentProcessRequest` — `{ checkinId, equipmentId, issueType, description, compensation }`
- `CreateEquipmentRequest` — `{ name, category, serialNumber?, dormitoryId? }`
- `EquipmentListParams` — `{ page?, pageSize?, dormitoryId?, status?, keyword? }`

### `types/log.ts`
- `LogAction` — `'create'|'update'|'delete'|'login'|'logout'|'checkin'|'checkout'|'import'|'confirm'`
- `LogResource` — `'dormitory'|'room'|'checkin'|'fee'|'equipment'|'user'|'import'`
- `Log` — `{ id, userId, username, action, resource, resourceId, detail, ipAddress, createdAt }`
- `LogListParams` — `{ page?, pageSize?, username?, action?, resource?, startDate?, endDate? }`

---

## 调用链路（Call Chains）

### 登录流程
```
pages/Login/index.tsx  handleFinish()
    ↓
api/auth.ts  mockLogin()  （DEV）/ login()  （PROD）
    ↓
store/authStore.ts  setAuth(token, user)
    ↓
navigate(redirect)
```

### 寮列表加载
```
pages/Dormitories/List.tsx  useQuery(['dormitories', page, keyword, typeFilter])
    ↓
api/dormitories.ts  getDormitories(params)
    ↓
后端 DormitoryController.getList()
```

### 新建寮
```
pages/Dormitories/New.tsx  onFinish()
    ↓
api/dormitories.ts  createDormitory(data)
    ↓
后端 DormitoryController.create()
    ↓
queryClient.invalidateQueries(['dormitories'])  → navigate('/dormitories/:id')
```

### 寮一覧 展開行（部屋・入居状況表示）
```
pages/Dormitories/List.tsx  expandable.onExpandedRowsChange → expandedRowKeys
    ↓
DormitoryExpandedRow({ dormitoryId, expanded })
    ↓ useQuery enabled: expanded（展開時のみ遅延ロード、N+1回避）
api/rooms.ts  getRooms({ dormitoryId, page:1, pageSize:100 })
api/checkins.ts  getCheckins({ dormitoryId, status:'active', page:1, pageSize:100 })
    ↓ checkin.roomNumber と room.name を文字列マッチでグルーピング
components/RoomOccupancyCard.tsx  × 部屋数分（Row/Colグリッド表示）
```

### 削除寮（一括）
```
pages/Dormitories/List.tsx  rowSelection → selectedRowKeys
    ↓
Popconfirm(page-level) → selectedRowKeys.forEach deleteMutation.mutate(id)
    ↓
api/dormitories.ts  deleteDormitory(id)
    ↓
后端 DormitoryController.delete()
    ↓
queryClient.invalidateQueries(['dormitories'])
```

### 編集寮遷移
```
pages/Dormitories/List.tsx  編集ボタン（1件選択時）onClick
    ↓
navigate(`/dormitories/${selectedRowKeys[0]}`)
    ↓
pages/Dormitories/Detail.tsx
```

### 削除部屋（一括）
```
pages/Rooms/List.tsx  rowSelection → selectedRowKeys
    ↓
Popconfirm(page-level) → selectedRowKeys.forEach deleteMutation.mutate(id)
    ↓
api/rooms.ts  deleteRoom(id)
    ↓
后端 RoomController.delete()
    ↓
queryClient.invalidateQueries(['rooms'])
```

### 入住登録（核心联动流程）
```
pages/Checkins/New.tsx  handleEmployeeBlur()  （员工ID失焦）
    ↓
api/checkins.ts  lookupEmployee(employeeId)
    ↓
后端 EmployeeController.lookup()
    ↓ → 根据性别过滤 filteredDormitories

pages/Checkins/New.tsx  handleDormitoryChange(dormitoryId)  （选择宿舍）
    ↓
api/rooms.ts  getVacantRooms(dormitoryId)
    ↓
后端 RoomController.getVacant()
    ↓ → 显示空房间选项

pages/Checkins/New.tsx  onFinish()  （提交）
    ↓
api/checkins.ts  createCheckin(data)
    ↓
后端 CheckinController.create()
    ↓ → navigate('/checkins/:id')
```

### 退寮手续
```
pages/Checkins/Checkout.tsx  useQuery(['checkin', id])
    ↓
api/checkins.ts  getCheckin(id)
    ↓
后端 CheckinController.get()

pages/Checkins/Checkout.tsx  onFinish()
    ↓
api/checkins.ts  checkout(id, { checkoutDate, remark })
    ↓
后端 CheckinController.checkout()
    ↓
queryClient.invalidateQueries(['checkin', id], ['checkins'])
    ↓ → navigate('/checkins/:id')
```

### 寮費確定（一括）
```
pages/Fees/List.tsx  rowSelection → selectedRowKeys
    ↓
Popconfirm(page-level) → confirmMutation.mutate(pendingIds)
    ↓
api/fees.ts  confirmFees({ feeIds: pendingIds })
    ↓
后端 FeeController.confirm()
    ↓
queryClient.invalidateQueries(['fees'])  + setSelectedRowKeys([])
```

### 寮費編集
```
pages/Fees/List.tsx  編集ボタン（pending1件選択時）onClick
    ↓
Modal（Form: periodStart/periodEnd/dailyRate/dailySuppliesCost）
    ↓
updateMutation.mutate({ id, data })
    ↓
api/fees.ts  updateFee(id, data)  → PUT /fees/:id
    ↓
queryClient.invalidateQueries(['fees'])  + setSelectedRowKeys([])
```

### Excel 导入（4步向导）
```
pages/Import/index.tsx  handleUpload()  （Step 0）
    ↓
api/imports.ts  uploadImportFile(file)  → ImportValidationResult
    ↓
后端 ImportController.upload()
    ↓ → currentStep = 1（预览）

pages/Import/index.tsx  handleConfirmAndImport()  （Step 2）
    ↓
api/imports.ts  executeImport(taskId, rows)  → ImportTask
    ↓
后端 ImportController.execute()
    ↓ → currentStep = 3（进度轮询）

pages/Import/index.tsx  startPolling(taskId)  （每2s）
    ↓
api/imports.ts  getImportTask(taskId)
    ↓
后端 ImportController.getTask()
    ↓ → status === 'completed'|'failed' → 停止轮询
```

### ダッシュボード 3パネル表示
```
pages/Dashboard/index.tsx  useQuery(['vacancies', 'summary'])
    ↓
api/vacancies.ts  getVacancySummary()
    ↓ → OccupancyRatePanel（寮別入居率横棒グラフ）
        Card: flex:1, display:flex, flexDirection:column（左Colのdisplay:flexで全高ストレッチ＋Card自身もflexコンテナにすることでant-card-bodyがflex:1を正しく受け取る）
        Card body: padding:12px 14px, flex:1, display:flex, flexDirection:column（Card内で残り高さを占有）
        チャートエリア: flex:1, display:flex, flexDirection:column（Card body内で残り高さを占有）
        グリッドライン絶対配置コンテナ: top:0, bottom:16px（X軸ラベル高さ分オフセット）, left:72px, right:40px
        各寮行コンテナ: flex:1, display:flex, flexDirection:column, justifyContent:space-between
            → marginBottom廃止、flexスペーシングで均等分配（データ件数によらず常にCard高さを満たす）
        X軸ラベル: height:16px, flexShrink:0, marginTop:0（コンテナに押し下げられて常にチャート底部に配置）
        X軸ラベル位置: position:absolute + left:`${p}%`
        0%: transform:none（左端揃え）
        20/40/60/80%: translateX(-50%)（グリッドライン中央）
        100%: translateX(-100%)（右端揃え、はみ出し防止）
    ↓ → VacancyDonutPanel（空室/入居中ドーナツグラフ、SVG自前実装）
        右Col内に縦積み（VacancyDonutPanel → RecentChangeLogsPanel）
        右Col: display:flex, flexDirection:column, gap:12（入れ子Row/Colなし）

pages/Dashboard/index.tsx  useQuery(['dashboard', 'recent-change-logs'])
    ↓
api/changeLogs.ts  getRecentChangeLogs(5)
    ↓ → RecentChangeLogsPanel（直近5件一覧）
        Card: flex:1, display:flex, flexDirection:column（右Col内で残り高さを占有し、下端が左パネルと揃う）
        Card body: flex:1, display:flex, flexDirection:column, overflow:hidden
        スクロールコンテナ: height:88px, flexShrink:0, overflowY:auto（固定88px高さで約2件表示、残りはスクロール）
```

### 空房汇总加载
```
pages/Vacancies/index.tsx  useQuery(['vacancySummary'])
    ↓
api/vacancies.ts  getVacancySummary()
    ↓
后端 VacancyController.getSummary()
```

### 操作日志查询
```
pages/Logs/index.tsx  useQuery(['logs', ...filters])
    ↓
api/logs.ts  getLogs(params)
    ↓
后端 LogController.getList()
```

### 路由鉴权
```
App.tsx  <AuthGuard>
    ↓
components/AuthGuard.tsx  isAuthenticated()
    ↓
store/authStore.ts  isAuthenticated()  → !!token
    ↓ 未登录 → Navigate('/login?redirect=...')
    ↓ 已登录 → <AppLayout> → <Outlet>
```

---

## 未实现 / Placeholder 页面（需开发）

以下页面当前为 `<Placeholder>` 组件，尚未实现业务逻辑：

| 路由 | 功能 | 优先级 |
|------|------|--------|
| `/residences/new` | 新规格入居登録 | 高 |
| `/residences/:id/edit` | 新规格入居编辑 | 高 |
| `/change-logs` | 变更历史 | 中 |
| `/departments` | 所属主数据 | 低 |
| `/settings` | 系统设置 | 低 |

---

## 技术栈备注

- **框架**：React 18 + TypeScript + Vite
- **UI 库**：Ant Design 5.x
- **路由**：React Router v6
- **数据请求**：TanStack Query（useQuery / useMutation）
- **状态管理**：Zustand（仅 authStore）
- **HTTP 客户端**：Axios（含 DEV mock 回退）
- **日期处理**：dayjs
