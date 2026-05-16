/*
 * $HeadURL: $
 * $Id: $
 * Copyright (c) 2013 by Riversoft System, all rights reserved.
 */
package com.riversoft.core.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.engine.spi.RowSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.riversoft.core.BeanFactory;
import com.riversoft.core.Config;
import com.riversoft.core.db.dialect.DatabaseMeta;
import com.riversoft.core.db.dialect.OracleColumnMapRowMapper;
import com.riversoft.core.exception.ExceptionType;
import com.riversoft.core.exception.SystemRuntimeException;

/**
 * 通用数据库服务.<br>
 * 封装通用jdbc操作。
 * 
 * @author Woden
 * 
 */
public class JdbcService {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(JdbcService.class);

	/**
	 * 数据库方言,用于分页
	 */
	private Dialect dialect;

	/**
	 * setter 方法
	 * 
	 * @param dialect
	 */
	public void setDialect(Dialect dialect) {
		this.dialect = dialect;
	}

	private Dialect getDialect() {
		if (dialect != null) {
			return dialect;
		}

		try {
			@SuppressWarnings("rawtypes")
			Class klass = Class.forName(Config.get("hibernate.dialect"));
			logger.info("使用了默认方言 {}", klass.getName());
			dialect = (Dialect) klass.newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			logger.warn("无法初始化数据库方言.", e);
			dialect = new MySQL5InnoDBDialect();// 默认mysql5
		}
		return dialect;
	}

	/**
	 * 根据sql入参创建符合数据库方言的分页sql语句
	 * 
	 * @param sql 原生sql入参
	 * @return
	 * @throws SQLException
	 */
	private PreparedStatement buildLimitSql(Connection con, String sql, int firstResult, int maxResult)
			throws SQLException {
		RowSelection selection = new RowSelection();
		selection.setFirstRow(firstResult);
		selection.setMaxRows(maxResult);
		LimitHandler limitHandler = getDialect().buildLimitHandler(sql, selection);
		PreparedStatement statement = con.prepareStatement(limitHandler.getProcessedSql());
		int col = 1;
		col += limitHandler.bindLimitParametersAtStartOfQuery(statement, col);
		col += limitHandler.bindLimitParametersAtEndOfQuery(statement, col);
		limitHandler.setMaxRows(statement);

		return statement;
	}

	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static JdbcService getInstance() {
		return BeanFactory.getInstance().getSingleBean(JdbcService.class);
	}

	/**
	 * 创建新实例
	 * 
	 * @param jdbcTemplate
	 * @return
	 */
	public static JdbcService newInstance(String key, JdbcTemplate jdbcTemplate, Dialect dialect) {
		Map<String, Object> propertyMap = new HashMap<>();
		propertyMap.put("jdbcTemplate", jdbcTemplate);
		propertyMap.put("namedParameterJdbcTemplate", new NamedParameterJdbcTemplate(jdbcTemplate));
		propertyMap.put("dialect", dialect);

		return BeanFactory.getInstance().getBean("JdbcService-new-" + key, JdbcService.class, false, false, null,
				propertyMap);
	}

	/**
	 * spring jdbc模板<br>
	 */
	protected JdbcTemplate jdbcTemplate;

	/**
	 * spring jdbc模板
	 */
	protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	/**
	 * spring auto setter
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * spring auto setter
	 * 
	 * @param namedParameterJdbcTemplate
	 */
	public void setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	/**
	 * 查询列表
	 * 
	 * @param sql
	 * @param args
	 * @return
	 */
	public List<Map<String, Object>> querySQL(String sql, Object... args) {
		try {
			return jdbcTemplate.query(sql, args, getRowMapper());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		}
	}

	/**
	 * 查询列表
	 * 
	 * @param sql
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> querySQL(String sql, Map<String, ?> params) {
		try {
			return namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource(params), getRowMapper());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		}
	}

	/**
	 * 查询值
	 * 
	 * @param sql
	 * @param args
	 * @return
	 */
	public Map<String, Object> findSQL(String sql, Object... args) {
		try {
			return (Map) jdbcTemplate.queryForObject(sql, args, getRowMapper());
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	/**
	 * 查询值
	 * 
	 * @param sql
	 * @param params
	 * @return
	 */
	public Map<String, Object> findSQL(String sql, Map<String, ?> params) {
		try {
			return (Map) namedParameterJdbcTemplate.queryForObject(sql, params, getRowMapper());
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	};

	/**
	 * 执行sql语句
	 * 
	 * @param sql
	 */
	public void executeSQL(String sql, Object... args) {
		jdbcTemplate.update(sql, args);
	}

	/**
	 * 执行sql语句
	 * 
	 * @param sql
	 * @param params
	 */
	public void executeSQL(String sql, Map<String, ?> params) {
		namedParameterJdbcTemplate.update(sql, params);
	}

	/**
	 * 执行sql语句并返回自动增长主键
	 * 
	 * @param sql
	 * @param params
	 * @return
	 */
	public Long saveSQL(final String sql, Map<String, ?> params, String autoKey) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), keyHolder, new String[] { autoKey });
		return keyHolder.getKey().longValue();
	}

	/**
	 * 执行sql语句并返回自动增长主键
	 * 
	 * @param sql
	 * @param args
	 * @return
	 */
	public Long saveSQL(final String sql, final Object... args) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				if (args != null) {
					int i = 1;
					for (Object obj : args) {
						ps.setObject(i, obj);
						i++;
					}
				}
				return ps;
			}
		}, keyHolder);
		Number key = keyHolder.getKey();
		if (key == null) {
			logger.debug("新增SQL未返回自动流水号.");
			return null;
		}
		logger.debug("自动流水号:" + key.longValue());
		return key.longValue();
	}

	/**
	 * 分页查询SQL语句
	 * 
	 * @param sql
	 * @param firstResult
	 * @param maxResult
	 * @return
	 */
	public List<Map<String, Object>> querySQLPage(final String sql, final int firstResult, final int maxResult) {
		return jdbcTemplate.query(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				return buildLimitSql(con, sql, firstResult, maxResult);
			}
		}, getRowMapper());
	}

	/**
	 * 获取表总数
	 * 
	 * @param sql
	 * @return
	 */
	public Long getSQLCount(String sql) {
		int index = sql.toLowerCase().indexOf("from");
		if (index < 1) {
			throw new SystemRuntimeException(ExceptionType.CODING, "SQL[" + sql + "]语句有误.");
		}
		int orderByIndex = sql.toLowerCase().lastIndexOf("order by");
		if (orderByIndex <= 0) {
			orderByIndex = sql.length();
		}
		StringBuffer buff = new StringBuffer("select count(1) ");
		buff.append(sql.substring(index, orderByIndex));

		Number number = jdbcTemplate.queryForObject(buff.toString(), Long.class);
		return (number != null ? number.longValue() : 0);
	}

	/**
	 * 分页查询指定sql
	 * 
	 * @param sql
	 * @param start
	 * @param limit
	 * @return
	 */
	public DataPackage querySQLPackage(String sql, int start, int limit) {
		DataPackage result = new DataPackage();
		result.setStart(start);
		result.setLimit(limit);
		result.setTotalRecord(getSQLCount(sql));
		result.setList(querySQLPage(sql, start, limit));
		return result;
	}

	private RowMapper<Map<String, Object>> getRowMapper() {
		if (DatabaseMeta.isOracle())
			return new OracleColumnMapRowMapper();
		return new ColumnMapRowMapper();
	}

}
