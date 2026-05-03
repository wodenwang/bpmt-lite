# bpmt-lite 初始化数据库

本目录用于放置可公开分发的初始化 SQL。

## v1.2.0 约定

- `bpmt-min.sql.gz` 是最小初始化库压缩包，解压后的数据库名为 `bpmt_min`。
- `bpmt.sql.gz` 是完整初始化库压缩包，解压后的数据库名为 `bpmt`。
- 两份 SQL 都必须自己包含 `CREATE DATABASE IF NOT EXISTS ...` 和 `USE ...`，不能依赖 Docker Compose 的 `MARIADB_DATABASE` 自动建库行为。
- `db/init/*.sql` 是本地运行目录，不提交 git。

当前已提交的是 `bpmt-min.sql.gz` 和 `bpmt.sql.gz`。原始 `bpmt.sql` 体积超过 GitHub 普通仓库 100 MiB 单文件限制，因此不直接提交；`bpmt-min.sql` 也统一改为压缩交付。`scripts/init-db.sh` 会从 `.sql.gz` 自动解压到 `db/init/`。

`bpmt.sql.gz` 来自本地删改后的 `bpmt` 数据库备份。导出时排除了失效视图 `v_demo_qj`，因为该视图引用的 demo 表已经不在当前 `bpmt` 数据库中，直接导出会导致备份失败。

已用临时 MariaDB 10.11 容器验证：

- `bpmt.sql.gz` 解压后可导入。
- `bpmt` 和 `bpmt_min` 可在同一个 MariaDB 实例中共存。
- 导入后 `bpmt` 包含 380 张表或视图；`bpmt_min` 包含 176 张表。

## 最小库来源

`bpmt-min.sql.gz` 继承 `v1.1.0` 的最小初始化库：

- 平台表结构来自旧项目 `support/hbm2ddl` 生成的 MySQL DDL。
- Activiti 表结构来自 `activiti-engine-5.16.3.jar` 内置的 MySQL DDL。
- Quartz 表结构来自 `com.riversoft:quartz-ddl:2.2.1` 中的 MySQL DDL。
- 初始化数据来自旧项目 `package/database/bpmt_init_data.xlsx`。
- `v1.5.0` 追加 `database/v1.5.0-oauth-tables.sql`，包含 `CM_THIRDPART`、`CM_THIRDPART_AUTH_CODE`、`CM_THIRDPART_ACCESS_TOKEN` 三张 OAuth 登录表。

已验证结果：

- 导入后 `bpmt_min` 包含 176 张表，其中 Activiti 24 张、Quartz 11 张、OAuth 登录表 3 张。
- 最小初始化数据包含 1 个用户、26 个菜单、27 条权限和 1 条用户角色关系。

## 完整库来源

`bpmt.sql.gz` 使用当前本地 MariaDB 中的 `bpmt` 数据库导出：

```bash
docker compose exec -T mariadb mariadb-dump -uroot -p123456 \
  --default-character-set=utf8 \
  --single-transaction \
  --routines --triggers --events \
  --ignore-table=bpmt.v_demo_qj \
  --databases bpmt > database/bpmt.sql
gzip -9 -c database/bpmt.sql > database/bpmt.sql.gz
```

原始 `database/bpmt.sql` 和 `database/bpmt-min.sql` 是本地生成文件，不提交 git。
`scripts/build-minimal-bpmt-db.py` 重新生成最小库时会同时写出本地 raw SQL 和可提交的 `database/bpmt-min.sql.gz`。

## 重新生成最小库

```bash
scripts/build-minimal-bpmt-db.py
```

如果 hbm2ddl 输出目录不存在，先在旧项目中生成 MySQL DDL：

```bash
cd /Users/wenzhewang/workspace/bpmt_project/riversoft/trunk
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-1.8.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
mvn -s settings.xml -pl support/hbm2ddl -am -DskipTests dependency:build-classpath -Dmdep.outputFile=/tmp/hbm2ddl.classpath
CP="support/hbm2ddl/target/classes:util/target/classes:platform/target/classes:$(cat /tmp/hbm2ddl.classpath)"
java -Dfile.encoding=UTF-8 -cp "$CP" com.riversoft.hbm2ddl.Main \
  /Users/wenzhewang/workspace/bpmt_project/riversoft/trunk/support/hbm2ddl \
  target/hbm \
  target/sql-bpmt-lite
```
