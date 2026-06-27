# func_server.md — 后端功能清单

> 最后更新：2026-06-21
> 作用：代码生成前必读，确认是否已有相关功能，避免重复开发
> 包结构：`com.dorm.server`
> 技术栈：Spring Boot 2.7.18 + MyBatis (XML-only) + MySQL + Redis (Lettuce) + JJWT 0.11.5

---

## Controller 层

所有 Controller 均挂载在 `/api/v1` 前缀下（`WebMvcConfigurer` 全局配置），返回 `Result<T>{ code, msg, data }`。

### AuthController (`/api/v1/auth`)
- POST `/auth/login` — 用户登录，校验密码，生成 JWT，写入 Redis，返回 `LoginVO`
- POST `/auth/logout` — 登出，删除 Redis 中的 Token

### DormitoryController (`/api/v1/dormitories`)
- GET `/dormitories` — 分页查询宿舍列表，支持 keyword/regionId 过滤，返回 `PageVO<DormitoryVO>`
- GET `/dormitories/{id}` — 查询宿舍详情，返回 `DormitoryVO`
- POST `/dormitories` — 新增宿舍（`@Validated CreateDormitoryDTO`），返回 `DormitoryVO`
- PUT `/dormitories/{id}` — 更新宿舍（含乐观锁 version），返回 `DormitoryVO`
- DELETE `/dormitories/{id}` — 软删除宿舍（先检查是否有房间）
- PUT `/dormitories/{id}/type` — 宿舎タイプ変更（`@Validated UpdateDormitoryTypeDTO`）、全室空室チェック後に更新、返回 `DormitoryVO`

### RoomController (`/api/v1/rooms`)
- GET `/rooms` — 分页查询房间列表，支持 dormitoryId 过滤，返回 `PageVO<RoomVO>`
- GET `/rooms/vacant` — 查询空闲房间列表（**注意：路由注册在 `/{id}` 之前**），支持 dormitoryId / date（yyyy-MM-dd）参数；date 未指定时默认当日
- GET `/rooms/{id}` — 查询房间详情，返回 `RoomVO`
- POST `/rooms` — 新增房间，返回 `RoomVO`
- PUT `/rooms/{id}` — 更新房间（含乐观锁），返回 `RoomVO`
- DELETE `/rooms/{id}` — 软删除房间（先检查是否有在住记录）

### CheckinController (`/api/v1/checkins`)
- GET `/checkins` — 分页查询入住记录，支持 keyword/status/dormitoryId 过滤；`status` 合法值扩展为三态：`pending`（入居予定、check_in_date > 今日）/`active`（在寮中、check_in_date <= 今日 AND (check_out_date IS NULL OR check_out_date >= 今日)）/`checked_out`（退寮済、check_out_date IS NOT NULL AND check_out_date < 今日）。返回列 `status`（CheckinVO，active/checked_out 二値、退寮ボタン制御用）の算出式は変更なし
- GET `/checkins/{id}` — 查询入住详情，返回 `CheckinVO`
- POST `/checkins` — 新增入住（校验房间容量），返回 `CheckinVO`
- POST `/checkins/{id}/checkout` — 办理退住（含乐观锁、日期校验），返回 `CheckinVO`

### EmployeeController (`/api/v1/employees`)
- GET `/employees/lookup` — 根据社員番号查询员工信息，无记录时返回默认值，返回 `EmployeeLookupVO`

### FeeController (`/api/v1/fees`)
- GET `/fees` — 分页查询费用列表，支持 status/employeeId/dormitoryId/periodStart/periodEnd 过滤
- GET `/fees/{id}` — 查询费用详情，返回 `FeeVO`
- POST `/fees/calculate` — 计算寮費并保存（pending 状态），返回 `FeeVO`
- POST `/fees/confirm` — 批量确认费用（`ConfirmFeeDTO.feeIds`）
- POST `/fees/generate` — 寮費月次一括生成（`GenerateFeeDTO` year/month），返回 `GenerateFeeResultVO`（generated/skipped/total）
- PUT `/fees/{id}` — 費用レコード編集（`UpdateFeeDTO`）、pending ステータスのみ対象、stay_days/baseAmount/totalAmount を再計算して保存、返回 `FeeVO`
- DELETE `/fees/{id}` — 単件ソフトデリート（pending ステータスのみ対象、存在チェック・ステータスチェック付き）、返回 `Result<Void>`
- DELETE `/fees/batch` — 一括ソフトデリート（`DeleteFeeDTO.ids`、pending ステータスのみ対象）、返回 `Result<Void>`

### VacancyController (`/api/v1/vacancies`)
- GET `/vacancies/summary` — 查询所有宿舍空房概况，返回 `List<VacancySummaryVO>`

### AlertController (`/api/v1/alerts`)
- GET `/alerts/long-term` — 分页查询长期入住预警（>90天 warning；>180天 critical），返回 `PageVO<LongTermAlertVO>`
- GET `/alerts/summary` — 查询预警汇总统计（warningCount/criticalCount/withdrawalCount），返回 `AlertSummaryVO`
- GET `/alerts/withdrawal` — 查询即将退住预警（15天内），返回 `List<WithdrawalAlertVO>`

### EquipmentController (`/api/v1/equipment`)
- GET `/equipment` — 分页查询设备列表，支持 dormitoryId/status/keyword 过滤
- GET `/equipment/{id}` — 查询设备详情，返回 `EquipmentVO`
- GET `/equipment/processes` — 查询指定入居ID的设备处理记录，参数 checkinId
- POST `/equipment/processes` — 新增设备处理记录（损坏/丢失），更新设备状态
- POST `/equipment/processes/{id}/complete` — 完成处理，将设备恢复 normal 状态
- GET `/equipment/storage` — 查询所有设备库存列表
- POST `/equipment/storage` — 将设备添加到库存，更新设备状态为 in_storage
- PUT `/equipment/{id}/transfer` — 備品転寮（`TransferEquipmentDTO.targetDormitoryId`、現役寮チェック付き）、返回 `EquipmentVO`
- DELETE `/equipment/{id}` — 備品廃棄（ソフトデリート）、返回 `Result<Void>`

### ImportController (`/api/v1/imports`)
- POST `/imports/upload` — 上传 Excel 并校验，有效数据暂存 Redis（30分钟），返回 `ImportValidationResultVO`（含 tempKey）
- POST `/imports/execute` — 异步执行导入（`@Async`），返回任务 `ImportTaskVO`（含 taskId）
- GET `/imports/tasks/{taskId}` — 轮询查询导入任务状态，返回 `ImportTaskVO`

### LogController (`/api/v1/logs`)
- GET `/logs` — 分页查询操作日志，支持 username/action/resource/startDate/endDate 过滤

### RegionController (`/api/v1/regions`)
- GET `/regions` — 查询所有地域列表（按 sort_order 升序），结果缓存 Redis

### DepartmentController (`/api/v1/departments`)
- GET `/departments` — 查询所有部门列表（按 sort_order 升序），结果缓存 Redis
- GET `/departments/page` — 分页查询部门列表，支持 keyword 过滤，返回 `PageVO<DepartmentVO>`
- POST `/departments` — 新增部门（`@RequestBody Map<String,Object>` name/sortOrder），返回 `DepartmentVO`，清除 Redis 缓存
- PUT `/departments/{id}` — 更新部门（`@RequestBody Map<String,Object>` name/sortOrder），返回 `DepartmentVO`，清除 Redis 缓存
- DELETE `/departments/{id}` — 逻辑删除部门，清除 Redis 缓存

### ResidenceController (`/api/v1/residences`)
- GET `/residences/{id}` — 查询入居履歴详情，返回 `ResidenceVO`
- POST `/residences` — 新增入居履歴，返回 `ResidenceVO`
- PUT `/residences/{id}` — 更新入居履歴（含乐观锁），返回 `ResidenceVO`
- DELETE `/residences/{id}` — 软删除入居履歴

### DashboardController (`/api/v1/dashboard`)
- GET `/dashboard/stats` — ダッシュボード統計データ取得（在籍者数/入居予定者数/空室数/退寮警告/重複エラー/長期滞在警告数）、返回 `DashboardStatsVO`

### CalendarController (`/api/v1/calendar`)
- GET `/calendar` — 查询指定年月的入住/退住日历事件，参数 regionId/year/month，返回 `CalendarDataVO`

### ResidenceChangeLogController (`/api/v1/residence-change-logs`)
- GET `/residence-change-logs` — 分页查询入居履歴変更ログ，支持 operationType(INSERT/UPDATE/DELETE)/from/to 过滤，返回 `PageVO<ResidenceChangeLogVO>`

### EmployeeMasterController (`/api/v1/employee-master`)
- GET `/employee-master/search?keyword=` — 氏名・社員番号でキーワード検索（コンボボックス用）、在職者のみ（status=1）最大20件返却、返回 `List<EmployeeMasterVO>`
- GET `/employee-master/next-dispatch-id?prefix=D` — 次の派遣社員番号を自動生成（プレフィックス対応：D=大連/S=瀋陽/C=CDXT、residence_histories.employee_id LIKE '{prefix}%' の最大値+1、ゼロ埋め6桁、prefix未指定時は "D" をデフォルト使用）、返回 `Result<String>`
- GET `/employee-master/{employeeId}` — 社員番号で社員マスタ1件取得、返回 `EmployeeMasterVO`

---

