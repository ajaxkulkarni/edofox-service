package com.rns.web.edo.service.util;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtilsBean;

public class NullAwareBeanUtils extends BeanUtilsBean{

    @Override
    public void copyProperty(Object dest, String name, Object value) throws IllegalAccessException, InvocationTargetException {
        if(value==null) {
        	return;
        }
        super.copyProperty(dest, name, value);
    }

}