package org.iii.esd.server.utils;

import java.util.concurrent.Callable;

import org.iii.esd.exception.WebException;

public interface CallableWithWebException<T> {
    T call() throws WebException;
}