> **asyncLog 追加済みコントローラー（操作ログ書き込み）:**
> - AuthController: ログイン成功後・ログアウト後に `LogService.asyncLog()` を呼び出し
> - DormitoryController: 新増/更新/削除後に `LogService.asyncLog()` を呼び出し
> - RoomController: 新増/更新/削除後に `LogService.asyncLog()` を呼び出し
> - CheckinController: 入居/退寮後に `LogService.asyncLog()` を呼び出し
> - FeeController: 寮費確定後に `LogService.asyncLog()` を呼び出し
> - ResidenceController: 新増/更新/削除後に `LogService.asyncLog()` を呼び出し、かつ `ResidenceChangeLogService.recordChange()` も呼び出し

---

## Service 层

### AuthService / AuthServiceImpl
- `login(LoginDTO)` — `@Transactional` 用户登录：查用户→校验状态→BCrypt密码比对→生成JWT→写Redis→更新最后登录时间
- `logout(Long userId)` — 删除 Redis Token

### DormitoryService / DormitoryServiceImpl
- `listDormitories(page, pageSize, keyword, regionId)` — 分页查询
- `getDormitoryById(Integer id)` — 查详情（不存在抛 BusinessException）
- `createDormitory(CreateDormitoryDTO)` — `@Transactional` 新增
- `updateDormitory(Integer id, UpdateDormitoryDTO)` — `@Transactional` 乐观锁更新
- `deleteDormitory(Integer id)` — `@Transactional` 软删除（前置：`countActiveResidentsByDormitoryId` で在住者 > 0 なら拒否、在住者 0 の場合は `softDeleteRoomsByDormitoryId` で配下全房間を連鎖ソフトデリート後に宿舎本体をソフトデリート）
- `updateDormitoryType(Integer id, UpdateDormitoryTypeDTO)` — `@Transactional` 宿舎タイプ変更（前置：全室空室チェック、在住者>0→BusinessException(DORMITORY_TYPE_CHANGE_DENIED)）

### EmployeeMasterService / EmployeeMasterServiceImpl
- `searchByKeyword(String keyword)` — キーワードで社員マスタを検索（最大20件、在職者のみ）
- `getByEmployeeId(String employeeId)` — 社員番号で社員マスタ1件取得
- `getNextDispatchId(String prefix)` — 派遣社員番号自動生成（プレフィックス対応：D=大連/S=瀋陽/C=CDXT、residenceHistories の LIKE '{prefix}%' 最大値+1、形式: D000001/S000001/C000001、prefix=null/"" 時は "D" をデフォルト使用）

### RoomService / RoomServiceImpl
- `listRooms(dormitoryId, page, pageSize)` — 分页查询
- `getRoomById(Integer id)` — 查详情
- `createRoom(CreateRoomDTO)` — `@Transactional` 新增
- `updateRoom(Integer id, UpdateRoomDTO)` — `@Transactional` 乐观锁更新
- `deleteRoom(Integer id)` — `@Transactional` 软删除（前置：检查在住记录）
- `getVacantRooms(Integer dormitoryId, String date)` — 查询空闲房间列表（指定日期时点，date 为 null 时默认 LocalDate.now()）

### CheckinService / CheckinServiceImpl
- `listCheckins(page, pageSize, keyword, status, dormitoryId)` — 分页查询；`status` パラメータはそのまま Mapper に透過（Service 層でのバリデーションなし）、三態フィルター条件は ResidenceHistoryMapper.xml 側で判定
- `getCheckinById(Integer id)` — 查详情
- `createCheckin(CreateCheckinDTO)` — `@Transactional` 新增（校验房間容量→性別制約チェック→入居登録）；性別制約：宿舍タイプ male+入居者female→例外、宿舍タイプ female+入居者male→例外、mixed→制限なし
- `checkout(Integer id, CheckoutDTO)` — `@Transactional` 退住（乐观锁、日期校验）
- `lookupEmployee(String employeeId)` — 按社員番号查员工（无记录返回默认值）

### FeeService / FeeServiceImpl
- `listFees(page, pageSize, status, employeeId, dormitoryId, periodStart, periodEnd)` — 分页查询
- `getFeeById(Long id)` — 查详情
- `calculateFee(CalculateFeeDTO)` — `@Transactional` 计算寮費并保存（pending）
- `confirmFees(ConfirmFeeDTO)` — `@Transactional` 批量确认费用状态
- `generateMonthlyFees(GenerateFeeDTO)` — `@Transactional` 月次一括生成：指定年月に在寮していた全入居者の費用を生成（重複チェックあり）、`GenerateFeeResultVO` を返す
- `updateFee(Long id, UpdateFeeDTO)` — `@Transactional` 費用レコード編集：存在チェック→pending チェック→日付バリデーション（periodEnd > periodStart）→stayDays/baseAmount/totalAmount 再計算→DB 更新→更新後 FeeVO を返す
- `deleteFee(Long id)` — `@Transactional` 単件ソフトデリート：selectById で存在チェック→pending ステータスチェック（非 pending は FEE_NOT_PENDING 例外）→softDeleteById→影響行数 0 の場合 FEE_NOT_FOUND 例外
- `batchDeleteFees(List<Long> ids)` — `@Transactional` 一括ソフトデリート：空リストチェック（FEE_DELETE_IDS_EMPTY 例外）→batchDelete 実行

### VacancyService / VacancyServiceImpl
- `getVacancySummary()` — 查询各宿舍空房概况（结果缓存 Redis VACANCY_SUMMARY，5分钟）

### AlertService / AlertServiceImpl
- `listLongTermAlerts(page, pageSize, alertLevel, minDays)` — 分页查询长期预警。**2026-06-21 第18回修正**：内部将 alertLevel 对应的天数区间（`effectiveMinDays`/`effectiveMaxDays`）下推到 SQL 层 WHERE 子句过滤，而非「先按 stay_days 排序分页、再在 Java 内存中按 alertLevel 二次过滤」。`alertLevel=warning` 时 `[90,180)`，`alertLevel=critical` 时 `[180,+∞)`，显式传入 `minDays` 时优先级最高且不强加上限，`alertLevel` 为空时保持默认 `[90,+∞)` 全部返回。`total` 直接取数据库精确统计值，不再在内存过滤后重新计算
- `getAlertSummary()` — 汇总统计（缓存 Redis ALERT_SUMMARY，10分钟）
- `getWithdrawalAlerts()` — 15天内退住预警列表

### EquipmentService / EquipmentServiceImpl
- `listEquipment(page, pageSize, dormitoryId, status, keyword)` — 分页查询
- `getEquipmentById(Integer id)` — 查详情
- `listProcessesByCheckinId(Integer checkinId)` — 查设备处理记录
- `createProcess(CreateEquipmentProcessDTO)` — `@Transactional` 新增处理记录+更新设备状态
- `completeProcess(Long id)` — `@Transactional` 完成处理+設備恢复 normal
- `listStorage()` — 查所有库存
- `addToStorage(AddToStorageDTO)` — `@Transactional` 添加库存+更新设备状态 in_storage
- `transferEquipment(Integer id, Integer targetDormitoryId)` — `@Transactional` 備品転寮：設備存在チェック→`DormitoryMapper.selectById`（現役寮チェック、deleted_at IS NULL のみ返す）→`equipmentMapper.transferDormitory`→更新後 EquipmentVO を返す
- `discardEquipment(Integer id)` — `@Transactional` 備品廃棄：設備存在チェック→`equipmentMapper.softDeleteById`

### ImportService / ImportServiceImpl
- `uploadAndValidate(MultipartFile)` — 解析 Excel，校验格式，有效数据存 Redis（30分钟），返回 tempKey
- `executeImport(ExecuteImportDTO)` — 创建任务ID，异步调用 `doImportAsync()`
- `doImportAsync(taskId, tempKey)` — `@Async` 异步执行，内部调用 `batchInsertResidences()`
- `batchInsertResidences(List<Object>)` — `@Transactional` 批量 DB 写入（事务边界）
- `getTaskStatus(String taskId)` — 查询任务进度

### LogService / LogServiceImpl
- `listLogs(page, pageSize, username, action, resource, startDate, endDate)` — 分页查询日志
- `asyncLog(username, action, resource, resourceId, detail, ipAddress)` — `@Async` 异步写入日志

### RegionService / RegionServiceImpl
- `listRegions()` — 查询所有地域（Redis 缓存 REGION_LIST）

### DepartmentService / DepartmentServiceImpl
- `listDepartments()` — 查询所有部门（Redis 缓存 DEPARTMENT_LIST）
- `listDepartmentsPage(page, pageSize, keyword)` — 分页查询部门（keyword 模糊匹配 name）
- `createDepartment(name, sortOrder)` — `@Transactional` 新增部门，清除 DEPARTMENT_LIST 缓存，返回 DepartmentVO
- `updateDepartment(id, name, sortOrder)` — `@Transactional` 更新部门，清除 DEPARTMENT_LIST 缓存，返回 DepartmentVO
- `deleteDepartment(id)` — `@Transactional` 逻辑删除部门，清除 DEPARTMENT_LIST 缓存

### ResidenceService / ResidenceServiceImpl
- `getResidenceById(Integer id)` — 查询入居履歴详情
- `createResidence(CreateResidenceDTO)` — `@Transactional` 新增
- `updateResidence(Integer id, UpdateResidenceDTO)` — `@Transactional` 乐观锁更新
- `deleteResidence(Integer id)` — `@Transactional` 软删除

### ResidenceChangeLogService / ResidenceChangeLogServiceImpl
- `listChangeLogs(page, pageSize, operationType, from, to)` — 分页查询入居履歴変更ログ
- `recordChange(residenceHistoryId, operationType, operatedBy, residentName, dormitoryName, roomName)` — `@Async` 非同期で変更ログを `residence_change_logs` テーブルに書き込む

