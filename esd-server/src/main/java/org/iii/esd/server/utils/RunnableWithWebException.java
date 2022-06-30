package org.iii.esd.server.utils;

import org.iii.esd.exception.WebException;

public interface RunnableWithWebException {
    void run() throws WebException;
}
