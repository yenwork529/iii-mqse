package org.iii.esd.interfaces;

import java.util.List;

public interface IResourceRelationService {
    
    public String getResourceMetaId(String fid);
    public String getFieldMetaId(String rid);
    public List<String> getFieldMetaIdList(String rid);
}