### DashboardService / DashboardServiceImpl
- `getStats()` — 統計データ集計：`currentResidents`（在寮中）/`pendingResidents`（入居予定、2026-06-20追加）/`vacantRooms`（空室数）/`withdrawalAlerts`（退寮警告14日以内）/`duplicateErrors`（重複エラー）/`longTermAlerts`（長期滞在警告数、2026-06-27追加）を DashboardMapper から集計して DashboardStatsVO に詰めて返す

### CalendarService / CalendarServiceImpl
- `getCalendarData(regionId, year, month, roomFilter)` — 查询指定年月的入住/退住日历数据；roomFilter 支持 `vacant`（空室のみ）、`occupied`（入居中のみ）、`error`（重複エラー部屋のみ：少なくとも1名の居住者に hasViolation=true が設定されている部屋）、`warning`（退寮14日以内警告のある部屋のみ：少なくとも1名の居住者に warning=true が設定されている部屋）、`all` / 未指定（全部屋）

---

## Mapper 层

### DormitoryMapper → `dormitories` 表
- `selectPageList(keyword, regionId, offset, pageSize)` — 分页查询（含 region JOIN）；`occupied_rooms` は `rh.id IS NOT NULL AND rh.check_in_date <= CURDATE() AND (rh.check_out_date IS NULL OR rh.check_out_date >= CURDATE()) AND rh.deleted_at IS NULL` で判定（在寮中＝activeのみカウント）。**2026-06-20 第17回修正**：`rh.id IS NOT NULL` を追加し NULLトラップ（入居履歴が一度も無い部屋を LEFT JOIN の NULL 列が `check_out_date IS NULL` に該当して誤って「在寮中」とカウントしてしまうバグ）を修正、`check_in_date`/`check_out_date` の三態判定を追加し前倒し退寮登録（退寮日設定済だが未到来）の部屋がカウント漏れするバグも修正。`/checkins`・`/rooms`・Dashboard と同一口径に統一
- `selectPageCount(keyword, regionId)` — 统计总数
- `selectVoById(id)` — 查 VO（含地域名称）；`occupied_rooms` の口径は selectPageList と同一（2026-06-20 第17回修正、同上）
- `selectById(id)` — 查实体
- `insert(Dormitory)` — 新增
- `updateWithVersion(Dormitory)` — 乐观锁更新（`AND version = #{version}`）
- `softDeleteById(id)` — 软删除（SET deleted_at = NOW()）
- `countRoomsByDormitoryId(dormitoryId)` — 统计房间数（空室チェック等補助用）
- `selectAllForVacancy()` — 查所有未删除宿舍（空房汇总用）
- `selectDormitoryTypeByRoomId(roomId)` — 房間IDから宿舎タイプ取得（性別制約チェック用、rooms JOIN dormitories）
- `countActiveResidentsByDormitoryId(dormitoryId)` — 宿舎IDの全房間の在住者数カウント（check_out_date IS NULL、宿舎削除前チェック・タイプ変更前空室チェック共用）
- `updateDormitoryType(id, dormitoryType)` — 宿舎タイプを更新（SET dormitory_type）
- `softDeleteRoomsByDormitoryId(dormitoryId)` — 指定宿舎IDの配下全房間を一括ソフトデリート（UPDATE rooms SET deleted_at=NOW()、宿舎削除時の連鎖削除用）

### EmployeeMasterMapper → `employee_master` 表
- `searchByKeyword(keyword)` — 氏名・社員番号のキーワード検索（最大20件、在職者のみ、departments JOIN、返却型 `List<EmployeeMasterVO>`）
- `selectVoByEmployeeId(employeeId)` — 社員番号で VO 取得（departments JOIN）
- `selectByEmployeeId(employeeId)` — 社員番号で実体取得

### ResidenceHistoryMapper（追加メソッド）
- `selectMaxDispatchId(@Param("prefix") String prefix)` — employee_id LIKE '{prefix}%' の最大値を取得（プレフィックス別自動採番用、deleted_at IS NULL のみ）

### RoomMapper → `rooms` 表
- `selectPageList(dormitoryId, offset, pageSize)` — 分页查询；`current_occupancy`（COUNT(rh.id)）の JOIN 条件は `check_in_date <= CURDATE() AND (check_out_date IS NULL OR check_out_date >= CURDATE())`（在寮中＝三態の active のみカウント、入居予定者は含まない）。`GET /checkins?status=active` の在住者氏名リストと同一の口径に統一済み（2026-06-20）
- `selectPageCount(dormitoryId)` — 统计总数
- `selectVoById(id)` — 查 VO（含宿舍名、入住人数）；`current_occupancy` の口径は selectPageList と同一（active のみ）
- `selectById(id)` — 查实体
- `selectVacantRooms(dormitoryId, date)` — 查指定日期时点的空闲房间（check_in_date <= date AND (check_out_date IS NULL OR check_out_date > date)）
- `insert(Room)` — 新增
- `updateWithVersion(Room)` — 乐观锁更新
- `softDeleteById(id)` — 软删除
- `countActiveResidents(roomId)` — 当前在住人数（容量校验用、`check_out_date IS NULL` の単純判定を維持。意図的に三態化していない：予約済み・未入居者も部屋枠を圧迫すべき＝二重予約防止のため、現状の単純判定が正しい業務要件。本次未変更）
- `selectByDormitoryId(dormitoryId)` — 查宿舍下所有房间实体（Entity，空房统计用）
- `selectVoListByDormitoryId(dormitoryId)` — 查宿舍下所有房间 VO（含 currentOccupancy，空房汇总专用，替代 selectPageList+Integer.MAX_VALUE，`/vacancies` 画面専用）。`current_occupancy` は `rh.id IS NOT NULL AND rh.check_in_date <= CURDATE() AND (rh.check_out_date IS NULL OR rh.check_out_date >= CURDATE()) AND rh.deleted_at IS NULL` で判定。**2026-06-20 第17回修正**：旧 `check_out_date IS NULL` 単純判定では入居履歴が一度も無い部屋が LEFT JOIN の NULL 列により誤って「入居中」とカウントされる NULLトラップ Bug があったため、DormitoryMapper の `occupied_rooms` と同一の三態判定に修正し口径統一

### ResidenceHistoryMapper → `residence_histories` 表
- `selectPageList(keyword, status, dormitoryId, offset, pageSize)` — 分页查询；`status` 三態フィルター：`pending`=`check_in_date > CURDATE()`／`active`=`check_in_date <= CURDATE() AND (check_out_date IS NULL OR check_out_date >= CURDATE())`／`checked_out`=`check_out_date IS NOT NULL AND check_out_date < CURDATE()`。**注意**：`checkinSelectColumns`（共通列定義）が返す `status` 列（`CASE WHEN check_out_date IS NULL THEN 'active' ELSE 'checked_out' END`、退寮ボタン制御用の二値）はこのフィルター条件と完全に独立しており、変更されていない
- `selectPageCount(keyword, status, dormitoryId)` — 统计总数（`status` 三態フィルターは selectPageList と同一ロジック）
- `selectCheckinVoById(id)` — 查 CheckinVO（含宿舍、房间、部门信息）
- `selectById(id)` — 查实体
- `selectResidenceVoById(id)` — 查 ResidenceVO
- `insert(ResidenceHistory)` — 新增
- `updateWithVersion(ResidenceHistory)` — 乐观锁更新
- `checkout(id, checkoutDate, remark, version)` — 办理退住（乐观锁）
- `softDeleteById(id)` — 软删除
- `selectEmployeeLookupByEmployeeId(employeeId)` — 按社員番号查员工
- `selectLongTermAlerts(minDays, maxDays, keyword, offset, pageSize)` — 长期预警列表。**2026-06-21 第18回修正**：新增 `maxDays`（可为 null）参数，SQL WHERE 子句增加 `<if test="maxDays != null">AND DATEDIFF(CURDATE(), rh.check_in_date) &lt; #{maxDays}</if>`，在 `LIMIT` 分页之前先按天数区间精确过滤，修复 `alertLevel=warning` 时 critical 记录（天数更大）在排序分页阶段占满名额、导致 warning 结果恒为空的 Bug
- `selectLongTermAlertCount(minDays, maxDays, keyword)` — 长期预警总数。同上新增 `maxDays` 参数，与 `selectLongTermAlerts` 过滤口径一致
- `selectWithdrawalAlerts(days)` — 即将退住预警
- `selectAlertCounts(warningDays, criticalDays, withdrawDays)` — 预警汇总统计
- `selectCalendarEvents(regionId, year, month)` — 日历事件统计

### FeeMapper → `fees` 表
- `selectPageList(status, employeeId, dormitoryId, periodStart, periodEnd, offset, pageSize)` — 分页查询
- `selectPageCount(...)` — 统计总数
- `selectVoById(id)` — 查 FeeVO
- `selectById(id)` — 查实体
- `insert(Fee)` — 新增
- `batchConfirm(feeIds)` — 批量确认（UPDATE SET status=confirmed）
- `selectActiveInMonth(monthStart, monthEnd)` — 指定月に在寮していた入居記録を全件取得（residence_histories JOIN rooms JOIN dormitories）、`d.daily_rate`（dormitories テーブルの日額）を daily_rate として返す（rooms.daily_rate カラム不存在のため COALESCE 廃止）。返却型 `List<Map<String, Object>>`。
- `countByResidenceAndPeriod(residenceId, periodStart, periodEnd)` — 同一入居ID・同一期間の費用レコード重複件数チェック
- `updateById(Fee)` — 費用レコード更新（WHERE id=#{id} AND status='pending' AND deleted_at IS NULL）、period_start/period_end/stay_days/daily_rate/base_amount/daily_supplies_cost/total_amount/updated_at を更新、影響行数 0 の場合は対象なし
- `softDeleteById(id)` — 単件ソフトデリート（SET deleted_at = NOW() WHERE id=#{id} AND deleted_at IS NULL）
- `batchDelete(ids)` — 一括ソフトデリート（SET deleted_at = NOW() WHERE id IN (...) AND deleted_at IS NULL）

