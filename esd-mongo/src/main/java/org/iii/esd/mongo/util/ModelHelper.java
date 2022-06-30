package org.iii.esd.mongo.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import org.iii.esd.exception.ApplicationException;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.integrate.CustomizedDocument;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.UserProfile;
import org.iii.esd.utils.DatetimeUtils;

public final class ModelHelper {
    private ModelHelper() {}

    private static String getIdentity(CustomizedDocument<?> document) {
        try {
            return Objects.requireNonNull(PropertyUtils.getProperty(document, document.getIdentityProperty())).toString();
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ApplicationException(Error.invalidIdentity, document.getIdentityProperty());
        }
    }

    public static void checkIdentity(CustomizedDocument<?> document) {
        String identity = getIdentity(Objects.requireNonNull(document));
        if (StringUtils.isEmpty(identity)) {
            throw new ApplicationException(Error.invalidIdentity, document.getIdentityProperty());
        }
    }

    public static void duplicatedIdentity(CustomizedDocument<?> document) {
        String identity = getIdentity(document);
        throw new ApplicationException(Error.duplicateIdentity, document.getIdentityProperty(), identity);
    }

    public static void initialModel(CustomizedDocument<?> document) {
        document.buildSequenceId();
        document.setCreateTime(DatetimeUtils.now());
    }

    public static void copyProperties(CustomizedDocument<?> src, CustomizedDocument<?> dest) {
        BeanUtils.copyProperties(src, dest, dest.getNoUpdateProperties());
    }

    public static void checkRelationChange(TxgFieldProfile updated, TxgFieldProfile curr) {
        if (!Objects.equals(curr.getTxgId(), updated.getTxgId())) {
            updated.setStartTime(new Date());
            updated.setEndTime(null);
        }
    }

    public static <T> List<T> asNonNull(List<T> list) {
        return Optional.ofNullable(list)
                       .orElse(Collections.emptyList());
    }

    public static void checkPassword(UserProfile updated, UserProfile curr) {
        if (StringUtils.isEmpty(updated.getPassword())) {
            updated.setPassword(curr.getPassword());
        }
    }
}
