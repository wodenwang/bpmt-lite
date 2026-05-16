/*
 * File Name  :CommonService.java
 * Create Date:2012-11-3 下午5:54:12
 * Author     :woden
 */

package com.riversoft.core.db;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.sql.Blob;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Cache;
import org.hibernate.Hibernate;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.riversoft.core.BeanFactory;
import com.riversoft.core.exception.ExceptionType;
import com.riversoft.core.exception.SystemRuntimeException;

/**
 * 通用数据库服务.<br>
 * 封装通用hibernate操作。
 *
 */
public class ORMService {

	private static final Logger logger = LoggerFactory.getLogger(ORMService.class);

	/**
	 * 用以自定义查询入参
	 * 
	 * @author Woden
	 * 
	 */
	public static class QueryVO {
		public QueryVO(String name, Object value) {
			this.name = name;
			this.value = value;
		}

		private String name;
		private Object value;

		public boolean isArray() {
			return value instanceof Collection;
		}
	}

	/**
	 * 获取通用数据库服务.<br>
	 * 单例模式.
	 * 
	 * @return 通用数据库服务对象 <code>CommonService</code>.
	 */
	public static ORMService getInstance() {
		ORMService service = BeanFactory.getInstance().getSingleBean(ORMService.class);
		return service;
	}

	/**
	 * <code>SessionFactory</code>对象.
	 */
	protected SessionFactory sessionFactory;

	/**
	 * 向PO设置默认值
	 * 
	 * @param po
	 * @param isNew
	 *            是否初始化
	 */
	protected void setDefalutValue(Object po, boolean isNew) {
		try {
			if (isNew) {
				// 设置创建时间
				PropertyUtils.setProperty(po, "createDate", new Date());
			}
			// 设置更新时间
			PropertyUtils.setProperty(po, "updateDate", new Date());
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			logger.warn("PO设默认值出错。");
		}
	}

	/**
	 * 执行自定义HQL操作.
	 * 
	 * @param hql
	 * @param values
	 * @throws SystemRuntimeException
	 */
	@SuppressWarnings("rawtypes")
	public void executeHQL(String hql, Object... values) {
		executeHQLUpdate(hql, values);
	}