### EquipmentMapper → `equipment` 表
- `selectPageList(dormitoryId, status, keyword, offset, pageSize)` — 分页查询（`equipmentColumns` に `dormitory_deleted_at` 含む、JOIN 条件から `d.deleted_at IS NULL` 削除済み）
- `selectPageCount(dormitoryId, status, keyword)` — 统计总数
- `selectVoById(id)` — 查 EquipmentVO（同上 JOIN 修正済み）
- `selectById(id)` — 查实体
- `updateStatus(id, status)` — 更新设备状态
- `transferDormitory(id, targetDormitoryId)` — 転寮（dormitory_id 更新、room_id を NULL クリア）
- `softDeleteById(id)` — 廃棄（deleted_at = NOW()）

### EquipmentProcessMapper → `equipment_processes` 表
- `selectByCheckinId(checkinId)` — 按入居ID查处理记录列表
- `selectVoById(id)` — 查 EquipmentProcessVO
- `selectById(id)` — 查实体
- `insert(EquipmentProcess)` — 新增
- `completeProcess(id)` — 完成处理（SET status=completed）

### EquipmentStorageMapper → `equipment_storage` 表
- `selectAll()` — 查所有库存
- `selectVoById(id)` — 查 StorageItemVO
- `insert(EquipmentStorage)` — 新增

### OperationLogMapper → `operation_logs` 表
- `selectPageList(username, action, resource, startDate, endDate, offset, pageSize)` — 分页查询
- `selectPageCount(...)` — 统计总数
- `insert(OperationLog)` — 新增日志

### RegionMapper → `regions` 表
- `selectAll()` — 查所有地域（按 sort_order 升序）

### DepartmentMapper → `departments` 表
- `selectAll()` — 查所有部门（按 sort_order 升序）
- `selectPage(keyword, offset, pageSize)` — 分页查询（keyword 可选模糊匹配 name）
- `selectPageCount(keyword)` — 分页总数统计
- `insert(Department)` — 新增部门（useGeneratedKeys 回填 id）
- `updateById(Department)` — 按ID更新（name/sortOrder，仅未删除记录）
- `softDeleteById(id)` — 逻辑删除（SET deleted_at = NOW()）

### ResidenceChangeLogMapper → `residence_change_logs` 表
- `insert(ResidenceChangeLog)` — 新増変更ログ
- `selectPageList(operationType, from, to, offset, pageSize)` — 分页查询（条件：operationType/operated_at 範囲；ORDER BY operated_at DESC）
- `selectPageCount(operationType, from, to)` — 統計总数

### DashboardMapper（テーブル直結なし、residence_histories/rooms 集計専用）
- `countActiveResidents()` — 現在の在籍者数（check_in_date <= CURDATE() AND (check_out_date IS NULL OR check_out_date >= CURDATE())）
- `countPendingResidents()` — 入居予定者数（check_in_date > CURDATE() AND (check_out_date IS NULL OR check_out_date >= CURDATE())、2026-06-20追加）
- `countVacantRooms()` — 空室数（在籍記録のない部屋）
- `countWithdrawalAlerts(days)` — 退寮予定 N 日以内の件数
- `countDuplicateRooms()` — 重複エラー件数（当月内に在籍期間が重なるペアが存在する部屋数）
- `countLongTermAlerts(minDays)` — 長期滞在警告件数（入居中かつ入居日から minDays 日以上経過している在住者数、2026-06-27追加）

### SysUserMapper → `sys_users` 表
- `selectByUsername(username)` — 按用户名查用户（登录用）
- `selectById(id)` — 按ID查用户
- `updateLastLoginAt(id)` — 更新最后登录时间

---

## Entity / DTO / VO

### 主要 Entity（表映射实体）

| 实体类 | 表名 | 关键字段 |
|--------|------|---------|
| `Dormitory` | `dormitories` | id, regionId, name, dormitoryType(male/female/mixed), address, dailyRate, sortOrder, version, deletedAt |
| `Room` | `rooms` | id, dormitoryId, name, capacity, dailyRate(DECIMAL/NULL、宿舎フォールバック用), remarks(TEXT/NULL), version, deletedAt |
| `ResidenceHistory` | `residence_histories` | id, employeeId, gender, roomId, departmentId, residentName, checkInDate, checkOutDate, plannedCheckoutDate, isResponsible, remarks, version, deletedAt |
| `Fee` | `fees` | id, residenceId, employeeId, employeeName, dormitoryId, dormitoryName, roomId, roomName, periodStart, periodEnd, stayDays, dailyRate, baseAmount, dailySuppliesCost, totalAmount, status(pending/confirmed), confirmedAt, deletedAt |
| `Equipment` | `equipment` | id, roomId, name, category, serialNumber, dormitoryId, checkinId, status(normal/damaged/lost/in_storage), attributes, version, deletedAt |
| `EquipmentProcess` | `equipment_processes` | id, equipmentId, checkinId, processType(damaged/lost), description, cost, status(pending/completed) |
| `EquipmentStorage` | `equipment_storage` | id, equipmentId, equipmentName, category, serialNumber, storageLocation, storedAt, remarks |
| `OperationLog` | `operation_logs` | id, username, action, resource, resourceId, detail, ipAddress, operatedAt |
| `SysUser` | `sys_users` | id, username, password(BCrypt), realName, role, status(0禁用/1启用), lastLoginAt |
| `Region` | `regions` | id, name, sortOrder |
| `Department` | `departments` | id, name, sortOrder |
| `EmployeeMaster` | `employee_master` | id, employeeId(6桁数字またはD+6桁、UNIQUE), name, nameKana, gender(male/female), departmentId, status(1=在職/0=退職), createdAt, updatedAt, deletedAt |

### 主要 DTO（入参）

| DTO | 用途 | 关键字段 |
|-----|------|---------|
| `LoginDTO` | 登录 | username(@NotBlank), password(@NotBlank) |
| `CreateDormitoryDTO` | 新增宿舍 | name, dormitoryType, address, dailyRate, regionId, sortOrder |
| `UpdateDormitoryDTO` | 更新宿舍 | name, dormitoryType, address, dailyRate, regionId, sortOrder, version(@NotNull) |
| `CreateRoomDTO` | 新增房间 | dormitoryId(@NotNull), name(@NotBlank), capacity(@NotNull) |
| `UpdateRoomDTO` | 更新房间 | name(@NotBlank), capacity(@NotNull,@Min(1)), version(@NotNull)（不含 dormitoryId，房间不允许跨寮迁移） |
| `CreateCheckinDTO` | 新增入住 | roomId(@NotNull), employeeId, employeeName(@NotBlank), gender, departmentId, checkinDate(@NotNull), plannedCheckoutDate, isResponsible, remark |
| `CheckoutDTO` | 退住 | checkoutDate(@NotNull), remark, version(@NotNull) |
| `CalculateFeeDTO` | 计算寮費 | residenceId(@NotNull, @JsonProperty("checkinId")), dailyRate(@NotNull), periodStart(@NotNull), periodEnd(@NotNull) |
| `GenerateFeeDTO` | 月次一括生成 | year(@NotNull), month(@NotNull, @Min(1), @Max(12)) |
| `ConfirmFeeDTO` | 批量确认费用 | feeIds(List<Long>) |
| `DeleteFeeDTO` | 批量删除费用 | ids(List<Long>, @NotEmpty) |
| `UpdateFeeDTO` | 費用レコード編集 | periodStart(@NotNull), periodEnd(@NotNull), dailyRate(@NotNull, @DecimalMin("0.01"))（dailySuppliesCost フィールド削除済み、Service 層で BigDecimal.ZERO を固定セット） |
| `UpdateDormitoryTypeDTO` | 宿舎タイプ変更 | dormitoryType(@NotBlank, @Pattern(male/female/mixed)) |
| `CreateEquipmentProcessDTO` | 新增设备处理 | equipmentId(@NotNull), checkinId, processType(@NotBlank, @JsonProperty("issueType")), description, cost(@JsonProperty("compensation")) |
| `AddToStorageDTO` | 添加库存 | equipmentId(@NotNull), storageLocation, remarks |
| `TransferEquipmentDTO` | 備品転寮 | targetDormitoryId(@NotNull) |
| `ExecuteImportDTO` | 执行导入 | tempKey(@NotBlank) |
| `CreateResidenceDTO` | 新增入居履歴 | roomId, employeeId, residentName, gender, departmentId, checkInDate, checkOutDate(@JsonFormat yyyy-MM-dd 可选), plannedCheckoutDate, isResponsible, remarks |
| `UpdateResidenceDTO` | 更新入居履歴 | 含 version(@NotNull) |

### 主要 VO（出参）

