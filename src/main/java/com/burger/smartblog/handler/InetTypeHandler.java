package com.burger.smartblog.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedJdbcTypes(JdbcType.OTHER)
@MappedTypes(String.class)
public class InetTypeHandler extends BaseTypeHandler<String> {
  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
    PGobject pg = new PGobject();
    pg.setType("inet");
    pg.setValue(parameter);
    ps.setObject(i, pg);
  }
  @Override public String getNullableResult(ResultSet rs, String column) throws SQLException {
    Object o = rs.getObject(column); return o == null ? null : o.toString();
  }
  @Override public String getNullableResult(ResultSet rs, int idx) throws SQLException {
    Object o = rs.getObject(idx); return o == null ? null : o.toString();
  }
  @Override public String getNullableResult(CallableStatement cs, int idx) throws SQLException {
    Object o = cs.getObject(idx); return o == null ? null : o.toString();
  }
}