	/**
	 * 执行自定义HQL更新并返回影响行数.
	 *
	 * @param hql
	 * @param values
	 * @return 影响行数
	 * @throws SystemRuntimeException
	 */
	@SuppressWarnings("rawtypes")
	public int executeHQLUpdate(String hql, Object... values) {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			Query query = session.createQuery(hql);
			query.setCacheable(true);
			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					if (values[i] instanceof QueryVO) {
						QueryVO vo = (QueryVO) values[i];
						if (vo.isArray()) {
							query.setParameterList(vo.name, (Collection) vo.value);
						} else {
							query.setParameter(vo.name, vo.value);
						}

					} else {
						query.setParameter(i, values[i]);
					}
				}
			}
			int rows = query.executeUpdate();
			session.flush();
			return rows;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "执行[" + hql + "]出错.", e);
		}
	}

	/**
	 * 根据查询条件查找唯一记录.
	 * 
	 * @param entityName
	 * @param queryMap
	 * @return
	 * @throws SystemRuntimeException
	 */
	@SuppressWarnings("rawtypes")
	public Object find(String entityName, Map queryMap) {
		String hql = "from " + entityName + " " + QueryStringBuilder.build(queryMap);
		Session session = this.sessionFactory.getCurrentSession();
		try {
			Query query = session.createQuery(hql);
			query.setCacheable(true);
			return query.uniqueResult();
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "查询[" + hql + "]出错.", e);
		}
	}

	/**
	 * 自定义查找.
	 * 
	 * @param entityName
	 *            对象名.
	 * @param keyName
	 *            字段Key.
	 * @param keyValue
	 *            字段值.
	 * @return 满足条件的唯一对象.
	 */
	@SuppressWarnings({ "rawtypes" })
	public Object findByKey(String entityName, String keyName, Object keyValue) {
		Session session = this.sessionFactory.getCurrentSession();
		Query query = session.createQuery("from " + entityName + " where " + keyName + " = ?");
		query.setCacheable(true);
		query.setParameter(0, keyValue);
		List result = query.list();
		if (result != null && result.size() > 0) {
			return result.get(0);
		} else {
			return null;
		}
	}

	/**
	 * 查找.
	 * 
	 * @param entityName
	 *            对象名.
	 * @param pk
	 *            主键.
	 * @return 返回实例.
	 */
	public Object findByPk(String entityName, Serializable pk) {
		Session session = this.sessionFactory.getCurrentSession();
		Object entity = session.get(entityName, pk);
		if (entity == null) {
			return null;
		} else {
			return entity;
		}
	}

	/**
	 * 使用load方法获取唯一值
	 * 
	 * @param entityName
	 * @param pk
	 * @return
	 */
	public Object loadByPk(String entityName, Serializable pk) {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			Object entity = session.load(entityName, pk);
			if (entity == null) {
				return null;
			} else {
				return entity;
			}
		} catch (ObjectNotFoundException e) {
			logger.warn("找不到记录[" + entityName + "][" + pk + "]", e);
			return null;
		}
	}

	/**
	 * 自定义HQL查询.
	 * 
	 * @param hql
	 *            HQL语句.
	 * @param values
	 *            入参数组.
	 * @return 结果对象.
	 * @throws SystemRuntimeException
	 *             数据操作失败时抛出.
	 */
	@SuppressWarnings("rawtypes")
	public Object findHQL(String hql, Object... values) {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			Query query = session.createQuery(hql);
			query.setCacheable(true);
			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					if (values[i] instanceof QueryVO) {
						QueryVO vo = (QueryVO) values[i];
						if (vo.isArray()) {
							query.setParameterList(vo.name, (Collection) vo.value);
						} else {
							query.setParameter(vo.name, vo.value);
						}

					} else {
						query.setParameter(i, values[i]);
					}
				}
			}
			return query.uniqueResult();
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "查询[" + hql + "]出错.", e);
		}
	}

	/**
	 * 自定义SQL查询.
	 * 
	 * @param sql
	 *            sql语句.
	 * @param values
	 *            替换入参.
	 * @return 结果对象.
	 * @throws SystemRuntimeException
	 */
	@SuppressWarnings("rawtypes")
	public Object findSQL(String sql, Object... values) {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			SQLQuery query = session.createSQLQuery(sql);
			query.setCacheable(true);
			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					if (values[i] instanceof QueryVO) {
						QueryVO vo = (QueryVO) values[i];
						if (vo.isArray()) {
							query.setParameterList(vo.name, (Collection) vo.value);
						} else {
							query.setParameter(vo.name, vo.value);
						}

					} else {
						query.setParameter(i, values[i]);
					}
				}
			}
			return query.uniqueResult();
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "查询[" + sql + "]出错.", e);
		}
	}

	/**
	 * 获取表记录总数.
	 * 
	 * @param entityName
	 *            对象名.
	 * @return 当前表记录总数.
	 * @throws SystemRuntimeException
	 */
	public Long getCount(String entityName) {
		Session session = this.sessionFactory.getCurrentSession();
		Long count = null;
		try {
			Query query = session.createQuery("select count(1) from " + entityName);
			query.setCacheable(true);
			count = (Long) query.list().iterator().next();
			return count;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "查询[" + entityName + "]总数出错.", e);
		}

	}

	/**
	 * 获取表记录总数.
	 * 
	 * @param entityName
	 *            对象名.
	 * @param queryMap
	 *            动态查询条件.
	 * @return 符合条件的记录总数.
	 * @throws SystemRuntimeException
	 */
	@SuppressWarnings("rawtypes")
	public Long getCount(String entityName, Map queryMap) {
		Session session = this.sessionFactory.getCurrentSession();
		Long count = null;
		try {
			String hql = "select count(1) from " + entityName + " " + QueryStringBuilder.buildWhere(queryMap);
			Query query = session.createQuery(hql);
			query.setCacheable(true);
			count = (Long) query.list().iterator().next();
			return count;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "查询[" + entityName + "]总数出错.", e);
		}

	}

	/**
	 * 从PO对象中获取对象名.
	 * 
	 * @param po
	 *            动态对象.
	 * @return 对象名.
	 */
	@SuppressWarnings({ "rawtypes" })
	protected String getEntityName(Map po) {
		if (po != null && po.containsKey("$type$")) {
			return (String) po.get("$type$");
		}

		throw new SystemRuntimeException(ExceptionType.DB, "对象无指定实体.");
	}

	/**
	 * 自定义HQL查询.
	 * 
	 * @param queryMap
	 *            动态查询入参,参考:{@link QueryStringBuilder}.
	 * @return 结果数组.
	 * @throws SystemRuntimeException
	 *             查询出错时抛出.
	 */
	@SuppressWarnings("rawtypes")
	public List query(String entityName, Map queryMap) {
		String hql = "from " + entityName + " " + QueryStringBuilder.build(queryMap);
		return this.queryHQL(hql);
	}

	/**
	 * 查找所有
	 * 
	 * @param entityName
	 *            对象名.
	 * @return 单表所有记录.
	 */
	@SuppressWarnings({ "rawtypes" })
	public List queryAll(String entityName) {
		Session session = this.sessionFactory.getCurrentSession();
		Query query = session.createQuery("from " + entityName);
		query.setCacheable(true);
		return query.list();
	}

	/**
	 * 自定义HQL查询.
	 * 
	 * @param hql
	 *            HQL语句.
	 * @param values
	 *            入参数组.
	 * @return 结果列表.
	 * @throws SystemRuntimeException
	 *             数据操作失败时抛出.
	 */
	@SuppressWarnings({ "rawtypes" })
	public List queryHQL(String hql, Object... values) {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			Query query = session.createQuery(hql);
			query.setCacheable(true);
			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					if (values[i] instanceof QueryVO) {
						QueryVO vo = (QueryVO) values[i];
						if (vo.isArray()) {
							query.setParameterList(vo.name, (Collection) vo.value);
						} else {
							query.setParameter(vo.name, vo.value);
						}

					} else {
						query.setParameter(i, values[i]);
					}
				}
			}
			return query.list();
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "查询[" + hql + "]出错.", e);
		}
	}

	/**
	 * 自定义HQL分页查询.
	 * 
	 * @param hql
	 *            HQL语句.
	 * @param firstResult
	 *            记录开端.
	 * @param maxResult
	 *            最大记录数.
	 * @param values
	 *            入参数组.
	 * @return 结果列表.
	 * @throws SystemRuntimeException
	 *             数据操作失败时抛出.
	 */
	@SuppressWarnings({ "rawtypes" })
	public List queryHQLPage(String hql, int firstResult, int maxResult, Object... values) {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			Query query = session.createQuery(hql);
			query.setCacheable(true);
			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					if (values[i] instanceof QueryVO) {
						QueryVO vo = (QueryVO) values[i];
						if (vo.isArray()) {
							query.setParameterList(vo.name, (Collection) vo.value);
						} else {
							query.setParameter(vo.name, vo.value);
						}

					} else {
						query.setParameter(i, values[i]);
					}
				}
			}
			return query.setFirstResult(firstResult).setMaxResults(maxResult).list();
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "执行[" + hql + "]出错.", e);
		}
	}

	/**
	 * 
	 * @param entityName
	 * @param firstResult
	 *            记录开端.
	 * @param maxResult
	 *            最大记录数.
	 * @param queryMap
	 *            动态查询入参,参考:{@link QueryStringBuilder}.
	 * @return 结果数组.
	 * @throws SystemRuntimeException
	 *             查询出错时抛出.
	 */
	@SuppressWarnings({ "rawtypes" })
	public List queryPage(String entityName, int firstResult, int maxResult, Map queryMap) {
		String hql = "from " + entityName + " " + QueryStringBuilder.build(queryMap);
		return this.queryHQLPage(hql, firstResult, maxResult);
	}

	/**
	 * 自定义SQL查询.
	 * 
	 * @param sql
	 *            sql语句.
	 * @param values
	 *            替换入参.
	 * @return 查询返回列表.
	 * @throws SystemRuntimeException
	 */
	@SuppressWarnings("rawtypes")
	public List querySQL(String sql, Object... values) {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			SQLQuery query = session.createSQLQuery(sql);
			query.setCacheable(true);
			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					if (values[i] instanceof QueryVO) {
						QueryVO vo = (QueryVO) values[i];
						if (vo.isArray()) {
							query.setParameterList(vo.name, (Collection) vo.value);
						} else {
							query.setParameter(vo.name, vo.value);
						}

					} else {
						query.setParameter(i, values[i]);
					}
				}
			}
			return query.list();
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "查询[" + sql + "]出错.", e);
		}
	}

	/**
	 * 删除.
	 * 
	 * @param po
	 *            动态PO实例
	 * @throws SystemRuntimeException
	 *             数据操作失败时抛出.
	 */
	public void remove(Map<String, Object> po) {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			session.delete(this.getEntityName(po), po);
			session.flush();
		} catch (SystemRuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "删除出错.", e);
		}
	}

	/**
	 * 删除。
	 * 
	 * @param po
	 *            静态实例
	 */
	public void removePO(Object po) {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			session.delete(po);
			session.flush();
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "删除出错.", e);
		}
	}

	/**
	 * 通过主键删除。
	 * 
	 * @param entityName
	 * @param pk
	 */
	public void removeByPk(String entityName, Serializable pk) {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			Object object = findByPk(entityName, pk);
			if (object != null) {
				session.delete(entityName, object);
				session.flush();
			}
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "删除出错.", e);
		}
	}

	/**
	 * 批量删除。
	 * 
	 * @param list
	 */
	public void removeBath(Collection<Map<String, Object>> list) {
		if (list == null || list.size() == 0) {
			return;
		}
		try {
			for (Map<String, Object> po : list) {
				remove(po);
			}
		} catch (SystemRuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "批量删除出错.", e);
		}
	}

	/**
	 * 批量删除。
	 * 
	 * @param list
	 */
	public void removeBathPO(Collection<Object> list) {
		if (list == null || list.size() == 0) {
			return;
		}
		try {
			for (Object po : list) {
				removePO(po);
			}
		} catch (SystemRuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "批量删除出错.", e);
		}
	}

	/**
	 * 批量删除。
	 * 
	 * @param entityName
	 * @param list
	 */
	public void removeByPkBath(String entityName, Collection<Serializable> list) {
		if (list == null || list.size() == 0) {
			return;
		}
		try {
			for (Serializable pk : list) {
				removeByPk(entityName, pk);
			}
		} catch (SystemRuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "批量删除出错.", e);
		}
	}

	/**
	 * 新增.
	 * 
	 * @param po
	 *            动态PO实例
	 * @throws SystemRuntimeException
	 *             数据操作失败时抛出.
	 */
	public void save(Map<String, Object> po) {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			// 设置默认值
			setDefalutValue(po, true);
			session.save(this.getEntityName(po), po);
			session.flush();
		} catch (SystemRuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "新增出错.", e);
		}
	}

	/**
	 * 新增。
	 * 
	 * @param po
	 *            静态实例
	 */
	public void savePO(Object po) {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			// 设置默认值
			setDefalutValue(po, true);
			session.save(po);
			session.flush();
		} catch (SystemRuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "新增出错.", e);
		}
	}

	/**
	 * 批量新增.
	 * 
	 * @param list
	 *            动态PO实例列表.
	 * @throws SystemRuntimeException
	 *             数据操作失败时抛出.
	 */
	public void saveBatch(Collection<Map<String, Object>> list) {
		if (list == null || list.size() == 0) {
			return;
		}
		try {
			for (Map<String, Object> po : list) {
				save(po);
			}
		} catch (SystemRuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "新增出错.", e);
		}
	}

	/**
	 * 批量新增。
	 * 
	 * @param list
	 *            静态实例列表。
	 */
	public void saveBatchPO(Collection<Object> list) {
		if (list == null || list.size() == 0) {
			return;
		}
		try {
			for (Object po : list) {
				savePO(po);
			}
		} catch (SystemRuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "新增出错.", e);
		}
	}

	/**
	 * 批量新增或更新
	 * 
	 * @param list
	 */
	public void saveOrUpdateBatchPO(Collection<Object> list) {
		if (list == null || list.size() == 0) {
			return;
		}
		try {
			for (Object po : list) {
				saveOrUpdatePO(po);
			}
		} catch (SystemRuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "新增或更新出错.", e);
		}
	}

	/**
	 * 批量新增或更新
	 * 
	 * @param list
	 */
	public void saveOrUpdateBatch(Collection<Map<String, Object>> list) {
		if (list == null || list.size() == 0) {
			return;
		}
		try {
			for (Map<String, Object> po : list) {
				saveOrUpdate(po);
			}
		} catch (SystemRuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "新增或更新出错.", e);
		}
	}

	/**
	 * 新增或更新.
	 * 
	 * @param po
	 *            动态PO实例
	 * @throws SystemRuntimeException
	 *             数据操作失败时抛出.
	 */
	public void saveOrUpdate(Map<String, Object> po) {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			// 设置默认值
			setDefalutValue(po, true);
			session.saveOrUpdate(this.getEntityName(po), po);
			session.flush();
		} catch (SystemRuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "新增或更新出错.", e);
		}
	}

	/**
	 * 新增或更新.
	 * 
	 * @param po
	 *            静态实例
	 */
	public void saveOrUpdatePO(Object po) {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			// FIXME 这里要考虑createDate的问题
			// 设置默认值
			setDefalutValue(po, true);
			session.saveOrUpdate(po);
			session.flush();
		} catch (SystemRuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "新增或更新出错.", e);
		}
	}

	/**
	 * Spring容器使用.
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * 合并.
	 * 
	 * @param po
	 *            动态PO实例
	 * @throws SystemRuntimeException
	 *             数据操作失败时抛出.
	 */
	public void merge(Map<String, Object> po) {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			// 设置默认值
			setDefalutValue(po, false);
			session.merge(this.getEntityName(po), po);
			session.flush();
		} catch (SystemRuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "更新出错.", e);
		}
	}

	/**
	 * 合并.
	 * 
	 * @param po
	 *            静态实例。
	 */
	public void mergePO(Object po) {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			// 设置默认值
			setDefalutValue(po, false);
			session.merge(po);
			session.flush();
		} catch (SystemRuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "更新出错.", e);
		}
	}

	/**
	 * 批量合并.
	 * 
	 * @param list
	 */
	public void mergeBathPO(Collection<Object> list) {
		if (list == null || list.size() == 0) {
			return;
		}
		try {
			for (Object po : list) {
				mergePO(po);
			}
		} catch (SystemRuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "批量更新出错.", e);
		}
	}

	/**
	 * 批量合并.
	 * 
	 * @param list
	 */
	public void mergeBath(Collection<Map<String, Object>> list) {
		if (list == null || list.size() == 0) {
			return;
		}
		try {
			for (Map<String, Object> po : list) {
				merge(po);
			}
		} catch (SystemRuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "批量更新出错.", e);
		}
	}

	/**
	 * 更新.
	 * 
	 * @param po
	 *            动态PO实例
	 * @throws SystemRuntimeException
	 *             数据操作失败时抛出.
	 */
	public void update(Map<String, Object> po) {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			// 设置默认值
			setDefalutValue(po, false);
			session.update(this.getEntityName(po), po);
			session.flush();
		} catch (SystemRuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "更新出错.", e);
		}
	}

	/**
	 * 更新。
	 * 
	 * @param po
	 *            静态实例。
	 */
	public void updatePO(Object po) {
		Session session = this.sessionFactory.getCurrentSession();
		try {
			// 设置默认值
			setDefalutValue(po, false);
			session.update(po);
			session.flush();
		} catch (SystemRuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "更新出错.", e);
		}
	}

	/**
	 * 批量更新
	 * 
	 * @param list
	 */
	public void updateBathPO(Collection<Object> list) {
		if (list == null || list.size() == 0) {
			return;
		}
		try {
			for (Object po : list) {
				updatePO(po);
			}
		} catch (SystemRuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "批量更新出错.", e);
		}
	}

	/**
	 * 批量更新
	 * 
	 * @param list
	 */
	public void updateBath(Collection<Map<String, Object>> list) {
		if (list == null || list.size() == 0) {
			return;
		}
		try {
			for (Map<String, Object> po : list) {
				update(po);
			}
		} catch (SystemRuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new SystemRuntimeException(ExceptionType.DB, "批量更新出错.", e);
		}
	}

	/**
	 * 分页查询.
	 * 
	 * @param entityName
	 *            对象名.
	 * @param start
	 *            起点.
	 * @param limit
	 *            限制.
	 * @return 对象集.
	 * @throws SystemRuntimeException
	 *             数据操作失败时抛出.
	 */
	public DataPackage queryPackage(String entityName, int start, int limit) {
		DataPackage result = new DataPackage();
		result.setStart(start);
		result.setLimit(limit);
		result.setTotalRecord(getCount(entityName));
		result.setList(queryHQLPage("from " + entityName, result.getStart(), result.getLimit()));
		return result;
	}

	/**
	 * 分页查询.
	 * 
	 * @param entityName
	 *            对象名.
	 * @param start
	 *            起点.
	 * @param limit
	 *            限制.
	 * @param queryMap
	 *            动态查询入参,参考:{@link QueryStringBuilder}.
	 * @return 对象集.
	 * @throws SystemRuntimeException
	 *             数据操作失败时抛出.
	 */
	@SuppressWarnings("rawtypes")
	public DataPackage queryPackage(String entityName, int start, int limit, Map queryMap) {
		DataPackage result = new DataPackage();
		result.setStart(start);
		result.setLimit(limit);
		result.setTotalRecord(getCount(entityName, queryMap));
		result.setList(queryPage(entityName, result.getStart(), result.getLimit(), queryMap));
		return result;
	}

	/**
	 * 创建hibernate大文件
	 * 
	 * @param bytes
	 * @return
	 */
	public Blob getBlob(byte[] bytes) {
		Session session = this.sessionFactory.getCurrentSession();
		return Hibernate.getLobCreator(session).createBlob(bytes);
	}

	/**
	 * 刷新缓存
	 */
	public void flush() {
		sessionFactory.getCurrentSession().flush();
	}

	/**
	 * 清除缓存
	 */
	public void clear() {
		sessionFactory.getCurrentSession().clear();
	}

	public void evictEntityRegions(String... entityNames) {
		Cache cache = sessionFactory.getCache();
		if (cache == null || entityNames == null) {
			return;
		}
		for (String entityName : entityNames) {
			if (StringUtils.isNotBlank(entityName)) {
				cache.evictEntityRegion(entityName);
			}
		}
	}

	public void evictQueryRegions() {
		Cache cache = sessionFactory.getCache();
		if (cache != null) {
			cache.evictQueryRegions();
		}
	}
}