| VO | 用途 | 关键字段 |
|----|------|---------|
| `LoginVO` | 登录响应 | token, userInfo(UserInfoVO) |
| `UserInfoVO` | 用户信息 | id, username, realName, role |
| `DormitoryVO` | 宿舍详情 | id, name, dormitoryType, address, dailyRate, regionId, regionName, totalRooms, occupiedRooms, version |
| `RoomVO` | 房间详情 | id, dormitoryId, dormitoryName, name, capacity, currentOccupancy（`selectPageList`/`selectVoById`/`selectVoListByDormitoryId` は三態判定で active のみカウント＝口径統一済み。`countActiveResidents`（容量校験専用）のみ意図的に `check_out_date IS NULL` の単純判定を維持、二重予約防止のための業務要件、2026-06-20 第17回修正）, version |
| `CheckinVO` | 入住详情 | id, employeeId, employeeName, gender, dormitoryId, dormitoryName, roomId, roomNumber, departmentId, departmentName, checkinDate, checkoutDate, plannedCheckoutDate, stayDays(DATEDIFF计算), status(active/checked_out), isResponsible, remark, version |
| `EmployeeLookupVO` | 员工查询 | employeeId, employeeName, gender, departmentId, departmentName |
| `FeeVO` | 费用详情 | id, residenceId(@JsonProperty("checkinId")), employeeId, employeeName, dormitoryId, dormitoryName, roomId, roomName(@JsonProperty("roomNumber")), periodStart, periodEnd, stayDays, dailyRate, baseAmount, dailySuppliesCost, totalAmount(@JsonProperty("amount")), status, confirmedAt, createdAt |
| `VacancySummaryVO` | 空房概况 | dormitoryId, dormitoryName, regionName, totalRooms, occupiedRooms, vacantRooms, occupancyRate, dormitoryType, maintenanceRooms(固定0), vacancyRate(0〜1小数) |
| `LongTermAlertVO` | 长期预警 | id, employeeId, employeeName, dormitoryName, roomNumber, checkinDate, stayDays, alertLevel(warning/critical), thresholdDays(warning=90/critical=180), departmentName |
| `AlertSummaryVO` | 预警汇总 | warningCount, criticalCount, withdrawalCount |
| `WithdrawalAlertVO` | 退住预警 | id, employeeId, employeeName(@JsonProperty("residentName")), dormitoryName, roomNumber(@JsonProperty("roomName")), checkinDate, plannedCheckoutDate(@JsonProperty("checkOutDate")), daysUntilCheckout(@JsonProperty("remainingDays")), departmentName |
| `EquipmentVO` | 设备详情 | id, name, category, serialNumber, dormitoryId, dormitoryName, dormitoryDeletedAt(@JsonFormat), roomId, roomName(@JsonProperty("roomNumber")), checkinId, attributes, version, createdAt, updatedAt |
| `EquipmentProcessVO` | 处理记录 | id, equipmentId, equipmentName, checkinId, processType, description, cost, status, createdAt |
| `StorageItemVO` | 库存详情 | id, equipmentId, equipmentName, category, serialNumber, storageLocation, storedAt, remarks |
| `ImportValidationResultVO` | 校验结果 | tempKey, totalRows, validRows, errorRows, errors(List<ValidationError>), previewData |
| `ImportTaskVO` | 导入任务 | taskId, status(pending/processing/success/partial/failed), totalRows, successRows, failedRows, startedAt, finishedAt, errorSummary |
| `LogVO` | 操作日志 | id, username, action, resource, resourceId, detail, ipAddress, createdAt |
| `RegionVO` | 地域 | id, name, sortOrder |
| `DepartmentVO` | 部门 | id, name, sortOrder |
| `ResidenceVO` | 入居履歴 | id, employeeId, gender, roomId, departmentId, residentName, checkInDate, checkOutDate, plannedCheckoutDate, isResponsible, remarks, version |
| `CalendarDataVO` | 日历数据 | year, month, daysInMonth, dormitories(List<DormitoryVO>：id/name/rooms(List<RoomVO>：id/name/residents(List<ResidentVO>：id/residentName/department/isResponsible/checkInDate/checkOutDate/days/hasViolation/warning/warningMessage/remarks))) |
| `GenerateFeeResultVO` | 月次一括生成結果 | generated(新規生成件数), skipped(重複スキップ件数), total(対象入居件数) |
| `PageVO<T>` | 分页通用 | items, total, page, pageSize, totalPages |
| `ResidenceChangeLogVO` | 入居履歴変更ログ | id, residenceHistoryId, operationType(INSERT/UPDATE/DELETE), operatedBy, operatedAt(@JsonFormat), residentName, dormitoryName, roomName |
| `EmployeeMasterVO` | 社員マスタ | employeeId, name, nameKana, gender, departmentId, departmentName |
| `DashboardStatsVO` | ダッシュボード統計 | currentResidents（在寮中）, pendingResidents（入居予定、2026-06-20追加）, vacantRooms（空室数）, withdrawalAlerts（退寮警告）, duplicateErrors（重複エラー）, longTermAlerts（長期滞在警告数、2026-06-27追加） |

---

## 枚举 / 常量

### MessageConstants（`com.dorm.server.constant.MessageConstants`）

| 常量 | 值 |
|------|-----|
| SUCCESS | "操作成功" |
| DATA_NOT_FOUND | "数据不存在" |
| VERSION_CONFLICT | "数据已被他人修改，请刷新后重试" |
| LOGIN_FAIL | "用户名或密码错误" |
| ACCOUNT_DISABLED | "账号已被禁用，请联系管理员" |
| LOGIN_SUCCESS | "登录成功" |
| LOGOUT_SUCCESS | "登出成功" |
| DORMITORY_NOT_FOUND | "宿舍不存在" |
| DORMITORY_NAME_EXISTS | "宿舍名称已存在" |
| DORMITORY_HAS_ROOMS | "入居者がいる宿舎は削除できません" |
| ROOM_NOT_FOUND | "房间不存在" |
| ROOM_IS_FULL | "房间已满，无法入住" |
| ROOM_HAS_RESIDENTS | "房间下存在在住记录，无法删除" |
| CHECKIN_NOT_FOUND | "入住记录不存在" |
| ALREADY_CHECKED_OUT | "该记录已办理退住" |
| CHECKOUT_DATE_INVALID | "退住日期不能早于入住日期" |
| FEE_NOT_FOUND | "费用记录不存在" |
| FEE_ALREADY_CONFIRMED | "费用已确认，不可重复操作" |
| FEE_NOT_PENDING | "只有待确认状态的费用才可以删除" |
| FEE_DELETE_IDS_EMPTY | "删除的费用ID列表不能为空" |
| EQUIPMENT_NOT_FOUND | "设备不存在" |
| EQUIPMENT_PROCESS_NOT_FOUND | "设备处理记录不存在" |
| EMPLOYEE_ID_EMPTY | "社員番号不能为空" |
| FEE_ID_LIST_EMPTY | "费用ID列表不能为空" |
| IMPORT_FILE_TYPE_ERROR | "不支持的文件格式，请上传 .xlsx 或 .xls 文件" |
| IMPORT_FILE_EMPTY | "上传文件为空" |
| IMPORT_TASK_NOT_FOUND | "导入任务不存在或已过期" |
| GENDER_MISMATCH_MALE_DORM | "男性寮に女性は入居できません" |
| GENDER_MISMATCH_FEMALE_DORM | "女性寮に男性は入居できません" |
| DORMITORY_TYPE_CHANGE_DENIED | "入居者がいるため、寮タイプを変更できません" |

### SystemConstants（`com.dorm.server.constant.SystemConstants`）关键常量

| 常量 | 值 | 说明 |
|------|-----|------|
| LOG_STATUS_SUCCESS | "success" | 操作日志ステータス：成功 |
| LOG_STATUS_FAIL | "fail" | 操作日志ステータス：失敗 |
| CHECKIN_STATUS_ACTIVE | "active" | 在住 |
| CHECKIN_STATUS_CHECKED_OUT | "checked_out" | 已退住 |
| GENDER_MALE / GENDER_FEMALE | "male" / "female" | 性别 |
| DORM_TYPE_MALE/FEMALE/MIXED | "male"/"female"/"mixed" | 宿舍类型 |
| FEE_STATUS_PENDING | "pending" | 费用待确认 |
| FEE_STATUS_CONFIRMED | "confirmed" | 费用已确认 |
| EQUIPMENT_STATUS_NORMAL | "normal" | 设备正常 |
| EQUIPMENT_STATUS_DAMAGED | "damaged" | 设备损坏（EquipmentServiceImpl 中用于 processType 比对，禁止硬编码 "damaged"） |
| EQUIPMENT_STATUS_LOST | "lost" | 设备丢失 |
| EQUIPMENT_STATUS_IN_STORAGE | "in_storage" | 设备库存中 |
| PROCESS_STATUS_PENDING | "pending" | 处理待完成 |
| PROCESS_STATUS_COMPLETED | "completed" | 处理已完成 |
| LONG_TERM_WARNING_DAYS | 90 | 长期入住 warning 阈值 |
| LONG_TERM_CRITICAL_DAYS | 180 | 长期入住 critical 阈值 |
| WITHDRAWAL_ALERT_DAYS | 15 | 退住预警提前天数 |
| TOKEN_EXPIRE_SECONDS | 86400L | JWT Token 过期时间（24小时） |
| IMPORT_TEMP_EXPIRE_SECONDS | 1800L | 导入临时数据过期（30分钟） |
| IMPORT_TASK_EXPIRE_SECONDS | 3600L | 导入任务状态过期（1小时） |

### RedisKeyConstants（`com.dorm.server.constant.RedisKeyConstants`）

