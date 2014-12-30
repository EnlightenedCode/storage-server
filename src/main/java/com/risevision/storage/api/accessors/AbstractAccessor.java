package com.risevision.storage.api.accessors;

import java.util.ArrayList;
import java.util.List;

import com.risevision.storage.Utils;

public abstract class AbstractAccessor {
  public static class Condition {
    private String field;
    private String value;
    
    public Condition(String field, String value) {
      this.field = field;
      this.value = value;
    }
    
    public String getField() {
      return field;
    }

    public String getValue() {
      return value;
    }
  }
  
  public Object[] mergeFilters(List<Condition> conditions, Object... filters) {
    List<Object> values = new ArrayList<Object>();
    
    for(Condition condition : conditions) {
      values.add(condition.getField());
      values.add(condition.getValue());
    }
    
    for(int i = 0; i < filters.length; i++) {
      values.add(filters[i]);
    }
    
    return values.toArray();
  }
  
  /**
   * Currently queries are only of this kind: "field: value"
   * 
   * @param query The query to parse
   * @return The list of conditions
   */
  public List<Condition> parseQuery(String query) {
    List<Condition> conditions = new ArrayList<Condition>();
    
    if(!Utils.isEmpty(query) && query.indexOf(":") >= 0) {
      String values[] = query.split(":");
      
      conditions.add(new Condition(values[0].trim(), values[1].trim()));
    }
    
    return conditions;
  }
}
