package com.fleety.util.pool.db;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import com.fleety.util.pool.db.DbConnPool.DbHandle;

public class NestConnection implements Connection {
	private DbConnPool pool = null;
	private Connection conn = null;
	private DbHandle handle = null;

	public NestConnection(DbConnPool pool, Connection _conn, DbHandle handle) {
		this.pool = pool;
		this.conn = _conn;
		this.handle = handle;
	}

	public void clearWarnings() throws SQLException {
		this.conn.clearWarnings();
	}

	public void close() throws SQLException {
		this.pool.releaseConn(this.handle);
	}

	public void commit() throws SQLException {
		this.conn.commit();
	}

	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		return this.conn.createArrayOf(typeName, elements);
	}

	public Blob createBlob() throws SQLException {
		return this.conn.createBlob();
	}

	public Clob createClob() throws SQLException {
		return this.conn.createClob();
	}

	public NClob createNClob() throws SQLException {
		return this.conn.createNClob();
	}

	public SQLXML createSQLXML() throws SQLException {
		return this.conn.createSQLXML();
	}

	public Statement createStatement() throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.createStatement();
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.createStatement(resultSetType, resultSetConcurrency);
	}

	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.createStatement(resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}

	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.createStruct(typeName, attributes);
	}

	public boolean getAutoCommit() throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.getAutoCommit();
	}

	public String getCatalog() throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.getCatalog();
	}

	public Properties getClientInfo() throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.getClientInfo();
	}

	public String getClientInfo(String name) throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.getClientInfo(name);
	}

	public int getHoldability() throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.getHoldability();
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.getMetaData();
	}

	public int getTransactionIsolation() throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.getTransactionIsolation();
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.getTypeMap();
	}

	public SQLWarning getWarnings() throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.getWarnings();
	}

	public boolean isClosed() throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.isClosed();
	}

	public boolean isReadOnly() throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.isReadOnly();
	}

	public boolean isValid(int timeout) throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.isValid(timeout);
	}

	public String nativeSQL(String sql) throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.nativeSQL(sql);
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.prepareCall(sql);
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.prepareCall(sql, resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.prepareStatement(sql);
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		// TODO Auto-generated method stub
		return this.conn.prepareStatement(sql, autoGeneratedKeys);
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {
		return this.conn.prepareStatement(sql, columnIndexes);
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {
		return this.conn.prepareStatement(sql, columnNames);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return this.conn.prepareStatement(sql, resultSetType,
				resultSetConcurrency);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {

		return this.conn.prepareStatement(sql, resultSetType,
				resultSetConcurrency, resultSetHoldability);
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		this.conn.releaseSavepoint(savepoint);
	}

	public void rollback() throws SQLException {
		this.conn.rollback();
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		this.conn.rollback(savepoint);
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		this.conn.setAutoCommit(autoCommit);
	}

	public void setCatalog(String catalog) throws SQLException {
		this.conn.setCatalog(catalog);
	}

	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		this.conn.setClientInfo(properties);
	}

	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		this.conn.setClientInfo(name, value);
	}

	public void setHoldability(int holdability) throws SQLException {
		this.conn.setHoldability(holdability);
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		this.conn.setReadOnly(readOnly);
	}

	public Savepoint setSavepoint() throws SQLException {
		return this.conn.setSavepoint();
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		return this.conn.setSavepoint(name);
	}

	public void setTransactionIsolation(int level) throws SQLException {
		this.conn.setTransactionIsolation(level);
	}

	public void setTypeMap(Map<String, Class<?>> arg0) throws SQLException {
		this.conn.setTypeMap(arg0);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return this.conn.isWrapperFor(iface);
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return this.conn.unwrap(iface);
	}

	public void abort(Executor executor) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public int getNetworkTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getSchema() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setNetworkTimeout(Executor executor, int milliseconds)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setSchema(String schema) throws SQLException {
		// TODO Auto-generated method stub
		
	}
}
