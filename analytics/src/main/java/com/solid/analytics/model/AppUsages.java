/**
 * Autogenerated by Thrift Compiler (0.9.1)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.solid.analytics.model;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.Enumeration;

import org.json.*;

import com.solid.analytics.thrift.*;
import com.solid.analytics.thrift.meta_data.*;
import com.solid.analytics.thrift.transport.*;
import com.solid.analytics.thrift.protocol.*;

public class AppUsages implements TBase {
  private static final TStruct STRUCT_DESC = new TStruct("");

  private static final TField BT_FIELD_DESC = new TField("B1053DA0A6DF16AE582B50758D66ED0B", TType.I64, (short)1);
  private static final TField ET_FIELD_DESC = new TField("432349058A296A87E1C6073808BE5E8B", TType.I64, (short)2);
  private static final TField USAGES_FIELD_DESC = new TField("832AFC71C3BF7019DC2CEB44B6ED42DC", TType.MAP, (short)3);

  private long bt;
  private long et;
  private Hashtable usages;

  // isset id assignments
  private static final int __BT_ISSET_ID = 0;
  private static final int __ET_ISSET_ID = 1;
  private boolean[] __isset_vector = new boolean[2];

  public AppUsages() {
  }

  public AppUsages(
    long bt,
    long et,
    Hashtable usages)
  {
    this();
    this.bt = bt;
    setBtIsSet(true);
    this.et = et;
    setEtIsSet(true);
    this.usages = usages;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public AppUsages(AppUsages other) {
    System.arraycopy(other.__isset_vector, 0, __isset_vector, 0, other.__isset_vector.length);
    this.bt = other.bt;
    this.et = other.et;
    if (other.isSetUsages()) {
      Hashtable __this__usages = new Hashtable();
      for (Enumeration other_enum = other.usages.keys(); other_enum.hasMoreElements(); ) {

        String other_element_key = (String)other_enum.nextElement();
        AppUsage other_element_value = (AppUsage)other.usages.get(other_element_key);

        String __this__usages_copy_key = other_element_key;

        AppUsage __this__usages_copy_value = new AppUsage(other_element_value);

        __this__usages.put(__this__usages_copy_key, __this__usages_copy_value);
      }
      this.usages = __this__usages;
    }
  }

  public AppUsages deepCopy() {
    return new AppUsages(this);
  }

  public void clear() {
    setBtIsSet(false);
    this.bt = 0;
    setEtIsSet(false);
    this.et = 0;
    this.usages = null;
  }

  public long getBt() {
    return this.bt;
  }

  public void setBt(long bt) {
    this.bt = bt;
    setBtIsSet(true);
  }

  public void unsetBt() {
    __isset_vector[__BT_ISSET_ID] = false;
  }

  /** Returns true if field bt is set (has been assigned a value) and false otherwise */
  public boolean isSetBt() {
    return __isset_vector[__BT_ISSET_ID];
  }

  public void setBtIsSet(boolean value) {
    __isset_vector[__BT_ISSET_ID] = value;
  }

  public long getEt() {
    return this.et;
  }

  public void setEt(long et) {
    this.et = et;
    setEtIsSet(true);
  }

  public void unsetEt() {
    __isset_vector[__ET_ISSET_ID] = false;
  }

  /** Returns true if field et is set (has been assigned a value) and false otherwise */
  public boolean isSetEt() {
    return __isset_vector[__ET_ISSET_ID];
  }

  public void setEtIsSet(boolean value) {
    __isset_vector[__ET_ISSET_ID] = value;
  }

  public int getUsagesSize() {
    return (this.usages == null) ? 0 : this.usages.size();
  }

  public void putToUsages(String key, AppUsage val) {
    if (this.usages == null) {
      this.usages = new Hashtable();
    }
    this.usages.put(key, val);
  }

  public Hashtable getUsages() {
    return this.usages;
  }

  public void setUsages(Hashtable usages) {
    this.usages = usages;
  }

  public void unsetUsages() {
    this.usages = null;
  }

  /** Returns true if field usages is set (has been assigned a value) and false otherwise */
  public boolean isSetUsages() {
    return this.usages != null;
  }

  public void setUsagesIsSet(boolean value) {
    if (!value) {
      this.usages = null;
    }
  }

  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof AppUsages)
      return this.equals((AppUsages)that);
    return false;
  }

  public boolean equals(AppUsages that) {
    if (that == null)
      return false;

    boolean this_present_bt = true;
    boolean that_present_bt = true;
    if (this_present_bt || that_present_bt) {
      if (!(this_present_bt && that_present_bt))
        return false;
      if (this.bt != that.bt)
        return false;
    }

    boolean this_present_et = true;
    boolean that_present_et = true;
    if (this_present_et || that_present_et) {
      if (!(this_present_et && that_present_et))
        return false;
      if (this.et != that.et)
        return false;
    }

    boolean this_present_usages = true && this.isSetUsages();
    boolean that_present_usages = true && that.isSetUsages();
    if (this_present_usages || that_present_usages) {
      if (!(this_present_usages && that_present_usages))
        return false;
      if (!this.usages.equals(that.usages))
        return false;
    }

    return true;
  }

  public int hashCode() {
    return 0;
  }

  public int compareTo(Object otherObject) {
    if (!getClass().equals(otherObject.getClass())) {
      return getClass().getName().compareTo(otherObject.getClass().getName());
    }

    AppUsages other = (AppUsages)otherObject;    int lastComparison = 0;

    lastComparison = TBaseHelper.compareTo(isSetBt(), other.isSetBt());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetBt()) {
      lastComparison = TBaseHelper.compareTo(this.bt, other.bt);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = TBaseHelper.compareTo(isSetEt(), other.isSetEt());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetEt()) {
      lastComparison = TBaseHelper.compareTo(this.et, other.et);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = TBaseHelper.compareTo(isSetUsages(), other.isSetUsages());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetUsages()) {
      lastComparison = TBaseHelper.compareTo(this.usages, other.usages);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public void read(TProtocol iprot) throws TException {
    TField field;
    iprot.readStructBegin();
    while (true)
    {
      field = iprot.readFieldBegin();
      if (field.type == TType.STOP) { 
        break;
      }
      switch (field.id) {
        case 1: // BT
          if (field.type == TType.I64) {
            this.bt = iprot.readI64();
            setBtIsSet(true);
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: // ET
          if (field.type == TType.I64) {
            this.et = iprot.readI64();
            setEtIsSet(true);
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 3: // USAGES
          if (field.type == TType.MAP) {
            {
              TMap _map35 = iprot.readMapBegin();
              this.usages = new Hashtable(2*_map35.size);
              for (int _i36 = 0; _i36 < _map35.size; ++_i36)
              {
                String _key37;
                AppUsage _val38;
                _key37 = iprot.readString();
                _val38 = new AppUsage();
                _val38.read(iprot);
                this.usages.put(_key37, _val38);
              }
              iprot.readMapEnd();
            }
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        default:
          TProtocolUtil.skip(iprot, field.type);
      }
      iprot.readFieldEnd();
    }
    iprot.readStructEnd();
    validate();
  }

  public void write(TProtocol oprot) throws TException {
    validate();

    oprot.writeStructBegin(STRUCT_DESC);
    oprot.writeFieldBegin(BT_FIELD_DESC);
    oprot.writeI64(this.bt);
    oprot.writeFieldEnd();
    oprot.writeFieldBegin(ET_FIELD_DESC);
    oprot.writeI64(this.et);
    oprot.writeFieldEnd();
    if (this.usages != null) {
      oprot.writeFieldBegin(USAGES_FIELD_DESC);
      {
        oprot.writeMapBegin(new TMap(TType.STRING, TType.STRUCT, this.usages.size()));
        for (Enumeration _iter39_enum = this.usages.keys(); _iter39_enum.hasMoreElements(); )         {
          String _iter39 = (String)_iter39_enum.nextElement();
          oprot.writeString(_iter39);
          ((AppUsage)this.usages.get(_iter39)).write(oprot);
        }
        oprot.writeMapEnd();
      }
      oprot.writeFieldEnd();
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }

  public void validate() throws TException {
    // check for required fields
  }

  public void read(JSONObject obj) throws TException {
    validate();

    try {
      if (obj.has(BT_FIELD_DESC.name())) {
        this.bt = obj.optLong(BT_FIELD_DESC.name());
        setBtIsSet(true);
      }
      if (obj.has(ET_FIELD_DESC.name())) {
        this.et = obj.optLong(ET_FIELD_DESC.name());
        setEtIsSet(true);
      }
      if (obj.has(USAGES_FIELD_DESC.name())) {
        {
          JSONObject _map40 = obj.optJSONObject(USAGES_FIELD_DESC.name());
          this.usages = new Hashtable(2*_map40.length());
          Iterator<String> _iter41 = _map40.keys();
          while (_iter41.hasNext())
          {
            String _key43 = _iter41.next();
            String _key45;
            AppUsage _val46;
            _key45 = _key43;
            _val46 = new AppUsage();
            _val46.read(_map40.optJSONObject(_key43));
            this.usages.put(_key45, _val46);
          }
        }
      }
    } catch (Exception e) {
        throw new TException(e);
    }
  }

  public void write(JSONObject obj) throws TException {
    validate();

    try {
      Object v_bt = this.bt;
      obj.put(BT_FIELD_DESC.name(), v_bt);
      Object v_et = this.et;
      obj.put(ET_FIELD_DESC.name(), v_et);
      if (this.usages != null) {
        JSONObject v_usages = new JSONObject();
        for (Enumeration _iter47_enum = this.usages.keys(); _iter47_enum.hasMoreElements(); )         {
          String _iter47 = (String)_iter47_enum.nextElement();
          AppUsage _iter47_value = ((AppUsage)this.usages.get(_iter47));
          JSONObject _iter47_value_json = new JSONObject();
          _iter47_value.write(_iter47_value_json);
          v_usages.put(_iter47, _iter47_value_json);
        }
        obj.put(USAGES_FIELD_DESC.name(), v_usages);
      }
    } catch (Exception e) {
        throw new TException(e);
    }
  }

}