| 常量 | 格式 | 说明 |
|------|------|------|
| AUTH_TOKEN | `dorm:auth:token:{userId}` | 用户 JWT Token |
| IMPORT_TEMP | `dorm:import:temp:{tempKey}` | Excel 导入临时数据 |
| IMPORT_TASK | `dorm:import:task:{taskId}` | 导入任务状态 |
| REGION_LIST | `dorm:region:list` | 地域列表缓存 |
| DEPARTMENT_LIST | `dorm:department:list` | 部门列表缓存 |
| VACANCY_SUMMARY | `dorm:vacancy:summary` | 空房汇总缓存（5分钟） |
| ALERT_SUMMARY | `dorm:alert:summary` | 预警汇总缓存（10分钟） |

---

## 工程配置

### application.yml 敏感配置（环境变量化）
- DB 密码：`${DB_PASSWORD:123456}`（生产须设置 `DB_PASSWORD` 环境变量）
- JWT 密钥：`${JWT_SECRET:dorm-server-secret-key-2024-enterprise-management-system}`（生产须设置 `JWT_SECRET` 环境变量）
- JWT 有效期：86400秒（24小时）

### 统一响应格式
```java
Result<T> { Integer code; String msg; T data; }
// 成功：code=200
// 业务异常：code=业务错误码（BusinessException）
// 系统异常：code=500
```

### 全局异常处理器 HTTP 状态码（`GlobalExceptionHandler`）
**2026-06-21 第18回修正**：8个 `@ExceptionHandler` 方法全部补充 `@ResponseStatus` 注解，HTTP 状态码与异常语义对齐（此前全部缺省返回 HTTP 200，导致前端 axios 拦截器误判业务失败为成功）：

| 异常类型 | 方法 | HTTP 状态码 |
|---------|------|------------|
| `BusinessException` | `handleBusinessException` | 400 |
| `MethodArgumentNotValidException` | `handleValidationException` | 400 |
| `BindException` | `handleBindException` | 400 |
| `MissingServletRequestParameterException` | `handleMissingParamException` | 400 |
| `MethodArgumentTypeMismatchException` | `handleTypeMismatchException` | 400 |
| `MaxUploadSizeExceededException` | `handleMaxUploadSizeException` | 400 |
| `IllegalArgumentException` | `handleIllegalArgumentException` | 400 |
| `Exception`（兜底） | `handleException` | 500 |

响应体格式（`{code, msg, data}`）不变，仅 HTTP 状态码变化。

### 软删除规范
- 所有实体含 `deleted_at` 字段，查询条件加 `AND deleted_at IS NULL`
- JOIN 关联表也须加软删除条件

### 乐观锁规范
- 更新操作含 `AND version = #{version}` 条件，UPDATE 同时 `version = version + 1`
- 影响行数为 0 时抛出 `BusinessException(MessageConstants.VERSION_CONFLICT)`

---

## 变更记录

