package org.iii.esd.api.vo.gateway;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Resource<T> {

    private List<T> resources;
    
}