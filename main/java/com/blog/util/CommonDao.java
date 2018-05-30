package com.blog.util;

import com.blog.mapper.CommonMapper;
import com.github.pagehelper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by paul on 2018/5/10.
 */
//如果使用工具类中的方法报nullpointexception，说明方法类中的dao接口层未注入到spring，需要构造init方法初始化dao接口内部调用
@Component
public class CommonDao {
    @Autowired
    private CommonMapper icommonMapper;

    public static CommonDao commonDao;

    @PostConstruct
    public void init() {
        commonDao = this;
    }

    //select
    public List findList(String sql, Class entityClass) throws Exception{
        List<LinkedHashMap<String, Object>> list= commonDao.icommonMapper.findList(sql);
        List returnlist = new ArrayList();
        for(LinkedHashMap<String, Object> map:list){
            Object obj = entityClass.newInstance();
            Field[] fs = entityClass.getDeclaredFields();
            for(Field f :fs){
                f.setAccessible(true);
                f.set(obj,map.get(f.getName()));
            }
            returnlist.add(obj);
        }
        return returnlist;
    }
    public Object FindEntity(String sql, Class entityClass) throws Exception{
        List returnlist = findList(sql,entityClass);
        if (returnlist.size()>0){
            return returnlist.get(0);
        }else{
            return null;
        }
    }

    //insert
    public void insert(Object entity) throws Exception{
        List<Field> fs = Arrays.asList(entity.getClass().getDeclaredFields());
        StringBuilder sqlbuilder = new StringBuilder();
        StringBuilder fieldbuilder = new StringBuilder();
        StringBuilder valuebuilder = new StringBuilder();
        //去除反射的类的包名
        String tablename = entity.getClass().getName().replace(entity.getClass().getPackage().getName()+".","");
        sqlbuilder.append(MessageFormat.format("insert into {0}",tablename));
        fieldbuilder.append("(");
        valuebuilder.append("(");
        for(Field f :fs){

            if (fs.indexOf(f)==fs.size()-1){
                fieldbuilder.append(f.getName().toString());
                valuebuilder.append(MessageFormat.format("{0}{1}{2}","'",f.get(entity).toString(),"'"));
            }else{
                fieldbuilder.append(f.getName().toString()+",");
                valuebuilder.append(MessageFormat.format("{0}{1}{2},","'",f.get(entity).toString(),"'"));
            }

        }
        fieldbuilder.append(")");
        valuebuilder.append(")");
        sqlbuilder.append(" "+fieldbuilder.toString());
        sqlbuilder.append(" values "+valuebuilder.toString());
        //System.out.println(sqlbuilder.toString());
        commonDao.icommonMapper.executeSql(sqlbuilder.toString());
    }

    //update
    public void update(Object entity,String Key) throws Exception{
        List<Field> fs = Arrays.asList(entity.getClass().getDeclaredFields());
        StringBuilder sqlbuilder = new StringBuilder();
        StringBuilder fieldbuilder = new StringBuilder();
        //去除反射的类的包名
        String tablename = entity.getClass().getName().replace(entity.getClass().getPackage().getName()+".","");
        sqlbuilder.append(MessageFormat.format("update {0} set ",tablename));
        for(Field f :fs){
            if (fs.indexOf(f)==fs.size()-1){
                fieldbuilder.append(MessageFormat.format("{0}={1}{2}{3}",f.getName(),"'",f.get(entity).toString(),"'"));
            }else{
                fieldbuilder.append(MessageFormat.format("{0}={1}{2}{3},",f.getName(),"'",f.get(entity).toString(),"'"));
            }
        }
        for(Field f :fs){
            if (f.getName().equals(Key)){
                fieldbuilder.append(MessageFormat.format(" where {0}={1}",Key,"'"+f.get(entity).toString()+"'"));
            }
        }
        sqlbuilder.append(" "+fieldbuilder.toString());
        //System.out.println(sqlbuilder.toString());
        commonDao.icommonMapper.executeSql(sqlbuilder.toString());
    }
}