| 日期 | 变更内容 |
|------|---------|
| 2026-06-06 | 初始创建，根据代码审查报告完成以下修复：①FeeVO 添加 @JsonProperty("amount"/"roomNumber"/"checkinId")；②CalculateFeeDTO 添加 @JsonProperty("checkinId") 及 dailyRate 字段；③AuthServiceImpl.login() 添加 @Transactional；④ImportServiceImpl 抽取 batchInsertResidences() 事务子方法；⑤MessageConstants 新增 EMPLOYEE_ID_EMPTY/FEE_ID_LIST_EMPTY；⑥CheckinServiceImpl/FeeServiceImpl 硬编码字符串替换为常量；⑦EquipmentServiceImpl 硬编码 "damaged" 替换为 SystemConstants.EQUIPMENT_STATUS_DAMAGED；⑧CreateEquipmentProcessDTO 添加 @JsonProperty("issueType"/"compensation")；⑨application.yml DB密码/JWT密钥环境变量化 |
| 2026-06-06 | 第2回修正（コードレビュー指摘対応）：①FeeServiceImpl.calculateFee() dailyRate を BigDecimal.ZERO→dto.getDailyRate() に修正（P0）；②ImportServiceImpl に ResidenceHistoryMapper DI 追加、batchInsertResidences() の TODO を実装（insert 呼び出し実装済み、P0）；③WithdrawalAlertVO に @JsonProperty("residentName"/"roomName"/"checkOutDate"/"remainingDays") 追加（P1）；④VacancySummaryVO に dormitoryType/maintenanceRooms/vacancyRate フィールド追加、VacancyServiceImpl でセット（P1）；⑤LongTermAlertVO に thresholdDays フィールド追加、AlertServiceImpl でセット（P1）；⑥AlertServiceImpl.listLongTermAlerts() で alertLevel フィルタ後に total を再計算するよう修正（P1）；⑦SystemConstants に LOG_STATUS_SUCCESS/LOG_STATUS_FAIL 定数追加、LogServiceImpl のハードコード "success" を定数参照に変更（P2）；⑧RoomMapper/RoomMapper.xml に selectVoListByDormitoryId() 追加、VacancyServiceImpl で Integer.MAX_VALUE 全件取得を置き換え（P2） |
| 2026-06-07 | 第3回修正（退寮日 Bug 修正）：①CreateResidenceDTO に checkOutDate(@JsonFormat yyyy-MM-dd) フィールド追加（P0 Bug-01）；②ResidenceHistoryMapper.xml の insert SQL に check_out_date 列と #{checkOutDate} 値を追加（P0 Bug-02）；③ResidenceServiceImpl.createResidence() で history.setCheckOutDate(dto.getCheckOutDate()) 呼び出しを追加（P0 Bug-03）；④ResidenceServiceImpl.updateResidence() に退寮日≥入寮日バリデーション（checkOutDate != null && checkOutDate.isBefore(checkInDate) → BusinessException(CHECKOUT_DATE_INVALID)）を追加（P1 Bug-04） |
| 2026-06-07 | 第4回修正（フロント未送フィールド上書き Bug 修正）：①UpdateRoomDTO から dormitoryId フィールド（@NotNull）を削除、房間不允许跨寮迁移のためフロントから送信不要（P0 BUG-01）；②RoomServiceImpl.updateRoom() で room.setDormitoryId(dto.getDormitoryId()) を room.setDormitoryId(existing.getDormitoryId()) に変更（P0 BUG-01）；③ResidenceServiceImpl.updateResidence() で employeeId/gender/plannedCheckoutDate の三フィールドを existing 値フォールバック（dto.getXxx() != null ? dto.getXxx() : existing.getXxx()）に変更し、前端未送時の NULL 上書きを防止（P1 BUG-02）；④DormitoryServiceImpl.updateDormitory() で dormitoryType を existing 値フォールバック（dto.getDormitoryType() != null ? dto.getDormitoryType() : existing.getDormitoryType()）に変更し、前端未送時の NULL 上書きを防止（P1 BUG-03） |
| 2026-06-07 | 第5回修正（重複エラーカード 0 表示 Bug 修正）：①DashboardMapper.xml の countDuplicateRooms SQL を最終修正：当月内に日付範囲が重なるペアを JOIN で検出（a.check_in_date <= LAST_DAY(CURDATE()) AND (a.check_out_date IS NULL OR a.check_out_date >= 月初) AND a.check_in_date <= IFNULL(b.check_out_date,'9999-12-31') AND IFNULL(a.check_out_date,'9999-12-31') >= b.check_in_date）に変更、本日在室者のみの誤ロジックを廃棄（P0 BUG-01）；②CalendarServiceImpl.getCalendarData() に `error` フィルター分岐を追加：hasViolation=true の居住者を持つ部屋のみ残し、全部屋が除外された寮も除外する（P0 BUG-02）；③func_server.md の CalendarService.getCalendarData() 説明に roomFilter=error の動作仕様を追記 |
| 2026-06-07 | 第6回修正（カレンダー備考フィールド追加）：①CalendarDataVO.ResidentVO に remarks フィールドを追加；②ResidenceHistoryMapper.xml の selectCalendarResidents SELECT 句に rh.remarks を追加；③CalendarServiceImpl.getCalendarData() の ResidentVO 組み立て部分に resident.setRemarks(str(row.get("remarks"))) を追加；④func_server.md の CalendarDataVO 説明を実際の実装構造（年月/daysInMonth/dormitories階層/remarks含む）に更新 |
| 2026-06-07 | 第7回修正（カレンダー warning フィルター追加）：①CalendarServiceImpl.getCalendarData() に `warning` フィルター分岐を追加（`error` ブロックの直後、`else` の前）：warning=true の居住者を持つ部屋のみ残し、全部屋が除外された寮も除外する；②func_server.md の CalendarService.getCalendarData() 説明に roomFilter=warning の動作仕様を追記 |
| 2026-06-07 | 第8回修正（/logs ページデータなし・/change-logs ページデータなし修正）：①AuthController/DormitoryController/RoomController/CheckinController/FeeController/ResidenceController に `LogService.asyncLog()` 呼び出しを追加（`operation_logs` テーブルへの書き込み）；②`ResidenceChangeLog` Entity / `ResidenceChangeLogVO` / `ResidenceChangeLogMapper` / `ResidenceChangeLogMapper.xml` / `ResidenceChangeLogService` / `ResidenceChangeLogServiceImpl` / `ResidenceChangeLogController` を新規作成（`GET /api/v1/residence-change-logs`）；③ResidenceController の create/update/delete で `ResidenceChangeLogService.recordChange()` を呼び出し `residence_change_logs` テーブルに書き込み；④`supplement_change_log.sql` を `src/main/resources/` に作成（`residence_change_logs` テーブル DDL）；⑤func_server.md に ResidenceChangeLogController/Service/Mapper/VO/Entity の全情報を同期 |
| 2026-06-08 | 第9回修正（寮費月次一括生成機能追加）：①`Room.java` に `dailyRate(BigDecimal)` / `remarks(String)` フィールド追加（rooms テーブルの既存カラムをマッピング）；②`GenerateFeeDTO` 新規作成（year/@NotNull, month/@NotNull/@Min(1)/@Max(12)）；③`GenerateFeeResultVO` 新規作成（generated/skipped/total）；④`FeeMapper` に `selectActiveInMonth(monthStart, monthEnd)` / `countByResidenceAndPeriod(residenceId, periodStart, periodEnd)` を追加；⑤`FeeMapper.xml` に対応 SQL を追加（selectActiveInMonth：COALESCE(rooms.daily_rate, dormitories.daily_rate)、countByResidenceAndPeriod：重複チェック）；⑥`FeeService` に `generateMonthlyFees(GenerateFeeDTO)` を追加；⑦`FeeServiceImpl` に `generateMonthlyFees()` を実装（@Transactional、重複チェック・日額NULL/0スキップ・内部変換ユーティリティメソッド付き）；⑧`FeeController` に `POST /fees/generate` を追加（asyncLog 付き）；⑨func_server.md 全セクション同期 |
| 2026-06-08 | 第10回修正（寮費レコード編集エンドポイント追加）：①`UpdateFeeDTO` 新規作成（periodStart/@NotNull, periodEnd/@NotNull, dailyRate/@NotNull/@DecimalMin("0.01"), dailySuppliesCost/@NotNull/@DecimalMin("0.00")）；②`FeeMapper` に `updateById(Fee)` を追加（pending かつ未削除のレコードのみ対象）；③`FeeMapper.xml` に UPDATE SQL を追加；④`FeeService` に `updateFee(Long id, UpdateFeeDTO)` を追加；⑤`FeeServiceImpl` に `updateFee()` を実装（@Transactional、存在チェック・pending チェック・日付バリデーション・stayDays/baseAmount/totalAmount 再計算）；⑥`FeeController` に `PUT /fees/{id}` を追加（asyncLog 付き）；⑦func_server.md 全セクション同期 |
| 2026-06-12 | 第11回修正（社員マスタ・D番号自動生成・性別制約・宿舎タイプ切替）：①`EmployeeMaster` Entity 新規作成（employee_master テーブルマッピング）；②`EmployeeMasterVO` 新規作成（employeeId/name/nameKana/gender/departmentId/departmentName）；③`UpdateDormitoryTypeDTO` 新規作成（dormitoryType/@NotBlank/@Pattern(male/female/mixed)）；④`EmployeeMasterMapper` + `EmployeeMasterMapper.xml` 新規作成（searchByKeyword/selectVoByEmployeeId/selectByEmployeeId の3メソッド）；⑤`ResidenceHistoryMapper` に `selectMaxDispatchId()` 追加、`ResidenceHistoryMapper.xml` に対応 SQL 追加（LIKE 'D%' 最大値取得）；⑥`EmployeeMasterService` インターフェース + `EmployeeMasterServiceImpl` 実装クラス新規作成（searchByKeyword/getByEmployeeId/getNextDispatchId の3メソッド）；⑦`EmployeeMasterController` 新規作成（GET /search, GET /next-dispatch-id, GET /{employeeId}、asyncLog 付き）；⑧`DormitoryMapper` に `selectDormitoryTypeByRoomId`/`countActiveResidentsByDormitoryId`/`updateDormitoryType` を追加、`DormitoryMapper.xml` に対応 SQL 追加；⑨`DormitoryService` に `updateDormitoryType()` 追加、`DormitoryServiceImpl` に実装（全室空室チェック付き @Transactional）；⑩`DormitoryController` に `PUT /{id}/type` エンドポイント追加（asyncLog 付き）；⑪`CheckinServiceImpl.createCheckin()` に性別制約チェックロジック追加（DormitoryMapper DI、宿舎タイプと入居者性別の整合チェック、male/female/mixed の3ケース対応）；⑫`MessageConstants` に GENDER_MISMATCH_MALE_DORM/GENDER_MISMATCH_FEMALE_DORM/DORMITORY_TYPE_CHANGE_DENIED を追加；⑬func_server.md 全セクション同期 |
| 2026-06-12 | 第12回修正（空室クエリに date パラメータ追加）：①`RoomMapper.selectVacantRooms()` のシグネチャに `@Param("date") String date` を追加；②`RoomMapper.xml` の `selectVacantRooms` SQL を `check_out_date IS NULL` 判定から `check_in_date <= #{date} AND (check_out_date IS NULL OR check_out_date > #{date})` 判定に変更（将来日付での空室確認に対応）；③`RoomService.getVacantRooms()` のシグネチャに `String date` を追加；④`RoomServiceImpl.getVacantRooms()` に date フォールバック処理（null 時 `LocalDate.now().toString()`）を追加、`java.time.LocalDate` インポート追加；⑤`RoomController.getVacant()` に `@RequestParam(required = false) String date` を追加；⑥func_server.md の Controller/Service/Mapper セクションを同期 |
| 2026-06-12 | 第13回修正（月次寮費Bug修正・所属CRUD・社員番号プレフィックス対応）：①`FeeMapper.xml` の `selectActiveInMonth` SQL で `COALESCE(rm.daily_rate, d.daily_rate)` を `d.daily_rate` に変更（rooms テーブルに daily_rate カラムが存在しないため SQL エラー修正）；②`DepartmentMapper` に `selectPage/selectPageCount/insert/updateById/softDeleteById` 5メソッド追加、`Department` エンティティインポート追加；③`DepartmentMapper.xml` に対応 SQL 追加（ページネーション検索/総数/INSERT/UPDATE/論理削除）；④`DepartmentService` に `listDepartmentsPage/createDepartment/updateDepartment/deleteDepartment` 4メソッド追加、`PageVO` インポート追加；⑤`DepartmentServiceImpl` に4メソッド実装追加（`@Transactional`、キャッシュ削除は `redisUtil.delete()` を使用）；⑥`DepartmentController` に `GET /page`/`POST /`/`PUT /{id}`/`DELETE /{id}` 4エンドポイント追加；⑦`ResidenceHistoryMapper.selectMaxDispatchId()` シグネチャを `(@Param("prefix") String prefix)` に変更；⑧`ResidenceHistoryMapper.xml` の SQL を `LIKE 'D%'` から `LIKE CONCAT(#{prefix}, '%')` に変更；⑨`EmployeeMasterService.getNextDispatchId()` シグネチャを `getNextDispatchId(String prefix)` に変更；⑩`EmployeeMasterServiceImpl.getNextDispatchId()` を prefix パラメータ対応に実装変更（null/"" 時は "D" をデフォルト使用）；⑪`EmployeeMasterController.getNextDispatchId()` に `@RequestParam(required=false, defaultValue="D") String prefix` を追加；⑫func_server.md 全セクション同期 |
| 2026-06-13 | 第15回修正（UpdateFeeDTO から dailySuppliesCost フィールド削除）：①`UpdateFeeDTO` から `dailySuppliesCost` フィールド（@NotNull/@DecimalMin("0.00")）を削除（UI 非表示化に対応）；②`FeeServiceImpl.updateFee()` の totalAmount 計算を `baseAmount.add(dto.getDailySuppliesCost())` → `baseAmount` に変更、`fee.setDailySuppliesCost()` を `BigDecimal.ZERO` 固定セットに変更；③`FeeController.update()` の JavaDoc から dailySuppliesCost の記載を削除；④DB カラム `daily_supplies_cost` は引き続き存在、常に 0 として保存；⑤`Fee` Entity / `FeeVO` / `FeeMapper.xml` は変更なし |
| 2026-06-12 | 第14回修正（費用レコード単件・一括ソフトデリート追加）：①`MessageConstants` に `FEE_NOT_PENDING`（"只有待确认状态的费用才可以删除"）/ `FEE_DELETE_IDS_EMPTY`（"删除的费用ID列表不能为空"）を追加；②`FeeMapper` に `softDeleteById(@Param("id") Long id)` / `batchDelete(@Param("ids") List<Long> ids)` を追加；③`FeeMapper.xml` に `softDeleteById` / `batchDelete` UPDATE SQL を追加（deleted_at = NOW() WHERE id IN (...) AND deleted_at IS NULL）；④`DeleteFeeDTO` 新規作成（ids/@NotEmpty）；⑤`FeeService` に `deleteFee(Long id)` / `batchDeleteFees(List<Long> ids)` メソッドシグネチャを追加；⑥`FeeServiceImpl` に `deleteFee()` 実装（selectById存在チェック→pending ステータスチェック→softDeleteById）/ `batchDeleteFees()` 実装（空リストチェック→batchDelete）を追加；⑦`FeeController` に `DELETE /fees/{id}` / `DELETE /fees/batch` 2エンドポイントを追加（asyncLog 付き）；⑧func_server.md 全セクション同期 |
| 2026-06-20 | 第16回修正（在住状態「入居予定／在寮中／退寮済」三態分離）：①`DashboardMapper.xml` に `countPendingResidents` 新規追加（`check_in_date > CURDATE() AND (check_out_date IS NULL OR check_out_date >= CURDATE())`）；②`DashboardMapper.java` に `countPendingResidents()` 追加、`countActiveResidents()` の JavaDoc を実態に合わせて修正；③`DashboardStatsVO` に `pendingResidents` フィールド追加、`currentResidents` のコメントを正確な説明に修正；④`DashboardServiceImpl.getStats()` で `pendingResidents` をセット、ログ出力に追加；⑤`ResidenceHistoryMapper.xml` の `selectPageList`/`selectPageCount` の `status` 絞り込み条件を2値（active/checked_out）→3値（pending/active/checked_out）に拡張。`pending`=`check_in_date > CURDATE()`、`active`=`check_in_date <= CURDATE() AND (check_out_date IS NULL OR check_out_date >= CURDATE())`、`checked_out`=`check_out_date IS NOT NULL AND check_out_date < CURDATE()`。**`checkinSelectColumns` の `status` 列算出式（CASE WHEN check_out_date IS NULL THEN 'active' ELSE 'checked_out' END、退寮ボタン制御用）は変更なし**；⑥`RoomMapper.xml` の `selectPageList`/`selectVoById` の `current_occupancy` JOIN 条件を `check_out_date IS NULL` の単純判定から `check_in_date <= CURDATE() AND (check_out_date IS NULL OR check_out_date >= CURDATE())` に変更（在寮中＝activeのみカウント、`GET /checkins?status=active` の氏名リストと口径統一）。`countActiveResidents`（容量校験用）/`selectVoListByDormitoryId`（/vacancies専用）は意図的に変更せず単純判定のまま維持；⑦CheckinController/CheckinService は `status` パラメータの型・バリデーションに変更なし（文字列透過のため拡張不要）；⑧func_server.md 全セクション同期（Dashboard 関連の Controller/Service/Mapper/VO 章節が未記載だったため本回で新規追加）；⑨実装報告書 `VIew/checkins_dashboard_occupancy_status_backend_impl_report.md` 作成 |
| 2026-06-20 | 第17回修正（`/dormitories`「入居状況」列・`/vacancies` 空室管理画面の NULLトラップ Bug 修正）：実データで確認した2つの不具合を修正。**Bug①NULLトラップ**：部屋に入居履歴が1件も無い場合、`LEFT JOIN residence_histories rh` により `rh.*` が全て NULL となり、`rh.check_out_date IS NULL` 判定が「NULL IS NULL」で恒真になることで空室を「在寮中」と誤カウントしていた（実測：テスト寮2＝2部屋0入居履歴が2/2満室と誤表示）。**Bug②三態ロジック未同期**：`/checkins`・`/rooms`・Dashboard で既に統一した「入居日が到来済み かつ（退寮日未設定 または 退寮日未到来）」の三態判定が、本箇所のみ旧ロジックのまま残っており、前倒し退寮登録（退寮日設定済だが未到来）の部屋がカウント漏れしていた（実測：豊洲D寮＝本来2/4空室のところ1/4と誤表示）。①`DormitoryMapper.xml` の `selectPageList`／`selectVoById` の `occupied_rooms` 集計に `rh.id IS NOT NULL` 条件を追加（NULLトラップ対策）し、`rh.check_in_date <= CURDATE() AND (rh.check_out_date IS NULL OR rh.check_out_date >= CURDATE())` の三態判定に変更；②`RoomMapper.xml` の `selectVoListByDormitoryId`（`/vacancies` 画面専用、`VacancyServiceImpl.getVacancySummary()` が呼び出し）の `current_occupancy` 集計も同様に `rh.id IS NOT NULL` ＋三態判定に修正、口径を `/checkins`・`/rooms`・Dashboard と統一；③`RoomMapper.xml` の `countActiveResidents`（容量校験専用）・`selectPageList`／`selectVoById`（既に第16回で修正済み）・`selectVacantRooms` は意図的に変更せず維持；④func_server.md の DormitoryMapper／RoomMapper／RoomVO 章節を同期；⑤調査報告書 `VIew/calendar_dormitories_occupancy_investigation_report.md`（Part B）、実施報告書 `VIew/dormitories_vacancies_occupancy_nulltrap_fix_report.md` 作成。**注意**：`/vacancies` の `getVacancySummary()` 結果は Redis に5分間キャッシュされる（`RedisKeyConstants.VACANCY_SUMMARY`）ため、コード修正後・サーバー再起動後も最大5分間は旧い誤集計値が画面に残る可能性がある（キャッシュクリア・サーバー再起動は別途対応）。**コンパイル検証**：PowerShell・Bash 両ツールともパーミッション拒否のため実行不可、静的レビューのみ実施（未実機検証） |
| 2026-06-27 | 第20回修正（備品転寮・廃棄機能追加）：①`EquipmentMapper.xml` の `equipmentColumns` に `dormitory_deleted_at` を追加、`selectPageList`/`selectVoById` の dormitories JOIN から `deleted_at IS NULL` 条件を削除（廃棄寮の備品も表示可能に）、`transferDormitory`/`softDeleteById` SQL を追加；②`EquipmentVO` に `dormitoryDeletedAt`（LocalDateTime）フィールドを追加；③`EquipmentMapper` に `transferDormitory`/`softDeleteById` メソッドを追加；④`TransferEquipmentDTO` 新規作成（targetDormitoryId/@NotNull）；⑤`EquipmentService` に `transferEquipment`/`discardEquipment` を追加；⑥`EquipmentServiceImpl` に `DormitoryMapper` DI 追加・`transferEquipment`/`discardEquipment` 実装追加；⑦`EquipmentController` に `PUT /{id}/transfer`/`DELETE /{id}` エンドポイント追加；⑧func_server.md 全セクション同期 |
| 2026-06-27 | 第19回修正（DashboardStatsVO に longTermAlerts フィールド追加）：①`DashboardStatsVO` に `longTermAlerts`（長期滞在警告数）フィールド追加；②`DashboardMapper` に `countLongTermAlerts(@Param("minDays") Integer minDays)` メソッド追加；③`DashboardMapper.xml` に対応 SQL 追加（`residence_histories` で `deleted_at IS NULL AND check_in_date <= CURDATE() AND (check_out_date IS NULL OR check_out_date >= CURDATE()) AND DATEDIFF(CURDATE(), check_in_date) >= #{minDays}`）；④`DashboardServiceImpl.getStats()` で `SystemConstants.LONG_TERM_WARNING_DAYS`（=90）を引数として呼び出し VO にセット、ログ出力に `longTermAlerts` 追加；⑤`func_server.md` 全セクション同期；⑥APIドキュメント変更報告書 `VIew/dashboard_longterm_alert_api_change_report.md` 作成 |
| 2026-06-21 | 第18回修正（全局異常処理器 HTTP 200 誤判定・長期入住警告 warning 級別フィルター空結果 Bug 修正）：`test_failures_root_cause_analysis_report.md`（TC-049/TC-065由来）で確認された2つの真実缺陥を修正。**缺陥①**：`GlobalExceptionHandler` の全8個 `@ExceptionHandler` メソッドが `@ResponseStatus` 未指定のため、業務異常を含む全エラーが HTTP 200 で返り、フロント axios 拦截器（`(response) => response`、HTTP 2xx を無条件成功と判定）が業務失敗を成功と誤判定していた（宿舎削除/房間削除/Excel空ファイルアップロードの3箇所で再現確認）。8メソッド全てに `@ResponseStatus` を追加（`BusinessException`/`MethodArgumentNotValidException`/`BindException`/`MissingServletRequestParameterException`/`MethodArgumentTypeMismatchException`/`MaxUploadSizeExceededException`/`IllegalArgumentException` → 400、兜底 `Exception` → 500）、レスポンスボディ形式は変更なし。**缺陥②**：`AlertServiceImpl.listLongTermAlerts()` が `ORDER BY stay_days DESC LIMIT` でページネーションした**後**に Java 側で `alertLevel` を二次フィルタしていたため、critical レコード（日数が大きい）が先頭ページを占有し、warning レコードが分页の枠外に押し出されて常に空結果になっていた（実データ確認：critical 13件 + warning 5件、pageSize=10 で warning が完全に排出）。①`ResidenceHistoryMapper.selectLongTermAlerts`/`selectLongTermAlertCount` に `maxDays`（Integer、null可）パラメータを追加；②`ResidenceHistoryMapper.xml` の両 `<select>` に `<if test="maxDays != null">AND DATEDIFF(CURDATE(), rh.check_in_date) &lt; #{maxDays}</if>` を `minDays` 条件の直後に追加し、`LIMIT` 分页の**前**に区間を絞り込むよう変更；③`AlertServiceImpl.listLongTermAlerts()` で `alertLevel=warning` 時 `[90,180)`、`critical` 時 `[180,+∞)`、`minDays` 明示指定時は従来通り上限なし、`alertLevel` 未指定時は従来通り `[90,+∞)` 全件を SQL 層で確定させ、Java 側の `stream().filter()` 二次フィルタと `total` 再計算ロジックを削除（`total` は `selectLongTermAlertCount` の DB 実値をそのまま使用）；未使用となった `StringUtils`/`ArrayList` import を削除；④func_server.md の AlertService/AlertServiceImpl・ResidenceHistoryMapper・統一響応格式（GlobalExceptionHandler HTTP 状態码表）章節を同期；⑤実施報告書 `VIew/global_exception_status_and_alert_level_pagination_bugfix_report.md` 作成。**コンパイル・起動・curl 検証**：本セッションの Bash/PowerShell ツールが `mvn`/`java` 等の実行コマンドに対して恒常的に Permission denied となり（`pwd`/`ls`/`echo` 等の読み取り専用コマンドのみ許可）、`dangerouslyDisableSandbox` オプションを付けても解消せず、コンパイル・サーバー再起動・curl 疎通確認を実機実行できなかった。**静的コードレビューのみ実施**（全変更箇所の構文・シグネチャ整合性を目視確認、呼び出し元3箇所のシグネチャ一致を確認済み）、未実機検証。ユーザーには報告書内で明示し、別途環境で `mvn compile` 等の実行を依頼 |
